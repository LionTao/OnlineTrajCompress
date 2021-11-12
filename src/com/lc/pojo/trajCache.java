package com.lc.pojo;

import org.apache.flink.api.java.tuple.Tuple3;

import java.io.Serializable;
import java.util.PriorityQueue;

public class trajCache implements Serializable {

    private PriorityQueue<Tuple3<Double, Double, Long>> cache;

    public trajCache(){

    }

    public trajCache(PriorityQueue<Tuple3<Double, Double, Long>> pq){

    }

    public PriorityQueue<Tuple3<Double, Double, Long>> getCache() {
        return cache;
    }

    public void setCache(PriorityQueue<Tuple3<Double, Double, Long>> cache) {
        this.cache = cache;
    }

    @Override
    public String toString() {
        StringBuffer s = new StringBuffer();
        while (!cache.isEmpty()){
            Tuple3<Double, Double, Long> tmp = cache.poll();
            s.append("[lon:"+tmp.f0+"\tlat:"+tmp.f1+"\ttimestamp:"+tmp.f2+"]\n");
        }
        return "trajCache{" +
                "cache=\n" + s +
                '}';
    }
}
