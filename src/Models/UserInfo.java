package Models;

/**
 * Created by huzhejie on 2016/7/19.
 */
public class UserInfo {
    private String remarkName = "";
    private String userId="";
    private String nickName="";
    private String signature="";
    private boolean acrossGroupFlag = false;
    private String uin = "";

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

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
