package com.lc.pojo;

import java.io.Serializable;

public class Line implements Serializable {
    Point s = new Point();
    Point e = new Point();
    //行顺序
    long order = -1;//改为时间

    //轨迹id？？
    String num = null;
    int classifiy = 0;

    public Line() {
    }
    public Line(Point s , Point e) {
        this.s = s;
        this.e = e;
    }

    public long getOrder() {
        return order;
    }


    public void setOrder(long order) {
        this.order = order;
    }


    public int getClassifiy() {
        return classifiy;
    }


    public void setClassifiy(int classifiy) {
        this.classifiy = classifiy;
    }

    public Point getS() {
        return s;
    }

    public void setS(Point s) {
        this.s = s;
    }

    public Point getE() {
        return e;
    }

    public void setE(Point e) {
        this.e = e;
    }

    public String getNum() {
        return num;
    }

    /**
     * @description 设置读入时的原始行号
     * @param num
     */
    public void setNum(String num) {
        this.num = num;
    }


    @Override
    public String toString() {
        return order + "," + num + "," + s +","+ e;
    }




}
