package weChat;

/**
 * Created by huzhejie on 2016/7/7.
 */

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import blade.kit.http.HttpRequest;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import javax.swing.*;


public class Main extends JFrame {
    private static HttpRequest httpRequest;
    private static Boolean flag = false;
    private static String url;
    private static String timeStamp;
    private static String uuid;
    private static String skey;
    private static String wxsid;
    private static String wxuin;
    private static String pass_ticket;
    private static String DeviceID;
    private static JSONObject js;
    private static String userID;
    private static List<String> activeGroupId = new ArrayList<>();//存放最近活跃的群id
    private static List<String> unactiveGroupId = new ArrayList<>();//存放最近不活跃的群id
    private static List<UserInfo> userInfoList = new ArrayList<>();//存放用户好友及订阅号，公众号信息
    private static List<GroupInfo> groupInfoList = new ArrayList<>();//存放群信息
    private static JSONObject syncKey;
    private static StringBuffer syncKeyList = new StringBuffer();
    private static String header;
    private static String host;
    private static JSONObject baseRequest = new JSONObject();
    private static String from;
    private static String to;
    private static String content;
    private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式;
//    private static String userAvatar;

    //按钮
    private static JButton jButton_0 = new JButton("查看好友列表");
    private static JButton jButton_1 = new JButton("确认发送");
    private static JButton jButton_2 = new JButton("点击后消息更新");
    private static JButton jButton_3 = new JButton("发送指定消息到某人或某个群");
    private static JPanel jPanel = new JPanel();
    private static JMenuBar jMenuBar = new JMenuBar();
    private static JScrollPane jScrollPane;
    private static JLabel jLabel_0;
    private static JTextArea jTextArea = new JTextArea();
    private static JTextArea jTextArea1 = new JTextArea();
    private static JTextArea jTextArea2 = new JTextArea();

    public Main() {
        setLocation(Toolkit.getDefaultToolkit().getScreenSize().width/2-350,Toolkit.getDefaultToolkit().getScreenSize().height/2-400);
        setVisible(false);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(700,800);
        add(jPanel);
        jPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        jPanel.setSize(700,800);
        jButton_0.setSize(100,100);
        jButton_1.setSize(100,100);
        jButton_2.setSize(100,100);
        jButton_3.setSize(100,100);
        jTextArea.setLineWrap(true);
        jTextArea1.setLineWrap(true);
        jTextArea2.setLineWrap(true);
        jTextArea.setEditable(false);
        jTextArea1.setEditable(true);
        jTextArea2.setEditable(true);
        jTextArea1.setSize(100,100);
        jTextArea2.setSize(300,100);
        setJMenuBar(jMenuBar);
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

    //生成DiviceID
    private String produceDevID(){
        Random rm = new Random();
        // 获得随机数
        double pross = (1 + rm.nextDouble()) * Math.pow(10, 16);
        // 将获得的获得随机数转化为字符串
        String fixLenthString = String.valueOf(pross);
        // 返回固定的长度的随机数
        return fixLenthString.substring(1, 16 + 1);
    }


    //生成二维码的url
    private String produceErWei(String s){
        s = "http://login.weixin.qq.com/qrcode/"+s;
        return s;
    }

    //SyncKey的转化
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
    //检查消息更新
    private void checkMsg() throws Exception{
        Long r = System.currentTimeMillis();
        url = "https://webpush." + host + "/cgi-bin/mmwebwx-bin/synccheck?r=" + r + "&skey=" + skey + "&sid=" + wxsid + "&uin=" + wxuin + "&deviceid=" + DeviceID + "&synckey=" + syncKeyList.toString() + "&_=" + timeStamp;
        httpRequest = HttpRequest.get(url);
        String id="";
        String s = httpRequest.header("Cookie", header).body();
        if (s.contains("2")) {
            //获取最新消息
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
                    //对消息进行分析并记录显示
                    if (!from.equals(to)&&from.contains("@@")) {
                        for (int j = 0; j < groupInfoList.size(); j++) {
                            if(from.equals(groupInfoList.get(j).getGroupID())){
                                from = groupInfoList.get(j).getGroupName();
                                String temp[] = content.split(":");
                                to = temp[0];
                                if(content.contains(":"))
                                    content = temp[1].replace("<br/>","");
                                for(int k = 0;k<groupInfoList.get(j).getGroup().size();k++){
                                    if(to.equals(groupInfoList.get(j).getGroup().get(k).getUserId())){
                                        to = groupInfoList.get(j).getGroup().get(k).getNickName();
                                    }
                                }
                                break;
                            }
                        }
                        if(content.startsWith("&lt"))
                            content = "[会话]";
                        jTextArea.append(df.format(new Date()) + "\n" + from + " 群中 "+ to +" 说:" + content + "\n");
                        jTextArea.paintImmediately(jTextArea.getBounds());
                        if(jScrollPane.getHeight()<=700) {
                            jPanel.validate();
                            jPanel.repaint();
                        }
                    }
                    else if(!from.equals(to)&&from.contains("@")) {
                        if (from.equals(userID))
                            from = "我";
                        else for (int j = 0; j < userInfoList.size(); j++)
                            if (from.equals(userInfoList.get(j).getUserId())) {
                                id = from;
                                from = userInfoList.get(j).getNickName();
                                break;
                            }

                        if (to.equals(userID))
                            to = "我";
                        else if(to.contains("@@"))
                            for(int j = 0;j<groupInfoList.size();j++) {
                                if (to.equals(groupInfoList.get(j).getGroupID())) {
                                    to = groupInfoList.get(j).getGroupName()+" 群";
                                    break;
                                }
                            }
                        else for (int j = 0; j < userInfoList.size(); j++) {
                                if (to.equals(userInfoList.get(j).getUserId())) {
                                    to = userInfoList.get(j).getNickName();
                                    break;
                                }
                            }
                        if(content.startsWith("&lt"))
                            content = "[会话]";
                        jTextArea.append(df.format(new Date())+"\n"+from+"对"+to+"说:"+content+"\n");
                        jTextArea.paintImmediately(jTextArea.getBounds());
                        if(id!="")
                        {this.replayMsg("hello,"+from,id);
                            jTextArea.append(df.format(new Date())+"\n我对"+from+"说:"+"hello,"+from+"\n");
                            jTextArea.paintImmediately(jTextArea.getBounds());}
                        if(jScrollPane.getHeight()<=700) {
                            jPanel.validate();
                            jPanel.repaint();
                        }
                    }
                }
            }
        }
    }
    //回复消息
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
        msg.put("Content",s);//会话内容
        msg.put("FromUserName",userID);
        msg.put("LocalID",cmId);
        msg.put("ToUserName",id);
        msg.put("Type",1);

        js.put("Msg",msg);
        js.put("Scene",0);
        this.sendPostRequest(url,js);
    }

    //发送消息
    private void sendMsg()throws Exception{
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
        msg.put("Content",jTextArea2.getText());//会话内容
        msg.put("FromUserName",userID);
        msg.put("LocalID",cmId);
        String id = jTextArea1.getText();
        //对名称进行搜索，先群后好友
        for(int i  = 0;i<groupInfoList.size();i++){
            GroupInfo groupInfo = groupInfoList.get(i);
            if(groupInfo.getGroupName().equals(id)) {
                id = groupInfo.getGroupID();
                break;
            }
        }
        for(int i = 0;i<userInfoList.size();i++){
            UserInfo userInfo = userInfoList.get(i);
            if(userInfo.getNickName().equals(id)){
                id = userInfo.getUserId();
                break;
            }
        }
        msg.put("ToUserName",id);
        msg.put("Type",1);

        js.put("Msg",msg);
        js.put("Scene",0);
        this.sendPostRequest(url,js);
        jTextArea.append(df.format(new Date())+"\n我对"+jTextArea1.getText()+"说:"+jTextArea2.getText()+"\n");
    }

    //获取好友及群列表
    private void getList(){
//        jTextArea.setText("");
        url = "https://"+host+"/cgi-bin/mmwebwx-bin/webwxgetcontact?lang=zh_CN&pass_ticket="+pass_ticket+"&r="+System.currentTimeMillis()+"&seq=0&skey="+skey;
        httpRequest = HttpRequest.post(url);
        String temp = httpRequest.header("Cookie",header).body();
        JSONObject jsonObject = JSONObject.fromObject(temp);
        JSONArray jsonArray = jsonObject.getJSONArray("MemberList");
        for(int i =0;i<jsonArray.size();i++){
            jsonObject = jsonArray.getJSONObject(i);
            if(!jsonObject.getString("UserName").contains("@@")) {
                UserInfo userInfo = new UserInfo();
                userInfo.setNickName(jsonObject.getString("NickName"));
                userInfo.setUserId(jsonObject.getString("UserName"));
                userInfo.setSignature(jsonObject.getString("Signature"));
                userInfoList.add(userInfo);
            }
            else if(jsonObject.getString("UserName").contains("@@")&&!activeGroupId.contains(jsonObject.getString("UserName")))
                unactiveGroupId.add(jsonObject.getString("UserName"));

        }
//        for(int i = 0;i<userInfoList.size();i++) {
//            jTextArea.append(i+" :用户ID: "+userInfoList.get(i).getUserId()+"\n");
//            jTextArea.append("   昵称: "+userInfoList.get(i).getNickName()+"\n");
//            jTextArea.append("  签名： "+userInfoList.get(i).getSignature()+"\n");
//        }

    }

    //生成必要的ID
    private void produceKey()throws Exception{
        //访问上一步获得到的redirect_url,获取skey,wxsid,wxuid,pass_ticket
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
            skey = URLEncoder.encode(skey,"utf-8");
            wxsid = s.substring(s.indexOf("<wxsid>"),s.indexOf("</wxsid>"));
            wxsid = wxsid.replace("<wxsid>","").trim();
            wxuin = s.substring(s.indexOf("<wxuin>"),s.indexOf("</wxuin>"));
            wxuin = wxuin.replace("<wxuin>","").trim();
            pass_ticket = s.substring(s.indexOf("<pass_ticket>"),s.indexOf("</pass_ticket>"));
            pass_ticket = pass_ticket.replace("<pass_ticket>","").trim();
            pass_ticket = URLEncoder.encode(pass_ticket,"utf-8");
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

        //转化并获取第一次的SyncKey中的key和val
        this.syncKeyTransfer(s);

        String s3[] = s.split(",");//进行第一次字符串分割
        List<String> usrName = new ArrayList<>();//存储所有的usernameID
        for(String usrname:s3){
            if(usrname.contains("UserName"))
                usrName.add(usrname);
        }
        //获取群ID
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

        //开启微信状态通知
        url = "https://"+host+"/cgi-bin/mmwebwx-bin/webwxstatusnotify?lang=zh_CN&pass_ticket="+pass_ticket;
        js.put("Code",3);
        js.put("FromUserName",userID);
        js.put("ToUserName",userID);
        js.put("ClientMsgId", Long.valueOf(timeStamp));
        this.sendPostRequest(url,js,header);
//        this.remove(jLabel_0);
//        this.add(jButton_0);
//        this.add(jButton_1);
//        this.add(jButton_2);
//        this.add(jButton_3);
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
//        int count = jsonObject.getInt("Count");
//        jTextArea.append("共有"+count+"个群:\n");
        JSONArray contactList = jsonObject.getJSONArray("ContactList");
        for(int i = 0;i<contactList.size();i++){
//            jTextArea.append("第"+(i+1)+"个群("+contactList.getJSONObject(i).getString("NickName")+")共有"+contactList.getJSONObject(i).getInt("MemberCount")+"位成员\n");
            GroupInfo groupInfo = new GroupInfo();
            groupInfo.setGroupID(contactList.getJSONObject(i).getString("UserName"));
            groupInfo.setMemberCount(contactList.getJSONObject(i).getInt("MemberCount"));
            groupInfo.setGroupName(contactList.getJSONObject(i).getString("NickName"));
            JSONArray jsonArray = contactList.getJSONObject(i).getJSONArray("MemberList");
            for(int j = 0;j<jsonArray.size();j++){
//                jTextArea.append("第"+(j+1)+"位成员: "+jsonArray.getJSONObject(j).getString("NickName")+"\n");
                UserInfo userInfo = new UserInfo();
                userInfo.setNickName(jsonArray.getJSONObject(j).getString("NickName"));
                userInfo.setUserId(jsonArray.getJSONObject(j).getString("UserName"));
                groupInfo.getGroup().add(userInfo);
            }
            groupInfoList.add(groupInfo);
        }
    }
    //生成二维码并显示
    private void showPic(String url)throws Exception{
        URL u = new URL(url);
        DataInputStream in = new DataInputStream(u.openStream());
        FileOutputStream out = new FileOutputStream(new File("1.jpg"));
        byte[] b = new byte[1024];
        int length;
        while((length=in.read(b))>0){
            out.write(b,0,length);
        }
        in.close();
        out.close();
        ImageIcon image = new ImageIcon("1.jpg");
        jLabel_0 = new JLabel(image);
        jLabel_0.setBounds(0,0,image.getIconWidth(),image.getIconHeight());
        if(jPanel.getComponentCount()==0) {
            jPanel.add(jLabel_0);
            this.setVisible(true);
        }
        jPanel.validate();
        jPanel.repaint();

    }

    //获取二维码的uuid
    private String produceUuid()throws Exception{
        timeStamp = String.valueOf(System.currentTimeMillis());
        url = "https://login.wx.qq.com/jslogin?appid=wx782c26e4c19acffb&redirect_uri=https%3A%2F%2Fwx.qq.com%2Fcgi-bin%2Fmmwebwx-bin%2Fwebwxnewloginpage&fun=new&lang=zh_CN&_=" + timeStamp;//request的设置
        String s = this.sendPostRequest(url, null);
        uuid = s.substring(s.length() - 14, s.length() - 2);
        s = this.produceErWei(uuid);
        return s;
    }

    //退出微信
    private void exitWeChat()throws Exception{
        url = "https://"+host+"/cgi-bin/mmwebwx-bin/webwxlogout?redirect=1&type=0&skey="+skey;
        String s = httpRequest.post(url).header("Cookie",header).send("sid="+URLEncoder.encode(wxsid,"utf-8")+"&uin="+wxuin).body();
        System.out.println(s);
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        System.setProperty ("jsse.enableSNIExtension", "false");
        final Main htmlUnit = new Main();
        Long oldTime;
        Long newTime;
        String s;
        //获取二维码的uuid
        out:while(true) {
            s = htmlUnit.produceUuid();
            htmlUnit.showPic(s);
            oldTime = System.currentTimeMillis();
            int tip=1;
            url = "https://login.weixin.qq.com/cgi-bin/mmwebwx-bin/login?loginicon=true&uuid="+uuid+"&tip="+tip+"&_="+timeStamp;
            in:while(true){
                s= htmlUnit.sendPostRequest(url, null);
                if(s.contains("408")){
                    newTime = System.currentTimeMillis();
                    if(newTime-oldTime>=22000)
                        continue out;
                }
                if(s.contains("201")&&s.contains("userAvatar")){
//                String s2[]=s.split("'");
//                userAvatar  =s2[1];
                    tip = 0;
//                continue;
                }
                else{
                    String s1[] = s.split("\"");
                    if(s1.length==3) {
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
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                jPanel.remove(jLabel_0);
//        this.add(jButton_0);
//       jPanel.add(jButton_1);
//                jPanel.add(jButton_2);
//        jPanel.add(jButton_3);
                jScrollPane = new JScrollPane(jTextArea);
                jTextArea.setText("您已登录成功，开始进行实时消息记录!\n");
                jTextArea.setSize(600,600);
                jScrollPane.setHorizontalScrollBarPolicy(
                        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                jScrollPane.setVerticalScrollBarPolicy(
                        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                jPanel.add(jScrollPane);
                jPanel.validate();
                jPanel.repaint();
//                jButton_1.addActionListener(new ActionListener() {
//                    @Override
//                    public void actionPerformed(ActionEvent e) {
//                        try{
//                            htmlUnit.exitWeChat();
//                        }catch (Exception e1){
//                            e1.printStackTrace();
//                        }
//                    }
//
//                });
            }
        });

//        jButton_2.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                try{
//
//                }catch (Exception e2){
//                    e2.printStackTrace();
//                }
//            }
//        });
//        jButton_3.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                jPanel.add(jButton_1);
//                jPanel.add(jTextArea1);
//                jPanel.add(jTextArea2);
//                jTextArea1.setText("");
//                jTextArea2.setText("");
//                jPanel.validate();
//                jPanel.repaint();
//                jTextArea.append("请在左边文本框输入群名称或好友名称，右边输入文本信息\n");
//            }
//        });
//        jButton_1.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                try{
//                    htmlUnit.sendMsg();
//                    jTextArea2.setText("");
//                }catch (Exception e1){
//                    e1.printStackTrace();
//                }
//            }
//        });
//        Thread thread= new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while(flag){
//                    try{
//                        htmlUnit.checkMsg();
//                    }catch (Exception e2){
//                        e2.printStackTrace();
//                    }
//                }
//            }
//        });
        htmlUnit.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {

            }

            @Override
            public void windowClosing(WindowEvent e) {
                  try{
                      htmlUnit.exitWeChat();
                  }catch (Exception e1){
                      e1.printStackTrace();
                  }
            }

            @Override
            public void windowClosed(WindowEvent e) {
                try{
                    htmlUnit.exitWeChat();
                }catch (Exception e1){
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
        while(true){
            try{
                htmlUnit.checkMsg();
            }catch (Exception e2){
                e2.printStackTrace();
            }
        }

    }
}
