package weChat;

import java.awt.*;

import java.util.*;
import java.util.List;

/**
 * Created by huzhejie on 2016/8/8.
 */
public class demo {
	public static void main(String args[]) throws Exception {
		WindowUI windowUI = new WindowUI();
		System.out.println(windowUI.getDf().parse(windowUI.getDf().format(new Date())));
	}
}