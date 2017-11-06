package g1.tool;


public class Tool {
    public static String getCurrentClassName(){
//        String s0 = Thread.currentThread().getStackTrace()[0].getClassName();
//        String s1 = Thread.currentThread().getStackTrace()[1].getClassName();
        String s2 = Thread.currentThread().getStackTrace()[2].getClassName();
//        System.out.println("s0="+s0+" s1="+s1+" s2="+s2);
        //s0=java.lang.Thread s1=g1.tool.Tool s2=g1.TRocketmq1Application
        return s2;
    }


    public static String getCurrentMethodName(){
//        String s0 = Thread.currentThread().getStackTrace()[0].getMethodName();
//        String s1 = Thread.currentThread().getStackTrace()[1].getMethodName();
        String s2 = Thread.currentThread().getStackTrace()[2].getMethodName();
//        System.out.println("s0="+s0+" s1="+s1+" s2="+s2);
        //s0=getStackTrace s1=getCurrentMethodName s2=run
        return s2;
    }
}
