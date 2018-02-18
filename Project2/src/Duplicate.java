/*
 * this class simulates a duplicate packet(error code 6)
 */
import java.io.*;
import java.net.*;
public class Duplicate extends Thread  {
	private DatagramPacket packet;
	private DatagramSocket socket;
	private 	Error error;
	private int serverPort;
	private InetAddress cAddress; // client's address
	byte data[] = new byte[512];
	
	public Duplicate(Error error ,DatagramPacket packet) {
		this.error= error;
		this.packet = packet;
		// TODO Auto-generated constructor stub
	}
	 public void run() {
		 if (error.getPacketType() == PacketType.DATA) {
			 DatagramPacket p = new DatagramPacket(packet.getData(), packet.getLength());
			 // duplicate the packet	and send it again		);
			 packet = new DatagramPacket(p.getData(), p.getLength());
			 System.out.println(" Sending the packet to the client");
			 try {
				 socket.send(packet);
			 } catch (IOException e1) {
			// TODO Auto-generated catch block
				 e1.printStackTrace();
			 }
		
			 //// receive ack packet from client
			 packet = new DatagramPacket( data, data.length);
			 try {
				 socket.receive(packet);
			 } catch (IOException e) {
			// TODO Auto-generated catch block
				 e.printStackTrace();
			 }
			 /// send it to server
			 packet = new DatagramPacket(packet.getData(), packet.getLength(), packet.getAddress(), packet.getPort());
			 try {
				socket.send(packet);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		
		 }
		 
		 
		 else if (error.getPacketType() == PacketType.ACK) {
			 DatagramPacket p = new DatagramPacket(packet.getData(), packet.getLength());
			 /// send ack packet to client
			 packet = new DatagramPacket(p.getData(), p.getLength());
			 try {
				 socket.send(packet);
			 } catch (IOException e1) {
				 // TODO Auto-generated catch block
				 e1.printStackTrace();
			 }		

			 // receive from client
			 try {
				socket.receive(packet);
			 } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			 // send again to server
			 packet = new DatagramPacket(packet.getData(), packet.getLength(), packet.getAddress(), packet.getPort());
			 try {
				 socket.send(packet);
			 } catch (IOException e) {
			// TODO Auto-generated catch block
				 e.printStackTrace();
			 }
			 
		// 
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
