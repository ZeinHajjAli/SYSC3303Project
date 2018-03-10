//This class will create a thread and simulate a delayed packet
import java.io.IOException;
import java.net.*;
import java.util.concurrent.TimeUnit;

public class Delay extends Thread{
	private DatagramPacket packet;
	private DatagramSocket socket;
	private 	Error error;
	private int serverPort;
	byte data[] = new byte[512];
	public static final int DELAY = 0;
	
	public Delay(Error error, DatagramPacket packet, int serverPort) {
		this.error = error;
		this.packet = packet;
		this.serverPort = serverPort;
		// TODO Auto-generated constructor stub
	}
	@Override
	public void run() {
		 if (error.getPacketType() == PacketType.DATA) {
			 System.out
				.println("Delaying Packet");
		System.out
				.println("Waiting for server to resend Data");
		
		packet = new DatagramPacket( data, data.length);
		try {

			TimeUnit.MILLISECONDS.sleep(DELAY);
			socket.send(packet);
		} catch (InterruptedException ie) {
			System.out.println("Error occured while trying to delay packet.");
		} catch (IOException ioe) {
			System.out.println("Error occured while trying to delay packet.");
		}
		 }
	}
		 
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
