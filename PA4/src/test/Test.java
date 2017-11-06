package test;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.text.SimpleDateFormat;

public class Test {

	public static void main(String[] args) {
		String x = "35_192.5.4.4._d";
		String[] y = x.split("_");
		for (int i = 0; i != y.length; ++i)
			System.out.println(y[i]);
		
//		String[] x = new String[3];
//		for (int i = 0; i != x.length; ++i) 
//			System.out.println(x[i]);
		
//		String s = "";
//		int x = 223;
//		s += x;
//		System.out.println(s);
//		try {
//		System.out.println(Inet4Address.getLocalHost().getHostAddress());
//		} catch (IOException e) {
//			System.out.println("error");
//		}
		
//		String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
//		System.out.println(timeStamp);
		
//		Map<String, Vector<Integer>> test = new ConcurrentHashMap<String, Vector<Integer>>();
//		test.put("abc", new Vector<Integer>());
//		test.get("abc").add(0);
//		test.get("abc").add(3);
//		test.get("abc").add(2);
//		test.get("abc").add(19);
//
//		test.put("def", new Vector<Integer>());
//		test.get("def").add(0);
//		test.get("def").add(3);
//		test.get("def").add(2);
//		test.get("def").add(19);
//		System.out.println(test);
	}

}
