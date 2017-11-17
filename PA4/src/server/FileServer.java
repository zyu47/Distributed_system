package server;

import java.io.*;
import java.net.*;
import java.util.Arrays;
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
			if (received != null && received.equals("SENDMAJORHEARTBEAT")) {
				System.out.println("Send major heart beat upon controller request");
				sendMajorHB();
			}
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
//		System.out.println(metaFileName + " begin");
		int version = 0;
		if (new File(metaFileName).exists()) {
			try {
			// file already exists, get and update chunkID
				BufferedReader br = new BufferedReader(new FileReader(metaFileName));
				version = Integer.parseInt(br.readLine());
				++version;
				br.close();
			} catch (Exception e) {
				// 
			}
		}
//		System.out.println(metaFileName + " end");
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
	
	public void storeChunkMeta(String version, String chunkSz, String chunkID, String fileName) {
		// Store meta data here
		String metaFileName = "/tmp/" + fileName + "_chunk" + chunkID + "_meta";
		try {
			PrintWriter fileMeta = new PrintWriter(new FileWriter(metaFileName));
			fileMeta.println(version);
			fileMeta.println(chunkSz);
			fileMeta.println(chunkID);
			fileMeta.println(fileName);
			fileMeta.println(CurrentTimeStamp.getFullTimeStamp());
			fileMeta.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		if (selfFiles.containsKey(fileName)) {
			return selfFiles.get(fileName).contains(chunkID);
		} else
			return false;
	}
	
	public void fixSliceMain (String fileName, String chunkID) {
		System.out.println("Fixing Slice here " + fileName + " " + chunkID);
		
		try {
			SocketClient ctrlSocket = new SocketClient(ctrlAddr, 1);
			ctrlSocket.trySocketClient();
			HeaderMSG hmsg = new HeaderMSG ("FIX", fileName, chunkID );
			ctrlSocket.trySendHeader(hmsg.headerMsg);
			String[] validServers = new String[3];
			for (int i = 0; i != 3; ++i) {
				validServers[i] = ctrlSocket.tryGetString();
			}
			for (int i = 0; i != 3; ++i) {
				if (!selfAddr.getFullAddr().equals(validServers[i]) && !validServers[i].equals("")) {
					if (fixSliceActual(fileName, chunkID, validServers[i])) {
						System.out.println("Fix from " + validServers[i]);
						break;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Wrong fixing");
		}
	}
	
	private boolean fixSliceActual(String fileName, String chunkID, String serverAddr) {
		try {
			FileOutputStream fileOut = new FileOutputStream("/tmp/" + fileName + "_chunk" + chunkID);
			// Write a header to specify file name and chunk ID
			SocketClient talkServer = new SocketClient(serverAddr, 1);
			talkServer.trySocketClient();
			HeaderMSG hmsg = new HeaderMSG("RETRIEVE", fileName, chunkID);
			talkServer.trySendHeader(hmsg.headerMsg);
			
			// Write received message (mostly 64KB unless end of file)
			byte[] chunk_buffer = new byte[65540]; // buffer to hold the whole chunk and the theoretical chunk size (4B + 64KB)
			int off = 0; // where chunk_buffer stops
			int count = 0;
	//		System.out.println("Trying to read byte data");
			while ((count = talkServer.tryGetByte(chunk_buffer, off, chunk_buffer.length-off)) > 0) 
				off += count;
			
			int chunkSzTrue = IntByte.ByteToInt(Arrays.copyOfRange(chunk_buffer, 0, 4));
			System.out.println("read size for chunk " + chunkID + " : " + off);
			System.out.println("actual size for chunk " + chunkID + " : " + chunkSzTrue);
			talkServer.closeSocketClient();
	
			if (chunkSzTrue + 4 != off) { // server problem, ask for a new server for fetching data
				fileOut.close();
				return false;
			} else { // received correct chunk, save it
				writeSHAAndContent(fileOut, chunk_buffer, 4, off-4);
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private void writeSHAAndContent (FileOutputStream fileOut, byte[] chunk_buffer, int off, int len) throws IOException{
//		byte[] tmp = new byte[8192];
		int sliceNum = (int) len/8192;
		for (int i = 0; i != sliceNum; ++i) {
			byte[] tmp = Arrays.copyOfRange(chunk_buffer, i*8192+off, (i+1)*8192 +off);
			fileOut.write(SHA1.hash(tmp));
			fileOut.write(tmp);
		}
		System.out.println(sliceNum*8192);
		System.out.println(len);
		
		byte[] tmp = Arrays.copyOfRange(chunk_buffer, sliceNum*8192 + off, len + off);
		fileOut.write(SHA1.hash(tmp));
		fileOut.write(tmp);
		fileOut.close();
	}
	
	/**
	 * 
	 * @param fileName
	 * @param chunkID
	 * @param serverAddr		The destination server which needs a copy of the chunk
	 */
	public void copyToServerMeta (String fileName, String chunkID, String serverAddr) {
//		System.out.println("Copy from " + selfAddr.getFullAddr() + " to " + serverAddr);
		// Read from meta file first
		String metaFileName = "/tmp/" + fileName + "_chunk" + chunkID + "_meta";
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(metaFileName));
			String version = br.readLine();
			String chunkSz = br.readLine();
			br.close();
			
			SocketClient talkServer = new SocketClient(serverAddr, 1);
			talkServer.trySocketClient();
			HeaderMSG hmsg = new HeaderMSG ("STOREMETA", version + "*" + chunkSz,
					chunkID + "*" + fileName);
			talkServer.trySendHeader(hmsg.headerMsg);
			talkServer.closeSocketClient();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void copyToServerChunk (String fileName, String chunkID, String serverAddr) {
		try {
			// Read actual chunk size first
			String metaFileName = "/tmp/" + fileName + "_chunk" + chunkID + "_meta";
			int chunkSzTrue = 0;
			int chunkSzRead = 0;
			
			BufferedReader br = new BufferedReader(new FileReader(metaFileName));
			br.readLine(); // version number
			chunkSzTrue = Integer.parseInt(br.readLine());// Theoretical chunk size 
			br.close();
			

			SocketClient talkServer = new SocketClient(serverAddr, 1);
			talkServer.trySocketClient();
			HeaderMSG hmsg = new HeaderMSG ("STORE", fileName + "*" + chunkSzTrue, chunkID);
			talkServer.trySendHeader(hmsg.headerMsg);
			
			FileInputStream fIn = new FileInputStream("/tmp/" + fileName + "_chunk" + chunkID);
			byte[] sha = new byte[20];
//			int slice_no = 0;
			int sha_cnt = 0;
			int count = 0;
			byte[] buffer = new byte[8192];
			while ((sha_cnt = fIn.read(sha)) > 0) { // read each SHA-1 byte
				if (sha_cnt != 20) {
					//Something wrong, initialize fixing current slice
					fIn.close();
					talkServer.closeSocketClient();
					throw new FileCorruptException("");
				}
				
				count = fIn.read(buffer); // read 8K actual content
//				System.out.println(count);
				if (SHA1.checkHash(sha, buffer, count) && (chunkSzRead + count) <= chunkSzTrue) {
					talkServer.trySendContent(sha);
					talkServer.trySendContent(buffer);
//					++slice_no;
					chunkSzRead += count;
				} else {
					//Something wrong, initialize fixing current slice
					fIn.close();
					talkServer.closeSocketClient();
					throw new FileCorruptException("");
				}
			}
			talkServer.closeSocketClient();
			if (chunkSzTrue != chunkSzRead) {
				//Something wrong, initialize fixing current slice
				fIn.close();
				talkServer.closeSocketClient();
				throw new FileCorruptException("");
			}
			fIn.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Wrong copying");
		} catch (FileCorruptException f) {
			fixSliceMain(fileName, chunkID);
		}
	}
	
	//********** Other methods
	public synchronized void addChunkEntry (String fileName, String chunkID) {
		if (!selfFiles.containsKey(fileName)) {
			selfFiles.put(fileName, new Vector<String>());
		}
		if (!selfFiles.get(fileName).contains(chunkID)) {
			selfFiles.get(fileName).add(chunkID);
		}
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
