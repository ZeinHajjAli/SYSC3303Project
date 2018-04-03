package ServerFiles;

import java.io.*;
import java.net.*;
import java.util.*;


import static java.lang.System.*;


public  class Server {

	private static DatagramSocket recSocket;
	private static DatagramPacket receivedPacket;
	private static final int TIMEOUT = 0;
	private static String type = "";
	private static Scanner input1;

	public static void main(String[] args){

		try {
			out.println(InetAddress.getLocalHost());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		byte[] data = new byte[512];
		receivedPacket = new DatagramPacket(data, data.length);
		Scanner scanner = new Scanner(in);
		boolean cont = true;


		
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
				System.out.println("Received Packet");
				cont = true;
			} catch (IOException e) {
				if (e.getClass().equals(SocketTimeoutException.class)) {
					cont = false;
				} else {
					e.printStackTrace();
				}
			}
			if(cont) {
				new ClientConnection(receivedPacket).start();
			}
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
		
	
	

