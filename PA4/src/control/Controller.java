package control;

import java.util.concurrent.ConcurrentHashMap;
import java.io.IOException;
import java.util.*;
//import java.io.*;

import dep.*;

public class Controller {
	private Vector<NetAddr> servers = new Vector<NetAddr>();
	private Vector<Long> serverFreeSz = new Vector<Long>();
	// {fileName:{chunkID1:[ server1ind, server2ind, server3ind], chunkID2:[], ...}}
	private Map<String, ChunkIDServerInfo> files= new ConcurrentHashMap<String, ChunkIDServerInfo>();
	private boolean restarted;
	
	public Controller (boolean r) {
		restarted = r;
//		System.out.println(restarted);
	}
	
	//******* Responses to client
	public String[] getThreeServers () {
		//First create a list of indices for servers, shuffle it
		List<Integer> indices = new ArrayList<Integer> ();
		for (int i = 0; i != servers.size(); ++i) {
			indices.add(i);
		}
		Collections.shuffle(indices);

		// Choose 3 available servers
		int[] resInd = {-1,-1,-1};
		int found = 0;
		for (int i= 0; i != indices.size(); ++i) {
			if (found >= 3)
				break;
			if (serverFreeSz.get(indices.get(i)) > 67584) {// if server has more than 66KB
				resInd[found] = indices.get(i);
				++found;
			}
		}
		
		// Find out the 3 random server addresses
		String[] res = {"NULL", "NULL", "NULL"};
		if (found >= 3) {
			for (int i = 0; i != res.length; ++i)
				res[i] = servers.get(resInd[i]).getFullAddr();
		}
		
		return res;
	}
	
	/**
	 * 
	 * @param fileName		The file to retrieve
	 * @return				String array, server address sorted by chunkID
	 */
	public String[] getStoringServers (String fileName) {
		// Sort chunk ID first
		ChunkIDServerInfo server_list = files.get(fileName);
//		List<Integer> sortedChunkID=new ArrayList<Integer>(server_list.info.keySet());
//		Collections.sort(sortedChunkID);
		
		// pick one server from each chunk id
		String[] res = new String[server_list.info.size()];
		for (int i = 0; i != res.length; ++i) {
//			System.out.println("Testing controller.java 59: " + server_list.info.get(i)[0]);
			int[] serversChunkI = server_list.info.get(i); // The array that contains the server indices storing chunk
			if (serversChunkI != null) {
				res[i] = servers.get(serversChunkI[(int) Math.random()*3]).getFullAddr();
			}
		}
		
//		// Testing
//		for (int i = 0; i != res.length; ++i) {
//			System.out.println(res[i]);
//		}
		
		return res;
	}
	
	public String[] getChunkServers (String fileName, String chunkID) {
		ChunkIDServerInfo server_list = files.get(fileName);
		int[] serversChunkI = server_list.info.get(Integer.parseInt(chunkID)); // The array that contains the server indices storing chunk
		
		String[] res = new String[3];
		for (int i = 0; i != res.length; ++i) {
			res[i] = servers.get(serversChunkI[i]).getFullAddr();
		}
		
		return res;
	}
	
	//********* Responses to file server
	public void addServer(String serverFullAddr, String freeSpace) {
		servers.add(new NetAddr(serverFullAddr));
		serverFreeSz.add(Long.parseLong(freeSpace));
	}

	public synchronized void  updateAll (String fileName, String chunkID, String serverFullAddr, String freeSpace) {
		int serverInd = updateFreeSpace(serverFullAddr, freeSpace);
		updateFiles(fileName, chunkID, serverInd);
	}
	
	public synchronized void processMajorHB (String serverFullAddr, String freeSpace, Vector<String> msg) {
		int serverInd = updateFreeSpace(serverFullAddr, freeSpace);
//		System.out.println(msg.toString());
		// Update file chunk information
		String[] fileNames = new String[msg.size()/2];
		String[] chunkIDs = new String[msg.size()/2];
		for (int i = 0;  i!= msg.size()/2; ++i) {
			fileNames[i] = msg.get(2*i);
			chunkIDs[i] = msg.get(2*i + 1);
		}
		
		for (int i = 0; i != fileNames.length; ++i) {
			String[] chunkID_split = chunkIDs[i].split("\\*");
			for (int j = 0; j != chunkID_split.length; ++j) {
//				System.out.println(fileNames[i] + " " + chunkID_split[j]);
				updateFiles (fileNames[i], chunkID_split[j], serverInd);
			}
		}
	}
	
	/**
	 * 
	 * @param serverFullAddr
	 * @param freeSpace
	 * @return 					Whether a major update is needed (true if needed)
	 */
	public boolean processMinorHB (String serverFullAddr, String freeSpace) {
		int serverInd = findServerIndex(serverFullAddr);
		//If server restart, and cannot find this server, request a major heart beat
		if (serverInd == -1 && restarted) 
			return true;
		updateFreeSpace(serverFullAddr, freeSpace);
		return false;
	}
	
	private synchronized void updateFiles (String fileName, String chunkID, int serverInd) {
//		System.out.println(fileName + " " + chunkID);
		ChunkIDServerInfo tmp = new ChunkIDServerInfo();
		if (files.containsKey(fileName)) 
			tmp = files.get(fileName);
		else
			files.put(fileName, tmp);
		
		int[] serverIndices = {-1,-1,-1};
		int chunkID_int = Integer.parseInt(chunkID);
		if (tmp.info.containsKey(chunkID_int)) 
			serverIndices = tmp.info.get(chunkID_int);
		else 
			tmp.info.put(chunkID_int, serverIndices);
		
		for (int i = 0; i != serverIndices.length; ++i) {
			if ( serverIndices[i] == -1 ) {
				serverIndices[i] = serverInd;
//				System.out.println(serverIndices[i]);
				break;
			} else if (!probeServerForChunk (serverIndices[i], fileName, chunkID)) {
//				System.out.println("-------" + fileName + " " + chunkID + " " + servers.get(serverIndices[i]).getFullAddr());
				serverIndices[i] = serverInd;
				break;
			}
		}
	}
	
	private int findServerIndex (String serverFullAddr) {
		// find the index for this server
		int serverInd = -1;
		for (int i = 0; i != servers.size(); ++i) {
			if (serverFullAddr.equals(servers.get(i).getFullAddr())) {
				serverInd = i;
//						System.out.println("**********************");
				break;
			}
		}
		return serverInd;
	}
	
	/**
	 * 
	 * @return Index of the server in server vector
	 */
	private synchronized int updateFreeSpace (String serverFullAddr, String freeSpace) {

		// first find the index for the server
		int serverInd = findServerIndex(serverFullAddr);
		if (serverInd == -1) {
			serverInd = servers.size();
			servers.add(new NetAddr(serverFullAddr));
			serverFreeSz.add(Long.parseLong(freeSpace));
		} else {
			serverFreeSz.set(serverInd, Long.parseLong(freeSpace));
		}
		
		return serverInd;
	}
	
	//******** Probing file server for information
	public void heartBeat() {
		for (int i = 0; i != servers.size(); ++i) {
			
		}
	}
	
	/**
	 * Check if the specified server contains the specific chunk of a file
	 * @param serverInd
	 * @param fileName
	 * @param chunkID
	 * @return
	 */
	private boolean probeServerForChunk (int serverInd, String fileName, String chunkID) {
		return probeServerForChunk(servers.get(serverInd),fileName, chunkID);
	}
	
	private boolean probeServerForChunk (NetAddr serverAddr, String fileName, String chunkID) {
		SocketClient probeServer = new SocketClient(serverAddr, 1);
		try {
			probeServer.trySocketClient();
			HeaderMSG hmsg = new HeaderMSG("PROBE", fileName, chunkID);
			probeServer.trySendHeader(hmsg.headerMsg);
			String tmp = probeServer.tryGetString();
			if (tmp.equals("YES"))
				return true;
		} catch (IOException e) {
			System.out.println("Probing serve " + serverAddr.getFullAddr() + " failed!");
		}
		return false;
	}
	
	private boolean probeServer (NetAddr serverAddr) {
		return probeServerForChunk (serverAddr, "", "");
	}
	
	public void printDebugInfo () {
		System.out.println("-----------" + CurrentTimeStamp.getPartialTimeStamp());
		System.out.println("Servers: ");
		for (int i = 0; i != servers.size(); ++i) {
			System.out.println("\t" + servers.get(i).getFullAddr());
		}
		System.out.println("Server sizes: ");
		for (int i = 0; i != serverFreeSz.size(); ++i) {
			System.out.println("\t" + serverFreeSz.get(i)/2014/2014 + "MB");
		}
		System.out.println("Files: ");
		for (Map.Entry<String, ChunkIDServerInfo> entry : files.entrySet()) {
			String fileName = entry.getKey();
			System.out.println("\tFileName: " + fileName);
			ChunkIDServerInfo tmp = entry.getValue();
			for (Map.Entry<Integer, int[]> entry2 : tmp.info.entrySet()) {
				Integer chunkID = entry2.getKey();
				System.out.println("\t\tChunkID: " + chunkID);
				System.out.print("\t\t\t");
				int[] tmp2 = entry2.getValue();
				for (int i = 0; i!= tmp2.length; ++i) {
					if (tmp2[i] != -1)
						System.out.print(servers.get(tmp2[i]).host + " ");
					else
						System.out.println("000.00.00.00" + " ");
				}
				System.out.println();
			}
		}
		System.out.println();
	}
	
	public void testAddr (String[] s) {
		for (int i = 0; i != s.length; ++i) {
			servers.add(new NetAddr(s[i]));
			serverFreeSz.add((long) 1000000);
		}
	}
	
}









