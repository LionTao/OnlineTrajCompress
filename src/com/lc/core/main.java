package com.lc.core;

import com.lc.configuration.conf;
import com.lc.func.GeoHash;
import com.lc.func.errCalcu;
import com.lc.pojo.Point;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.*;
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import javax.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.lc.configuration.conf.*;

public class main {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(parallelism);
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
//        env.getConfig().registerPojoType(com.nocml.pojo.Trajectory.class);

        String file;
        if (args.length != 0){
//            System.out.println(111);
            file = args[0];
        }else {
            file=datasourceFile;
//            System.out.println(222);

        }

        DataStreamSource<String> dataSource = env.readTextFile(file);

        dataSource
                .map(new MapFunction<String, Tuple3<Double, Double, Long>>() { //<lat, lon, timestamp>
                    @Override
                    public Tuple3<Double, Double, Long> map(String value) throws Exception {
                        String[] tmp = value.split("\t"); //根据空格拆分，返回时调整lon，lat顺序<lon, lat, ts>
                        return new Tuple3<>(Double.parseDouble(tmp[1]), Double.parseDouble(tmp[0]), Long.parseLong(String.valueOf(new SimpleDateFormat(format).parse(tmp[2]).getTime())));
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
                .keyBy(new distributeData())
                .timeWindow(Time.seconds(winStep), Time.seconds(slideStep))
                .process(new ProcessWindowFunction<Tuple3<Double, Double, Long>, Tuple3<Point, Point, Long>, String, TimeWindow>() { //第三个string好像是key 1110
                    private ValueState<Double> errThreshold; //累计误差
                    private ValueState<Point> pre;

                    @Override
                    public void open(Configuration parameters) throws Exception { //初始化
                        ValueStateDescriptor<Double> errThresholdDescriber = new ValueStateDescriptor<Double>("errDescriber", Double.class);
                        ValueStateDescriptor<Point> preDescriber = new ValueStateDescriptor<Point>("preDescriber", Point.class);
                        errThreshold = getRuntimeContext().getState(errThresholdDescriber);
                        pre = getRuntimeContext().getState(preDescriber);
                    }

                    @Override
                    public void process(String s, Context context, Iterable<Tuple3<Double, Double, Long>> elements, Collector<Tuple3<Point, Point, Long>> out) throws Exception {
                        Double errLast = errThreshold.value();
                        Point prePoint = pre.value();//考虑省区，仅保留第一个点，和后面误差判别出来的最后一个点构成点对-->抽象线段 1110
                        for (Tuple3<Double, Double, Long> e:elements){
                            if (errLast==null ){
                                errThreshold.update(0.0); //初始化操作可能不一样，需要改1108 new errCalcu().calcFun(e)  1112
                                pre.update(new Point(e.f0, e.f1)); //时间是否应该考虑？ 1112
                            } else { //内部if是否先判断决定了开窗截止的数据是否应该放入考量，目前先不考量，即重开之后第一个数据为空，因为第一个数据算在了前面一个窗口的结尾，不参与计算 1110
                                errThreshold.update(new errCalcu().calcFun(e, prePoint, errLast)); //初始化操作可能不一样，需要改1108 由于要累计误差，因此之前的误差值也得传入，以及得传入上一个点 1112
                                if (errLast >= errBound) { //仅保留起止点。不用MQ了-->所以我们的重点应该放在设计一个增量的误差计算方式,以成对的方式输出。 1110
                                    errThreshold.update(0.0);// 清空累计误差 1112
                                    out.collect(new Tuple3<Point, Point, Long>(prePoint, new Point(e.f0, e.f1), e.f2)); //记录了后一个时间，是否需要改进？ 1110
                                }
                            }
                        }
                    }
                })
                .print();


        env.execute();
    }

    private static class distributeData implements KeySelector<Tuple3<Double, Double, Long>, String> {
        @Override
        public String getKey(Tuple3<Double, Double, Long> value) throws Exception {
            return new GeoHash().encode(value.f0, value.f1);//考虑提前到map操作--1108未做
        }
    }
}
