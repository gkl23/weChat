package weChat;


import Models.TipRecord;
import blade.kit.http.HttpRequest;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by huzhejie on 2016/8/8.
 */
public class demo {
    public static void main(String args[])throws Exception{
        String s = HttpRequest.get("http://v.gdt.qq.com/gdt_stats.fcg?stkey=60621BED00E8465E6A1E362E09423634ACD8E23D4846CDAABE7F438819C876CF7CAF6166B4DCE478536DB719494DB3AD2854D74811EFB8BC5BE76BE19BEC1165901E2F0B8C9456A968C3CD3D4D398C41E4315E8F94DD0B0E")
                .header("Cookie","cuid=790453306;pac_uid=1_604142287;o_cookie=604142287;qz_gdtinner=zj45ovy5aaabnawrf5ta;gdt_uid=0_604142287;ptcz=1c5418250052530a022f52936f98d10ad30e44a21f2378e85efe3504eee12696").body();
        System.out.println(s);
    }
}