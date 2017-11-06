package client;

import java.io.*;
import java.util.Vector;

import dep.*;

public class ClientRetrieve {
	
	public ClientRetrieve (String s, NetAddr ctrlAddr) {
		fileName = s;
		talkCtrl = new SocketClient(ctrlAddr, -1);
	}
	
	public void retrieve () {
//		System.out.println("Testing retrieve files");
		try {
			// Get the list of servers first
			getStoringServers();
			
			// Open a filestream to write to file
			fileOut = new FileOutputStream("/tmp/" + fileName);
			
			// For each server (a chunk), retrieve chunk and write to file
			for (int i = 0; i != storingServers.size(); ++i) {
				writeChunk(i);
			}
			
			// Close everything
			fileOut.close();
			System.out.println("File " + fileName + " is stored under /tmp");
		} catch (IOException e) {
			System.out.println("Fail to retrieve file " + fileName);
		}
		talkCtrl.closeSocketClient();
	}
	
	private void getStoringServers () throws IOException {
		// Write a header to specify file name to retrieve
		HeaderMSG hmsg = new HeaderMSG("RETRIEVE", fileName);
		talkCtrl.trySendHeader(hmsg.headerMsg);
		
		String server_tmp = talkCtrl.tryGetString();
		while (server_tmp != null) {
			storingServers.add(new NetAddr(server_tmp));
		}
		if (storingServers.size() == 0) {
			System.out.println("Cannnot retrieve servers");
			throw new IOException();
		}
	}
	
	private void writeChunk(int chunkID) throws IOException{
		// Write a header to specify file name and chunk ID
		SocketClient talkServer = new SocketClient(storingServers.get(chunkID), 1);
		HeaderMSG hmsg = new HeaderMSG("RETRIEVE", fileName, chunkID);
		talkServer.trySendHeader(hmsg.headerMsg);
		
		// Write received message (mostly 64KB unless end of file)
		buffer = new byte[8192];
		int count = 0;
		while ((count = talkServer.tryGetByte(buffer)) > 0) {
			fileOut.write(buffer, 0, count);
		}
		talkServer.closeSocketClient();
	} 
	
	private String fileName;
	private SocketClient talkCtrl;
	private Vector<NetAddr> storingServers;
	private byte[] buffer;
	private FileOutputStream fileOut;
}
