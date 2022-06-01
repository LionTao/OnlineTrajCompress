package com.lc.core;

import com.lc.configuration.conf;
import com.lc.pojo.Line;
import com.lc.pojo.Point;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.flink.util.Collector;

import javax.annotation.Nullable;
import java.text.SimpleDateFormat;

import static com.lc.configuration.conf.*;
import static com.lc.func.calAngle.changeAngle;
import static com.lc.func.calAngle.diffAngle;
import static com.lc.func.errCalcu.errCalcu;
import static com.lc.func.geoDist.geoDist;

public class main {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(parallelism);
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
//        env.getConfig().registerPojoType(com.nocml.pojo.Trajectory.class);

        String file;
        if (args.length != 0) {
//            System.out.println(111);
            file = args[0];
        } else {
            file = datasourceFile;
//            System.out.println(222);

        }

        DataStreamSource<String> dataSource = env.readTextFile(file);

        dataSource
                .map(new MapFunction<String, Tuple3<Double, Double, Long>>() { //<lat, lon, timestamp>
                    @Override
                    public Tuple3<Double, Double, Long> map(String value) throws Exception {
                        String[] tmp = value.split(","); //根据空格拆分，返回时调整lon，lat顺序<lon, lat, ts> --> 1215更改为lat lon，方便后续计算
                        return new Tuple3<>(Double.parseDouble(tmp[1]), Double.parseDouble(tmp[0]), Long.parseLong(String.valueOf(new SimpleDateFormat(format).parse(tmp[5]).getTime())));
                    }
                })
                .assignTimestampsAndWatermarks(new AssignerWithPeriodicWatermarks<Tuple3<Double, Double, Long>>() {
                    long currentMaxTimeStamp = conf.currentMaxTimeStamp;
                    final long maxOutOfOrderness = conf.maxOutOfOrderness;

                    @Nullable
                    @Override
                    public Watermark getCurrentWatermark() {
                        return new Watermark(currentMaxTimeStamp - maxOutOfOrderness);
                    }

                    @Override
                    public long extractTimestamp(Tuple3<Double, Double, Long> element, long previousElementTimestamp) {
                        long timestamp = element.f2;
                        this.currentMaxTimeStamp = Math.max(timestamp, this.currentMaxTimeStamp);
                        return timestamp;
                    }
                })
                .keyBy(new distributeData())//可能不需要keyby，因为是单一轨迹线的压缩，分布式了反而不方便 1210 或者根据Tid划分key
                .process(new ProcessFunction<Tuple3<Double, Double, Long>, String>() {
                    private ValueState<Point> pre;
                    private ValueState<Point> startPoint;
                    private ValueState<Point> compressedPoint;
                    private ValueState<Line> line;
                    private ValueState<Double> avg; //0304
                    private ValueState<Double> last; //0304
                    private ValueState<Integer> countProcess; //一定要有3个点才能构成数据 1213

                    @Override
                    public void open(Configuration parameters) throws Exception { //初始化
                        ValueStateDescriptor<Point> preDescriber = new ValueStateDescriptor<Point>("preDescriber", Point.class);
                        ValueStateDescriptor<Point> startPointDescriber = new ValueStateDescriptor<Point>("startPointDescriber", Point.class);
                        ValueStateDescriptor<Point> compressedPointDescriber = new ValueStateDescriptor<Point>("compressedPointDescriber", Point.class);
                        ValueStateDescriptor<Line> lineDescriber = new ValueStateDescriptor<Line>("lineDescriber", Line.class);
                        ValueStateDescriptor<Double> avgDescriber = new ValueStateDescriptor<Double>("avgDescriber", Double.class);
                        ValueStateDescriptor<Double> lastDescriber = new ValueStateDescriptor<Double>("lastDescriber", Double.class);
                        ValueStateDescriptor<Integer> countDescriber = new ValueStateDescriptor<Integer>("countDescriber", Integer.class);

                        pre = getRuntimeContext().getState(preDescriber);
                        startPoint = getRuntimeContext().getState(startPointDescriber);
                        compressedPoint = getRuntimeContext().getState(compressedPointDescriber);
                        line = getRuntimeContext().getState(lineDescriber);
                        avg = getRuntimeContext().getState(avgDescriber);
                        last = getRuntimeContext().getState(lastDescriber);
                        countProcess = getRuntimeContext().getState(countDescriber);
                    }

                    @Override
                    public void processElement(Tuple3<Double, Double, Long> e, Context context, Collector<String> out) throws Exception {
                        Point prePoint = pre.value();//-->参考草稿，现在理解为抽象轨迹线短的末端点所在线点前一个实点pre 1211
                        Integer count = countProcess.value();
                        int flag = 0;
                        if (count == null || count < 1) { //errLast==null || abstract_traj.getE() == null
//                            System.out.println("State0");
                            countProcess.update(1); //状态变换
                            Point t = new Point(e.f0, e.f1, e.f2);
                            pre.update(t); //时间是否应该考虑？ 1112
                            compressedPoint.update(t);
                            out.collect(pre.value().toString()); //0304
                        } //内部if是否先判断决定了开窗截止的数据是否应该放入考量，目前先不考量，即重开之后第一个数据为空，因为第一个数据算在了前面一个窗口的结尾，不参与计算 1110
                        else if (count == 1) {
//                            System.out.println("State1");//0304 下面全部注释

                            countProcess.update(2); //状态变换
//                            startPoint.update(prePoint); //0304
                            Point t = new Point(e.f0, e.f1, e.f2);
                            line.update(new Line(null, prePoint.add_bin_avg(t))); //先更新at，这样才好区分now点和pre点，第一次初始化的时候，pre更新为null 1213
//                            startPoint.update(prePoint.add_bin_avg(new Point(e.f0, e.f1, e.f2)));
                            pre.update(t);
                        } else if (count == 2) { //count意味着构造初始的航向线段的点数有了几个
//                            System.out.println("state2");
                            countProcess.update(3);//状态变换,防止第一次生成完整的at就进行 err计算，此时的err为正常的圆周角，一定偏大的，会记录该点。
                            Line abstract_traj = line.value();
                            Point start = abstract_traj.getE();
                            Point t = new Point(e.f0, e.f1, e.f2);
                            line.update(new Line(start, prePoint.add_bin_avg(t))); //上一轮的末端值作为新线段的起点,非初始阶段外，start点由已输出点线段决定
                            pre.update(t); //0304
                            double l = errCalcu(line.value()); //0305 传出的必为0-360
                            last.update(l); //0304
                            avg.update(l); //0304 first time,内部累加，外部除法 0306 第一次更新范围必定合法
                        } else {
//                            System.out.println("in");
                            Line abstract_traj = line.value();
                            double lv = last.value();
                            double av = changeAngle(avg.value());
                            double cv = countProcess.value() - 2;
                            Point lastSaved = compressedPoint.value();
                            Point ep = new Point(e.f0, e.f1, e.f2);
                            line.update(new Line(abstract_traj.getE(), prePoint.add_bin_avg(ep))); //上一轮的末端值作为新线段的起点
                            double err = errCalcu(line.value()); //0304 计算当前航向角
                            double errAvg = (av / cv * 1.0); //0304 历史航向值，在avg状态中记录历史的总计累加值
                            double errDelta = diffAngle(err, lv); //0304 大于180，就取小角，作为偏向误差
                            double realErr = diffAngle(errCalcu(new Line(lastSaved, prePoint)), errCalcu(new Line(prePoint, ep))); //考虑a点是否用摘要线的头点

//                            System.out.println("errdelta:"+errDelta+ "\t errAvg"+errAvg+"\n" + "err:" + err + "\t" + "lv:" + lv + "\t" + "av:" + av + "\t" + "cv:" + cv ); //0304
////                            System.out.println(errPercent); //0304

                            countProcess.update(countProcess.value() + 1);//0304
                            avg.update(err + av); //范围可能不合法，需要更改，这里只是简单的累加，需要注意为0-360的累加，这样除法才能平均在合法范围内
                            last.update(err);

                            //0304 仅保留起止点。不用MQ了-->所以我们的重点应该放在设计一个增量的误差计算方式,以成对的方式输出。 1110

                            //prePoint 为抽象线段的所在点的前一个点 2
                            //ep 为后一个点 3 //predecessor
                            //sp为第一个点 1 //
//                            判断是否与上次保存的点过近，每次在out中的地方保存
//                            double compare = localDist*Math.pow(Math.E,-cv/20);
                            double compare = localDist;
//                            if(realErr >= 1.5*errBound ){
//                                System.out.println("nested0");
//                                out.collect(ep.toString());
//                                compressedPoint.update(prePoint);
//                                countProcess.update(1);
//                                pre.update(ep);
//                                flag = 1;
//                            }else
                            if (errDelta >= errBound) { //0304 下面全是，急剧点检测
//                                Point ep = new Point(e.f0, e.f1, e.f2);
//                                System.out.println("nested1");
//                                Point lastSaved = compressedPoint.value();
                                if (Math.abs(geoDist(lastSaved, prePoint)) >= compare) {
//                                    System.out.println("nested3");
//                                        out.collect(sp.toString());
                                    out.collect(prePoint.toString()); //针对连续的abc三点，ab间隔很远，此时b急转，取了中点线会导致转向角度稀疏，无法检测到该角度，因此需要计算原始到偏转角度。
//                                        out.collect(ep.toString()); //lat, lon 0304
                                    //compress.update(new Line(prePoint, ep)); //0304
//                                        count = 1; //回归状态，有一个点的状态
                                    compressedPoint.update(prePoint);
                                    countProcess.update(1);
                                    pre.update(ep);
                                    flag = 1;
                                }
                            } else if (Math.abs(err - errAvg) >= errBound) {// 0304 if else
//                                    errThreshold.clear();// 清空累计误差 1112 => 保持局部相关 / 如果不清楚就是全局，全局的化要进行选择性衰退，实验比较的第二个点，第一个点在于保存out的输出是否带尾巴，目前暂时不考虑带尾巴
//                                Point ep = new Point(e.f0, e.f1, e.f2);
//                                System.out.println("nested1");
//                                Point lastSaved = compressedPoint.value();
                                //数据输出
                                if (Math.abs(geoDist(lastSaved, ep)) >= compare) {

//                                    System.out.println("nested4");
//                                        out.collect(sp.toString()); // 0304
                                    out.collect(ep.toString()); //lat, lon
                                    //compress.update(new Line(sp, ep)); //0304
//                                        count = 1;
                                    compressedPoint.update(ep);
                                    countProcess.update(1);//回归状态，有一个点的状态
                                    pre.update(ep);
                                    flag = 1;
                                }
                            }

                            if (flag == 0) { //若无作为
                                pre.update(new Point(e.f0, e.f1, e.f2));
                            }
                        }
                    }
                })
                .writeAsText(outputFileName, FileSystem.WriteMode.OVERWRITE);
//                .print();
        long start = System.currentTimeMillis();
//        System.out.println("Start:"+start);
        env.execute("1");
        long end = System.currentTimeMillis();
//        System.out.println("Total time consumed:" + (end-start));
    }

    private static class distributeData implements KeySelector<Tuple3<Double, Double, Long>, String> {
        @Override
        public String getKey(Tuple3<Double, Double, Long> value) throws Exception {
            return "1";//考虑提前到map操作--1108未做
        }
    }
}
