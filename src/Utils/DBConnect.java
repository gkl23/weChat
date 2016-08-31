package Utils;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by huzhejie on 2016/8/9.
 */
public class DBConnect {
    private static Connection connection;
    private static Statement statement;
    private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式;

    public static Statement getStatement() {
        return statement;
    }

    public static void setStatement(Statement statement) {
        DBConnect.statement = statement;
    }

    public static Connection getConnection() {
        return connection;
    }

    public static void setConnection(Connection connection) {
        DBConnect.connection = connection;
    }

    public void connectDB(){
        // 驱动程序名
        String driver = "com.mysql.jdbc.Driver";
        // URL指向要访问的数据库名we_chat_robot
        String url = "jdbc:mysql://57a81a9d2651c.sh.cdb.myqcloud.com:8243/we_chat_robot";
        // MySQL配置时的用户名
        String user = "cdb_outerroot";
        // MySQL配置时的密码
        String password = "hzj199429";
        try {
            // 加载驱动程序
            Class.forName(driver);
            // 连续数据库
            connection = DriverManager.getConnection(url, user, password);
            if(!connection.isClosed())
                System.out.println("Succeeded connecting to the Database!");
        } catch(ClassNotFoundException e) {
            System.out.println("Sorry,can`t find the Driver!");             e.printStackTrace();
        } catch(SQLException e) {
            e.printStackTrace();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    public void addTable(String userName)throws SQLException{
        statement = connection.createStatement();
        String sql = "create table "+userName+"_sign(userName varchar(255),groupName varchar(255),time timestamp DEFAULT CURRENT_TIMESTAMP,id INT PRIMARY KEY AUTO_INCREMENT);";
        statement.execute(sql);
    }
    public void insertRegisterRecord(String userName,String groupName)throws SQLException{
        statement = connection.createStatement();
        String sql = "insert into "+userName+"_sign(userName,groupName) VALUES ('"+userName+"','"+groupName+"')";
        statement.execute(sql);
    }
    public static void main(String args[]){
        DBConnect dbConnect = new DBConnect();
        dbConnect.connectDB();
        try {
//            dbConnect.addTable("lala");
            dbConnect.insertRegisterRecord("lala","lalala");
            connection.close();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }
}

