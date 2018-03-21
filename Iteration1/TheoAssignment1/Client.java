//TheodoreHronowsky
//101008637
//SYSC 3303 assignment1


import java.io.*;
import java.net.*;

public class Client {
	
	private DatagramSocket socket;//DatagramSocket to send and receive 
	private DatagramPacket sendPacket, receivePacket;//DatagramPackets to send and receive
	
	
	public Client(){ // The client creates a DatagramSocket that is bound to any available port on the local host
		try{
			socket = new DatagramSocket();
			
		}catch (SocketException socEx){
			socEx.printStackTrace();
			System.exit(1);
		}
	}
	
	
	public void sendReceiveDatagram(){
		byte[] message = new byte[100]; //message to be sent
		byte[] filebyte = new byte[100]; //filename as an array of bytes
		byte[] modebyte = new byte[100]; //mode as an array of bytes
		byte[] packet = new byte[100]; //content of packet as an array of bytes
		String filename, mode; //file name as a string
		int hostPort = 23;//wellknown port: 23 on the intermediate host 
		
		//send 11 packets, 5 read, 5 write and 1 invalid request. 
		//create loop to determine what each packet contains. even is read, odd is write. 
		
		for (int i=0; i<11; i++){//repeat 11 times
			
			message[0] = 0; //both read and write packets start with a 0
			
			if(i%2 ==0){
				message[1] =1; //if the ith time creating a packet is even, it will be a read and the 2nd byte will be 1
			}else{
				message[1] =2; //if the ith time creating a packet is even, it will be a read and the 2nd byte will be 2
			}
			
			if (i==10){
				message[1] = 7; //the last iteration will be an invalid request as per assignment instructions
			}
			
			filename = "filetotest.txt"; //add a file name and convert it into bytes
			filebyte = filename.getBytes();
			
			//in order to coppy the file name(byte array) into the message array for the packet, use arraycopy to copy arguments from source into destination array
			//declaration: arraycopy(source, source position, destination, destination posititon, length)
			System.arraycopy(filebyte, 0, message, 2, filebyte.length); //add filebyte into 2nd element into message
			
			message[filebyte.length + 2] = 0; //add a 0 byte after the filename and 2 bytes 
			
			mode = "ocTEt";//create a mode(octet or netascii), convert it to a byte array and add to message array like done for filename
			modebyte = mode.getBytes();
			System.arraycopy(modebyte, 0, message, filebyte.length+3, modebyte.length);
			
			int lengthmessage = filebyte.length+modebyte.length+4; //determine how long the message packet is with the 4 bytes + the filebyte and modebyte
			
			message[lengthmessage -1] = 0; //add the last 0 byte at the end of the message
			
			
		
		
			try{
				sendPacket = new DatagramPacket(message, lengthmessage, InetAddress.getLocalHost(),hostPort ); //the client sends the packet to a well-known port: 23 on the intermediate host
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			
			System.out.println("Client: Sending Packet " + i); //print out information it has put into packet using code similar to that of SimpleEchoServer example posted on Culearn
			System.out.println("To host: " + sendPacket.getAddress());
		    System.out.println("Destination host port: " + sendPacket.getPort());
		    System.out.println("Length: " + sendPacket.getLength());
		    System.out.println("Containing: ");
		    packet = sendPacket.getData();
		    for(int k =0;k<lengthmessage;i++){ //go through the packet, printing each byte as it parses through the message
		    	System.out.println(k + ": " + packet[k]);
		    }
		    
		    try{ //Client sends the packet to the hostport
		    	socket.send(sendPacket);
		    }catch(IOException e){
		    	e.printStackTrace();
		    	System.exit(1);
		    }
		    
		    packet = new byte[100]; //
		    receivePacket = new DatagramPacket(packet, packet.length);
		    
		    try {//the client waits on its Datagram Socket
				socket.receive(receivePacket);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		    
		    System.out.println("Client: Packet received:");//when the client receives a DatagramPacket from the host, it prints out the information received as well as the byte array
			System.out.println("From host: " + receivePacket.getAddress());
			System.out.println("Host port: " + receivePacket.getPort());
			System.out.println("Length: " + receivePacket.getLength());
			System.out.println("Containing: " );
			
			packet = receivePacket.getData();
			for (int b =0; b<receivePacket.getLength();b++){
				System.out.println(b + ": " + packet[b] );
			}
			
			
			System.out.println();
			
			
	
		}
		
		socket.close();//close the socket
	}

	public static void main(String[] args) {//used to create a client and then instantiate sendReceiveDatagram function 
		Client client1 = new Client();
		
		client1.sendReceiveDatagram();

	}

}
