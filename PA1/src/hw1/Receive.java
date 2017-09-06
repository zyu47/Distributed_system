package hw1;
import java.net.*;
//import java.util.*;
import java.io.*;
import java.math.*;
//import hw1.Send.*;

public class Receive {
//	public static void main (String[] args){
	public static void startReceive(String portNo, String collateAddr){
		int port = Integer.parseInt(portNo);
		try{
			receiveMessage r = new receiveMessage(port);
			r.receive(collateAddr);
		}catch(IOException e){
			e.printStackTrace();
		}
	}
}

class receiveMessage{
	private ServerSocket serverSocket;
	public receiveMessage(int port) throws IOException{
		serverSocket = new ServerSocket(port);
		//serverSocket.setSoTimeout(60000);
	}
	public void receive(String collateAddr){
		Socket server = null;
		while(true){
			System.out.println("Waiting for client on port " + 
			        serverSocket.getLocalPort() + "...");
			try{
				server = serverSocket.accept();
			}catch(IOException e){
				e.printStackTrace();
			}
			new readThread(server, collateAddr).start();
		}
	}
}

class readThread extends Thread{
	public readThread(Socket server, String c){
		this.socket = server;
		this.collateAddr = c;
	}
	
	public void run() {
		try{
			DataInputStream in = new DataInputStream(socket.getInputStream());
			for(int i = 0; i != 5; ++i){
				incCounter();
				addInt(in.readInt());
			}
			socket.close();
			System.out.println("Receive << Count: " + getCount() + " ; Value: " + getVal().toString());
			Send.update2Collate(getCount(), getVal(), false, collateAddr);//< update current count and val to collator
		}catch(IOException e){
				e.printStackTrace();
		}
	}
	private static synchronized void incCounter(){
		++count;
	}
	private static synchronized void addInt(int x){
		val = val.add(BigInteger.valueOf(x));
	}
	public static synchronized int getCount(){
		return count;
	}
	public static synchronized BigInteger getVal(){
		return val;
	}
	private static int count;
	private static BigInteger val = BigInteger.valueOf(0);
	private Socket socket;
	private String collateAddr;
}





