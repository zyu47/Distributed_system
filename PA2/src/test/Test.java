package test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import dep.*;

public class Test {

	public static void main(String[] args) {
		String s = "./test/folder/1.txt";
		String[] x = s.split("\\*");
		for (int i = 0; i != x.length; ++i) {
			System.out.println(x[i]);
			System.out.println(i);
		}
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