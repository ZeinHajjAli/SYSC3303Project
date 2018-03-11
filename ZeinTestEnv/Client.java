import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client
{

	private static DatagramSocket socket;
	private static byte[] block = {0,0};
	private static FileOutputStream fileWriter;
	private static FileInputStream myInputStream;
	private static DatagramPacket lastPacket;
	private static int port = 23;
	private static final int timeout = 50000;

	public static void main(String[] args)
	{

		Scanner reader = new Scanner(System.in);
		System.out.println("Read(1) or Write(2): ");
		int WR = reader.nextInt();

		if(WR != 1 && WR != 2){
			System.exit(0);
		}
		System.out.println("Input Filename: ");
		String filename;
		filename = reader.next();

		if(filename.equals("exit") || filename.length()==0){
			System.exit(0);
		}

		reader.close();
		createSocket(24);
		DatagramPacket packet = formRequest(WR, filename,"octet");

		try {
			socket.send(packet);

			if(WR == 1) {
				readRequest(filename);
			} else if(WR == 2) {
				writeRequest(filename);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static void readRequest(String filename) throws IOException
	{
		byte data[] = new byte[512];
		DatagramPacket received = new DatagramPacket(data, data.length);
		boolean keepReceiving = true;
		File newFile = new File(filename);
		newFile.createNewFile();
		fileWriter = new FileOutputStream(filename);

		while(keepReceiving) {

			try {
				socket.receive(received);
			} catch (SocketTimeoutException e) {
				sendACK(received);
			}
			port = received.getPort();

			if (validatePacket(received).equals("DATA")) {
				if(received.getPort() == port) {
					byte[] receivedBytes = unpackReadData(received);
					byte[] blockNumber = unpackBlockNumber(received);

					if (blockNumber.equals(nextBlock(block))) {
						if (received.getLength() < 516) {
							keepReceiving = false;
						}
						block = nextBlock(block);
						fileWriter.write(receivedBytes);
						sendACK(received);
					} else if (blockNumber.equals(block)) {
						sendACK(received);
					} else {
						//TODO: ERROR maybe?
					}
				} else {
					sendError(5,received.getPort());
				}


			} else if(validatePacket(received).equals("ERROR")) {
				keepReceiving = false;
				System.out.println("Server had an ERROR");
				shutdown();
			} else {
				keepReceiving = false;
				System.out.println("There was an ERROR");
				//TODO: ERROR handling!!
				shutdown();
			}
		}
	}

	private static void sendACK(DatagramPacket packet)
	{
		byte[] data = new byte[4];
		data[0] = 0;
		data[1] = 4;
		data[2] = block[0];
		data[3] = block[1];
		DatagramPacket ACK =  new DatagramPacket(data, data.length, packet.getSocketAddress());
		lastPacket = ACK;

		try {
			socket.send(ACK);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static byte[] unpackBlockNumber(DatagramPacket packet)
	{
		byte[] packetData = packet.getData();
		byte[] data = new byte[2];
		data[0] = packetData[2];
		data[1] = packetData[3];

		return data;
	}

	private static byte[] unpackReadData(DatagramPacket packet)
	{
		byte[] packetData = packet.getData();
		byte[] data = new byte[packetData.length - 4];
		for(int i=4; i<packetData.length; i++){
			data[i-4] = packetData[i];
		}

		return data;
	}

	private static void writeRequest(String filename) throws IOException
	{
		byte data[] = new byte[512];
		DatagramPacket received = new DatagramPacket(data, data.length);
		byte[] fileBytes = null;

		try {
			fileBytes = createArray(filename);
		} catch (FileNotFoundException e) {
			sendError(1, port);
			e.printStackTrace();
		}

		boolean keepSending = true;

		while(keepSending) {
			try {
				socket.receive(received);
			} catch(SocketTimeoutException e){
				socket.send(lastPacket);
			}
			port = received.getPort();


			if(fileBytes.length < 508){
				keepSending = false;
			}
			if (validatePacket(received).equals("ACK")) {
				if(received.getPort() == port) {
					byte[] blockNumber = unpackBlockNumber(received);
					if (blockNumber.equals(block)) {
						block = nextBlock(block);
						fileBytes = sendData(fileBytes, received);
					}
				} else {
					sendError(5,received.getPort());
				}
			} else if(validatePacket(received).equals("ERROR")) {
				keepSending = false;
				System.out.println("Server had an error");
				shutdown();
			} else {
				keepSending = false;
				System.out.println("There was an ERROR");
				//TODO: ERROR handling!!
				shutdown();
			}
		}
	}

	private static byte[] nextBlock(byte[] myBlock)
	{
		byte[] ret = new byte[2];
		if(myBlock[1] < 127){
			ret[1] = (byte) (myBlock[1] + 1);
		} else {
			ret[1] = 0;
			ret[0] = (byte) (myBlock[0] + 1);
		}

		return ret;
	}

	private static byte[] sendData(byte[] fileBytes, DatagramPacket packet) throws IOException
	{
		byte[] data = new byte[512];
		data[0] = 0;
		data[1] = 3;
		data[2] = block[0];
		data[3] = block[1];

		for(int i=4; i<=512; i++){
			data[i] = fileBytes[i-4];
		}
		packet.setData(data, 0, data.length);
		lastPacket = packet;
		socket.send(packet);
		byte[] changedFile = new byte[fileBytes.length - 508];

		for(int i=0; i<changedFile.length; i++){
			changedFile[i] = fileBytes[i+508];
		}

		return changedFile;
	}

	private static String validatePacket(DatagramPacket packet)
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
	private static void createSocket(int listenPort)
	{
		socket = null;
		//try/catch block for SocketException and UnknownHostException hat might arise from initializing the DatagramSocket and the InetAddress respectively
		try {
			socket = new DatagramSocket(listenPort, InetAddress.getByName("127.0.0.1"));
			socket.setSoTimeout(timeout);
		} catch (SocketException | UnknownHostException e) {
			e.printStackTrace();
		}
	}

	//Creates the DatagramPacket following the guidelines in the assignment document
	private static byte[] createArray(String filename) throws FileNotFoundException
	{
		myInputStream = null;
		File file = new File(filename);
		try{
			myInputStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			sendError(1,port);
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

	private static DatagramPacket formRequest(int WR, String filename, String mode)
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
			} else if(i < 2+filenameBytes.length) {
				finalBytes[i] = filenameBytes[j];
				j++;
			} else if(i == 2+filenameBytes.length) {
				finalBytes[i] = 0;
				j = 0;
			} else if(i < 3+filenameBytes.length+modeBytes.length) {
				finalBytes[i] = modeBytes[j];
				j++;
			} else {
				finalBytes[i] = 0;
			}
		}

		//puts the final byte array into a new DatagramPacket and gives it the Address as well as the receiving port
		DatagramPacket packet = new DatagramPacket(finalBytes, finalBytes.length, new InetSocketAddress("localhost",port));
		printPacket(packet);

		return packet;
	}

	private static void sendError(int code, int port)
	{
		String errorMessage;
		byte[] data;
		switch(code){
			case 5: errorMessage = "	Wrong Number (ERROR: 05)	";
				break;
			default: errorMessage = "	Unknown Error	";
		}
		byte[] message = errorMessage.getBytes();
		data = new byte[5 + message.length];
		data[0] = 0;
		data[1] = 5;
		data[2] = 0;
		data[3] = (byte) code;
		for(int i=4; i<message.length+4; i++){
			data[i] = message[i-4];
		}
		data[data.length-1] = 0;
		DatagramPacket send = new DatagramPacket(data, data.length, new InetSocketAddress("localhost",port));
		try {
			socket.send(send);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//method for printing DatagramPackets with a specific format, both in bytes and as a String, as well as the address and the port
	private static void printPacket(DatagramPacket p)
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

	private static void shutdown() {
		socket.close();
		try {
			fileWriter.close();
			myInputStream.close();
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
