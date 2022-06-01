package com.lc.core;

import com.lc.pojo.enpoint;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.BufferedReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JFrame;
import javax.swing.JPanel;

import static com.lc.configuration.conf.*;
import static com.lc.func.Rad.Rad;
import static com.lc.func.geoDist.geoDist;

public class OPW {
    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();
        System.out.println("Start:"+start);
        //阈值定义
        double maxDistanceError = 30;
        /** 文件读取 * */
        //存放从文件读取的位置点的信息列表
        ArrayList<enpoint> ENPList = new ArrayList<enpoint>();
        //源数据文件的地址 建立文件对象 //这里是需要更改的地方 改你源文件的存放地址 记住如果地址中含"\",记得再加一个"\"，原因"\"是转义符号
        // 这里可以写成C:/Users/Administrator/Desktop/11.6/2007-10-14-GPS.log
        File sourceFile = new File(datasourceFile); //调用文件读取函数 读取文件数据
        ENPList = getENPointFromFile(sourceFile);
        //这里是测试 有没有读到里面 看看列表里的数据个数 交作业的时候记得注释掉
//        System.out.println(ENPList.size());
        /** 数据处理 * 方法：开放窗口轨迹压缩法 * */
        //存放目标点的集合
        ArrayList<enpoint> rePointList = new ArrayList<enpoint>();
        rePointList = openWindowTra(ENPList, maxDistanceError);
        System.out.println(rePointList.size());
        /** 写入目标文件 * */
        File targetFile = new File("./2007-10-14-GPSResult.log");
        writeTestPointToFile(targetFile, rePointList);
        long end = System.currentTimeMillis();
        System.out.println("consume:"+(end-start));
        /** 压缩率计算 */
        double cpL = (double) rePointList.size() / (double) ENPList.size() * 100;
        DecimalFormat df = new DecimalFormat("0.000000");
        System.out.println("压缩率：" + df.format(cpL) + "%");
        /** 计算平均距离误差 * */
        double aveDisErr = getMeanDistError(ENPList, rePointList);
        System.out.println("平均误差值："+aveDisErr);

        /** 画线形成对比图 * */
        //generateImage(ENPList,rePointList);

    }

    /**
     * 从提供的文件信息里提取位置点
     * 并将每个点的坐标数值调用转换函数存到列表里
     * 函数返回一个 存放所有位置点 的集合
     */
    public static ArrayList<enpoint> getENPointFromFile(File fGPS) throws Exception {
        ArrayList<enpoint> pGPSArray = new ArrayList<enpoint>();

        if (fGPS.exists() && fGPS.isFile()) {
            InputStreamReader read = new InputStreamReader(new FileInputStream(fGPS)); //输入流初始化
            BufferedReader bReader = new BufferedReader(read); //缓存读取初始化
            String str;
            String[] strGPS;
//            int i = 0;
            while ((str = bReader.readLine()) != null) { //每次读一行
                strGPS = str.split(",");
                enpoint p = new enpoint();
                Long s = Long.parseLong(String.valueOf(new SimpleDateFormat(format).parse(strGPS[5]).getTime()));
                p.id = s;
//                i++;

                /*
                改：我们的数据不需要转换,元数据为lat lon，这里要改成 lon lat 1228
                 */
//                p.pe = (dfTodu(strGPS[3]));
//                p.pn = (dfTodu(strGPS[5]));

                p.pe = Double.parseDouble(strGPS[0]);
                p.pn = Double.parseDouble(strGPS[1]);
                pGPSArray.add(p);
            }
            bReader.close();
        }
        return pGPSArray;
    }

    /**
     * 函数功能：将原始经纬度坐标数据转换成度
     * 获取的经纬度数据为一个字符串
     */
    public static double dfTodu(String str) {
        int indexD = str.indexOf('.'); //获取 . 字符所在的位置
        String strM = str.substring(0, indexD - 2);
        //整数部分
        String strN = str.substring(indexD - 2);
        //小数部分
        double d = Double.parseDouble(strM) + Double.parseDouble(strN) / 60;
        return d;
    }

    /**
     * 开放窗口方法实现
     * 返回一个压缩后的位置列表
     * 列表每条数据存放ID、点的坐标
     * 算法描述：
     * 初始点和浮动点计算出投影点，判断投影点和轨迹点的距离与阈值 若存在距离大于阈值
     * 则初始点放入targetList，浮动点向前检索一点作为新的初始点,新的初始点向后检索第二个作为新的浮动点
     * 这里存在判断 即新的初始点位置+1是不是等于列表长度
     * 这里决定了浮动点的选取 * 如此处理至终点
     */
    public static ArrayList<enpoint> openWindowTra(ArrayList<enpoint> sourceList, double maxDis) {
        ArrayList<enpoint> targetList = new ArrayList<enpoint>();
        //定义初始点位置 最开始初始点位置为0
        int startPoint = 0;
        //定义浮动点位置 最开始初始点位置2
        int floatPoint = 2;
        //定义当前轨迹点位置 最开始初始点位置为1
        int nowPoint = 1;
        int len = sourceList.size();
        //存放所有窗口内的点的信息集合
        ArrayList<enpoint> listPoint = new ArrayList<enpoint>();
        listPoint.add(sourceList.get(nowPoint));
        //浮动点位置决定循环
        while (true) {
            //标志 用来控制判断是否进行窗口内轨迹点更新
            Boolean flag = false;
            //计算并判断窗口内所有点和投影点的距离是否大于阈值
            for (enpoint point : listPoint) {
                double disOfTwo = getDistance(sourceList.get(startPoint), sourceList.get(floatPoint), point);
                if (disOfTwo >= OPWErr) {
                    flag = true;
                    break;
                }
            }
            if (flag) { //窗口内点距离都大于阈值
                // 初始点加到目标列表
                targetList.add(sourceList.get(startPoint));
                //初始点变化
                startPoint = floatPoint - 1;
                //浮动点变化
                floatPoint += 1;
                if (floatPoint >= len) {
                    targetList.add(sourceList.get(floatPoint - 1));
                    break;
                }
                //窗口内点变化
                listPoint.clear();
//                System.out.println(listPoint.size());
                listPoint.add(sourceList.get(startPoint + 1));
            } else { //距离小于阈值的情况
                // 初始点不变
                // 当前窗口集合加入当前浮动点
                listPoint.add(sourceList.get(floatPoint));
                //浮动点后移一位
                floatPoint += 1;
                //如果浮动点是终点 且当前窗口点距离都小于阈值 就直接忽略窗口点 直接将终点加入目标点集合
                if (floatPoint >= len) {
                    targetList.add(sourceList.get(startPoint));
                    targetList.add(sourceList.get(floatPoint - 1));
                    break;
                }
            }
            flag = false;
        }
        return targetList;
    }

    /**
     * 计算投影点到轨迹点的距离
     * 入口是初始点A、浮动点B、当前轨迹点C
     * 三角形面积公式
     */
    public static double getDistance(enpoint A, enpoint B, enpoint C) {
        double a = Math.abs(geoDist(A, B));
        double b = Math.abs(geoDist(B, C));
        double c = Math.abs(geoDist(A, C));
        double p = (a + b + c) / 2.0;
        double s = Math.sqrt(p * (p - a) * (p - b) * (p - c)); //三角形面积
//        System.out.println(distance);
        double dis = s * 2.0 / a;
        return Double.isNaN(dis)?0.0:dis;
    }

    /**
     * ArrayList 拷贝函数 *
     */
    /*提供的函数
     * * 其中计算距离的函数 经过改造得到下面的距离计算方法
     * * 具体是怎么计算距离的 我也没研究了 * */
//    public static double geoDist(enpoint pA, enpoint pB) {
//        double radLat1 = Rad(pA.pn);
//        double radLat2 = Rad(pB.pn);
//        double delta_lon = Rad(pB.pe - pA.pe);
//        double top_1 = Math.cos(radLat2) * Math.sin(delta_lon);
//        double top_2 = Math.cos(radLat1) * Math.sin(radLat2) - Math.sin(radLat1) * Math.cos(radLat2) * Math.cos(delta_lon);
//        double top = Math.sqrt(top_1 * top_1 + top_2 * top_2);
//        double bottom = Math.sin(radLat1) * Math.sin(radLat2) + Math.cos(radLat1) * Math.cos(radLat2) * Math.cos(delta_lon);
//        double delta_sigma = Math.atan2(top, bottom);
//        double distance = delta_sigma * 6378137.0;
//        return distance;
//    }

//    public static double Rad(double d) {
//        return d * Math.PI / 180.0;
//    }

    /**
     * 将压缩后的位置点信息写入到文件中 *
     */
    public static void writeTestPointToFile(File outGPSFile, ArrayList<enpoint> pGPSPointFilter) throws Exception {
        Iterator<enpoint> iFilter = pGPSPointFilter.iterator();
        RandomAccessFile rFilter = new RandomAccessFile(outGPSFile, "rw");
        while (iFilter.hasNext()) {
            enpoint p = iFilter.next();
            String sFilter = p.getResultString();
            byte[] bFilter = sFilter.getBytes();
            rFilter.write(bFilter);
        }
        rFilter.close();
    }

    /**
     * 函数功能：求平均距离误差
     * 返回平均距离
     */
    public static double getMeanDistError( ArrayList<enpoint> pGPSArray,ArrayList<enpoint> pGPSArrayRe) {
        double sumDist = 0.0;
        int index = 0;
        for (int i = 1; i < pGPSArrayRe.size(); i++) {
//            int start = pGPSArrayRe.get(i-1).id;

            long end = pGPSArrayRe.get(i).id; //取出原始的前俩个
//            System.out.println("wai"+end);
            while (pGPSArray.get(index).id < end && index < pGPSArray.size()) { //由于记录的是后一个的时间，因此要小于不能等于
                //存在NaN异常
                sumDist += getDistance(pGPSArrayRe.get(i - 1), pGPSArrayRe.get(i), pGPSArray.get(index));
//                System.out.println(getDistance(pGPSArrayRe.get(i - 1), pGPSArrayRe.get(i), pGPSArray.get(index)));
                index++;
//                System.out.println(index);
            }
        }
        return sumDist / (pGPSArray.size() * 1.0);
    }
}