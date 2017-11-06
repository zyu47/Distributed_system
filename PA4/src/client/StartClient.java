package client;

import java.util.*;

import dep.*;

public class StartClient {
	
	public static void main (String[] args) {
		System.out.println("You can use this client to send/retrieve files");
		
		// First read the controller address
		NetAddr ctrlAddr = ReadControllerAddr.getControllerAddr();
		
		Scanner sc = new Scanner(System.in);
		boolean notExit = true;
		while (notExit) {
			String input = sc.nextLine();
			String[] commands = input.split(" ");
			
			switch (commands[0]) {
			case "Store":
				ClientStore cs = new ClientStore(commands[1], ctrlAddr);
				cs.store();
				break;
				
			case "Retrieve":
				ClientRetrieve cr = new ClientRetrieve(commands[1], ctrlAddr);
				cr.retrieve();
				break;
				
			case "Exit":
				notExit = false;
				break;
				
			default:
				System.out.println("Command is not recognized!");
				break;
			}
		}
		sc.close();
		System.out.println("Goodbye!");
	}
	
}
