package weChat;


import java.awt.*;

/**
 * Created by huzhejie on 2016/8/8.
 */
public class demo {
    private static WindowUI windowUI;
    public static void main(String args[])throws Exception{
        windowUI = new WindowUI();
        windowUI.getLoginFrame().setVisible(true);
    }
}