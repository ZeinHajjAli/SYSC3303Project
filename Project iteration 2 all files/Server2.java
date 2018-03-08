import java.io.*;
import java.net.*;
import java.util.*;


public class Server2 {
	
	private DatagramPacket receivedPacket;
	private DatagramSocket recSocket;
	byte data[];
	byte sendInfo[];
	String request;
	
	
			
	public void main(){
		
		try {
			recSocket = new DatagramSocket(69);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
			
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);String input ="";
  	      System.out.println("Enter Q to terminate Server.");
  	      
  	  
		
		
		for(;;){
			try {
				recSocket.receive(receivedPacket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			new ClientConnection2(receivedPacket).start();
			if (scanner.hasNext()){
				input = scanner.next();
				if(input.equalsIgnoreCase("q")){
		    	  System.out.println("user has terminated the server. Shutting down");
		    	  System.exit(0);
				}
			
			}
		}
			
			
		}
 	}
		
	
	

