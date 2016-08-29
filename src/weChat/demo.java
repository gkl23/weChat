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
        windowUI.getChatIn().setVisible(true);
        windowUI.getSaveRecord().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "信息以及保存成功!", "提示", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }
}
