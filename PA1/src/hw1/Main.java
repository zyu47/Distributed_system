package hw1;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		for(int i = 0; i != 2; ++i){
			new mainThread(i, args[0]).start();
		}
	}

}

class mainThread extends Thread{
	public mainThread(int f, String p){
		this.flag = f;
		this.port = p;
	}
	public void run(){
		if(flag == 0){
			hw1.Receive.startReceive(port, collateAddr);
		}else{
			hw1.Send.startSend(serverList, collateAddr);
		}
	}
	private int flag; //Send or Receive
	private String port;
	private String serverList = "/s/chopin/k/grad/zhixian/CS555/PA1/server_list.txt";
	private String collateAddr = "/s/chopin/k/grad/zhixian/CS555/PA1/collate_addr.txt";
}