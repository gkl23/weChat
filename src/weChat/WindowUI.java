package weChat;

import Models.TipRecord;
import org.jb2011.lnf.beautyeye.BeautyEyeLNFHelper;
import org.jb2011.lnf.beautyeye.ch3_button.BEButtonUI;

import javax.swing.*;
import javax.swing.plaf.TabbedPaneUI;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by huzhejie on 2016/8/5.
 */
public class WindowUI {

	/*
	 * 所有按钮图标资源名常量
	 */
	public static final String CHAT_ICON_NAME = "/resource/chat.png";
	public static final String ADD_FRIEND_ICON_NAME = "/resource/add_friend.png";
	public static final String SIGN_RECORD_ICON_NAME = "/resource/sign_record.png";
	public static final String AUTO_CHAT_ICON_NAME = "/resource/auto_chat.png";
	public static final String REPLY_ICON_NAME = "/resource/reply.png";
	public static final String WARN_ICON_NAME = "/resource/warn.png";
	public static final String LOCAL_WORD_NAME = "/resource/local_word.png";
	public static final String SET_ICON_NAME = "/resource/set.png";
	public static final String SEND_BY_TIME_ICON_NAME = "/resource/send_by_time.png";

	/*
	 * 各功能界面
	 */
	private JFrame mainFrame = null;
	private JFrame chatIn = null;
	private JFrame dailyTip = null;
	private JFrame wordModify = null;
	private JFrame chooseGroup = null;
	private JFrame groupInvite = null;
	private JFrame groupRemove = null;
	private JFrame setFrame = null;

	private JButton chatJButton = new JButton(new ImageIcon(WindowUI.class.getResource(CHAT_ICON_NAME)));
	private JButton addFriend = new JButton(new ImageIcon(WindowUI.class.getResource(ADD_FRIEND_ICON_NAME)));
	private JButton signRecord = new JButton(new ImageIcon(WindowUI.class.getResource(SIGN_RECORD_ICON_NAME)));
	private JButton autoChat = new JButton(new ImageIcon(WindowUI.class.getResource(AUTO_CHAT_ICON_NAME)));
	private JButton reply = new JButton(new ImageIcon(WindowUI.class.getResource(REPLY_ICON_NAME)));
	private JButton warn = new JButton(new ImageIcon(WindowUI.class.getResource(WARN_ICON_NAME)));
	private JButton localWord = new JButton(new ImageIcon(WindowUI.class.getResource(LOCAL_WORD_NAME)));
	private JButton set = new JButton(new ImageIcon(WindowUI.class.getResource(SET_ICON_NAME)));
	private JButton showGroupList = new JButton(new ImageIcon(WindowUI.class.getResource(CHAT_ICON_NAME)));
	private JButton inviteIntoGroup = new JButton(new ImageIcon(WindowUI.class.getResource(REPLY_ICON_NAME)));
	private JButton removeFromGroup = new JButton(new ImageIcon(WindowUI.class.getResource(WARN_ICON_NAME)));
	private JButton sendByTime = new JButton(new ImageIcon(WindowUI.class.getResource(SEND_BY_TIME_ICON_NAME)));
	private JButton publicWord = new JButton(new ImageIcon(WindowUI.class.getResource(SEND_BY_TIME_ICON_NAME)));
	private JButton privateWord = new JButton(new ImageIcon(WindowUI.class.getResource(SEND_BY_TIME_ICON_NAME)));
	private JButton senseWord = new JButton(new ImageIcon(WindowUI.class.getResource(SEND_BY_TIME_ICON_NAME)));
	private JPanel userInfoJPanel = new JPanel();
	private JPanel jPanel = new JPanel();
	private JPanel chatJPanel = new JPanel();
	private JPanel tipPanel = new JPanel();
	private JPanel addTipPanel = new JPanel();
	private JPanel wordPanel = new JPanel();
	private JPanel showGroupPanel = new JPanel();
	private JPanel invitePanel = new JPanel();
	private JPanel removePanel = new JPanel();
	private JTabbedPane setPanel = new JTabbedPane();
	private JPanel setGroupNamePanel = new JPanel();
	private JPanel setTuLingAPIPanel = new JPanel();
	private JScrollPane jScrollPane;
	private JScrollPane tipJScrollPane;
	private JScrollPane setGroupNameJScrollPane;
	private JScrollPane inviteScrollPane1;
	private JScrollPane inviteScrollPane2;
	private JScrollPane removeScrollPane1;
	private JScrollPane removeScrollPane2;
	private JLabel userNameLabel = new JLabel();// 存放用户名字
	private JLabel userHeaderImg = new JLabel();// 存放用户头像
	private JLabel jLabel_0 = new JLabel();// 存放二维码
	private JPanel jPanel_1 = new JPanel();// 存放检查群消息按钮
	private JPanel jPanel_2 = new JPanel();// 好友自动通过
	private JPanel jPanel_3 = new JPanel();// 存放查看签到记录的按钮
	private JPanel jPanel_4 = new JPanel();// 存放智能聊天的按钮
	private JPanel jPanel_5 = new JPanel();// 存放自动回复功能的按钮
	private JPanel jPanel_6 = new JPanel();// 存放敏感警告功能的按钮
	private JPanel jPanel_7 = new JPanel();// 存放本地词库功能的按钮
	private JPanel jPanel_8 = new JPanel();// 存放设置功能的按钮
	private JPanel jPanel_9 = new JPanel();// 存放消息定时发送功能的按钮
	private JPanel jPanel_10 = new JPanel();// 存放显示群列表的功能的按钮
	private JPanel jPanel_11 = new JPanel();// 存放邀请进群
	private JPanel jPanel_12 = new JPanel();// 存放T人出群
	private JPanel publicPanel = new JPanel();
	private JPanel privatePanel = new JPanel();
	private JPanel sensePanel = new JPanel();
	private JList jList1 = null;
	private JList jList2 = null;
	private JList jList3 = null;
	private JList jList4 = null;
	private JTextPane jTextPane = new JTextPane();
	private JTextArea timeArea = new JTextArea();
	private JTextArea propertyArea = new JTextArea();
	private JComboBox groupNameArea = new JComboBox();// 定时发送中选择群名的选择框
	private JButton addTipButton = new JButton("添加定时消息发送");
	private JButton saveRecord = new JButton("保存消息到本地");
	private JButton seeRecord = new JButton("查看本地消息");
	private JButton remove = new JButton("踢出该群");
	private JButton invite = new JButton("邀请进群");
	private SimpleAttributeSet attributeSet = new SimpleAttributeSet();
	private GridBagConstraints gb = new GridBagConstraints();
	private SimpleDateFormat df = new SimpleDateFormat("HH:mm");
	private Vector<String> friend = new Vector<>();
	private Vector<String> friendID = new Vector<>();

	/**
	 * 初始化窗口及组件
	 */
	public WindowUI() {
		try {
			BeautyEyeLNFHelper.frameBorderStyle = BeautyEyeLNFHelper.FrameBorderStyle.translucencyAppleLike;
			BeautyEyeLNFHelper.launchBeautyEyeLNF();
			UIManager.put("RootPane.setupButtonVisible", false);
			mainFrame = new JFrame("微信机器人");
			chatIn = new JFrame("微信机器人--消息接收");
			dailyTip = new JFrame("微信机器人--定时消息提醒");
			wordModify = new JFrame("微信机器人--修改词库");
			chooseGroup = new JFrame("微信机器人--群列表");
			groupInvite = new JFrame("微信机器人--邀请进群");
			groupRemove = new JFrame("微信机器人--踢人出群");
			setFrame = new JFrame("设置");

			mainFrame.setLocation(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - 200,
					Toolkit.getDefaultToolkit().getScreenSize().height / 2 - 300);
			mainFrame.setVisible(false);
			mainFrame.setResizable(false);
			mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			mainFrame.setLayout(new GridBagLayout());
			gb.gridx = GridBagConstraints.RELATIVE;
			gb.gridy = GridBagConstraints.RELATIVE;
			gb.ipadx = 0;
			gb.ipady = -30;
			mainFrame.add(jPanel, gb);

			chatIn.setLocation(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - 300,
					Toolkit.getDefaultToolkit().getScreenSize().height / 2 - 200);
			chatIn.setVisible(false);
			chatIn.setResizable(false);
			chatIn.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			chatIn.setLayout(new GridBagLayout());
			gb.gridx = GridBagConstraints.RELATIVE;
			gb.gridy = GridBagConstraints.RELATIVE;
			chatIn.setSize(600, 600);

			dailyTip.setLocation(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - 500,
					Toolkit.getDefaultToolkit().getScreenSize().height / 2 - 200);
			dailyTip.setVisible(false);
			dailyTip.setResizable(false);
			dailyTip.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			dailyTip.setLayout(new GridLayout());
			dailyTip.setSize(1000, 400);
			tipJScrollPane = new JScrollPane(tipPanel);
			tipPanel.setLayout(new BoxLayout(tipPanel, BoxLayout.Y_AXIS));
			dailyTip.add(tipJScrollPane);

			wordModify.setLocation(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - 200,
					Toolkit.getDefaultToolkit().getScreenSize().height / 2 - 100);
			wordModify.setVisible(false);
			wordModify.setResizable(false);
			wordModify.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			wordModify.setSize(400, 200);
			wordModify.add(wordPanel);

			chooseGroup.setLocation(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - 100,
					Toolkit.getDefaultToolkit().getScreenSize().height / 2 - 300);
			chooseGroup.setVisible(false);
			chooseGroup.setResizable(false);
			chooseGroup.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			chooseGroup.add(showGroupPanel);

			groupInvite.setLocation(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - 400,
					Toolkit.getDefaultToolkit().getScreenSize().height / 2 - 300);
			groupInvite.setVisible(false);
			groupInvite.setResizable(false);
			groupInvite.setLayout(new BorderLayout());
			groupInvite.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			groupInvite.setSize(800, 700);
			groupInvite.add(BorderLayout.CENTER, invitePanel);
			groupInvite.add(BorderLayout.SOUTH, invite);

			groupRemove.setLocation(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - 400,
					Toolkit.getDefaultToolkit().getScreenSize().height / 2 - 300);
			groupRemove.setVisible(false);
			groupRemove.setResizable(false);
			groupRemove.setLayout(new BorderLayout());
			groupRemove.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			groupRemove.setSize(800, 700);
			groupRemove.add(BorderLayout.CENTER, removePanel);
			groupRemove.add(BorderLayout.SOUTH, remove);

			setFrame.setLocation(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - 400,
					Toolkit.getDefaultToolkit().getScreenSize().height / 2 - 300);
			setFrame.setVisible(false);
			setFrame.setResizable(false);
			setFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			setFrame.setSize(800, 700);
			setFrame.add(setPanel);

			// 主界面按钮的UI设置
			userInfoJPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 50, 15));
			userInfoJPanel.setBackground(Color.lightGray);
			userInfoJPanel.setBorder(null);
			chatJButton.setBorder(null);
			chatJButton.setFocusPainted(false);
			addFriend.setBorder(null);
			addFriend.setFocusPainted(false);
			signRecord.setBorder(null);
			signRecord.setFocusPainted(false);
			autoChat.setBorder(null);
			autoChat.setFocusPainted(false);
			localWord.setBorder(null);
			localWord.setFocusPainted(false);
			reply.setBorder(null);
			reply.setFocusPainted(false);
			set.setBorder(null);
			set.setFocusPainted(false);
			warn.setBorder(null);
			warn.setFocusPainted(false);
			sendByTime.setBorder(null);
			sendByTime.setFocusPainted(false);
			publicWord.setBorder(null);
			publicWord.setFocusPainted(false);
			privateWord.setBorder(null);
			privateWord.setFocusPainted(false);
			senseWord.setBorder(null);
			senseWord.setFocusPainted(false);
			showGroupList.setBorder(null);
			showGroupList.setFocusPainted(false);
			inviteIntoGroup.setBorder(null);
			inviteIntoGroup.setFocusPainted(false);
			removeFromGroup.setBorder(null);
			removeFromGroup.setFocusPainted(false);
			// 主界面的组件设置
			jPanel.setLayout(new GridLayout());
			jPanel_1.setLayout(new GridLayout(2, 1));
			jPanel_1.add(chatJButton);
			JLabel j1 = new JLabel("消息盒子");
			j1.setFont(new Font("黑体", 1, 16));
			jPanel_1.add(j1);
			jPanel_2.setBorder(null);
			jPanel_2.setLayout(new GridLayout(2, 1));
			jPanel_2.add(addFriend);
			JLabel j2 = new JLabel("自动通过");
			j2.setFont(new Font("黑体", 1, 16));
			jPanel_2.add(j2);
			jPanel_3.setBorder(null);
			jPanel_3.setLayout(new GridLayout(2, 1));
			jPanel_3.add(signRecord);
			JLabel j3 = new JLabel("签到记录");
			j3.setFont(new Font("黑体", 1, 16));
			jPanel_3.add(j3);
			jPanel_4.setBorder(null);
			jPanel_4.setLayout(new GridLayout(2, 1));
			jPanel_4.add(autoChat);
			JLabel j4 = new JLabel("智能聊天");
			j4.setFont(new Font("黑体", 1, 16));
			jPanel_4.add(j4);
			jPanel_5.setBorder(null);
			jPanel_5.setLayout(new GridLayout(2, 1));
			jPanel_5.add(reply);
			JLabel j5 = new JLabel("自动回复");
			j5.setFont(new Font("黑体", 1, 16));
			jPanel_5.add(j5);
			jPanel_6.setBorder(null);
			jPanel_6.setLayout(new GridLayout(2, 1));
			jPanel_6.add(warn);
			JLabel j6 = new JLabel("敏感警告");
			j6.setFont(new Font("黑体", 1, 16));
			jPanel_6.add(j6);
			jPanel_7.setBorder(null);
			jPanel_7.setLayout(new GridLayout(2, 1));
			jPanel_7.add(localWord);
			JLabel j7 = new JLabel("本地词库");
			j7.setFont(new Font("黑体", 1, 16));
			jPanel_7.add(j7);
			jPanel_8.setBorder(null);
			jPanel_8.setLayout(new GridLayout(2, 1));
			jPanel_8.add(set);
			JLabel j8 = new JLabel("设    置");
			j8.setFont(new Font("黑体", 1, 16));
			jPanel_8.add(j8);
			jPanel_9.setBorder(null);
			jPanel_9.setLayout(new GridLayout(2, 1));
			jPanel_9.add(sendByTime);
			JLabel j9 = new JLabel("定时发送");
			j9.setFont(new Font("黑体", 1, 16));
			jPanel_9.add(j9);
			jPanel_10.setBorder(null);
			jPanel_10.setLayout(new GridLayout(2, 1));
			jPanel_10.add(showGroupList);
			JLabel j10 = new JLabel("群组列表");
			j10.setFont(new Font("黑体", 1, 16));
			jPanel_10.add(j10);
			jPanel_11.setBorder(null);
			jPanel_11.setLayout(new GridLayout(2, 1));
			jPanel_11.add(inviteIntoGroup);
			JLabel j11 = new JLabel("邀请进群");
			j11.setFont(new Font("黑体", 1, 16));
			jPanel_11.add(j11);
			jPanel_12.setBorder(null);
			jPanel_12.setLayout(new GridLayout(2, 1));
			jPanel_12.add(removeFromGroup);
			JLabel j12 = new JLabel("踢人出群");
			j12.setFont(new Font("黑体", 1, 16));
			jPanel_12.add(j12);

			// 消息盒子的组件设置
			saveRecord.setUI(new BEButtonUI().setNormalColor(BEButtonUI.NormalColor.red));
			seeRecord.setUI(new BEButtonUI().setNormalColor(BEButtonUI.NormalColor.green));
			saveRecord.setForeground(Color.white);
			seeRecord.setForeground(Color.white);
			chatJPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 50, 50));
			chatJPanel.add(saveRecord);
			chatJPanel.add(seeRecord);
			jTextPane.setFocusable(true);
			jTextPane.setEditable(false);
			jScrollPane = new JScrollPane(jTextPane);
			StyleConstants.setFontSize(attributeSet, 16);
			jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			gb.gridx = 0;
			gb.ipady = 500;
			gb.ipadx = 520;
			gb.gridy = GridBagConstraints.RELATIVE;
			gb.gridwidth = GridBagConstraints.REMAINDER;
			chatIn.add(jScrollPane, gb);
			gb.ipady = 100;
			gb.ipadx = 300;
			gb.gridheight = 3;
			chatIn.add(chatJPanel, gb);

			// 定时发布的组件设置
			addTipPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 20));
			timeArea.setSize(100, 100);
			timeArea.setLineWrap(true);
			propertyArea.setSize(200, 100);
			propertyArea.setLineWrap(true);
			groupNameArea.setSize(100, 100);
			JLabel time = new JLabel("时间：");
			JLabel groupName = new JLabel("群名：");
			JLabel property = new JLabel("内容：");
			addTipPanel.add(time);
			addTipPanel.add(timeArea);
			addTipPanel.add(groupName);
			addTipPanel.add(groupNameArea);
			addTipPanel.add(property);
			addTipPanel.add(propertyArea);
			addTipPanel.add(addTipButton);
			tipJScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			tipJScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			addTipButton.setSize(0, 100);
			addTipButton.setUI(new BEButtonUI().setNormalColor(BEButtonUI.NormalColor.green));
			tipPanel.add(addTipPanel);

			wordPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 30));
			publicPanel.setLayout(new GridLayout(2, 1));
			JLabel publicLabel = new JLabel("公开词库");
			publicLabel.setFont(new Font("黑体", 1, 16));
			publicPanel.add(publicWord);
			publicPanel.add(publicLabel);
			JLabel privateLabel = new JLabel("私密词库");
			privateLabel.setFont(new Font("黑体", 1, 16));
			privatePanel.setLayout(new GridLayout(2, 1));
			privatePanel.add(privateWord);
			privatePanel.add(privateLabel);
			JLabel senseLabel = new JLabel("敏感词库");
			senseLabel.setFont(new Font("黑体", 1, 16));
			sensePanel.setLayout(new GridLayout(2, 1));
			sensePanel.add(senseWord);
			sensePanel.add(senseLabel);
			wordPanel.add(publicPanel);
			wordPanel.add(privatePanel);
			wordPanel.add(sensePanel);

			showGroupPanel.setLayout(new GridLayout(0, 1, 20, 20));
			showGroupPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

			invitePanel.setLayout(new GridLayout(0, 2, 20, 20));
			invitePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
			invite.setUI(new BEButtonUI().setNormalColor(BEButtonUI.NormalColor.green));
			invite.setForeground(Color.white);

			removePanel.setLayout(new GridLayout(0, 2, 20, 20));
			removePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
			remove.setUI(new BEButtonUI().setNormalColor(BEButtonUI.NormalColor.green));
			remove.setForeground(Color.white);
			jList4 = new JList();

			setPanel.add("修改群名", setGroupNamePanel);
			setGroupNamePanel.setLayout(new GridLayout(0, 3, 20, 0));
			setGroupNamePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
			setGroupNameJScrollPane = new JScrollPane(setGroupNamePanel);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 添加群名修改的组件
	 *
	 * @param oldName
	 *
	 * @param newName
	 **/
	public void addGroupNameModify(String oldName, String newName) {

	}

	/**
	 * 添加定时记录的组件
	 *
	 * @param tipRecord
	 *
	 **/
	public void addTip(final TipRecord tipRecord) {
		JButton deleteButton = new JButton("删除");
		deleteButton.setUI(new BEButtonUI().setNormalColor(BEButtonUI.NormalColor.red));
		JLabel record = new JLabel();
		final JPanel totalRecord = new JPanel();
		totalRecord.setLayout(new FlowLayout(FlowLayout.CENTER, 50, 20));
		try {
			record.setText("时间：" + df.format(df.parse(timeArea.getText())) + "   群："
					+ groupNameArea.getSelectedItem().toString() + "   内容: " + propertyArea.getText());
			totalRecord.add(record);
			totalRecord.add(deleteButton);
			deleteButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Iterator iterator = Main.getTipRecordList().iterator();
					while (iterator.hasNext()) {
						TipRecord tipRecord1 = (TipRecord) iterator.next();
						if (tipRecord.equals(tipRecord1)) {
							iterator.remove();
						}
					}
					tipPanel.remove(totalRecord);
					dailyTip.validate();
					dailyTip.repaint();
				}
			});
			tipPanel.add(totalRecord);
			dailyTip.validate();
			dailyTip.repaint();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "错误：输入时间格式不正确", "错误", JOptionPane.ERROR_MESSAGE);
		}

	}

	public SimpleAttributeSet getAttributeSet() {
		return attributeSet;
	}

	public void setAttributeSet(SimpleAttributeSet attributeSet) {
		this.attributeSet = attributeSet;
	}

	public JPanel getjPanel_3() {
		return jPanel_3;
	}

	public void setjPanel_3(JPanel jPanel_3) {
		this.jPanel_3 = jPanel_3;
	}

	public JFrame getF() {
		return mainFrame;
	}

	public void setF(JFrame f) {
		this.mainFrame = f;
	}

	public JFrame getChatIn() {
		return chatIn;
	}

	public void setChatIn(JFrame chatIn) {
		this.chatIn = chatIn;
	}

	public JButton getChatJButton() {
		return chatJButton;
	}

	public void setChatJButton(JButton chatJButton) {
		this.chatJButton = chatJButton;
	}

	public JButton getCheckJButton() {
		return addFriend;
	}

	public void setCheckJButton(JButton checkJButton) {
		this.addFriend = checkJButton;
	}

	public JPanel getjPanel() {
		return jPanel;
	}

	public void setjPanel(JPanel jPanel) {
		this.jPanel = jPanel;
	}

	public JPanel getChatJPanel() {
		return chatJPanel;
	}

	public void setChatJPanel(JPanel chatJPanel) {
		this.chatJPanel = chatJPanel;
	}

	public JScrollPane getjScrollPane() {
		return jScrollPane;
	}

	public void setjScrollPane(JScrollPane jScrollPane) {
		this.jScrollPane = jScrollPane;
	}

	public JLabel getjLabel_0() {
		return jLabel_0;
	}

	public void setjLabel_0(JLabel jLabel_0) {
		this.jLabel_0 = jLabel_0;
	}

	public JPanel getjPanel_1() {
		return jPanel_1;
	}

	public void setjPanel_1(JPanel jPanel_1) {
		this.jPanel_1 = jPanel_1;
	}

	public JPanel getjPanel_2() {
		return jPanel_2;
	}

	public void setjPanel_2(JPanel jPanel_2) {
		this.jPanel_2 = jPanel_2;
	}

	public JTextPane getjTextPane() {
		return jTextPane;
	}

	public void setjTextPane(JTextPane jTextPane) {
		this.jTextPane = jTextPane;
	}

	public JButton getThirdJButton() {
		return signRecord;
	}

	public void setThirdJButton(JButton thirdJButton) {
		this.signRecord = thirdJButton;
	}

	public JFrame getDailyTip() {
		return dailyTip;
	}

	public void setDailyTip(JFrame dailyTip) {
		this.dailyTip = dailyTip;
	}

	public JPanel getTipPanel() {
		return tipPanel;
	}

	public void setTipPanel(JPanel tipPanel) {
		this.tipPanel = tipPanel;
	}

	public JScrollPane getTipJScrollPane() {
		return tipJScrollPane;
	}

	public void setTipJScrollPane(JScrollPane tipJScrollPane) {
		this.tipJScrollPane = tipJScrollPane;
	}

	public JTextArea getTimeArea() {
		return timeArea;
	}

	public void setTimeArea(JTextArea timeArea) {
		this.timeArea = timeArea;
	}

	public JTextArea getPropertyArea() {
		return propertyArea;
	}

	public void setPropertyArea(JTextArea propertyArea) {
		this.propertyArea = propertyArea;
	}

	public JButton getAddTipButton() {
		return addTipButton;
	}

	public void setAddTipButton(JButton addTipButton) {
		this.addTipButton = addTipButton;
	}

	public JPanel getAddTipPanel() {
		return addTipPanel;
	}

	public void setAddTipPanel(JPanel addTipPanel) {
		this.addTipPanel = addTipPanel;
	}

	public SimpleDateFormat getDf() {
		return df;
	}

	public void setDf(SimpleDateFormat df) {
		this.df = df;
	}

	public JComboBox getGroupNameArea() {
		return groupNameArea;
	}

	public void setGroupNameArea(JComboBox groupNameArea) {
		this.groupNameArea = groupNameArea;
	}

	public JButton getAddFriend() {
		return addFriend;
	}

	public void setAddFriend(JButton addFriend) {
		this.addFriend = addFriend;
	}

	public JButton getSignRecord() {
		return signRecord;
	}

	public void setSignRecord(JButton signRecord) {
		this.signRecord = signRecord;
	}

	public JButton getAutoChat() {
		return autoChat;
	}

	public void setAutoChat(JButton autoChat) {
		this.autoChat = autoChat;
	}

	public JButton getReply() {
		return reply;
	}

	public void setReply(JButton reply) {
		this.reply = reply;
	}

	public JButton getWarn() {
		return warn;
	}

	public void setWarn(JButton warn) {
		this.warn = warn;
	}

	public JButton getLocalWord() {
		return localWord;
	}

	public void setLocalWord(JButton localWord) {
		this.localWord = localWord;
	}

	public JButton getSet() {
		return set;
	}

	public void setSet(JButton set) {
		this.set = set;
	}

	public JPanel getjPanel_4() {
		return jPanel_4;
	}

	public void setjPanel_4(JPanel jPanel_4) {
		this.jPanel_4 = jPanel_4;
	}

	public JPanel getjPanel_5() {
		return jPanel_5;
	}

	public void setjPanel_5(JPanel jPanel_5) {
		this.jPanel_5 = jPanel_5;
	}

	public JPanel getjPanel_6() {
		return jPanel_6;
	}

	public void setjPanel_6(JPanel jPanel_6) {
		this.jPanel_6 = jPanel_6;
	}

	public JPanel getjPanel_7() {
		return jPanel_7;
	}

	public void setjPanel_7(JPanel jPanel_7) {
		this.jPanel_7 = jPanel_7;
	}

	public JPanel getjPanel_8() {
		return jPanel_8;
	}

	public void setjPanel_8(JPanel jPanel_8) {
		this.jPanel_8 = jPanel_8;
	}

	public JButton getSendByTime() {
		return sendByTime;
	}

	public void setSendByTime(JButton sendByTime) {
		this.sendByTime = sendByTime;
	}

	public JPanel getjPanel_9() {
		return jPanel_9;
	}

	public void setjPanel_9(JPanel jPanel_9) {
		this.jPanel_9 = jPanel_9;
	}

	public JLabel getUserNameLabel() {
		return userNameLabel;
	}

	public void setUserNameLabel(JLabel userNameLabel) {
		this.userNameLabel = userNameLabel;
	}

	public JPanel getUserInfoJPanel() {
		return userInfoJPanel;
	}

	public void setUserInfoJPanel(JPanel userInfoJPanel) {
		this.userInfoJPanel = userInfoJPanel;
	}

	public JLabel getUserHeaderImg() {
		return userHeaderImg;
	}

	public void setUserHeaderImg(JLabel userHeaderImg) {
		this.userHeaderImg = userHeaderImg;
	}

	public GridBagConstraints getGb() {
		return gb;
	}

	public void setGb(GridBagConstraints gb) {
		this.gb = gb;
	}

	public JFrame getWordModify() {
		return wordModify;
	}

	public void setWordModify(JFrame wordModify) {
		this.wordModify = wordModify;
	}

	public JButton getPublicWord() {
		return publicWord;
	}

	public void setPublicWord(JButton publicWord) {
		this.publicWord = publicWord;
	}

	public JButton getPrivateWord() {
		return privateWord;
	}

	public void setPrivateWord(JButton privateWord) {
		this.privateWord = privateWord;
	}

	public JButton getSenseWord() {
		return senseWord;
	}

	public void setSenseWord(JButton senseWord) {
		this.senseWord = senseWord;
	}

	public JPanel getWordPanel() {
		return wordPanel;
	}

	public void setWordPanel(JPanel wordPanel) {
		this.wordPanel = wordPanel;
	}

	public JPanel getPublicPanel() {
		return publicPanel;
	}

	public void setPublicPanel(JPanel publicPanel) {
		this.publicPanel = publicPanel;
	}

	public JPanel getPrivatePanel() {
		return privatePanel;
	}

	public void setPrivatePanel(JPanel privatePanel) {
		this.privatePanel = privatePanel;
	}

	public JPanel getSensePanel() {
		return sensePanel;
	}

	public void setSensePanel(JPanel sensePanel) {
		this.sensePanel = sensePanel;
	}

	public JButton getSaveRecord() {
		return saveRecord;
	}

	public void setSaveRecord(JButton saveRecord) {
		this.saveRecord = saveRecord;
	}

	public JButton getSeeRecord() {
		return seeRecord;
	}

	public void setSeeRecord(JButton seeRecord) {
		this.seeRecord = seeRecord;
	}

	public JFrame getChooseGroup() {
		return chooseGroup;
	}

	public void setChooseGroup(JFrame chooseGroup) {
		this.chooseGroup = chooseGroup;
	}

	public JButton getShowGroupList() {
		return showGroupList;
	}

	public void setShowGroupList(JButton showGroupList) {
		this.showGroupList = showGroupList;
	}

	public JPanel getShowGroupPanel() {
		return showGroupPanel;
	}

	public void setShowGroupPanel(JPanel showGroupPanel) {
		this.showGroupPanel = showGroupPanel;
	}

	public JPanel getjPanel_10() {
		return jPanel_10;
	}

	public void setjPanel_10(JPanel jPanel_10) {
		this.jPanel_10 = jPanel_10;
	}

	public JFrame getGroupInvite() {
		return groupInvite;
	}

	public void setGroupInvite(JFrame groupInvite) {
		this.groupInvite = groupInvite;
	}

	public JButton getInviteIntoGroup() {
		return inviteIntoGroup;
	}

	public void setInviteIntoGroup(JButton inviteIntoGroup) {
		this.inviteIntoGroup = inviteIntoGroup;
	}

	public JPanel getInvitePanel() {
		return invitePanel;
	}

	public void setInvitePanel(JPanel invitePanel) {
		this.invitePanel = invitePanel;
	}

	public JPanel getjPanel_11() {
		return jPanel_11;
	}

	public void setjPanel_11(JPanel jPanel_11) {
		this.jPanel_11 = jPanel_11;
	}

	public JList getjList1() {
		return jList1;
	}

	public void setjList1(JList jList1) {
		this.jList1 = jList1;
	}

	public JList getjList2() {
		return jList2;
	}

	public void setjList2(JList jList2) {
		this.jList2 = jList2;
	}

	public JButton getRemove() {
		return remove;
	}

	public void setRemove(JButton remove) {
		this.remove = remove;
	}

	public JButton getInvite() {
		return invite;
	}

	public void setInvite(JButton invite) {
		this.invite = invite;
	}

	public JScrollPane getInviteScrollPane1() {
		return inviteScrollPane1;
	}

	public void setInviteScrollPane1(JScrollPane inviteScrollPane1) {
		this.inviteScrollPane1 = inviteScrollPane1;
	}

	public JScrollPane getInviteScrollPane2() {
		return inviteScrollPane2;
	}

	public void setInviteScrollPane2(JScrollPane inviteScrollPane2) {
		this.inviteScrollPane2 = inviteScrollPane2;
	}

	public JFrame getGroupRemove() {
		return groupRemove;
	}

	public void setGroupRemove(JFrame groupRemove) {
		this.groupRemove = groupRemove;
	}

	public JButton getRemoveFromGroup() {
		return removeFromGroup;
	}

	public void setRemoveFromGroup(JButton removeFromGroup) {
		this.removeFromGroup = removeFromGroup;
	}

	public JPanel getRemovePanel() {
		return removePanel;
	}

	public void setRemovePanel(JPanel removePanel) {
		this.removePanel = removePanel;
	}

	public JScrollPane getRemoveScrollPane1() {
		return removeScrollPane1;
	}

	public void setRemoveScrollPane1(JScrollPane removeScrollPane1) {
		this.removeScrollPane1 = removeScrollPane1;
	}

	public JScrollPane getRemoveScrollPane2() {
		return removeScrollPane2;
	}

	public void setRemoveScrollPane2(JScrollPane removeScrollPane2) {
		this.removeScrollPane2 = removeScrollPane2;
	}

	public JPanel getjPanel_12() {
		return jPanel_12;
	}

	public void setjPanel_12(JPanel jPanel_12) {
		this.jPanel_12 = jPanel_12;
	}

	public JList getjList3() {
		return jList3;
	}

	public void setjList3(JList jList3) {
		this.jList3 = jList3;
	}

	public JList getjList4() {
		return jList4;
	}

	public void setjList4(JList jList4) {
		this.jList4 = jList4;
	}

	public Vector<String> getFriend() {
		return friend;
	}

	public void setFriend(Vector<String> friend) {
		this.friend = friend;
	}

	public Vector<String> getFriendID() {
		return friendID;
	}

	public void setFriendID(Vector<String> friendID) {
		this.friendID = friendID;
	}

	public JFrame getSetFrame() {
		return setFrame;
	}

	public void setSetFrame(JFrame setFrame) {
		this.setFrame = setFrame;
	}

	public JTabbedPane getSetPanel() {
		return setPanel;
	}

	public void setSetPanel(JTabbedPane setPanel) {
		this.setPanel = setPanel;
	}

	public JPanel getSetGroupNamePanel() {
		return setGroupNamePanel;
	}

	public void setSetGroupNamePanel(JPanel setGroupNamePanel) {
		this.setGroupNamePanel = setGroupNamePanel;
	}

	public JPanel getSetTuLingAPIPanel() {
		return setTuLingAPIPanel;
	}

	public void setSetTuLingAPIPanel(JPanel setTuLingAPIPanel) {
		this.setTuLingAPIPanel = setTuLingAPIPanel;
	}

	public JScrollPane getSetGroupNameJScrollPane() {
		return setGroupNameJScrollPane;
	}

	public void setSetGroupNameJScrollPane(JScrollPane setGroupNameJScrollPane) {
		this.setGroupNameJScrollPane = setGroupNameJScrollPane;
	}
}
