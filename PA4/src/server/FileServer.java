package server;

import java.io.*;
import java.net.*;

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
		ctrlSocket = new SocketClient(ctrlAddr, -1);
		freeSpace = new File("/tmp").getFreeSpace();
	}
	
	public void reportJoin () {
		HeaderMSG hmsg = new HeaderMSG("JOINED", selfAddr.getFullAddr(), freeSpace);
		try {
			ctrlSocket.trySendHeader(hmsg.headerMsg);
		} catch (IOException e) {
			System.err.println("Cannot report to controller when joining");
		}
	}
	
	/**
	 * Store metadata to /tmp
	 * @param fileName
	 * @param chunkID
	 */
	public void storeChunkMeta(String fileName, String chunkID) throws IOException{
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
			fileMeta.println(chunkID);
			fileMeta.println(fileName);
			fileMeta.println(CurrentTimeStamp.getFullTimeStamp());
			fileMeta.close();
			fw.close();
	}
	
	public void storeChunk (String fileName, String chunkID, byte[] content, int off, int count) throws IOException {
		FileOutputStream fw = new FileOutputStream("/tmp/" + fileName + "_chunk" + chunkID, true);
		fw.write(content, off, count);
		fw.close();
	}
	
	/**
	 *  This function reads the SHA-1 and actual content for a 8KB slice
	 * @param fin
	 * @param fileName
	 * @param chunkID
	 * @param sha			20 bytes array, to read the SHA-1 value
	 * @param buffer		8KB bytes array, to read the actual slice
	 * @return				<code>true</code> if SHA and the slice content matches
	 */
	public boolean getChunk (FileInputStream fin, String fileName, String chunkID, byte[] sha, byte[] buffer) throws IOException{
		fin.read(sha);
		fin.read(buffer);
		
	}
	
	private NetAddr selfAddr = new NetAddr();
	private NetAddr ctrlAddr;
	private SocketClient ctrlSocket;
	private long freeSpace;
}
