package store;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

import dep.*;
import peer.*;

public class StoreData {
	public StoreData(String s, String id) {
		path = Paths.get(s);
		fileID = id;
	}
	
	public StoreData(String s) {
		path = Paths.get(s);
		fileID = GetID.getHexID(path.getFileName().toString());
	}
	
	public void startStore(){ 
		System.out.println("Start to store the file " + path.getFileName());
		while(true){
			String entryAddr = findEntry();
			if (entryAddr.equals("STOP")){
				System.out.println("Contact entry failed!! Trying again...");
				continue;
			} else {
				storeFromEntry(entryAddr);
			}
			break;
		}
	}	

	private String findEntry() {
		String[] probeMsg = {"GETRANDOMPEER", "", ""};
		String[] receivedMsg = Talk.talkDis(probeMsg);
		
		if(receivedMsg != null){
			return receivedMsg[0]; // return the entry point, first line of received message
		} else {
			System.out.println("Contacting discovery node failed.");
			return "STOP"; // If discovery node is not available, return "STOP"
		}
	}
	
	private void storeFromEntry(String entryAddr) {
		if (entryAddr.equals("")) {
			System.out.println("No peer in the network!");
		}
		else {
			System.out.println("Contacting peer " + entryAddr + " to look up the responsible node");
			NetAddr entry_addr = new NetAddr(entryAddr);
			String[] lookupMsg = {"LOOKUP", fileID, ""};
			String[] receivedMsg = Talk.talkPeer(entry_addr, lookupMsg);
			System.out.print("Lookup path: ");
			System.out.println(receivedMsg[2]);
			Talk.storeAt(fileID, path.toString(), receivedMsg[1]);
		}
	}
	
	private Path path = null;
	private String fileID = null;
}
