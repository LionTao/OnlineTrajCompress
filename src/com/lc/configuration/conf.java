package com.lc.configuration;

public class conf {
    public static final int grid = 5;
    public static final int parallelism = Math.min(grid * grid, 10); // standone test
    public static String dataNo = "100";
    public static final String datasourceFile = "/Users/chenliang/Downloads/Geolife Trajectories 1.3/Data/"+dataNo+"/Trajectory"+"/20110809143636_new.csv";
    public static final double min_lat = 0.0;
    public static final double max_lat = 0.0;
    public static final double min_lon = 0.0;
    public static final double max_lon = 0.0;
    public static final String format = "yyyy-MM-dd HH:mm:ss";
    public static final long currentMaxTimeStamp = 0L;
    public static final long maxOutOfOrderness = 500L;
    public static final int winStep = 360;
    public static final int slideStep = 180;
    public static final double errBound = 0.00; //目前预设累计误差为0，待调整1108
}
