import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Client
{

	public static void main(String[] args)
	{

		/**
		//sets up DatagramPacket for receiving from intermediate
		byte[] buffer = new byte[100];
		int bufferLength = 0;
		InetAddress socketAddress = null;
		try {
			socketAddress = InetAddress.getByName("127.0.0.1");
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		int socketPort = 0;
		DatagramPacket received = new DatagramPacket(buffer, bufferLength, socketAddress, socketPort); //Initialises receiving packet
		*/




		//try/catch block to circumvent IOExceptions for sending and receiving on sockets
		try {
			DatagramSocket socket = createSocket(24);

			//Send 5 read requests
			for (int i=0; i<5; i++) {
				byte data[] = new byte[100];
				DatagramPacket received = new DatagramPacket(data, data.length);
				DatagramPacket packet = createPacket("read", "newfile.txt", "octet");
				String testData = new String(packet.getData(), 0, packet.getLength());
				System.out.println(testData);
				socket.send(packet);
				System.out.println("sent");
				socket.receive(received);
				data = new byte[received.getLength()];
        System.arraycopy(received.getData(), received.getOffset(), data, 0, received.getLength());
				received.setData(data);
				printPacket(received);
			}

			//Send 5 write requests
			for (int i=0; i<5; i++) {
				byte data[] = new byte[100];
				DatagramPacket received = new DatagramPacket(data, data.length);
				DatagramPacket packet = createPacket("write", "newfile.txt", "octet");
				socket.send(packet);
				socket.receive(received);
				data = new byte[received.getLength()];
        System.arraycopy(received.getData(), received.getOffset(), data, 0, received.getLength());
				received.setData(data);
				printPacket(received);
			}

			//Send an invalid request
			byte data[] = new byte[100];
			DatagramPacket received = new DatagramPacket(data, data.length);
			DatagramPacket packet = createPacket("anything else", "newfile.txt", "octet");
			socket.send(packet);
			socket.receive(received);
			data = new byte[received.getLength()];
			System.arraycopy(received.getData(), received.getOffset(), data, 0, received.getLength());
			received.setData(data);
			printPacket(received);

		} catch (IOException e) {
			e.printStackTrace();
		}





	}

	//Method to initialize socket
	public static DatagramSocket createSocket(int port)
	{

		DatagramSocket socket = null;
		//try/catch block for SocketException and UnknownHostException hat might arise from initializing the DatagramSocket and the InetAddress respectively
		try {
			socket = new DatagramSocket(port, InetAddress.getByName("127.0.0.1"));
		} catch (SocketException | UnknownHostException e) {
			e.printStackTrace();
		}

		return socket;

	}

	//Creates the DatagramPacket following the guidelines in the assignment document
	public static DatagramPacket createPacket(String WR, String filename, String mode)
	{
		byte[] wrBytes = new byte[2];
		wrBytes[0] = 0;
		byte[] filenameBytes = new byte[filename.length()];
		byte[] modeBytes = new byte[mode.length()];

		if(WR.compareTo("read") == 0) {
			wrBytes[1] = 1;
		}
		else if(WR.compareTo("write") == 0) {
			wrBytes[1] = 2;
		}
		else {
			wrBytes[0] = -1;
			wrBytes[1] = -1;
		}

		if(filename.length() > 0) {
			filenameBytes = filename.getBytes();
		}

		if(mode.length() > 0) {
			modeBytes = mode.getBytes();
		}


		//Adding the different parts of the packet into one byte array
		byte[] finalBytes = new byte[4 + filenameBytes.length + modeBytes.length];
		int j = 0;
		for(int i=0; i<finalBytes.length; i++) {
			if(i<2) {
				finalBytes[i] = wrBytes[i];
			}
			else if(i < 2+filenameBytes.length) {
				finalBytes[i] = filenameBytes[j];
				j++;
			}
			else if(i == 2+filenameBytes.length) {
				finalBytes[i] = 0;
				j = 0;
			}
			else if(i < 3+filenameBytes.length+modeBytes.length) {
				finalBytes[i] = modeBytes[j];
				j++;
			}
			else {
				finalBytes[i] = 0;
			}
		}

		//puts the final byte array into a new DatagramPacket and gives it the Address as well as the receiving port
		DatagramPacket packet = new DatagramPacket(finalBytes, finalBytes.length, new InetSocketAddress("localhost",23));
		printPacket(packet);
		return packet;

	}


	//method for printing DatagramPackets with a specific format, both in bytes and as a String, as well as the address and the port
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

}
