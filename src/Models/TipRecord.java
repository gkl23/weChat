package Models;

/**
 * Created by huzhejie on 2016/8/8.
 */
public class TipRecord {
    private static Boolean flag;
    private static String time;
    private static String groupName;
    private static String property;
    public TipRecord(){
        flag=false;
        time = "";
        groupName="";
        property="";
    }

    public static Boolean getFlag() {
        return flag;
    }

    public static void setFlag(Boolean flag) {
        TipRecord.flag = flag;
    }

    public static String getTime() {
        return time;
    }

    public static void setTime(String time) {
        TipRecord.time = time;
    }

    public static String getGroupName() {
        return groupName;
    }

    public static void setGroupName(String groupName) {
        TipRecord.groupName = groupName;
    }

    public static String getProperty() {
        return property;
    }

    public static void setProperty(String property) {
        TipRecord.property = property;
    }
}
