package client;

import java.io.*;
import java.util.Arrays;
import java.util.Vector;

import dep.*;

public class ClientRetrieve {
	
	public ClientRetrieve (String s, NetAddr ctrlAddr) {
		fileName = s;
		talkCtrl = new SocketClient(ctrlAddr, -1);
	}
	
	public void retrieve () throws IOException{
//		System.out.println("Testing retrieve files");
		// Get the list of servers first
		getStoringServers();
		
		//testing
//			for (int i = 0; i != storingServers.size(); ++i) {
//				System.out.println(storingServers.get(i));
//			}
			
		// Open a filestream to write to file
		fileOut = new FileOutputStream("/tmp/" + fileName);
		
		// For each server (a chunk), retrieve chunk and write to file
		for (int i = 0; i != storingServers.size(); ++i) {
			writeChunk(i);
		}
		// Close everything
		fileOut.close();
		System.out.println("File " + fileName + " is stored under /tmp");
	}
	
	private void getStoringServers () throws IOException {
		// Write a header to specify file name to retrieve
		HeaderMSG hmsg = new HeaderMSG("RETRIEVE", fileName);
		storingServers = sendController(hmsg);
	}
	
	private Vector<NetAddr> sendController (HeaderMSG hmsg) throws IOException{
		talkCtrl.trySocketClient();
		talkCtrl.trySendHeader(hmsg.headerMsg);
		
		Vector<NetAddr> servers = new Vector<NetAddr>();
		String server_tmp = null;
		while ((server_tmp = talkCtrl.tryGetString())!= null) {
			servers.add(new NetAddr(server_tmp));
		}
		if (servers.size() == 0) {
			System.out.println("Cannnot retrieve servers");
			throw new IOException();
		}
		talkCtrl.closeSocketClient();
		return servers;
	}
	
	private void writeChunk(int chunkID) throws IOException{
		// Try get the chunk
		// If cannot, try connect other two servers to retrieve
		// If cannot, throw IOException
		if (!writeChunk(storingServers.get(chunkID), chunkID)) {
			System.out.println("Problem in retrieving/writing chunk " + chunkID);
			throw new IOException();
//			if (!getRightChunk(chunkID)) {
//				throw new IOException();
//			}
		}
	} 
	
	private boolean writeChunk (NetAddr n, int chunkID) throws IOException {

		// Write a header to specify file name and chunk ID
		SocketClient talkServer = new SocketClient(n, 1);
		talkServer.trySocketClient();
		HeaderMSG hmsg = new HeaderMSG("RETRIEVE", fileName, chunkID);
		talkServer.trySendHeader(hmsg.headerMsg);
		
		// Write received message (mostly 64KB unless end of file)
		byte[] chunk_buffer = new byte[65540]; // buffer to hold the whole chunk and the theoretical chunk size (4B + 64KB)
		int off = 0; // where chunk_buffer stops
//		buffer = new byte[2048]; // buffer to read from socket
		int count = 0;
//		System.out.println("Trying to read byte data");
		while ((count = talkServer.tryGetByte(chunk_buffer, off, chunk_buffer.length-off)) > 0) {
//			System.out.println("Byte data read " + count);
//			for (int i = 0; i != count; ++i) {
//				if (off == 65540) { // Read more than allowed, error happened in server side
//					++off; // 65541 means error in server side, sending more data than allowed
//					break;
//				}
//				chunk_buffer[off] = buffer[i];
//				++off;
//			}
			off += count;
//			if (off == 65540) { //end of buffer, try read one more time
//				byte[] tmp_byte = new byte[1];
//				if (talkServer.tryGetByte(tmp_byte) != -1) {
//					++off;
//					break;
//				}
//			}
		}
		int chunkSzTrue = IntByte.ByteToInt(Arrays.copyOfRange(chunk_buffer, 0, 4));
		System.out.println("read size for chunk " + chunkID + " : " + off);
		System.out.println("actual size for chunk " + chunkID + " : " + chunkSzTrue);
		talkServer.closeSocketClient();
		
		if (chunkSzTrue + 4 != off) { // server problem, ask for a new server for fetching data
			return false;
		} else { // received correct chunk, save it
			fileOut.write(chunk_buffer, 4, off-4);
			return true;
		}
	}
	
	private boolean getRightChunk (int chunkID) throws IOException {
		HeaderMSG hmsg = new HeaderMSG("RETRIEVE_CHUNK", fileName, chunkID);
		talkCtrl.trySocketClient(); // Reconnect to the controller
		Vector<NetAddr> serversThisChunk = sendController(hmsg);
		for (int i = 0; i != serversThisChunk.size(); ++i) {
			if (serversThisChunk.get(i).getFullAddr().equals(storingServers.get(chunkID).getFullAddr()))
				continue;
			if (writeChunk(serversThisChunk.get(i), chunkID)) {
				talkCtrl.closeSocketClient();
				return true;
			}
		}
		talkCtrl.closeSocketClient();
		return false;
	}
	
	private String fileName;
	private SocketClient talkCtrl;
	private Vector<NetAddr> storingServers;
//	private byte[] buffer;
	private FileOutputStream fileOut;
}
