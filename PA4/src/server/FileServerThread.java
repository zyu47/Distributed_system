package server;

import java.io.*;
import java.net.*;

import dep.*;

public class FileServerThread extends SocketServerThread{
	public FileServerThread (Socket s, FileServer f) {
		super(s);
		fs = f;
	}
	
	public void run() {
//		System.out.println("Testing FileServerThread received header: " + header[0]);
		
		switch (header[0]) {
			case "STORE":
//				System.out.println("Testing fileServer store");				
				store();
				break;
				
			case "RETRIEVE":
//				System.out.println("Testing fileServer retrieve");
				retrieve();
				break;
				
			case "PROBE":
				probe(header[1], header[2]);
				break;
				
			case "COPY":
				String[] chunkID_split = header[2].split("\\*");
				fs.copyToServerMeta(header[1], chunkID_split[0], chunkID_split[1]);
				fs.copyToServerChunk(header[1], chunkID_split[0], chunkID_split[1]);
				break;
				
			case "STOREMETA":
				String[] version_split = header[1].split("\\*");
				String[] chunkID_split2 = header[2].split("\\*");
				fs.storeChunkMeta(version_split[0], version_split[1], chunkID_split2[0], chunkID_split2[1]);
				break;
		}
	}
	
	private void store () {
		String[] chunkID_split = header[2].split("_"); // chunkID, [hostname1, [hostname2]]
		String[] fileName_split = header[1].split("\\*"); // fileName, chunkSz
		try {
			fs.storeChunkMeta(fileName_split[0], fileName_split[1], chunkID_split[0]);
			
			// if chunkID_split contains other addresses, we need to copy to other servers as well
			SocketClient sendNext = null;
			if (chunkID_split.length > 1) {
				HeaderMSG hmsg = new HeaderMSG("STORE", header[1], chunkID_split[0]);
				if (chunkID_split.length == 3) {
					hmsg.headerMsg[2] = hmsg.headerMsg[2] + "_" + chunkID_split[2];
				}
				sendNext = new SocketClient(chunkID_split[1],1);
				sendNext.trySocketClient();
				sendNext.trySendHeader(hmsg.headerMsg);
			}
			
			// Create a new file (may overwrite) and start storing
			// First store what is buffered from reading the header
			fs.storeChunkNoAppend(fileName_split[0], chunkID_split[0], buffer, off, count-off);
			if (chunkID_split.length > 1) {
				sendNext.trySendContent(buffer, off, count-off);
			}
			
			while ((count = in.read(buffer)) > 0) {
				// Any new read from in to buffer should start with 0
				fs.storeChunkAppend(fileName_split[0], chunkID_split[0], buffer, count);
				if (chunkID_split.length > 1) {
					sendNext.trySendContent(buffer, 0, count);
				}
			}
			
			fs.addChunkEntry(fileName_split[0], chunkID_split[0]);
			fs.reportStore(fileName_split[0], chunkID_split[0]);
			
			if (chunkID_split.length > 1)
				sendNext.closeSocketClient();
			
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void retrieve () {		
		try {
			// Read actual chunk size first
			String metaFileName = "/tmp/" + header[1] + "_chunk" + header[2] + "_meta";
			int chunkSzTrue = 0;
			int chunkSzRead = 0;
			
			BufferedReader br = new BufferedReader(new FileReader(metaFileName));
			br.readLine(); // version number
			chunkSzTrue = Integer.parseInt(br.readLine());// Theoretical chunk size 
			br.close();
			sendByte(IntByte.intToByteArray(chunkSzTrue));
			
			FileInputStream fIn = new FileInputStream(new File("/tmp/" + header[1] + "_chunk" + header[2]));
			System.out.println(fIn.available());
			byte[] sha = new byte[20];
//			int slice_no = 0;
			int sha_cnt = 0;
			while ((sha_cnt = fIn.read(sha)) > 0) { // read each SHA-1 byte
				if (sha_cnt != 20) {
					//Something wrong, initialize fixing current slice
					fIn.close();
					closeConnection();
					throw new FileCorruptException("");
				}
				
				count = fIn.read(buffer); // read 8K actual content
//				System.out.println(count);
				if (SHA1.checkHash(sha, buffer, count) && (chunkSzRead + count) <= chunkSzTrue) {
					sendByte();
//					++slice_no;
					chunkSzRead += count;
				} else {
					//Something wrong, initialize fixing current slice
					fIn.close();
					closeConnection();
					throw new FileCorruptException("");
				}
			}
			server.close();
			if (chunkSzTrue != chunkSzRead) {
				//Something wrong, initialize fixing current slice
				fIn.close();
				closeConnection();
				throw new FileCorruptException("");
			}
			fIn.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Wrong retrieving");
		} catch (FileCorruptException f) {
				fs.fixSliceMain(header[1], header[2]);
				closeConnection();
		}
	}
	
	private void probe (String fileName, String chunkID) {
		String[] reportBack = {"YES"};
		if (!fileName.equals("") && !fs.containsChunk(fileName, chunkID))
			reportBack[0] = "NO";
		sendLine(reportBack);
	}
	
	private FileServer fs;
}
