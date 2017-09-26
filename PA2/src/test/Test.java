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
		System.out.println(GetID.getHexID("1.txt"));
//		Set<String> x = new HashSet<String>();
//		x.add("abc");
//		x.add("def");
//		x.add("ghi");
//		System.out.println(x);
//		x.remove("abc");
//		System.out.println(x);
//		String msg = "UPDATE\n66e5\n127.34.5.9:6666\n";
//		String anotherMsg = "abcdefg";
//        // Start socket and send file by byte
//		NetAddr target = new NetAddr("129.82.45.44:6666");
//		Socket client = null;
//		PrintWriter out = null;
//		
//		try {
//			client = new Socket(target.host, target.port);
//			out = new PrintWriter(client.getOutputStream());
////			byte[] msgByte = msg.getBytes();
////	        
////			for (int i = 0; i != msgByte.length; ++i){
////	            out.write(msgByte[i]);
////	        }
//
//			out.println("STORE");
//			out.println("FileName");
//			out.println("");
//			
//			byte[] anotherByte = anotherMsg.getBytes();
//			for (int i = 0; i != anotherByte.length; ++i){
//				out.write(anotherByte[i]);
//			}
//			
//	        out.close();
//	        client.close();
//		} catch(IOException e){
//			e.printStackTrace();
//		}
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