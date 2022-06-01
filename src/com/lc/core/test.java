//package com.lc.core;
//
//import java.util.*;
//
//
//
//public class test {
//    /**
//     * Note: 类名、方法名、参数名已经指定，请勿修改
//     *
//     *
//     * 找到数组中乘积最大的连续子数组，并返回乘积
//     * @param nums long长整型 一维数组 原始数组
//     * @return long长整型
//     */
//    public int res = Integer.MAX_VALUE;
//    public long GetMinCalculateCount(long sourceX, long sourceY, long targetX, long targetY) {
//        // write code here
//
//        bfs(sourceX,sourceY,targetX,targetY,0);
//        return  res==Integer.MAX_VALUE?-1:res;
//    }
//
//    public void bfs(long sourceX, long sourceY, long targetX, long targetY, int tmp_res){
//        if(sourceX > targetX || sourceY > targetY
//                || (sourceX == targetX && sourceY != targetY)
//                || (sourceX != targetX && sourceY == targetY)){
//            return;
//        }
//        if(sourceX == targetX && sourceY == targetY){
//            res = Math.min(res,tmp_res);
//            return;
//        }
//
//        bfs(sourceX+1,sourceY+1,targetX,targetY, tmp_res+1);
//        bfs(sourceX*2,sourceY*2,targetX,targetY, tmp_res+1);
//    }
//
//    public static void main(String[] args) {
//        System.out.println(new test().GetMinCalculateCount(2,1,3,5));
//    }
//}
