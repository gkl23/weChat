package weChat;

/**
 * Created by huzhejie on 2016/7/7.
 */

import java.io.PrintWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import blade.kit.http.HttpRequest;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


public class Main {
    private static HttpRequest httpRequest;

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
    private static JSONObject syncKey;
    private static StringBuffer syncKeyList = new StringBuffer();
    private static String header;
    private static String host;
    private static JSONObject baseRequest;
    private static String userAvatar;

    public Main() {
    }

    /**
     * Get请求
     *
     * @param url
     * @return
     * @throws Exception
     */
    private String sendGetRequest(String url) throws Exception {
        httpRequest =HttpRequest.get(url);
         return  httpRequest.body();
    }
    /**
     * Post 请求
     *
     * @param url
     * @return
     * @throws Exception
     */
    private String sendPostRequest(String url,JSONObject js) throws Exception {
        httpRequest = HttpRequest.post(url);
        if(js==null) {
            return httpRequest.body();
        }
        else {
            return httpRequest.send(js.toString()).body();
        }
    }
    /**
     * Post 请求
     *
     * @param url
     * @return
     * @throws Exception
     */
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
    private void checkMsg(String url) throws Exception{
        Long r = System.currentTimeMillis();
        url ="https://webpush."+host+"/cgi-bin/mmwebwx-bin/synccheck?r="+r+"&skey="+skey+"&sid="+wxsid+"&uin="+wxuin+"&deviceid="+DeviceID+"&synckey="+syncKeyList.toString()+"&_="+timeStamp;
        System.out.println(syncKeyList);
        httpRequest = HttpRequest.get(url);
        String s = httpRequest.header("Cookie",header).body();
//        System.out.print(s);
        if (s.contains("2")||s.contains("7")) {
            //获取最新消息
            url = "https://"+host+"/cgi-bin/mmwebwx-bin/webwxsync?sid=" + wxsid + "&skey=" + skey + "&lang=zh_CN&pass_ticket=" + pass_ticket;
            js.clear();
            js.put("BaseRequest", baseRequest);
            js.put("SyncKey", syncKey);
            js.put("rr", ~(System.currentTimeMillis()));
            s = this.sendPostRequest(url, js,header);
            syncKeyList = new StringBuffer();
            this.syncKeyTransfer(s);
        }
    }

    //发送最新消息
    private void sendMsg(String url)throws Exception{
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
        msg.put("Content","测试一下");//输入会话内容
        msg.put("FromUserName",userID);
        msg.put("LocalID",cmId);
        msg.put("ToUserName","filehelper");
        msg.put("Type",1);

        js.put("Msg",msg);
        js.put("Scene",0);
        this.sendPostRequest(url,js);
    }

    //获取好友及列表
    private String getList(String url){
        url = "https://"+host+"/cgi-bin/mmwebwx-bin/webwxgetcontact?lang=zh_CN&pass_ticket="+pass_ticket+"&r="+System.currentTimeMillis()+"&seq=0&skey="+skey;
        httpRequest = HttpRequest.post(url);
        return httpRequest.header("Cookie",header).body();
    }

    //轮询直到用户确认登录
    private void checkLogin(String url)throws Exception{
        String s = "";
        int tip=1;
        url = "https://login.weixin.qq.com/cgi-bin/mmwebwx-bin/login?loginicon=true&uuid="+uuid+"&tip="+tip+"&_="+timeStamp;
        while(true){
            s= this.sendPostRequest(url, null);
            httpRequest.disconnect();
            if(s.contains("408")){
                continue;
            }
            if(s.contains("201")&&s.contains("userAvatar")){
                String s2[]=s.split("'");
                userAvatar  =s2[1];
                tip = 0;
                continue;
            }
            else{
                String s1[] = s.split("\"");
                if(s1.length==3) {
                    url = s1[1] + "&fun=new&version=v2&lang=zh_CN";
                    URL u = new URL(url);
                    host = u.getHost();
                }
                break;
            }
        }
        //访问上一步获得到的redirect_url,获取skey,wxsid,wxuid,pass_ticket
        s = this.sendGetRequest(url);
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
    private void initWeChat(String url)throws Exception{
        timeStamp  = String.valueOf(System.currentTimeMillis());
        url = "https://"+host+"/cgi-bin/mmwebwx-bin/webwxinit?pass_ticket="+pass_ticket;
        DeviceID = "e"+this.produceDevID().substring(1);
        js = new JSONObject();
        JSONObject baseRequest = new JSONObject();
        baseRequest.put("Uin",wxuin);
        baseRequest.put("Sid",wxsid);
        baseRequest.put("Skey",skey);
        baseRequest.put("DeviceID",DeviceID.toString());
        js.put("BaseRequest",baseRequest);
        String s = this.sendPostRequest(url,js,header);
        httpRequest.disconnect();

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
        usrName.clear();
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
        httpRequest.disconnect();
    }

    //获取最近活跃的群以及好友列表(XhatRoomID)
    private String getRecentList(String url)throws Exception{
        url="https://"+host+"/cgi-bin/mmwebwx-bin/webwxbatchgetcontact?type=ex&r="+timeStamp+"&lang=zh_CN&pass_ticket="+pass_ticket;
        js.clear();
        js.put("BaseRequest",baseRequest);
        js.put("Count",3);
        JSONArray groupJs = new JSONArray();
        for(int i = 0;i<activeGroupId.size();i++){
            JSONObject single = new JSONObject();
            single.put("UserName",activeGroupId.get(i));
            single.put("ChatRoomId","");
            groupJs.add(single);
        }
        js.put("List",groupJs);
        return this.sendPostRequest(url,js);
    }


    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        System.setProperty ("jsse.enableSNIExtension", "false");
        timeStamp  = String.valueOf(System.currentTimeMillis());
        int tip = 1;
        String url = "";
        //获取二维码的uuid
        url = "https://login.wx.qq.com/jslogin?appid=wx782c26e4c19acffb&redirect_uri=https%3A%2F%2Fwx.qq.com%2Fcgi-bin%2Fmmwebwx-bin%2Fwebwxnewloginpage&fun=new&lang=zh_CN&_="+timeStamp;//request的设置
        Main htmlUnit = new Main();
        String s= htmlUnit.sendPostRequest(url, null);
        httpRequest.disconnect();
        uuid = s.substring(s.length()-14,s.length()-2);
        String temp  = htmlUnit.produceErWei(uuid);
        ImageViewerFrame imageViewerFrame = new ImageViewerFrame();
        imageViewerFrame.producePic(temp);

        htmlUnit.checkLogin(url);
        htmlUnit.initWeChat(url);
        s = htmlUnit.getList(url);
        PrintWriter pw = new PrintWriter("3.txt");
        pw.write(s);
        pw.close();


        //退出微信
//        url = "https://"+host+"/cgi-bin/mmwebwx-bin/webwxlogout?redirect=1&type=0&skey="+skey;
//        HttpRequest.post(url).send("sid="+wxsid+"&uin="+wxuin);
    }
}
