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
//		int x = 0;
		while(true){
			//updateThread.print_map();
//			System.out.println("Waiting for client on port " + 
//			        serverSocket.getLocalPort() + "...");
			try{
				server = serverSocket.accept();
//				++x;
//				System.out.println(x);
//				server.close();
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
		//this.remote_addr = new String(s);
		this.remote_addr = s;
	}
	public void run(){
		try{
			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
			BigInteger[] update_contents = new BigInteger[3];
			for(int i = 0; i != 3; ++i){
				try{
					update_contents[i] = (BigInteger)in.readObject();
					//System.out.println(update_contents[i]);
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
		int sent = contents[0].intValue(); //Sent = 1, receive = 0
		// {ip_addr: [cnt_sent, cnt_received, val_sent, val_received]}
		//int add_i = index.get(ipAddr);
		if(sent == 1 && (contents[1].compareTo(cnt_sent.get(ipAddr)) == 1)){
			// Only update when received count is larger.
//			records.set(0, contents[1])index.get(ipAddr);
//			map.get(ipAddr).set(2, contents[2]);
//			System.out.println(ipAddr + " send: " + contents[1]);
			cnt_sent.put(ipAddr, contents[1]);
			val_sent.put(ipAddr, contents[2]);
		}
		if(sent == 0 && (contents[1].compareTo(cnt_receive.get(ipAddr)) == 1)){
			// Only update when received count is larger.
//			map.get(ipAddr).set(1, contents[1]);
//			map.get(ipAddr).set(3, contents[2]);
//			System.out.println(ipAddr + " receive: " + contents[1]);
			cnt_receive.put(ipAddr, contents[1]);
			val_receive.put(ipAddr, contents[2]);
		}
		//print_map();
		//System.out.println(map);
		System.out.println(cnt_receive);
		//System.out.println(val_receive);
	}
	
	public static void ini_map(){
		ReadSet rs = new ReadSet("./proc_set.txt");
		int server_cnt = rs.servers.size();
		for(int i = 0; i != server_cnt; ++i){
			//index.put(rs.servers.elementAt(i), i);
			String addr = rs.servers.elementAt(i);
//			Vector<BigInteger> x= new Vector<BigInteger>();
//			for(int j = 0; j != 4; ++j){
			cnt_sent.put(addr, BigInteger.ZERO);
			cnt_receive.put(addr, BigInteger.ZERO);
			val_sent.put(addr, BigInteger.ZERO);
			val_receive.put(addr, BigInteger.ZERO);
//			}
			//records.add(x);
		}
		//System.out.println(records);
	}
//	public static synchronized void print_map(){
//		System.out.println(map);
//	}
	private Socket socket;
	String remote_addr;
	// {ip_addr: [cnt_sent, cnt_received, val_sent, val_received]}
	//private static Map<String, Integer> index = new ConcurrentHashMap<String, Integer>();
//	private static Vector< Vector<BigInteger> > records = new Vector< Vector<BigInteger> >();
	private static Map<String, BigInteger> cnt_sent = new ConcurrentHashMap<String, BigInteger>();
	private static Map<String, BigInteger> cnt_receive = new ConcurrentHashMap<String, BigInteger>();
	private static Map<String, BigInteger> val_sent = new ConcurrentHashMap<String, BigInteger>();
	private static Map<String, BigInteger> val_receive = new ConcurrentHashMap<String, BigInteger>();
}


