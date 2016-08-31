package Models;

/**
 * Created by huzhejie on 2016/8/8.
 */
public class TipRecord {
	private Boolean flag;
	private String time;
	private String groupName;
	private String property;

	public TipRecord() {
		flag = false;
		time = "";
		groupName = "";
		property = "";
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
