package Utils;

import javax.swing.*;
import java.io.UnsupportedEncodingException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by huzhejie on 2016/8/9.
 */
public class DBConnect {
	private static Connection connection;
	private static Statement statement;
	private static PreparedStatement preparedStatement;
	private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 设置日期格式;
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	public Statement getStatement() {
		return statement;
	}

	public void setStatement(Statement statement) {
		DBConnect.statement = statement;
	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		DBConnect.connection = connection;
	}

	public void connectDB() {
		// 驱动程序名
		String driver = "com.mysql.jdbc.Driver";
		// URL指向要访问的数据库名we_chat_robot
		String url = "jdbc:mysql://57e897042b666.sh.cdb.myqcloud.com:3666/we_chat_robot";
		// MySQL配置时的用户名
		String user = "cdb_outerroot";
		// MySQL配置时的密码
		String password = "hzj199429";
		try {
			// 加载驱动程序
			Class.forName(driver);
			// 连续数据库
			connection = DriverManager.getConnection(url, user, password);
			if (!connection.isClosed())
				System.out.println("Succeeded connecting to the Database!");
		} catch (ClassNotFoundException e) {
			System.out.println("Sorry,can`t find the Driver!");
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 更新个人数据库敏感词（包括群列表，群成员的信息）
	 * 
	 * @param userName
	 * @param groupName
	 * @param memberName
	 * @throws SQLException
	 */
	public int updateSenseWarn(String userName, String groupName, String memberName)
			throws SQLException, UnsupportedEncodingException {
		String sql = "SELECT * FROM " + userName + "_info WHERE groupName = '" + groupName + "' AND memberName = '"
				+ memberName + "' AND (warn != 0 OR activeDegree!=0) AND is_sign = 0";
		preparedStatement = connection.prepareStatement(sql);
		ResultSet rs = preparedStatement.executeQuery(sql);
		if (rs.next()) {
			sql = "UPDATE " + userName + "_info SET warn=warn+1 WHERE groupName = '" + groupName
					+ "' AND memberName = '" + memberName + "' AND (warn != 0 OR activeDegree!=0) AND is_sign = 0";
			statement = connection.createStatement();
			statement.execute(sql);
			return rs.getInt(5) + 1;
		} else {
			sql = "INSERT INTO " + userName + "_info VALUES ('" + groupName + "','" + memberName + "','"
					+ df.format(new Date()) + "',0,1,0)";
			statement = connection.createStatement();
			statement.execute(sql);
			return 1;
		}
	}

	/**
	 * 插入信息到个人数据库签到信息（包括群列表，群成员的信息）
	 ** 
	 * @param userName
	 *            机器人软件的用户名
	 * @param groupNumberId
	 *            群聊解密的id
	 * @param memberUin
	 *            群成员的uin
	 * @return 该月的签到次数
	 */
	public int insertSignRecord(String userName, String groupNumberId, String memberUin) throws Exception {
		String sql = "SELECT signDate FROM " + userName + "_info" + " WHERE groupName='" + groupNumberId
				+ "' AND memberName='" + memberUin + "' AND is_sign=1 order by signDate desc";
		preparedStatement = connection.prepareStatement(sql);
		ResultSet rs = preparedStatement.executeQuery(sql);
		int count = 0;
		if (rs.first()) { // 如果该用户有签到记录
			Calendar recordDate = Calendar.getInstance(); // 获取最近一次签到记录
			recordDate.setTime(sdf.parse(rs.getString("signDate")));
			Calendar now = Calendar.getInstance(); // 当前时间
			int recordYear = recordDate.get(Calendar.YEAR), nowYear = now.get(Calendar.YEAR);
			int recordMonth = recordDate.get(Calendar.MONTH) + 1, nowMonth = now.get(Calendar.MONTH) + 1;
			int recordDay = recordDate.get(Calendar.DAY_OF_MONTH), nowDay = now.get(Calendar.DAY_OF_MONTH);
			if (recordYear == nowYear) { // 如果最近一次签到记录和当前时间处于同一年
				if (recordMonth == nowMonth) { // 如果还是同一月份，则统计该月的签到记录
					if(recordDay==nowDay){
						return 0;
					}
					else {
						sql = "insert into " + userName + "_info" + "(groupName,memberName,signDate,is_sign) VALUES ('"
								+ groupNumberId + "','" + memberUin + "','" + df.format(new Date()) + "',1)";
						connection.createStatement().execute(sql);
						sql = "select count(signDate) as signCount from " + userName + "_info where groupName='"
								+ groupNumberId + "' and memberName='" + memberUin
								+ "' and is_sign=1 and signDate between "
								+ sdf.format(sdf.parse(recordYear + "-" + recordMonth + "-" + "01") + " and "
								+ sdf.format(sdf.parse(recordYear + "-" + (recordMonth + 1))));
						ResultSet rt1 = connection.createStatement().executeQuery(sql);
						if (rt1.first()) {
							count = rt1.getInt("count(signDate)");
						}
						return count;
					}
				}
				else {
					sql = "insert into " + userName + "_info" + "(groupName,memberName,signDate,is_sign) VALUES ('"
							+ groupNumberId + "','" + memberUin + "','" + df.format(new Date()) + "',1)";
					connection.createStatement().execute(sql);
					return 1;
				}
			}
			else
			{
				sql = "insert into " + userName + "_info" + "(groupName,memberName,signDate,is_sign) VALUES ('"
						+ groupNumberId + "','" + memberUin + "','" + df.format(new Date()) + "',1)";
				connection.createStatement().execute(sql);
				return 1;
			}
		} else { // 如果该用户第一次使用签到功能
			sql = "insert into " + userName + "_info" + "(groupName,memberName,signDate,is_sign) VALUES ('"
					+ groupNumberId + "','" + memberUin + "','" + df.format(new Date()) + "',1)";
			connection.createStatement().execute(sql);
			return 1;
		}
	}

	/**
	 * 统计活跃度信息
	 * 
	 * @param userName
	 *            机器人软件的用户名
	 * @param groupNumberId
	 *            群聊的解密id
	 * @param memberUin
	 *            群成员的uin
	 */
	public void mergeActiveDegree(String userName, String groupNumberId, String memberUin) throws SQLException {
		String sql = "SELECT signDate FROM " + userName + "_info" + " WHERE groupName='" + groupNumberId
				+ "' AND memberName='" + memberUin + "'AND activeDegree!=0 order by signDate desc";
		preparedStatement = connection.prepareStatement(sql);
		ResultSet rs = preparedStatement.executeQuery(sql);
		if (!rs.first()) { // 第一次记录该成员的活跃度
			statement = connection.createStatement();
			sql = "insert into " + userName + "_info" + "(groupName,memberName,signDate,activeDegree) VALUES ('"
					+ groupNumberId + "','" + memberUin + "','" + sdf.format(new Date()) + "',1)";
			statement.execute(sql);
		} else {
			String recordDate = rs.getString("signDate").split(" ")[0]; // 最近一条记录的时间
			if (recordDate.equals(sdf.format(new Date()))) // 如果是当天的活跃度
				sql = "Update " + userName + "_info set activeDegree=activeDegree+1 where groupName='" + groupNumberId
						+ "' and memberName='" + memberUin + "' and signDate='" + rs.getString("signDate") + "'";
			else
				sql = "Insert " + userName + "_info" + "(groupName,memberName,signDate,activeDegree) values('"
						+ groupNumberId + "','" + memberUin + "','" + sdf.format(new Date()) + "',1)";
			connection.createStatement().executeUpdate(sql);
		}
	}

	/**
	 * 检查用户的用户名及密码
	 * 
	 * @param userName
	 * @param passWord
	 * @throws SQLException
	 *             return
	 */
	public boolean checkLogin(String userName, String passWord) throws SQLException {
		statement = connection.createStatement();
		String sql = "SELECT * FROM userInfo WHERE userName = '" + userName + "' AND passWord = '" + passWord + "'";
		return statement.executeQuery(sql).first();
	}

	/**
	 * 设置使用用户的注册
	 * 
	 * @param userName
	 * @param passWord
	 * @throws SQLException
	 */
	public void insertRegisterRecord(String userName, String passWord) throws SQLException {
		statement = connection.createStatement();
		String sql = "insert into userInfo(userName,passWord,time,is_login) VALUES ('" + userName + "','" + passWord
				+ "','" + df.format(new Date()) + "','1')";
		statement.execute(sql);
		sql = "CREATE TABLE " + userName + "_info"
				+ "(groupName VARCHAR(127) NOT NULL,memberName VARCHAR(127) NOT NULL,signDate TIMESTAMP ,is_sign INT DEFAULT 0,warn INT DEFAULT 0, activeDegree INT DEFAULT 0, PRIMARY KEY(groupName,memberName,signDate))";
		statement.executeUpdate(sql);
	}

	/**
	 * 设置30天到期
	 * 
	 * @param info
	 * @return
	 * @throws SQLException
	 */
	public Boolean checkProgram(String info) throws SQLException {
		String sql = "SELECT p.login_date FROM pcInfo p WHERE p.info = '" + info + "'";
		preparedStatement = connection.prepareStatement(sql);
		ResultSet rs = preparedStatement.executeQuery(sql);
		if (!rs.next()) {
			sql = "INSERT INTO pcInfo(info,login_date) VALUES ('" + info + "','" + df.format(new Date()) + "')";
			statement = connection.createStatement();
			return !statement.execute(sql);
		} else {
			long d = 0l;
			try {
				d = (df.parse(df.format(new Date())).getTime() - df.parse(rs.getObject(1).toString()).getTime())
						/ (1000 * 60 * 60 * 24);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (d > 29) {
				return false;
			} else
				return true;
		}
	}
}
