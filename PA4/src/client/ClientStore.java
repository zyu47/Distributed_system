package client;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.lang.Math;

import dep.*;

public class ClientStore {
	
	public ClientStore (String s, NetAddr ctrlAddr) {
		filePath = s;
		talkCtrl = new SocketClient(ctrlAddr, -1);
	}
	
	public void store () {
//		System.out.println("Testing storing...");
		Path path = Paths.get(filePath);
		fileName = path.getFileName().toString();
		
		// Stream reading from file 
		File file = new File(path.toString());
		if (file.exists()) {
	        int fileLen = (int) file.length(); // Get the size of the file
	        int chunkSz = (int) Math.ceil(fileLen*1.0/65536); // The number of 64KB size chunk
	        
	        try {
	        	fileIn = new FileInputStream(file);
		        // For each chunk, get 3 server addresses, and send data 
		        for (int chunkID = 0; chunkID != chunkSz; ++chunkID) {
	    			getStoringServers();
	    			storeToServers(chunkID);
		        }
		        fileIn.close();
		    	System.out.println("Succeed to store file " + fileName);
		    } catch (IOException e) {
		    	System.out.println("Fail to store file " + fileName);
		    }
			talkCtrl.closeSocketClient();
		} else {
			System.out.println("Cannot find local file!");
		}
	}
	
	private void getStoringServers () throws IOException{
//		System.out.println("Testing getting servers...");
		
		HeaderMSG hmsg = new HeaderMSG("STORE");
		talkCtrl.trySendHeader(hmsg.headerMsg);
		for (int i = 0; i != StoreServers.length; ++i) {
			String server_tmp = talkCtrl.tryGetString();
//			System.out.println("Testing received address: " + server_tmp);
			if (server_tmp != null && server_tmp != "NULL")
				StoreServers[i] = new NetAddr(server_tmp);
			else
				throw new IOException();
		}
	}
	
	private void storeToServers (int chunkID) throws IOException{
		// Send header first
//		System.out.println("Testing ClientStore storeToServers...");
		HeaderMSG hmsg = new HeaderMSG("STORE", fileName, chunkID + "_" + StoreServers[1].getFullAddr() + StoreServers[2].getFullAddr());
		
		SocketClient talkServer = new SocketClient(StoreServers[0], 1);
		talkServer.trySendHeader(hmsg.headerMsg);
		
        byte[] bytes = new byte[8192]; // 8K buffer
        
		for (int i = 0; i != 8; ++i) {
			int count = fileIn.read(bytes);
			if (count > 0) {
				if (count != 8192) {
					bytes = Arrays.copyOfRange(bytes, 0, count);
				}
				talkServer.trySendContent(SHA1.hash(bytes)); // Send 20 byte SHA1 hash first
				talkServer.trySendContent(bytes, count);
			}
			else
				break;
		}
		talkServer.closeSocketClient();
	}
	
	private String filePath;
	private String fileName;
	private NetAddr[] StoreServers = new NetAddr[3];
	private SocketClient talkCtrl;
//	private SocketClient talkServer;
	private InputStream fileIn;
}
