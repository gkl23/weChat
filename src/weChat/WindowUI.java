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
    /**
     * 界面和按钮的元素.依次为
     * 窗口
     * 主功能按钮
     * 主面板
     * 滚动条
     * 存放主功能按钮的小面板
     * T人和邀请人的选择列表
     */

    private static JFrame mainFrame = null;
    private static JFrame chatIn = null;
    private static JFrame dailyTip = null;
    private static JFrame wordModify = null;
    private static JFrame chooseGroup = null;
    private static JFrame groupInvite = null;
    private static JFrame groupRemove = null;
    private static JFrame setFrame = null;
    private static JButton chatJButton = new JButton(new ImageIcon(WindowUI.class.getResource("resource/chat.png")));
    private static JButton addFriend = new JButton(new ImageIcon(WindowUI.class.getResource("resource/add_friend.png")));
    private static JButton signRecord = new JButton(new ImageIcon(WindowUI.class.getResource("resource/sign_record.png")));
    private static JButton autoChat = new JButton(new ImageIcon(WindowUI.class.getResource("resource/auto_chat.png")));
    private static JButton reply = new JButton(new ImageIcon(WindowUI.class.getResource("resource/reply.png")));
    private static JButton warn = new JButton(new ImageIcon(WindowUI.class.getResource("resource/warn.png")));
    private static JButton localWord = new JButton(new ImageIcon(WindowUI.class.getResource("resource/local_word.png")));
    private static JButton set = new JButton(new ImageIcon(WindowUI.class.getResource("resource/set.png")));
    private static JButton showGroupList = new JButton(new ImageIcon(WindowUI.class.getResource("resource/chat.png")));
    private static JButton inviteIntoGroup = new JButton(new ImageIcon(WindowUI.class.getResource("resource/reply.png")));
    private static JButton removeFromGroup = new JButton(new ImageIcon(WindowUI.class.getResource("resource/warn.png")));
    private static JButton sendByTime = new JButton(new ImageIcon(WindowUI.class.getResource("resource/send_by_time.png")));
    private static JButton publicWord = new JButton(new ImageIcon(WindowUI.class.getResource("resource/send_by_time.png")));
    private static JButton privateWord = new JButton(new ImageIcon(WindowUI.class.getResource("resource/send_by_time.png")));
    private static JButton senseWord = new JButton(new ImageIcon(WindowUI.class.getResource("resource/send_by_time.png")));
    private static JPanel userInfoJPanel = new JPanel();
    private static JPanel jPanel = new JPanel();
    private static JPanel chatJPanel = new JPanel();
    private static JPanel tipPanel = new JPanel();
    private static JPanel addTipPanel = new JPanel();
    private static JPanel wordPanel = new JPanel();
    private static JPanel showGroupPanel = new JPanel();
    private static JPanel invitePanel = new JPanel();
    private static JPanel removePanel = new JPanel();
    private static JTabbedPane setPanel = new JTabbedPane();
    private static JPanel setGroupNamePanel = new JPanel();
    private static JPanel setTuLingAPIPanel = new JPanel();
    private static JScrollPane jScrollPane;
    private static JScrollPane tipJScrollPane;
    private static JScrollPane setGroupNameJScrollPane;
    private static JScrollPane inviteScrollPane1;
    private static JScrollPane inviteScrollPane2;
    private static JScrollPane removeScrollPane1;
    private static JScrollPane removeScrollPane2;
    private static JLabel userNameLabel = new JLabel();//存放用户名字
    private static JLabel userHeaderImg = new JLabel();//存放用户头像
    private static JLabel jLabel_0 = new JLabel();//存放二维码
    private static JPanel jPanel_1 = new JPanel();//存放检查群消息按钮
    private static JPanel jPanel_2 = new JPanel();//好友自动通过
    private static JPanel jPanel_3 = new JPanel();//存放查看签到记录的按钮
    private static JPanel jPanel_4 = new JPanel();//存放智能聊天的按钮
    private static JPanel jPanel_5 = new JPanel();//存放自动回复功能的按钮
    private static JPanel jPanel_6 = new JPanel();//存放敏感警告功能的按钮
    private static JPanel jPanel_7 = new JPanel();//存放本地词库功能的按钮
    private static JPanel jPanel_8 = new JPanel();//存放设置功能的按钮
    private static JPanel jPanel_9 = new JPanel();//存放消息定时发送功能的按钮
    private static JPanel jPanel_10 = new JPanel();//存放显示群列表的功能的按钮
    private static JPanel jPanel_11 = new JPanel();//存放邀请进群
    private static JPanel jPanel_12 = new JPanel();//存放T人出群
    private static JPanel publicPanel = new JPanel();
    private static JPanel privatePanel = new JPanel();
    private static JPanel sensePanel = new JPanel();
    private static JList jList1 = null;
    private static JList jList2 = null;
    private static JList jList3 =null;
    private static JList jList4 = null;
    private static JTextPane jTextPane = new JTextPane();
    private static JTextArea timeArea = new JTextArea();
    private static JTextArea propertyArea = new JTextArea();
    private static JComboBox groupNameArea = new JComboBox();//定时发送中选择群名的选择框
    private static JButton addTipButton = new JButton("添加定时消息发送");
    private static JButton saveRecord = new JButton("保存消息到本地");
    private static JButton seeRecord = new JButton("查看本地消息");
    private static JButton remove = new JButton("踢出该群");
    private static JButton invite = new JButton("邀请进群");
    private static SimpleAttributeSet attributeSet = new SimpleAttributeSet();
    private static GridBagConstraints gb = new GridBagConstraints();
    private static SimpleDateFormat df = new SimpleDateFormat("HH:mm");
    private static Vector<String> friend = new Vector<>();
    private static Vector<String> friendID = new Vector<>();

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
            chooseGroup =new JFrame("微信机器人--群列表");
            groupInvite = new JFrame("微信机器人--邀请进群");
            groupRemove = new JFrame("微信机器人--踢人出群");



            mainFrame.setLocation(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - 240, Toolkit.getDefaultToolkit().getScreenSize().height / 2 - 400);
            mainFrame.setVisible(false);
            mainFrame.setResizable(false);
            mainFrame.setDefaultCloseOperation(mainFrame.EXIT_ON_CLOSE);
            mainFrame.setLayout(new GridBagLayout());
            gb.gridx = GridBagConstraints.RELATIVE;
            gb.gridy = GridBagConstraints.RELATIVE;
            gb.ipadx = 0;
            gb.ipady = -30;
            mainFrame.add(jPanel,gb);


            chatIn.setLocation(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - 300, Toolkit.getDefaultToolkit().getScreenSize().height / 2 - 200);
            chatIn.setVisible(false);
            chatIn.setResizable(false);
            chatIn.setDefaultCloseOperation(chatIn.HIDE_ON_CLOSE);
            chatIn.setLayout(new GridLayout(0,1));
            chatIn.setSize(600, 400);

            dailyTip.setLocation(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - 500, Toolkit.getDefaultToolkit().getScreenSize().height / 2 - 200);
            dailyTip.setVisible(false);
            dailyTip.setResizable(false);
            dailyTip.setDefaultCloseOperation(dailyTip.HIDE_ON_CLOSE);
            dailyTip.setLayout(new GridLayout());
            dailyTip.setSize(1000, 400);
            tipJScrollPane = new JScrollPane(tipPanel);
            tipPanel.setLayout(new BoxLayout(tipPanel, BoxLayout.Y_AXIS));
            dailyTip.add(tipJScrollPane);

            wordModify.setLocation(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - 200, Toolkit.getDefaultToolkit().getScreenSize().height / 2 - 100);
            wordModify.setVisible(false);
            wordModify.setResizable(false);
            wordModify.setDefaultCloseOperation(wordModify.HIDE_ON_CLOSE);
            wordModify.setSize(400, 200);
            wordModify.add(wordPanel);

            chooseGroup.setLocation(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - 100, Toolkit.getDefaultToolkit().getScreenSize().height / 2 - 300);
            chooseGroup.setVisible(false);
            chooseGroup.setResizable(false);
            chooseGroup.setDefaultCloseOperation(chooseGroup.HIDE_ON_CLOSE);
            chooseGroup.add(showGroupPanel);

            groupInvite.setLocation(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - 400, Toolkit.getDefaultToolkit().getScreenSize().height / 2 - 300);
            groupInvite.setVisible(false);
            groupInvite.setResizable(false);
            groupInvite.setLayout(new BorderLayout());
            groupInvite.setDefaultCloseOperation(groupInvite.HIDE_ON_CLOSE);
            groupInvite.setSize(800,700);
            groupInvite.add(BorderLayout.CENTER,invitePanel);
            groupInvite.add(BorderLayout.SOUTH,invite);

            groupRemove.setLocation(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - 400, Toolkit.getDefaultToolkit().getScreenSize().height / 2 - 300);
            groupRemove.setVisible(false);
            groupRemove.setResizable(false);
            groupRemove.setLayout(new BorderLayout());
            groupRemove.setDefaultCloseOperation(groupRemove.HIDE_ON_CLOSE);
            groupRemove.setSize(800,700);
            groupRemove.add(BorderLayout.CENTER,removePanel);
            groupRemove.add(BorderLayout.SOUTH,remove);

            setFrame.setLocation(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - 400, Toolkit.getDefaultToolkit().getScreenSize().height / 2 - 300);
            setFrame.setVisible(false);
            setFrame.setResizable(false);
            setFrame.setDefaultCloseOperation(setFrame.HIDE_ON_CLOSE);
            setFrame.setSize(800,700);
            setFrame.add(setPanel);

            //主界面按钮的UI设置
            userInfoJPanel.setLayout(new FlowLayout(FlowLayout.CENTER,50,20));
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
            //主界面的组件设置
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
            j3.setFont(new Font("黑体",1,16));
            jPanel_3.add(j3);
            jPanel_4.setBorder(null);
            jPanel_4.setLayout(new GridLayout(2, 1));
            jPanel_4.add(autoChat);
            JLabel j4 = new JLabel("智能聊天");
            j4.setFont(new Font("黑体",1,16));
            jPanel_4.add(j4);
            jPanel_5.setBorder(null);
            jPanel_5.setLayout(new GridLayout(2, 1));
            jPanel_5.add(reply);
            JLabel j5 = new JLabel("自动回复");
            j5.setFont(new Font("黑体",1,16));
            jPanel_5.add(j5);
            jPanel_6.setBorder(null);
            jPanel_6.setLayout(new GridLayout(2, 1));
            jPanel_6.add(warn);
            JLabel j6 = new JLabel("敏感警告");
            j6.setFont(new Font("黑体",1,16));
            jPanel_6.add(j6);
            jPanel_7.setBorder(null);
            jPanel_7.setLayout(new GridLayout(2, 1));
            jPanel_7.add(localWord);
            JLabel j7 = new JLabel("本地词库");
            j7.setFont(new Font("黑体",1,16));
            jPanel_7.add(j7);
            jPanel_8.setBorder(null);
            jPanel_8.setLayout(new GridLayout(2, 1));
            jPanel_8.add(set);
            JLabel j8 = new JLabel("设    置");
            j8.setFont(new Font("黑体",1,16));
            jPanel_8.add(j8);
            jPanel_9.setBorder(null);
            jPanel_9.setLayout(new GridLayout(2, 1));
            jPanel_9.add(sendByTime);
            JLabel j9 = new JLabel("定时发送");
            j9.setFont(new Font("黑体",1,16));
            jPanel_9.add(j9);
            jPanel_10.setBorder(null);
            jPanel_10.setLayout(new GridLayout(2,1));
            jPanel_10.add(showGroupList);
            JLabel j10 = new JLabel("群组列表");
            j10.setFont(new Font("黑体",1,16));
            jPanel_10.add(j10);
            jPanel_11.setBorder(null);
            jPanel_11.setLayout(new GridLayout(2,1));
            jPanel_11.add(inviteIntoGroup);
            JLabel j11 = new JLabel("邀请进群");
            j11.setFont(new Font("黑体",1,16));
            jPanel_11.add(j11);
            jPanel_12.setBorder(null);
            jPanel_12.setLayout(new GridLayout(2,1));
            jPanel_12.add(removeFromGroup);
            JLabel j12 = new JLabel("踢人出群");
            j12.setFont(new Font("黑体",1,16));
            jPanel_12.add(j12);

            //消息盒子的组件设置
            saveRecord.setUI(new BEButtonUI().setNormalColor(BEButtonUI.NormalColor.red));
            seeRecord.setUI(new BEButtonUI().setNormalColor(BEButtonUI.NormalColor.green));
            saveRecord.setForeground(Color.white);
            seeRecord.setForeground(Color.white);
            chatJPanel.setLayout(new FlowLayout(FlowLayout.CENTER,50,50));
            chatJPanel.add(saveRecord);
            chatJPanel.add(seeRecord);
            jTextPane.setFocusable(true);
            jTextPane.setEditable(false);
            jScrollPane = new JScrollPane(jTextPane);
            StyleConstants.setFontSize(attributeSet,16);
            jScrollPane.setHorizontalScrollBarPolicy(
                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            jScrollPane.setVerticalScrollBarPolicy(
                    JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            chatIn.add(jScrollPane);
            chatIn.add(chatJPanel);

            //定时发布的组件设置
            addTipPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 20));
            timeArea.setSize(100, 100);
            timeArea.setLineWrap(true);
            propertyArea.setSize(200, 100);
            propertyArea.setLineWrap(true);
            groupNameArea.setSize(100,100);
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
            tipJScrollPane.setHorizontalScrollBarPolicy(
                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            tipJScrollPane.setVerticalScrollBarPolicy(
                    JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            addTipButton.setSize(0, 100);
            addTipButton.setUI(new BEButtonUI().setNormalColor(BEButtonUI.NormalColor.green));
            tipPanel.add(addTipPanel);


            wordPanel.setLayout(new FlowLayout(FlowLayout.CENTER,10,30));
            publicPanel.setLayout(new GridLayout(2,1));
            JLabel publicLabel = new JLabel("公开词库");
            publicLabel.setFont(new Font("黑体",1,16));
            publicPanel.add(publicWord);
            publicPanel.add(publicLabel);
            JLabel privateLabel = new JLabel("私密词库");
            privateLabel.setFont(new Font("黑体",1,16));
            privatePanel.setLayout(new GridLayout(2,1));
            privatePanel.add(privateWord);
            privatePanel.add(privateLabel);
            JLabel senseLabel = new JLabel("敏感词库");
            senseLabel.setFont(new Font("黑体",1,16));
            sensePanel.setLayout(new GridLayout(2,1));
            sensePanel.add(senseWord);
            sensePanel.add(senseLabel);
            wordPanel.add(publicPanel);
            wordPanel.add(privatePanel);
            wordPanel.add(sensePanel);


            showGroupPanel.setLayout(new GridLayout(0,1,20,20));
            showGroupPanel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

            invitePanel.setLayout(new GridLayout(0,2,20,20));
            invitePanel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
            invite.setUI(new BEButtonUI().setNormalColor(BEButtonUI.NormalColor.green));
            invite.setForeground(Color.white);


            removePanel.setLayout(new GridLayout(0,2,20,20));
            removePanel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
            remove.setUI(new BEButtonUI().setNormalColor(BEButtonUI.NormalColor.green));
            remove.setForeground(Color.white);
            jList4 = new JList();


            setPanel.add("修改群名",setGroupNamePanel);
            setGroupNamePanel.setLayout(new GridLayout(0,3,20,0));
            setGroupNamePanel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
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
    public static void addGroupNameModify(String oldName,String newName){

    }
    /**
     * 添加定时记录的组件
     *
     * @param tipRecord
     *
     **/
    public static void addTip(final TipRecord tipRecord) {
        JButton deleteButton = new JButton("删除");
        deleteButton.setUI(new BEButtonUI().setNormalColor(BEButtonUI.NormalColor.red));
        JLabel record = new JLabel();
        final JPanel totalRecord = new JPanel();
        totalRecord.setLayout(new FlowLayout(FlowLayout.CENTER, 50, 20));
        try{
            record.setText("时间：" + df.format(df.parse(timeArea.getText())) + "   群："+groupNameArea.getSelectedItem().toString()+"   内容: " + propertyArea.getText());
            totalRecord.add(record);
            totalRecord.add(deleteButton);
            deleteButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Iterator iterator = Main.getTipRecordList().iterator();
                    while(iterator.hasNext()){
                        TipRecord tipRecord1 = (TipRecord)iterator.next();
                        if(tipRecord.equals(tipRecord1)){
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
        }catch (Exception e){
            JOptionPane.showMessageDialog(null, "错误：输入时间格式不正确", "错误", JOptionPane.ERROR_MESSAGE);
        }

    }



    public static SimpleAttributeSet getAttributeSet() {
        return attributeSet;
    }

    public static void setAttributeSet(SimpleAttributeSet attributeSet) {
        WindowUI.attributeSet = attributeSet;
    }

    public static JPanel getjPanel_3() {
        return jPanel_3;
    }

    public static void setjPanel_3(JPanel jPanel_3) {
        WindowUI.jPanel_3 = jPanel_3;
    }

    public static JFrame getF() {
        return mainFrame;
    }

    public static void setF(JFrame f) {
        WindowUI.mainFrame = f;
    }

    public static JFrame getChatIn() {
        return chatIn;
    }

    public static void setChatIn(JFrame chatIn) {
        WindowUI.chatIn = chatIn;
    }

    public static JButton getChatJButton() {
        return chatJButton;
    }

    public static void setChatJButton(JButton chatJButton) {
        WindowUI.chatJButton = chatJButton;
    }

    public static JButton getCheckJButton() {
        return addFriend;
    }

    public static void setCheckJButton(JButton checkJButton) {
        WindowUI.addFriend = checkJButton;
    }

    public static JPanel getjPanel() {
        return jPanel;
    }

    public static void setjPanel(JPanel jPanel) {
        WindowUI.jPanel = jPanel;
    }

    public static JPanel getChatJPanel() {
        return chatJPanel;
    }

    public static void setChatJPanel(JPanel chatJPanel) {
        WindowUI.chatJPanel = chatJPanel;
    }

    public static JScrollPane getjScrollPane() {
        return jScrollPane;
    }

    public static void setjScrollPane(JScrollPane jScrollPane) {
        WindowUI.jScrollPane = jScrollPane;
    }

    public static JLabel getjLabel_0() {
        return jLabel_0;
    }

    public static void setjLabel_0(JLabel jLabel_0) {
        WindowUI.jLabel_0 = jLabel_0;
    }

    public static JPanel getjPanel_1() {
        return jPanel_1;
    }

    public static void setjPanel_1(JPanel jPanel_1) {
        WindowUI.jPanel_1 = jPanel_1;
    }

    public static JPanel getjPanel_2() {
        return jPanel_2;
    }

    public static void setjPanel_2(JPanel jPanel_2) {
        WindowUI.jPanel_2 = jPanel_2;
    }

    public static JTextPane getjTextPane() {
        return jTextPane;
    }

    public static void setjTextPane(JTextPane jTextPane) {
        WindowUI.jTextPane = jTextPane;
    }

    public static JButton getThirdJButton() {
        return signRecord;
    }

    public static void setThirdJButton(JButton thirdJButton) {
        WindowUI.signRecord = thirdJButton;
    }

    public static JFrame getDailyTip() {
        return dailyTip;
    }

    public static void setDailyTip(JFrame dailyTip) {
        WindowUI.dailyTip = dailyTip;
    }

    public static JPanel getTipPanel() {
        return tipPanel;
    }

    public static void setTipPanel(JPanel tipPanel) {
        WindowUI.tipPanel = tipPanel;
    }

    public static JScrollPane getTipJScrollPane() {
        return tipJScrollPane;
    }

    public static void setTipJScrollPane(JScrollPane tipJScrollPane) {
        WindowUI.tipJScrollPane = tipJScrollPane;
    }

    public static JTextArea getTimeArea() {
        return timeArea;
    }

    public static void setTimeArea(JTextArea timeArea) {
        WindowUI.timeArea = timeArea;
    }

    public static JTextArea getPropertyArea() {
        return propertyArea;
    }

    public static void setPropertyArea(JTextArea propertyArea) {
        WindowUI.propertyArea = propertyArea;
    }

    public static JButton getAddTipButton() {
        return addTipButton;
    }

    public static void setAddTipButton(JButton addTipButton) {
        WindowUI.addTipButton = addTipButton;
    }

    public static JPanel getAddTipPanel() {
        return addTipPanel;
    }

    public static void setAddTipPanel(JPanel addTipPanel) {
        WindowUI.addTipPanel = addTipPanel;
    }

    public static SimpleDateFormat getDf() {
        return df;
    }

    public static void setDf(SimpleDateFormat df) {
        WindowUI.df = df;
    }

    public static JComboBox getGroupNameArea() {
        return groupNameArea;
    }

    public static void setGroupNameArea(JComboBox groupNameArea) {
        WindowUI.groupNameArea = groupNameArea;
    }

    public static JButton getAddFriend() {
        return addFriend;
    }

    public static void setAddFriend(JButton addFriend) {
        WindowUI.addFriend = addFriend;
    }

    public static JButton getSignRecord() {
        return signRecord;
    }

    public static void setSignRecord(JButton signRecord) {
        WindowUI.signRecord = signRecord;
    }

    public static JButton getAutoChat() {
        return autoChat;
    }

    public static void setAutoChat(JButton autoChat) {
        WindowUI.autoChat = autoChat;
    }

    public static JButton getReply() {
        return reply;
    }

    public static void setReply(JButton reply) {
        WindowUI.reply = reply;
    }

    public static JButton getWarn() {
        return warn;
    }

    public static void setWarn(JButton warn) {
        WindowUI.warn = warn;
    }

    public static JButton getLocalWord() {
        return localWord;
    }

    public static void setLocalWord(JButton localWord) {
        WindowUI.localWord = localWord;
    }

    public static JButton getSet() {
        return set;
    }

    public static void setSet(JButton set) {
        WindowUI.set = set;
    }

    public static JPanel getjPanel_4() {
        return jPanel_4;
    }

    public static void setjPanel_4(JPanel jPanel_4) {
        WindowUI.jPanel_4 = jPanel_4;
    }

    public static JPanel getjPanel_5() {
        return jPanel_5;
    }

    public static void setjPanel_5(JPanel jPanel_5) {
        WindowUI.jPanel_5 = jPanel_5;
    }

    public static JPanel getjPanel_6() {
        return jPanel_6;
    }

    public static void setjPanel_6(JPanel jPanel_6) {
        WindowUI.jPanel_6 = jPanel_6;
    }

    public static JPanel getjPanel_7() {
        return jPanel_7;
    }

    public static void setjPanel_7(JPanel jPanel_7) {
        WindowUI.jPanel_7 = jPanel_7;
    }

    public static JPanel getjPanel_8() {
        return jPanel_8;
    }

    public static void setjPanel_8(JPanel jPanel_8) {
        WindowUI.jPanel_8 = jPanel_8;
    }

    public static JButton getSendByTime() {
        return sendByTime;
    }

    public static void setSendByTime(JButton sendByTime) {
        WindowUI.sendByTime = sendByTime;
    }

    public static JPanel getjPanel_9() {
        return jPanel_9;
    }

    public static void setjPanel_9(JPanel jPanel_9) {
        WindowUI.jPanel_9 = jPanel_9;
    }

    public static JLabel getUserNameLabel() {
        return userNameLabel;
    }

    public static void setUserNameLabel(JLabel userNameLabel) {
        WindowUI.userNameLabel = userNameLabel;
    }

    public static JPanel getUserInfoJPanel() {
        return userInfoJPanel;
    }

    public static void setUserInfoJPanel(JPanel userInfoJPanel) {
        WindowUI.userInfoJPanel = userInfoJPanel;
    }

    public static JLabel getUserHeaderImg() {
        return userHeaderImg;
    }

    public static void setUserHeaderImg(JLabel userHeaderImg) {
        WindowUI.userHeaderImg = userHeaderImg;
    }

    public static GridBagConstraints getGb() {
        return gb;
    }

    public static void setGb(GridBagConstraints gb) {
        WindowUI.gb = gb;
    }

    public static JFrame getMainFrame() {
        return mainFrame;
    }

    public static void setMainFrame(JFrame mainFrame) {
        WindowUI.mainFrame = mainFrame;
    }

    public static JFrame getWordModify() {
        return wordModify;
    }

    public static void setWordModify(JFrame wordModify) {
        WindowUI.wordModify = wordModify;
    }

    public static JButton getPublicWord() {
        return publicWord;
    }

    public static void setPublicWord(JButton publicWord) {
        WindowUI.publicWord = publicWord;
    }

    public static JButton getPrivateWord() {
        return privateWord;
    }

    public static void setPrivateWord(JButton privateWord) {
        WindowUI.privateWord = privateWord;
    }

    public static JButton getSenseWord() {
        return senseWord;
    }

    public static void setSenseWord(JButton senseWord) {
        WindowUI.senseWord = senseWord;
    }

    public static JPanel getWordPanel() {
        return wordPanel;
    }

    public static void setWordPanel(JPanel wordPanel) {
        WindowUI.wordPanel = wordPanel;
    }

    public static JPanel getPublicPanel() {
        return publicPanel;
    }

    public static void setPublicPanel(JPanel publicPanel) {
        WindowUI.publicPanel = publicPanel;
    }

    public static JPanel getPrivatePanel() {
        return privatePanel;
    }

    public static void setPrivatePanel(JPanel privatePanel) {
        WindowUI.privatePanel = privatePanel;
    }

    public static JPanel getSensePanel() {
        return sensePanel;
    }

    public static void setSensePanel(JPanel sensePanel) {
        WindowUI.sensePanel = sensePanel;
    }

    public static JButton getSaveRecord() {
        return saveRecord;
    }

    public static void setSaveRecord(JButton saveRecord) {
        WindowUI.saveRecord = saveRecord;
    }

    public static JButton getSeeRecord() {
        return seeRecord;
    }

    public static void setSeeRecord(JButton seeRecord) {
        WindowUI.seeRecord = seeRecord;
    }

    public static JFrame getChooseGroup() {
        return chooseGroup;
    }

    public static void setChooseGroup(JFrame chooseGroup) {
        WindowUI.chooseGroup = chooseGroup;
    }

    public static JButton getShowGroupList() {
        return showGroupList;
    }

    public static void setShowGroupList(JButton showGroupList) {
        WindowUI.showGroupList = showGroupList;
    }

    public static JPanel getShowGroupPanel() {
        return showGroupPanel;
    }

    public static void setShowGroupPanel(JPanel showGroupPanel) {
        WindowUI.showGroupPanel = showGroupPanel;
    }

    public static JPanel getjPanel_10() {
        return jPanel_10;
    }

    public static void setjPanel_10(JPanel jPanel_10) {
        WindowUI.jPanel_10 = jPanel_10;
    }

    public static JFrame getGroupInvite() {
        return groupInvite;
    }

    public static void setGroupInvite(JFrame groupInvite) {
        WindowUI.groupInvite = groupInvite;
    }

    public static JButton getInviteIntoGroup() {
        return inviteIntoGroup;
    }

    public static void setInviteIntoGroup(JButton inviteIntoGroup) {
        WindowUI.inviteIntoGroup = inviteIntoGroup;
    }

    public static JPanel getInvitePanel() {
        return invitePanel;
    }

    public static void setInvitePanel(JPanel invitePanel) {
        WindowUI.invitePanel = invitePanel;
    }

    public static JPanel getjPanel_11() {
        return jPanel_11;
    }

    public static void setjPanel_11(JPanel jPanel_11) {
        WindowUI.jPanel_11 = jPanel_11;
    }

    public static JList getjList1() {
        return jList1;
    }

    public static void setjList1(JList jList1) {
        WindowUI.jList1 = jList1;
    }

    public static JList getjList2() {
        return jList2;
    }

    public static void setjList2(JList jList2) {
        WindowUI.jList2 = jList2;
    }

    public static JButton getRemove() {
        return remove;
    }

    public static void setRemove(JButton remove) {
        WindowUI.remove = remove;
    }

    public static JButton getInvite() {
        return invite;
    }

    public static void setInvite(JButton invite) {
        WindowUI.invite = invite;
    }

    public static JScrollPane getInviteScrollPane1() {
        return inviteScrollPane1;
    }

    public static void setInviteScrollPane1(JScrollPane inviteScrollPane1) {
        WindowUI.inviteScrollPane1 = inviteScrollPane1;
    }

    public static JScrollPane getInviteScrollPane2() {
        return inviteScrollPane2;
    }

    public static void setInviteScrollPane2(JScrollPane inviteScrollPane2) {
        WindowUI.inviteScrollPane2 = inviteScrollPane2;
    }

    public static JFrame getGroupRemove() {
        return groupRemove;
    }

    public static void setGroupRemove(JFrame groupRemove) {
        WindowUI.groupRemove = groupRemove;
    }

    public static JButton getRemoveFromGroup() {
        return removeFromGroup;
    }

    public static void setRemoveFromGroup(JButton removeFromGroup) {
        WindowUI.removeFromGroup = removeFromGroup;
    }

    public static JPanel getRemovePanel() {
        return removePanel;
    }

    public static void setRemovePanel(JPanel removePanel) {
        WindowUI.removePanel = removePanel;
    }

    public static JScrollPane getRemoveScrollPane1() {
        return removeScrollPane1;
    }

    public static void setRemoveScrollPane1(JScrollPane removeScrollPane1) {
        WindowUI.removeScrollPane1 = removeScrollPane1;
    }

    public static JScrollPane getRemoveScrollPane2() {
        return removeScrollPane2;
    }

    public static void setRemoveScrollPane2(JScrollPane removeScrollPane2) {
        WindowUI.removeScrollPane2 = removeScrollPane2;
    }

    public static JPanel getjPanel_12() {
        return jPanel_12;
    }

    public static void setjPanel_12(JPanel jPanel_12) {
        WindowUI.jPanel_12 = jPanel_12;
    }

    public static JList getjList3() {
        return jList3;
    }

    public static void setjList3(JList jList3) {
        WindowUI.jList3 = jList3;
    }

    public static JList getjList4() {
        return jList4;
    }

    public static void setjList4(JList jList4) {
        WindowUI.jList4 = jList4;
    }

    public static Vector<String> getFriend() {
        return friend;
    }

    public static void setFriend(Vector<String> friend) {
        WindowUI.friend = friend;
    }

    public static Vector<String> getFriendID() {
        return friendID;
    }

    public static void setFriendID(Vector<String> friendID) {
        WindowUI.friendID = friendID;
    }

    public static JFrame getSetFrame() {
        return setFrame;
    }

    public static void setSetFrame(JFrame setFrame) {
        WindowUI.setFrame = setFrame;
    }

    public static JTabbedPane getSetPanel() {
        return setPanel;
    }

    public static void setSetPanel(JTabbedPane setPanel) {
        WindowUI.setPanel = setPanel;
    }

    public static JPanel getSetGroupNamePanel() {
        return setGroupNamePanel;
    }

    public static void setSetGroupNamePanel(JPanel setGroupNamePanel) {
        WindowUI.setGroupNamePanel = setGroupNamePanel;
    }

    public static JPanel getSetTuLingAPIPanel() {
        return setTuLingAPIPanel;
    }

    public static void setSetTuLingAPIPanel(JPanel setTuLingAPIPanel) {
        WindowUI.setTuLingAPIPanel = setTuLingAPIPanel;
    }

    public static JScrollPane getSetGroupNameJScrollPane() {
        return setGroupNameJScrollPane;
    }

    public static void setSetGroupNameJScrollPane(JScrollPane setGroupNameJScrollPane) {
        WindowUI.setGroupNameJScrollPane = setGroupNameJScrollPane;
    }
}
