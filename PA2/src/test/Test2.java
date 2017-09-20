package test;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Socket;
import java.util.Vector;

import dep.*;

public class Test2 {
	public static void main(String[] args){
		Vector<String[]> fingerTable = new Vector<String[]>();
		
		for(int i =0;  i != 5; ++i){
			String[] x = {Integer.toString(i*2), Integer.toString(i*3)};
			System.out.println(x);
			fingerTable.add(x);
		}
		String[] t = {"a","b"};
		test(t);
		
	}
	public static void test(String[] x){
		
	}
}
