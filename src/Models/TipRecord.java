package Models;

/**
 * Created by huzhejie on 2016/8/8.
 */
public class TipRecord {
	private String time;// 可以做指定时间，也可以做间隔发布的起始时间
	private int period;// 间隔时间
	private String groupName;
	private String property;
	private String timeStamp;
	private boolean isVisit;

	public TipRecord(String time, int period, String groupName, String property) {
		this.time = time;
		this.period = period;
		this.groupName = groupName;
		this.property = property;
		this.timeStamp = "";
		this.isVisit = false;
	}

	public String getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}

	public boolean isVisit() {
		return isVisit;
	}

	public void setVisit(boolean visit) {
		isVisit = visit;
	}

	public int getPeriod() {
		return period;
	}

	public void setPeriod(int period) {
		this.period = period;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}
}
