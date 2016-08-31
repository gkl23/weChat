package weChat;


import java.awt.*;

/**
 * Created by huzhejie on 2016/8/8.
 */
public class demo {
    private static WindowUI windowUI;
    public static void main(String args[])throws Exception{
        windowUI = new WindowUI();
        windowUI.getMainFrame().setSize(400, 600);
        windowUI.getGb().gridx = 0;
        windowUI.getGb().gridy = 0;
        windowUI.getGb().gridwidth = 0;
        windowUI.getGb().gridheight = GridBagConstraints.BOTH;
        windowUI.getGb().ipady = 100;
        windowUI.getGb().ipadx = 480;
        windowUI.getMainFrame().add(windowUI.getUserInfoJPanel(), windowUI.getGb());
        windowUI.getjPanel().setLayout(new FlowLayout(FlowLayout.CENTER, 15, 20));
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
        windowUI.getGb().gridx = 0;
        windowUI.getGb().gridy = 1;
        windowUI.getGb().weightx = 4;
        windowUI.getGb().ipady = 400;
        windowUI.getMainFrame().add(windowUI.getjPanel(), windowUI.getGb());
        windowUI.getMainFrame().setVisible(true);
    }
}