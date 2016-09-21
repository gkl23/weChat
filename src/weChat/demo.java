package weChat;


import Models.TipRecord;
import blade.kit.http.HttpRequest;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.Properties;

/**
 * Created by huzhejie on 2016/8/8.
 */
public class demo {
    public static void main(String args[])throws Exception{
        WindowUI windowUI = new WindowUI();
        windowUI.getDailyTip().setVisible(true);
        windowUI.getGroupNameArea().addItem("留日申请（大学院）起航群      ");
        windowUI.getGroupNamePeriodArea().addItem("留日申请（大学院）起航群       ");
    }
}