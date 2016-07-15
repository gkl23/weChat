package weChat;

import blade.kit.http.HttpRequest;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by huzhejie on 2016/7/13.
 */
public class Practice {
    public static void main(String args[])throws Exception{
        System.setProperty ("jsse.enableSNIExtension", "false");
        String url = "https://wx.qq.com/cgi-bin/mmwebwx-bin/webwxgetcontact?lang=zh_CN&pass_ticket=VmsE%252F6pN7Lcw9%252BqRYMZdxx0xCXM0G1hMHKy2%252FCWAMIORz4WSaaXcMhsOzyd%252Fw7On&r=1468498846606&seq=0&skey=@crypt_c9cbcb35_7c81f763f4c15ebc1cff2647f0c7e678";
        HttpRequest httpRequest = HttpRequest.get(url);
        String friendList = httpRequest
                .header("Cookie","wxuin=2347013607; wxsid=nN4E2BguGC0dBgQl; wxloadtime=1468498845; webwx_data_ticket=gSd/gOPapCtX2whNDvgM+MIf")
                .body();
        System.out.println(friendList);


    }
}
