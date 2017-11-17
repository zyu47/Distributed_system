package test;

import client.ClientRetrieve;
import client.ClientStore;
import dep.NetAddr;
import dep.ReadControllerAddr;

public class TestClientRetrieve {
	public static void main (String[] args) {
		NetAddr ctrlAddr = ReadControllerAddr.getControllerAddr();
		ClientRetrieve cs = new ClientRetrieve("test1.jpg", ctrlAddr);
		try {
		cs.retrieve();
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		
		cs = new ClientRetrieve("test2.jpg", ctrlAddr);
		try {
		cs.retrieve();
		} catch (Exception e) {
			e.printStackTrace();
			
		}
	}
}
