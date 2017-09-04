package hw1;
import java.net.*;
import java.util.*;

import dep.ReadSet;

import java.io.*;
import java.math.BigInteger;

public class Send {
	public static void main(String[] args){
		try{
			localAddr = Inet4Address.getLocalHost().getHostAddress();
		}catch(IOException e){
			e.printStackTrace();
		}
		sendAll();
		System.out.println("Count: " + count + " ; Value: " + val.toString());
		update2Collate();
	}
	
	public static void sendAll(){
		ReadSet rs = new ReadSet("./proc_set.txt");
		int server_cnt = rs.servers.size();
//		for(int i = 0; i != 5; ++i){
		for(int i = 0; i != 5000; ++i){
			Random x = new Random();
			int rand_ind = x.nextInt(server_cnt);
			String serverAddr = rs.servers.elementAt(rand_ind);
			int serverPort = rs.ports.elementAt(rand_ind);
			
			//If it is a localhost, do not connect
			if(serverAddr.equals(localAddr)){
				--i;
				System.out.println(i + " Self-connected, ignore");
				continue;
			}
			/// Send five random integers
			sendMessage sm = new sendMessage(serverAddr, serverPort);
			for(int j = 0; j != 5; ++j){
				val = val.add(BigInteger.valueOf(sm.send_rand()));
				++count;
			}
			sm.close();
		}
	}
	
	/// pkgCNT: the number of packages sent or received
	/// pkgValue: the summation of sent/received packages
	/// flag: whether this is sent (1) or received(0)
	public static void update2Collate(){
		update2Collate(count, val, true);
	}
	
	public static void update2Collate(int pkgCNT, BigInteger pkgValue, Boolean sent){
		ReadSet rs = new ReadSet("./collate_addr.txt");
		sendMessage sm = new sendMessage(rs.servers.elementAt(0), rs.ports.elementAt(0));
		try{
			ObjectOutputStream outdata = new ObjectOutputStream(sm.client.getOutputStream());
			if(sent){
				outdata.writeObject(BigInteger.ONE);
			}else{
				outdata.writeObject(BigInteger.ZERO);
			}
			outdata.writeObject(BigInteger.valueOf(pkgCNT));
			outdata.writeObject(pkgValue);
			sm.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	private static int count;
	private static BigInteger val = BigInteger.valueOf(0);
	private static String localAddr;
}

class sendMessage{
	public sendMessage(String servername, int port){
		s_name = servername;
		p_no = port;
		try{
			client = new Socket(s_name, p_no);
			outdata = new DataOutputStream(client.getOutputStream());
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
	
	public void close(){
		try{
			client.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	private String s_name; //< server ip address
	private int p_no; //< server port number
	private DataOutputStream outdata = null;
	public Socket client = null;
}

