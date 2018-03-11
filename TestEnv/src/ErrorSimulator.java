import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;


public class ErrorSimulator {
	
	private DatagramSocket receiveSocket;
	private Error error;
	private static String hostIP;		
	private static final int PORT_NUMBER = 23;
	private static final int SERVER_PORT_NUMBER = 69;
	private Delay delaysim;
	private LostFile lostSim;
	private Duplicate duplicatesim;
	
	public ErrorSimulator(){
		try {
			receiveSocket = new DatagramSocket(PORT_NUMBER);
		} catch (SocketException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	private synchronized void sendReceive() {
		byte data[] = new byte[512];
		  DatagramPacket receivedPacket = new DatagramPacket(data, data.length);
		  try {
			  receiveSocket.receive(receivedPacket);
		  }catch (IOException e) {
				e.printStackTrace();
			}
	}


public void createSim() {
	
}
	//same method as the one found in the client class
	public static void printPacket(DatagramPacket p)
	{

		byte[] receivedBytes = p.getData();
		System.out.println("Data being sent/received in bytes: ");
		for(byte element : receivedBytes) {
			System.out.print(element);
		}
		System.out.println();
		String receivedString = new String(receivedBytes);
		System.out.println("Data being sent/received: " + receivedString);
		System.out.println("from/to address: " + p.getAddress());
		System.out.println("Port Number: " + p.getPort());




	}

	//same method thats found in the client class
	public static DatagramSocket createSocket(int port)
	{

		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket(port, InetAddress.getByName("127.0.0.1"));
		} catch (SocketException | UnknownHostException e) {
			e.printStackTrace();
		}

		return socket;

	}
	
	public static void main(String args[]) {
		ErrorSimulator errorSim = new ErrorSimulator();
		
		try {
			while (true) {
				errorSim.sendReceive();
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	



}

