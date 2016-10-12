package weChat;

import java.awt.*;

import Models.GroupInfo;
import org.jb2011.lnf.beautyeye.ch3_button.BEButtonUI;
import org.jb2011.lnf.beautyeye.ch4_scroll.BEScrollPaneUI;

import javax.swing.*;

/**
 * Created by huzhejie on 2016/8/8.
 */
public class demo {
	public static void main(String args[]) throws Exception {
		final WindowUI windowUI = new WindowUI();
		windowUI.getSetFrame().setVisible(true);
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
		final GroupInfo group=new GroupInfo();
			final JLabel jLabel = new JLabel("test");
			jLabel.setBorder(BorderFactory.createTitledBorder("原群名"));
			final JTextField jTextArea = new JTextField(20);
			jTextArea.setDocument(new CustomJTextFieldDocument(32));
			jTextArea.setBorder(BorderFactory.createTitledBorder("新群名"));
			JButton jButton = new JButton("修改群名");
			jButton.setForeground(Color.white);
			jButton.setUI(new BEButtonUI().setNormalColor(BEButtonUI.NormalColor.red));

	}
}