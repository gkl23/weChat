package Models;

/**
 * Created by huzhejie on 2016/7/19.
 */
public class UserInfo {
    private String remarkName = "";
    private String userId="";
    private String signature="";
    private boolean acrossGroupFlag = false;
    private String uin = "";
    private boolean boardcastFlag = false;

    public boolean isBoardcastFlag() {
        return boardcastFlag;
    }

    public void setBoardcastFlag(boolean boardcastFlag) {
        this.boardcastFlag = boardcastFlag;
    }

    public String getUin() {
        return uin;
    }

    public void setUin(String uin) {
        this.uin = uin;
    }

    public boolean isAcrossGroupFlag() {
        return acrossGroupFlag;
    }

    public void setAcrossGroupFlag(boolean acrossGroupFlag) {
        this.acrossGroupFlag = acrossGroupFlag;
    }

    public String getRemarkName() {
        return remarkName;
    }

    public void setRemarkName(String remarkName) {
        this.remarkName = remarkName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
