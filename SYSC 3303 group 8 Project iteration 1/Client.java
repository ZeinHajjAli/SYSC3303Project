import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.io.File;
import java.nio.file.Files;
import java.io.FileInputStream;

@SuppressWarnings("unused")
public class Client
{

	public static void main(String[] args)
	{

		@SuppressWarnings("resource")
		Scanner reader = new Scanner(System.in);
		System.out.println("Input Filename/Path: ");
		String filename = reader.next();
		if(filename.equals("exit") || filename.length()==0){
			System.exit(0);
		}

		//try/catch block to circumvent IOExceptions for sending and receiving on sockets
		try {
			DatagramSocket socket = createSocket(24);
			byte data[] = new byte[512];
			DatagramPacket received = new DatagramPacket(data, data.length);
			DatagramPacket packet = createPacket(filename);
			String testData = new String(packet.getData(), 0, packet.getLength());
			System.out.println(testData);
			socket.send(packet);
			System.out.println("sent");
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
	public static DatagramPacket createPacket(String filename) throws FileNotFoundException
	{
		FileInputStream myInputStream = null;
		File file = new File(filename);
		try{
			myInputStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		byte fileBytes[] = new byte[(int)file.length()];
		try{
			myInputStream.read(fileBytes);
		} catch (IOException e){
			e.printStackTrace();
		}



		//puts the final byte array into a new DatagramPacket and gives it the Address as well as the receiving port
		DatagramPacket packet = new DatagramPacket(fileBytes, fileBytes.length, new InetSocketAddress("localhost",23));
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