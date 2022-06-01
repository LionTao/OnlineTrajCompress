package com.lc.pojo;

import java.text.DecimalFormat;

public class enpoint implements Comparable<enpoint>{
        public long id;//点ID
        public double pe; //经度
        public double pn; //维度

        public enpoint(){ }//空构造函数

        public String toString(){
            return this.id+"#"+this.pn+","+this.pe;
        }

        public String getResultString(){
            DecimalFormat df = new DecimalFormat("0.000000");
            return df.format(this.pn)+"\t"+df.format(this.pe)+"\t" + this.id + "\n"; //反了e和n，然后分隔符改变
        }

        public boolean equals(enpoint obj) {
            return pe == obj.pe && pn == obj.pn;
        }

        @Override
        public int compareTo(enpoint other) {
            if(this.id<other.id)
                return -1;
            else if(this.id>other.id)
                return 1;
            else
                return 0;
        }
}
