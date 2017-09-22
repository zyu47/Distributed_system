package test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import dep.BytesHex;

public class Test {

	public static void main(String[] args) {
		ServerSocket serverSocket = null;
		try{
			serverSocket = new ServerSocket(6666);
		} catch (IOException e){
			e.printStackTrace();
		}
		new input(serverSocket).start();
		Socket server = null;
		while(true){
			if (serverSocket.isClosed()) {
				System.out.println("Server shuts down");
				break;
			}
			System.out.println("Waiting for client on port " + 
			        serverSocket.getLocalPort() + "...");
			try{
				server = serverSocket.accept();
			}catch(IOException e){
				System.out.println(e.getMessage());
			}
			// START LISTENING THREAD
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