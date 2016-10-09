package Models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huzhejie on 2016/7/19.
 */
public class GroupInfo {
	private Boolean hasMemberUin = false;//是否收集了群成员Uin
	private Boolean acrossGroupFlag;// 开启跨群功能
	private Boolean flag = false;
	private String groupID = ""; // 加密的群聊id
	private String groupName = "";
	private String groupNumberId = ""; // 解密的群聊id
	private int memberCount;
	private List<UserInfo> group = new ArrayList<>();

	public Boolean getHasMemberUin() {
		return hasMemberUin;
	}

	public void setHasMemberUin(Boolean hasMemberUin) {
		this.hasMemberUin = hasMemberUin;
	}

	public String getGroupNumberId() {
		return groupNumberId;
	}

	public void setGroupNumberId(String groupNumberId) {
		this.groupNumberId = groupNumberId;
	}

	public Boolean getAcrossGroupFlag() {
		return acrossGroupFlag;
	}

	public void setAcrossGroupFlag(Boolean acrossGroupFlag) {
		this.acrossGroupFlag = acrossGroupFlag;
	}

	public Boolean getFlag() {
		return flag;
	}

	public void setFlag(Boolean flag) {
		this.flag = flag;
	}

	public String getGroupID() {
		return groupID;
	}

	public void setGroupID(String groupID) {
		this.groupID = groupID;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public int getMemberCount() {
		return memberCount;
	}

	public void setMemberCount(int memberCount) {
		this.memberCount = memberCount;
	}

	public List<UserInfo> getGroup() {
		return group;
	}

	public void setGroup(List<UserInfo> group) {
		this.group = group;
	}
}
