package test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import dep.BytesHex;

public class Test {

	public static void main(String[] args) {
//		// TODO Auto-generated method stub
//		byte[] x = BytesHex.HexToBytes("102030A0");
//		for(int i = 0; i != x.length; ++i){
//			System.out.println(x[i]);
//		}
//		ServerSocket serverSocket = null;
//		try{
//			serverSocket = new ServerSocket(6666);
//		}catch(IOException e){
//			e.printStackTrace();
//		}
//		
//		new testThread(serverSocket, false).start();
//		new testThread(serverSocket, true).start();
		Map<String, String> x = new HashMap<String, String>();
		x.put("A", "B");
		x.put("C", "D");
		System.out.println(x);
		x.remove("A");
		System.out.println(x);
	}

}

class testThread extends Thread {
	public testThread(ServerSocket s, boolean flag){
		this.s = s;
		this.flag = flag;
	}
	public void run(){
		if(flag){
			Scanner sc = new Scanner(System.in);
			while(true){
				System.out.print("Continue (y/n) ?:");
				String x = sc.next();
				System.out.println(x);
				if(x.equals("n")){
					System.out.println("Going to close server");
					try{
						s.close();
					}catch (IOException e){
						e.printStackTrace();
					}
				}
			}
		}
		else{
			
			while(true){
				if(s.isClosed()){
					System.out.println("Server is already closed.");
					break;
				}
				try{
					System.out.println("Waiting on port 6666...");
					Socket server = s.accept();
					System.out.println("Connected!");
					server.close();
				}catch(IOException e){
					e.printStackTrace();
				}
			}
		}
	}
	private ServerSocket s;
	private boolean flag;
}