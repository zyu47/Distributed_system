package test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import dep.*;

public class Test {

	public static void main(String[] args) {
//		System.out.println(CompareIDrange.inrange("3c07", "6ca5", "3c09"));
		System.out.println(ByteMath.minus("5000",
				(int)Math.pow(2, 15)));
	}
}

class input extends Thread{
	public input(ServerSocket s){
		ss = s;
	}
	public void run(){
		ArrayList<String> tmp = new ArrayList<String>();
		String x = "";
		while(true){
			Scanner sc = new Scanner(System.in);
			x = sc.next();
			if(x.equals("def")){
				break;
			}
		}
		try{
		ss.close();
		} catch (IOException e){
			e.printStackTrace();
			
		}
	}
	public static String x = "abc"; 
	public ServerSocket ss;
}