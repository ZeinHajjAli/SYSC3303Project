//TheodoreHronowsky
//101008637
//SYSC 3303 assignment1

import java.io.*;
import java.net.*;


public class Host {
	
	private DatagramSocket Socket, sendSocket, receiveSocket; //datagram socket to recieve, to send and to receive&send
	private DatagramPacket sendPacket, receivePacket; //datagram packets to recieve and send packets. 
	
	public Host(){
		
		try{
			receiveSocket = new DatagramSocket(23); //Host  creates a DatagramSocket to use to receive (port 23) 
			Socket = new DatagramSocket();// host creates a DatagramSocket to use to send and receive 
		}catch(SocketException se){
			se.printStackTrace();
			System.exit(1);
		}
		
	}
	
	public void receiveAndSend(){
		
		byte[] info, senddata;
		
		for(;;){ // repeat the following "forever"
			
			
			info = new byte[100];
			receivePacket = new DatagramPacket(info, info.length);
			
			try{
				receiveSocket.receive(receivePacket); //the host waits to receive a request
			}catch(IOException r){
				r.printStackTrace();
				System.exit(1);
			}
			
			//Similar to the receiving datagram printout from the SimpleEchoServer example posted on Culearn
			//the host prints out the recieved datagram Packet information it has received
			 System.out.println("Host: Packet received:");
			 System.out.println("From host: " + receivePacket.getAddress());
			 System.out.println("Host port: " + receivePacket.getPort());
			 System.out.println("Length: " + receivePacket.getLength());
			 System.out.println("Containing: " );
				
			 info = receivePacket.getData();
			 for (int b =0; b<receivePacket.getLength();b++){//print the request as bytes
				 System.out.println(b + ": " + info[b] );
			 }
				
			 //now form a string from the byte array recieved in the packet
			 String infoinstr = new String(info,0,receivePacket.getLength());
			 //print out the string aswell
			 System.out.println(infoinstr);
				
			 sendPacket = new DatagramPacket(info, receivePacket.getLength(), receivePacket.getAddress(), 69); //the host forms a packet to send containing exactly what it received
				
			 //the host prints out the information it is about to send
			 System.out.println("Host: sending packet.");
		     System.out.println("To host: " + sendPacket.getAddress());
		     System.out.println("Destination host port: " + sendPacket.getPort());
		     System.out.println("Length: " + sendPacket.getLength());
		     System.out.println("Containing: ");
		     info = sendPacket.getData();
		     for (int j=0;j<sendPacket.getLength();j++) {
		    	 System.out.println( j + ": " + info[j]);
		     }
		        
		        
		     try{
		    	 Socket.send(sendPacket); //host sends this packet on its send/receive socket to port 69 
		     } catch(IOException i){
		    	 i.printStackTrace();
		    	 System.exit(1);
		     }

		     info = new byte[100];
		     receivePacket = new DatagramPacket(info, info.length);
		        
		     System.out.println("WAITING FOR INCOMING PACKET");
		     try{	//Wait until a receive packet is received
		    	 Socket.receive(receivePacket);
		     }catch(IOException i){
		    	 i.printStackTrace();
		    	 System.exit(1);
		     }
		     
		     // print out the received datagram information.
	         System.out.println("Host: Packet received:");
	         System.out.println("From host: " + receivePacket.getAddress());
	         System.out.println("Host port: " + receivePacket.getPort());
	         System.out.println("Length: " + receivePacket.getLength());
	         System.out.println("Containing: ");

	         // Access the data inside the received datagram and print out bytes
	         info = receivePacket.getData();
	         for (int j=0;j<receivePacket.getLength();j++) {
	            System.out.println(j + ": " + info[j]);
	         }
	         
	         sendPacket = new DatagramPacket(info, receivePacket.getLength(),receivePacket.getAddress(), 23); //forms a packet to send back to the host sending the request
	         
	         //the host prints out the information it is about to send
	         System.out.println("Host: sending packet.");
		     System.out.println("To host: " + sendPacket.getAddress());
		     System.out.println("Destination host port: " + sendPacket.getPort());
		     System.out.println("Length: " + sendPacket.getLength());
		     System.out.println("Containing: ");
		     
		     
		     senddata = sendPacket.getData();
		     for (int j=0;j<sendPacket.getLength();j++) {
		    	 System.out.println( j + ": " + senddata[j]);
		     }
	         
		     
		     try{
		    	 sendSocket = new DatagramSocket(); //creates a DatagramSocket to use to send this request 
		     }catch(SocketException s){
		    	 s.printStackTrace();
		    	 System.exit(1);
		     }
		     
		     try{
		    	 sendSocket.send(sendPacket);//Host sends the request
		     }catch(IOException i){
		    	 i.printStackTrace();
		    	 System.exit(1);
		     }
		     
		     System.out.println("Host: packet sent");
		     
		     sendSocket.close();
		        
				
				
				
				
				
				
		}
		
		
		
		
		
		
	}

	public static void main(String[] args) {
		Host host = new Host();
		host.receiveAndSend();
		
		}

}

