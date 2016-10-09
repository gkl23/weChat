package weChat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import Utils.DBConnect;

/**
 * Created by huzhejie on 2016/8/8.
 */
public class demo {
	public static void main(String args[]) throws Exception {
		final WindowUI windowUI = new WindowUI();
		Calendar test = Calendar.getInstance();
		SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd");
		test.setTime(df.parse("1999-12-21"));
		System.out.println(
				test.get(Calendar.YEAR) + "," + test.get(Calendar.MONTH) + "," + test.get(Calendar.DAY_OF_MONTH));
	}
}