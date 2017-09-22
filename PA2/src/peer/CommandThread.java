package peer;

import java.util.*;
import java.net.*;
import java.io.*;

public class CommandThread extends Thread{
	public void run(){
		Scanner sc = new Scanner(System.in);
		while(true){
			String command = sc.next();
			switch (command) {
				case "EXIT":
					// TRANSFER FILES
					// NOTIFY SUCCESSOR
					// NOTIFY OTHER NODES
					// UPDATE DISCOVERY
					
					// close the server
					Peer.closeServer();
					
					break;
					
				case "PRINT":
					Peer.printDiag();
					break;
					
				default:
					System.out.println("Command is not recognized!");
					break;
			}
			if (command.equals("EXIT")) {
				break;
			}
		}
		sc.close();
		System.out.println("Goodbye!");
	}
	
}
