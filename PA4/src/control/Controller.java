package control;

import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;
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
	private Set<Integer> downServerInds = new HashSet<Integer>();
	
	public Controller (boolean r) {
		restarted = r;
//		System.out.println(restarted);
	}
	
	//******* Responses to client
	public String[] getThreeServers () {
		
		int[] resInd = getRandAvailServers(3);
		
		// Find out the 3 random server addresses
		String[] res = {"NULL", "NULL", "NULL"};
		for (int i = 0; i != res.length; ++i) 
			if (resInd[i] != -1)
				res[i] = servers.get(resInd[i]).getFullAddr();
		
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
				Random randomno = new Random();
				res[i] = servers.get(serversChunkI[randomno.nextInt(3)]).getFullAddr();

//				// REMEMBER TO DELETE
//				for (int j = 0; j != 3; ++j) {
//					if (servers.get(serversChunkI[j]).getFullAddr().equals("129.82.44.134:6666")) {
//						res[i] = "129.82.44.134:6666";
//						break;
//					}
//				}
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
			if (serversChunkI[i] != -1)
				res[i] = servers.get(serversChunkI[i]).getFullAddr();
			else
				res[i] = "";
		}
		
		return res;
	}
	
	private int[] getRandAvailServers (int n) {
		//First create a list of indices for servers, shuffle it
		List<Integer> indices = new ArrayList<Integer> ();
		for (int i = 0; i != servers.size(); ++i) {
			indices.add(i);
		}
		Collections.shuffle(indices);

		// Choose n available servers
		int[] resInd = new int[n];
		for (int i = 0; i != n; ++i)
			resInd[i] = -1;
		
		int found = 0;
		for (int i= 0; i != indices.size(); ++i) {
			if (found >= n)
				break;
			if (serverFreeSz.get(indices.get(i)) > 67584) {// if server has more than 66KB
				resInd[found] = indices.get(i);
				++found;
			}
		}
		
		return resInd; // int array holding the server indices
	}
	
	//********* Responses to file server
	public void addServer(String serverFullAddr, String freeSpace) {
		int serverInd = findServerIndex(serverFullAddr);
		if (serverInd == -1) {
			servers.add(new NetAddr(serverFullAddr));
			serverFreeSz.add(Long.parseLong(freeSpace));
		} else {
			serverFreeSz.set(serverInd, Long.parseLong(freeSpace));
			if (downServerInds.contains(serverInd))
				downServerInds.remove(serverInd);
		}
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
			if (IntStream.of(serverIndices).anyMatch(x -> x == serverInd)) {
				continue;
			}
			if ( serverIndices[i] == -1) {
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
	
	/**
	 * 
	 * @param serverAddr
	 * @return				false if server is down or cannot be connected
	 */
	private boolean probeServer (NetAddr serverAddr) {
		return probeServerForChunk (serverAddr, "", "");
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
			probeServer.closeSocketClient();
		} catch (Exception e) {
			System.out.println("Probing serve " + serverAddr.getFullAddr() + " failed!");
		}
		return false;
	}
	
	//*********** deal with server failure
	public void heartBeat() {
//		System.out.println("Controller heartbeat");
		for (int i = 0; i != servers.size(); ++i) {
//			System.out.println(servers.get(i).getFullAddr() + " : " + probeServer(servers.get(i)));
			if (downServerInds.contains(i))
				continue;
			if (!probeServer(servers.get(i))) { // server failure
				serverFreeSz.set(i, (long) -1); // -1 in free space meaning server not available
				fixServerFailure(i);
				downServerInds.add(i);
			}
		}
	}
	
	private void fixServerFailure (int downServerInd) {
		System.out.println("Server " + servers.get(downServerInd).getFullAddr() + " is down. Try fixing...");
		// Iterate through all the files/chunks
		// Copy the chunk to other servers if the chunk was saved on the down server
		for (Map.Entry<String, ChunkIDServerInfo> entryMain : files.entrySet()) {
			String fileName = entryMain.getKey();
			for (Map.Entry<Integer, int[]> entrySub : entryMain.getValue().info.entrySet()) {
				int chunkID = entrySub.getKey();
				int[] serverInds = entrySub.getValue();
				int fromServerInd = 0; // This holds the index of server which will serve as the source of copy
				boolean containFailedServer = false; // This is the flag for whether the down server contains a copy of this chunk
				for (int i = 0; i != serverInds.length; ++i) {
					if (serverInds[i] != downServerInd)
						fromServerInd = serverInds[i];
					else { // we do find a down server holding this piece of chunk
						serverInds[i] = -1;
						containFailedServer = true;
					}				
				}
				if (containFailedServer)
					copyChunk(fromServerInd, fileName, chunkID);
			}
		}
	}
	
	private void copyChunk (int fromServerInd, String fileName, int chunkID) {
		// First find a server to hold the copy
		// The chunk cannot be saved on the server which already holds the copy
		// The chunk server cannot be dead
		// First create a list of indices for servers, shuffle it
//		System.out.println("copyChunk " + fileName + chunkID + " from" + servers.get(fromServerInd).getFullAddr());
		List<Integer> indices = new ArrayList<Integer> ();
		for (int i = 0; i != servers.size(); ++i) {
			indices.add(i);
		}
		Collections.shuffle(indices);
		
		
		int toServerInd = -1;
		for (int i= 0; i != indices.size(); ++i) {
//			System.out.println(".........Testing " + i + " " + servers.get(indices.get(i)).getFullAddr());
			if (serverFreeSz.get(indices.get(i)) > 67584 &&    // if server has more than 66KB
					!probeServerForChunk(indices.get(i), fileName, "" + chunkID)) { // and server does not already contain chunk
				toServerInd = indices.get(i);
				break;
			}
		}
		
		if (toServerInd == -1) {
			System.out.println("Cannot find available server");
			return;
		}
		try {
			System.out.println("Copying "+fileName + chunkID + " from " + servers.get(fromServerInd).getFullAddr() + " to " + servers.get(toServerInd).getFullAddr());
			SocketClient talkServer = new SocketClient(servers.get(fromServerInd), 1);
			HeaderMSG hmsg = new HeaderMSG("COPY", fileName,
					chunkID + "*" + servers.get(toServerInd).getFullAddr());
			talkServer.trySocketClient();
			talkServer.trySendHeader(hmsg.headerMsg);
			talkServer.closeSocketClient();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//*********** self use
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

	public void printDebugInfoSimp () {
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
				System.out.print("\t\tChunk " + chunkID + ": \t");
				int[] tmp2 = entry2.getValue();
				for (int i = 0; i!= tmp2.length; ++i) {
					System.out.print(tmp2[i] + " ");
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









