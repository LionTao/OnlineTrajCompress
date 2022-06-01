package com.lc.core;

import com.lc.configuration.conf;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks;
import org.apache.flink.streaming.api.watermark.Watermark;

import javax.annotation.Nullable;

import java.text.SimpleDateFormat;

import static com.lc.configuration.conf.format;
import static com.lc.configuration.conf.parallelism;

public class sample {
    public static void main(String[] args) {
        String FileName = "/Users/chenliang/Downloads/data/10";

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(parallelism);
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        DataStreamSource<String> dataSource = env.readTextFile(FileName);

        dataSource.map(new MapFunction<String, Tuple3<Double, Double, Long>>() { //<lat, lon, timestamp>
                           @Override
                           public Tuple3<Double, Double, Long> map(String value) throws Exception {
                               String[] tmp = value.split("\t"); //根据空格拆分，返回时调整lon，lat顺序<lon, lat, ts>
                               return new Tuple3<>(Double.parseDouble(tmp[1]), Double.parseDouble(tmp[0]), Long.parseLong(String.valueOf(new SimpleDateFormat(format).parse(tmp[2]).getTime())));
                           }
                       }
                       )
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
                });
    }
}
