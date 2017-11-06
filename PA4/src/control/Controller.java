package control;

import java.util.concurrent.ConcurrentHashMap;
import java.util.*;
//import java.io.*;

import dep.*;

public class Controller {
	private Vector<NetAddr> servers = new Vector<NetAddr>();
	private Vector<Long> serverFreeSz = new Vector<Long>();
	// {fileName:{chunkID1:[ server1ind, server2ind, server3ind], chunkID2:[], ...}}
	private Map<String, ChunkIDServerInfo> files= new ConcurrentHashMap<String, ChunkIDServerInfo>();
	
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
			if (serverFreeSz.get(i) > 67584) {// if server has more than 66KB
				resInd[found] = i;
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
			int[] serversChunkI = server_list.info.get(i);
			if (serversChunkI != null)	
				res[i] = servers.get(serversChunkI[0]).getFullAddr();
		}
		return res;
	}
	
	public void addServer(String serverFullAddr, String freeSpace) {
		servers.add(new NetAddr(serverFullAddr));
		serverFreeSz.add(Long.parseLong(freeSpace));
	}
	
	public void printDebugInfo () {
		System.out.println("-----------" + CurrentTimeStamp.getPartialTimeStamp());
		System.out.println("Servers: ");
		for (int i = 0; i != servers.size(); ++i) {
			System.out.println("\t" + servers.get(i).getFullAddr());
		}
		System.out.println("Files: ");
		System.out.println(files);
	}
	
	public void testAddr (String[] s) {
		for (int i = 0; i != s.length; ++i) {
			servers.add(new NetAddr(s[i]));
			serverFreeSz.add((long) 1000000);
		}
	}
	
}
