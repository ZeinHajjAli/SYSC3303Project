import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;


public class Intermediate {

	public static void main(String[] args)
	{

		//Initializes paxckets for sending and receiving from both the server and the client
		byte data[] = new byte[512];
	  DatagramPacket receivedPacket = new DatagramPacket(data, data.length);

	 	byte serverData[] = new byte[512];
	  DatagramPacket serverPacket = new DatagramPacket(serverData, serverData.length);

	   //try/catch block to catch the IOException that may arise from sending and receiving through sockets
		try {

			//Initializes the two DatagramSockets used to send and receive from the client and server respectivly
			DatagramSocket recSocket = createSocket(23);
			DatagramSocket sendRecSocket = new DatagramSocket(80, InetAddress.getByName("127.0.0.1"));
			System.out.println("Opened Sockets");

			//while loop to keep the process running
			while(true) {
				//waits to receive a packet from the client
				recSocket.receive(receivedPacket);
				data = new byte[receivedPacket.getLength()];
        System.arraycopy(receivedPacket.getData(), receivedPacket.getOffset(), data, 0, receivedPacket.getLength());
				receivedPacket.setData(data);
				System.out.println("received");

				receivedPacket.setPort(69);
				printPacket(receivedPacket);
				//sends the packet on to the server
				sendRecSocket.send(receivedPacket);
				//waits to receive a packet from the server
				sendRecSocket.receive(serverPacket);
				serverData = new byte[serverPacket.getLength()];
        System.arraycopy(serverPacket.getData(), serverPacket.getOffset(), serverData, 0, serverPacket.getLength());
				serverPacket.setData(serverData);
				printPacket(serverPacket);
				serverPacket.setPort(24);
				//opens a new socket to send back to the client
				DatagramSocket sendSocket = new DatagramSocket(81, InetAddress.getByName("127.0.0.1"));
				printPacket(serverPacket);
				//sends packet from the server on to the client
				sendSocket.send(serverPacket);
				//closes the recently opened socket
				sendSocket.close();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}





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





}
