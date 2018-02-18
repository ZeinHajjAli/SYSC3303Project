/*
 * This class will create a thread and simulate a lost packet(error code 1)
 */
import java.io.*;
import java.net.*;


public class LostFile extends Thread{
		private DatagramPacket packet;
		private DatagramSocket socket;
		private 	Error error;
		private int serverPort;
		byte data[] = new byte[512];
		
		

	public LostFile(Error error) {
		this.error = error;
		// TODO Auto-generated constructor stub
	}
 public void run() {
	 if (error.getPacketType() == PacketType.DATA) {
		 System.out
			.println("The data from the server is lost");
	System.out
			.println("Waiting for server to resend Data");
	
	packet = new DatagramPacket( data, data.length);
	try {
		socket.receive(packet);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	serverPort = packet.getPort();
	 }
	 
	 else if (error.getPacketType() == PacketType.ACK) {
		 System.out
			.println("The ACK from the client was lost");
	System.out
			.println("Receiving the same data packet from client");

	packet = new DatagramPacket( data, data.length);
	try {
		socket.receive(packet);
	} catch (IOException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}

	// Send that back to server
	try {
		packet = new DatagramPacket(packet.getData(),
				packet.getLength(), InetAddress.getByName("127.0.0.1"), serverPort);
	} catch (UnknownHostException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	try {
		socket.send(packet);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	// get the ACK from the server
	packet = new DatagramPacket( data, data.length);
	try {
		socket.receive(packet);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	 }
	 DatagramPacket sendDp = null;
	try {
		sendDp = new DatagramPacket(
					packet.getData(), packet.getLength(), InetAddress.getByName("127.0.0.1"),
					serverPort);
	} catch (UnknownHostException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
		try {
			socket.send(sendDp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 }
}
