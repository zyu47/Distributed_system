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
		new CommandThread().start(); // Catch the input of the main command such as LEAVE or PRINT
		startServer(); // Start the listening server
		startJoin(); // Joining the network
	}
	
	private static void startJoin() {
		System.out.println("Start to join the network");
		while(true){
			String entryAddr = findEntry();
			if (entryAddr.equals("STOP")){
				System.out.println("Join failed!! Trying again...");
				continue;
			} else {
				joinFromEntry(entryAddr);
				reportJoin();
				// MOVE FILES
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
		
		new Server (serverSocket).start();
	}
	
	private static String findEntry() {
		
		String[] probeMsg = {"JOINING", selfInfo.id, ""};
		String[] receivedMsg = Talk.talkDis(probeMsg);
		
		if(receivedMsg != null){
			return receivedMsg[0]; // return the entry point, first line of received message
		} else {
			System.out.println("Contacting discovery node failed.");
			return "STOP"; // If discovery node is not available, return "STOP"
		}
	}
	
	private static void joinFromEntry(String entryAddr) {
		if(entryAddr.equals("")){
			// Indicates this is the first peer in the whole system, nothing special needs to be done
			System.out.println("I am the first peer in the network!");
			for (int i = 0; i != 16; ++i){
				InfoEntry tmp = new InfoEntry(selfInfo);
				fingerTable.add(tmp);
			}
			predInfo = new InfoEntry(selfInfo);
			printFT();
		}
		else{
			System.out.println("Contacting peer " + entryAddr + " to join the network");
			findSUC(entryAddr);
			notifySUC();
			notifyPRED();
			populateFT();
			updateImpactedFT();
		}
	}

	private static void findSUC (String entryAddr) {
		NetAddr entry_addr = new NetAddr(entryAddr);
		String[] lookupMsg = {"LOOKUP", selfInfo.id, ""};
		String[] receivedMsg = Talk.talkPeer(entry_addr, lookupMsg);
		InfoEntry sucInfo = new InfoEntry(receivedMsg[0], receivedMsg[1]);
		fingerTable.add(sucInfo); //This is the successor, i.e. fingerTable[0]
	}
	
	/**
	 * Contact the successor to notify that this node becomes its predecessor
	 * Receives the original predecessor of my successor, and this will be my predecessor
	 */
	private static void notifySUC() {
		// Update the predecessor of my successor to selfInfo
		String[] notify_MSG = {"UPDATEPRED", selfInfo.id, selfInfo.getNetAddr()};
		String[] receivedMsg = Talk.talkPeer(fingerTable.get(0).addr, notify_MSG);
		if (receivedMsg != null){
			InfoEntry info_tmp = new InfoEntry(receivedMsg[0], receivedMsg[1]);
			predInfo = new InfoEntry(info_tmp);
			System.out.println("Successor is up-to-date!");
		}
	}
	
	/**
	 * Contact my predecessor to notify that this node becomes its successor
	 * Receives confirmation from my predecessor to confirm update
	 */
	private static void notifyPRED() {
		// Update the successor of my predecessor to selfInfo
		String[] notify_MSG = {"UPDATESUC", selfInfo.id, selfInfo.getNetAddr()};
		String[] receivedMsg = Talk.talkPeer(predInfo.addr, notify_MSG);
		if (receivedMsg != null){
//			InfoEntry info_tmp = new InfoEntry(receivedMsg[0], receivedMsg[1]);
//			predInfo = new InfoEntry(info_tmp);
			System.out.println("Predecessor is up-to-date!");
		}
	}
	
	/**
	 * This function updates all impacted finger tables
	 * The algorithm is adapted from the original Chord paper
	 */
	private static void updateImpactedFT() {
		for (int i = 0; i != 16; ++i) {
			System.out.println("-----Updating Entry " + i);
			if (i == 0) {
				updatePredFT ("0", selfInfo.getFullFTEntry());
			}
			else {
				String[] tmp = lookup(ByteMath.minus(selfInfo.id,
						(int)Math.pow(2, i)));
				System.out.println("next update "+tmp[0]);
				notifyToUpdateFT (new NetAddr(tmp[1]), String.valueOf(i), selfInfo.getFullFTEntry());
			}
		}
	}
	
	/**
	 * This function populates the finger table when a peer starts and tries to join the network
	 * 
	 * @param entryAddr
	 */
	private static void populateFT() {
		System.out.println("Start to populate finger table");
		//String rowIDinFT = selfInfo.id; 
		
		// First find my successor, then populate the rest of FT
		for (int i = 1; i !=16; ++i){
			// add 2^(i-1) to the id
			String rowIDinFT = ByteMath.add(selfInfo.id, (int)Math.pow(2, i)); // This is the 16-bit ID in each row in the finger table; Start from the Second row
			
			// Look up to find out the host:port information, and add entry
			String[] lookupMsg = {"LOOKUP", rowIDinFT, ""};
			String[] receivedMsg = Talk.talkPeer(fingerTable.get(i-1).addr, lookupMsg);
			
			if(receivedMsg != null){
				InfoEntry info_tmp = new InfoEntry(receivedMsg[0], receivedMsg[1]);
				fingerTable.add(info_tmp);
				// Print the lookup track
//				System.out.println("Lookup Path is: ");
//				System.out.println("\t" + receivedMsg.get(2));
			} else {
				InfoEntry info_tmp = new InfoEntry(selfInfo);
				fingerTable.add(info_tmp);
			}		
			
			
						
		}
		
		printFT();
	}
	
	/**
	 * This function is used for updating impacted finger table entries when node joining
	 * It contacts the node, which will contact its predecessor to update the information
	 * Note that the contacted node does not update finger table
	 * The predecessor of the updated node updates the finger table and propagate information
	 */
	private static void notifyToUpdateFT (NetAddr target, String entryIndex, String entryInfo) {
		System.out.println("\tUpdating entry " + entryIndex + "of the predecessor of " + target.getFullAddr());
		String[] propagateMsg = {"UPDATEPREDFT", entryIndex, entryInfo};
		Talk.talkPeer(target, propagateMsg);
	}
	
	private static void reportJoin() {
		String[] report_MSG = {"JOINED", selfInfo.id, selfInfo.getNetAddr()};
		Talk.talkDis(report_MSG);
	}
	
	// Following methods are used for manipulating information after receiving a message
	
	/**
	 * return [ID, Address, Path]
	 * @param targetID
	 * @return
	 */
	public static String[] lookup(String targetID) {
//		System.out.println("Start looking up ID: " + targetID + " on peer " + selfInfo.id);
		
		String[] returnValue = new String[3];  // [ID, Address, Path]
		
		// Special cases: targetID == selfID or selfID == successorID
		if (targetID.equals(selfInfo.id) || selfInfo.id.equals(fingerTable.get(0).id)) {
			returnValue[0] = selfInfo.id;
			returnValue[1] = selfInfo.getNetAddr();
			returnValue[2] = selfInfo.id;
			return returnValue; // found!
		}

		// First compare with self and successor
		if (CompareIDrange.inrange(targetID, selfInfo.id, fingerTable.get(0).id) ||
				targetID.equals(fingerTable.get(0).id)) {
			returnValue[0] = fingerTable.get(0).id;
			returnValue[1] = fingerTable.get(0).getNetAddr();
			returnValue[2] = fingerTable.get(0).id;
			return returnValue; // found!
		} 
		
		InfoEntry toContact = null;
		int i = 1;
		for (; i != fingerTable.size(); ++i){
			if (CompareIDrange.inrange(targetID, fingerTable.get(i-1).id, fingerTable.get(i).id) || 
					targetID.equals(fingerTable.get(i).id)) {
				break;
			}
		}
		toContact = fingerTable.get(i - 1);
		
		// start to talk to the next peer and lookup
		String[] contactMsg = {"LOOKUP", targetID, ""}; 
		returnValue = Talk.talkPeer(toContact.addr, contactMsg);
		returnValue[2] = selfInfo.id + "->" + returnValue[2];
		return returnValue;
	}
	
	public static InfoEntry updatePred(String i, String a) {
		InfoEntry tmp = new InfoEntry(predInfo);
		predInfo.id = i;
		predInfo.addr = new NetAddr(a);
		return tmp;
	}
	
	public static void updateSuc(String i, String a) {
		fingerTable.get(0).addr = new NetAddr(a);
		fingerTable.get(0).id = i;
	}
	
	/**
	 * Update the finger table of the predecessor.
	 * entryIndex is the to-be-updated row of the finger table
	 * @param entryIndex
	 * @param entryInfo
	 */
	public static void updatePredFT (String entryIndex, String entryInfo) {
		System.out.println("\tUpdatng entry "+ entryIndex + "of pred " + predInfo.id);
		String[] propagateMsg = {"UPDATEFTENTRY", entryIndex, entryInfo};
		Talk.talkPeer(predInfo.addr, propagateMsg);
	}
	
	public static void updateFTEntry (String entryIndex, String entryInfo) {
		InfoEntry tmpInfo = new InfoEntry(entryInfo);
		System.out.println("\tUpdating entry  "+ entryIndex + " of self " + selfInfo.id + " to " + tmpInfo.id);
		int i = Integer.parseInt(entryIndex);
		// If targetID > selfID && targetID < FT[entryIndex].ID, update and propagate
		// Otherwise do nothing
		if (!selfInfo.id.equals(tmpInfo.id) && 
				CompareIDrange.inrange(tmpInfo.id, selfInfo.id, fingerTable.get(i).id)) {
			System.out.println("\ttmpInfo: " + tmpInfo.id + "\tselfInfo: " + selfInfo.id + "\tsucInfo: " + fingerTable.get(i).id);
			fingerTable.set(i, tmpInfo);
//			if (!predInfo.id.equals(tmpInfo.id)) {
			updatePredFT (entryIndex, entryInfo);
//			}
		}
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


