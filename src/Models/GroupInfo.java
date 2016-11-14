package Models;

import com.sun.org.apache.xpath.internal.operations.Bool;
import net.sourceforge.pinyin4j.PinyinHelper;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by huzhejie on 2016/7/19.
 */
public class GroupInfo{
	private Boolean acrossGroupFlag=false;// 开启跨群功能
	private Boolean boardcastFlag=false;// 开启直播功能
	private Boolean fromGroupFlag = false;//嘉宾源于哪个群
	private Boolean flag = false;
	private String groupID = ""; // 加密的群聊id
	private String groupName = "";
	private String groupNumberId = ""; // 解密的群聊id
	private int memberCount;
	private Map<String, UserInfo> group = new HashMap<String, UserInfo>(); // 群成员加密id和信息组成的键值对列表

	public Boolean getFromGroupFlag() {
		return fromGroupFlag;
	}

	public void setFromGroupFlag(Boolean fromGroupFlag) {
		this.fromGroupFlag = fromGroupFlag;
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

	public Map<String, UserInfo> getGroup() {
		return group;
	}

	public void setGroup(Map<String, UserInfo> group) {
		this.group = group;
	}

	public Boolean getBoardcastFlag() {
		return boardcastFlag;
	}

	public void setBoardcastFlag(Boolean boardcastFlag) {
		this.boardcastFlag = boardcastFlag;
	}
}
