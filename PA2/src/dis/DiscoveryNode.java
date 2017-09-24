package dis;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
//import java.util.function.BiConsumer;

import dep.*;

public class DiscoveryNode {
	
	public static void main(String[] args){
//		discoveryThread.constructFuncs(); // Populate the message-processing map
		startDiscovery(Integer.parseInt(args[0]));
	}
	
	public static void startDiscovery(int port){
		ServerSocket serverSocket = null;
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
				new discoveryThread(server).start();
			}catch(IOException e){
				e.printStackTrace();
			}
		}
	}
}

class discoveryThread extends Thread{
	public discoveryThread(Socket s){
		server = s;
	}
	
	public void run(){
		// Input messages: 
		//    First Line: task (GETRANDOMPEER, JOINING, JOINED, LEFT)
		//    Second Line: ID (HEX number or "")
		//    Third Line: Address
		BufferedReader in = null;
		String task = "";
		String ID_hex = "";
		String addr = "";
		try{
			in = new BufferedReader(new InputStreamReader(server.getInputStream()));
			task = in.readLine(); 
			ID_hex = in.readLine();
			addr = in.readLine();
		}catch(IOException e){
			e.printStackTrace();
		}
		
		// Process Messages
		if (task.equals("GETRANDOMPEER")){
			sendString(getRandom());
		} else if (task.equals("JOINING")){
			sendString(getRandom());
			while (peerList.containsKey(ID_hex)){
				ID_hex = GetID.getHexID();
			}
			sendString(ID_hex);
		} else if (task.equals("JOINED")){
			addPeer(ID_hex, addr, String.valueOf(peerList.size()));
			printPeerList();
		} else if (task.equals("LEFT")){
			deletePeer(ID_hex);
			printPeerList();
		} else {
			System.out.println("\"" + task + "\" is not a valid command!");
		}
		
		try{
			server.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	private void sendString(String s){
		try{
			PrintWriter out = new PrintWriter(server.getOutputStream());
			out.println(s);
			out.println("");
			out.println("");			
			out.flush();
		}catch (IOException e){
			e.printStackTrace();
		}
	}
	
	/**
	 * This method return the IP address of a random peer
	 * @return
	 */
	public static synchronized String getRandom(){
		if (peerList.size() == 0){
			return "";
		}
		List<String> keyList = new ArrayList<String>(peerList.keySet());
		
		Random rand = new Random();
		String randKey = keyList.get(rand.nextInt(keyList.size()));
		
		return peerList.get(randKey).get(0);
	}
	
	public static synchronized void addPeer(String id, String addr, String nickname){
		String[] info_tmp = {addr, nickname};
		peerList.put(id, new Vector<String>(Arrays.asList(info_tmp)));
	}
	
	public static synchronized void deletePeer(String id){
		peerList.remove(id);
	}
	
	public static synchronized void printPeerList(){
		System.out.println("----------------------------Printing peer list----------------------------");
		System.out.println("ID  \t[     host:port   , nickname]");
		for (String id:peerList.keySet()) {
			System.out.print(id + "\t");
			System.out.println(peerList.get(id));
		}
		System.out.println("--------------------------Done printing peer list-------------------------");
	}
	
	// Information of peerList: {ID_HEX:[address, nickname], ...}
	private static Map<String, Vector<String>> peerList = new ConcurrentHashMap<String, Vector<String>>();
	private Socket server;
}


