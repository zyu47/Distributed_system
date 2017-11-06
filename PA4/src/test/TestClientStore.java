package test;

import client.ClientStore;
import dep.NetAddr;
import dep.ReadControllerAddr;

public class TestClientStore {
	public static void main (String[] args) {
		NetAddr ctrlAddr = ReadControllerAddr.getControllerAddr();
		ClientStore cs = new ClientStore("/s/chopin/k/grad/zhixian/tmp.txt", ctrlAddr);
		cs.store();
	}
}
