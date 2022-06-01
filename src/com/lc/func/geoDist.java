package com.lc.func;

import com.lc.pojo.enpoint;
import com.lc.pojo.Point;

import static com.lc.configuration.conf.earth_r;
import static com.lc.func.Rad.Rad;

public class geoDist {
    public static double geoDist(enpoint pA, enpoint pB) {
        double radLat1 = Rad(pA.pn);
        double radLat2 = Rad(pB.pn);
        double delta_lon = Rad(pB.pe - pA.pe);
        double top_1 = Math.cos(radLat2) * Math.sin(delta_lon);
        double top_2 = Math.cos(radLat1) * Math.sin(radLat2) - Math.sin(radLat1) * Math.cos(radLat2) * Math.cos(delta_lon);
        double top = Math.sqrt(top_1 * top_1 + top_2 * top_2);
        double bottom = Math.sin(radLat1) * Math.sin(radLat2) + Math.cos(radLat1) * Math.cos(radLat2) * Math.cos(delta_lon);
        double delta_sigma = Math.atan2(top, bottom);
        double distance = delta_sigma * 6378137.0;
        return distance;
    }

    public static double geoDist(Point pA, Point pB) {
        double radLat1 = Rad(pA.lat);
        double radLat2 = Rad(pB.lat);
        double delta_lon = Rad(pB.lon - pA.lon);
        double top_1 = Math.cos(radLat2) * Math.sin(delta_lon);
        double top_2 = Math.cos(radLat1) * Math.sin(radLat2) - Math.sin(radLat1) * Math.cos(radLat2) * Math.cos(delta_lon);
        double top = Math.sqrt(top_1 * top_1 + top_2 * top_2);
        double bottom = Math.sin(radLat1) * Math.sin(radLat2) + Math.cos(radLat1) * Math.cos(radLat2) * Math.cos(delta_lon);
        double delta_sigma = Math.atan2(top, bottom);
        double distance = delta_sigma * earth_r; //m
        return distance;
    }
}
