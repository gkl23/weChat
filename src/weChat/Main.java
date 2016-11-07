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
import java.net.URL;
import java.sql.ResultSet;
import java.text.ParseException;
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
import Utils.DBConnect.TIMETYPE;
import Utils.PinyinComparator;
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
	private static Boolean isLogin = false;// 是否登录
	private static int minSenseWarn;
	private static int maxSenseWarn;
	private static String onlineTime;//上线时间
	private static String offlineTime;//下线时间
	private static String apiKey = "";
	private static String url;
	private static String timeStamp;
	private static String uuid;
	private static String skey;
	private static String wxsid;
	private static String wxuin;
	private static String pass_ticket;
	private static String DeviceID;
	private static JSONObject js;
	private static String userID = "";
	private static String userName_wx = "";
	private static String userName_robot = "";
	private static String memberID = "";
	private static String groupID = "";
	private static String tipGroupID = "";
	private static final String ATDELIM = " "; // ！！注意，@消息的分隔符并不是空格，所以设为常量
	private String webwx_data_ticket; // 用于上传图片和文件
	private int fileIndex; // 记录上传文件的序号
	private int untitledFileNameIndex; // 记录未命名的文件名序号
	private boolean isOnline = false;//机器人是否上线

	// 群聊id列表，key表示加密id，value表示解密id
	private static Map<String, String> groupIdMap = new HashMap<String, String>();

	// 群成员uin列表，key表示群聊加密id，value表示每个成员的加密id和uin的键值对列表
	private static Map<String, Map<String, String>> groupMemberUinMap = new HashMap<String, Map<String, String>>();

	// 群聊id，群名
	private static List<String> groupNameList = new ArrayList<>();
	private static List<String> recordList = new ArrayList<>();
	private static Map<String,GroupInfo> tempGroupInfoList = new HashMap<>();
	private static List<GroupInfo> groupInfoList = new ArrayList<>();
	private static Map<String, UserInfo> userInfoList = new HashMap<String, UserInfo>();
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
	private static GroupInfo modifyGroup;
	private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static List<TipRecord> tipRecordList = new ArrayList<>();
	private static Document doc;
	private static MMAnalyzer analyzer = new MMAnalyzer(1);

	private static WindowUI windowUI;

	private static DBConnect dbConnect;

	private static LoadingDialogJFrame loadingDialogJFrame; // 加载提示框

	private boolean hasNotified = false; // 标识是否已经获取了联系人列表之外的群聊

	private static Thread checkForMsgThread; // 监听最新消息的线程

	private static Vector<String> inviteFriendName = new Vector<>();// 存放能够邀请的用户的用户名
	private static Vector<UserInfo> inviteFriendList = new Vector<>();// 存放邀请的用户列表

	private static Vector<String> searchInviteFriend = new Vector<>();// 存放搜索并排序后的结果
	private static Vector<String> searchInvitedGroup = new Vector<>();// 存放搜索并排序后的结果

	private static Vector<String> removeFriendName = new Vector<>();
	private static Vector<UserInfo> removeFriendList = new Vector<>();

	private static Vector<String> searchRemoveFriend = new Vector<>();
	private static Vector<String> searchRemoveGroup = new Vector<>();

	private static Vector<String> group = new Vector<>();// 存放现在所有群的群名
	private static Vector<String> modifyMemberList = new Vector<>();
    private static UserInfo modifyMember = new UserInfo();//存放需要修改的list
    private static int modifyUserIndex;
//	private String timerDate; // 记录定时发布的日期，防止一个时刻同时发送多条定时消息

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
	 *
	 * @param content
	 *            聊天内容
	 * @param userId
	 *            用户ID
	 * @return 回复内容
	 */
	public String aiChat(String content, String userId) {
		final String apiUrl = "http://www.tuling123.com/openapi/api";
		final String secretKey = "02a06d8364d4ef9a"; // key
		final String timeStamp = System.currentTimeMillis() + "";

		String key = Md5.MD5(secretKey + timeStamp + apiKey);

		JSONObject chatData = new JSONObject();
		chatData.put("key", apiKey);
		chatData.put("info", content);
		chatData.put("userid", userId);
		String encryptChatData = new Aes(key).encrypt(chatData.toString());

		JSONObject requestData = new JSONObject();
		requestData.put("key", apiKey);
		requestData.put("timestamp", timeStamp);
		requestData.put("data", encryptChatData);
		HttpRequest request = HttpRequest.post(apiUrl).header("Content-Type", "application/json")
				.send(requestData.toString());
		String response = request.body();
		// System.out.println(response);
		request.disconnect();

		// 将响应转换为json数据
		JSONObject responseJson = JSONObject.fromObject(response);
		int code = responseJson.getInt("code");
		String text = responseJson.getString("text"); // 提示文字
		JSONArray list; // 新闻列表或者菜谱列表
		switch (code) {
			case 100000: // 文字消息
				response = text;
				break;
			case 200000: // 链接消息
				response = "该功能暂时不开放";
				break;
			case 302000: // 新闻消息
				response = "该功能暂时不开放";
				break;
			case 308000: // 菜谱消息
				response = text;
				list = responseJson.getJSONArray("list");
				JSONObject cookbook;
				for (Object o : list) {
					cookbook = (JSONObject) o;
					sendFileMsg(cookbook.getString("icon"), userId);
				}
				break;
			default: // 不可处理的消息
				// System.out.println(text);
				response = "智能聊天存在故障，请稍后重试。";
				break;
		}

		return response;
	}

	/**
	 * 读取词库文件
	 *
	 * @throws Exception
	 */
	public void readFiles() throws Exception {
		String s[] = null;

		// 根据对应的微信用户创建词库目录
		File dir = new File(wxuin);
		if (!dir.exists())
			dir.mkdir();

		File public_file = new File(wxuin + "/公开.txt");
		File private_file = new File(wxuin + "/私密.txt");
		File sense_file = new File(wxuin + "/敏感词.txt");
		File config_file = new File(wxuin+ "/配置.txt");
		BufferedWriter bw = null;
		Boolean flag1 = false, flag2 = false, flag3 = false,flag4 = false;
		if (!public_file.exists()) {
			public_file.createNewFile();
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(public_file), "UTF-8"));
			bw.write("请按照格式：  关键词--回复内容(一行)    来编写~");
			bw.flush();
			bw.close();
		}
		if (!private_file.exists()) {
			private_file.createNewFile();
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(private_file), "UTF-8"));
			bw.write("请按照格式：  关键词--回复内容(一行)   来编写~");
			bw.flush();
			bw.close();
		}
		if (!sense_file.exists()) {
			sense_file.createNewFile();
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(sense_file), "UTF-8"));
			bw.write("请按照格式：  敏感词(一行)    来编写~");
			bw.flush();
			bw.close();
		}
		if(!config_file.exists()){
			config_file.createNewFile();
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(config_file),"UTF-8"));
			bw.write("敏感词上限,敏感词下限,图灵API,机器人上班时间,机器人下班时间\r\n");
			bw.flush();
			bw.close();
		}
		InputStreamReader read = null;
		BufferedReader br = null;
		String str = null;
		read = new InputStreamReader(new FileInputStream(public_file), "UTF-8");
		br = new BufferedReader(read);

		while ((str = br.readLine()) != null) {
			if (!flag1)
				flag1 = true;
			else {
				s = str.split("--");
				publicReply.put(s[0], s[1]);
			}
		}
		read.close();
		br.close();
		read = new InputStreamReader(new FileInputStream(private_file), "UTF-8");
		br = new BufferedReader(read);
		while ((str = br.readLine()) != null) {
			if (!flag2)
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
		read = new InputStreamReader(new FileInputStream(sense_file), "UTF-8");
		br = new BufferedReader(read);
		while ((str = br.readLine()) != null) {
			if (!flag3)
				flag3 = true;
			else {
				senseReply.add(str);
			}
		}
		read.close();
		br.close();
		read = new InputStreamReader(new FileInputStream(config_file), "UTF-8");
		br = new BufferedReader(read);
		while (true) {
			if (!flag4) {
				br.readLine();
				flag4 = true;
			}
			else if((str = br.readLine())==null) {
				bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(config_file,true), "UTF-8"));
				bw.write("3,1,49d5dd04005a4d82b7d5bc30dae96821,8:00,20:00");
				bw.flush();
				bw.close();
				minSenseWarn = 1;
				maxSenseWarn = 3;
				apiKey = "49d5dd04005a4d82b7d5bc30dae96821";
				onlineTime = "8:00";
				offlineTime = "20:00";
				break;
			}
			else {
				String []config = str.split(",");
				try {
					minSenseWarn = Integer.parseInt(config[1]);
					maxSenseWarn = Integer.parseInt(config[0]);
					apiKey = config[2];
					onlineTime = config[3];
					offlineTime = config[4];
				}catch (Exception e){
					JOptionPane.showMessageDialog(null,"错误：配置文件已损坏，请删除后重启程序","信息提示",JOptionPane.ERROR_MESSAGE);
				}
				break;
			}
		}
		read.close();
		br.close();
	}

	private void configInfo(){
		File dir = new File(wxuin);
		if (!dir.exists())
			dir.mkdir();
		File config_file = new File(wxuin+ "/配置.txt");
		BufferedWriter bw = null;
		config_file.delete();
		try {
			config_file.createNewFile();
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(config_file), "UTF-8"));
			bw.write("敏感词上限,敏感词下限,图灵API,机器人上班时间,机器人下班时间\r\n");
			bw.flush();
			String config =  maxSenseWarn+","+minSenseWarn+","+apiKey+","+onlineTime+","+offlineTime;
			bw.write(config);
			bw.flush();
			bw.close();
		}catch(Exception e){
			e.printStackTrace();
		}

	}

	/**
	 * 生成DeviceID
	 *
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
	 *
	 * @param s
	 *            为uuid
	 * @return 生成二维码的url
	 */
	private String produceErWei(String s) {
		s = "http://login.weixin.qq.com/qrcode/" + s;
		return s;
	}

	/**
	 * 构建一个按照拼音排序的groupInfoList
	 */
	private void reOrderGroupList(){
		for(int i = 0;i<groupNameList.size();i++)
			for(GroupInfo groupInfo:tempGroupInfoList.values()){
				if(groupNameList.get(i).equals(groupInfo.getGroupName()+groupInfo.getGroupID()))
					groupInfoList.add(groupInfo);
			}
	}

	/**
	 * SyncKey转换
	 *
	 * @param s
	 *            转换前的包含synckey的json数据
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
		getRecentList();
		v_ticket = "";
	}

	/**
	 * 检查消息是否有更新 并对消息类别进行分类（如好友验证，图片消息，文字消息等）
	 */
	private void checkMsg() {
		timeStamp = String.valueOf(System.currentTimeMillis());
		String url = "https://webpush." + host + "/cgi-bin/mmwebwx-bin/synccheck?r=" + timeStamp + "&skey=" + skey
				+ "&sid=" + wxsid + "&uin=" + wxuin + "&deviceid=" + DeviceID + "&synckey=" + syncKeyList.toString()
				+ "&_=" + timeStamp;
		String s = HttpRequest.get(url).header("Cookie", header).body();
		if (StringKit.isBlank(s))
			return;

		int retcode = Integer.parseInt(s.substring(s.indexOf('"') + 1, s.indexOf(',') - 1));
		if (retcode != 0) {
			System.out.println(retcode);
			loadingDialogJFrame.shutdown("程序通信存在异常，请重新启动！");
		}

		int selector = Integer.parseInt(s.substring(s.lastIndexOf(":\"") + 2, s.lastIndexOf('"')));

		boolean isListen = false;

		if (selector != 0) { // 如果selector不为0，则获取最新消息
			url = "https://" + host + "/cgi-bin/mmwebwx-bin/webwxsync?sid=" + wxsid + "&skey=" + skey
					+ "&lang=zh_CN&pass_ticket=" + pass_ticket;
			js.clear();
			js.put("BaseRequest", baseRequest);
			js.put("SyncKey", syncKey);
			js.put("rr", ~System.currentTimeMillis());
			s = null;
			while (StringKit.isBlank(s)) // 避免响应为空的情况
				s = sendPostRequest(url, js, header);
			syncKeyList = new StringBuffer();
			syncKeyTransfer(s);
			JSONObject jsonObject = JSONObject.fromObject(s);

			/*
			 * 获取群成员uin
			 */
			int modContactCount = jsonObject.getInt("ModContactCount");
			if (modContactCount > 0) { // 如果有联系人信息更新
				JSONArray modContactList = jsonObject.getJSONArray("ModContactList");
				JSONObject modContact, member;
				JSONArray memberList;
				String groupUserName;
				boolean groupHasUpdated = false; // 标记群聊是否有更新
				for (int i = 0; i < modContactCount; i++) {
					modContact = modContactList.getJSONObject(i);
					groupUserName = modContact.getString("UserName");
					if (groupUserName.startsWith("@@")) { // 如果是群聊
						groupHasUpdated = true;
						if (groupIdMap.get(groupUserName) == null) // 如果是新的群聊信息
							groupIdMap.put(groupUserName, "0");
						memberList = modContact.getJSONArray("MemberList");
						Map<String, String> memberUins = new HashMap<String, String>();
						for (Object temp : memberList) {
							member = (JSONObject) temp;
							memberUins.put(member.getString("UserName"), member.getString("Uin"));
						}
						groupMemberUinMap.put(groupUserName, memberUins);
					}
				}
				if (groupHasUpdated)
					getGroupList();
			}

			/*
			 * 处理各类消息，包括文本、图片、表情等等
			 */
			int count = jsonObject.getInt("AddMsgCount");
			if (count != 0) {
				JSONArray jsonArray = jsonObject.getJSONArray("AddMsgList");
				for (int i = 0; i < jsonArray.size(); i++) {
					JSONObject jsonObject1 = jsonArray.getJSONObject(i);
					from = jsonObject1.getString("FromUserName");
					to = jsonObject1.getString("ToUserName");
					content = jsonObject1.getString("Content");
					int msgType = jsonObject1.getInt("MsgType");
					JSONObject jsonObject2 = jsonObject1.getJSONObject("RecommendInfo");
					v_ticket = jsonObject2.getString("Ticket");
					friendId = jsonObject2.getString("UserName");
					String replyInGroupContent = null, replyToMemberContent = null; // 在群聊中回复的内容和对好友回复的内容
					String fromId = null, toId = null; // 保存来往用户的id，用于回复消息
					ImageIcon image = null;
					GroupInfo group = null;
					UserInfo member = null;

					// 对from等变量进行处理，便于保存消息记录
					if (from.startsWith("@@")) { // 如果消息来自群聊
						for(GroupInfo groupInfo:groupInfoList)
							if(groupInfo.getGroupID().equals(from)) {
								group = groupInfo;
								break;
							}
						if (group != null) {
							isListen = group.getFlag();
							groupID = from;
							from = group.getGroupName();
							String temp[] = content.split(":");
							if (temp.length > 1) {
								memberID = temp[0];
								content = temp[1].replace("<br/>", "").trim();
								member = group.getGroup().get(memberID);
								if (member != null)
									to = member.getRemarkName();
							}
						}
					} else { // 消息来自单个用户
						fromId = from;
						if (userID.equals(fromId)) // 自己发送的消息，也就是从手机发出的消息
							from = "我";
						else if (userInfoList.get(fromId) != null) // 好友发出的消息
							from = userInfoList.get(fromId).getRemarkName();
						toId = to;
						if (userID.equals(toId)) { // 来自好友的消息
							to = "我";
							isListen = true;
						} else if (toId.startsWith("@@")) { // 自己发送到群聊的消息
							for(GroupInfo groupInfo:groupInfoList)
								if(groupInfo.getGroupID().equals(toId)) {
									group = groupInfo;
									break;
								}
							if (group != null) {
								isListen = group.getFlag();
								to = group.getGroupName();
							}
						} else if (userInfoList.get(toId) != null) { // 自己发送给好友的消息
							to = userInfoList.get(toId).getRemarkName();
							isListen = true;
						}
					}

					// 对不同类型的消息进行处理
					switch (msgType) {
						case 1: // 文本消息
							if (!"".equals(groupID) && isListen) { // 来自群聊的消息

								String groupNumberId = group.getGroupNumberId(); // 群聊解密id
								String memberUin = member.getUin(); // 群成员uin

								// 统计活跃度和签到记录的时候获取uin，避免大批量地同时获取uin出现ret:1205断连现象
								if (memberUin.equals("0"))
									getGroupMemberUins(groupID);

							/*
							 * 标记消息类型，用于区别处理，0表示普通发言，1表示活跃榜，2表示签到，3表示签到榜
							 */
								int contentType;
								switch (content) {

							/*
							 * 活跃榜
							 */
									case "日活跃榜":
									case "周活跃榜":
									case "活跃榜":
									case "月活跃榜":
									case "年活跃榜":
									case "总活跃榜":
										contentType = 1;
										break;

									case "签到": // 签到
										contentType = 2;
										break;

							/*
							 * 签到榜
							 */
									case "周签到榜":
									case "签到榜":
									case "月签到榜":
									case "年签到榜":
									case "总签到榜":
										contentType = 3;
										break;

									default: // 普通发言
										contentType = 0;
										break;
								}

								boolean rankFlag = false; // 处理活跃榜和签到榜，拦截消息回复

								try {
									if (!groupNumberId.equals("0") && !memberUin.equals("0")) { // 如果获取到了群聊解密id和群成员的uin
										if (userName_robot.equals("")) { // 如果没有登录软件
											switch (contentType) {
												case 1: // 活跃榜获取失败
													rankFlag = true;
													replyInGroupContent = "@" + to + ATDELIM + "功能存在异常！请稍后重试！";
													replyMsg(replyInGroupContent, groupID);
													JOptionPane.showMessageDialog(null, "请登录软件后再获取活跃榜！", "信息提示",
															JOptionPane.INFORMATION_MESSAGE);
													break;
												case 2: // 签到失败
													rankFlag = true;
													replyInGroupContent = "@" + to + ATDELIM + "功能存在异常！请稍后重试！";
													replyMsg(replyInGroupContent, groupID);
													JOptionPane.showMessageDialog(null, "请登录软件后再统计签到！", "信息提示",
															JOptionPane.INFORMATION_MESSAGE);
													break;
												case 3: // 签到榜获取失败
													rankFlag = true;
													replyInGroupContent = "@" + to + ATDELIM + "功能存在异常！请稍后重试！";
													replyMsg(replyInGroupContent, groupID);
													JOptionPane.showMessageDialog(null, "请登录软件后再获取签到榜！", "信息提示",
															JOptionPane.INFORMATION_MESSAGE);
													break;
												default: // 活跃度统计失败
													JOptionPane.showMessageDialog(null, "请登录软件后再统计活跃度！", "信息提示",
															JOptionPane.INFORMATION_MESSAGE);
													break;
											}
										} else {
											dbConnect.mergeActiveDegree(userName_robot, groupNumberId, memberUin, from, to);

										/*
										 * 处理活跃榜和签到榜，拦截消息回复
										 */
											List<String> result = null;
											int j, len;
											String rankText = null;
											switch (content) {
												case "日活跃榜": // 日活跃榜
													rankFlag = true;
													result = dbConnect.getActiveDegreeRank(userName_robot, groupNumberId,
															memberUin, TIMETYPE.DAY);
													rankText = "今日的活跃度";
													break;
												case "周活跃榜": // 周活跃榜
													rankFlag = true;
													result = dbConnect.getActiveDegreeRank(userName_robot, groupNumberId,
															memberUin, TIMETYPE.WEEK);
													rankText = "本周的活跃度";
													break;
												case "活跃榜":
												case "月活跃榜": // 月活跃榜
													rankFlag = true;
													result = dbConnect.getActiveDegreeRank(userName_robot, groupNumberId,
															memberUin, TIMETYPE.MONTH);
													rankText = "本月的活跃度";
													break;
												case "年活跃榜": // 年活跃榜
													rankFlag = true;
													result = dbConnect.getActiveDegreeRank(userName_robot, groupNumberId,
															memberUin, TIMETYPE.YEAR);
													rankText = "今年的活跃度";
													break;
												case "总活跃榜": // 总活跃榜
													rankFlag = true;
													result = dbConnect.getActiveDegreeRank(userName_robot, groupNumberId,
															memberUin, TIMETYPE.DEFAULT);
													rankText = "总的活跃度";
													break;
												case "周签到榜": // 周签到榜
													rankFlag = true;
													result = dbConnect.getSignRank(userName_robot, groupNumberId, memberUin,
															TIMETYPE.WEEK);
													rankText = "本周的签到";
													break;
												case "签到": // 签到
													rankFlag = true;
													result = dbConnect.insertSignRecord(userName_robot, groupNumberId,
															memberUin, from, to);
													rankText = "";
													break;
												case "签到榜":
												case "月签到榜": // 月签到榜
													rankFlag = true;
													result = dbConnect.getSignRank(userName_robot, groupNumberId, memberUin,
															TIMETYPE.MONTH);
													rankText = "本月的签到";
													break;
												case "年签到榜": // 年签到榜
													rankFlag = true;
													result = dbConnect.getSignRank(userName_robot, groupNumberId, memberUin,
															TIMETYPE.YEAR);
													rankText = "今年的签到";
													break;
												case "总签到榜": // 总签到榜
													rankFlag = true;
													result = dbConnect.getSignRank(userName_robot, groupNumberId, memberUin,
															TIMETYPE.DEFAULT);
													rankText = "总的签到";
													break;
											}
											if (rankFlag) { // 如果是处理活跃榜或者签到榜
												boolean b = false;
												replyInGroupContent = "@" + to + ATDELIM;
												if (rankText.equals("")) { // 处理签到
													replyInGroupContent += result.get(0).equals("0") ? "签到成功！"
															: "您今日已经在 " + result.get(1) + " 签到！";
													replyInGroupContent += "您本月的签到排名为 " + result.get(2) + "（"
															+ result.get(3) + "）";
												} else { // 处理榜单
													len = result.size();
													replyInGroupContent += "您" + rankText + "排名为 " + result.get(0) + "（"
															+ result.get(1) + "），前 " + (len - 2) / 3 + " 名的名单为：";
													for (j = 2; j < len; j += 3) {
														UserInfo targetMember = null;
														for (UserInfo tempMember : group.getGroup().values())
															if (tempMember.getUin().equals(result.get(j))) {
																targetMember = tempMember;
																break;
															}

													/*
													 * 如果群聊中没有前5名成员的信息，
													 * 则直接显示保存的群成员信息
													 */
														replyInGroupContent += (targetMember == null ? result.get(j + 1)
																: targetMember.getRemarkName()) + "（" + result.get(j + 2)
																+ "），";
													}
												}
												for(String key:userInfoList.keySet())
													if(key.equals(memberID)){
														replyMsg(replyInGroupContent, memberID);
														b = true;
													}
												if(!b)
													replyMsg(replyInGroupContent, groupID);
											}
										}
									} else {
										switch (contentType) {
											case 1: // 活跃榜获取失败
												rankFlag = true;
												replyInGroupContent = "@" + to + ATDELIM + "活跃榜获取失败！请私聊我反馈问题！";
												break;
											case 2: // 签到失败
												rankFlag = true;
												replyInGroupContent = "@" + to + ATDELIM + "签到失败！请私聊我反馈问题！";
												break;
											case 3: // 签到榜获取失败
												rankFlag = true;
												replyInGroupContent = "@" + to + ATDELIM + "签到榜获取失败！请私聊我反馈问题！";
												break;
											default: // 活跃度统计失败
												replyInGroupContent = "@" + to + ATDELIM + "活跃度统计失败！请私聊我反馈问题！";
												break;
										}
										replyMsg(replyInGroupContent, groupID);
									}
								} catch (Exception e) {
									switch (contentType) {
										case 1: // 活跃榜获取失败
											rankFlag = true;
											replyInGroupContent = "@" + to + ATDELIM + "活跃榜获取失败！请私聊我反馈问题！";
											break;
										case 2: // 签到失败
											rankFlag = true;
											replyInGroupContent = "@" + to + ATDELIM + "签到失败！请私聊我反馈问题！";
											break;
										case 3: // 签到榜获取失败
											rankFlag = true;
											replyInGroupContent = "@" + to + ATDELIM + "签到榜获取失败！请私聊我反馈问题！";
											break;
										default: // 活跃度统计失败
											replyInGroupContent = "@" + to + ATDELIM + "活跃度统计失败！请私聊我反馈问题！";
											break;
									}
									replyMsg(replyInGroupContent, groupID);
								}

								boolean matchKeyword = false; // 标识是否已经匹配到了关键词

								// 如果不是查询榜单或者签到，而且开启了自动回复
								if (!rankFlag && autoReplyFlag) {
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
												UserInfo userInfo = group.getGroup().get(userID);
												atFlag = userInfo != null && userInfo.getRemarkName().equals(atUser);
											}

										/*
										 * 处理跨群功能，满足"跨群 content"的格式才触发跨群功能，满足
										 * "跨群回复 群聊名@用户名 content"的格式才触发跨群回复功能
										 */
											if (atFlag) {
												String acrossKeyword = content.indexOf(' ') < 0 ? ""
														: content.substring(0, content.indexOf(' '));
												if (!member.isAcrossGroupFlag()) { // 如果用户没有赋予跨群权限
													replyInGroupContent = "@" + to + ATDELIM
															+ "您没有跨群（回复）权限，请私聊我开通跨群（回复）权限！";
													replyMsg(replyInGroupContent, groupID);
												} else if (windowUI.getRequestKeyword().equals(acrossKeyword)) {
													for (GroupInfo groupInfo : groupInfoList)
														if (groupInfo.getAcrossGroupFlag()) {
															replyInGroupContent = groupName + "@" + to + "（"
																	+ windowUI.getRequestKeyword() + "）："
																	+ content.substring(content.indexOf(' ') + 1);
															replyMsg(replyInGroupContent, groupInfo.getGroupID());
														}
													atFlag = false; // 取消自动回复，只进行跨群交流
												} else if (windowUI.getReplyKeyword().equals(acrossKeyword)) {
													content = content.substring(content.indexOf(' ') + 1);
													if (content.indexOf(' ') >= 0) {
														String names = content.substring(0, content.indexOf(' '));
														String[] nameArray = names.split("@");
														boolean hasName = false;
														if (nameArray.length > 1)
															for (GroupInfo groupInfo : groupInfoList)
																if (groupInfo.getGroupName().equals(nameArray[0])) {
																	for (UserInfo userInfo : groupInfo.getGroup().values())
																		if (userInfo.getRemarkName().equals(nameArray[1])) {
																			hasName = true;
																			break;
																		}
																}
														if (hasName) {
															for (GroupInfo groupInfo : groupInfoList)
																if (groupInfo.getAcrossGroupFlag()) {
																	replyInGroupContent = groupName + "@" + to + "（"
																			+ windowUI.getReplyKeyword() + "）：" + content;
																	replyMsg(replyInGroupContent, groupInfo.getGroupID());
																}
															atFlag = false; // 取消自动回复，只进行跨群交流
														}
													}
												}
											} else
												atFlag = true; // 取消@模式
										} else
											atFlag = true; // 取消@模式
									}
									if (atFlag) { // 如果没有开启@模式，或者满足@模式的条件
										for (String keyword : publicReply.keySet()) { // 匹配公共关键词
											if (content.equals(keyword)) {
												replyInGroupContent = "@" + to + ATDELIM + publicReply.get(keyword);
												replyMsg(replyInGroupContent, groupID);
												matchKeyword = true;
												break;
											}
										}

										if (!matchKeyword) // 公共关键词匹配失败，匹配私密关键词
											for (String keyword : privateReply.keySet())
												if (content.equals(keyword)) {
													replyInGroupContent = "@" + to + ATDELIM + "信息已私信回复您，谢谢！";
													replyMsg(replyInGroupContent, groupID);
													replyToMemberContent = privateReply.get(keyword);
													replyMsg(replyToMemberContent, memberID);
													matchKeyword = true;
													break;
												}

										if (!matchKeyword) { // 关键词匹配失败，智能回复
											replyInGroupContent = "@" + to + ATDELIM + aiChat(content, groupID);
											replyMsg(replyInGroupContent, groupID);
										}
									}

									// 如果开启了敏感词警告
									if (sensitiveFlag)
										for (String senseWord : senseReply) {
											if (content.equals(senseWord.trim())) {
												if (userName_robot.equals(""))
													JOptionPane.showMessageDialog(null, "请登录软件再统计敏感词记录！", "错误",
															JOptionPane.ERROR_MESSAGE);
												else
													try {
														int c = dbConnect.updateSenseWarn(userName_robot, groupNumberId,
																memberUin, from, to);
														if (c <= maxSenseWarn && c >= minSenseWarn) {
															replyInGroupContent = "@" + to + ATDELIM + "您言语有不当之处，警告一次";
															replyMsg(replyInGroupContent, groupID);
														}
													} catch (Exception e) {
														e.printStackTrace();
														JOptionPane.showMessageDialog(null, "敏感词功能异常！", "错误",
																JOptionPane.ERROR_MESSAGE);
													}
												break;
											}
										}
								}
							}
							else if (!userID.equals(fromId) && userInfoList.get(fromId) != null&&isListen) { // 来自好友的消息
								replyToMemberContent = aiChat(content, fromId);
								replyMsg(replyToMemberContent, fromId);
							}
							break;
						case 3: // 图片消息
							// url = "https://" + host +
							// "/cgi-bin/mmwebwx-bin/webwxgetmsgimg?&MsgID=" + msgID
							// + "&skey="
							// + skey + "&type=slave";
							// image = new
							// ImageIcon(HttpRequest.get(url).header("Cookie",header).bytes());
							// content = "\n";
							content = null;
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
//						System.out.println(jsonObject1);
							content = null;
							break;
						case 47: // emoji表情消息
//						System.out.println(jsonObject1);
							content = null;
							break;
						case 51: // 状态提醒消息
							String[] statusNotifyUserNames = jsonObject1.getString("StatusNotifyUserName").split(","); // 获取所有群聊加密id
							String[] groupIdSetList = jsonObject1.getString("Content").split(";")[6].split(","); // 获取所有群聊解密id
							if (statusNotifyUserNames.length > 1) { // 如果需要提醒的用户多于1个，说明是群聊列表
								content = null;
								if (!hasNotified) {
									int j, len = statusNotifyUserNames.length;
									for (j = 0; j < len; j++)
										if (statusNotifyUserNames[j].startsWith("@@"))
											groupIdMap.put(statusNotifyUserNames[j], groupIdSetList[j]);
									hasNotified = true;
									getGroupList();
								}
							} else { // 否则不是群聊列表，只是打开了一个对话或者收到了特殊账号发出的信息
								// System.out.println(
								// content.substring(content.indexOf("username&gt;")
								// + "username&gt;".length(),
								// content.indexOf("&lt;/username")));
								if (content.contains("@chatroom")) { // 如果statusnotify获取群聊id失败，则在这里再次获取
									groupIdMap.put(toId,
											content.substring(content.indexOf("username&gt;") + "username&gt;".length(),
													content.indexOf("&lt;/username")));
									getGroupList();
								}
								content = "[会话]";
							}
							break;
						case 9999: // 系统提示信息
//						System.out.println(jsonObject1);
							content = null;
							break;
						case 10000: // 系统消息
//						System.out.println(jsonObject1);

						/*
						 * 只处理加入群聊的消息
						 */
							if (content.contains("加入") && content.contains("群聊")
									&& groupIdMap.get(jsonObject1.getString("FromUserName")) == null)
								groupIdMap.put(jsonObject1.getString("FromUserName"), "0");
							content = null;

							break;
						case 10002: // 撤回消息
//						System.out.println(jsonObject1);
							content = null;
							break;
					}

					// 保存消息记录
					try {
						if (content != null && isListen) {
							content.replaceAll(",", "，");
							if ("".equals(groupID)) { // 不是来自群聊的消息
								boolean fromUserListOrGroupList = false; // 标记消息的来源和接收是否都在好友或者群聊列表里
								if (userID.equals(fromId)) { // 自己发出的消息
									fromUserListOrGroupList = userInfoList.get(toId) != null;
									if (!fromUserListOrGroupList)
										for(GroupInfo groupInfo:groupInfoList)
											if(groupInfo.getGroupID().equals(toId)) {
												fromUserListOrGroupList = true;
												break;
											}
//										fromUserListOrGroupList = groupInfoList.get(toId) != null;
								} else
									fromUserListOrGroupList = userInfoList.get(fromId) != null;
								if (fromUserListOrGroupList) { // 只记录来源和接收都在好友或者群聊列表里的消息
									recordList.add(df.format(new Date()) + "," + from + "," + to + ","
											+ (content.equals("\n") ? "[图片]" : content));
									doc.insertString(doc.getLength(),
											toId.startsWith("@@")
													? df.format(new Date()) + "\n我在 " + to + " 群中说："
													+ (content.equals("\n") ? "" : content + "\n")
													: df.format(new Date()) + "\n" + from + " 对 " + to + " 说："
													+ (content.equals("\n") ? "" : content + "\n"),
											windowUI.getAttributeSet());
									if (content.equals("\n")) {
										windowUI.getjTextPane().setCaretPosition(doc.getLength());
										windowUI.getjTextPane().insertIcon(image);
										doc.insertString(doc.getLength(), "\n", windowUI.getAttributeSet());
									}
									windowUI.getjTextPane().setCaretPosition(doc.getLength());
								}
							} else {
								recordList.add(df.format(new Date()) + "," + from + " 群," + to + ","
										+ (content.equals("\n") ? "[图片]" : content));
								doc.insertString(doc.getLength(),
										df.format(new Date()) + "\n" + from + " 群中 " + to + " 说："
												+ (content.equals("\n") ? "" : content + "\n"),
										windowUI.getAttributeSet());
								if (content.equals("\n")) {
									windowUI.getjTextPane().setCaretPosition(doc.getLength());
									windowUI.getjTextPane().insertIcon(image);
									doc.insertString(doc.getLength(), "\n", windowUI.getAttributeSet());
								}
								windowUI.getjTextPane().setCaretPosition(doc.getLength());
							}
						}

						if (replyInGroupContent != null && isListen) {
							replyInGroupContent.replaceAll(",", "，");
							doc.insertString(doc.getLength(), df.format(new Date()) + "\n我在 " + from + " 群中对 " + to
									+ " 说：" + replyInGroupContent + "\n", windowUI.getAttributeSet());
							windowUI.getjTextPane().setCaretPosition(doc.getLength());
							recordList.add(df.format(new Date()) + ",我" + "," + to + "(" + from + " 群),"
									+ replyInGroupContent);
						}

						if (replyToMemberContent != null && isListen) {
							replyToMemberContent.replaceAll(",", "，");
							doc.insertString(doc.getLength(), df.format(new Date()) + "\n我对 "
											+ (groupID.equals("") ? from : to) + " 说：" + replyToMemberContent + "\n",
									windowUI.getAttributeSet());
							windowUI.getjTextPane().setCaretPosition(doc.getLength());
							recordList.add(df.format(new Date()) + ",我" + "," + from + "," + replyToMemberContent);
						}
					} catch (BadLocationException e) {
						e.printStackTrace();
					}
					groupID = "";
					windowUI.getChatJPanel().validate();
					windowUI.getChatJPanel().repaint();
				}
			}
		}
	}

	/**
	 * 回复及发送消息
	 *
	 * @param s
	 *            回复的内容
	 * @param id
	 *            回复对象的ID
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
	 * @param addUserID
	 *            用户ID
	 * @param addGroupID
	 *            邀请用户进群的ID
	 */
	private boolean addMember(String addUserID, String addGroupID) {
		url = "https://" + host + "/cgi-bin/mmwebwx-bin/webwxupdatechatroom";
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("AddMemberList", addUserID);
		jsonObject.put("BaseRequest", baseRequest);
		jsonObject.put("ChatRoomName", addGroupID);
		httpRequest = HttpRequest.post(url, true, "fun", "addmember", "pass_ticket", pass_ticket)
				.header("Content-Type", "application/json;charset=UTF-8").header("Cookie", header)
				.send(jsonObject.toString());
		String response = httpRequest.body();
		httpRequest.disconnect();
		if (StringKit.isBlank(response))
			return false;
		return JSONObject.fromObject(response).getJSONObject("BaseResponse").getInt("Ret") == 0;
	}

	/**
	 * 将用户T出该群
	 *
	 * @param UserID
	 *            用户ID
	 * @param GroupID
	 *            群ID
	 */
	private boolean deleteMember(String UserID, String GroupID) {
		url = "https://" + host + "/cgi-bin/mmwebwx-bin/webwxupdatechatroom";
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("DelMemberList", UserID);
		jsonObject.put("BaseRequest", baseRequest);
		jsonObject.put("ChatRoomName", GroupID);
		httpRequest = HttpRequest.post(url, true, "fun", "delmember", "pass_ticket", pass_ticket)
				.header("Content-Type", "application/json;charset=UTF-8").header("Cookie", header)
				.send(jsonObject.toString());
		String response = httpRequest.body();
		httpRequest.disconnect();
		if (StringKit.isBlank(response))
			return false;
		return JSONObject.fromObject(response).getJSONObject("BaseResponse").getInt("Ret") == 0;
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
		String temp = HttpRequest.post(url).header("Cookie", header).body();
		if (StringKit.isBlank(temp))
			return;

		JSONObject jsonObject = JSONObject.fromObject(temp);
		JSONArray jsonArray = jsonObject.getJSONArray("MemberList");
		for (int i = 0; i < jsonArray.size(); i++) {
			jsonObject = jsonArray.getJSONObject(i);
			String userId = jsonObject.getString("UserName");
			if (!userId.contains("@@") && jsonObject.getInt("VerifyFlag") == 0) {
				UserInfo userInfo = userInfoList.get(userId);
				if (userInfo == null) {
					userInfo = new UserInfo();
					userInfo.setUserId(userId);
				}
				userInfo.setSignature(jsonObject.getString("Signature"));
				userInfo.setRemarkName(jsonObject.getString("RemarkName"));
				if (userInfo.getRemarkName().equals(""))
					userInfo.setRemarkName(jsonObject.getString("NickName"));
				userInfoList.put(userId, userInfo);
			}
		}
	}

	/**
	 * 获取skey，wxsid，wxuid等key
	 */
	private void produceKey() {
		String s = this.sendGetRequest(url);

		// 获取cookie
		Map<String, List<String>> headerFields = httpRequest.headers();
		header = "";
		for (String key : headerFields.keySet())
			if ("Set-Cookie".equalsIgnoreCase(key))
				for (String cookie : headerFields.get(key))
					if (cookie != null) {
						cookie = cookie.substring(0, cookie.indexOf(';') + 1);
						header += cookie;
						if (cookie.contains("webwx_data_ticket"))
							webwx_data_ticket = cookie.substring(cookie.indexOf('=') + 1, cookie.indexOf(';'));
					}

		if (StringKit.isAnyBlank(webwx_data_ticket, header, s)) { // 监听网络微信的必需参数，如果一个为空则不能正常监听微信
			loadingDialogJFrame.shutdown("微信登录失败！请重新启动软件！");
			return;
		}

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
	 * 修改备注姓名
	 * @param remarkName 修改后的备注
	 * @param userId 被修改用户的用户id（加密）
     */
	private boolean modifyRemarkName(String userId,String remarkName){
		url = "https://"+host+"/cgi-bin/mmwebwx-bin/webwxoplog?pass_ticket="+pass_ticket;
		js = new JSONObject();
		js.put("BaseRequest", baseRequest);
		js.put("CmdId",2);
		js.put("RemarkName",remarkName);
		js.put("UserName",userId);

		String response = this.sendPostRequest(url,js,header);
		return JSONObject.fromObject(response).getJSONObject("BaseResponse").getInt("Ret") == 0;

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
		if (StringKit.isBlank(s)) {
			loadingDialogJFrame.shutdown("程序初始化失败！请重新启动！");
			return;
		}
		this.syncKeyTransfer(s);

		// 获取userID
		JSONObject sc = JSONObject.fromObject(s);
		JSONObject user = sc.getJSONObject("User");
		userID = user.get("UserName").toString();
		userName_wx = user.get("NickName").toString();

		// 获取部分群聊信息
		JSONArray contactList = sc.getJSONArray("ContactList");
		for (Object temp : contactList) {
			JSONObject contact = (JSONObject) temp;
			String userName = contact.getString("UserName");
			if (userName.startsWith("@@"))
				groupIdMap.put(userName, "0");
		}

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
		groupNameList.clear();
		groupInfoList.clear();
		url = "https://" + host + "/cgi-bin/mmwebwx-bin/webwxbatchgetcontact?type=ex&r=" + timeStamp
				+ "&lang=zh_CN&pass_ticket=" + pass_ticket;
		js.clear();
		js.put("BaseRequest", baseRequest);
		js.put("Count", groupIdMap.size());
		JSONArray groupJs = new JSONArray();
		for (String groupId : groupIdMap.keySet()) {
			JSONObject single = new JSONObject();
			single.put("UserName", groupId);
			single.put("EncryChatRoomId", "");
			groupJs.add(single);
		}
		js.put("List", groupJs);
		String s = this.sendPostRequest(url, js);
		if (StringKit.isBlank(s))
			return;

		JSONObject jsonObject = JSONObject.fromObject(s);
		JSONArray contactList = jsonObject.getJSONArray("ContactList");
		String groupId, groupName, userId;
		for (int i = 0; i < contactList.size(); i++) {
			groupId = contactList.getJSONObject(i).getString("UserName");
			GroupInfo groupInfo = null;
			for(GroupInfo group:groupInfoList)
				if(group.getGroupID().equals(groupId)) {
					groupInfo = group;
					break;
				}
			if (groupInfo == null) { // 如果是新的群聊信息
				groupInfo = new GroupInfo();
				groupInfo.setAcrossGroupFlag(false);
				groupInfo.setGroupID(groupId);
			}
			groupInfo.setGroupNumberId(groupIdMap.get(groupInfo.getGroupID()));
			groupInfo.setMemberCount(contactList.getJSONObject(i).getInt("MemberCount"));
			groupName = contactList.getJSONObject(i).getString("NickName");
			groupInfo.setGroupName(groupName.equals("") ? "群聊" : groupName);

			JSONArray jsonArray = contactList.getJSONObject(i).getJSONArray("MemberList");
			Map<String, String> memberUins = groupMemberUinMap.get(groupInfo.getGroupID());
			Map<String, UserInfo> memberList = groupInfo.getGroup();
			for (int j = 0; j < jsonArray.size(); j++) {
				userId = jsonArray.getJSONObject(j).getString("UserName");
				UserInfo userInfo = memberList.get(userId);
				if (userInfo == null) {
					userInfo = new UserInfo();
					userInfo.setUserId(userId);
				}
				userInfo.setUin(
						memberUins == null ? "0" : memberUins.get(userId) == null ? "0" : memberUins.get(userId));
				userInfo.setRemarkName(jsonArray.getJSONObject(j).getString("DisplayName"));
				if (userInfo.getRemarkName().equals(""))
					userInfo.setRemarkName(jsonArray.getJSONObject(j).getString("NickName"));
				groupInfo.getGroup().put(userId, userInfo);
			}
			tempGroupInfoList.put(groupInfo.getGroupID(),groupInfo);
			if(!groupNameList.contains(groupInfo.getGroupName()+groupInfo.getGroupID()))
			groupNameList.add(groupInfo.getGroupName()+groupInfo.getGroupID());
		}
		Collections.sort(groupNameList,new PinyinComparator());
		this.reOrderGroupList();
	}

	/**
	 * 通过置顶群聊和置顶文件助手(filehelper)的形式获取群成员的uin
	 *
	 * @param groupUserName
	 *            群聊加密id列表
	 */
	private void getGroupMemberUins(String groupUserName) {
		final String functionUrl = "https://" + host + "/cgi-bin/mmwebwx-bin/webwxoplog"; // 进行(取消)置顶或者修改好友备注的url

		JSONObject requestJson = new JSONObject();
		requestJson.put("CmdId", 3);
		requestJson.put("OP", 1);
		requestJson.put("BaseRequest", baseRequest);

		/*
		 * 置顶群聊获取uin
		 */
		HttpRequest request;
		requestJson.put("UserName", groupUserName);
		request = HttpRequest.get(functionUrl, true, "pass_ticket", pass_ticket).header("Cookie", header)
				.send(requestJson.toString());
		request.body();
		request.disconnect();
		requestJson.put("UserName", "filehelper");
		request = HttpRequest.get(functionUrl, true, "pass_ticket", pass_ticket).header("Cookie", header)
				.send(requestJson.toString());
		request.body();
		request.disconnect();

		/*
		 * 取消置顶，隐藏获取uin的细节
		 */
		requestJson.put("OP", 0);
		request = HttpRequest.get(functionUrl, true, "pass_ticket", pass_ticket).header("Cookie", header)
				.send(requestJson.toString());
		request.body();
		request.disconnect();
		requestJson.put("UserName", groupUserName);
		request = HttpRequest.get(functionUrl, true, "pass_ticket", pass_ticket).header("Cookie", header)
				.send(requestJson.toString());
		request.body();
		request.disconnect();
	}

	/**
	 * 显示二维码
	 *
	 * @param url
	 *            二维码获取地址
	 */
	private void showPic(String url) {
		ImageIcon image = new ImageIcon(HttpRequest.get(url).bytes());
		if (updatePic) {
			windowUI.getjPanel().remove(windowUI.getjLabel_0());
			updatePic = false;
		}
		windowUI.setjLabel_0(new JLabel(new ImageIcon(image.getImage().getScaledInstance(350, 350, Image.SCALE_FAST))));
		windowUI.getjPanel().add(windowUI.getjLabel_0());
		windowUI.getMainFrame().setTitle("微信机器人--等待用户扫码");
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
		windowUI.setUserNameLabel(
				new JLabel("用户名：" + (userName_wx.length() > 10 ? userName_wx.substring(0, 11) + "..." : userName_wx)));
		windowUI.getUserNameLabel().setFont(new Font("黑体", 1, 16));
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		windowUI.getUserNameJPanel().add(windowUI.getUserNameLabel(), gbc);
		gbc.insets = new Insets(10, 20, 10, 20);
		windowUI.getUserButtonJPanel().add(windowUI.getLogin());
		windowUI.getUserButtonJPanel().add(windowUI.getSynchronization());
		gbc.gridx = 0;
		gbc.gridy = 1;
		windowUI.getUserNameJPanel().add(windowUI.getUserButtonJPanel(), gbc);
		windowUI.getUserInfoJPanel().add(windowUI.getUserNameJPanel());
	}

	/**
	 * 获取uuid
	 *
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
	private static void searchAndOrder(String s, Vector<String> nameList, Vector<String> searchList) {
		try {
			String keys[] = analyzer.segment(s, "|").split("|");
			HashMap<String, Integer> map = new HashMap<>();
			for (int i = 0; i < nameList.size(); i++) {
				int count = 0;
				for (int j = 1; j < keys.length - 1; j++) {
					if (nameList.get(i).contains(keys[j]))
						count++;
				}
				if (count != 0)
					map.put(nameList.get(i), count);
			}
			Set<Map.Entry<String, Integer>> mapEntries = map.entrySet();
			List<Map.Entry<String, Integer>> aList = new LinkedList<Map.Entry<String, Integer>>(mapEntries);
			// 对得到的搜索结果进行排序
			Collections.sort(aList, new Comparator<Map.Entry<String, Integer>>() {
				@Override
				public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
					return o2.getValue().compareTo(o1.getValue());
				}
			});
			searchList.clear();
			for (int i = 0; i < aList.size(); i++) {
				searchList.add(aList.get(i).getKey());
			}
		} catch (Exception e1) {
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
				try {
					Date offline = windowUI.getDf().parse(offlineTime);
					Date online = windowUI.getDf().parse(onlineTime);
					if (online.before(windowUI.getDf().parse(windowUI.getDf().format(new Date()))) &&
							offline.after(windowUI.getDf().parse(windowUI.getDf().format(new Date())))) {
						isOnline = true;
					}
					else{
						isOnline = false;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				while (isOnline) {
					checkMsg();
					Date now = new Date();
					Iterator<TipRecord> iterator = tipRecordList.iterator();
					while (iterator.hasNext()) {
						TipRecord tipRecord = iterator.next();
						// 指定时间发布
						if (windowUI.getDf().format(now).toString().equals(tipRecord.getTime())
								&& tipRecord.getPeriod() == 0) { // 如果今天没有发布定时消息，而且现在需要发送定时消息
							if(!df.format(now).split(" ")[0].equals(tipRecord.getTimeStamp())){
								tipRecord.setVisit(false);
								tipRecord.setTimeStamp(df.format(now).split(" ")[0]);
							}
							if(df.format(now).split(" ")[0].equals(tipRecord.getTimeStamp())&&!tipRecord.isVisit()) {
								for (GroupInfo groupInfo : groupInfoList) {
									if (groupInfo.getGroupName().equals(tipRecord.getGroupName())) {
										tipGroupID = groupInfo.getGroupID();
										break;
									}
								}
								replyMsg(tipRecord.getProperty(), tipGroupID);
								tipRecord.setVisit(true);
							}
						}
						// 间隔时间发布
						else if (windowUI.getDf().format(now).toString().equals(tipRecord.getTime())
								&&tipRecord.getPeriod() != 0) {
							for (GroupInfo groupInfo : groupInfoList) {
								if (groupInfo.getGroupName().equals(tipRecord.getGroupName())) {
									tipGroupID = groupInfo.getGroupID();
									break;
								}
							}
							int period = tipRecord.getPeriod();
							replyMsg(tipRecord.getProperty(), tipGroupID);
							int startHour = Integer.parseInt(tipRecord.getTime().split(":")[0]);
							String minute = tipRecord.getTime().split(":")[1];
							String nextHour = String.valueOf((startHour + period)%24);
							tipRecord.setTime(nextHour + ":" + minute);
						}
					}
				}
			}
		}, "listenForMsg");
		checkForMsgThread.start();
	}

	/**
	 * 发送文件消息（包括图片），如果成功返回true，否则返回false
	 *
	 * @param filePath
	 *            要发送的文件路径，如果是网络文件，路径应该以http开头
	 * @param toUserName
	 *            接收消息的好友或者群聊的加密id
	 * @return 如果发送图片成功返回true，否则返回false
	 */
	private boolean sendFileMsg(String filePath, String toUserName) {

		if (StringKit.isAnyBlank(filePath, toUserName)) // 确保路径和接收消息的对象不能为空
			return false;

		String uploadFileUrl = "https://file." + host + "/cgi-bin/mmwebwx-bin/webwxuploadmedia?f=json"; // 上传图片的url
		if (HttpRequest.options(uploadFileUrl).code() != 200) // 检验服务器是否可以上传图片，如果响应码不是200，说明不能上传图片
			return false;

		/*
		 * 上传文件
		 */
		// 分隔符
		final String boundary = "----WebKitFormBoundary" + generateFormBoundary();
		final String boundaryLine = "--";
		final String newLine = "\r\n";
		final long nowTime = System.currentTimeMillis();

		// 文件信息
		filePath = filePath.replaceAll("\\\\", "/"); // 替换所有转义字符‘\’，此处双转义的目的是正则表达式中‘\’也表示转义
		String fileType = getFileType(filePath); // 文件的contentType
		if (StringKit.isBlank(fileType)) // 无效链接或者不支持的文件类型
			return false;
		String mediaType = fileType.contains("image") ? "pic" : "doc"; // 文件的mediaType，目前理解的就是图片为pic，非图片均为doc
		String fileName; // 文件名
		Date lastModifiedDate; // 文件的最后修改日期
		int fileSize; // 文件大小（字节数）
		byte[] fileBytes = null; // 网络文件的字节流
		File file = null; // 本地文件
		if (filePath.startsWith("http")) { // 如果是网络文件
			fileName = "untitled" + (++untitledFileNameIndex) + "." + fileType.substring(fileType.lastIndexOf('/') + 1);
			lastModifiedDate = new Date();
			fileBytes = HttpRequest.get(filePath).bytes();
			fileSize = fileBytes.length;
		} else { // 本地文件
			file = new File(filePath);
			if (!file.exists())
				return false;
			fileName = file.getName();
			lastModifiedDate = new Date(file.lastModified());
			fileSize = (int) file.length();
		}

		if (fileSize == 0) // 不能上传空文件
			return false;
		final int onceFileMaxSize = 500 * 1000; // 一次上传请求的文件大小不能超过500K
		int chunks = fileSize % onceFileMaxSize == 0 ? fileSize / onceFileMaxSize : fileSize / onceFileMaxSize + 1;
		HttpRequest request;
		String response = null;

		for (int i = 0; i < chunks; i++) {
			// payload内容
			StringBuilder payload = new StringBuilder();
			payload.append(boundaryLine).append(boundary).append(newLine);
			payload.append("Content-Disposition: form-data; name=\"id\"").append(newLine).append(newLine);
			payload.append("WU_FILE_").append(fileIndex++).append(newLine);
			payload.append(boundaryLine).append(boundary).append(newLine);
			payload.append("Content-Disposition: form-data; name=\"name\"").append(newLine).append(newLine);
			payload.append(fileName).append(newLine);
			payload.append(boundaryLine).append(boundary).append(newLine);
			payload.append("Content-Disposition: form-data; name=\"type\"").append(newLine).append(newLine);
			payload.append(fileType).append(newLine);
			payload.append(boundaryLine).append(boundary).append(newLine);
			payload.append("Content-Disposition: form-data; name=\"lastModifiedDate\"").append(newLine).append(newLine);
			payload.append(lastModifiedDate).append(newLine);
			payload.append(boundaryLine).append(boundary).append(newLine);
			payload.append("Content-Disposition: form-data; name=\"size\"").append(newLine).append(newLine);
			payload.append(fileSize).append(newLine);
			payload.append(boundaryLine).append(boundary).append(newLine);
			if (chunks > 1) { // 分块上传
				payload.append("Content-Disposition: form-data; name=\"chunks\"").append(newLine).append(newLine);
				payload.append(chunks).append(newLine);
				payload.append(boundaryLine).append(boundary).append(newLine);
				payload.append("Content-Disposition: form-data; name=\"chunk\"").append(newLine).append(newLine);
				payload.append(i).append(newLine);
				payload.append(boundaryLine).append(boundary).append(newLine);
			}
			payload.append("Content-Disposition: form-data; name=\"mediatype\"").append(newLine).append(newLine);
			payload.append(mediaType).append(newLine);
			payload.append(boundaryLine).append(boundary).append(newLine);
			payload.append("Content-Disposition: form-data; name=\"uploadmediarequest\"").append(newLine)
					.append(newLine);

			// uploadMediaRequest
			JSONObject uploadMediaRequest = new JSONObject();
			uploadMediaRequest.put("UploadType", 2);
			uploadMediaRequest.put("BaseRequest", baseRequest);
			uploadMediaRequest.put("ClientMediaId", nowTime);
			uploadMediaRequest.put("TotalLen", fileSize);
			uploadMediaRequest.put("StartPos", 0);
			uploadMediaRequest.put("DataLen", fileSize);
			uploadMediaRequest.put("MediaType", 4);
			uploadMediaRequest.put("FromUserName", userID);
			uploadMediaRequest.put("ToUserName", toUserName);
			uploadMediaRequest.put("FileMd5", Md5.MD5(fileName));

			payload.append(uploadMediaRequest.toString()).append(newLine);
			payload.append(boundaryLine).append(boundary).append(newLine);
			payload.append("Content-Disposition: form-data; name=\"webwx_data_ticket\"").append(newLine)
					.append(newLine);
			payload.append(webwx_data_ticket).append(newLine);
			payload.append(boundaryLine).append(boundary).append(newLine);
			payload.append("Content-Disposition: form-data; name=\"pass_ticket\"").append(newLine).append(newLine);
			payload.append(pass_ticket).append(newLine);
			payload.append(boundaryLine).append(boundary).append(newLine);
			payload.append("Content-Disposition: form-data; name=\"filename\"; filename=\"").append(fileName)
					.append("\"").append(newLine).append("Content-Type: ")
					.append(chunks > 1 ? "application/octet-stream" : fileType).append(newLine).append(newLine);

			request = HttpRequest.post(uploadFileUrl)
					.header("Content-Length", i + 1 < chunks ? onceFileMaxSize : fileSize - i * onceFileMaxSize)
					.header("Content-Type", "multipart/form-data; boundary=" + boundary).send(payload.toString());
			if (file == null)
				request.send(fileBytes);
			else
				request.send(file);
			request.send(newLine + boundaryLine + boundary + boundaryLine + newLine);
			response = request.body();
			request.disconnect();
		}
		// System.out.println(response);
		if (StringKit.isBlank(response))
			return false;

		/*
		 * 发送文件消息
		 */
		String mediaId = JSONObject.fromObject(response).getString("MediaId");
		String sendFileMsgUrl = "https://" + host + "/cgi-bin/mmwebwx-bin/"
				+ (fileType.contains("image") ? "webwxsendmsgimg" : "webwxsendappmsg");
		JSONObject requestData = new JSONObject();
		requestData.put("BaseRequest", baseRequest);
		JSONObject msg = new JSONObject();
		msg.put("ClientMsgId", nowTime);
		msg.put("FromUserName", userID);
		msg.put("ToUserName", toUserName);
		msg.put("MediaId", mediaId);
		msg.put("LocalId", nowTime);
		msg.put("Type", fileType.contains("image") ? 3 : 6);
		requestData.put("Msg", msg);
		requestData.put("Scene", 0);
		HttpRequest.post(sendFileMsgUrl, true, "fun", "async", "f", "json", "pass_ticket", pass_ticket)
				.header("Content-Type", "application/json;charset=UTF-8").header("Cookie", header)
				.send(requestData.toString()).body();
		return false;
	}

	/**
	 * 生成16位的boundary，用于上传图片和文件
	 *
	 * @return 16位的boundary
	 */
	private String generateFormBoundary() {
		final char[] chars = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E',
				'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
				'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u',
				'v', 'w', 'x', 'y', 'z' }; // 10个数字和52个大小写字母
		final int len = 16; // 生成16位的boundary
		StringBuilder sb = new StringBuilder();
		Random random = new Random();
		for (int i = 0; i < len; i++)
			sb.append(chars[random.nextInt(chars.length)]);
		return sb.toString();
	}

	/**
	 * 根据文件路径获取文件的类型
	 *
	 * @param filePath
	 *            文件路径
	 * @return 文件的类型
	 */
	private String getFileType(String filePath) {
		if (filePath.startsWith("http")) // 如果是网络图片
			return HttpRequest.get(filePath).headers().get("Content-Type").get(0);
		String type;
		switch (filePath.substring(filePath.lastIndexOf('.') + 1)) {
			case "jpg":
				type = "image/jpeg";
				break;
			case "bmp":
				type = "image/bmp";
				break;
			case "gif":
				type = "image/gif";
				break;
			case "png":
				type = "image/png";
				break;
			default:
				type = "";
				break;
		}
		return type;
	}

	public static void main(String[] args) {
		dbConnect = new DBConnect();
		try {
			dbConnect.connectDB();
		}catch(Exception e){
			loadingDialogJFrame.shutdown("网络未连接，请检查网络后重启");
		}
		Properties properties = System.getProperties();
		String info = properties.getProperty("user.name") + properties.getProperty("os.version");
		// System.out.println(info);
		try {
			if (!dbConnect.checkProgram(info)) {
				JOptionPane.showMessageDialog(null, "提示：软件试用期已到", "信息提示", JOptionPane.INFORMATION_MESSAGE);
				System.exit(1);
			}
		} catch (Exception et) {
			et.printStackTrace();
		}
		loadingDialogJFrame = new LoadingDialogJFrame("载入二维码...");
		System.setProperty("jsse.enableSNIExtension", "false"); // 设置该属性，避免出现unrecognized_name异常
		System.setProperty("https.protocols", "TLSv1"); // 设置协议为TLSv1，与服务器对应，避免出现与服务器断连的异常
		// System.setProperty("javax.net.debug","ssl");

		final Main htmlUnit = new Main();
		try {
			String s;
			out: while (true) {
				s = htmlUnit.produceUuid();

				// uuid获取失败
				if (s == null) {
					// System.out.println("uuid获取失败！");
					continue;
				}

				htmlUnit.showPic(s);
				loadingDialogJFrame.dispose();
				int tip = 1;
				url = "https://login.wx.qq.com/cgi-bin/mmwebwx-bin/login?uuid=" + uuid + "&tip=" + tip + "&_="
						+ timeStamp;
				while (true) {
					s = htmlUnit.sendPostRequest(url, null);

					// 响应为空
					if (StringKit.isBlank(s)) {
						// System.out.println("扫码失败！");
						continue;
					}

					// 扫码成功
					if (s.contains("201")) {
						// String s2[]=s.split("'");
						// userAvatar =s2[1];
						tip = 0;
						windowUI.getMainFrame().setTitle("微信机器人--扫码成功，等待用户确认登录");
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
						loadingDialogJFrame.setLoadingText("正在重新载入二维码...");
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
			windowUI.getjPanel().setLayout(new FlowLayout(FlowLayout.LEFT, 15, 20));
			windowUI.getjPanel().add(windowUI.getjPanel_1());
			windowUI.getjPanel().add(windowUI.getjPanel_2());
			windowUI.getjPanel().add(windowUI.getjPanel_9());
			windowUI.getjPanel().add(windowUI.getjPanel_3());
			// windowUI.getjPanel().add(windowUI.getjPanel_4());
			windowUI.getjPanel().add(windowUI.getjPanel_5());
			windowUI.getjPanel().add(windowUI.getjPanel_6());
			windowUI.getjPanel().add(windowUI.getjPanel_7());
			windowUI.getjPanel().add(windowUI.getjPanel_10());
			windowUI.getjPanel().add(windowUI.getjPanel_11());
			windowUI.getjPanel().add(windowUI.getjPanel_12());
			windowUI.getjPanel().add(windowUI.getjPanel_8());
			windowUI.getjPanel().add(windowUI.getjPanel_4());
			windowUI.getjPanel().add(windowUI.getjPanel_13());
			windowUI.getjPanel().add(windowUI.getjPanel_14());
			windowUI.getGb().gridx = 0;
			windowUI.getGb().gridy = 1;
			windowUI.getGb().weightx = 4;
			windowUI.getGb().ipady = 500;
			windowUI.getMainFrame().add(windowUI.getjPanel(), windowUI.getGb());
			windowUI.getMainFrame().setTitle("微信机器人");
			windowUI.getMainFrame().setVisible(true);
			loadingDialogJFrame.dispose();
			windowUI.getActiveDegreeRecord().addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					if (!isLogin) {
						JOptionPane.showMessageDialog(null, "请您先登录", "信息提示", JOptionPane.INFORMATION_MESSAGE);
					} else {
						try {
							loadingDialogJFrame.setLoadingText("正在读取活跃度记录......");
							File record = new File(userName_robot + "_活跃度记录.csv");
							if (record.exists())
								record.delete(); // 确保活跃度记录文件永远是全新的文件
							record.createNewFile();

							FileOutputStream out = new FileOutputStream(record);
							OutputStreamWriter writer;
							String encoding = System.getProperty("file.encoding");
							byte[] bom = new byte[3];
							writer = new OutputStreamWriter(out);
							if (encoding.equalsIgnoreCase("UTF-8")) {
								// 添加utf-8的bom头，避免office乱码
								bom[0] = (byte) 0xEF;
								bom[1] = (byte) 0xBB;
								bom[2] = (byte) 0xBF;
								writer.write(new String(bom));
							}
							writer.write("群名,群成员,活跃度,敏感词警告数\r\n");

							String sql = "SELECT  * FROM " + userName_robot + "_info WHERE activeDegree>0";
							ResultSet rs = dbConnect.getStatement().executeQuery(sql);
							loadingDialogJFrame.setLoadingText("读取完成，正在载入活跃度记录......");
							String groupNumberId, memberUin, groupName, memberName;
							GroupInfo targetGroup;
							UserInfo targetMember;
							while (rs.next()) {
								targetGroup = null;
								groupNumberId = rs.getString("groupId");
								groupName = rs.getString("groupName");
								memberUin = rs.getString("memberUin");
								memberName = rs.getString("memberName");
								for (GroupInfo groupInfo : groupInfoList)
									if (groupInfo.getGroupNumberId().equals(groupNumberId)) {
										targetGroup = groupInfo;
										break;
									}
								if (targetGroup == null) // 如果没有该群聊的信息
									writer.write(groupName + "," + memberName);
								else {
									writer.write(targetGroup.getGroupName() + ",");
									targetMember = null;
									for (UserInfo userInfo : targetGroup.getGroup().values())
										if (userInfo.getUin().equals(memberUin)) {
											targetMember = userInfo;
											break;
										}
									writer.write(targetMember == null ? memberName : targetMember.getRemarkName());
								}
								writer.write("," + rs.getInt("activeDegree") + "," + rs.getInt("warn") + "\r\n");
							}
							writer.flush();
							writer.close();
							out.close();
							Runtime.getRuntime().exec("cmd /c start " + userName_robot + "_活跃度记录.csv");
							loadingDialogJFrame.dispose();
						} catch (Exception e1) {
							loadingDialogJFrame.dispose();
							JOptionPane.showMessageDialog(null, "活跃度记录读取/载入失败！", "错误", JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			});
			windowUI.getSignRecord().addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (!isLogin) {
						JOptionPane.showMessageDialog(null, "请您先登录", "信息提示", JOptionPane.INFORMATION_MESSAGE);
					} else {
						try {
							loadingDialogJFrame.setLoadingText("正在读取签到记录......");
							File record = new File(userName_robot + "_群签到记录.csv");
							if (record.exists())
								record.delete(); // 确保签到记录永远是全新的文件
							record.createNewFile();

							FileOutputStream out = new FileOutputStream(record);
							OutputStreamWriter writer;
							String encoding = System.getProperty("file.encoding");
							byte[] bom = new byte[3];
							writer = new OutputStreamWriter(out);
							if (encoding.equalsIgnoreCase("UTF-8")) {
								// 添加utf-8的bom头，避免office乱码
								bom[0] = (byte) 0xEF;
								bom[1] = (byte) 0xBB;
								bom[2] = (byte) 0xBF;
								writer.write(new String(bom));
							}
							writer.write("签到日期,群名,群成员\r\n");

							String sql = "SELECT  * FROM " + userName_robot + "_info WHERE is_sign=1";
							ResultSet rs = dbConnect.getStatement().executeQuery(sql);
							loadingDialogJFrame.setLoadingText("读取完成，正在载入签到记录......");
							GroupInfo targetGroupInfo;
							UserInfo targetMember;
							String groupNumberId, memberUin, groupName, memberName;
							while (rs.next()) {
								writer.write(df.format(rs.getTimestamp("signDate")) + ",");
								targetGroupInfo = null;
								groupNumberId = rs.getString("groupId");
								memberUin = rs.getString("memberUin");
								groupName = rs.getString("groupName");
								memberName = rs.getString("memberName");
								for (GroupInfo groupInfo : groupInfoList)
									if (groupInfo.getGroupNumberId().equals(groupNumberId)) {
										targetGroupInfo = groupInfo;
										break;
									}
								if (targetGroupInfo == null) // 如果没有该群聊信息
									writer.write(groupName + "," + memberName);
								else {
									writer.write(targetGroupInfo.getGroupName() + ",");
									targetMember = null;
									for (UserInfo userInfo : targetGroupInfo.getGroup().values())
										if (userInfo.getUin().equals(memberUin)) {
											targetMember = userInfo;
											break;
										}
									writer.write(targetMember == null ? memberName : targetMember.getRemarkName());
								}
								writer.write("\r\n");
							}
							writer.flush();
							writer.close();
							out.close();
							Runtime.getRuntime().exec("cmd /c start " + userName_robot + "_群签到记录.csv");
							loadingDialogJFrame.dispose();
						} catch (Exception e1) {
							loadingDialogJFrame.dispose();
							JOptionPane.showMessageDialog(null, "签到记录读取/载入失败！", "错误", JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			});
			windowUI.getLogin().addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (!windowUI.getLoginFrame().isVisible())
						windowUI.getLoginFrame().setVisible(true);
				}
			});
			windowUI.getLog().addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						String s = new String(windowUI.getUserPasswdArea().getPassword());
						userName_robot = windowUI.getUserNameArea().getText().trim();
						if (dbConnect.checkLogin(userName_robot, s.trim())) {
							loadingDialogJFrame.setSuccessText("登陆成功！");
							isLogin = true;
							windowUI.getLogin().setEnabled(false);
							windowUI.getLogin().setBackground(Color.GRAY);
							windowUI.getLoginFrame().setVisible(false);
						} else {
							JOptionPane.showMessageDialog(null, "用户不存在或密码错误！", "错误", JOptionPane.ERROR_MESSAGE);
							windowUI.getUserPasswdArea().setText("");
							windowUI.getUserNameArea().setText("");
						}
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(null, "登录异常！", "错误", JOptionPane.ERROR_MESSAGE);
						windowUI.getUserPasswdArea().setText("");
						windowUI.getUserNameArea().setText("");
					}
				}
			});
			windowUI.getRegister().addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String userName = windowUI.getUserNameArea().getText();
					char[] passwdChars = windowUI.getUserPasswdArea().getPassword();
					String passwd;
					if (userName == null || userName.equals(""))
						JOptionPane.showMessageDialog(null, "用户名不能为空！", "错误", JOptionPane.ERROR_MESSAGE);
					else if (passwdChars == null || (passwd = new String(passwdChars)).length() < 8)
						JOptionPane.showMessageDialog(null, "密码长度要大于等于8！", "错误", JOptionPane.ERROR_MESSAGE);
					else {
						try {
							dbConnect.insertRegisterRecord(userName, passwd);
							userName_robot = windowUI.getUserNameArea().getText().trim();
							loadingDialogJFrame.setSuccessText("恭喜：您已注册成功！");
							isLogin = true;
							windowUI.getLogin().setBackground(Color.GRAY);
							windowUI.getLogin().setEnabled(false);
							windowUI.getLoginFrame().setVisible(false);
						} catch (Exception e1) {
							JOptionPane.showMessageDialog(null, "错误：用户名已存在!", "错误", JOptionPane.ERROR_MESSAGE);
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
					try {
						htmlUnit.configInfo();
						htmlUnit.readFiles();
						htmlUnit.getRecentList();
						htmlUnit.getGroupList();
						loadingDialogJFrame.setSuccessText("数据同步成功！");
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(null, "数据同步失败！", "错误", JOptionPane.ERROR_MESSAGE);
					}
				}
			});
			windowUI.getModifyRemarkName().addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					windowUI.getModifyRemarkName().setEnabled(false);
					htmlUnit.getGroupList();
					group.clear();
					for (GroupInfo groupInfo : groupInfoList)
						group.add(groupInfo.getGroupName());
					windowUI.setjList5(new JList(group));
					windowUI.setjList6(new JList());
					windowUI.getjList5().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					windowUI.getjList6().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					windowUI.setModifyScrollPane1(new JScrollPane(windowUI.getjList5()));
					windowUI.setModifyScrollPane2(new JScrollPane(windowUI.getjList6()));
					windowUI.getModifyScrollPane1().setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
					windowUI.getModifyScrollPane1().setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
					windowUI.getModifyScrollPane2().setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
					windowUI.getModifyScrollPane2().setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
					windowUI.getSetRemarkNamePanel().removeAll();
					GridBagConstraints gb = new GridBagConstraints();
					windowUI.getGb().gridy = GridBagConstraints.NONE;
					windowUI.getGb().gridx = GridBagConstraints.NONE;
					windowUI.getGb().gridheight = GridBagConstraints.BOTH;
					windowUI.getGb().gridwidth = GridBagConstraints.BOTH;
//					windowUI.getGb().ipady = 0;
//					windowUI.getGb().ipadx = 355;
//					windowUI.getGb().gridx = 0;
//					windowUI.getGb().gridy = 0;
//					windowUI.getGb().gridx = 1;
					windowUI.getGb().ipady = 450;
					windowUI.getGb().ipadx = 330;
					windowUI.getGb().gridx = 0;
					windowUI.getGb().gridy = 1;
					windowUI.getSetRemarkNamePanel().add(windowUI.getModifyScrollPane1(),windowUI.getGb());
					windowUI.getGb().gridx = 1;
					windowUI.getSetRemarkNamePanel().add(windowUI.getModifyScrollPane2(),windowUI.getGb());
					windowUI.getModifyScrollPane1().setBorder(BorderFactory.createTitledBorder("群昵称"));
					windowUI.getModifyScrollPane2().setBorder(BorderFactory.createTitledBorder("好友昵称"));
					windowUI.getRemarkNameFrame().setVisible(true);
					windowUI.getjList5().addListSelectionListener(new ListSelectionListener() {
						@Override
						public void valueChanged(ListSelectionEvent e) {
							modifyMemberList.clear();
							modifyUserIndex = 0;
                             for(GroupInfo groupInfo:groupInfoList){
                             	if(groupInfo.getGroupName().equals(windowUI.getjList5().getSelectedValue().toString())){
                             		modifyGroup = groupInfo;
                             		for(UserInfo userInfo:groupInfo.getGroup().values()){
                             			modifyMemberList.add(userInfo.getRemarkName());
									}
									break;
								}
							 }
							 windowUI.getjList6().setListData(modifyMemberList);
							 windowUI.getModifyScrollPane2().repaint();
						}
					});
					windowUI.getjList6().addListSelectionListener(new ListSelectionListener() {
						@Override
						public void valueChanged(ListSelectionEvent e) {
							modifyUserIndex = windowUI.getjList6().getSelectedIndex();
							for(UserInfo userInfo:modifyGroup.getGroup().values()){
								if(modifyMemberList.get(modifyUserIndex).equals(userInfo.getRemarkName())) {
									modifyMember=userInfo;
									break;
								}
							}

						}
					});
				}
			});
			windowUI.getModifyName().addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if(!StringKit.isNotBlank(windowUI.getRemarkName().getText())){
						JOptionPane.showMessageDialog(null,"错误信息：备注名不能为空","提示信息",JOptionPane.ERROR_MESSAGE);
					}
					else if(htmlUnit.modifyRemarkName(modifyMember.getUserId(),windowUI.getRemarkName().getText())){
						modifyMember.setRemarkName(windowUI.getRemarkName().getText());
						modifyMemberList.set(modifyUserIndex,windowUI.getRemarkName().getText());
						loadingDialogJFrame.setSuccessText("提示：备注修改成功！");
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
					htmlUnit.getGroupList();
					// 对于群名修改面板的组件改变
					JPanel groupListJPanel = new JPanel(); // 群名列表
					groupListJPanel.setLayout(new BoxLayout(groupListJPanel, BoxLayout.Y_AXIS));
					JScrollPane groupListScrollPane = new JScrollPane(groupListJPanel);
					groupListScrollPane.setUI(new BEScrollPaneUI());
					groupListScrollPane.setBorder(BorderFactory.createTitledBorder("群聊列表"));
					final JPanel groupMemberListJPanel = new JPanel(); // 群成员列表
					groupMemberListJPanel.setLayout(new BoxLayout(groupMemberListJPanel, BoxLayout.Y_AXIS));
					JScrollPane groupMemberListScrollPane = new JScrollPane(groupMemberListJPanel);
					groupMemberListScrollPane.setUI(new BEScrollPaneUI());
					groupMemberListScrollPane.setBorder(BorderFactory.createTitledBorder("群成员列表"));
					windowUI.getAcrossGroupListJPanel().add(groupListScrollPane);
					windowUI.getAcrossGroupListJPanel().add(groupMemberListScrollPane);
					GridBagConstraints gb = new GridBagConstraints();
					gb.gridx = 0;
					gb.ipadx = 0;
					gb.fill = GridBagConstraints.VERTICAL;
					for (final GroupInfo group : groupInfoList) {
						final JLabel jLabel = new JLabel(group.getGroupName());
						jLabel.setBorder(BorderFactory.createTitledBorder("原群名"));
						final JTextField jTextArea = new JTextField(20);
						jTextArea.setDocument(new CustomJTextFieldDocument(32));
						jTextArea.setBorder(BorderFactory.createTitledBorder("新群名"));
						JButton jButton = new JButton("修改群名");
						jButton.setForeground(Color.white);
						jButton.setUI(new BEButtonUI().setNormalColor(BEButtonUI.NormalColor.red));
						JPanel aSetGroupNamePanel = new JPanel(new GridLayout(0, 3, 20, 20));
						aSetGroupNamePanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
						aSetGroupNamePanel.add(jLabel);
						aSetGroupNamePanel.add(jTextArea);
						aSetGroupNamePanel.add(jButton);
						windowUI.getSetGroupNamePanel().add(aSetGroupNamePanel,gb);
						jButton.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								String newGroupName = jTextArea.getText();
								if (newGroupName != null && !newGroupName.equals("")) {
									htmlUnit.modifyChatroomName(group.getGroupID(), newGroupName);
									jLabel.setText(newGroupName);
									group.setGroupName(newGroupName);
									jTextArea.setText("");
									loadingDialogJFrame.setSuccessText("群名修改成功！");
								} else
									JOptionPane.showMessageDialog(new Frame(), "错误：新群名一栏不能为空", "错误",
											JOptionPane.ERROR_MESSAGE);
							}
						});
						// 添加跨群设置列表
						final JCheckBox groupCheckBox = new JCheckBox(group.getGroupName()+"("+group.getGroupNumberId()+")");
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
									if (groupInfo.getGroupName().equals(groupCheckBox.getText().split("\\(")[0])
											&& groupInfo.getAcrossGroupFlag()) {
										for (final UserInfo userInfo : groupInfo.getGroup().values()) {
											final JCheckBox memberCheckBox = new JCheckBox(userInfo.getRemarkName()+"("+userInfo.getUin()+")");
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
					windowUI.getOnlineTimeHour().setSelectedItem(Integer.parseInt(onlineTime.split(":")[0]));
					windowUI.getOnlineTimeMinute().setSelectedItem(Integer.parseInt(onlineTime.split(":")[1]));
					windowUI.getOfflineTimeHour().setSelectedItem(Integer.parseInt(offlineTime.split(":")[0]));
					windowUI.getOfflineTimeMinute().setSelectedItem(Integer.parseInt(offlineTime.split(":")[1]));
					windowUI.getMinWarnCount().setText(String.valueOf(minSenseWarn));
					windowUI.getMaxWarnCount().setText(String.valueOf(maxSenseWarn));
					if(apiKey.equals("49d5dd04005a4d82b7d5bc30dae96821"))
						windowUI.getTulingKeyArea().setText("");
					else
					windowUI.getTulingKeyArea().setText(apiKey);
				}
			});

			windowUI.getInviteIntoGroup().addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					windowUI.getInviteIntoGroup().setEnabled(false);
					htmlUnit.getGroupList();
					group.clear();
					for (GroupInfo groupInfo : groupInfoList)
						group.add(groupInfo.getGroupName());
					windowUI.setjList2(new JList(group));
					invitedGroup = null;
					windowUI.setjList1(new JList());
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
					windowUI.getGb().ipady = 0;
					windowUI.getGb().ipadx = 355;
					windowUI.getGb().gridx = 0;
					windowUI.getGb().gridy = 0;
					// windowUI.getInvitePanel().add(windowUI.getSearchInviteUser(),windowUI.getGb());
					windowUI.getGb().gridx = 1;
					// windowUI.getInvitePanel().add(windowUI.getSearchInviteGroup(),windowUI.getGb());
					windowUI.getGb().ipady = 450;
					windowUI.getGb().ipadx = 330;
					windowUI.getGb().gridx = 0;
					windowUI.getGb().gridy = 1;
					windowUI.getInvitePanel().add(windowUI.getInviteScrollPane1(), windowUI.getGb());
					windowUI.getGb().gridx = 1;
					windowUI.getInvitePanel().add(windowUI.getInviteScrollPane2(), windowUI.getGb());
					windowUI.getInviteScrollPane1().setBorder(BorderFactory.createTitledBorder("好友昵称"));
					windowUI.getInviteScrollPane2().setBorder(BorderFactory.createTitledBorder("群名称"));
					windowUI.getGroupInvite().setVisible(true);
					windowUI.getjList1().addListSelectionListener(new ListSelectionListener() {
						@Override
						public void valueChanged(ListSelectionEvent e) {
							inviteFriendList.clear();
							int inviteUserIndex[] = windowUI.getjList1().getSelectedIndices();
							for (int i = 0; i < inviteUserIndex.length; i++)
								for (UserInfo userInfo : userInfoList.values()) {
									if (searchInviteFriend.get(inviteUserIndex[i]).equals(userInfo.getRemarkName())) {
										inviteFriendList.add(userInfo);
										break;
									}
								}

						}
					});
					windowUI.getjList2().addListSelectionListener(new ListSelectionListener() {
						@Override
						public void valueChanged(ListSelectionEvent e) {
							inviteFriendName.clear();
							for (UserInfo userInfo : userInfoList.values())
								inviteFriendName.add(userInfo.getRemarkName());
							for (GroupInfo groupInfo : groupInfoList) {
								if (groupInfo.getGroupName()
										.equals(windowUI.getjList2().getSelectedValue().toString())) {
									invitedGroup = groupInfo;
									for (UserInfo userInfo : groupInfo.getGroup().values())
										inviteFriendName.remove(userInfo.getRemarkName());
									break;
								}
							}
							searchInviteFriend = (Vector<String>) inviteFriendName.clone();
							windowUI.getjList1().setListData(searchInviteFriend);
							windowUI.getInviteScrollPane1().repaint();
						}
					});
				}
			});
			windowUI.getSearchInviteUser().getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void insertUpdate(DocumentEvent e) {
					if (!windowUI.getSearchInviteUser().getText().equals(""))
						searchAndOrder(windowUI.getSearchInviteUser().getText(), inviteFriendName, searchInviteFriend);
					else
						searchInviteFriend = (Vector<String>) inviteFriendName.clone();
					windowUI.getjList1().setListData(searchInviteFriend);
					windowUI.getInviteScrollPane1().repaint();
				}

				@Override
				public void removeUpdate(DocumentEvent e) {
					if (!windowUI.getSearchInviteUser().getText().equals(""))
						searchAndOrder(windowUI.getSearchInviteUser().getText(), inviteFriendName, searchInviteFriend);
					else
						searchInviteFriend = (Vector<String>) inviteFriendName.clone();
					windowUI.getjList1().setListData(searchInviteFriend);
					windowUI.getInviteScrollPane1().repaint();
				}

				@Override
				public void changedUpdate(DocumentEvent e) {
					if (!windowUI.getSearchInviteUser().getText().equals(""))
						searchAndOrder(windowUI.getSearchInviteUser().getText(), inviteFriendName, searchInviteFriend);
					else
						searchInviteFriend = (Vector<String>) inviteFriendName.clone();
					windowUI.getjList1().setListData(searchInviteFriend);
					windowUI.getInviteScrollPane1().repaint();
				}
			});
			windowUI.getSearchInviteGroup().getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void insertUpdate(DocumentEvent e) {
					if (!windowUI.getSearchInviteGroup().getText().equals(""))
						htmlUnit.searchAndOrder(windowUI.getSearchInviteGroup().getText(), group, searchInvitedGroup);
					else
						searchInvitedGroup = (Vector<String>) group.clone();
					windowUI.getjList2().setListData(searchInvitedGroup);
					windowUI.getInviteScrollPane2().repaint();
				}

				@Override
				public void removeUpdate(DocumentEvent e) {
					if (!windowUI.getSearchInviteGroup().getText().equals(""))
						htmlUnit.searchAndOrder(windowUI.getSearchInviteGroup().getText(), group, searchInvitedGroup);
					else
						searchInvitedGroup = (Vector<String>) group.clone();
					windowUI.getjList2().setListData(searchInvitedGroup);
					windowUI.getInviteScrollPane2().repaint();
				}

				@Override
				public void changedUpdate(DocumentEvent e) {
					if (!windowUI.getSearchInviteGroup().getText().equals(""))
						htmlUnit.searchAndOrder(windowUI.getSearchInviteGroup().getText(), group, searchInvitedGroup);
					else
						searchInvitedGroup = (Vector<String>) group.clone();
					windowUI.getjList2().setListData(searchInvitedGroup);
					windowUI.getInviteScrollPane2().repaint();
				}
			});
			windowUI.getInvite().addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					if (invitedGroup != null && inviteFriendList.size() != 0) {
						String s = "";
						for (int i = 0; i < inviteFriendList.size(); i++)
							s += inviteFriendList.get(i).getUserId() + ",";
						if (!htmlUnit.addMember(s.substring(0, s.length() - 1), invitedGroup.getGroupID())) {
							JOptionPane.showMessageDialog(null, "邀请好友进群失败！", "失败", JOptionPane.ERROR_MESSAGE);
							return;
						}
						for (UserInfo userInfo : inviteFriendList) {
							searchInviteFriend.remove(userInfo.getRemarkName());
							invitedGroup.getGroup().put(userInfo.getUserId(),userInfo);
						}
						windowUI.getSearchInviteGroup().setText("");
						windowUI.getSearchInviteUser().setText("");
						windowUI.getjList1().setListData(searchInviteFriend);
						windowUI.getInviteScrollPane1().repaint();
						loadingDialogJFrame.setSuccessText("邀请好友进群成功！");
					}
				}
			});

			windowUI.getRemoveFromGroup().addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					windowUI.getRemoveFromGroup().setEnabled(false);
					htmlUnit.getGroupList();
					group.clear();
					for (GroupInfo groupInfo : groupInfoList)
						group.add(groupInfo.getGroupName());
					windowUI.setjList3(new JList(group));
					removeGroup = null;
					windowUI.setjList4(new JList());
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
					windowUI.getGb().ipady = 0;
					windowUI.getGb().ipadx = 355;
					windowUI.getGb().gridx = 0;
					windowUI.getGb().gridy = 0;
					// windowUI.getRemovePanel().add(windowUI.getSearchRemoveGroup(),windowUI.getGb());
					windowUI.getGb().gridx = 1;
					// windowUI.getRemovePanel().add(windowUI.getSearchRemoveUser(),windowUI.getGb());
					windowUI.getGb().ipady = 450;
					windowUI.getGb().ipadx = 330;
					windowUI.getGb().gridx = 0;
					windowUI.getGb().gridy = 1;
					windowUI.getRemovePanel().add(windowUI.getRemoveScrollPane1(), windowUI.getGb());
					windowUI.getGb().gridx = 1;
					windowUI.getRemovePanel().add(windowUI.getRemoveScrollPane2(), windowUI.getGb());
					windowUI.getRemoveScrollPane1().setBorder(BorderFactory.createTitledBorder("群名称"));
					windowUI.getRemoveScrollPane2().setBorder(BorderFactory.createTitledBorder("群成员"));
					windowUI.getGroupRemove().setVisible(true);

					windowUI.getjList3().addListSelectionListener(new ListSelectionListener() {
						@Override
						public void valueChanged(ListSelectionEvent e) {
							removeFriendName.clear();
							for (GroupInfo groupInfo : groupInfoList)
								if (groupInfo.getGroupName()
										.equals(windowUI.getjList3().getSelectedValue().toString())) {
									removeGroup = groupInfo;
									for (UserInfo userInfo : groupInfo.getGroup().values())
										removeFriendName.add(userInfo.getRemarkName());
									break;
								}
							searchRemoveFriend = (Vector<String>) removeFriendName.clone();
							windowUI.getjList4().setListData(searchRemoveFriend);
							windowUI.getRemoveScrollPane2().repaint();
						}
					});
					windowUI.getjList4().addListSelectionListener(new ListSelectionListener() {
						@Override
						public void valueChanged(ListSelectionEvent e) {
							removeFriendList.clear();
							int removeUserIndex[] = windowUI.getjList4().getSelectedIndices();
							for (int i = 0; i < removeUserIndex.length; i++)
								for (UserInfo userInfo : removeGroup.getGroup().values())
									if (searchRemoveFriend.get(removeUserIndex[i]).equals(userInfo.getRemarkName())) {
										removeFriendList.add(userInfo);
										break;
									}
						}
					});
					windowUI.getSearchRemoveUser().getDocument().addDocumentListener(new DocumentListener() {
						@Override
						public void insertUpdate(DocumentEvent e) {
							if (!windowUI.getSearchRemoveUser().getText().equals(""))
								htmlUnit.searchAndOrder(windowUI.getSearchRemoveUser().getText(), removeFriendName,
										searchRemoveFriend);

							else
								searchRemoveFriend = (Vector<String>) removeFriendName.clone();
							windowUI.getjList4().setListData(searchRemoveFriend);
							windowUI.getRemoveScrollPane2().repaint();

						}

						@Override
						public void removeUpdate(DocumentEvent e) {
							if (!windowUI.getSearchRemoveUser().getText().equals(""))
								htmlUnit.searchAndOrder(windowUI.getSearchRemoveUser().getText(), removeFriendName,
										searchRemoveFriend);

							else
								searchRemoveFriend = (Vector<String>) removeFriendName.clone();
							windowUI.getjList4().setListData(searchRemoveFriend);
							windowUI.getRemoveScrollPane2().repaint();
						}

						@Override
						public void changedUpdate(DocumentEvent e) {
							if (!windowUI.getSearchRemoveUser().getText().equals(""))
								htmlUnit.searchAndOrder(windowUI.getSearchRemoveUser().getText(), removeFriendName,
										searchRemoveFriend);

							else
								searchRemoveFriend = (Vector<String>) removeFriendName.clone();
							windowUI.getjList4().setListData(searchRemoveFriend);
							windowUI.getRemoveScrollPane2().repaint();
						}
					});
				}
			});
			windowUI.getSearchRemoveGroup().getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void insertUpdate(DocumentEvent e) {
					if (!windowUI.getSearchRemoveGroup().getText().equals(""))
						htmlUnit.searchAndOrder(windowUI.getSearchRemoveGroup().getText(), group, searchRemoveGroup);
					else
						searchRemoveGroup = (Vector<String>) group.clone();
					windowUI.getjList3().setListData(searchRemoveGroup);
					windowUI.getRemoveScrollPane2().repaint();
				}

				@Override
				public void removeUpdate(DocumentEvent e) {
					if (!windowUI.getSearchRemoveGroup().getText().equals(""))
						htmlUnit.searchAndOrder(windowUI.getSearchRemoveGroup().getText(), group, searchRemoveGroup);
					else
						searchRemoveGroup = (Vector<String>) group.clone();
					windowUI.getjList3().setListData(searchRemoveGroup);
					windowUI.getRemoveScrollPane2().repaint();
				}

				@Override
				public void changedUpdate(DocumentEvent e) {
					if (!windowUI.getSearchRemoveGroup().getText().equals(""))
						htmlUnit.searchAndOrder(windowUI.getSearchRemoveGroup().getText(), group, searchRemoveGroup);
					else
						searchRemoveGroup = (Vector<String>) group.clone();
					windowUI.getjList3().setListData(searchRemoveGroup);
					windowUI.getRemoveScrollPane2().repaint();
				}
			});
			windowUI.getRemove().addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (removeGroup != null && removeFriendList.size() != 0) {
						String s = "";
						for (int i = 0; i < removeFriendList.size(); i++)
							s += removeFriendList.get(i).getUserId() + ",";
						if (htmlUnit.deleteMember(s.substring(0, s.length() - 1), removeGroup.getGroupID())) {
							for (UserInfo userInfo : removeFriendList) {
								searchRemoveFriend.remove(userInfo.getRemarkName());
								removeGroup.getGroup().remove(userInfo.getUserId());
							}
							windowUI.getSearchRemoveGroup().setText("");
							windowUI.getSearchRemoveUser().setText("");
							windowUI.getjList4().setListData(searchRemoveFriend);
							windowUI.getRemoveScrollPane2().repaint();
							loadingDialogJFrame.setSuccessText("群成员踢出成功！");
						} else
							JOptionPane.showMessageDialog(null, "提示：踢出群成员失败！您不是群主", "错误", JOptionPane.ERROR_MESSAGE);
					}
				}
			});

			windowUI.getChatJButton().addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					windowUI.getChatIn().setVisible(true);
					windowUI.getChatJButton().setEnabled(false);
				}

			});

			windowUI.getAddFriend().addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
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
						windowUI.getReply().setIcon(new ImageIcon(WindowUI.class.getResource("resource/reply.png")));
						windowUI.getReply().repaint();
					}
				}
			});

			windowUI.getAtModeBtn().addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					// if(atModeFlag){
					// atModeFlag = false;
					// windowUI.getAtModeBtn().setIcon(windowUI.getAtModeBtn().getDisabledIcon());
					// windowUI.getAtModeBtn().repaint();
					// }else{
					// atModeFlag = true;
					// windowUI.getAtModeBtn().setIcon(new
					// ImageIcon(WindowUI.class.getResource("resource/auto_chat.png")));
					// windowUI.getAtModeBtn().repaint();
					// }
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
					htmlUnit.getGroupList();
					windowUI.getGroupNamePeriodArea().removeAllItems();
					windowUI.getGroupNameArea().removeAllItems();
					for (GroupInfo groupInfo : groupInfoList) {
						windowUI.getGroupNamePeriodArea().addItem(groupInfo.getGroupName());
						windowUI.getGroupNameArea().addItem(groupInfo.getGroupName());
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
					String time = windowUI.getTimeArea().getText();
					if (StringKit.isBlank(time)) {
						JOptionPane.showMessageDialog(null, "时间不能为空！", "错误", JOptionPane.ERROR_MESSAGE);
						return;
					}
					Pattern pattern = Pattern.compile("([01]?[0-9]|2[0-3]):[0-5][0-9]");
					Matcher matcher = pattern.matcher(time);
					if (!matcher.matches()) {
						JOptionPane.showMessageDialog(null, "时间格式错误！", "错误", JOptionPane.ERROR_MESSAGE);
						return;
					}
					String content = windowUI.getPropertyTimeArea().getText();
					if (StringKit.isBlank(content)) {
						JOptionPane.showMessageDialog(null, "内容不能为空！", "错误", JOptionPane.ERROR_MESSAGE);
						return;
					}
					TipRecord tipRecord = null;
					try {
						tipRecord = new TipRecord(windowUI.getDf().format(windowUI.getDf().parse(time)), 0,
								windowUI.getGroupNameArea().getSelectedItem().toString(), content);
					} catch (ParseException e1) {
						JOptionPane.showMessageDialog(null, "时间格式错误！", "错误", JOptionPane.ERROR_MESSAGE);
					}
					tipRecord.setTimeStamp(df.format(new Date()).split(" ")[0]);
					windowUI.addTimeTip(tipRecord);
					tipRecordList.add(tipRecord);
				}
			});
			windowUI.getAddTipPeriodButton().addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String time = windowUI.getPeriodStartTime().getText();
					if (StringKit.isBlank(time)) {
						JOptionPane.showMessageDialog(null, "时间不能为空！", "错误", JOptionPane.ERROR_MESSAGE);
						return;
					}
					Pattern pattern = Pattern.compile("([01]?[0-9]|2[0-3]):[0-5][0-9]");
					Matcher matcher = pattern.matcher(time);
					if (!matcher.matches()) {
						JOptionPane.showMessageDialog(null, "时间格式错误！", "错误", JOptionPane.ERROR_MESSAGE);
						return;
					}
					String periodText = windowUI.getPeriodTime().getText();
					int period = StringKit.isBlank(periodText) ? 0 : Integer.parseInt(periodText);
					if (period > 24) { // 超过24小时没有实现，这里等同于24小时以内的情况，所以直接作为异常处理
						JOptionPane.showMessageDialog(null, "间隔时间不能超过24小时！", "错误", JOptionPane.ERROR_MESSAGE);
						return;
					}
					String content = windowUI.getPropertyPeriodArea().getText();
					if (StringKit.isBlank(content)) {
						JOptionPane.showMessageDialog(null, "内容不能为空！", "错误", JOptionPane.ERROR_MESSAGE);
						return;
					}
					TipRecord tipRecord = null;
					try {
						tipRecord = new TipRecord(windowUI.getDf().format(windowUI.getDf().parse(time)), period,
								windowUI.getGroupNamePeriodArea().getSelectedItem().toString(), content);
					} catch (ParseException e1) {
						JOptionPane.showMessageDialog(null, "时间格式错误！", "错误", JOptionPane.ERROR_MESSAGE);
					}
					windowUI.addPeriodTip(tipRecord);
					tipRecordList.add(tipRecord);
					int hour = Integer.parseInt(tipRecord.getTime().split(":")[0]);
					String minute =tipRecord.getTime().split(":")[1];
					for(;hour<24;hour=hour+period){
						String newTime = String.valueOf(hour)+":"+minute;
						TipRecord tipRecord1 = new TipRecord(newTime,period,windowUI.getGroupNamePeriodArea().getSelectedItem().toString(),content);
						tipRecordList.add(tipRecord1);
					}
				}
			});
			windowUI.getShowGroupList().addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					windowUI.getShowGroupList().setEnabled(false);
					windowUI.getShowGroupPanel().removeAll();
					htmlUnit.getGroupList();
					for (final GroupInfo groupInfo : groupInfoList) {
						final JCheckBox jc = new JCheckBox(groupInfo.getGroupName()+"("+groupInfo.getGroupNumberId().split("@")[0]+")");
						jc.setSelected(groupInfo.getFlag());
						jc.setBackground(windowUI.getShowGroupPanel().getBackground());
						windowUI.getShowGroupPanel().add(jc);
						jc.addChangeListener(new ChangeListener() {
							@Override
							public void stateChanged(ChangeEvent e) {
								groupInfo.setFlag(jc.isSelected());
							}
						});
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
						JOptionPane.showMessageDialog(null, "读取聊天记录失败！", "错误", JOptionPane.ERROR_MESSAGE);
					}

				}
			});
			windowUI.getModifyWarnCount().addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String minCount = windowUI.getMinWarnCount().getText();
					String maxCount = windowUI.getMaxWarnCount().getText();
					if (StringKit.isAnyBlank(minCount, maxCount)) {
						JOptionPane.showMessageDialog(null, "上（下）限不能为空！", "错误", JOptionPane.ERROR_MESSAGE);
						return;
					}
					minSenseWarn = Integer.parseInt(minCount);
					maxSenseWarn = Integer.parseInt(maxCount);
					if (maxSenseWarn < minSenseWarn) {
						JOptionPane.showMessageDialog(null, "下限不能小于上限！", "错误", JOptionPane.ERROR_MESSAGE);
						return;
					}
					loadingDialogJFrame.setSuccessText("敏感词上下限修改成功！");
				}
			});
			windowUI.getModifyTulingKey().addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (StringKit.isNotBlank(windowUI.getTulingKeyArea().getText())) {
						if (windowUI.getTulingKeyArea().getText().length() != 32) {
							JOptionPane.showMessageDialog(null, "错误：图灵机器人API密钥应该由32位小写字母及0到9数字组成");
							windowUI.getTulingKeyArea().setText("");
						} else {
							apiKey = windowUI.getTulingKeyArea().getText();
							loadingDialogJFrame.setSuccessText("图灵key已经成功修改！");
						}
					}
				}
			});
			windowUI.getModifyOnlineTime().addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					onlineTime = windowUI.getOnlineTimeHour().getSelectedItem().toString()+":"+windowUI.getOnlineTimeMinute().getSelectedItem().toString();
					offlineTime = windowUI.getOfflineTimeHour().getSelectedItem().toString()+":"+windowUI.getOfflineTimeMinute().getSelectedItem().toString();
					try{
						Date offline = windowUI.getDf().parse(offlineTime);
						Date online = windowUI.getDf().parse(onlineTime);
						if(offline.before(online)) {
							JOptionPane.showMessageDialog(null, "错误：下线时间设置要在上线时间设置之前", "信息提示", JOptionPane.ERROR_MESSAGE);
							onlineTime="8:0";
							offlineTime = "20:0";
							windowUI.getOnlineTimeHour().setSelectedItem(8);
							windowUI.getOnlineTimeMinute().setSelectedItem(0);
							windowUI.getOfflineTimeHour().setSelectedItem(20);
							windowUI.getOfflineTimeMinute().setSelectedItem(0);
						}
						else {
							loadingDialogJFrame.setSuccessText("上下线时间修改成功");
						}
					}catch (Exception e1){
						e1.printStackTrace();
					}

				}
			});

			windowUI.getSaveRecord().addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						File record = new File(userName_wx + "_群聊天记录.csv");
						if (!record.exists())
							record.createNewFile();
						record.setWritable(true); // 打开文件的写入权限

						FileOutputStream out = new FileOutputStream(record, true);
						OutputStreamWriter writer;
						String encoding = System.getProperty("file.encoding");
						byte[] bom = new byte[3];

						if (record.length() == 0) {
							writer = new OutputStreamWriter(out);
							if (encoding.equalsIgnoreCase("UTF-8")) {
								// 添加utf-8的bom头，避免office乱码
								bom[0] = (byte) 0xEF;
								bom[1] = (byte) 0xBB;
								bom[2] = (byte) 0xBF;
								writer.write(new String(bom));
							}
							writer.write("时间,群名/发起人,群成员/接收人,内容\r\n");
						} else {
							InputStream reader = new FileInputStream(record);
							reader.read(bom, 0, bom.length);
							switch (bom[0]) {
								case (byte) 0xEF:
									encoding = "UTF-8";
									break;
								case (byte) 0xFE:
									encoding = "UTF-16BE";
									break;
								case (byte) 0xFF:
									encoding = "UTF-16LE";
									break;
								default:
									encoding = "GBK";
									break;
							}
							reader.close();
							writer = new OutputStreamWriter(out, encoding);
						}
						for (String r : recordList)
							writer.write((r + "\r\n"));
						writer.flush();
						writer.close();
						out.close();
						record.setWritable(false); // 关闭文件的写入权限，禁止外部程序修改文件

						loadingDialogJFrame.setSuccessText("记录已保存到当前目录下，请查看！");
						recordList.clear();
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(null, "记录保存失败，请检查原因后重试！", "错误", JOptionPane.ERROR_MESSAGE);
					}
				}
			});
			windowUI.getPublicWord().addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						Runtime.getRuntime().exec("notepad.exe " + wxuin + "/公开.txt");
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(null, "读取公共关键词文件失败！", "错误", JOptionPane.ERROR_MESSAGE);
					}
				}
			});

			windowUI.getPrivateWord().addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						Runtime.getRuntime().exec("notepad.exe " + wxuin + "/私密.txt");
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(null, "读取私密关键词文件失败！", "错误", JOptionPane.ERROR_MESSAGE);
					}
				}
			});

			windowUI.getSenseWord().addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						Runtime.getRuntime().exec("notepad.exe " + wxuin + "/敏感词.txt");
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(null, "读取敏感词文件失败！", "错误", JOptionPane.ERROR_MESSAGE);
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
			windowUI.getRemarkNameFrame().addWindowListener(new WindowListener() {
				@Override
				public void windowOpened(WindowEvent e) {

				}

				@Override
				public void windowClosing(WindowEvent e) {
                      windowUI.getModifyRemarkName().setEnabled(true);
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
			loadingDialogJFrame.shutdown("与微信服务器连接超时，请检查网络后重启");
		}
	}
}
