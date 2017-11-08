package test;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.text.SimpleDateFormat;

public class Test {

	public static void main(String[] args) {
		Vector<String> entry = new Vector<String>();
		entry.add("a");
		String[] toSend = {"",""};
		for (int i = 0; i != entry.size(); ++i) {
			toSend[1] += entry.get(i);
			if (i != entry.size()-1)
				toSend[1] += "*";
		}
		System.out.println(toSend[1]);
	}

}
