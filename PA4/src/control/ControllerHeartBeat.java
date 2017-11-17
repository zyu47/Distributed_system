package control;

public class ControllerHeartBeat extends Thread {
	private Controller ctrl;
	
	public ControllerHeartBeat (Controller c) {
		ctrl = c;
	}
	
	public void run () {
		while (true) {
			try{
//				Thread.sleep(5000);
				Thread.sleep(30000); 
			} catch(InterruptedException ex){
				ex.printStackTrace();
			}
			ctrl.heartBeat();
//			System.out.println("Controller heartbeat");
		}
	}
}
