package com.lc.configuration;

public class conf {
    public static final int grid = 5;
    public static final int parallelism = 1; // standone test
    public static String dataNo = "100";
    public static final double earth_r = 6378137.0; //m
    ///Users/chenliang/Downloads/GeolifeTrajectories1.3/Data/100/Trajectory/20110809143636_newx100.csv
    //20070727144059_new
    public static final String datasourceFile = "/mnt/g/Data/Geolife Trajectories 1.3/Data/" + dataNo + "/Trajectory" + "/x10.csv";
    public static final double min_lat = 0.0;
    public static final double max_lat = 0.0;
    public static final double min_lon = 0.0;
    public static final double max_lon = 0.0;
    public static final String format = "yyyy-MM-dd HH:mm:ss";
    public static final long currentMaxTimeStamp = 0L;
    public static final long maxOutOfOrderness = 500L;
    public static final int winStep = 6;
    public static final int slideStep = 0;
    // 30-50
    public static final double errBound = 30.0; //目前预设累计误差为0，待调整1108 //35.00压缩率为0.8% 累积误差
    public static final double errBoundPercent = 0.8; //相对急剧变化误差
    public static final int OPWErr = 3; //1：15.523285%，0.3698344001442652 3：压缩率：5.858788% 平均误差值：1.190429350291254
    public static final double localDist = 5.0; //m
    public static final double beta = 0.4; //过去值的影响
    public static final String outputFileName = "logs/res1213";


}
