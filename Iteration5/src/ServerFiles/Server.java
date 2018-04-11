package ServerFiles;

import java.io.*;
import java.net.*;
import java.util.*;


import static java.lang.System.*;
/*This class is the Server class which creates client connection threads when a request is received
SYSC 3303 - Group 8
*/
public  class Server {

	private static DatagramSocket recSocket; //socket to receive transmissions
	private static DatagramPacket receivedPacket; //receiving packet to contain the data
	private static final int TIMEOUT = 0; //final integer to contain the timout time
	private static String type = "";
	private static Scanner input1;

	public static void main(String[] args){

		try {
			out.println(InetAddress.getLocalHost()); //prints out the local address that the server is running on in order to easily access this adress
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		byte[] data = new byte[512]; //create a new byte array to store the received datagram packet
		receivedPacket = new DatagramPacket(data, data.length);
		Scanner scanner = new Scanner(in);
		boolean cont = true;


		
		try {
			recSocket = new DatagramSocket(69); //set the port number to receive from
			recSocket.setSoTimeout(TIMEOUT); //set the timeout amount for the receiving socket
		} catch (SocketException e) {
			e.printStackTrace(); //error handling
			exit(1);
		}

		out.println("Enter Q to terminate Server.");

		while(true){
			try {
				recSocket.receive(receivedPacket); //receive the datagram packet on the receiver socket
				System.out.println("Received Packet");
				cont = true;
			} catch (IOException e) {
				if (e.getClass().equals(SocketTimeoutException.class)) {
					cont = false;
				} else {
					e.printStackTrace(); //error handling
				}
			}
			if(cont) {
				new ClientConnection(receivedPacket).start(); //creates a new client connection thread to handle the request
			}
			if (scanner.hasNext()){
				String input = scanner.next(); //access the keyboard input

				if(input.equalsIgnoreCase("q")){ //if q or Q is entered, the server is shutdown
		    	  out.println("user has terminated the server. Shutting down");
		    	  exit(0);
				}
			}
		}
	}

}
		
	
	

