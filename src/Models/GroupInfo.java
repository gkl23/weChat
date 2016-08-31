package Models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huzhejie on 2016/7/19.
 */
public class GroupInfo {
    private Boolean flag = true;
    private String groupID = "";
    private String groupName="";
    private int memberCount;
    private List<UserInfo> group = new ArrayList<>();
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
