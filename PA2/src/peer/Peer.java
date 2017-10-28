package peer;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

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
		
//		if (args.length <= 2){
//			heartBeatInterval = 2000;
//		} else {
//			heartBeatInterval = Integer.parseInt(args[2]);
//		}
		
		// Start to join the network
		new CommandThread().start(); // Catch the input of the main command such as LEAVE or PRINT
		startServer(); // Start the listening server
		startJoin(); // Joining the network
		new HeartBeat(heartBeatInterval, serverSocket).start(); //Heart beat mechanism
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
			notifySUC(true);
			notifyPRED(true);
			populateFT();
			updateImpactedFT(true);
		}
	}

	private static void findSUC (String entryAddr) {
		NetAddr entry_addr = new NetAddr(entryAddr);
		String[] lookupMsg = {"LOOKUP", selfInfo.id, ""};
		String[] receivedMsg = Talk.talkPeer(entry_addr, lookupMsg);
		System.out.print("Lookup path: ");
		System.out.println(receivedMsg[2]);
		InfoEntry sucInfo = new InfoEntry(receivedMsg[0], receivedMsg[1]);
		fingerTable.add(sucInfo); //This is the successor, i.e. fingerTable[0]
	}
	
	/**
	 * Contact the successor to notify that this node becomes its predecessor
	 * Receives the original predecessor of my successor, and this will be my predecessor
	 */
	private static void notifySUC(boolean join) {
		String[] notify_MSG = {"UPDATEPRED", selfInfo.id, selfInfo.getNetAddr()};
		if (!join) { // Node leaving
			// Update the predecessor of my successor to my predecessor
			notify_MSG[1] = predInfo.id;
			notify_MSG[2] = predInfo.getNetAddr();
		}
		String[] receivedMsg = Talk.talkPeer(fingerTable.get(0).addr, notify_MSG);
		if (join && receivedMsg != null){
			InfoEntry info_tmp = new InfoEntry(receivedMsg[0], receivedMsg[1]);
			predInfo = new InfoEntry(info_tmp);
//			System.out.println("Successor is up-to-date!");
		}
//		if (!join) {
//			System.out.println("Successor is up-to-date! (Leaving)");
//		}
	}
	
	/**
	 * Contact my predecessor to notify that this node becomes its successor
	 * Receives confirmation from my predecessor to confirm update
	 */
	private static void notifyPRED(boolean join) {
		String[] notify_MSG = {"UPDATESUC", selfInfo.id, selfInfo.getNetAddr()};
		if(!join) {
			notify_MSG[1] = fingerTable.get(0).id;
			notify_MSG[2] = fingerTable.get(0).getNetAddr();
		}
		Talk.talkPeer(predInfo.addr, notify_MSG);
//		System.out.println("Predecessor is up-to-date!");
	}
	
	/**
	 * This function updates all impacted finger tables
	 * The algorithm is adapted from the original Chord paper
	 */
	private static void updateImpactedFT(boolean join) {
		for (int i = 0; i != 16; ++i) {
//			System.out.println("-----Updating Entry " + i);
			if (i == 0) {
				if (join) {
					updatePredFT ("0", selfInfo.getFullFTEntry(), true);
				}
//				else {
//					updatePredFT ("0", selfInfo.id + "\t" + fingerTable.get(0).getFullFTEntry(), false);
//				}
			}
			else {
				String[] tmp = lookup(ByteMath.minus(selfInfo.id,
						(int)Math.pow(2, i)-1));
//				System.out.println("Looked up: "+tmp[0]);
				if (join) {
//					if (tmp[0].equals(selfInfo.id)) {
//						updatePredFT ("0",
//								selfInfo.getFullFTEntry(), true);
//					} else {
						notifyToUpdateFT (new NetAddr(tmp[1]), String.valueOf(i),
								selfInfo.getFullFTEntry(), true);
//					}
				} else {
					if (tmp[0].equals(selfInfo.id)) {
						updatePredFT ("0",
								selfInfo.id + "\t" + fingerTable.get(0).getFullFTEntry(), false);
					} else {
						notifyToUpdateFT (new NetAddr(tmp[1]), String.valueOf(i),
								selfInfo.id + "\t" + fingerTable.get(0).getFullFTEntry(), false);
					}
				}
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
	private static void notifyToUpdateFT (NetAddr target, String entryIndex, String entryInfo, boolean join) {
//		System.out.println("\tUpdating entry " + entryIndex + "of the predecessor of " + target.getFullAddr());
		String[] propagateMsg = {"UPDATEPREDFTJOIN", entryIndex, entryInfo};
		if (!join) {
			propagateMsg[0] = "UPDATEPREDFTLEAVE";
		}
		Talk.talkPeer(target, propagateMsg);
	}
	
	private static void reportJoin() {
		String[] report_MSG = {"JOINED", selfInfo.id, selfInfo.getNetAddr()};
		Talk.talkDis(report_MSG);
	}
	
	private static void reportLeave() {
		String[] report_MSG = {"LEFT", selfInfo.id, selfInfo.getNetAddr()};
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
			returnValue[2] = selfInfo.id + "->" + fingerTable.get(0).id;
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

		// start to talk to the next peer and lookup
		toContact = fingerTable.get(i-1);
		String[] contactMsg = {"LOOKUP", targetID, ""}; 
		returnValue = Talk.talkPeer(toContact.addr, contactMsg);
		
		// If the fingerTable fails, route to the successor
		if (returnValue == null) {
			String[] contactMsgBackup = {"LOOKUP", targetID, ""}; 
			returnValue = Talk.talkPeer(fingerTable.get(0).addr, contactMsgBackup);
			returnValue[2] = selfInfo.id + "*->" + returnValue[2];
		} else {		
			returnValue[2] = selfInfo.id + "->" + returnValue[2];
		}
		return returnValue;
	}
	
	public static InfoEntry updatePred(String i, String a) {
		InfoEntry tmp = new InfoEntry(predInfo);
		predInfo.id = i;
		predInfo.addr = new NetAddr(a);
		
		// Move files
		for (String fileID : storedFiles.keySet()) {
			if (CompareIDrange.inrange(fileID, selfInfo.id, predInfo.id) ||
					predInfo.id.equals(fileID)) {
				transferFile (fileID, predInfo.addr);
			}
		}
		
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
	public static void updatePredFT (String entryIndex, String entryInfo, boolean join) {
//		System.out.println("\tUpdatng entry "+ entryIndex + "of pred " + predInfo.id);
		// if the predecessor is the target, do not contact to update FT any more
		InfoEntry tmpInfo = null;
		if (join) {
			tmpInfo = new InfoEntry(entryInfo);
			if (!tmpInfo.id.equals(predInfo.id)){
				String[] propagateMsg = {"UPDATEFTENTRYJOIN", entryIndex, entryInfo};
//				if (!join) {
//					propagateMsg[0] = "UPDATEFTENTRYLEAVE";
//				}
				Talk.talkPeer(predInfo.addr, propagateMsg);
			}
		} else {
//			String[] entryInfoSplit = entryInfo.split("\\t"); 
//			tmpInfo = new InfoEntry(entryInfoSplit[1], entryInfoSplit[2]);
//			if (!tmpInfo.id.equals(predInfo.id)){
				String[] propagateMsg = {"UPDATEFTENTRYLEAVE", entryIndex, entryInfo};
//				if (!join) {
//					propagateMsg[0] = "UPDATEFTENTRYLEAVE";
//				}
				Talk.talkPeer(predInfo.addr, propagateMsg);
//			}
		}
	}
	
	public static void updateFTEntry (String entryIndex, String entryInfo, boolean join) {
		
		int i = Integer.parseInt(entryIndex);
		// If targetID > selfID && targetID < FT[entryIndex].ID, update and propagate
		// Otherwise do nothing
		if (join) { // Node joining
			InfoEntry tmpInfo = new InfoEntry(entryInfo);
//			System.out.println("\tUpdating entry  "+ entryIndex + " of self " + selfInfo.id + " to " + tmpInfo.id);
			if (!selfInfo.id.equals(tmpInfo.id) &&
					(CompareIDrange.inrange(tmpInfo.id, selfInfo.id, fingerTable.get(i).id) || selfInfo.id.equals(fingerTable.get(i).id))) {
			
//				System.out.println("\ttmpInfo: " + tmpInfo.id + "\tselfInfo: " + selfInfo.id + "\tsucInfo: " + fingerTable.get(i).id);
				fingerTable.set(i, tmpInfo);
				updatePredFT (entryIndex, entryInfo, join);
			}
		} else { //Node leaving
			// when node leaving, entryInfo contains [leavingID \t leavingSucID \t leavingSucAddr]
			// First retrieve the information of leaving ID and the information of the sucessor of the leaving node
			String[] entryInfoSplit = entryInfo.split("\\t"); 
			InfoEntry tmpInfo = new InfoEntry(entryInfoSplit[1], entryInfoSplit[2]);
			
			if (!selfInfo.id.equals(entryInfoSplit[0]) &&
					fingerTable.get(i).id.equals(entryInfoSplit[0]) ) {
//				System.out.println("Updating enetry " + entryIndex + " of my (" + selfInfo.id + ") finger table from " + fingerTable.get(i).id + " to " + tmpInfo.id);
				fingerTable.set(i, tmpInfo);
				updatePredFT (entryIndex, entryInfo, join);
			}
		}
	}
	
	//------------------Heartbeat methods---------------------------//
	public static void stabilize() {
		String[] getPredMsg = {"RETURNPRED", "", ""};
		String[] receivedMsg = Talk.talkPeer(predInfo.addr, getPredMsg);
		if (receivedMsg != null) {
			InfoEntry oriSuc = fingerTable.get(0);
			if (CompareIDrange.inrange(receivedMsg[0], selfInfo.id, oriSuc.id)) {
				fingerTable.set(0, new InfoEntry(receivedMsg[0], receivedMsg[1]));
			}
			
			// let my new successor know that I am his new predecessor.
			String[] notify_MSG = {"UPDATEPRED", selfInfo.id, selfInfo.getNetAddr()};
			Talk.talkPeer(fingerTable.get(0).addr, notify_MSG);
			
			// Let my new successor know that its successor is my old successor
			String[] notify_MSG2 = {"UPDATESUC", oriSuc.id, oriSuc.getNetAddr()};
			Talk.talkPeer(fingerTable.get(0).addr, notify_MSG2);
		}
	}
	
	public static void fix_fingers() {
		Random rand = new Random();
		int checkFTIndex =  rand.nextInt(fingerTable.size());
		String[] lookupResult = lookup(ByteMath.add(selfInfo.id, (int)Math.pow(2, checkFTIndex)));
		if (lookupResult != null){
			fingerTable.get(checkFTIndex).id = lookupResult[0];
			fingerTable.get(checkFTIndex).addr = new NetAddr(lookupResult[1]);
		}
	}
	
	public static String[] getPredInfo () {
		String[] info = {"", "", ""};
		info[0] = predInfo.id;
		info[1] = predInfo.getNetAddr();
		return info;
	}
	
	//-----------------File related methods------------------------//
	public static void storeFile (String fileID, String fileName, InputStream socketIn,
			byte[] byteArray, int count, int stoppedAt) {
		// Open an file stream to write byte
		OutputStream fileOut = null;
		
		try {
			fileOut = new FileOutputStream("/tmp/" + fileName);
			
			// First write the rest of unread stream to stream
			fileOut.write(byteArray, stoppedAt, count-stoppedAt);
			// Then read the rest of input stream to complete file transfer
			while ((count = socketIn.read(byteArray)) > 0) {
				fileOut.write(byteArray, 0, count);
			}
			fileOut.close();
			addFileEntry(fileID, fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private static synchronized void removeFile (String fileID) {
		String fileName = storedFiles.get(fileID);
		File file = new File ("/tmp/" + fileName);
		if (file.delete()) {
			System.out.println(fileName + " is successfully deleted");
		} else {
			System.out.println("Failed to delete " + fileName);
		}
	}
	
	private static void transferFile (String fileID, NetAddr targetAddr) {
		String fileName = storedFiles.get(fileID);
		String filePath = "/tmp/" + fileName;
		Talk.storeAt(fileID, filePath, targetAddr.getFullAddr());
		// If sending to self machine (not node), do not remove file because of potential writing/reading conflict
//		System.out.println("transfer to address " + targetAddr.host);
//		System.out.println("self host is: " + selfInfo.addr.host);
		if (!targetAddr.host.equals(selfInfo.addr.host)) {
			removeFile(fileID);
		}
		removeFileEntry (fileID);
	}
	
	private static synchronized void addFileEntry (String fileID, String fileName) {
		storedFiles.put(fileID, fileName);
		System.out.println(fileName + " is stored under /tmp");
	}
	
	private static synchronized void removeFileEntry (String fileID) {
		//String fileName = storedFiles.get(fileID);
		storedFiles.remove(fileID);
//		System.out.println(fileName + " is removed from /tmp");
	}
	
	// --------------- Diagnostic information----------------------//
	private static void printFT() {
		System.out.println("The finger table is:");
		for(int i=0; i != fingerTable.size(); ++i){
			System.out.println("\tEntry " + (i+1) +":\t" + fingerTable.get(i).getFullFTEntry());
		}
	}
	
	public static synchronized void printFileList() {
		System.out.println("Stored files are : ");
		for (String val : storedFiles.values()) {
			System.out.println("\t" + val);
		}
	}
	
	public static void printDiag() {
		System.out.println("--------------------Diagnostic Infomation----------------------");
		System.out.println("Self Info: " + selfInfo.id + "\t" + selfInfo.getNetAddr());
		System.out.println("Predecessor: " + predInfo.id + "\t" + predInfo.getNetAddr());
		System.out.println("Successor: " + fingerTable.get(0).id + "\t" + fingerTable.get(0).getNetAddr());
		printFT();
		printFileList();
		System.out.println("-----------------------------END-------------------------------");
	}
	
	//---------------- Leaving the network ------------------------//
	public static void startLeaving() {
		System.out.println("Start to leave the network");
		closeServer();
		leave();
		moveAllFiles();
		reportLeave();
		}
	
	private static void moveAllFiles () {
		boolean lastPeer = selfInfo.id.equals(fingerTable.get(0).id);
		if (lastPeer) {
			System.out.print("I am the last peer in the network! ");
			System.out.println("All files will be lost");
		}
		for (String fileID : storedFiles.keySet()) {
			if (!lastPeer) {
				transferFile (fileID, fingerTable.get(0).addr);
			}
			else {
				removeFile(fileID);
				removeFileEntry (fileID);
			}
		}
	}
	
	private static void leave() {
		if(!selfInfo.id.equals(fingerTable.get(0).id)){
			// Indicates this is the last peer in the whole system, nothing special needs to be done
			System.out.println("Initiating the leaving protocol");
			notifyPRED(false);
			notifySUC(false);
			updateImpactedFT(false);
			}
	}
	
	private static void closeServer() {
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
	private static Map<String, String> storedFiles = new ConcurrentHashMap<String, String>(); //{ID:Name, ...}
	private static ServerSocket serverSocket;
	private static int heartBeatInterval;
}


