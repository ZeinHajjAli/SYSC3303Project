//TheodoreHronowsky
//101008637
//SYSC 3303 assignment1

import java.net.*;
import java.io.*;



public class Server {
	
	private DatagramSocket receiveSocket, sendSocket;
	private DatagramPacket receivePacket, sendPacket;
	public static final byte[] read = {0, 3, 0, 1};
	public static final byte[] write = {0, 4, 0, 0};
	
	public Server(){
		
		try{ //Server creates a DatagramSocket to use to receive on port 69
			receiveSocket = new DatagramSocket(69);
		}catch(SocketException s){
			s.printStackTrace();
			System.exit(1);
		}
	}

	public void sendreceiveserver(){
		
		byte[] content, out;
		byte[] answer = new byte[4];
		int type, length;
		int j = 0, c=0;
		@SuppressWarnings("unused")
		String filein,modein;
		
		
		for(;;){ //repeat the following "forever"
			
			content = new byte[100];
			receivePacket = new DatagramPacket(content, content.length);
			
			try{
				receiveSocket.receive(receivePacket); //server waits to receive a request
			}catch(IOException e){
				e.printStackTrace();
				System.exit(1);
			}
			
			// server prints out the received datagram.
	         System.out.println("Server: Packet received:");
	         System.out.println("From host: " + receivePacket.getAddress());
	         System.out.println("Host port: " + receivePacket.getPort());
	         System.out.println("Length: " + receivePacket.getLength());
	         System.out.println("Containing: ");

	         // Access the data inside the received datagram and print out bytes
	         content = receivePacket.getData();
	         for (int p=0;p<receivePacket.getLength();p++) {
	            System.out.println(p + ": " + content[p]);
	         }
	         
	         String contentreceived = new String(content,0,receivePacket.getLength());
	         System.out.println(contentreceived);
	         
	         //determine whether the received packet is read, write or invalid
	         //check the first byte, it should be 0 for read or write and if not it is invalid
	         if (content[0]!=0){
	        	 type = 0; // packet received is invalid
	         } else if (content[1]==1){
	        	 type = 1;// packet recaived could be read as the second byte is a 1
	         }
	         else if (content[1]==2){
	        	 type = 2;// packet received could be write as the second byte is a 2
	         }
	         else 
	        	 type = 0; // packet received is invalid
	         
	         
	         length = receivePacket.getLength();
	         
	         if (type != 0){ //now check for 0 byte
	        	 for (j = 2; j<length;j++){
	        		 if (content[j]==0)break;
	        		 }
	        		 
	        		 if (j == length)type = 0; //if it reaches the end wit out 0 byte its invalid
	        		 filein = new String(content,2,j-2); //get filename
	         }
	         
	         
	         if (type != 0){ //now check for 0 byte
	        	 for (c = j+ 1; c<length;c++){
	        		 if (content[c]==0)break;
	        		 }
	        		 
	        		 if (c == length)type = 0;
	        		 modein = new String(content,j,c-j-2);
	         }
	         
	         
	         if (0 != length-1){//this means there is no 0 byte at the end of 
	        	 type = 0;
	         }
	         
	         
	      // Create a response.
	         if (type==1) {
	            answer = read;//if the packet is valid read request, it sends back 0301
	         } else if (type==2) { 
	            answer = write;//if the packet is valid write request, it sends back 0400
	         } else { 
	           throw new IllegalArgumentException("Quit");//throw error
	           
	         }
	         
	         
	         sendPacket = new DatagramPacket(answer, answer.length,
                     receivePacket.getAddress(), receivePacket.getPort());//creates a sendpacket with response

	         System.out.println( "Server: Sending packet:");//server prints out the response packet information 
	         System.out.println("To host: " + sendPacket.getAddress());
	         System.out.println("Destination host port: " + sendPacket.getPort());
	         System.out.println("Length: " + sendPacket.getLength());
	         System.out.println("Containing: ");
	         out = sendPacket.getData();
	         for (int t=0;t<sendPacket.getLength();t++) {
	        	 System.out.println("byte " + t + " " + out[t]);
	         }

	         // Send the datagram packet to the client via a new socket.

	         try {
	        
	        	 sendSocket = new DatagramSocket();//create datagram socket to use for this response
	         } catch (SocketException se) {
	        	 se.printStackTrace();
	        	 System.exit(1);
	         }

	         try {
	        	 sendSocket.send(sendPacket);//sends the packet via the new socket to the port it received the request from
	         } catch (IOException e) {
	        	 e.printStackTrace();
	        	 System.exit(1);
	         }

	         System.out.println("Server: packet sent using port " + sendSocket.getLocalPort()); //server prints out whih port it sent the packet with
	         System.out.println();

	         
	         sendSocket.close(); //server closes the socket it just created
	         
		
		}
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Server server = new Server();
		server.sendreceiveserver();

	}
}
