package weChat;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URL;

/**
 * Created by huzhejie on 2016/7/12.
 */
public class ImageViewerFrame extends JFrame {
    public ImageViewerFrame(){

    }
    public void producePic(String url)throws Exception{
        setTitle("二维码");
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        URL u = new URL(url);
        DataInputStream in = new DataInputStream(u.openStream());
        FileOutputStream out = new FileOutputStream(new File("1.jpg"));
        byte[] b = new byte[1024];
        int length;
        while((length=in.read(b))>0){
            out.write(b,0,length);
        }
        in.close();
        out.close();
        ImageIcon image = new ImageIcon("1.jpg");
        label = new JLabel(image);
        label.setBounds(0,0,image.getIconWidth(),image.getIconHeight());
        add(label);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
    private JLabel label;
    private static final int DEFAULT_WIDTH = 450;
    private static final int DEFAULT_HEIGHT = 450;
}