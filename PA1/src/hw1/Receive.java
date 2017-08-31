package hw1;
import java.net.*;
import java.util.*;
import java.io.*;
import java.math.*;
import hw1.Send.*;

public class Receive {
	public static void main (String[] args){
		int port = Integer.parseInt(args[0]);
		try{
			receiveMessage r = new receiveMessage(port);
			r.receive();
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
	public void receive(){
		Socket server = null;
		while(true){
			System.out.println("Waiting for client on port " + 
			        serverSocket.getLocalPort() + "...");
			try{
				server = serverSocket.accept();
			}catch(IOException e){
				e.printStackTrace();
			}
			new readThread(server).start();
		}
	}
}

class readThread extends Thread{
	public readThread(Socket server){
		this.socket = server;
	}
	
	public void run() {
		try{
			DataInputStream in = new DataInputStream(socket.getInputStream());
			for(int i = 0; i != 5; ++i){
				incCounter();
				addInt(in.readInt());
			}
			socket.close();
			System.out.println("Count: " + getCount() + " ; Value: " + getVal().toString());
			Send.update2Collate(getCount(), getVal(), false);//< update current count and val to collator
			//Send.update2Collate(count, val, false); 
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
}





