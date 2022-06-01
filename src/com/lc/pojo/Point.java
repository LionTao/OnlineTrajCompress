package com.lc.pojo;
import java.io.Serializable;

public class Point implements Serializable {
    public double lon; //lon
    public double lat; //lat
    //轨迹内顺序
    long order = -1;
    //点的序号（所属轨迹的序号）
    long num = -1;
    public long time;
    public Point(){

    }

    public long getOrder() {
        return order;
    }

    public void setOrder(long order) {
        this.order = order;
    }

    public Point(Point p) {
        this.lon = p.getLon();
        this.lat = p.getLat();
        this.time = p.getTime();
    }
    public Point(double lat , double lon, long time){
        this.lon = lon;
        this.lat = lat;
        this.time = time;
    }

    public long getNum() {
        return num;
    }
    /**
     * @description 设置轨迹序号
     * @param num
     */
    public void setNum(long num) {
        this.num = num;
    }
    public double getLon() {
        return lon;
    }
    public void setLon(double x) {
        this.lon = lon;
    }
    public double getLat() {
        return lat;
    }
    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    public Point add_bin_avg(Point tmp) {
        //舍去由于取平均值带来的毫秒误差
        long t = (this.getTime() + tmp.getTime())/2;
        if((t/100) % 10 == 5){
            t -= 500;
        }
        return new Point((this.getLat()+tmp.getLat())/2.0, (this.getLon()+tmp.getLon())/2.0, t); //时间取前一个的值
    }
//
//    public Point mul(Long N) {
//        return new Point(this.getX()*N, this.getY()*N);
//    }
//
//    public Point div(Long n) {
//        return new Point(this.getX()*1.0/n, this.getY()*1.0/n);
//    }
//
//    public Double angle(Point p) {
//        return Math.atan2(p.getY()-this.getY(),p.getX()-this.getX())*180/Math.PI;
//    }
//
//    public Point min(Point p) {
//        Double min_x = this.getX()>p.getX()?p.getX():this.getX();
//        Double min_y = this.getY()>p.getY()?p.getY():this.getY();
//        return new Point(min_x,min_y);
//    }
//
//    public Point max(Point p) {
//        Double max_x = this.getX()<p.getX()?p.getX():this.getX();
//        Double max_y = this.getY()<p.getY()?p.getY():this.getY();
//        return new Point(max_x,max_y);
//    }

    @Override
    public String toString() {
        return lat + "\t" + lon + "\t" + time;
    }

}
