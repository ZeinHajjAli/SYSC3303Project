import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Server {

	public static void main(String[] args)
	{

		//Initializes packet to receive from the intermediate
		byte data[] = new byte[100];
	  DatagramPacket receivedPacket = new DatagramPacket(data, data.length);

	    //try/catch block to catch any exceptions made by sending and receiving through sockets
		try {

			DatagramSocket recSocket = createSocket(69);
			System.out.println("Opened Sockets (Server)");
			//Initialize a byte array with the base pattern
			byte[] sendInfo = {0,0,0,0};


			while(true) {
				//resets the byte array
				sendInfo[1] = 0;
				sendInfo[3] = 0;
				recSocket.receive(receivedPacket);
				data = new byte[receivedPacket.getLength()];
        System.arraycopy(receivedPacket.getData(), receivedPacket.getOffset(), data, 0, receivedPacket.getLength());
				receivedPacket.setData(data);
				int sendPort = receivedPacket.getPort();
				System.out.println("received");
				printPacket(receivedPacket);
				//calls validatePacket method to find what pattern to use
				String packetInfo = validatePacket(receivedPacket);

				if(packetInfo.compareTo("read") == 0) {
					sendInfo[1] = 3;
					sendInfo[3] = 1;
				}
				else if(packetInfo.compareTo("write") == 0) {
					sendInfo[1] = 4;
				}

				DatagramPacket sendPacket = new DatagramPacket(sendInfo, sendInfo.length, new InetSocketAddress("localhost",sendPort));
				printPacket(sendPacket);
				DatagramSocket sendSocket = new DatagramSocket(96, InetAddress.getByName("127.0.0.1"));
				sendSocket.send(sendPacket);
				sendSocket.close();

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static String validatePacket(DatagramPacket p) throws Exception
	{
		byte[] receivedBytes = p.getData();
		if(receivedBytes[0] == 0) {
			if(receivedBytes[receivedBytes.length-1] == 0) {
				if(receivedBytes[1] == 1) {
					return "read";
				}
				else if(receivedBytes[1] == 2) {
					return "write";
				} else {

					throw new Exception();
				}
			} else {
				throw new Exception();
			}


		} else {
			throw new Exception();
		}
	}


	//method is the same as the one in the client class
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

	//method is the same as the one in the client class
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
