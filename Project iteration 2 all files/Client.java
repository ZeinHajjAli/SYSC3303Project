import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.nio.file.Files;

public class Client
{

	public static void main(String[] args)
	{

		Scanner reader = new Scanner(System.in);
		System.out.println("Read(1) or Write(2): ");
		int WR = reader.nextInt();
		if(WR != 1 && WR != 2){
			System.exit(0);
		}
		System.out.println("Input Filename: ");
		String filename = reader.next();
		if(filename.equals("exit") || filename.length()==0){
			System.exit(0);
		}

		DatagramSocket socket = createSocket(24);
		DatagramPacket packet = formRequest(WR, filename,"octet");
		try {
			socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(WR == 1) {
			readRequest(socket, filename);
		} else if(WR ==2) {
			writeRequest(socket, filename);
		}


	}

	public static void readRequest(DatagramSocket socket, String filename){
		byte data[] = new byte[512];
		DatagramPacket received = new DatagramPacket(data, data.length);

		FileOutputStream fileWriter = null;
		boolean keepReceiving = true;

		while(keepReceiving) {
			try {
				socket.receive(received);
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (validatePacket(received).equals("DATA")) {
				byte[] receivedBytes = unpackReadData(received);
				if(receivedBytes.length < 508){
					keepReceiving = false;
				}
				try {
					fileWriter = new FileOutputStream(filename);
					fileWriter.write(receivedBytes);
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						fileWriter.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				sendACK(socket, received);

			} else {
				keepReceiving = false;
				System.out.println("Something went wrong");
			}
		}
	}

	public static void sendACK(DatagramSocket socket, DatagramPacket packet)
	{

		byte[] packetBytes = packet.getData();
		byte[] data = new byte[4];
		data[0] = 0;
		data[1] = 4;
		data[2] = packetBytes[2];
		data[3] = packetBytes[3];
		try {
			socket.send(new DatagramPacket(data, data.length, packet.getSocketAddress()));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static byte[] unpackReadData(DatagramPacket packet)
	{
		byte[] packetData = packet.getData();
		byte[] data = new byte[packetData.length - 4];
		for(int i=4; i<packetData.length; i++){
			data[i-4] = packetData[i];
		}

		return data;

	}

	public static void writeRequest(DatagramSocket socket, String filename){
		byte data[] = new byte[512];
		DatagramPacket received = new DatagramPacket(data, data.length);
		byte[] fileBytes = null;

		try {
			fileBytes = createArray(filename);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		boolean keepSending = true;

		while(keepSending) {
			try {
				socket.receive(received);
			} catch (IOException e) {
				e.printStackTrace();
			}

			if(fileBytes.length < 512){
				keepSending = false;
			}

			if (validatePacket(received).equals("ACK")) {
				fileBytes = sendData(fileBytes, received, socket);
			} else {
				keepSending = false;
				System.out.println("Something went wrong");
			}
		}
	}

	public static byte[] sendData(byte[] fileBytes, DatagramPacket packet, DatagramSocket socket)
	{

		byte[] data = new byte[512];
		data[0] = 0;
		data[1] = 3;
		data[2] = packet.getData()[2];
		data[3] = (byte) (packet.getData()[3] + 1);
		for(int i=4; i<512; i++){
			data[i] = fileBytes[i-4];
		}
		packet.setData(data, 0, data.length);
		try {
			socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}

		byte[] changedFile = new byte[fileBytes.length - 508];
		for(int i=0; i<changedFile.length; i++){
			changedFile[i] = fileBytes[i+508];
		}

		return changedFile;

	}

	public static String validatePacket(DatagramPacket packet)
	{

		byte[] data = packet.getData();
		if(data[0]==0){
			if(data[1]==1){
				return "RRQ";
			}else if(data[1]==2){
				return "WRQ";
			}else if(data[1]==3){
				return "DATA";
			}else if(data[1]==4){
				return "ACK";
			}else if(data[1]==5){
				return "ERROR";
			}else{
				return "INVALID";
			}
		}else{
			return "INVALID";
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
	public static byte[] createArray(String filename) throws FileNotFoundException
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
		return fileBytes;

	}

	public static DatagramPacket formRequest(int WR, String filename, String mode)
	{
		byte[] wrBytes = new byte[2];
		wrBytes[0] = 0;
		byte[] filenameBytes = new byte[filename.length()];
		byte[] modeBytes = new byte[mode.length()];

		if(WR == 1 || WR == 2) {
			wrBytes[1] = (byte) WR;
		} else {
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
		DatagramPacket packet = new DatagramPacket(finalBytes, finalBytes.length, new InetSocketAddress("localhost",69));
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
