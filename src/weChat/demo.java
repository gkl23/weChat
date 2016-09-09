package weChat;


import Models.TipRecord;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by huzhejie on 2016/8/8.
 */
public class demo {
    private static WindowUI windowUI;
    public static void main(String args[])throws Exception{
        windowUI = new WindowUI();
        windowUI.getDailyTip().setVisible(true);
    }
}