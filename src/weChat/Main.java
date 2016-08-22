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
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import Models.GroupInfo;
import Models.TipRecord;
import Models.UserInfo;
import Utils.Aes;
import Utils.Md5;
import blade.kit.http.HttpRequest;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.Document;


public class Main{
    private static Thread mainThread;
    private static HttpRequest httpRequest;
    private static Boolean statusNotifyFlag = false;
    private static Boolean updatePic = false;
    private static volatile Boolean flag = false;//控制消息更新
    private static volatile Boolean flag1 = false;//控制自动添加好友
    private static Boolean flag2 = true;
    private static Boolean flag3 = true;
    private static volatile Boolean flag4 = false;//控制定时发布
    private static Boolean flag5 = false;
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
    private static String userName="";
    private static String memberID="";
    private static String groupID="";
    private static String tipGroupID="";
    private static String msgType="";
    private static String msgID="";
    private static List<String> activeGroupId = new ArrayList<>();
    private static List<String> unactiveGroupId = new ArrayList<>();
    private static List<String> recordList = new ArrayList<>();
    private static List<UserInfo> userInfoList = new ArrayList<>();
    private static List<GroupInfo> groupInfoList = new ArrayList<>();
    private static Map<String,String> publicReply = new HashMap<>();
    private static Map<String,String> privateReply = new HashMap<>();
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
    private static String v_ticket="";
    private static String friendId;
    private static String invitedUserID="";
    private static String invitedGroupID="";
    private static String removeUserID="";
    private static String removeGroupID="";
    private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static List<TipRecord> tipRecordList = new ArrayList<>();
    private static Document doc;
//    private static String userAvatar;

    private static WindowUI windowUI;
    public Main() {
        windowUI = new WindowUI();
        doc = windowUI.getjTextPane().getDocument();
        try{
            doc.insertString(doc.getLength(),"您已登陆成功，开始记录消息!\n",windowUI.getAttributeSet());
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static List<TipRecord> getTipRecordList() {
        return tipRecordList;
    }

    private String sendGetRequest(String url) throws Exception {
        httpRequest =HttpRequest.get(url);
        return  httpRequest.body();
    }

    private String sendPostRequest(String url,JSONObject js) throws Exception {
        httpRequest = HttpRequest.post(url);
        if(js==null) {
            return httpRequest.body();
        }
        else {
            return httpRequest.send(js.toString()).body();
        }
    }

    private String sendPostRequest(String url,JSONObject js,String header) throws Exception {
        httpRequest = HttpRequest.post(url);
        return httpRequest.header("Cookie",header)
                .send(js.toString()).body();
    }
    private String aiChat(String content, String userId) {
        final String apiUrl = "http://www.tuling123.com/openapi/api"; // ????????api????
        final String apiKey = "49d5dd04005a4d82b7d5bc30dae96821"; // ????????????apikey
        final String secretKey = "02a06d8364d4ef9a"; // ????????key
        final String timeStamp = System.currentTimeMillis() + "";

        // ???????
        String key = Md5.MD5(secretKey + timeStamp + apiKey);

        // ????????????м???
        JSONObject chatData = new JSONObject();
        chatData.put("key", apiKey);
        chatData.put("info", content);
        chatData.put("userid", userId);
        String encryptChatData = new Aes(key).encrypt(chatData.toString());

        // ????????
        OutputStreamWriter outWriter = null;
        BufferedReader inReader = null;
        StringBuilder response = new StringBuilder("");
        final int timeOut = 50 * 1000; // ?????????????50s
        try {

            // ??????????Щ???????????????
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
                    response.append("API密钥有错误");
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.append("出错，请稍后再试");
        }

        return response.toString();
    }
    //读取文件
    private void readFiles()throws Exception{
        String  s[]=null;
        File public_file = new File("公开.txt");
        File private_file = new File("私密.txt");
        File sense_file = new File("敏感词.txt");
        if(!public_file.exists()){
            public_file.createNewFile();
        }
        if(!private_file.exists()){
            private_file.createNewFile();
        }
        if(!sense_file.exists()){
            sense_file.createNewFile();
        }
        Scanner sc = new Scanner(public_file);
        while(sc.hasNextLine()){
            s=sc.nextLine().split("--");
            if(s.length==2)
                publicReply.put(s[0],s[1]);
        }
        sc= new Scanner(private_file);
        while(sc.hasNextLine()){
            s = sc.nextLine().split("--");
            if(s.length==2)
                privateReply.put(s[0],s[1]);
        }
        sc = new Scanner(sense_file);
        while(sc.hasNextLine()){
            senseReply.add(sc.nextLine());
        }
    }

    //生成DiviceID
    private String produceDevID(){
        Random rm = new Random();

        double pross = (1 + rm.nextDouble()) * Math.pow(10, 16);

        String fixLenthString = String.valueOf(pross);

        return fixLenthString.substring(1, 16 + 1);
    }


    //生成二维码url
    private String produceErWei(String s){
        s = "http://login.weixin.qq.com/qrcode/"+s;
        return s;
    }

    //SyncKey转换
    private void syncKeyTransfer(String s){
        JSONObject fr = JSONObject.fromObject(s);
        syncKey = fr.getJSONObject("SyncKey");
        JSONArray ja = syncKey.getJSONArray("List");
        for(int i=0;i<ja.size();i++){
            JSONObject json =ja.getJSONObject(i);
            if(i!=ja.size()-1) {
                syncKeyList.append(json.get("Key") + "_" + json.get("Val") + "%7C");
            }
            else {
                syncKeyList.append(json.get("Key") + "_" + json.get("Val"));
            }
        }
    }
    //自动添加好友
    private void addFriend() throws Exception{
        url = "https://"+host+"/cgi-bin/mmwebwx-bin/webwxverifyuser?r="+System.currentTimeMillis()+"&lang=zh_CN&pass_ticket="+pass_ticket;
        js.clear();
        js.put("BaseRequest",baseRequest);
        js.put("Opcode",3);
        js.put("VerifyUserListSize",1);
        verifyUserList.put("Value",friendId);
        verifyUserList.put("VerifyUserTicket",v_ticket);
        js.put("VerifyUserList",verifyUserList);
        js.put("VerifyContent","");
        js.put("SceneListCount",1);
        js.put("SceneList",33);
        js.put("skey",skey);
        this.sendPostRequest(url,js,header);
        v_ticket="";

    }
    //检查消息更新
    private void checkMsg() throws Exception{
        Long r = System.currentTimeMillis();
        url = "https://webpush." + host + "/cgi-bin/mmwebwx-bin/synccheck?r=" + r + "&skey=" + skey + "&sid=" + wxsid + "&uin=" + wxuin + "&deviceid=" + DeviceID + "&synckey=" + syncKeyList.toString() + "&_=" + timeStamp;
        httpRequest = HttpRequest.get(url);
        String s = httpRequest.header("Cookie", header).body();
        if (s.contains("2")) {
            //检查最新消息
            url = "https://" + host + "/cgi-bin/mmwebwx-bin/webwxsync?sid=" + wxsid + "&skey=" + skey + "&lang=zh_CN&pass_ticket=" + pass_ticket;
            js.clear();
            js.put("BaseRequest", baseRequest);
            js.put("SyncKey", syncKey);
            js.put("rr", ~(System.currentTimeMillis()));
            s = this.sendPostRequest(url, js, header);
            syncKeyList = new StringBuffer();
            this.syncKeyTransfer(s);
            JSONObject jsonObject = JSONObject.fromObject(s);
            int count = jsonObject.getInt("AddMsgCount");
            if (count != 0) {
                JSONArray jsonArray = jsonObject.getJSONArray("AddMsgList");
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    from = jsonObject1.getString("FromUserName");
                    to = jsonObject1.getString("ToUserName");
                    content = jsonObject1.getString("Content");
                    msgType = String.valueOf(jsonObject1.getInt("MsgType"));
                    msgID = jsonObject1.getString("NewMsgId");
                    JSONObject jsonObject2 = jsonObject1.getJSONObject("RecommendInfo");
                    v_ticket = jsonObject2.getString("Ticket");
                    friendId = jsonObject2.getString("UserName");
                    //statusNotify
                    if(from.equals(to)&&from.equals(userID)){
                        String groupid = jsonObject1.getString("StatusNotifyUserName");
                        String id[] = groupid.split(",");
                        for(int j = 0;j<id.length;j++){
                            if(id[j].contains("@@")&&!unactiveGroupId.contains(id[j])&&!activeGroupId.contains(id[j]))
                                unactiveGroupId.add(id[j]);
                        }
                        statusNotifyFlag = true;
                        continue;
                    }
                    if (!from.equals(to)&&from.contains("@@")&&v_ticket.equals("")) {
                        int j = 0;
                        for (; j < groupInfoList.size(); j++) {
                            if(from.equals(groupInfoList.get(j).getGroupID())&&groupInfoList.get(j).getFlag()){
                                groupID = from;
                                from = groupInfoList.get(j).getGroupName();
                                String temp[] = content.split(":");
                                to = temp[0];
                                if(content.contains(":"))
                                    content = temp[1].replace("<br/>","");
                                if(content.startsWith("&lt")&&msgType.equals("1"))
                                    content = "[会话]";
                                for(int k = 0;k<groupInfoList.get(j).getGroup().size();k++){
                                    if(to.equals(groupInfoList.get(j).getGroup().get(k).getUserId())){
                                        memberID = to;
                                        to = groupInfoList.get(j).getGroup().get(k).getNickName();
                                    }
                                }
                                doc.insertString(doc.getLength(),df.format(new Date()) + "\n" + from + " 群中 "+ to +" 说:" + content + "\n",windowUI.getAttributeSet());
                                windowUI.getjTextPane().paintImmediately(windowUI.getjTextPane().getBounds());
                                recordList.add(df.format(new Date()) + "," + from + ","+ to +"," + content);
                                break;
                            }
                        }
                        //图片消息
                        if(msgType.equals("3")){
                            url = "https://"+host+"/cgi-bin/mmwebwx-bin/webwxgetmsgimg?&MsgID="+msgID+"&skey="+skey+"&type=slave";
                            ImageIcon image = new ImageIcon(httpRequest.get(url).bytes());
                            windowUI.getjTextPane().setCaretPosition(doc.getLength());
                            windowUI.getjTextPane().insertIcon(image);
                            doc.insertString(doc.getLength(),"\n",windowUI.getAttributeSet());
                            windowUI.getjTextPane().paintImmediately(windowUI.getjTextPane().getBounds());
                        }
                        //图灵机器人智能聊天
                        if(!groupID.equals("")&&!content.equals("[会话]")&&flag5){
                            this.replayMsg(this.aiChat(content,userID),groupID);
                        }
                        //关键词回复
                        if(!groupID.equals("")&&!content.equals("[会话]")&&flag2)
                        {
                            Iterator iterator = publicReply.entrySet().iterator();
                            while(iterator.hasNext()){
                                Map.Entry entry = (Map.Entry)iterator.next();
                                String key = entry.getKey().toString();
                                String value = entry.getValue().toString();
                                if(content.contains(key)) {
                                    this.replayMsg(value, groupID);
                                    doc.insertString(doc.getLength(),df.format(new Date()) + "\n我对" + from + "说:" + value + "\n",windowUI.getAttributeSet());
                                    windowUI.getjTextPane().paintImmediately(windowUI.getjTextPane().getBounds());
                                    recordList.add(df.format(new Date()) +from+",我,"+ value);
                                    break;
                                }
                            }
                            iterator = privateReply.entrySet().iterator();
                            while(iterator.hasNext()){
                                Map.Entry entry = (Map.Entry)iterator.next();
                                String key = entry.getKey().toString();
                                String value = entry.getValue().toString();
                                if(content.contains(key)) {
                                    this.replayMsg(value, memberID);
                                    doc.insertString(doc.getLength(),df.format(new Date()) + "\n我对" + from + "说:" + value + "\n",windowUI.getAttributeSet());
                                    windowUI.getjTextPane().paintImmediately(windowUI.getjTextPane().getBounds());
                                    recordList.add(df.format(new Date()) +",我,"+from+","+ value);
                                    break;
                                }
                            }
                        }
                        //敏感词警告
                        if(!groupID.equals("")&&!content.equals("[会话]")&&flag3){
                            for(int index = 0;index<senseReply.size();index++){
                                if(content.contains(senseReply.get(index))){
                                    this.replayMsg("您言语有不当之处，警告一次",memberID);
                                    break;
                                }
                            }
                        }

                        if(windowUI.getjScrollPane().getHeight()<=400) {
                            windowUI.getChatJPanel().validate();
                            windowUI.getChatJPanel().repaint();
                        }
                    }
                    else if(!from.equals(to)&&from.contains("@")&&v_ticket.equals("")) {
                        if (from.equals(userID))
                            from = "我";
                        else for (int j = 0; j < userInfoList.size(); j++)
                            if (from.equals(userInfoList.get(j).getUserId())) {
                                memberID = from;
                                from = userInfoList.get(j).getNickName();
                                break;
                            }

                        if (to.equals(userID))
                            to = "我";
                        else if(to.contains("@@"))
                            for(int j = 0;j<groupInfoList.size();j++) {
                                if (to.equals(groupInfoList.get(j).getGroupID())&&groupInfoList.get(j).getFlag()) {
                                    to = groupInfoList.get(j).getGroupName()+" 群";
                                    if(content.startsWith("&lt"))
                                        content = "[会话]";
                                    doc.insertString(doc.getLength(),df.format(new Date())+"\n"+from+"对"+to+"说:"+content+"\n",windowUI.getAttributeSet());
                                    recordList.add(df.format(new Date()) + "," + from + ","+ to +"," + content);
                                    break;
                                }
                            }
                        else for (int j = 0; j < userInfoList.size(); j++) {
                                if (to.equals(userInfoList.get(j).getUserId())) {
                                    to = userInfoList.get(j).getNickName();
                                    if(content.startsWith("&lt"))
                                        content = "[会话]";
                                    doc.insertString(doc.getLength(),df.format(new Date())+"\n"+from+"对"+to+"说:"+content+"\n",windowUI.getAttributeSet());
                                    recordList.add(df.format(new Date()) + "," + from + ","+ to +"," + content);
                                    break;
                                }
                            }
                        if(windowUI.getjScrollPane().getHeight()<=400) {
                            windowUI.getChatJPanel().validate();
                            windowUI.getChatJPanel().repaint();
                        }
                    }
                }
            }
        }
    }
    //消息回复
    private void replayMsg(String s,String id) throws Exception{
        url = "https://"+host+"/cgi-bin/mmwebwx-bin/webwxsendmsg?lang=zh_CN&pass_ticket="+pass_ticket;
        js.clear();
        js.put("BaseRequest",baseRequest);
        JSONObject msg = new JSONObject();
        //生成ClientMsgId
        int r=(int)(Math.random()*9000)+1000;
        Long ts = System.currentTimeMillis()<<4;
        String cmId = ts.toString()+String.valueOf(r);

        //生成Msg
        msg.put("ClientMsgId",cmId);
        msg.put("Content",s);//??????
        msg.put("FromUserName",userID);
        msg.put("LocalID",cmId);
        msg.put("ToUserName",id);
        msg.put("Type",1);

        js.put("Msg",msg);
        js.put("Scene",0);
        this.sendPostRequest(url,js);
    }

    //邀请用户进群
    private void addMember(String addUserID,String addGroupID){
        url = "https://"+host+"/cgi-bin/mmwebwx-bin/webwxupdatechatroom";
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("AddMemberList",addUserID);
        jsonObject.put("BaseRequest",baseRequest);
        jsonObject.put("ChatRoomName",addGroupID);
        httpRequest = HttpRequest.post(url,true,"fun","addmember","pass_ticket",pass_ticket)
                .header("Content-Type","application/json;charset=UTF-8").header("Cookie",header).send(jsonObject.toString());
        httpRequest.body();
        httpRequest.disconnect();
    }
    //将用户踢出群
    private void deleteMember(String UserID,String GroupID){
        url = "https://"+host+"/cgi-bin/mmwebwx-bin/webwxupdatechatroom";
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("DelMemberList",UserID);
        jsonObject.put("BaseRequest",baseRequest);
        jsonObject.put("ChatRoomName",GroupID);
        httpRequest = HttpRequest.post(url,true,"fun","delmember","pass_ticket",pass_ticket)
                .header("Content-Type","application/json;charset=UTF-8").header("Cookie",header).send(jsonObject.toString());
        httpRequest.body();
        httpRequest.disconnect();
    }

    //获取最近联系人列表
    private void getList(){
//        jTextArea.setText("");
        url = "https://"+host+"/cgi-bin/mmwebwx-bin/webwxgetcontact?lang=zh_CN&pass_ticket="+pass_ticket+"&r="+System.currentTimeMillis()+"&seq=0&skey="+skey;
        httpRequest = HttpRequest.post(url);
        String temp = httpRequest.header("Cookie",header).body();
        JSONObject jsonObject = JSONObject.fromObject(temp);
        JSONArray jsonArray = jsonObject.getJSONArray("MemberList");
        for(int i =0;i<jsonArray.size();i++){
            jsonObject = jsonArray.getJSONObject(i);
            if(!jsonObject.getString("UserName").contains("@@")&&jsonObject.getInt("Sex")!=0) {
                UserInfo userInfo = new UserInfo();
                userInfo.setNickName(jsonObject.getString("NickName"));
                userInfo.setUserId(jsonObject.getString("UserName"));
                userInfo.setSignature(jsonObject.getString("Signature"));
                userInfoList.add(userInfo);
            }
            else if(jsonObject.getString("UserName").contains("@@")&&!activeGroupId.contains(jsonObject.getString("UserName"))&&!unactiveGroupId.contains(jsonObject.getString("UserName")))
                unactiveGroupId.add(jsonObject.getString("UserName"));

        }

    }

    //生成key
    private void produceKey()throws Exception{
        String s = this.sendGetRequest(url);
        List<String> l = httpRequest.getConnection().getHeaderFields().get("Set-Cookie");
        for(String s1:l){
            s1 = s1.substring(0, s1.indexOf(";"));
            if(!s1.equals(null)&&s1!=null) {
                header = header + s1 + ";";
            }
        }
        header = header.substring(4);//去掉null
        if(s.contains("<message>OK</message>")){
            skey = s.substring(s.indexOf("<skey>"),s.indexOf("</skey>"));
            skey = skey.replace("<skey>","").trim();
//            skey = URLEncoder.encode(skey,"gbk");
            wxsid = s.substring(s.indexOf("<wxsid>"),s.indexOf("</wxsid>"));
            wxsid = wxsid.replace("<wxsid>","").trim();
            wxuin = s.substring(s.indexOf("<wxuin>"),s.indexOf("</wxuin>"));
            wxuin = wxuin.replace("<wxuin>","").trim();
            pass_ticket = s.substring(s.indexOf("<pass_ticket>"),s.indexOf("</pass_ticket>"));
            pass_ticket = pass_ticket.replace("<pass_ticket>","").trim();
//            pass_ticket = URLEncoder.encode(pass_ticket,"gbk");
        }
    }

    //初始化微信
    private void initWeChat()throws Exception{
        timeStamp  = String.valueOf(System.currentTimeMillis());
        url = "https://"+host+"/cgi-bin/mmwebwx-bin/webwxinit?pass_ticket="+pass_ticket;
        DeviceID = "e"+this.produceDevID().substring(1);
        js = new JSONObject();
        baseRequest.put("Uin",wxuin);
        baseRequest.put("Sid",wxsid);
        baseRequest.put("Skey",skey);
        baseRequest.put("DeviceID",DeviceID.toString());
        js.put("BaseRequest",baseRequest);
        String s = this.sendPostRequest(url,js,header);
        this.syncKeyTransfer(s);

        String s3[] = s.split(",");
        List<String> usrName = new ArrayList<>();
        for(String usrname:s3){
            if(usrname.contains("UserName"))
                usrName.add(usrname);
        }
        //获取一部分群组ID
        for(String id:usrName){
            if(id.contains("@@")) {
                String sinId = (id.split(":"))[1].replace("\"","").trim();
                activeGroupId.add(sinId);
            }
        }
        //获取userID
        JSONObject sc = JSONObject.fromObject(s);
        JSONObject user = sc.getJSONObject("User");
        userID = user.get("UserName").toString();
        userName = user.get("NickName").toString();
        //开启微信状态通知
        url = "https://"+host+"/cgi-bin/mmwebwx-bin/webwxstatusnotify?lang=zh_CN&pass_ticket="+pass_ticket;
        js.put("Code",3);
        js.put("FromUserName",userID);
        js.put("ToUserName",userID);
        js.put("ClientMsgId", Long.valueOf(timeStamp));
        this.sendPostRequest(url,js,header);
        this.getHeaderImg();
        while(!statusNotifyFlag)
            this.checkMsg();
    }

    //获取群列表(ChatRoomID)
    private void getRecentList()throws Exception{
//        jTextArea.setText("");
        url="https://"+host+"/cgi-bin/mmwebwx-bin/webwxbatchgetcontact?type=ex&r="+timeStamp+"&lang=zh_CN&pass_ticket="+pass_ticket;
        js.clear();
        js.put("BaseRequest",baseRequest);
        js.put("Count",activeGroupId.size()+unactiveGroupId.size());
        JSONArray groupJs = new JSONArray();
        for(int i = 0;i<activeGroupId.size();i++){
            JSONObject single = new JSONObject();
            single.put("UserName",activeGroupId.get(i));
            single.put("ChatRoomId","");
            groupJs.add(single);
        }
        for(int i = 0;i<unactiveGroupId.size();i++){
            JSONObject single=new JSONObject();
            single.put("UserName",unactiveGroupId.get(i));
            single.put("EncryChatRoomId","");
            groupJs.add(single);
        }
        js.put("List",groupJs);
        String s = this.sendPostRequest(url,js);
        JSONObject jsonObject = JSONObject.fromObject(s);
        JSONArray contactList = jsonObject.getJSONArray("ContactList");
        for(int i = 0;i<contactList.size();i++){
            GroupInfo groupInfo = new GroupInfo();
            groupInfo.setGroupID(contactList.getJSONObject(i).getString("UserName"));
            groupInfo.setMemberCount(contactList.getJSONObject(i).getInt("MemberCount"));
            groupInfo.setGroupName(contactList.getJSONObject(i).getString("NickName"));
            JSONArray jsonArray = contactList.getJSONObject(i).getJSONArray("MemberList");
            for(int j = 0;j<jsonArray.size();j++){
                UserInfo userInfo = new UserInfo();
                userInfo.setNickName(jsonArray.getJSONObject(j).getString("NickName"));
                userInfo.setUserId(jsonArray.getJSONObject(j).getString("UserName"));
                groupInfo.getGroup().add(userInfo);
            }
            groupInfoList.add(groupInfo);
        }
    }
    //显示二维码
    private void showPic(String url)throws Exception{
        ImageIcon image = new ImageIcon(httpRequest.get(url).bytes());
        if(updatePic){
            windowUI.getjPanel().remove(windowUI.getjLabel_0());
            updatePic=false;
        }
        windowUI.setjLabel_0(new JLabel(image));
        windowUI.getjPanel().add(windowUI.getjLabel_0());
        windowUI.getF().setVisible(true);
        windowUI.getjPanel().validate();
        windowUI.getjPanel().repaint();
        windowUI.getF().setSize(480,480);
    }
    //获取头像
    private void getHeaderImg()throws Exception{
        url = "https://"+host+"/cgi-bin/mmwebwx-bin/webwxgeticon?username="+userID+"&skey="+skey;
        ImageIcon image = new ImageIcon(httpRequest.get(url).header("Cookie",header).bytes());
        windowUI.setUserHeaderImg(new JLabel(image));
        windowUI.getUserHeaderImg().setBounds(0,0,image.getIconWidth(),image.getIconHeight());
        windowUI.getUserInfoJPanel().add(windowUI.getUserHeaderImg());
        windowUI.setUserNameLabel(new JLabel("用户名"+userName));
        windowUI.getUserNameLabel().setFont(new Font("黑体",1,16));
        windowUI.getUserInfoJPanel().add(windowUI.getUserNameLabel());
    }
    //获取uuid
    private String produceUuid()throws Exception{
        timeStamp = String.valueOf(System.currentTimeMillis());
        url = "https://login.wx.qq.com/jslogin?appid=wx782c26e4c19acffb&redirect_uri=https%3A%2F%2Fwx.qq.com%2Fcgi-bin%2Fmmwebwx-bin%2Fwebwxnewloginpage&fun=new&lang=zh_CN&_=" + timeStamp;//request??????
        String s = this.sendPostRequest(url, null);
        uuid = s.substring(s.length() - 14, s.length() - 2);
        s = this.produceErWei(uuid);
        return s;
    }

    //退出微信
    private void exitWeChat()throws Exception{
        url = "https://"+host+"/cgi-bin/mmwebwx-bin/webwxlogout?redirect=1&type=0&skey="+skey;
        httpRequest.post(url).header("Cookie",header).send("sid="+wxsid+"&uin="+wxuin).body();
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        System.setProperty ("jsse.enableSNIExtension", "false");
        final Main htmlUnit = new Main();
        mainThread =new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Long oldTime;
                    Long newTime;
                    String s;
                    out:
                    while (true) {
                        s = htmlUnit.produceUuid();
                        htmlUnit.showPic(s);
                        oldTime = System.currentTimeMillis();
                        int tip = 1;
                        url = "https://login.weixin.qq.com/cgi-bin/mmwebwx-bin/login?loginicon=true&uuid=" + uuid + "&tip=" + tip + "&_=" + timeStamp;
                        in:
                        while (true) {
                            s = htmlUnit.sendPostRequest(url, null);
                            newTime = System.currentTimeMillis();
                            if (newTime - oldTime >= 30000) {
                                updatePic = true;
                                continue out;
                            }
                            if (s.contains("201") && s.contains("userAvatar")) {
//                String s2[]=s.split("'");
//                userAvatar  =s2[1];
                                tip = 0;
//                continue;
                            } else if (s.contains("200")) {
                                String s1[] = s.split("\"");
                                if (s1.length == 3) {
                                    url = s1[1] + "&fun=new&version=v2&lang=zh_CN";
                                    URL u = new URL(url);
                                    host = u.getHost();
                                }
                                htmlUnit.produceKey();
                                break out;
                            }
                        }
                    }
                    htmlUnit.initWeChat();
                    htmlUnit.getList();
                    htmlUnit.getRecentList();
                    htmlUnit.readFiles();
                    windowUI.getjPanel().remove(windowUI.getjLabel_0());
                    windowUI.getF().remove(windowUI.getjPanel());
                    windowUI.getF().setSize(480,600);
                    windowUI.getGb().gridx=0;
                    windowUI.getGb().gridy=0;
                    windowUI.getGb().gridwidth=0;
                    windowUI.getGb().ipady=200;
                    windowUI.getGb().ipadx=480;
                    windowUI.getF().add(windowUI.getUserInfoJPanel(),windowUI.getGb());
                    windowUI.getjPanel().setLayout(new FlowLayout(FlowLayout.CENTER,30,20));
                    windowUI.getjPanel().add(windowUI.getjPanel_1());
                    windowUI.getjPanel().add(windowUI.getjPanel_2());
                    windowUI.getjPanel().add(windowUI.getjPanel_9());
                    windowUI.getjPanel().add(windowUI.getjPanel_3());
                    windowUI.getjPanel().add(windowUI.getjPanel_4());
                    windowUI.getjPanel().add(windowUI.getjPanel_5());
                    windowUI.getjPanel().add(windowUI.getjPanel_6());
                    windowUI.getjPanel().add(windowUI.getjPanel_7());
                    windowUI.getjPanel().add(windowUI.getjPanel_10());
                    windowUI.getjPanel().add(windowUI.getjPanel_11());
                    windowUI.getjPanel().add(windowUI.getjPanel_12());
                    windowUI.getjPanel().add(windowUI.getjPanel_8());
                    windowUI.getGb().gridx=0;
                    windowUI.getGb().gridy=1;
                    windowUI.getGb().weightx=4;
                    windowUI.getGb().ipady=400;
                    windowUI.getF().add(windowUI.getjPanel(),windowUI.getGb());
                    windowUI.getF().setVisible(true);
                    windowUI.getInviteIntoGroup().addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            htmlUnit.getList();
                            Vector<String> group = new Vector<>();
                            Vector<String> friend = new Vector<>();
                            for(int i = 0;i<groupInfoList.size();i++){
                                group.add(groupInfoList.get(i).getGroupName());
                            }
                            windowUI.setjList2(new JList(group));
                            windowUI.getjList2().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                            for(int i = 0;i<userInfoList.size();i++){
                                friend.add(userInfoList.get(i).getNickName());
                            }
                            windowUI.setjList1(new JList(friend));
                            windowUI.setInviteScrollPane1(new JScrollPane(windowUI.getjList1()));
                            windowUI.setInviteScrollPane2(new JScrollPane(windowUI.getjList2()));
                            windowUI.getjList1().setSize(400,400);
                            windowUI.getjList2().setSize(400,400);
                            windowUI.setInviteScrollPane1(new JScrollPane(windowUI.getjList1()));
                            windowUI.setInviteScrollPane2(new JScrollPane(windowUI.getjList2()));
                            windowUI.getInviteScrollPane1().setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                            windowUI.getInviteScrollPane1().setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                            windowUI.getInviteScrollPane2().setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                            windowUI.getInviteScrollPane2().setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                            windowUI.getInvitePanel().add(windowUI.getInviteScrollPane1());
                            windowUI.getInvitePanel().add(windowUI.getInviteScrollPane2());
                            windowUI.getInviteScrollPane1().setBorder(BorderFactory.createTitledBorder("好友昵称"));
                            windowUI.getInviteScrollPane2().setBorder(BorderFactory.createTitledBorder("群名称"));
                            windowUI.getGroupInvite().add(BorderLayout.SOUTH,windowUI.getInvite());
                            windowUI.getGroupInvite().setVisible(true);
                            windowUI.getjList1().addListSelectionListener(new ListSelectionListener() {
                                @Override
                                public void valueChanged(ListSelectionEvent e) {
                                    int index= windowUI.getjList1().getSelectedIndex();
                                    if(invitedUserID.equals(""))
                                        invitedUserID = userInfoList.get(index).getUserId();
                                    else
                                        invitedUserID = invitedUserID+","+userInfoList.get(index).getUserId();
                                }
                            });
                            windowUI.getjList2().addListSelectionListener(new ListSelectionListener() {
                                @Override
                                public void valueChanged(ListSelectionEvent e) {
                                    int index= windowUI.getjList2().getSelectedIndex();
                                    invitedGroupID = groupInfoList.get(index).getGroupID();
                                }
                            });
                        }
                    });

                    windowUI.getInvite().addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            if(!invitedGroupID.equals("")&&!invitedUserID.equals("")) {
                                htmlUnit.addMember(invitedUserID, invitedGroupID);
                            }
                        }
                    });

                    windowUI.getRemoveFromGroup().addActionListener(new ActionListener(){
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            try{
                                htmlUnit.getRecentList();
                            }catch(Exception e1){
                                System.out.println(e1);
                            }
                            Vector<String> group = new Vector<>();
                            for(int i = 0;i<groupInfoList.size();i++){
                                group.add(groupInfoList.get(i).getGroupName());
                            }
                            windowUI.setjList3(new JList(group));
                            windowUI.setRemoveScrollPane1(new JScrollPane(windowUI.getjList3()));
                            windowUI.setRemoveScrollPane2(new JScrollPane(windowUI.getjList4()));
                            windowUI.getjList4().setSize(400,400);
                            windowUI.getjList3().setSize(400,400);
                            windowUI.getjList3().setBorder(BorderFactory.createTitledBorder("选择群"));
                            windowUI.getjList3().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                            windowUI.getRemoveScrollPane2().setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                            windowUI.getRemoveScrollPane2().setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                            windowUI.getRemoveScrollPane2().setBorder(BorderFactory.createTitledBorder("选择群成员"));
                            windowUI.getRemoveScrollPane1().setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                            windowUI.getRemoveScrollPane1().setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                            windowUI.getRemovePanel().add(windowUI.getRemoveScrollPane1());
                            windowUI.getRemovePanel().add(windowUI.getRemoveScrollPane2());
                            windowUI.getGroupRemove().setVisible(true);
                            windowUI.getjList3().addListSelectionListener(new ListSelectionListener() {
                                @Override
                                public void valueChanged(ListSelectionEvent e) {
                                    windowUI.getFriend().clear();
                                    windowUI.getFriendID().clear();
                                    int index = windowUI.getjList3().getSelectedIndex();
                                    removeGroupID = groupInfoList.get(index).getGroupID();
                                    for(int i =0;i<groupInfoList.get(index).getGroup().size();i++) {
                                        windowUI.getFriend().add(groupInfoList.get(index).getGroup().get(i).getNickName());
                                        windowUI.getFriendID().add(groupInfoList.get(index).getGroup().get(i).getUserId());
                                    }
                                    windowUI.getjList4().setListData(windowUI.getFriend());
                                    windowUI.getRemoveScrollPane2().repaint();
                                }
                            });
                            windowUI.getjList4().addListSelectionListener(new ListSelectionListener() {
                                @Override
                                public void valueChanged(ListSelectionEvent e) {
                                    int index = windowUI.getjList4().getSelectedIndex();
                                    removeUserID = windowUI.getFriendID().get(index);
                                }
                            });
                        }
                    });
                    windowUI.getRemove().addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            if(!removeUserID.equals("")&&!removeGroupID.equals(""))
                                htmlUnit.deleteMember(removeUserID,removeGroupID);
                        }
                    });

                    windowUI.getChatJButton().addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            try {
                                flag=true;
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
                                if (flag1) {
                                    flag1 = false;
                                    windowUI.getAddFriend().setIcon(new ImageIcon(WindowUI.class.getResource("resource/add_friend.png")));
                                    windowUI.getAddFriend().repaint();
                                }
                                else {
                                    flag1 = true;
                                    windowUI.getAddFriend().setIcon(windowUI.getAddFriend().getDisabledIcon());
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
                            if (flag2){
                                flag2 = false;
                                windowUI.getReply().setIcon(new ImageIcon(WindowUI.class.getResource("resource/reply.png")));
                                windowUI.getReply().repaint();
                            }
                            else {
                                flag2 = true;
                                windowUI.getReply().setIcon(windowUI.getReply().getDisabledIcon());
                                windowUI.getReply().repaint();
                            }
                        }
                    });

                    windowUI.getWarn().addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            if (flag3) {
                                flag3 = false;
                                windowUI.getWarn().setIcon(new ImageIcon(WindowUI.class.getResource("resource/warn.png")));
                                windowUI.getWarn().repaint();
                            }
                            else {
                                flag3 = true;
                                windowUI.getWarn().setIcon(windowUI.getWarn().getDisabledIcon());
                                windowUI.getWarn().repaint();
                            }
                        }
                    });

                    windowUI.getAutoChat().addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            if(flag5) {
                                flag5 = false;
                                windowUI.getAutoChat().setIcon(new ImageIcon(WindowUI.class.getResource("resource/auto_chat.png")));
                                windowUI.getAutoChat().repaint();
                            }
                            else {
                                flag5 = true;
                                windowUI.getAutoChat().setIcon(windowUI.getAutoChat().getDisabledIcon());
                                windowUI.getAutoChat().repaint();
                            }
                        }
                    });

                    windowUI.getSendByTime().addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            flag4=true;
                            windowUI.getSendByTime().setEnabled(false);
                            windowUI.getDailyTip().setVisible(true);
                            for(int i = 0;i<groupInfoList.size();i++)
                                windowUI.getGroupNameArea().addItem(groupInfoList.get(i).getGroupName());
                        }
                    });

                    windowUI.getLocalWord().addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            windowUI.getWordModify().setVisible(true);
                            windowUI.getLocalWord().setEnabled(false);
                        }
                    });

                    windowUI.getAddTipButton().addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            TipRecord tipRecord = new TipRecord();
                            windowUI.addTip(tipRecord);
                            try {
                                tipRecord.setTime(windowUI.getDf().format(windowUI.getDf().parse(windowUI.getTimeArea().getText())).toString());
                                tipRecord.setProperty(windowUI.getPropertyArea().getText());
                                tipRecord.setGroupName(windowUI.getGroupNameArea().getSelectedItem().toString());
                                tipRecordList.add(tipRecord);
                            }catch (Exception e1){
                                e1.printStackTrace();
                            }
                        }
                    });
                    windowUI.getShowGroupList().addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            if(windowUI.getShowGroupPanel().getComponentCount()==0) {
                                for (final GroupInfo groupInfo: groupInfoList) {
                                    final JCheckBox jc = new JCheckBox(groupInfo.getGroupName());
                                    jc.setSelected(true);
                                    windowUI.getShowGroupPanel().add(jc);
                                    jc.addChangeListener(new ChangeListener() {
                                        @Override
                                        public void stateChanged(ChangeEvent e) {
                                            if(!jc.isSelected())
                                                groupInfo.setFlag(false);
                                            else{
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
                                Runtime.getRuntime().exec("cmd /c start "+userName+"_群聊天记录.csv");
                            }catch(Exception e1){
                                e1.printStackTrace();
                            }

                        }
                    });

                    windowUI.getSaveRecord().addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            try{
                                File record = new File(userName+"_群聊天记录.csv");
                                if(!record.exists()){
                                    record.createNewFile();
                                    String title = "时间,群名/发起人,群成员/接收人,内容\r\n";
                                    BufferedWriter bw = new BufferedWriter(new FileWriter(record,true));
                                    bw.write(title);
                                    bw.flush();
                                    bw.close();
                                }
                                BufferedWriter bw = new BufferedWriter(new FileWriter(new File(userName+"_群聊天记录.csv"),true));
                                for(String r:recordList){
                                    bw.write(r+"\r\n");
                                }
                                bw.flush();
                                bw.close();
                            }catch (IOException e1){
                                e1.printStackTrace();
                            }
                        }
                    });
                    windowUI.getPublicWord().addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            try {
                                Runtime.getRuntime().exec("notepad.exe 公开.txt");
                            }catch (Exception e1){
                                e1.printStackTrace();
                            }
                        }
                    });

                    windowUI.getPrivateWord().addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            try {
                                Runtime.getRuntime().exec("notepad.exe 私密.txt");
                            }catch (Exception e1){
                                e1.printStackTrace();
                            }
                        }
                    });

                    WindowUI.getSenseWord().addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            try {
                                Runtime.getRuntime().exec("notepad.exe 敏感词.txt");
                            }catch (Exception e1){
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
                            flag=false;
                            windowUI.getChatIn().setVisible(false);
                            windowUI.getChatJButton().setEnabled(true);
                        }

                        @Override
                        public void windowClosed(WindowEvent e) {
                            flag = false;
                            windowUI.getChatIn().setVisible(false);
                            windowUI.getChatJButton().setEnabled(true);
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
                    windowUI.getF().addWindowListener(new WindowListener() {
                        @Override
                        public void windowOpened(WindowEvent e) {

                        }

                        @Override
                        public void windowClosing(WindowEvent e) {
                            try {
                                htmlUnit.exitWeChat();
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                        }

                        @Override
                        public void windowClosed(WindowEvent e) {
                            try {
                                htmlUnit.exitWeChat();
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
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
                            flag4 = false;
                            windowUI.getDailyTip().setVisible(false);
                            windowUI.getSendByTime().setEnabled(true);
                            windowUI.getGroupNameArea().removeAllItems();
                        }

                        @Override
                        public void windowClosed(WindowEvent e) {
                            flag4 = false;
                            windowUI.getDailyTip().setVisible(false);
                            windowUI.getSendByTime().setEnabled(true);
                            windowUI.getGroupNameArea().removeAllItems();
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
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        mainThread.setPriority(10);
        mainThread.start();
        while (true) {
            if(flag) {
                htmlUnit.checkMsg();
            }
            if (flag1 && !v_ticket.equals("")) {
                htmlUnit.addFriend();
            }
            if(flag4){
                if(tipRecordList.size()==0)
                    continue;
                for(int i=0;i<tipRecordList.size();i++){
                    if (!tipRecordList.get(i).getFlag()&&windowUI.getDf().format(new Date()).toString().equals(tipRecordList.get(i).getTime())) {
                        for(int j = 0;j<groupInfoList.size();j++){
                            if(groupInfoList.get(j).getGroupName().equals(tipRecordList.get(i).getGroupName())){
                                tipGroupID = groupInfoList.get(j).getGroupID();
                                break;
                            }
                        }
                        htmlUnit.replayMsg(tipRecordList.get(i).getProperty(), tipGroupID);
                        tipRecordList.get(i).setFlag(true);
                    }
                }
            }
        }

    }
}
