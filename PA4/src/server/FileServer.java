package server;

import java.io.*;
import java.net.*;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import dep.*;

public class FileServer {
	
	public FileServer (int p) {
		try {
			selfAddr.host = Inet4Address.getLocalHost().getHostAddress();
		} catch (IOException e) {
			e.printStackTrace();
		}
		selfAddr.port = p;
		ctrlAddr = ReadControllerAddr.getControllerAddr();
//		ctrlSocket = new SocketClient(ctrlAddr, -1);
		freeSpace = new File("/tmp").getFreeSpace();
	}
	
	//*********** Reporting methods
	public void reportJoin () {
		HeaderMSG hmsg = new HeaderMSG("JOINED", selfAddr.getFullAddr(), freeSpace);
		SocketClient ctrlSocket = new SocketClient(ctrlAddr, -1);
		try {			
			ctrlSocket.trySocketClient();
			ctrlSocket.trySendHeader(hmsg.headerMsg);
		} catch (IOException e) {
			System.err.println("Cannot report to controller when joining");
		}
		ctrlSocket.closeSocketClient();
	}
	
	public void reportStore (String fileName, String chunkID) {
		HeaderMSG hmsg = new HeaderMSG("STORED",
				fileName + "*" + chunkID,
				selfAddr.getFullAddr() + "*" + freeSpace);
		SocketClient ctrlSocket = new SocketClient(ctrlAddr, -1);
		try {
			ctrlSocket.trySocketClient();
			ctrlSocket.trySendHeader(hmsg.headerMsg);
		} catch (IOException e) {
//			System.err.println("Cannot report to controller after storing");
			e.printStackTrace();
		}
		ctrlSocket.closeSocketClient();
	}
	
	/**
	 * This is the major heart beat
	 */
	public void sendMajorHB (){ 
		// Establish connection to controller
		SocketClient ctrlSocket = new SocketClient(ctrlAddr, 1);
		ctrlSocket.trySocketClient();
		
		try {
			// Send header first
			HeaderMSG hmsg = new HeaderMSG("MAHEARTBEAT", selfAddr.getFullAddr(), freeSpace);
			ctrlSocket.trySendHeader(hmsg.headerMsg);
			// Send fileName and chunkIDs
			for (Map.Entry<String, Vector<String>> entry : selfFiles.entrySet()) {
				String[] toSend = {"", ""}; //fileName and chunkIDs
				toSend[0] = entry.getKey();
				for (int i = 0; i != entry.getValue().size(); ++i) {
					toSend[1] += entry.getValue().get(i);
					if (i != entry.getValue().size()-1)
						toSend[1] += "*";
				}
				ctrlSocket.trySendHeader(toSend);
			}
			ctrlSocket.closeSocketClient();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Cannot send major heartbeat");
		}
	}
	
	public void sendMinorHB () {
		// Establish connection to controller
		SocketClient ctrlSocket = new SocketClient(ctrlAddr, 1);
		ctrlSocket.trySocketClient();
		
		try {
			// Send header first
			HeaderMSG hmsg = new HeaderMSG("MIHEARTBEAT", selfAddr.getFullAddr(), freeSpace);
			ctrlSocket.trySendHeader(hmsg.headerMsg);
			String received = ctrlSocket.tryGetString();
			System.out.println(received);
			if (received != null && received.equals("SENDMAJORHEARTBEAT"))
				sendMajorHB();
			ctrlSocket.closeSocketClient();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Cannot send minor heartbeat!");
		}
	}
	
	//*********** Chunk methods	
	/**
	 * Store metadata to /tmp
	 * @param fileName
	 * @param chunkID
	 */
	public void storeChunkMeta(String fileName, String chunkSz, String chunkID) throws IOException{
		// Store meta data here
		String metaFileName = "/tmp/" + fileName + "_chunk" + chunkID + "_meta";
		int version = 0;
		if (new File(metaFileName).exists()) {
			// file already exists, get and update chunkID
			BufferedReader br = new BufferedReader(new FileReader(metaFileName));
			version = Integer.parseInt(br.readLine());
			++version;
			br.close();
		}
		FileWriter fw = new FileWriter(metaFileName);
		PrintWriter fileMeta = new PrintWriter(fw);
		fileMeta.println(version);
		fileMeta.println(chunkSz);
		fileMeta.println(chunkID);
		fileMeta.println(fileName);
		fileMeta.println(CurrentTimeStamp.getFullTimeStamp());
		fileMeta.close();
		fw.close();
		freeSpace = new File("/tmp").getFreeSpace();
	}
	
	public void storeChunkNoAppend (String fileName, String chunkID, byte[] content, int off, int len) throws IOException {
		FileOutputStream fw = new FileOutputStream("/tmp/" + fileName + "_chunk" + chunkID);
		fw.write(content, off, len);
		fw.close();
		freeSpace = new File("/tmp").getFreeSpace();
	}
	
	public void storeChunkAppend (String fileName, String chunkID, byte[] content, int len) throws IOException {
		FileOutputStream fw = new FileOutputStream("/tmp/" + fileName + "_chunk" + chunkID, true);
		fw.write(content, 0, len);
		fw.close();
		freeSpace = new File("/tmp").getFreeSpace();
	}
	
	public boolean containsChunk(String fileName, String chunkID) {
		return selfFiles.get(fileName).contains(chunkID);
	}
	
	public void fixSlice (String fileName, String chunkID, int sliceNo) {
		System.out.println("Fixing Slice here");
		// NEED IMPLEMENTATION HERE
//		ctrlSocket.trySocketClient();
	}
	
	//********** Other methods
	public synchronized void addChunkEntry (String fileName, String chunkID) {
		if (!selfFiles.containsKey(fileName)) {
			selfFiles.put(fileName, new Vector<String>());
		}
		selfFiles.get(fileName).add(chunkID);
	}
	
	public void debug () {
		for (Map.Entry<String, Vector<String>> entry : selfFiles.entrySet()) {
			System.out.println(entry.getKey());
			for (int i = 0; i != entry.getValue().size(); ++i) {
				System.out.print("  " + entry.getValue().get(i));
			}
		}
	}
	
	private NetAddr selfAddr = new NetAddr();
	private NetAddr ctrlAddr;
//	private SocketClient ctrlSocket;
	private long freeSpace;
	private Map<String, Vector<String>> selfFiles = new ConcurrentHashMap<String, Vector<String>>();
}
