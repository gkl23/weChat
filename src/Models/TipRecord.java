package Models;

/**
 * Created by huzhejie on 2016/8/8.
 */
public class TipRecord {
	private Boolean flag;
	private String time;//可以做指定时间，也可以做间隔发布的起始时间
	private String period;//间隔时间
	private String groupName;
	private String property;

	public TipRecord() {
		flag = false;
		time = "";
		groupName = "";
		property = "";
		period = "";
	}

	public String getPeriod() {
		return period;
	}

	public void setPeriod(String period) {
		this.period = period;
	}

	public Boolean getFlag() {
		return flag;
	}

	public void setFlag(Boolean flag) {
		this.flag = flag;
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
