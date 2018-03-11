import java.io.*;
import java.net.*;
import java.util.*;


public class Server {

	private static DatagramSocket recSocket;
	private static final int TIMEOUT = 2000;
	private static byte data[];
	private static DatagramPacket receivedPacket;
	private static Scanner scanner;
	private static String input;

	public static void main(String[] args){

		boolean cont = true;
		
		try {
			recSocket = new DatagramSocket(69);
			recSocket.setSoTimeout(TIMEOUT);
		} catch (SocketException e) {
			e.printStackTrace();
			System.exit(1);
		}

		data = new byte[512];
		receivedPacket = new DatagramPacket(data, data.length);
		scanner = new Scanner(System.in);
		System.out.println("Enter Q to terminate Server.");

		for(;;){
			try {
				recSocket.receive(receivedPacket);
				cont = true;
			} catch (IOException e) {
				if(e.getClass().equals(SocketTimeoutException.class)){
					cont = false;
				} else {
					e.printStackTrace();
				}
			}
			if(cont) {
				new ClientConnection(receivedPacket).start();
			}
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
		
	
	

