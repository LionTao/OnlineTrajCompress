package com.lc.pojo;
import java.io.Serializable;

public class Point implements Serializable {
    public double x;
    public double y;
    //轨迹内顺序
    long order = -1;
    //点的序号（所属轨迹的序号）
    long num = -1;
    public Point(){

    }

    public long getOrder() {
        return order;
    }

    public void setOrder(long order) {
        this.order = order;
    }

    public Point(Point p) {
        this.x = p.getX();
        this.y = p.getY();
    }
    public Point(double x , double y){
        this.x = x;
        this.y = y;
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
    public double getX() {
        return x;
    }
    public void setX(double x) {
        this.x = x;
    }
    public double getY() {
        return y;
    }
    public void setY(double y) {
        this.y = y;
    }

    public Point add(Point tmp) {
        return new Point(this.getX()+tmp.getX(), this.getY()+tmp.getY());
    }

    public Point mul(Long N) {
        return new Point(this.getX()*N, this.getY()*N);
    }

    public Point div(Long n) {
        return new Point(this.getX()*1.0/n, this.getY()*1.0/n);
    }

    public Double angle(Point p) {
        return Math.atan2(p.getY()-this.getY(),p.getX()-this.getX())*180/Math.PI;
    }

    public Point min(Point p) {
        Double min_x = this.getX()>p.getX()?p.getX():this.getX();
        Double min_y = this.getY()>p.getY()?p.getY():this.getY();
        return new Point(min_x,min_y);
    }

    public Point max(Point p) {
        Double max_x = this.getX()<p.getX()?p.getX():this.getX();
        Double max_y = this.getY()<p.getY()?p.getY():this.getY();
        return new Point(max_x,max_y);
    }

    @Override
    public String toString() {
        return x + "," + y;
    }

}
