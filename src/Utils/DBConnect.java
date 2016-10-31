package Utils;

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
	private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 设置日期格式;
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	private String startTime, endTime; // 活跃榜和签到榜的时间段

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
			statement = connection.createStatement();
			// if (!connection.isClosed())
			// System.out.println("Succeeded connecting to the Database!");
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
	 * 更新群成员的敏感词记录次数
	 * 
	 * @param userName
	 *            机器人用户名
	 * @param groupNumberId
	 *            群聊解密id
	 * @param memberUin
	 *            群成员uin
	 * @param groupName
	 *            群昵称
	 * @param memberName
	 *            群成员昵称
	 * @return 返回该群成员今天的敏感词记录次数
	 * @throws SQLException
	 */
	public int updateSenseWarn(String userName, String groupNumberId, String memberUin, String groupName,
			String memberName) throws SQLException, UnsupportedEncodingException {
		String sql = "SELECT * FROM " + userName + "_info WHERE groupId = '" + groupNumberId + "' AND memberUin = '"
				+ memberUin + "' AND activeDegree>0 order by signDate desc";
		ResultSet rs = statement.executeQuery(sql);
		rs.first();
		int warnCount = rs.getInt("warn") + 1;
		sql = "update " + userName + "_info set warn=warn+1,groupName='" + groupName + "',memberName='" + memberName
				+ "' where groupId='" + groupNumberId + "' and memberUin='" + memberUin + "' and signDate='"
				+ rs.getString("signDate") + "'";
		statement.executeUpdate(sql);
		return warnCount;
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
	 * @param groupName
	 *            群昵称
	 * @param memberName
	 *            群成员昵称
	 * @return 返回一个list，第一个表示是否已经签到，第二个表示今日的签到时间（如果已经签到），第三个表示本月的签到排名，
	 *         第四个表示本月的签到次数
	 */
	public List<String> insertSignRecord(String userName, String groupNumberId, String memberUin, String groupName,
			String memberName) throws SQLException {
		String sql = "SELECT signDate,is_sign FROM " + userName + "_info" + " WHERE groupId='" + groupNumberId
				+ "' AND memberUin='" + memberUin + "' AND activeDegree>0 order by signDate desc";
		ResultSet rs = statement.executeQuery(sql);
		List<String> result = new ArrayList<String>(), temp;
		rs.first();
		String signDate = df.format(rs.getTimestamp(1));
		if (rs.getInt(2) == 0) { // 如果今天没有签到
			sql = "update " + userName + "_info set signDate='" + df.format(new Date()) + "',is_sign=1,groupName='"
					+ groupName + "',memberName='" + memberName + "' where groupId='" + groupNumberId
					+ "' and memberUin='" + memberUin + "' and signDate='" + signDate + "'";
			statement.executeUpdate(sql);
			result.add("0");
		} else
			result.add("1");
		result.add(signDate);
		temp = getSignRank(userName, groupNumberId, memberUin, TIMETYPE.MONTH);
		result.add(temp.get(0));
		result.add(temp.get(1));
		return result;
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
	 * @param groupName
	 *            群昵称
	 * @param memberName
	 *            群成员昵称
	 */
	public void mergeActiveDegree(String userName, String groupNumberId, String memberUin, String groupName,
			String memberName) throws SQLException {
		String sql = "SELECT signDate FROM " + userName + "_info" + " WHERE groupId='" + groupNumberId
				+ "' AND memberUin='" + memberUin + "'AND activeDegree>0 order by signDate desc";
		ResultSet rs = statement.executeQuery(sql);
		if (!rs.first()) // 第一次记录该成员的活跃度
			sql = "insert into " + userName + "_info VALUES ('" + groupNumberId + "','" + memberUin + "','"
					+ df.format(new Date()) + "','" + groupName + "','" + memberName + "',0,0,1)";
		else {
			if (sdf.format(rs.getDate(1)).equals(sdf.format(new Date()))) // 如果是当天的活跃度
				sql = "Update " + userName + "_info set activeDegree=activeDegree+1,groupName='" + groupName
						+ "',memberName='" + memberName + "' where groupId='" + groupNumberId + "' and memberUin='"
						+ memberUin + "' and signDate='" + rs.getString(1) + "'";
			else
				sql = "Insert " + userName + "_info values('" + groupNumberId + "','" + memberUin + "','"
						+ df.format(new Date()) + "','" + groupName + "','" + memberName + "',0,0,1)";
		}
		statement.executeUpdate(sql);
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
		String sql = "insert into userInfo(userName,passWord,time,is_login) VALUES ('" + userName + "','" + passWord
				+ "','" + df.format(new Date()) + "','1')";
		statement.executeUpdate(sql);
		sql = "CREATE TABLE " + userName + "_info"
				+ "(groupId VARCHAR(127) NOT NULL,memberUin VARCHAR(127) NOT NULL,signDate datetime,groupName VARCHAR(127),memberName VARCHAR(127),is_sign INT DEFAULT 0,warn INT DEFAULT 0, activeDegree INT DEFAULT 0, PRIMARY KEY(groupId,memberUin,signDate))";
		statement.executeUpdate(sql);
	}

	/**
	 * 设置30天到期
	 * 
	 * @param info
	 * @return
	 * @throws SQLException
	 */
	public boolean checkProgram(String info) throws SQLException {
		String sql = "SELECT p.login_date FROM pcInfo p WHERE p.info = '" + info + "'";
		ResultSet rs = statement.executeQuery(sql);
		if (!rs.next()) {
			sql = "INSERT INTO pcInfo(info,login_date) VALUES ('" + info + "','" + df.format(new Date()) + "')";
			return !statement.execute(sql);
		}
		long d = 0l;
		try {
			d = (df.parse(df.format(new Date())).getTime() - df.parse(rs.getObject(1).toString()).getTime())
					/ (1000 * 60 * 60 * 24);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return d <= 29;
	}

	/**
	 * 根据时间段类型获取某个群聊的活跃榜单（用户的排名，用户的活跃度和前5名的uin、群昵称及其活跃度）
	 * 
	 * @param userName
	 *            机器人的用户名
	 * @param groupNumberId
	 *            群聊解密id
	 * @param memberUin
	 *            群成员的uin
	 * @param timetype
	 *            时间段类型
	 * @return 用户的排名，用户的活跃度和前5名的uin、群昵称及其活跃度组成的列表
	 * @throws SQLException
	 */
	public List<String> getActiveDegreeRank(String userName, String groupNumberId, String memberUin, TIMETYPE timetype)
			throws SQLException {
		getTimeByType(timetype);

		List<String> result = new ArrayList<String>();

		String sql = "select memberUin,memberName,sum(activeDegree) activeDegreeSum from " + userName
				+ "_info where groupId='" + groupNumberId + "' ";
		if (!startTime.equals(""))
			sql += "and signDate between '" + startTime + "' and '" + endTime + "' ";
		sql += "group by memberUin order by activeDegreeSum desc";
		ResultSet rt = statement.executeQuery(sql);
		int rank = 0, targetRank = 0, degreeSum, targetDegreeSum = 0;
		String tempMemberUin;
		while (rt.next()) {
			rank++;
			tempMemberUin = rt.getString(1);
			degreeSum = rt.getInt(3);
			if (tempMemberUin.equals(memberUin)) {
				targetRank = rank;
				targetDegreeSum = degreeSum;
			}
			if (rank <= 5) {
				result.add(tempMemberUin);
				result.add(rt.getString(2));
				result.add(degreeSum + "");
			} else if (targetRank != 0)
				break;
		}
		result.add(0, targetRank + "");
		result.add(1, targetDegreeSum + "");
		return result;
	}

	/**
	 * 根据时间段类型获取对应的签到榜（用户的排名，用户的签到次数和排名前5的群成员uin、群昵称及其签到次数）
	 * 
	 * @param userName
	 *            机器人的用户名
	 * @param groupNumberId
	 *            群聊解密id
	 * @param memberUin
	 *            群成员的uin
	 * @param timetype
	 *            时间段类型
	 * @return 用户的排名，用户的签到次数和排名前5的群成员uin、群昵称及其签到次数组成的列表
	 * @throws SQLException
	 */
	public List<String> getSignRank(String userName, String groupNumberId, String memberUin, TIMETYPE timetype)
			throws SQLException {
		getTimeByType(timetype);

		List<String> result = new ArrayList<String>();

		String sql = "select memberUin,memberName,sum(is_sign) signCount from " + userName + "_info where groupId='"
				+ groupNumberId + "' ";
		if (!startTime.equals(""))
			sql += "and signDate between '" + startTime + "' and '" + endTime + "' ";
		sql += "group by memberUin order by signCount desc";
		ResultSet rt = statement.executeQuery(sql);
		int rank = 0, targetRank = 0, signCount, targetSignCount = 0;
		String tempMemberUin;
		while (rt.next()) {
			rank++;
			tempMemberUin = rt.getString(1);
			signCount = rt.getInt(3);
			if (tempMemberUin.equals(memberUin)) {
				targetRank = rank;
				targetSignCount = signCount;
			}
			if (rank <= 5) {
				result.add(tempMemberUin);
				result.add(rt.getString(2));
				result.add(signCount + "");
			} else if (targetRank != 0)
				break;
		}
		result.add(0, targetRank + "");
		result.add(1, targetSignCount + "");
		return result;
	}

	/**
	 * 根据时间段类型获得对应的时间段
	 * 
	 * @param timetype
	 *            时间段类型
	 */
	private void getTimeByType(TIMETYPE timetype) {
		// 获取当前的日期
		Calendar nowTime = Calendar.getInstance();
		int year = nowTime.get(Calendar.YEAR);
		int month = nowTime.get(Calendar.MONTH) + 1;
		int day_of_month = nowTime.get(Calendar.DAY_OF_MONTH);
		int day_of_week = nowTime.get(Calendar.DAY_OF_WEEK);
		int days_of_month = nowTime.getActualMaximum(Calendar.DAY_OF_MONTH);

		// 根据时间段类型进行时间段的拼接
		final String startTimeText = "00:00:00";
		final String endTimeText = "23:59:59";
		switch (timetype) {
		case DAY:
			startTime = year + "-" + month + "-" + day_of_month + " " + startTimeText;
			endTime = year + "-" + month + "-" + day_of_month + " " + endTimeText;
			break;
		case WEEK:
			int tempYear = year;
			int tempMonth = month;
			int tempDay = day_of_month;

			// 获取周日的日期
			tempDay -= day_of_week - 1;
			if (tempDay < 1) { // 如果周日在上个月
				tempMonth -= 1;
				if (tempMonth == 0) { // 如果当前月是1月
					tempYear -= 1;
					tempMonth = 12;
				}
				nowTime.set(Calendar.MONTH, tempMonth - 1);
				tempDay += nowTime.getActualMaximum(Calendar.DAY_OF_MONTH);
			}
			startTime = tempYear + "-" + tempMonth + "-" + tempDay + " " + startTimeText;

			// 获取周六的日期
			tempYear = year;
			tempMonth = month;
			tempDay = day_of_month;
			tempDay += 7 - day_of_week;
			if (tempDay > days_of_month) { // 如果周六在下个月
				tempMonth += 1;
				if (tempMonth > 12) { // 如果当前月是12月
					tempYear += 1;
					tempMonth = 1;
				}
				tempDay -= days_of_month;
			}
			endTime = tempYear + "-" + tempMonth + "-" + tempDay + " " + endTimeText;

			break;
		case MONTH:
			startTime = year + "-" + month + "-1 " + startTimeText;
			endTime = year + "-" + month + "-" + days_of_month + " " + endTimeText;
			break;
		case YEAR:
			startTime = year + "-1-1 " + startTimeText;
			endTime = year + "-12-31 " + endTimeText;
			break;
		case DEFAULT:
			startTime = "";
			endTime = "";
			break;
		}
	}

	/**
	 * 获取活跃榜和签到榜的时间段类型
	 * 
	 * @author maomaojun
	 *
	 */
	public enum TIMETYPE {
		/**
		 * 日活跃榜
		 */
		DAY,

		/**
		 * 周活跃榜和签到榜
		 */
		WEEK,

		/**
		 * 月活跃榜和签到榜
		 */
		MONTH,

		/**
		 * 年活跃榜和签到榜
		 */
		YEAR,

		/**
		 * 总活跃榜和签到榜
		 */
		DEFAULT
	}
}
