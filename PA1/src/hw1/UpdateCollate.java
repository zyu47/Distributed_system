package hw1;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.Socket;

import dep.ReadSet;

public class UpdateCollate extends Thread{
	public UpdateCollate(String s, int w, Boolean f, int p){
		this.collateAddr = s;
		this.waitTime = w;
		this.isSent = f;
		this.PORT = p;
	}
	public void run(){		
		while(true){
			try{
				Thread.sleep(waitTime);//Update received values to collate every 3 seconds;
			}catch(InterruptedException ex){
				ex.printStackTrace();
			}
			if(isSent){
				update2Collate(sendThread.getCount(), sendThread.getVal());
			}
			else{
				update2Collate(readThread.getCount(), readThread.getVal());
			}
		}
	}

	/// pkgCNT: the number of packages sent or received
	/// pkgValue: the summation of sent/received packages
	/// flag: whether this is sent (1) or received(0)
	
	private void update2Collate(int pkgCNT, BigInteger pkgValue){
		ReadSet rs = new ReadSet(collateAddr);
		Socket client = null;
		while(client == null)
		{
			try{
				client = new Socket(rs.servers.elementAt(0), rs.ports.elementAt(0));
			}catch(IOException e){
				System.out.println("Collator is not available now. Retry in 2 seconds");
				client = null;
				try{
					Thread.sleep(2000);
				}catch(InterruptedException ex){
					ex.printStackTrace();
				}
			}
		}
		try{
			ObjectOutputStream outdata = new ObjectOutputStream(client.getOutputStream());
			if(isSent){
				outdata.writeObject(BigInteger.ONE);
			}else{
				outdata.writeObject(BigInteger.ZERO);
			}
			outdata.writeObject(BigInteger.valueOf(pkgCNT));
			outdata.writeObject(pkgValue);
			outdata.writeObject(BigInteger.valueOf(PORT));
			client.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	private String collateAddr;
	private int waitTime;
	private Boolean isSent;
	private int PORT;
}
