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
    }
}
