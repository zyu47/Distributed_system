package hw1;
import java.net.*;
import java.util.*;

import dep.ReadSet;

import java.io.*;
import java.math.BigInteger;

public class Send {
//	public static void main(String[] args){
	public static void startSend(String serverList, String collateAddr){
		try{
			localAddr = Inet4Address.getLocalHost().getHostAddress();
		}catch(IOException e){
			e.printStackTrace();
		}
		sendAll(serverList);
		System.out.println("Send >> Count: " + count + " ; Value: " + val.toString());
		update2Collate(collateAddr);
	}
	
	public static void sendAll(String serverList){
		ReadSet rs = new ReadSet(serverList);
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
			sendMessage sm = null;
			while(sm == null)
			{
				try{
					sm = new sendMessage(serverAddr, serverPort);
				}catch(IOException e){
					System.out.println(serverAddr + " is not available now. Retry in 2 seconds");
					sm = null;
//					java.util.concurrent.TimeUnit.SECONDS.sleep(2);
					try{
						Thread.sleep(2000);
					}catch(InterruptedException ex){
						ex.printStackTrace();
					}
				}
			}
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
	public static void update2Collate(String collateList){
		update2Collate(count, val, true, collateList);
	}
	
	public static void update2Collate(int pkgCNT, BigInteger pkgValue, Boolean sent, String collateAddr){
		ReadSet rs = new ReadSet(collateAddr);
		sendMessage sm = null;
		while(sm == null)
		{
			try{
				sm = new sendMessage(rs.servers.elementAt(0), rs.ports.elementAt(0));
			}catch(IOException e){
				System.out.println("Collator is not available now. Retry in 2 seconds");
				sm = null;
//				java.util.concurrent.TimeUnit.SECONDS.sleep(2);
				try{
					Thread.sleep(2000);
				}catch(InterruptedException ex){
					ex.printStackTrace();
				}
			}
		}
//		sendMessage sm = null;
//		try{
//			sm = new sendMessage(rs.servers.elementAt(0), rs.ports.elementAt(0));
//		}catch(IOException e){
//			e.printStackTrace();
//		}
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
	public sendMessage(String servername, int port) throws IOException{
		s_name = servername;
		p_no = port;
//		try{
			client = new Socket(s_name, p_no);
			outdata = new DataOutputStream(client.getOutputStream());
//		}catch(IOException e){
//				e.printStackTrace();
//		}
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

