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
		this.PORT = port;
		//serverSocket.setSoTimeout(60000);
	}
	public void receive(String collateAddr){
		Socket server = null;
//		new readThread(null, "", true).start();
		new UpdateCollate(collateAddr, 3000, false, PORT).start();
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
	private int PORT;
}

class readThread extends Thread{
	public readThread(Socket server, String c){
		this.socket = server;
//		this.collateAddr = c;
//		this.isUpdateCollate = f;
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
	private static int count = 0;
	private static BigInteger val = BigInteger.valueOf(0);
	private Socket socket;
	private String collateAddr;
	private Boolean isUpdateCollate;
}



