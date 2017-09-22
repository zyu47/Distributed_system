package peer;

import java.io.*;
import java.net.*;
import java.util.*;

import dep.*;

public class Peer {

	public static void main(String[] args) {
		selfInfo = new InfoEntry();
		
		//First update the address of this peer 
		try{
			selfInfo.addr.host = Inet4Address.getLocalHost().getHostAddress();
		}catch(IOException e){
			e.printStackTrace();
		}
		selfInfo.addr.port = Integer.parseInt(args[0]);
		
		// Initialize ID; 
		// Generate an ID if it is not specified
		if (args.length <= 1){
			selfInfo.id = GetID.getHexID();
		} else {
			selfInfo.id = args[1];
		}
		
		// Start to join the network
		startJoin(); // Joining the network
		new CommandThread().start(); // Catch the input of the main command such as LEAVE or PRINT
		startServer(); // Start the listening server
	}
	
	private static void startJoin() {		
		while(true){
			String entryAddr = findEntry();
			if (entryAddr.equals("STOP")){
				System.out.println("Join failed!! Trying again...");
				continue;
			} else {
				contactEntry(entryAddr);
			}
			break;
		}
	}
	
	private static void startServer() {
		try {
			serverSocket = new ServerSocket(selfInfo.addr.port);
			System.out.println("Server is started!");
		} catch (IOException e){
			e.printStackTrace();
		}
		
		Socket server = null;
		while(true){
			if (serverSocket.isClosed()) {
				break;
			}
			System.out.println("Waiting for client on port " + 
			        serverSocket.getLocalPort() + "...");
			try{
				server = serverSocket.accept();
			}catch(IOException e){
				if(e.getMessage().equals("Socket closed")){
					System.out.println("Socket closed");
				} else {
					e.printStackTrace();
				}
			}
			new AcceptSocketThread(server).start();
		}
	}
	
	private static String findEntry() {
		
		String[] probeMsg = {"JOINING", selfInfo.id, ""};
		Vector<String> receivedMsg = Talk.talkDis(probeMsg);
		
		if(receivedMsg != null){
			return receivedMsg.get(0); // return the entry point, first line of received message
		} else {
			System.out.println("Contacting discovery node failed.");
			return "STOP"; // If discovery node is not available, return "STOP"
		}
	}
	
	private static void contactEntry(String entryAddr) {
		if(entryAddr.equals("")){
			// Indicates this is the first peer in the whole system, nothing special needs to be done
			System.out.println("I am the first peer in the network!");
			for(int i = 0; i != 16; ++i){
				InfoEntry tmp = new InfoEntry(selfInfo);
				fingerTable.add(tmp);
			}
			predInfo = new InfoEntry(selfInfo);
			printFT();
		}
		else{			
			System.out.println("Contacting peer " + entryAddr + " to join the network");
			populateFT(entryAddr);
			notifySUC();
			//NEEDS TO UPDATE OTHER PEERS
		}
		reportJoin();
	}
	
	/**
	 * This function populates the finger table when a peer starts and tries to join the network
	 * 
	 * @param entryAddr
	 */
	private static void populateFT(String entryAddr) {
		System.out.println("Start to populate finger table");
		String rowIDinFT = selfInfo.id; // This is the 16-bit ID in each row in the finger table; Start from the first row
		
		// First find my successor, then populate the rest of FT
		for (int i = 0; i !=17; ++i){			
			NetAddr entry_addr = new NetAddr(entryAddr);
			// Look up to find out the host:port information, and add entry
			String[] lookupMsg = {"LOOKUP", rowIDinFT, ""};
			Vector<String> receivedMsg = Talk.talkPeer(entry_addr, lookupMsg);
			
			if(receivedMsg != null){
				InfoEntry info_tmp = new InfoEntry(receivedMsg.get(0), receivedMsg.get(1));
				fingerTable.add(info_tmp);
				// Start the next lookup from the last added peer 
				entryAddr = fingerTable.get(i).getNetAddr();
			} else {
				InfoEntry info_tmp = new InfoEntry(selfInfo);
				fingerTable.add(info_tmp);
			}		
			
			// continue here to add 2^(i-1) to the id
			rowIDinFT = ByteMath.add(selfInfo.id, (int)Math.pow(2, i-1)); 
						
		}
		
		printFT();
	}
	
	private static void notifySUC() {
		String[] notify_MSG = {"UPDATEPRED", selfInfo.id, selfInfo.getNetAddr()};
		Vector<String> receivedMsg = Talk.talkPeer(fingerTable.get(0).addr, notify_MSG);
		if (receivedMsg != null){
			InfoEntry info_tmp = new InfoEntry(receivedMsg.get(0), receivedMsg.get(1));
			predInfo = new InfoEntry(info_tmp);
		}
	}
	
	private static void reportJoin() {
		String[] report_MSG = {"JOINED", selfInfo.id, selfInfo.getNetAddr()};
		Talk.talkDis(report_MSG);
	}
	
	/**
	 * return [ID, Address, Path]
	 * @param targetID
	 * @return
	 */
	public static Vector<String> lookup(String targetID) {
		System.out.println("Start looking up ID: " + targetID + " on peer " + selfInfo.id);
		
		Vector<String> returnValue = new Vector<String>();  // [ID, Address, Path]
		
		// Special cases: targetID == selfID or selfID == successorID
		if (targetID.equals(selfInfo.id) || selfInfo.id.equals(fingerTable.get(0).id)) {
			returnValue.add(selfInfo.id);
			returnValue.add(selfInfo.getNetAddr());
			returnValue.add(selfInfo.id);
			return returnValue; // found!
		}

		// First compare with self and successor
		if (targetID.compareTo(selfInfo.id) > 0 && targetID.compareTo(fingerTable.get(0).id) <= 0){
			returnValue.add(fingerTable.get(0).id);
			returnValue.add(fingerTable.get(0).getNetAddr());
			returnValue.add(fingerTable.get(0).id);
			return returnValue; // found!
		} 
		
		// Look through other entries in finger table
		// Find out which node to contact
		String newTargetID = new String(targetID); 
		
		// in case key < currentID, add 2^16 to key
		if (targetID.compareTo(selfInfo.id) < 0) {
			newTargetID = "1" + targetID;
		}
		int targetIntID = Integer.parseInt(newTargetID, 16); //This is the ID that should be compared
		
		InfoEntry toContact = null;
		int i= 1;
		while (i != fingerTable.size()){
			int compareIntID = Integer.parseInt(fingerTable.get(i).id, 16);
			if (fingerTable.get(i).id.compareTo(targetID) < 0) {
				compareIntID += Math.pow(2, 16);
			}
			if (targetIntID <= compareIntID) {
				break;
			}
			++i;
		}
		toContact = fingerTable.get(i - 1);
		
		// start to talk to the next peer and lookup
		String[] contactMsg = {"LOOKUP", targetID, ""}; 
		returnValue = Talk.talkPeer(toContact.addr, contactMsg);
		returnValue.set(2, selfInfo.id + "->" + returnValue.get(2));
		return returnValue;
	}
	
	public static InfoEntry updatePred(String i, String a) {
		InfoEntry tmp = new InfoEntry(predInfo);
		predInfo.id = i;
		predInfo.addr = new NetAddr(a);
		return tmp;
	}
	
	public static void printFT() {
		System.out.println("The finger table is:");
		for(int i=0; i != fingerTable.size(); ++i){
			System.out.println("\tEntry " + (i+1) +":\t" + fingerTable.get(i).getFullFTEntry());
		}
	}
	
	public static void printDiag() {
		System.out.println("--------------------Diagnostic Infomation----------------------");
		System.out.println("Self Info: " + selfInfo.id + "\t" + selfInfo.getNetAddr());
		System.out.println("Predecessor: " + predInfo.id + "\t" + predInfo.getNetAddr());
		System.out.println("Successor: " + fingerTable.get(0).id + "\t" + fingerTable.get(0).getNetAddr());
		printFT();
		// FILE MANAGED BY THIS SERVER
		System.out.println("-----------------------------END-------------------------------");
	}
	
	public static void closeServer() {
		try {
			serverSocket.close();
			System.out.println("Server is now closed!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static InfoEntry selfInfo; 
	private static InfoEntry predInfo;
	private static Vector<InfoEntry> fingerTable = new Vector<InfoEntry>();
	private static ServerSocket serverSocket;
}


