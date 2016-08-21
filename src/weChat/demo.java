package weChat;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by huzhejie on 2016/8/8.
 */
public class demo {
    private static List<String> id = new ArrayList<>();
    private static WindowUI windowUI;
    public static void main(String args[])throws Exception{
        windowUI = new WindowUI();
        windowUI.getF().setSize(480,600);
        String s[] = {"huhuhuasassa","dadadaasasa"};
        windowUI.setjList1(new JList(s));
        windowUI.setjList2(new JList(s));
        windowUI.getjList1().setSize(400,400);
        windowUI.getjList2().setSize(400,400);
        windowUI.getjList1().setBorder(BorderFactory.createTitledBorder("好友昵称"));
        windowUI.getjList2().setBorder(BorderFactory.createTitledBorder("群名称"));
        windowUI.setInviteScrollPane1(new JScrollPane(windowUI.getjList1()));
        windowUI.setInviteScrollPane2(new JScrollPane(windowUI.getjList2()));
        windowUI.getInviteScrollPane1().setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        windowUI.getInviteScrollPane1().setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        windowUI.getInviteScrollPane2().setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        windowUI.getInviteScrollPane2().setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        windowUI.getInvitePanel().add(windowUI.getInviteScrollPane1());
        windowUI.getInvitePanel().add(windowUI.getInviteScrollPane2());
        windowUI.getGroupInvite().add(BorderLayout.SOUTH,windowUI.getInvite());
        windowUI.getGroupInvite().setVisible(true);
        windowUI.getGb().gridx=0;
        windowUI.getGb().gridy=1;
        windowUI.getGb().weightx=3;
        windowUI.getGb().ipady=400;
        windowUI.getF().add(windowUI.getjPanel(),windowUI.getGb());
//        windowUI.getF().setVisible(true);
        windowUI.getInviteIntoGroup().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                windowUI.getGroupInvite().setVisible(true);
            }
        });
        windowUI.getjList1().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
            }
        });
    }
}
