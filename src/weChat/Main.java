package weChat;

/**
 * Created by huzhejie on 2016/7/7.
 */

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Models.GroupInfo;
import Models.TipRecord;
import Models.UserInfo;
import Utils.Aes;
import Utils.DBConnect;
import Utils.Md5;
import blade.kit.StringKit;
import blade.kit.http.HttpRequest;
import jeasy.analysis.MMAnalyzer;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.jb2011.lnf.beautyeye.ch3_button.BEButtonUI;
import org.jb2011.lnf.beautyeye.ch4_scroll.BEScrollPaneUI;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

public class Main {
    private static HttpRequest httpRequest;
    private static Boolean updatePic = false;
    private static Boolean autoAddFriendFlag = true; // 控制自动添加好友
    private static Boolean autoReplyFlag = true; // 自动回复加智能聊天
    private static Boolean sensitiveFlag = true; // 敏感词警告
    private static Boolean atModeFlag = true;// 控制@模式
    private static Boolean isLogin = false;//是否登录
    private static Boolean isSign = false;//是否开启签到功能
    private static int  minSenseWarn = 0;
    private static int maxSenseWarn = 3;
    private static String apiKey="49d5dd04005a4d82b7d5bc30dae96821";
    private static String url;
    private static String timeStamp;
    private static String uuid;
    private static String skey;
    private static String wxsid;
    private static String wxuin;
    private static String pass_ticket;
    private static String DeviceID;
    private static JSONObject js;
    private static String userID="";
    private static String userName_wx = "";
    private static String userName_robot = "";
    private static String memberID = "";
    private static String groupID="";
    private static String tipGroupID = "";
    private static String msgID = "";
    private static final String ATDELIM = " "; // ！！注意，@消息的分隔符并不是空格，所以设为常量
    private static final int maxSize = 5;
    private static List<String> activeGroupId = new ArrayList<>();
    private static List<String> unactiveGroupId = new ArrayList<>();
    private static List<String> recordList = new ArrayList<>();
    private static List<UserInfo> userInfoList = new ArrayList<>();
    private static List<GroupInfo> groupInfoList = new ArrayList<>();
    private static Map<String, String> publicReply = new HashMap<>();
    private static Map<String, String> privateReply = new HashMap<>();
    private static List<String> senseReply = new ArrayList<>();
    private static JSONObject syncKey;
    private static StringBuffer syncKeyList = new StringBuffer();
    private static String header;
    private static String host;
    private static JSONObject baseRequest = new JSONObject();
    private static JSONObject verifyUserList = new JSONObject();
    private static String from;
    private static String to;
    private static String content;
    private static String v_ticket = "";
    private static String friendId;
    private static GroupInfo invitedGroup;
    private static GroupInfo removeGroup;
    private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static List<TipRecord> tipRecordList = new ArrayList<>();
    private static Document doc;
    private static MMAnalyzer analyzer = new MMAnalyzer(1);

    private static WindowUI windowUI;
    private static Pattern tulingKey = Pattern.compile("[0-9|a-z]+");//图灵key是否满足条件
    private static Matcher matcher;

    private static DBConnect dbConnect;

    private static LoadingDialogJFrame loadingDialogJFrame; // 加载提示框

    private boolean hasNotified=false; // 标识是否已经获取了联系人列表之外的群聊

    private static Thread checkForMsgThread; // 监听最新消息的线程

    private static Vector<String> inviteFriendName = new Vector<>();//存放能够邀请的用户的用户名
    private static Vector<UserInfo> inviteFriendList = new Vector<>();//存放邀请的用户列表

    private static Vector<String> searchInviteFriend = new Vector<>();//存放搜索并排序后的结果
    private  static Vector<String> searchInvitedGroup = new Vector<>();//存放搜索并排序后的结果

    private static Vector<String> removeFriendName = new Vector<>();
    private static Vector<UserInfo> removeFriendList = new Vector<>();

    private static Vector<String> searchRemoveFriend = new Vector<>();
    private static Vector<String> searchRemoveGroup = new Vector<>();

    private static Vector<String> group = new Vector<>();//存放现在所有群的群名

    /**
     * 初始化微信机器人
     */
    public Main() {
        windowUI = new WindowUI();
        doc = windowUI.getjTextPane().getDocument();
        hasNotified = false;
        try {
            doc.insertString(doc.getLength(), "您已登陆成功，开始记录消息!\n", windowUI.getAttributeSet());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<TipRecord> getTipRecordList() {
        return tipRecordList;
    }

    private String sendGetRequest(String url) {
        httpRequest = HttpRequest.get(url);
        return httpRequest.body();
    }

    private String sendPostRequest(String url, JSONObject js) {
        httpRequest = HttpRequest.post(url);
        return js == null ? httpRequest.body() : httpRequest.send(js.toString()).body();
    }

    private String sendPostRequest(String url, JSONObject js, String header) {
        httpRequest = HttpRequest.post(url);
        return httpRequest.header("Cookie", header).send(js.toString()).body();
    }

    /**
     * 图灵机器人智能聊天
     * @param content 聊天内容
     * @param userId  用户ID
     * @return  回复内容
     */
    private String aiChat(String content, String userId) {
        final String apiUrl = "http://www.tuling123.com/openapi/api";
        final String secretKey = "02a06d8364d4ef9a"; //key
        final String timeStamp = System.currentTimeMillis() + "";


        String key = Md5.MD5(secretKey + timeStamp + apiKey);


        JSONObject chatData = new JSONObject();
        chatData.put("key", apiKey);
        chatData.put("info", content);
        chatData.put("userid", userId);
        String encryptChatData = new Aes(key).encrypt(chatData.toString());


        OutputStreamWriter outWriter = null;
        BufferedReader inReader = null;
        StringBuilder response = new StringBuilder("");
        final int timeOut = 50 * 1000;
        try {


            HttpURLConnection request = (HttpURLConnection) new URL(apiUrl).openConnection();
            request.setDoOutput(true);
            request.setDoInput(true);
            request.setUseCaches(false);
            request.setRequestMethod("POST");
            request.setConnectTimeout(timeOut);
            request.setReadTimeout(timeOut);
            request.setRequestProperty("Content-Type", "application/json");
            request.setRequestProperty("Accept", "application/json");
            request.setRequestProperty("Autherization", "token");
            request.connect();

            outWriter = new OutputStreamWriter(request.getOutputStream(), "UTF-8");
            JSONObject requestData = new JSONObject();
            requestData.put("key", apiKey);
            requestData.put("timestamp", timeStamp);
            requestData.put("data", encryptChatData);
            outWriter.write(requestData.toString());
            outWriter.flush();
            outWriter.close();

            // ???????????????
            inReader = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"));
            String line;
            while ((line = inReader.readLine()) != null)
                response.append(line);
            // System.out.println(response);

            // ????????????????????????
            JSONObject responseJson = JSONObject.fromObject(response.toString());
            response.delete(0, response.length());
            int code = responseJson.getInt("code");
            String text = responseJson.getString("text"); // ???????????????????????
            switch (code) {
                case 100000: // ??????
                    response.append(text);
                    break;
                case 200000: // ???????
                    response.append("该功能暂时不开放");
                    break;
                case 302000: // ???????
                    response.append("该功能暂时不开放");
                    break;
                case 308000: // ???????
                    response.append("该功能暂时不开放");
                    break;
                default:
                    System.out.println(text);
                    response.append("智能聊天存在故障，请稍后重试。");
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.append("智能聊天存在故障，请稍后重试。");
        }

        return response.toString();
    }

    /**
     * 读取词库文件
     * @throws Exception
     */
    public void readFiles() throws Exception {
        String s[] = null;
        File public_file = new File("公开.txt");
        File private_file = new File("私密.txt");
        File sense_file = new File("敏感词.txt");
        BufferedWriter bw = null;
        Boolean flag1=false,flag2=false,flag3=false;
        if (!public_file.exists()) {
            public_file.createNewFile();
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(public_file), "UTF-8"));
            bw.write("请按照格式：  关键词--回复内容(一行)    来编写~");
            bw.flush();
            bw.close();
        }
        if (!private_file.exists()) {
            private_file.createNewFile();
            bw = new BufferedWriter (new OutputStreamWriter (new FileOutputStream (private_file), "UTF-8"));
            bw.write("请按照格式：  关键词--回复内容(一行)   来编写~");
            bw.flush();
            bw.close();
        }
        if (!sense_file.exists()) {
            sense_file.createNewFile();
            bw = new BufferedWriter (new OutputStreamWriter (new FileOutputStream (sense_file), "UTF-8"));
            bw.write("请按照格式：  敏感词(一行)    来编写~");
            bw.flush();
            bw.close();
        }
        InputStreamReader read = null;
        BufferedReader br = null;
        String str = null;
        read = new InputStreamReader(new FileInputStream(public_file),"UTF-8");
        br = new BufferedReader(read);

        while ((str = br.readLine())!=null) {
            if(!flag1)
                flag1 = true;
            else {
                s = str.split("--");
                publicReply.put(s[0], s[1]);
            }
        }
        read.close();
        br.close();
        read = new InputStreamReader(new FileInputStream(private_file),"UTF-8");
        br = new BufferedReader(read);
        while ((str = br.readLine())!=null) {
            if(!flag2)
                flag2 = true;
            else {
                s = str.split("--");
                if (s.length == 2) {
                    privateReply.put(s[0], s[1]);
                }
            }
        }
        read.close();
        br.close();
        read = new InputStreamReader(new FileInputStream(sense_file),"UTF-8");
        br = new BufferedReader(read);
        while ((str = br.readLine())!=null) {
            if(!flag3)
                flag3 =true;
            else{
                senseReply.add(str);
            }
        }
        read.close();
        br.close();
    }

    /**
     * 生成DeviceID
     * @return DeviceID
     */
    private String produceDevID() {
        Random rm = new Random();

        double pross = (1 + rm.nextDouble()) * Math.pow(10, 16);

        String fixLenthString = String.valueOf(pross);

        return fixLenthString.substring(1, 16 + 1);
    }

    /**
     * 生成二维码的url
     * @param s 为uuid
     * @return  生成二维码的url
     */
    private String produceErWei(String s) {
        s = "http://login.weixin.qq.com/qrcode/" + s;
        return s;
    }

    /**
     * SyncKey转换
     * @param s 转换前的包含synckey的json数据
     */
    private void syncKeyTransfer(String s) {
        JSONObject fr = JSONObject.fromObject(s);
        syncKey = fr.getJSONObject("SyncKey");
        JSONArray ja = syncKey.getJSONArray("List");
        for (int i = 0; i < ja.size(); i++) {
            JSONObject json = ja.getJSONObject(i);
            if (i != ja.size() - 1) {
                syncKeyList.append(json.get("Key") + "_" + json.get("Val") + "%7C");
            } else {
                syncKeyList.append(json.get("Key") + "_" + json.get("Val"));
            }
        }
    }

    /**
     * 自动通过好友验证
     */
    private void addFriend() {
        url = "https://" + host + "/cgi-bin/mmwebwx-bin/webwxverifyuser?r=" + System.currentTimeMillis()
                + "&lang=zh_CN&pass_ticket=" + pass_ticket;
        js.clear();
        js.put("BaseRequest", baseRequest);
        js.put("Opcode", 3);
        js.put("VerifyUserListSize", 1);
        verifyUserList.put("Value", friendId);
        verifyUserList.put("VerifyUserTicket", v_ticket);
        js.put("VerifyUserList", verifyUserList);
        js.put("VerifyContent", "");
        js.put("SceneListCount", 1);
        js.put("SceneList", 33);
        js.put("skey", skey);
        this.sendPostRequest(url, js, header);
        v_ticket = "";

    }

    /**
     * 检查消息是否有更新
     * 并对消息类别进行分类（如好友验证，图片消息，文字消息等）
     */
    private void checkMsg() {
        timeStamp = String.valueOf(System.currentTimeMillis());
        String url = "https://webpush." + host + "/cgi-bin/mmwebwx-bin/synccheck?r="+timeStamp+"&skey=" + skey + "&sid=" + wxsid
                + "&uin=" + wxuin + "&deviceid=" + DeviceID + "&synckey=" + syncKeyList.toString() + "&_=" + timeStamp;
        String s = HttpRequest.get(url).header("Cookie", header).body();

        int retcode = Integer.parseInt(s.substring(s.indexOf('"') + 1, s.indexOf(',') - 1));
        int selector = Integer.parseInt(s.substring(s.lastIndexOf(":\"") + 2, s.lastIndexOf('"')));

        Boolean isListen = true;


        if (selector!=0) { // 如果selector不为0，则获取最新消息
            url = "https://" + host + "/cgi-bin/mmwebwx-bin/webwxsync?sid=" + wxsid + "&skey=" + skey
                    + "&lang=zh_CN&pass_ticket=" + pass_ticket;
            js.clear();
            js.put("BaseRequest", baseRequest);
            js.put("SyncKey", syncKey);
            js.put("rr", ~System.currentTimeMillis());
            s = sendPostRequest(url, js, header);
            syncKeyList = new StringBuffer();
            syncKeyTransfer(s);
            JSONObject jsonObject = JSONObject.fromObject(s);
            int count = jsonObject.getInt("AddMsgCount");
            if (count != 0) {
                JSONArray jsonArray = jsonObject.getJSONArray("AddMsgList");
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    from = jsonObject1.getString("FromUserName");
                    to = jsonObject1.getString("ToUserName");
                    content = jsonObject1.getString("Content");
                    int msgType = jsonObject1.getInt("MsgType");
                    msgID = jsonObject1.getString("MsgId");
                    JSONObject jsonObject2 = jsonObject1.getJSONObject("RecommendInfo");
                    v_ticket = jsonObject2.getString("Ticket");
                    friendId = jsonObject2.getString("UserName");
                    String replyInGroupContent = null, replyToMemberContent = null; // 在群聊中回复的内容和对好友回复的内容
                    String fromId = null, toId = null; // 保存来往用户的id，用于回复消息
                    ImageIcon image=null;
                    GroupInfo group = null;
                    UserInfo member = null;

                    // 对from等变量进行处理，便于保存消息记录
                    if (from.startsWith("@@")) { // 如果消息来自群聊
                        for (GroupInfo groupInfo : groupInfoList)
                            if (groupInfo.getGroupID().equals(from)) {
                                isListen = groupInfo.getFlag();
                                groupID = from;
                                group=groupInfo;
                                from = groupInfo.getGroupName();
                                String temp[] = content.split(":");
                                if (temp.length > 1) {
                                    memberID = temp[0];
                                    content = temp[1].replace("<br/>", "").trim();
                                    for (UserInfo userInfo : groupInfo.getGroup())
                                        if (userInfo.getUserId().equals(memberID)) {
                                            to = userInfo.getNickName();
                                            member=userInfo;
                                            break;
                                        }
                                }
                                break;
                            }
                    } else { // 消息来自单个用户
                        fromId = from;
                        if (userID.equals(fromId)) // 自己发送的消息，也就是从手机发出的消息
                            from = "我";
                        else // 好友发出的消息
                            for (UserInfo userInfo : userInfoList)
                                if (userInfo.getUserId().equals(fromId))
                                    from = userInfo.getNickName();
                        toId = to;
                        if (userID.equals(toId)) // 来自好友的消息
                            to = "我";
                        else if (toId.startsWith("@@")) { // 自己发送到群聊的消息
                            for (GroupInfo groupInfo : groupInfoList)
                                if (groupInfo.getGroupID().equals(toId)) {
                                    isListen = groupInfo.getFlag();
                                    to = groupInfo.getGroupName();
                                }
                        } else // 自己发送给好友的消息
                            for (UserInfo userInfo : userInfoList)
                                if (userInfo.getUserId().equals(toId))
                                    to = userInfo.getNickName();
                    }

                    // 对不同类型的消息进行处理
                    switch (msgType) {
                        case 1: // 文本消息

                                if (!"".equals(groupID)&&isListen) { // 来自群聊的消息
                                boolean matchKeyword = false; // 标识是否已经匹配到了关键词
                                //是否为签到
                                if(content.equals("签到")&&isSign){
                                    replyMsg(to+",您已签到,谢谢~",groupID);
                                    try {
                                        dbConnect.insertSignRecord(userName_robot, from, to);
                                    }catch(Exception e){
                                        e.printStackTrace();
                                    }
                                }
                                // 如果开启了自动回复
                                else if (autoReplyFlag) {
                                    // 判断是否开启了@模式
                                    boolean atFlag = !atModeFlag;

                                    // 如果@模式已开启
                                    if (!atFlag) {

                                        // 是否满足@模式的条件，即内容是否同时包含@符号和特定的分隔符
                                        atFlag = content.contains("@") && content.contains(ATDELIM);

                                        if (atFlag) { // 如果满足@模式的条件
                                            String[] atContent = content.split(ATDELIM);
                                            String atUser = atContent[0].substring(atContent[0].indexOf('@') + 1);
                                            String groupName = null; // 用于跨群指定群名
                                            content = atContent.length > 1 ? atContent[1] : "";

                                            // 消息不能为空，否则不回复
                                            atFlag = !content.equals("");

                                            if (atFlag) { // 判断是否@机器人
                                                atFlag = false;
                                                groupName = group.getGroupName();
                                                for (UserInfo userInfo : group.getGroup())
                                                    if (userInfo.getUserId().equals(userID)
                                                            && userInfo.getRemarkName().equals(atUser)) {
                                                        atFlag = true;
                                                        break;
                                                    }
                                            }

										/*
										 * 处理跨群功能，满足"跨群 content"的格式才触发跨群功能，满足
										 * "跨群回复 群聊名@用户名 content"的格式才触发跨群回复功能
										 */
                                            if (atFlag) {
                                                String acrossKeyword = content.indexOf(' ')<0 ? "" :content.substring(0, content.indexOf(' '));
                                                if (windowUI.getRequestKeyword().equals(acrossKeyword)&& member.isAcrossGroupFlag()) {
                                                    for (GroupInfo groupInfo : groupInfoList)
                                                        if (groupInfo.getAcrossGroupFlag())
                                                        {
                                                            replyInGroupContent=groupName + "@" + to + "（"
                                                                    + windowUI.getRequestKeyword() + "）："
                                                                    + content.substring(content.indexOf(' ') + 1);
                                                            replyMsg(replyInGroupContent, groupInfo.getGroupID());

                                                        }
                                                    atFlag = false; // 取消自动回复，只进行跨群交流
                                                } else if (windowUI.getReplyKeyword().equals(acrossKeyword)&& member.isAcrossGroupFlag()) {
                                                    content = content.substring(content.indexOf(' ') + 1);
                                                    if(content.indexOf(' ')>=0) {
                                                        String names = content.substring(0, content.indexOf(' '));
                                                        String[] nameArray=names.split("@");
                                                        boolean hasName=false;
                                                        if(nameArray.length>1)
                                                            for(GroupInfo groupInfo:groupInfoList)
                                                                if(groupInfo.getGroupName().equals(nameArray[0])) {
                                                                    for (UserInfo userInfo : groupInfo.getGroup())
                                                                        if (userInfo.getRemarkName().equals(nameArray[1])) {
                                                                            hasName=true;
                                                                            break;
                                                                        }
                                                                }
                                                        if(hasName)
                                                            for (GroupInfo groupInfo : groupInfoList)
                                                                if (!groupInfo.getGroupID().equals(groupID)
                                                                        && groupInfo.getAcrossGroupFlag()) {
                                                                    replyInGroupContent = groupName + "@" + to + "（"
                                                                            + windowUI.getReplyKeyword() + "）：" + content;
                                                                    replyMsg(replyInGroupContent, groupInfo.getGroupID());
                                                                    atFlag = false; // 取消自动回复，只进行跨群交流
                                                                }
                                                    }
                                                }
                                            }  else
                                                atFlag=true;  //取消@模式
                                        }  else
                                            atFlag=true;  //取消@模式
                                    }
                                    if (atFlag) { // 如果没有开启@模式，或者满足@模式的条件
                                        for (String keyword : publicReply.keySet()) { // 匹配公共关键词
                                            if (content.equals(keyword)) {
                                                replyInGroupContent = to + " 您好，" + publicReply.get(keyword);
                                                replyMsg(replyInGroupContent, groupID);
                                                matchKeyword = true;
                                                break;
                                            }
                                        }

                                        if (!matchKeyword) // 公共关键词匹配失败，匹配私密关键词
                                            for (String keyword : privateReply.keySet())
                                                if (content.equals(keyword)) {
                                                    replyInGroupContent = to + " 您好，信息已私信回复您，谢谢！";
                                                    replyMsg(replyInGroupContent, groupID);
                                                    replyToMemberContent = privateReply.get(keyword);
                                                    replyMsg(replyToMemberContent, memberID);
                                                    matchKeyword = true;
                                                    break;
                                                }

                                        if (!matchKeyword) { // 关键词匹配失败，智能回复
                                            replyInGroupContent = to + " 您好，" + aiChat(content, memberID);
                                            replyMsg(replyInGroupContent, groupID);
                                        }
                                    }

                                    // 如果开启了敏感词警告
                                    if (sensitiveFlag)
                                        for (String senseWord : senseReply) {
                                            if (content.equals(senseWord.trim())) {
                                                //数据库更新一次
                                                try {
                                                    int c = dbConnect.updateSenseWarn(userName_robot,from,to);
                                                    if(c<=maxSenseWarn&&c>=minSenseWarn){
                                                        replyInGroupContent = to + " 您好，" + "您言语有不当之处，警告一次";
                                                        replyMsg(replyInGroupContent, groupID);
                                                    }
                                                }catch (Exception e){
                                                    e.printStackTrace();
                                                }
                                                matchKeyword = true;
                                                break;
                                            }
                                        }
                                } else if (!userID.equals(fromId))
                                    for (UserInfo userInfo : userInfoList)
                                        if (userInfo.getUserId().equals(fromId)) { // 来自好友的消息
                                            replyToMemberContent = aiChat(content, fromId);
                                            replyMsg(replyToMemberContent, fromId);
                                        }
                            }
                            break;
                        case 3: // 图片消息
//							url = "https://" + host + "/cgi-bin/mmwebwx-bin/webwxgetmsgimg?&MsgID=" + msgID + "&skey="
//									+ skey + "&type=slave";
//							image = new ImageIcon(HttpRequest.get(url).header("Cookie",header).bytes());
//							content = "\n";
                            content=null;
                            break;
                        case 34: // 语音消息
                            System.out.println(jsonObject1);
                            content = null;
                            break;
                        case 37: // 好友请求消息
                            if (autoAddFriendFlag)
                                addFriend();
                            content = null;
                            break;
                        case 40: // 疑似朋友消息
                            System.out.println(jsonObject1);
                            content = null;
                            break;
                        case 47: // emoji表情消息
                            System.out.println(jsonObject1);
                            content = null;
                            break;
                        case 51: // 状态提醒消息
                            String[] statusNotifyUserNames = jsonObject1.getString("StatusNotifyUserName").split(","); // 获取不在联系人列表里的群聊
                            if (statusNotifyUserNames.length > 1) { // 如果需要提醒的用户多于1个，说明是群聊列表
                                content = null;
                                if (!hasNotified) {
                                    for (String statusNotifyUserName : statusNotifyUserNames)
                                        if (statusNotifyUserName.startsWith("@@")
                                                && !activeGroupId.contains(statusNotifyUserName)
                                                && !unactiveGroupId.contains(statusNotifyUserName))
                                            unactiveGroupId.add(statusNotifyUserName);
                                    getGroupList();
                                }
                                hasNotified = true;
                            } else // 否则说明不是群聊列表，而是打开了一个会话或者特殊账号发了信息
                                content = "[会话]";
                            break;
                        case 9999: // 系统提示信息
                            System.out.println(jsonObject1);
                            content = null;
                            break;
                        case 10000: // 系统消息
                            System.out.println(jsonObject1);
                            content = null;
                            break;
                        case 10002: // 撤回消息
                            System.out.println(jsonObject1);
                            content = null;
                            break;
                    }
                    // 保存消息记录
                    try {
                        if (content != null&&isListen) {
                            content.replaceAll(",", "，");
                            if ("".equals(groupID)) { // 不是来自群聊的消息
                                boolean fromUserListOrGroupList = false; // 标记消息的来源和接收是否都在好友或者群聊列表里
                                if (userID.equals(fromId)) { // 自己发出的消息
                                    for (UserInfo userInfo : userInfoList)
                                        if (userInfo.getUserId().equals(toId))
                                            fromUserListOrGroupList = true;
                                    if (!fromUserListOrGroupList)
                                        for (GroupInfo groupInfo : groupInfoList)
                                            if (groupInfo.getGroupID().equals(toId))
                                                fromUserListOrGroupList = true;
                                } else
                                    for (UserInfo fromUserInfo : userInfoList)
                                        if (fromUserInfo.getUserId().equals(fromId))
                                            fromUserListOrGroupList = true;
                                if (fromUserListOrGroupList) { // 只记录来源和接收都在好友或者群聊列表里的消息
                                    recordList.add(df.format(new Date()) + "," + from + "," + to + "," + (content.equals("\n")?"[图片]":content));
                                    doc.insertString(doc.getLength(), toId.startsWith("@@")
                                                    ? df.format(new Date()) + "\n我在 " + to + " 群中说：" + (content.equals("\n")?"":content + "\n")
                                                    : df.format(new Date()) + "\n" + from + " 对 " + to + " 说：" + (content.equals("\n")?"":content + "\n"),
                                            windowUI.getAttributeSet());
                                    if(content.equals("\n")) {
                                        windowUI.getjTextPane().setCaretPosition(doc.getLength());
                                        windowUI.getjTextPane().insertIcon(image);
                                        doc.insertString(doc.getLength(),"\n",windowUI.getAttributeSet());
                                    }
                                    windowUI.getjTextPane().setCaretPosition(doc.getLength());
                                }
                            } else {
                                recordList.add(df.format(new Date()) + "," + from + " 群," + to + "," + (content.equals("\n")?"[图片]":content));
                                doc.insertString(doc.getLength(),
                                        df.format(new Date()) + "\n" + from + " 群中 " + to + " 说：" + (content.equals("\n")?"":content + "\n"),
                                        windowUI.getAttributeSet());
                                if(content.equals("\n")) {
                                    windowUI.getjTextPane().setCaretPosition(doc.getLength());
                                    windowUI.getjTextPane().insertIcon(image);
                                    doc.insertString(doc.getLength(),"\n",windowUI.getAttributeSet());
                                }
                                groupID = "";
                                windowUI.getjTextPane().setCaretPosition(doc.getLength());
                            }
                        }

                        if (replyInGroupContent != null&&isListen) {
                            replyInGroupContent.replaceAll(",", "，");
                            doc.insertString(doc.getLength(), df.format(new Date()) + "\n我在 " + from + " 群中对 " + to + " 说："
                                    + replyInGroupContent + "\n", windowUI.getAttributeSet());
                            windowUI.getjTextPane().setCaretPosition(doc.getLength());
                            recordList.add(df.format(new Date()) + ",我" + "," + to + "(" + from + " 群),"
                                    + replyInGroupContent);
                        }

                        if (replyToMemberContent != null&&isListen) {
                            replyToMemberContent.replaceAll(",", "，");
                            doc.insertString(doc.getLength(),
                                    df.format(new Date()) + "\n我对 " + from + " 说：" + replyToMemberContent + "\n",
                                    windowUI.getAttributeSet());
                            windowUI.getjTextPane().setCaretPosition(doc.getLength());
                            recordList.add(df.format(new Date()) + ",我" + "," + from + "," + replyToMemberContent);
                        }
                    } catch (BadLocationException e) {
                        e.printStackTrace();
                    }
                    windowUI.getChatJPanel().validate();
                    windowUI.getChatJPanel().repaint();
                }
            }
        }
    }

    /**
     * 回复及发送消息
     * @param s   回复的内容
     * @param id  回复对象的ID
     */
    private void replyMsg(String s, String id) {
        url = "https://" + host + "/cgi-bin/mmwebwx-bin/webwxsendmsg?lang=zh_CN&pass_ticket=" + pass_ticket;
        js.clear();
        js.put("BaseRequest", baseRequest);
        JSONObject msg = new JSONObject();
        // 生成ClientMsgId
        int r = (int) (Math.random() * 9000) + 1000;
        Long ts = System.currentTimeMillis() << 4;
        String cmId = ts.toString() + String.valueOf(r);

        // 生成Msg
        msg.put("ClientMsgId", cmId);
        msg.put("Content", s);
        msg.put("FromUserName", userID);
        msg.put("LocalID", cmId);
        msg.put("ToUserName", id);
        msg.put("Type", 1);

        js.put("Msg", msg);
        js.put("Scene", 0);
        this.sendPostRequest(url, js);
    }

    /**
     * 邀请用户进群
     *
     * @param addUserID  用户ID
     * @param addGroupID 邀请用户进群的ID
     */
    private void addMember(String addUserID, String addGroupID) {
        url = "https://" + host + "/cgi-bin/mmwebwx-bin/webwxupdatechatroom";
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("AddMemberList", addUserID);
        jsonObject.put("BaseRequest", baseRequest);
        jsonObject.put("ChatRoomName", addGroupID);
        httpRequest = HttpRequest.post(url, true, "fun", "addmember", "pass_ticket", pass_ticket)
                .header("Content-Type", "application/json;charset=UTF-8").header("Cookie", header)
                .send(jsonObject.toString());
        httpRequest.body();
        httpRequest.disconnect();
    }

    /**
     * 将用户T出该群
     * @param UserID  用户ID
     * @param GroupID  群ID
     */
    private Boolean deleteMember(String UserID, String GroupID) {
        url = "https://" + host + "/cgi-bin/mmwebwx-bin/webwxupdatechatroom";
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("DelMemberList", UserID);
        jsonObject.put("BaseRequest", baseRequest);
        jsonObject.put("ChatRoomName", GroupID);
        httpRequest = HttpRequest.post(url, true, "fun", "delmember", "pass_ticket", pass_ticket)
                .header("Content-Type", "application/json;charset=UTF-8").header("Cookie", header)
                .send(jsonObject.toString());
        JSONObject jsonObject1 = JSONObject.fromObject(httpRequest.body());
        httpRequest.disconnect();
        return jsonObject1.getJSONObject("BaseResponse").get("Ret").equals(0);
    }

    /**
     * 修改群名
     *
     * @param chatroomUserName
     *            群id
     * @param newChatroomName
     *            新的群名
     */
    private void modifyChatroomName(String chatroomUserName, String newChatroomName) {
        url = "https://" + host + "/cgi-bin/mmwebwx-bin/webwxupdatechatroom";
        JSONObject baseRequest = new JSONObject();
        baseRequest.put("BaseRequest", baseRequest);
        baseRequest.put("ChatRoomName", chatroomUserName);
        baseRequest.put("NewTopic", newChatroomName);

        httpRequest = HttpRequest.post(url, true, "fun", "modtopic", "pass_ticket", pass_ticket)
                .header("Content-Type", "application/json;charset=UTF-8").header("Cookie", header)
                .send(baseRequest.toString());
        httpRequest.body();
        httpRequest.disconnect();
    }

    /**
     * 得到最近联系人的名单
     */
    private void getRecentList() {
        url = "https://" + host + "/cgi-bin/mmwebwx-bin/webwxgetcontact?lang=zh_CN&pass_ticket=" + pass_ticket + "&r="
                + System.currentTimeMillis() + "&seq=0&skey=" + skey;
        httpRequest = HttpRequest.post(url);
        String temp = httpRequest.header("Cookie", header).body();
        JSONObject jsonObject = JSONObject.fromObject(temp);
        JSONArray jsonArray = jsonObject.getJSONArray("MemberList");
        for (int i = 0; i < jsonArray.size(); i++) {
            jsonObject = jsonArray.getJSONObject(i);
            if (!jsonObject.getString("UserName").contains("@@") && jsonObject.getInt("VerifyFlag") == 0) {
                UserInfo userInfo = new UserInfo();
                userInfo.setNickName(jsonObject.getString("NickName"));
                userInfo.setUserId(jsonObject.getString("UserName"));
                userInfo.setSignature(jsonObject.getString("Signature"));
                userInfoList.add(userInfo);
            } else if (jsonObject.getString("UserName").contains("@@")
                    && !activeGroupId.contains(jsonObject.getString("UserName"))
                    && !unactiveGroupId.contains(jsonObject.getString("UserName")))
                unactiveGroupId.add(jsonObject.getString("UserName"));
        }
    }

    /**
     * 获取skey，wxsid，wxuid等key
     */
    private void produceKey() {
        String s = this.sendGetRequest(url);
        List<String> l = httpRequest.getConnection().getHeaderFields().get("Set-Cookie");
        for (String s1 : l) {
            s1 = s1.substring(0, s1.indexOf(";"));
            if (!s1.equals(null) && s1 != null) {
                header = header + s1 + ";";
            }
        }
        header = header.substring(4);// 去掉null
        skey = s.substring(s.indexOf("<skey>"), s.indexOf("</skey>"));
        skey = skey.replace("<skey>", "").trim();
        wxsid = s.substring(s.indexOf("<wxsid>"), s.indexOf("</wxsid>"));
        wxsid = wxsid.replace("<wxsid>", "").trim();
        wxuin = s.substring(s.indexOf("<wxuin>"), s.indexOf("</wxuin>"));
        wxuin = wxuin.replace("<wxuin>", "").trim();
        pass_ticket = s.substring(s.indexOf("<pass_ticket>"), s.indexOf("</pass_ticket>"));
        pass_ticket = pass_ticket.replace("<pass_ticket>", "").trim();
    }

    /**
     * 初始化微信
     */
    private void initWeChat() {
        timeStamp = String.valueOf(System.currentTimeMillis());
        url = "https://" + host + "/cgi-bin/mmwebwx-bin/webwxinit?pass_ticket=" + pass_ticket;
        DeviceID = "e" + this.produceDevID().substring(1);
        js = new JSONObject();
        baseRequest.put("Uin", wxuin);
        baseRequest.put("Sid", wxsid);
        baseRequest.put("Skey", skey);
        baseRequest.put("DeviceID", DeviceID.toString());
        js.put("BaseRequest", baseRequest);
        String s = this.sendPostRequest(url, js, header);
        this.syncKeyTransfer(s);

        String s3[] = s.split(",");
        List<String> usrName = new ArrayList<>();
        for (String usrname : s3) {
            if (usrname.contains("UserName"))
                usrName.add(usrname);
        }
        // 获取一部分群组ID
        for (String id : usrName) {
            if (id.contains("@@")) {
                String sinId = (id.split(":"))[1].replace("\"", "").trim();
                activeGroupId.add(sinId);
            }
        }
        // 获取userID
        JSONObject sc = JSONObject.fromObject(s);
        JSONObject user = sc.getJSONObject("User");
        userID = user.get("UserName").toString();
        userName_wx = user.get("NickName").toString();
        // 开启微信状态通知
        url = "https://" + host + "/cgi-bin/mmwebwx-bin/webwxstatusnotify?lang=zh_CN&pass_ticket=" + pass_ticket;
        js.put("Code", 3);
        js.put("FromUserName", userID);
        js.put("ToUserName", userID);
        js.put("ClientMsgId", Long.valueOf(timeStamp));
        this.sendPostRequest(url, js, header);
        this.getHeaderImg();
    }

    /**
     * 获取所有群列表
     */
    private void getGroupList() {
        // jTextArea.setText("");
        url = "https://" + host + "/cgi-bin/mmwebwx-bin/webwxbatchgetcontact?type=ex&r=" + timeStamp
                + "&lang=zh_CN&pass_ticket=" + pass_ticket;
        js.clear();
        js.put("BaseRequest", baseRequest);
        js.put("Count", activeGroupId.size() + unactiveGroupId.size());
        JSONArray groupJs = new JSONArray();
        for (int i = 0; i < activeGroupId.size(); i++) {
            JSONObject single = new JSONObject();
            single.put("UserName", activeGroupId.get(i));
            single.put("ChatRoomId", "");
            groupJs.add(single);
        }
        for (int i = 0; i < unactiveGroupId.size(); i++) {
            JSONObject single = new JSONObject();
            single.put("UserName", unactiveGroupId.get(i));
            single.put("EncryChatRoomId", "");
            groupJs.add(single);
        }
        js.put("List", groupJs);
        String s = this.sendPostRequest(url, js);
        JSONObject jsonObject = JSONObject.fromObject(s);
        JSONArray contactList = jsonObject.getJSONArray("ContactList");
        String groupName;
        for (int i = 0; i < contactList.size(); i++) {
            GroupInfo groupInfo = new GroupInfo();
            groupInfo.setAcrossGroupFlag(false);
            groupInfo.setGroupID(contactList.getJSONObject(i).getString("UserName"));
            groupInfo.setMemberCount(contactList.getJSONObject(i).getInt("MemberCount"));
            groupName = contactList.getJSONObject(i).getString("NickName");
            groupInfo.setGroupName(groupName.equals("") ? "群聊" : groupName);
            JSONArray jsonArray = contactList.getJSONObject(i).getJSONArray("MemberList");
            for (int j = 0; j < jsonArray.size(); j++) {
                UserInfo userInfo = new UserInfo();
                userInfo.setNickName(jsonArray.getJSONObject(j).getString("NickName"));
                userInfo.setUserId(jsonArray.getJSONObject(j).getString("UserName"));
                userInfo.setRemarkName(jsonArray.getJSONObject(j).getString("DisplayName"));
                userInfo.setRemarkName(userInfo.getRemarkName().equals("") ? userInfo.getNickName():userInfo.getRemarkName());
                groupInfo.getGroup().add(userInfo);
            }
            groupInfoList.add(groupInfo);
        }
    }

    /**
     * 显示二维码
     * @param url 二维码获取地址
     */
    private void showPic(String url) {
        ImageIcon image = new ImageIcon(HttpRequest.get(url).bytes());
        if (updatePic) {
            windowUI.getjPanel().remove(windowUI.getjLabel_0());
            updatePic = false;
        }
        windowUI.setjLabel_0(new JLabel(new ImageIcon(image.getImage().getScaledInstance(350, 350, Image.SCALE_FAST))));
        windowUI.getjPanel().add(windowUI.getjLabel_0());
        windowUI.getMainFrame().setVisible(true);
        windowUI.getjPanel().validate();
        windowUI.getjPanel().repaint();
        windowUI.getMainFrame().setSize(400, 400);
    }

    /**
     * 获取用户头像
     */
    private void getHeaderImg() {
        url = "https://" + host + "/cgi-bin/mmwebwx-bin/webwxgeticon?username=" + userID + "&skey=" + skey;
        ImageIcon image = new ImageIcon(HttpRequest.get(url).header("Cookie", header).bytes());
        windowUI.setUserHeaderImg(
                new JLabel(new ImageIcon(image.getImage().getScaledInstance(70, 70, Image.SCALE_FAST))));
        GridBagConstraints gbc = new GridBagConstraints();
        windowUI.getUserInfoJPanel().add(windowUI.getUserHeaderImg());
        windowUI.setUserNameLabel(new JLabel("用户名：" + userName_wx));
        windowUI.getUserNameLabel().setFont(new Font("黑体", 1, 16));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.ipadx = 0;
        gbc.ipady = 0;
        windowUI.getUserNameJPanel().add(windowUI.getUserNameLabel(),gbc);
        gbc.insets = new Insets(10,20,10,20);
        windowUI.getUserButtonJPanel().add(windowUI.getLogin());
        windowUI.getUserButtonJPanel().add(windowUI.getSynchronization());
        gbc.gridx = 0;
        gbc.gridy = 1;
        windowUI.getUserNameJPanel().add(windowUI.getUserButtonJPanel(),gbc);
        windowUI.getUserInfoJPanel().add(windowUI.getUserNameJPanel());
    }

    /**
     * 获取uuid
     * @return uuid
     */
    private String produceUuid() {
        timeStamp = String.valueOf(System.currentTimeMillis());
        url = "https://login.wx.qq.com/jslogin?appid=wx782c26e4c19acffb&fun=new&lang=zh_CN&_=" + timeStamp;// request??????
        String s = this.sendGetRequest(url);
        if (StringKit.isBlank(s))
            return null;
        uuid = s.substring(s.length() - 14, s.length() - 2);
        s = this.produceErWei(uuid);
        return s;
    }
    /**
     * 搜索并对结果进行排序
     */
    private static void searchAndOrder(String s,Vector<String> nameList,Vector<String> searchList){
        try{
            String keys[] = analyzer.segment(s,"|").split("|");
            HashMap<String,Integer> map = new HashMap<>();
            for(int i = 0;i<nameList.size();i++){
                int count = 0;
                for(int j = 1;j<keys.length-1;j++){
                    if(nameList.get(i).contains(keys[j]))
                        count++;
                }
                if(count!=0)
                    map.put(nameList.get(i),count);
            }
            Set<Map.Entry<String,Integer>> mapEntries = map.entrySet();
            List<Map.Entry<String,Integer>> aList = new LinkedList<Map.Entry<String, Integer>>(mapEntries);
            //对得到的搜索结果进行排序
            Collections.sort(aList, new Comparator<Map.Entry<String, Integer>>() {
                @Override
                public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                    return o2.getValue().compareTo(o1.getValue());
                }
            });
            searchList.clear();
            for(int i = 0;i<aList.size();i++) {
                searchList.add(aList.get(i).getKey());
            }
        }catch(Exception e1){
            e1.printStackTrace();
        }
    }
    /**
     * 退出微信
     */
    private void exitWeChat() {
        url = "https://" + host + "/cgi-bin/mmwebwx-bin/webwxlogout?redirect=1&type=0&skey=" + skey;
        HttpRequest.post(url).header("Cookie", header).send("sid=" + wxsid + "&uin=" + wxuin).body();
    }

    /**
     * 开个线程监听最新消息
     */
    private void listenForMsg() {
        checkForMsgThread = new Thread(new Runnable() {

            @Override
            public void run() {
                while (true) {
                    checkMsg();
                    for (int i = 0; i < tipRecordList.size(); i++) {
                        //指定时间发布
                        if (!tipRecordList.get(i).getFlag() && windowUI.getDf().format(new Date()).toString()
                                .equals(tipRecordList.get(i).getTime())&&tipRecordList.get(i).getPeriod().equals("")) {
                            for (int j = 0; j < groupInfoList.size(); j++) {
                                if (groupInfoList.get(j).getGroupName().equals(tipRecordList.get(i).getGroupName())) {
                                    tipGroupID = groupInfoList.get(j).getGroupID();
                                    break;
                                }
                            }
                            replyMsg(tipRecordList.get(i).getProperty(), tipGroupID);
                            tipRecordList.get(i).setFlag(true);
                        }
                        //间隔时间发布
                        else if(windowUI.getDf().format(new Date()).toString()
                                .equals(tipRecordList.get(i).getTime())&&!tipRecordList.get(i).getPeriod().equals("")){
                            for (int j = 0; j < groupInfoList.size(); j++) {
                                if (groupInfoList.get(j).getGroupName().equals(tipRecordList.get(i).getGroupName())) {
                                    tipGroupID = groupInfoList.get(j).getGroupID();
                                    break;
                                }
                            }
                            int period = Integer.parseInt(tipRecordList.get(i).getPeriod());
                            replyMsg(tipRecordList.get(i).getProperty(),tipGroupID);
                            int startHour = Integer.parseInt(tipRecordList.get(i).getTime().split(":")[0]);
                            String minute = tipRecordList.get(i).getTime().split(":")[1];
                            String nextHour = String.valueOf(startHour + period);
                            tipRecordList.get(i).setTime(nextHour+":"+minute);
                        }
                    }
                }
            }
        }, "listenForMsg");
        checkForMsgThread.start();
    }

    public static void main(String[] args) {
        dbConnect = new DBConnect();
        dbConnect.connectDB();
        Properties properties = System.getProperties();
        String info = properties.getProperty("user.name")+properties.getProperty("os.version");
        System.out.println(info);
        try {
            if (!dbConnect.checkProgram(info)) {
                JOptionPane.showMessageDialog(null,"提示：软件试用期已到","信息提示",JOptionPane.INFORMATION_MESSAGE);
                System.exit(1);
            }
        }catch (Exception et){
            et.printStackTrace();
        }
        loadingDialogJFrame = new LoadingDialogJFrame("载入二维码...");
        System.setProperty("jsse.enableSNIExtension", "false"); // 设置该属性，避免出现unrecognized_name异常
        System.setProperty("https.protocols", "TLSv1"); // 设置协议为TLSv1，与服务器对应，避免出现与服务器断连的异常
        //System.setProperty("javax.net.debug","ssl");

        final Main htmlUnit = new Main();
        try {
            String s;
            out: while (true) {
                s = htmlUnit.produceUuid();

                // uuid获取失败
                if (s == null) {
                    System.out.println("uuid获取失败！");
                    // loadingDialogJFrame.shutdown("程序启动失败！请重新启动！");
                    return;
                }

                htmlUnit.showPic(s);
                if (loadingDialogJFrame != null)
                    loadingDialogJFrame.dispose();
                int tip = 1;
                url = "https://login.wx.qq.com/cgi-bin/mmwebwx-bin/login?uuid=" + uuid + "&tip=" + tip + "&_="
                        + timeStamp;
                while (true) {
                    s = htmlUnit.sendPostRequest(url, null);

                    // 响应为空
                    if (StringKit.isBlank(s)) {
                        System.out.println("扫码失败！");
                        continue;
                    }

                    // 扫码成功
                    if (s.contains("201")) {
                        // String s2[]=s.split("'");
                        // userAvatar =s2[1];
                        tip = 0;
                        continue;
                    }

                    // 确认登录成功
                    if (s.contains("200")) {
                        String s1[] = s.split("\"");
                        if (s1.length == 3) {
                            url = s1[1] + "&fun=new&version=v2&lang=zh_CN";
                            URL u = new URL(url);
                            host = u.getHost();
                        }
                        loadingDialogJFrame.setLoadingText("正在登录...");
                        windowUI.getMainFrame().setVisible(false);
                        htmlUnit.produceKey();
                        break out;
                    }

                    // 二维码已经失效
                    if (s.contains("400")) {
                        updatePic = true;
                        continue out;
                    }
                }
            }
            loadingDialogJFrame.setLoadingText("登录成功，正在初始化...");
            htmlUnit.initWeChat();
            loadingDialogJFrame.setLoadingText("初始化已完成，正在启动主程序...");
            htmlUnit.getRecentList();
            htmlUnit.checkMsg();
            htmlUnit.readFiles();
            htmlUnit.listenForMsg();
            windowUI.getjPanel().remove(windowUI.getjLabel_0());
            windowUI.getMainFrame().remove(windowUI.getjPanel());
            windowUI.getMainFrame().setSize(400, 700);
            windowUI.getGb().gridx = 0;
            windowUI.getGb().gridy = 0;
            windowUI.getGb().gridwidth = 0;
            windowUI.getGb().gridheight = GridBagConstraints.BOTH;
            windowUI.getGb().ipady = 200;
            windowUI.getGb().ipadx = 480;
            windowUI.getMainFrame().add(windowUI.getUserInfoJPanel(), windowUI.getGb());
            windowUI.getjPanel().setLayout(new FlowLayout(FlowLayout.CENTER, 15, 20));
            windowUI.getjPanel().add(windowUI.getjPanel_1());
            windowUI.getjPanel().add(windowUI.getjPanel_2());
            windowUI.getjPanel().add(windowUI.getjPanel_9());
            windowUI.getjPanel().add(windowUI.getjPanel_3());
//			windowUI.getjPanel().add(windowUI.getjPanel_4());
            windowUI.getjPanel().add(windowUI.getjPanel_5());
            windowUI.getjPanel().add(windowUI.getjPanel_6());
            windowUI.getjPanel().add(windowUI.getjPanel_7());
            windowUI.getjPanel().add(windowUI.getjPanel_10());
            windowUI.getjPanel().add(windowUI.getjPanel_11());
            windowUI.getjPanel().add(windowUI.getjPanel_12());
            windowUI.getjPanel().add(windowUI.getjPanel_8());
            windowUI.getjPanel().add(windowUI.getjPanel_4());
            windowUI.getGb().gridx = 0;
            windowUI.getGb().gridy = 1;
            windowUI.getGb().weightx = 4;
            windowUI.getGb().ipady = 500;
            windowUI.getMainFrame().add(windowUI.getjPanel(), windowUI.getGb());
            windowUI.getMainFrame().setVisible(true);
            loadingDialogJFrame.dispose();
            windowUI.getCheckSignRecord().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        loadingDialogJFrame.setLoadingText("正在读取签到记录......");
                        File record =  new File(userName_robot+"_群签到记录.csv");
                        if(!record.exists())
                            record.createNewFile();
                        record.setWritable(true);

                        FileOutputStream out = new FileOutputStream(record);
                        OutputStreamWriter writer;
                        String encoding = System.getProperty("file.encoding");
                        byte[] bom = new byte[3];

                        if(record.length()==0){
                            writer = new OutputStreamWriter(out);
                            if(encoding.equalsIgnoreCase("UTF-8")){
                                // 添加utf-8的bom头，避免office乱码
                                bom[0]=(byte) 0xEF;
                                bom[1]=(byte) 0xBB;
                                bom[2]=(byte) 0xBF;
                                writer.write(new String(bom));
                            }
                            writer.write("签到日期,群名,群成员\r\n");
                        }
                        else{
                            InputStream reader=new FileInputStream(record);
                            reader.read(bom,0,bom.length);
                            switch (bom[0]){
                                case (byte)0xEF:
                                    encoding="UTF-8";
                                    break;
                                case (byte)0xFE:
                                    encoding="UTF-16BE";
                                    break;
                                case (byte)0xFF:
                                    encoding="UTF-16LE";
                                    break;
                                default:
                                    encoding="GBK";
                                    break;
                            }
                            reader.close();
                            writer=new OutputStreamWriter(out,encoding);
                        }
                        dbConnect.setStatement(dbConnect.getConnection().createStatement());
                        String sql = "SELECT  * FROM "+ userName_robot +"_info";
                        ResultSet rs = dbConnect.getStatement().executeQuery(sql);
                        loadingDialogJFrame.setLoadingText("读取完成，正在载入签到记录......");
                        while(rs.next())
                            writer.write(rs.getTimestamp(3)+","+rs.getString(1)+","+rs.getString(2)+"\r\n");
                        writer.flush();
                        writer.close();
                        out.close();
                        record.setWritable(false);
                        loadingDialogJFrame.dispose();
                        Runtime.getRuntime().exec("cmd /c start " + userName_robot+"_群签到记录.csv");
                    }catch(Exception e1){
                        e1.printStackTrace();
                    }
                }
            });
            windowUI.getSignRecord().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(!isLogin){
                        JOptionPane.showMessageDialog(null,"请您先登录","信息提示",JOptionPane.INFORMATION_MESSAGE);
                    }
                    else {
                        windowUI.getSignFrame().setVisible(true);
                        windowUI.getSignRecord().setEnabled(false);
                    }
                }
            });
            windowUI.getOpenSign().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    isSign=true;
                    loadingDialogJFrame.setSuccessText("签到功能已开启！");
                }
            });
            windowUI.getCloseSign().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    isSign= false;
                    loadingDialogJFrame.setSuccessText("签到功能已关闭！");
                }
            });
            windowUI.getLogin().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(!windowUI.getLoginFrame().isVisible())
                        windowUI.getLoginFrame().setVisible(true);
                }
            });
            windowUI.getLog().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        String s = new String(windowUI.getUserPasswdArea().getPassword());
                        if (dbConnect.checkLogin(windowUI.getUserNameArea().getText().trim(), s.trim())) {
                            loadingDialogJFrame.setSuccessText("登陆成功！");
                            windowUI.getLogin().setEnabled(false);
                            isLogin = true;
                            isSign = true;
                            userName_robot = windowUI.getUserNameArea().getText().trim();
                            windowUI.getLoginFrame().setVisible(false);
                            windowUI.getUserPasswdArea().setText("");
                        }
                        else{
                            JOptionPane.showMessageDialog(null,"用户不存在或密码错误","信息提示",JOptionPane.ERROR_MESSAGE);
                            windowUI.getUserPasswdArea().setText("");
                            windowUI.getUserNameArea().setText("");
                        }
                    }catch (Exception e1){
                        e1.printStackTrace();
                    }
                }
            });
            windowUI.getRegister().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(windowUI.getUserNameArea().getText().trim().length()==0){
                        JOptionPane.showMessageDialog(null,"用户名不能为空","信息提示",JOptionPane.ERROR_MESSAGE);
                    }
                    else if(windowUI.getUserPasswdArea().getPassword().toString().trim().length()<8){
                        JOptionPane.showMessageDialog(null,"密码长度要大于等于8","信息提示",JOptionPane.ERROR_MESSAGE);
                    }
                    else {
                        try {
                            String s = new String(windowUI.getUserPasswdArea().getPassword());
                            dbConnect.insertRegisterRecord(windowUI.getUserNameArea().getText().trim(), s.trim());
                            userName_robot=  windowUI.getUserNameArea().getText().trim();
                            loadingDialogJFrame.setSuccessText("恭喜：您已注册成功！");
                            isLogin = true;
                            isSign =true;
                            windowUI.getLogin().setEnabled(false);
                            windowUI.getLogin().setForeground(Color.black);
                            windowUI.getLoginFrame().setVisible(false);
                            windowUI.getUserPasswdArea().setText("");
                        }catch (Exception e1){
                            e1.printStackTrace();
                            JOptionPane.showMessageDialog(null, "错误：用户名已存在!", "信息提示", JOptionPane.ERROR_MESSAGE);
                            windowUI.getUserPasswdArea().setText("");
                        }
                    }
                }
            });
            windowUI.getSynchronization().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    publicReply.clear();
                    privateReply.clear();
                    senseReply.clear();
                    try{
                        htmlUnit.readFiles();
                        JOptionPane.showMessageDialog(null,"提示:数据同步成功！","信息提示",JOptionPane.INFORMATION_MESSAGE);
                    }catch (Exception e1){
                        e1.printStackTrace();
                    }
                }
            });
            windowUI.getSet().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    windowUI.getSetFrame().setVisible(true);
                    windowUI.getSet().setEnabled(false);
                    windowUI.getAcrossGroupListJPanel().removeAll();
                    windowUI.getSetGroupNamePanel().removeAll();
                    // 对于群名修改面板的组件改变
                    JPanel groupListJPanel = new JPanel(); // 群名列表
                    groupListJPanel.setLayout(new BoxLayout(groupListJPanel,BoxLayout.Y_AXIS));
                    JScrollPane groupListScrollPane = new JScrollPane(groupListJPanel);
                    groupListScrollPane.setUI(new BEScrollPaneUI());
                    groupListScrollPane.setBorder(BorderFactory.createTitledBorder("群聊列表"));
                    final JPanel groupMemberListJPanel = new JPanel(); // 群成员列表
                    groupMemberListJPanel.setLayout(new BoxLayout(groupMemberListJPanel,BoxLayout.Y_AXIS));
                    JScrollPane groupMemberListScrollPane = new JScrollPane(groupMemberListJPanel);
                    groupMemberListScrollPane.setUI(new BEScrollPaneUI());
                    groupMemberListScrollPane.setBorder(BorderFactory.createTitledBorder("群成员列表"));
                    windowUI.getAcrossGroupListJPanel().add(groupListScrollPane);
                    windowUI.getAcrossGroupListJPanel().add(groupMemberListScrollPane);
                    for (final GroupInfo group : groupInfoList) {
                        final JLabel jLabel = new JLabel(group.getGroupName());
                        jLabel.setBorder(BorderFactory.createTitledBorder("原群名"));
                        final JTextField jTextArea = new JTextField(20);
                        jTextArea.setBorder(BorderFactory.createTitledBorder("新群名"));
                        JButton jButton = new JButton("修改群名");
                        jButton.setForeground(Color.white);
                        jButton.setUI(new BEButtonUI().setNormalColor(BEButtonUI.NormalColor.red));
                        windowUI.getSetGroupNamePanel().add(jLabel);
                        windowUI.getSetGroupNamePanel().add(jTextArea);
                        windowUI.getSetGroupNamePanel().add(jButton);
                        jButton.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                if (!jTextArea.getText().equals("") || jTextArea.getText() != null) {
                                    htmlUnit.modifyChatroomName(group.getGroupID(), jTextArea.getText());
                                    jLabel.setText(jTextArea.getText());
                                    group.setGroupName(jTextArea.getText());
                                    jTextArea.setText("");
                                    JOptionPane.showMessageDialog(null,"提示：群名修改成功","提示",JOptionPane.INFORMATION_MESSAGE);
                                } else {
                                    JOptionPane.showMessageDialog(new Frame(), "错误：新群名一栏不能为空", "错误", JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        });
                        // 添加跨群设置列表
                        final JCheckBox groupCheckBox = new JCheckBox(group.getGroupName());
                        groupCheckBox.setSelected(group.getAcrossGroupFlag());
                        groupListJPanel.add(groupCheckBox);
                        groupCheckBox.addChangeListener(new ChangeListener() {
                            @Override
                            public void stateChanged(ChangeEvent arg0) {
                                group.setAcrossGroupFlag(groupCheckBox.isSelected());
                            }
                        });
                        groupCheckBox.addActionListener(new ActionListener() {

                            @Override
                            public void actionPerformed(ActionEvent e) {
                                groupMemberListJPanel.removeAll();
                                for (GroupInfo groupInfo : groupInfoList)
                                    if (groupInfo.getGroupName().equals(groupCheckBox.getText())
                                            && groupInfo.getAcrossGroupFlag()) {
                                        for (final UserInfo userInfo : groupInfo.getGroup()) {
                                            final JCheckBox memberCheckBox = new JCheckBox(userInfo.getNickName());
                                            memberCheckBox.setSelected(userInfo.isAcrossGroupFlag());
                                            groupMemberListJPanel.add(memberCheckBox);
                                            memberCheckBox.addChangeListener(new ChangeListener() {

                                                @Override
                                                public void stateChanged(ChangeEvent changeevent) {
                                                    userInfo.setAcrossGroupFlag(memberCheckBox.isSelected());
                                                }
                                            });
                                        }
                                        break;
                                    }
                                groupMemberListJPanel.updateUI(); // 立即刷新页面
                            }
                        });

                    }
                }
            });


            windowUI.getInviteIntoGroup().addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    windowUI.getInviteIntoGroup().setEnabled(false);
                    group.clear();
                    for (int i = 0; i < groupInfoList.size(); i++)
                        group.add(groupInfoList.get(i).getGroupName());
                    windowUI.setjList2(new JList(group));
                    windowUI.getjList2().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                    windowUI.getjList1().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
                    windowUI.setInviteScrollPane1(new JScrollPane(windowUI.getjList1()));
                    windowUI.setInviteScrollPane2(new JScrollPane(windowUI.getjList2()));
                    windowUI.getInviteScrollPane1().setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                    windowUI.getInviteScrollPane1()
                            .setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                    windowUI.getInviteScrollPane2().setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                    windowUI.getInviteScrollPane2()
                            .setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                    windowUI.getInvitePanel().removeAll();
                    windowUI.getGb().gridy = GridBagConstraints.NONE;
                    windowUI.getGb().gridx = GridBagConstraints.NONE;
                    windowUI.getGb().gridheight = GridBagConstraints.BOTH;
                    windowUI.getGb().gridwidth = GridBagConstraints.BOTH;
                    windowUI.getGb().ipady=0;
                    windowUI.getGb().ipadx=355;
                    windowUI.getGb().gridx =0;
                    windowUI.getGb().gridy =0;
//                    windowUI.getInvitePanel().add(windowUI.getSearchInviteUser(),windowUI.getGb());
                    windowUI.getGb().gridx =1;
//                    windowUI.getInvitePanel().add(windowUI.getSearchInviteGroup(),windowUI.getGb());
                    windowUI.getGb().ipady = 450;
                    windowUI.getGb().ipadx= 330;
                    windowUI.getGb().gridx = 0;
                    windowUI.getGb().gridy =1;
                    windowUI.getInvitePanel().add(windowUI.getInviteScrollPane1(),windowUI.getGb());
                    windowUI.getGb().gridx = 1;
                    windowUI.getInvitePanel().add(windowUI.getInviteScrollPane2(),windowUI.getGb());
                    windowUI.getInviteScrollPane1().setBorder(BorderFactory.createTitledBorder("好友昵称"));
                    windowUI.getInviteScrollPane2().setBorder(BorderFactory.createTitledBorder("群名称"));
                    windowUI.getGroupInvite().setVisible(true);
                    windowUI.getjList1().addListSelectionListener(new ListSelectionListener() {
                        @Override
                        public void valueChanged(ListSelectionEvent e) {
                            inviteFriendList.clear();
                            int inviteUserIndex[] = windowUI.getjList1().getSelectedIndices();
                            out:for(int i = 0;i<inviteUserIndex.length;i++)
                                in:for(UserInfo userInfo:userInfoList) {
                                    if (searchInviteFriend.get(inviteUserIndex[i]).equals(userInfo.getNickName())) {
                                        inviteFriendList.add(userInfo);
                                        break in;
                                    }
                                }

                        }
                    });
                    windowUI.getjList2().addListSelectionListener(new ListSelectionListener() {
                        @Override
                        public void valueChanged(ListSelectionEvent e) {
                            inviteFriendName.clear();
                            for(int j = 0;j<userInfoList.size();j++){
                                inviteFriendName.add(userInfoList.get(j).getNickName());
                            }
                            out:for(GroupInfo groupInfo:groupInfoList) {
                                if(groupInfo.getGroupName().equals(windowUI.getjList2().getSelectedValue().toString())) {
                                    invitedGroup = groupInfo;
                                    in:for (int i = 0; i < groupInfo.getGroup().size(); i++)
                                        inviteFriendName.remove(groupInfo.getGroup().get(i).getNickName());
                                    break out;
                                }
                            }
                            searchInviteFriend = (Vector<String>)inviteFriendName.clone();
                            windowUI.getjList1().setListData(searchInviteFriend);
                            windowUI.getInviteScrollPane1().repaint();
                        }
                    });
                }
            });
            windowUI.getSearchInviteUser().getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    if(!windowUI.getSearchInviteUser().getText().equals(""))
                        searchAndOrder(windowUI.getSearchInviteUser().getText(),inviteFriendName,searchInviteFriend);
                    else
                        searchInviteFriend = (Vector<String>)inviteFriendName.clone();
                    windowUI.getjList1().setListData(searchInviteFriend);
                    windowUI.getInviteScrollPane1().repaint();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    if(!windowUI.getSearchInviteUser().getText().equals(""))
                        searchAndOrder(windowUI.getSearchInviteUser().getText(),inviteFriendName,searchInviteFriend);
                    else
                        searchInviteFriend = (Vector<String>)inviteFriendName.clone();
                    windowUI.getjList1().setListData(searchInviteFriend);
                    windowUI.getInviteScrollPane1().repaint();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    if(!windowUI.getSearchInviteUser().getText().equals(""))
                        searchAndOrder(windowUI.getSearchInviteUser().getText(),inviteFriendName,searchInviteFriend);
                    else
                        searchInviteFriend = (Vector<String>)inviteFriendName.clone();
                    windowUI.getjList1().setListData(searchInviteFriend);
                    windowUI.getInviteScrollPane1().repaint();
                }
            });
            windowUI.getSearchInviteGroup().getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    if(!windowUI.getSearchInviteGroup().getText().equals(""))
                        htmlUnit.searchAndOrder(windowUI.getSearchInviteGroup().getText(),group,searchInvitedGroup);
                    else
                        searchInvitedGroup =(Vector<String>)group.clone();
                    windowUI.getjList2().setListData(searchInvitedGroup);
                    windowUI.getInviteScrollPane2().repaint();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    if(!windowUI.getSearchInviteGroup().getText().equals(""))
                        htmlUnit.searchAndOrder(windowUI.getSearchInviteGroup().getText(),group,searchInvitedGroup);
                    else
                        searchInvitedGroup =(Vector<String>)group.clone();
                    windowUI.getjList2().setListData(searchInvitedGroup);
                    windowUI.getInviteScrollPane2().repaint();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    if(!windowUI.getSearchInviteGroup().getText().equals(""))
                        htmlUnit.searchAndOrder(windowUI.getSearchInviteGroup().getText(),group,searchInvitedGroup);
                    else
                        searchInvitedGroup =(Vector<String>)group.clone();
                    windowUI.getjList2().setListData(searchInvitedGroup);
                    windowUI.getInviteScrollPane2().repaint();
                }
            });
            windowUI.getInvite().addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!invitedGroup.equals(null) && inviteFriendList.size()!=0) {
                        String s = "";
                        out:for(int i = 0;i<inviteFriendList.size();i++) {
                            s += inviteFriendList.get(i).getUserId() + ",";
                            searchInviteFriend.remove(inviteFriendList.get(i).getNickName());
                            inviteFriendName.remove(inviteFriendList.get(i).getNickName());
                            in:for(GroupInfo groupInfo:groupInfoList)
                                if(groupInfo.getGroupID().equals(invitedGroup.getGroupID())) {
                                    groupInfo.getGroup().add(inviteFriendList.get(i));
                                    break in;
                                }
                        }
                        htmlUnit.addMember(s.substring(0,s.length()-1), invitedGroup.getGroupID());
                        windowUI.getSearchInviteUser().setText("");
                        windowUI.getSearchInviteGroup().setText("");
                        windowUI.getjList1().setListData(inviteFriendName);
                        windowUI.getInviteScrollPane1().repaint();
                        JOptionPane.showMessageDialog(null, "提示：邀请群成员成功！", "信息提示", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            });

            windowUI.getRemoveFromGroup().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    windowUI.getRemoveFromGroup().setEnabled(false);
                    group.clear();
                    for (int i = 0; i < groupInfoList.size(); i++)
                        group.add(groupInfoList.get(i).getGroupName());
                    windowUI.setjList3(new JList(group));
                    windowUI.getjList3().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                    windowUI.getjList4().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
                    windowUI.setRemoveScrollPane1(new JScrollPane(windowUI.getjList3()));
                    windowUI.setRemoveScrollPane2(new JScrollPane(windowUI.getjList4()));
                    windowUI.getRemoveScrollPane1().setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                    windowUI.getRemoveScrollPane1()
                            .setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                    windowUI.getRemoveScrollPane2().setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                    windowUI.getRemoveScrollPane2()
                            .setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                    windowUI.getRemovePanel().removeAll();
                    windowUI.getGb().gridy = GridBagConstraints.NONE;
                    windowUI.getGb().gridx = GridBagConstraints.NONE;
                    windowUI.getGb().gridheight = GridBagConstraints.BOTH;
                    windowUI.getGb().gridwidth = GridBagConstraints.BOTH;
                    windowUI.getGb().ipady=0;
                    windowUI.getGb().ipadx=355;
                    windowUI.getGb().gridx =0;
                    windowUI.getGb().gridy =0;
//                    windowUI.getRemovePanel().add(windowUI.getSearchRemoveGroup(),windowUI.getGb());
                    windowUI.getGb().gridx =1;
//                    windowUI.getRemovePanel().add(windowUI.getSearchRemoveUser(),windowUI.getGb());
                    windowUI.getGb().ipady = 450;
                    windowUI.getGb().ipadx= 330;
                    windowUI.getGb().gridx = 0;
                    windowUI.getGb().gridy =1;
                    windowUI.getRemovePanel().add(windowUI.getRemoveScrollPane1(),windowUI.getGb());
                    windowUI.getGb().gridx = 1;
                    windowUI.getRemovePanel().add(windowUI.getRemoveScrollPane2(),windowUI.getGb());
                    windowUI.getRemoveScrollPane1().setBorder(BorderFactory.createTitledBorder("群名称"));
                    windowUI.getRemoveScrollPane2().setBorder(BorderFactory.createTitledBorder("好友昵称"));
                    windowUI.getGroupRemove().setVisible(true);

                    windowUI.getjList3().addListSelectionListener(new ListSelectionListener() {
                        @Override
                        public void valueChanged(ListSelectionEvent e) {
                            removeFriendName.clear();
                            out:for (GroupInfo groupInfo:groupInfoList) {
                                if(groupInfo.getGroupName().equals(windowUI.getjList3().getSelectedValue().toString())) {
                                    removeGroup = groupInfo;
                                    in:
                                    for (int i = 0; i < groupInfo.getGroup().size(); i++)
                                        removeFriendName.add(groupInfo.getGroup().get(i).getNickName());
                                    break out;
                                }
                            }
                            searchRemoveFriend = (Vector<String>)removeFriendName.clone();
                            windowUI.getjList4().setListData(searchRemoveFriend);
                            windowUI.getRemoveScrollPane2().repaint();
                        }
                    });
                    windowUI.getjList4().addListSelectionListener(new ListSelectionListener() {
                        @Override
                        public void valueChanged(ListSelectionEvent e) {
                            removeFriendList.clear();
                            int removeUserIndex[] = windowUI.getjList4().getSelectedIndices();
                            out:for(int i = 0;i<removeUserIndex.length;i++)
                                in:for(UserInfo userInfo:userInfoList)
                                    if(searchRemoveFriend.get(removeUserIndex[i]).equals(userInfo.getNickName())){
                                        removeFriendList.add(userInfo);
                                        break in;
                                    }
                        }
                    });
                    windowUI.getSearchRemoveUser().getDocument().addDocumentListener(new DocumentListener() {
                        @Override
                        public void insertUpdate(DocumentEvent e) {
                            if(!windowUI.getSearchRemoveUser().getText().equals(""))
                                htmlUnit.searchAndOrder(windowUI.getSearchRemoveUser().getText(),removeFriendName,searchRemoveFriend);

                            else
                                searchRemoveFriend = (Vector<String>)removeFriendName.clone();
                            windowUI.getjList4().setListData(searchRemoveFriend);
                            windowUI.getRemoveScrollPane2().repaint();

                        }

                        @Override
                        public void removeUpdate(DocumentEvent e) {
                            if(!windowUI.getSearchRemoveUser().getText().equals(""))
                                htmlUnit.searchAndOrder(windowUI.getSearchRemoveUser().getText(),removeFriendName,searchRemoveFriend);

                            else
                                searchRemoveFriend = (Vector<String>)removeFriendName.clone();
                            windowUI.getjList4().setListData(searchRemoveFriend);
                            windowUI.getRemoveScrollPane2().repaint();
                        }

                        @Override
                        public void changedUpdate(DocumentEvent e) {
                            if(!windowUI.getSearchRemoveUser().getText().equals(""))
                                htmlUnit.searchAndOrder(windowUI.getSearchRemoveUser().getText(),removeFriendName,searchRemoveFriend);

                            else
                                searchRemoveFriend = (Vector<String>)removeFriendName.clone();
                            windowUI.getjList4().setListData(searchRemoveFriend);
                            windowUI.getRemoveScrollPane2().repaint();
                        }
                    });
                }
            });
            windowUI.getSearchRemoveGroup().getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    if(!windowUI.getSearchRemoveGroup().getText().equals(""))
                        htmlUnit.searchAndOrder(windowUI.getSearchRemoveGroup().getText(),group,searchRemoveGroup);
                    else
                        searchRemoveGroup = (Vector<String>)group.clone();
                    windowUI.getjList4().setListData(searchRemoveGroup);
                    windowUI.getRemoveScrollPane2().repaint();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    if(!windowUI.getSearchRemoveGroup().getText().equals(""))
                        htmlUnit.searchAndOrder(windowUI.getSearchRemoveGroup().getText(),group,searchRemoveGroup);
                    else
                        searchRemoveGroup = (Vector<String>)group.clone();
                    windowUI.getjList4().setListData(searchRemoveGroup);
                    windowUI.getRemoveScrollPane2().repaint();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    if(!windowUI.getSearchRemoveGroup().getText().equals(""))
                        htmlUnit.searchAndOrder(windowUI.getSearchRemoveGroup().getText(),group,searchRemoveGroup);
                    else
                        searchRemoveGroup = (Vector<String>)group.clone();
                    windowUI.getjList4().setListData(searchRemoveGroup);
                    windowUI.getRemoveScrollPane2().repaint();
                }
            });
            windowUI.getRemove().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!removeGroup.equals(null) && removeFriendList.size()!=0) {
                        String s = "";
                        for(int i = 0;i<removeFriendList.size();i++) {
                            s += removeFriendList.get(i).getUserId() + ",";
                            removeFriendName.remove(removeFriendList.get(i).getNickName());
                            searchRemoveGroup.remove(removeFriendList.get(i).getNickName());
                            for(GroupInfo groupInfo:groupInfoList)
                                if(groupInfo.getGroupID().equals(removeGroup.getGroupID())){
                                    groupInfo.getGroup().remove(removeFriendList.get(i));
                                    break;
                                }
                        }
                        if(htmlUnit.deleteMember(s.substring(0,s.length()-1), removeGroup.getGroupID())) {
                            windowUI.getSearchRemoveGroup().setText("");
                            windowUI.getSearchRemoveUser().setText("");
                            windowUI.getjList4().setListData(removeFriendName);
                            windowUI.getRemoveScrollPane2().repaint();
                            JOptionPane.showMessageDialog(null, "提示：删除群成员成功！", "信息提示", JOptionPane.INFORMATION_MESSAGE);
                        }
                        else
                            JOptionPane.showMessageDialog(null, "提示：删除群成员失败！您不是群主", "信息提示", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            });

            windowUI.getChatJButton().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        windowUI.getChatIn().setVisible(true);
                        windowUI.getChatJButton().setEnabled(false);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }

            });

            windowUI.getAddFriend().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        if (autoAddFriendFlag) {
                            autoAddFriendFlag = false;
                            windowUI.getAddFriend().setIcon(windowUI.getAddFriend().getDisabledIcon());
                            windowUI.getAddFriend().repaint();
                        } else {
                            autoAddFriendFlag = true;
                            windowUI.getAddFriend()
                                    .setIcon(new ImageIcon(WindowUI.class.getResource("resource/add_friend.png")));
                            windowUI.getAddFriend().repaint();
                        }
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            });

            windowUI.getReply().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (autoReplyFlag) {
                        autoReplyFlag = false;
                        windowUI.getReply().setIcon(windowUI.getReply().getDisabledIcon());
                        windowUI.getReply().repaint();
                    } else {
                        autoReplyFlag = true;
                        windowUI.getReply()
                                .setIcon(new ImageIcon(WindowUI.class.getResource("resource/reply.png")));
                        windowUI.getReply().repaint();
                    }
                }
            });

            windowUI.getAtModeBtn().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
//					if(atModeFlag){
//						atModeFlag = false;
//						windowUI.getAtModeBtn().setIcon(windowUI.getAtModeBtn().getDisabledIcon());
//						windowUI.getAtModeBtn().repaint();
//					}else{
//						atModeFlag = true;
//						windowUI.getAtModeBtn().setIcon(new ImageIcon(WindowUI.class.getResource("resource/auto_chat.png")));
//						windowUI.getAtModeBtn().repaint();
//					}
                }
            });

            windowUI.getWarn().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (sensitiveFlag) {
                        sensitiveFlag = false;
                        windowUI.getWarn().setIcon(windowUI.getWarn().getDisabledIcon());
                        windowUI.getWarn().repaint();
                    } else {
                        sensitiveFlag = true;
                        windowUI.getWarn().setIcon(new ImageIcon(WindowUI.class.getResource("resource/warn.png")));
                        windowUI.getWarn().repaint();
                    }
                }
            });

            windowUI.getSendByTime().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    windowUI.getSendByTime().setEnabled(false);
                    windowUI.getDailyTip().setVisible(true);
                    for (int i = 0; i < groupInfoList.size(); i++) {
                        windowUI.getGroupNamePeriodArea().addItem(groupInfoList.get(i).getGroupName());
                        windowUI.getGroupNameArea().addItem(groupInfoList.get(i).getGroupName());
                    }
                }
            });

            windowUI.getLocalWord().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    windowUI.getWordModify().setVisible(true);
                    windowUI.getLocalWord().setEnabled(false);
                }
            });

            windowUI.getAddTipTimeButton().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    TipRecord tipRecord = new TipRecord();
                    windowUI.addTimeTip(tipRecord);
                    try {
                        tipRecord.setTime(windowUI.getDf()
                                .format(windowUI.getDf().parse(windowUI.getTimeArea().getText())).toString());
                        tipRecord.setProperty(windowUI.getPropertyTimeArea().getText());
                        tipRecord.setGroupName(windowUI.getGroupNameArea().getSelectedItem().toString());
                        tipRecordList.add(tipRecord);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            });
            windowUI.getAddTipPeriodButton().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    TipRecord tipRecord = new TipRecord();
                    windowUI.addPeriodTip(tipRecord);
                    try{
                        tipRecord.setTime(windowUI.getDf().format(windowUI.getDf().parse(windowUI.getPeriodStartTime().getText())).toString());
                        tipRecord.setPeriod(windowUI.getPeriodTime().getText());
                        tipRecord.setGroupName(windowUI.getGroupNamePeriodArea().getSelectedItem().toString());
                        tipRecord.setProperty(windowUI.getPropertyPeriodArea().getText());
                        tipRecordList.add(tipRecord);
                    }catch(Exception e1){
                        e1.printStackTrace();
                    }
                }
            });
            windowUI.getShowGroupList().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    windowUI.getShowGroupList().setEnabled(false);
                    if (windowUI.getShowGroupPanel().getComponentCount() == 0) {
                        for (final GroupInfo groupInfo : groupInfoList) {
                            final JCheckBox jc = new JCheckBox(groupInfo.getGroupName());
                            jc.setSelected(false);
                            windowUI.getShowGroupPanel().add(jc);
                            jc.addChangeListener(new ChangeListener() {
                                @Override
                                public void stateChanged(ChangeEvent e) {
                                    if (!jc.isSelected())
                                        groupInfo.setFlag(false);
                                    else {
                                        groupInfo.setFlag(true);
                                    }
                                }
                            });
                        }
                    }
                    windowUI.getChooseGroup().setVisible(true);
                    windowUI.getChooseGroup().pack();
                }
            });

            windowUI.getSeeRecord().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        Runtime.getRuntime().exec("cmd /c start " + userName_wx + "_群聊天记录.csv");
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }

                }
            });
            windowUI.getModifyWarnCount().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    minSenseWarn = Integer.parseInt(windowUI.getMinWarnCount().getText());
                    maxSenseWarn = Integer.parseInt(windowUI.getMaxWarnCount().getText());
                }
            });
            windowUI.getModifyTulingKey().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    matcher=tulingKey.matcher(windowUI.getTulingKeyArea().getText());
                    if(!windowUI.getTulingKeyArea().getText().equals("")) {
                        if(windowUI.getTulingKeyArea().getText().length()!=32){
                            JOptionPane.showMessageDialog(null,"错误：图灵机器人API密钥应该由32位小写字母及0到9数字组成");
                            windowUI.getTulingKeyArea().setText("");
                        }
                        else if(!matcher.matches()){
                            JOptionPane.showMessageDialog(null,"错误：图灵机器人API密钥应该由小写字母及0到9数字组成");
                            windowUI.getTulingKeyArea().setText("");
                        }
                        apiKey = windowUI.getTulingKeyArea().getText();
                        loadingDialogJFrame.setSuccessText("图灵key已经成功修改！");
                    }
                    else
                        apiKey = "49d5dd04005a4d82b7d5bc30dae96821";
                }
            });

            windowUI.getSaveRecord().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        File record = new File(userName_wx + "_群聊天记录.csv");
                        if(!record.exists())
                            record.createNewFile();
                        record.setWritable(true);  //打开文件的写入权限

                        FileOutputStream out = new FileOutputStream(record,true);
                        OutputStreamWriter writer;
                        String encoding=System.getProperty("file.encoding");
                        byte[] bom=new byte[3];

                        if(record.length()==0) {
                            writer=new OutputStreamWriter(out);
                            if(encoding.equalsIgnoreCase("UTF-8")) {
                                // 添加utf-8的bom头，避免office乱码
                                bom[0]=(byte) 0xEF;
                                bom[1]=(byte) 0xBB;
                                bom[2]=(byte) 0xBF;
                                writer.write(new String(bom));
                            }
                            writer.write("时间,群名/发起人,群成员/接收人,内容\r\n");
                        } else {
                            InputStream reader=new FileInputStream(record);
                            reader.read(bom,0,bom.length);
                            switch (bom[0]) {
                                case (byte)0xEF:
                                    encoding="UTF-8";
                                    break;
                                case (byte)0xFE:
                                    encoding="UTF-16BE";
                                    break;
                                case (byte)0xFF:
                                    encoding="UTF-16LE";
                                    break;
                                default:
                                    encoding="GBK";
                                    break;
                            }
                            reader.close();
                            writer=new OutputStreamWriter(out,encoding);
                        }
                        for (String r : recordList)
                            writer.write((r + "\r\n"));
                        writer.flush();
                        writer.close();
                        out.close();
                        record.setWritable(false);  //关闭文件的写入权限，禁止外部程序修改文件

                        loadingDialogJFrame.setSuccessText("记录已保存到当前目录下，请查看！");
                        recordList.clear();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        JOptionPane.showMessageDialog(null, "记录保存失败，请检查原因后重试！", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            windowUI.getPublicWord().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        Runtime.getRuntime().exec("notepad.exe 公开.txt");
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            });

            windowUI.getPrivateWord().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        Runtime.getRuntime().exec("notepad.exe 私密.txt");
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            });

            windowUI.getSenseWord().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        Runtime.getRuntime().exec("notepad.exe 敏感词.txt");
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            });

            windowUI.getChatIn().addWindowListener(new WindowListener() {
                @Override
                public void windowOpened(WindowEvent e) {

                }

                @Override
                public void windowClosing(WindowEvent e) {
                    windowUI.getChatIn().setVisible(false);
                    windowUI.getChatJButton().setEnabled(true);
                }

                @Override
                public void windowClosed(WindowEvent e) {
                }

                @Override
                public void windowIconified(WindowEvent e) {

                }

                @Override
                public void windowDeiconified(WindowEvent e) {

                }

                @Override
                public void windowActivated(WindowEvent e) {

                }

                @Override
                public void windowDeactivated(WindowEvent e) {

                }
            });
            windowUI.getGroupInvite().addWindowListener(new WindowListener() {
                @Override
                public void windowOpened(WindowEvent e) {

                }

                @Override
                public void windowClosing(WindowEvent e) {
                    windowUI.getInviteIntoGroup().setEnabled(true);
                    windowUI.getGroupInvite().setVisible(false);
                }

                @Override
                public void windowClosed(WindowEvent e) {

                }

                @Override
                public void windowIconified(WindowEvent e) {

                }

                @Override
                public void windowDeiconified(WindowEvent e) {

                }

                @Override
                public void windowActivated(WindowEvent e) {

                }

                @Override
                public void windowDeactivated(WindowEvent e) {

                }
            });
            windowUI.getGroupRemove().addWindowListener(new WindowListener() {
                @Override
                public void windowOpened(WindowEvent e) {

                }

                @Override
                public void windowClosing(WindowEvent e) {
                    windowUI.getRemoveFromGroup().setEnabled(true);
                    windowUI.getGroupRemove().setVisible(false);
                }

                @Override
                public void windowClosed(WindowEvent e) {

                }

                @Override
                public void windowIconified(WindowEvent e) {

                }

                @Override
                public void windowDeiconified(WindowEvent e) {

                }

                @Override
                public void windowActivated(WindowEvent e) {

                }

                @Override
                public void windowDeactivated(WindowEvent e) {

                }
            });
            windowUI.getMainFrame().addWindowListener(new WindowListener() {
                @Override
                public void windowOpened(WindowEvent e) {

                }

                @Override
                public void windowClosing(WindowEvent e) {
                    checkForMsgThread.interrupt();
                    htmlUnit.exitWeChat();
                }

                @Override
                public void windowClosed(WindowEvent e) {
                }

                @Override
                public void windowIconified(WindowEvent e) {

                }

                @Override
                public void windowDeiconified(WindowEvent e) {

                }

                @Override
                public void windowActivated(WindowEvent e) {

                }

                @Override
                public void windowDeactivated(WindowEvent e) {

                }
            });
            windowUI.getSignFrame().addWindowListener(new WindowListener() {
                @Override
                public void windowOpened(WindowEvent e) {

                }

                @Override
                public void windowClosing(WindowEvent e) {
                    windowUI.getSignRecord().setEnabled(true);
                }

                @Override
                public void windowClosed(WindowEvent e) {
                    windowUI.getSignRecord().setEnabled(true);
                }

                @Override
                public void windowIconified(WindowEvent e) {

                }

                @Override
                public void windowDeiconified(WindowEvent e) {

                }

                @Override
                public void windowActivated(WindowEvent e) {

                }

                @Override
                public void windowDeactivated(WindowEvent e) {

                }
            });
            windowUI.getChooseGroup().addWindowListener(new WindowListener() {
                @Override
                public void windowOpened(WindowEvent e) {

                }

                @Override
                public void windowClosing(WindowEvent e) {
                    windowUI.getShowGroupList().setEnabled(true);
                    windowUI.getChooseGroup().setVisible(false);
                }

                @Override
                public void windowClosed(WindowEvent e) {

                }

                @Override
                public void windowIconified(WindowEvent e) {

                }

                @Override
                public void windowDeiconified(WindowEvent e) {

                }

                @Override
                public void windowActivated(WindowEvent e) {

                }

                @Override
                public void windowDeactivated(WindowEvent e) {

                }
            });
            windowUI.getDailyTip().addWindowListener(new WindowListener() {
                @Override
                public void windowOpened(WindowEvent e) {

                }

                @Override
                public void windowClosing(WindowEvent e) {
                    // timerSendMsgFlag = false;
                    windowUI.getDailyTip().setVisible(false);
                    windowUI.getSendByTime().setEnabled(true);
                    // windowUI.getGroupNameArea().removeAllItems();
                }

                @Override
                public void windowClosed(WindowEvent e) {
                }

                @Override
                public void windowIconified(WindowEvent e) {

                }

                @Override
                public void windowDeiconified(WindowEvent e) {

                }

                @Override
                public void windowActivated(WindowEvent e) {

                }

                @Override
                public void windowDeactivated(WindowEvent e) {

                }
            });
            windowUI.getWordModify().addWindowListener(new WindowListener() {
                @Override
                public void windowOpened(WindowEvent e) {

                }

                @Override
                public void windowClosing(WindowEvent e) {
                    windowUI.getLocalWord().setEnabled(true);
                }

                @Override
                public void windowClosed(WindowEvent e) {
                    windowUI.getLocalWord().setEnabled(true);
                }

                @Override
                public void windowIconified(WindowEvent e) {

                }

                @Override
                public void windowDeiconified(WindowEvent e) {

                }

                @Override
                public void windowActivated(WindowEvent e) {

                }

                @Override
                public void windowDeactivated(WindowEvent e) {

                }
            });
            windowUI.getSetFrame().addWindowListener(new WindowListener() {
                @Override
                public void windowOpened(WindowEvent e) {

                }

                @Override
                public void windowClosing(WindowEvent e) {
                    windowUI.getSet().setEnabled(true);
                }

                @Override
                public void windowClosed(WindowEvent e) {
                    windowUI.getSet().setEnabled(true);
                }

                @Override
                public void windowIconified(WindowEvent e) {

                }

                @Override
                public void windowDeiconified(WindowEvent e) {

                }

                @Override
                public void windowActivated(WindowEvent e) {

                }

                @Override
                public void windowDeactivated(WindowEvent e) {

                }
            });
            windowUI.getLoginFrame().addWindowListener(new WindowListener() {
                @Override
                public void windowOpened(WindowEvent e) {

                }

                @Override
                public void windowClosing(WindowEvent e) {
                    windowUI.getUserNameArea().setText("");
                    windowUI.getUserPasswdArea().setText("");
                }

                @Override
                public void windowClosed(WindowEvent e) {
                    windowUI.getUserNameArea().setText("");
                    windowUI.getUserPasswdArea().setText("");
                }

                @Override
                public void windowIconified(WindowEvent e) {

                }

                @Override
                public void windowDeiconified(WindowEvent e) {

                }

                @Override
                public void windowActivated(WindowEvent e) {

                }

                @Override
                public void windowDeactivated(WindowEvent e) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
