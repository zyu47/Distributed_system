package control;

public class ControllerHeartBeat extends Thread {
	private Controller ctrl;
	
	public ControllerHeartBeat (Controller c) {
		ctrl = c;
	}
	
	public void run () {
		try{
			Thread.sleep(5000);
//			Thread.sleep(30000); 
		} catch(InterruptedException ex){
			ex.printStackTrace();
		}
	}
}
