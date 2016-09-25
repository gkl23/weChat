package weChat;


import Models.GroupInfo;
import Models.TipRecord;
import Models.UserInfo;
import Utils.DBConnect;
import blade.kit.http.HttpRequest;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

/**
 * Created by huzhejie on 2016/8/8.
 */
public class demo {
    public static void main(String args[])throws Exception{
        WindowUI windowUI = new WindowUI();
        windowUI.getSetFrame().setVisible(true);
    }
}