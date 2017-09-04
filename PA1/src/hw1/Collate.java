package hw1;
import java.net.*;
import java.math.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import dep.ReadSet;

import java.io.*;
import hw1.Receive.*;

public class Collate {
	public static void main (String[] args){
		updateThread.ini_map();
		int port = Integer.parseInt(args[0]);
		ServerSocket serverSocket = null;
		//Receive.receive(port, true, map);
		try{
			serverSocket = new ServerSocket(port);
		}catch(IOException e){
			e.printStackTrace();
		}
		Socket server = null;
		System.out.println("Waiting for client on port " + 
		        serverSocket.getLocalPort() + "...");
		
		while(true){
			try{
				server = serverSocket.accept();
				String r_addr = server.getInetAddress().toString().split("/", 0)[1];
				new updateThread(server, r_addr).start();
			}catch(IOException e){
				e.printStackTrace();
			}
		}
	}
}

class updateThread extends Thread{
	public updateThread(Socket server, String s){
		this.socket = server;
		this.remote_addr = s;
	}
	public void run(){
		try{
			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
			BigInteger[] update_contents = new BigInteger[3];
			for(int i = 0; i != 3; ++i){
				try{
					update_contents[i] = (BigInteger)in.readObject();
				}catch(ClassNotFoundException e){
					e.printStackTrace();
				}
			}
			
			socket.close();
			//System.out.println(remote_addr);
			update_map(remote_addr, update_contents);
			
		}catch(IOException e){
				e.printStackTrace();
		}
	}
	private synchronized void update_map(String ipAddr, BigInteger[] contents){
		//BigInteger flag, BigInteger cnt, BigInteger val
		int sent_flag = (contents[0].intValue() + 1) % 2; //Sent = 0, receive = 1
		if(contents[1].compareTo(result.get(ipAddr).get(sent_flag)) == 1){
			// Only update when received count is larger.
			result.get(ipAddr).set(sent_flag, contents[1]);
			result.get(ipAddr).set(sent_flag + 2, contents[2]);
		}
		System.out.println(result);
		print_sum();
	}
	
	public static void ini_map(){
		ReadSet rs = new ReadSet("./proc_set.txt");
		int server_cnt = rs.servers.size();
		for(int i = 0; i != server_cnt; ++i){
			String addr = rs.servers.elementAt(i);
			Vector<BigInteger> x= new Vector<BigInteger>();
			for(int j = 0; j != 4; ++j){
				x.add(BigInteger.ZERO);
			}
			result.put(addr, x);
		}
		System.out.println(result);
	}
	
	private static void print_sum(){
		Vector<BigInteger> tmp = new Vector<BigInteger>();
		for(int j = 0; j != 4; ++j){
			tmp.add(BigInteger.ZERO);
		}
		for(String s:result.keySet()){
			for(int j = 0; j != 4; ++j){
				tmp.set(j, tmp.get(j).add(result.get(s).get(j)));
			}
		}
		System.out.println(tmp);
	} 
	
	private Socket socket;
	String remote_addr;
	// {ip_addr: [cnt_sent, cnt_received, val_sent, val_received]}
	private static Map<String, Vector<BigInteger>> result = new ConcurrentHashMap<String, Vector<BigInteger>>();
}


