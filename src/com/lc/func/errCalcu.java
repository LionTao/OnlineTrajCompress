package com.lc.func;

import com.lc.pojo.Line;
import com.lc.pojo.Point;

import static com.lc.configuration.conf.beta;

public class errCalcu {

    public static double errCalcu(Line line) {
        double err = 0.0;

//        if (errLast == null){
//            errLast = 0.0;
//        }
//        象限判断
        double lng_b = line.getE().getLon();
        double lng_a = line.getS().getLon();
        double lat_b = line.getE().getLat();
        double lat_a = line.getS().getLat();

        err = (Math.atan2(line.getE().getLat() - line.getS().getLat()
                        , line.getE().getLon() - line.getS().getLat())*180)/Math.PI;

        double y = Math.sin(lng_b-lng_a) * Math.cos(lat_b);
        double x = Math.cos(lat_a)*Math.sin(lat_b) - Math.sin(lat_a)*Math.cos(lat_b)*Math.cos(lng_b-lng_a);
        err = Math.atan2(y, x)*180/Math.PI;
        err = err%360;
        if(err<0.0){
            err += 360.0;
        }
        return err;
//        double lng_b = line.getE().getLon()*Math.PI/180;
//        double lng_a = line.getS().getLon()*Math.PI/180;
//        double lat_b = line.getE().getLat()*Math.PI/180;
//        double lat_a = line.getS().getLat()*Math.PI/180;

//        double d = Math.sin(lat_a)*Math.sin(lat_b)+Math.cos(lat_a)*Math.cos(lat_b)*Math.cos(lng_b-lng_a);
//        d = Math.sqrt(1-d*d);
//        d = Math.cos(lat_b)*Math.sin(lng_b-lng_a)/d;
//        d = Math.asin(d)*180/Math.PI;
//
//        if(Double.isNaN(d)){
//            if(lng_a <= lng_b){
//                d = 90.0;
//            }else{
//                d = 270.0;
//            }
//        } else if(d<0){
//            d += 360.0;
//        }

//        double deltaFI = Math.log(Math.tan(lat_b / 2 + Math.PI / 4) / Math.tan(lat_a / 2 + Math.PI / 4));
//
//        double deltaLON = Math.abs(lng_a - lng_b) % 180;
//
//        double theta = Math.atan2(deltaLON, deltaFI);
//
//        return Math.toDegrees(theta);
//        return d;

//        double lng_b = line.getE().getLon();
//        double lng_a = line.getS().getLon();
//        double lat_b = line.getE().getLat();
//        double lat_a = line.getS().getLat();
//
//        double lat1 = lat_a, lon1 = lng_a, lat2 = lat_b,
//                lon2 = lng_b;
//        double result = 0.0;
//
//        int ilat1 = (int) (0.50 + lat1 * 360000.0);
//        int ilat2 = (int) (0.50 + lat2 * 360000.0);
//        int ilon1 = (int) (0.50 + lon1 * 360000.0);
//        int ilon2 = (int) (0.50 + lon2 * 360000.0);
//
//        lat1 = Math.toRadians(lat1);
//        lon1 = Math.toRadians(lon1);
//        lat2 = Math.toRadians(lat2);
//        lon2 = Math.toRadians(lon2);
//
//        if ((ilat1 == ilat2) && (ilon1 == ilon2)) {
//            return result;
//        } else if (ilon1 == ilon2) {
//            if (ilat1 > ilat2)
//                result = 180.0;
//        } else {
//            double c = Math
//                    .acos(Math.sin(lat2) * Math.sin(lat1) + Math.cos(lat2)
//                            * Math.cos(lat1) * Math.cos((lon2 - lon1)));
//            double A = Math.asin(Math.cos(lat2) * Math.sin((lon2 - lon1))
//                    / Math.sin(c));
//            result = Math.toDegrees(A);
//            if ((ilat2 > ilat1) && (ilon2 > ilon1)) {
//            } else if ((ilat2 < ilat1) && (ilon2 < ilon1)) {
//                result = 180.0 - result;
//            } else if ((ilat2 < ilat1) && (ilon2 > ilon1)) {
//                result = 180.0 - result;
//            } else if ((ilat2 > ilat1) && (ilon2 < ilon1)) {
//                result += 360.0;
//            }
//        }
//        return result;

    }

    public static void main(String[] args) { //注意大于180度取小的那个
        System.out.println(errCalcu(new Line(new Point(30	,120, 0L)
                ,new Point(50,110, 0L))));
    }
}

//new Point(39.982414713	,116.3050169415, 0L)
//        ,new Point(39.982414830500005,116.305020581000010,
