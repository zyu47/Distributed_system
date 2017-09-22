package test;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Vector;

import dep.*;

public class Test2 {
	public static void main(String[] args){
		ArrayList<String> tmp = new ArrayList<String>();
		String x = "";
		while(true){
			System.out.println("dksfldsfjlds");
			Scanner sc = new Scanner(System.in);
			x = sc.next();
			
			tmp.add(x);
			if(x.equals("abc")){
				break;
			}
		}
		for(int i = 0; i != tmp.size(); ++i){
			System.out.println(tmp.get(i));
		}
		
	}
	public static void test(String[] x){
		
	}
}
