import java.io.*;
import java.net.*;
import java.util.*;

import static java.lang.System.*;


class Server {

	private static DatagramSocket recSocket;
    private static final int TIMEOUT = 0;

	public static void main(String[] args){

		byte[] data = new byte[512];
        DatagramPacket receivedPacket = new DatagramPacket(data, data.length);
		Scanner scanner = new Scanner(in);
		
		try {
			recSocket = new DatagramSocket(69);
			recSocket.setSoTimeout(TIMEOUT);
		} catch (SocketException e) {
			e.printStackTrace();
			exit(1);
		}

		out.println("Enter Q to terminate Server.");

		while(true){
			try {
				recSocket.receive(receivedPacket);
			} catch (IOException e) {
				e.printStackTrace();
			}

			new ClientConnection(receivedPacket).start();

			if (scanner.hasNext()){
				String input = scanner.next();

				if(input.equalsIgnoreCase("q")){
		    	  out.println("user has terminated the server. Shutting down");
		    	  exit(0);
				}
			}
		}
	}

}
		
	
	

