package test;

import client.ClientStore;
import dep.NetAddr;
import dep.ReadControllerAddr;

public class TestClientStore {
	public static void main (String[] args) {
		NetAddr ctrlAddr = ReadControllerAddr.getControllerAddr();
		ClientStore cs = new ClientStore("/s/chopin/k/grad/zhixian/test1.jpg", ctrlAddr);
		cs.store();
		cs = new ClientStore("/s/chopin/k/grad/zhixian/test2.jpg", ctrlAddr);
		cs.store();
	}
}
