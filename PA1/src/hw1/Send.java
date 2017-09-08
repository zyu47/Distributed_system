package hw1;
import java.net.*;
import java.util.*;

import dep.ReadSet;

import java.io.*;
import java.math.BigInteger;

public class Send {
//	public static void main(String[] args){
	public static void startSend(String PORT, String serverList, String collateAddr){
		new UpdateCollate(collateAddr, 3000, true, Integer.parseInt(PORT)).start(); //Start a updateCollate thread;
		sendAll(PORT, serverList, 5000);
	}
	
	public static void sendAll(String PORT, String serverList, int rounds){
		/// get current client's IP address
		try{
			localAddr = Inet4Address.getLocalHost().getHostAddress();
		}catch(IOException e){
			e.printStackTrace();
		}
		/// Read server list
		ReadSet rs = new ReadSet(serverList);
		int server_cnt = rs.servers.size();
		
		for(int i = 0; i != rounds; ++i){
			Random x = new Random();
			int rand_ind = x.nextInt(server_cnt);
			String serverAddr = rs.servers.elementAt(rand_ind);
			int serverPort = rs.ports.elementAt(rand_ind);
			int selfPort = Integer.parseInt(PORT);
			
			///If it is a localhost, do not connect
			if(serverAddr.equals(localAddr) && serverPort == selfPort){
				--i;
				System.out.println(i + " Self-connected, ignore");
				continue;
			}
			/// Send five random integers
			sendMessage sm = new sendMessage(serverAddr, serverPort);
			sm.send();
		}
	}
	private static String localAddr;
}

class sendMessage{
	public sendMessage(String s, int p){
		this.servername = s;
		this.port = p;
	}
	public void send(){
		while(client == null)
		{
			try{
				client = new Socket(servername, port);
			}catch(IOException e){
				System.out.println(servername + " is not available now. Retry in 2 seconds");
				client = null;
				try{
					Thread.sleep(2000);
				}catch(InterruptedException ex){
					ex.printStackTrace();
				}
			}
		}
		new sendThread(client).start();
	}
	
	public Socket client = null;
	private String servername;
	private int port;
}

class sendThread extends Thread{
	public sendThread(Socket c){
		this.client = c;
	}
	public void run(){
		try{
			outdata = new DataOutputStream(client.getOutputStream());
			for(int j = 0; j != 5; ++j){
				addInt(send_rand());
				incCounter();
			}
			client.close();
			System.out.println("Receive << Count: " + getCount() + " ; Value: " + getVal().toString());
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public int send_rand(){
		Random r = new Random();
		int sent_no = r.nextInt();
		try{
			outdata.writeInt(sent_no);
			//outdata.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		
		return sent_no;
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
	private Socket client;
	private DataOutputStream outdata;
}
