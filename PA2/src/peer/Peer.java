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
		selfInfo.addr.port = Integer.parseInt(args[1]);
		
		// Initialize ID; 
		// Generate an ID if it is not specified
		if (args.length <= 2){
			selfInfo.id = GetID.getHexID();
		} else {
			selfInfo.id = args[2];
		}
		
		// Start to join the network
		startJoin();
	}
	
	private static void startJoin(){		
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
	
	private static String findEntry(){
		
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
		String rowIDinFT = selfInfo.id; // This is the 16-bit ID in each row in the finger table
		
		System.out.println("Start to populate finger table");
		for (int i = 0; i !=16; ++i){
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

			// CONTINUE HERE TO ADD 2^(I-1) TO THE ID
			rowIDinFT = ByteMath.add(rowIDinFT, (int)Math.pow(2, i));
		}
		System.out.println("The finger table is:");
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
	
	private static void reportJoin(){
		String[] report_MSG = {"JOINED", selfInfo.id, selfInfo.getNetAddr()};
		Talk.talkDis(report_MSG);
	}
	
	public static void printFT(){
		for(int i=0; i != fingerTable.size(); ++i){
			System.out.println("\tEntry " + (i+1) +":\t" + fingerTable.get(i).getFullFTEntry());
		}
	}
	
	private static InfoEntry selfInfo; 
	private static InfoEntry predInfo;
	private static Vector<InfoEntry> fingerTable = new Vector<InfoEntry>();
}


