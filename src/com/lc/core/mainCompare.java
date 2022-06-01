package com.lc.core;

import com.lc.pojo.enpoint;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import static com.lc.configuration.conf.*;
import static com.lc.core.OPW.getENPointFromFile;
import static com.lc.core.OPW.getMeanDistError;

public class mainCompare {
    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();
        System.out.println("Start:" + start);
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
        System.out.println(ENPList.size());
        /** 数据处理 * 方法：开放窗口轨迹压缩法 * */
        //存放目标点的集合
        ArrayList<enpoint> rePointList = new ArrayList<enpoint>();
        File outputFile = new File(outputFileName); //调用文件读取函数 读取文件数据
        rePointList = getENPointFromOutputFile(outputFile); //第二次读读时候不用simpleformat
        /** 压缩率计算 */
        System.out.println(rePointList.size());

        double cpL = (double) rePointList.size() / (double) ENPList.size() * 100;
        DecimalFormat df = new DecimalFormat("0.000000");
        System.out.println("压缩率：" + df.format(cpL) + "%");
        /** 计算平均距离误差 * */
        double aveDisErr = getMeanDistError(ENPList, rePointList);
        System.out.println(aveDisErr);
        /** 画线形成对比图 * */
        //generateImage(ENPList,rePointList);
    }

    public static ArrayList<enpoint> getENPointFromOutputFile(File fGPS) throws Exception {
        ArrayList<enpoint> pGPSArray = new ArrayList<enpoint>();

        if (fGPS.exists() && fGPS.isFile()) {
            InputStreamReader read = new InputStreamReader(new FileInputStream(fGPS)); //输入流初始化
            BufferedReader bReader = new BufferedReader(read); //缓存读取初始化
            String str;
            String[] strGPS;
//            int i = 0;
            while ((str = bReader.readLine()) != null) { //每次读一行
                strGPS = str.split("\t");
                enpoint p = new enpoint();
                p.id = Long.parseLong(strGPS[2]);
//                i++;

                /*
                改：我们的数据不需要转换,元数据为lat lon，这里要改成 lon lat 1228
                 */
//                p.pe = (dfTodu(strGPS[3]));
//                p.pn = (dfTodu(strGPS[5]));

                p.pe = Double.parseDouble(strGPS[1]);
                p.pn = Double.parseDouble(strGPS[0]);
                pGPSArray.add(p);
            }
            bReader.close();
        }
        return pGPSArray;
    }
}