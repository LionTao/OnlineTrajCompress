package com.lc.func;

public class calAngle {
    public static final double changeAngle(double p1){
        p1 = p1 % 360.0;
        if (p1<0){
            p1 += 360.0;
        }

        return p1;
    }

    public static final double diffAngle(double p1, double p2){
        if(p1>p2){
            double tmp = p1;
            p1 = p2;
            p2 = tmp; //2大1小
        }

        if((p2-p1)>180.0){
            return 360.0-p2+p1;
        }
        return p2-p1;
    }
}
