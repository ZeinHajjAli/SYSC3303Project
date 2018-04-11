package ServerFiles;

import java.io.*;
import java.net.*;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;
import java.util.Arrays;

import static java.lang.System.*;

/*
this is the threadable client class used to handle requests. This class is started by the server

 */

public class ClientConnection extends Thread {

	private DatagramPacket request; //datagram packet to contain the request
	private DatagramSocket socket; //socket to receive the request
	private byte[] block = {0,0};
	private FileOutputStream fileWriter;
	private static FileInputStream myInputStream; // input stream to handle file I/O
	private static DatagramPacket lastPacket;
	private static int port;
	private static final int TIMEOUT = 100; //final int for the timeout amount
	private static final String ServPath = ".\\src\\Serv\\"; //path descriptor for the server
	private static int REC_PORT = 25; //set the receiving port
	private static InetAddress localhost;

	private static String type = "";
	private static InetAddress address;


	ClientConnection(DatagramPacket request){ //constructor to initialize the class

		this.request = request;
		address = request.getAddress();
		port = request.getPort();
		try {
			out.println(InetAddress.getLocalHost());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
        int random = (int )(Math.random() * 6000 + 5000);



		try {
			socket = new DatagramSocket(random);
			socket.setSoTimeout(TIMEOUT); //set the timeout amount
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	public void run(){

		out.println("ClientConnection running");

		String type = validatePacket(request); //validate the received packet
		String file = null;

		try {
			file = getFilename(request); //get the file name from the packet
		} catch (FileNotFoundException e) {
			sendError(1, port); //raise an error code 1
			e.printStackTrace();
		}
		String mode = getMode(request, file); //determine the mode of the request

		if(!mode.equalsIgnoreCase("octet")) { //if the mode is not implemented(octet) send an error code 4
			sendError(4, port);
			shutdown(); //shut down the client connection
		}

		file = encodeFilename(file); //encode the filename

		try {
			switch (type) {
				case "WRQ": //if the type returned is write request, send an ack and print the type
					sendACK(request);
					out.println("writeRequest");
					writeRequest(file);
					break;
				case "RRQ": //if the type returned is read request, send an ack and print the type
					out.println("Read Request");
					readRequest(file);
					break;
				default: //if it is not read or write it is an error and the client connection is shut down
					shutdown();
					break;
			}
		} catch (IOException e) {
			String msg = e.getMessage(); //get the error message

			if (msg.equals("There is not enough space on the disk") || msg.equals("Not enough space") || msg.equals("No space left on device")) {
				sendError(3, port); //send a error code 3 if there is no room on disk
			} else if (e.getClass().equals(AccessDeniedException.class)) {
				sendError(2, port); //send a error code 2 if the permissions are not correct
			} else if (e.getClass().equals(FileAlreadyExistsException.class)) {
				sendError(6, port); //send a error code 6 if the file already exists
			}
			e.printStackTrace();
		}
	}

	private String validatePacket(DatagramPacket packet) //class to determine what is contained within the packet
	{

		byte[] data = packet.getData();
		if(data[0]==0){
			if(data[1]==1){
				return "RRQ"; //it is a read request
			}else if(data[1]==2){
				return "WRQ"; //it is a write request
			}else if(data[1]==3){
				return "DATA"; //it is a data packet
			}else if(data[1]==4){
				return "ACK"; //it is a acknowledgement
			}else if(data[1]==5){
				return "ERROR"; //it is an error
			}else{
				return "INVALID"; //otherwise it is invalid
			}
		}else{
			return "INVALID";
		}

	}
	
	private void readRequest(String filename) throws IOException{ //class to send a read request
		byte data[] = new byte[512];
		DatagramPacket received = new DatagramPacket(data, data.length);
		byte[] fileBytes;
		fileBytes = createArray(filename);
		boolean keepSending = true;
		boolean cont = true;
		boolean first = true;

		while(keepSending) {
			if(!first) {
				try {
					socket.receive(received);
					cont = true;
				} catch (SocketTimeoutException e) {
					if (lastPacket != null) {
						socket.send(lastPacket);
					}
					cont = false;
				}
			}
			if(cont) {

				if (fileBytes.length < 508) {
					keepSending = false;
				}
				if(first){
					block = nextBlock(block);
					received.setPort(REC_PORT);
					received.setAddress(address);
					fileBytes = sendData(fileBytes, received);
					first = false;
				} else {
					switch (validatePacket(received)) {
						case "ACK":

							if ((received.getPort() == REC_PORT) && (received.getAddress().equals(address))) {
								byte[] blockNumber = unpackBlockNumber(received);
								if (Arrays.equals(blockNumber, block)) {
									block = nextBlock(block);
									fileBytes = sendData(fileBytes, received);
								} else {
									sendError(4, received.getPort());
								}
							} else {
								sendError(5, received.getPort());
							}
							break;
						case "ERROR":
							keepSending = false;
							out.println("Server had an error");
							shutdown();
							break;
						case "RRQ":
							out.println("duplicate RRQ");
						case "INVALID":
							keepSending = false;
							out.println("opcode error");
							sendError(4, received.getPort());
							shutdown();
							break;
						default:
							out.println("DEFAULT");
							keepSending = false;
							out.println("There was an ERROR");
							shutdown();
							break;
					}
				}
			}
			out.println(keepSending);
		}
	}


	private void sendError(int code, int port) //class to send error based on an error code
	{
		String errorMessage;
		byte[] data; //byte array to contain an error code
		switch(code){
			case 0: errorMessage =	"UNKNOWN ERROR";
			break;
			case 1: errorMessage =	"File Not Found";
			break;
			case 2: errorMessage =	"Access Violation";
			break;
			case 3: errorMessage =	"Disk Full/Allocation Exceeded";
			break;
			case 4: errorMessage =	"Illegal TFTP Operation";
			break;
			case 5: errorMessage =	"Unknown Transfer ID";
			break;
			case 6: errorMessage =	"File Already Exists";
			break;
			case 7: errorMessage =	"No Such User";
			break;
			default: errorMessage =	"Unknown Error";
			break;
		}
		byte[] message = errorMessage.getBytes();
		data = new byte[5 + message.length];
		data[0] = 0;
		data[1] = 5;
		data[2] = 0;
		data[3] = (byte) code;
		out.println(errorMessage); //prints out the concatonated error message

		arraycopy(message, 0, data, 4, message.length + 4 - 4);
		data[data.length-1] = 0;
		DatagramPacket send = new DatagramPacket(data, data.length, new InetSocketAddress(address,port));
		lastPacket = send;

		try {
			socket.send(send); //send the error message/code
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	private byte[] unpackReadData(DatagramPacket packet)
	{
		byte[] packetData = packet.getData();
		byte[] data = new byte[packetData.length - 4];
		arraycopy(packetData, 4, data, 0, packetData.length - 4);

		return data;

	}

	private String encodeFilename(String filename){
		return ServPath + filename;
	} //encode the filename by concatonating the path with the filename

	private void writeRequest(String filename) throws IOException{ //class to send a write request
		byte data[] = new byte[512];
		DatagramPacket received = new DatagramPacket(data, data.length);
		boolean keepReceiving = true;
		boolean cont;
		File newFile = new File(filename);
		newFile.createNewFile();
		fileWriter = new FileOutputStream(filename);

		while(keepReceiving) {

			try {
				socket.receive(received);
				cont = true;
			} catch (SocketTimeoutException e) {
				sendACK(lastPacket);
				cont = false;
			}
			if(cont) {
                switch (validatePacket(received)) {
                    case "DATA":
                        if ((received.getPort() == port) && (received.getAddress().equals(address))) {
                            byte[] receivedBytes = unpackReadData(received);
                            byte[] blockNumber = unpackBlockNumber(received);

                            if (Arrays.equals(blockNumber, nextBlock(block))) {
                                if (received.getLength() < 512) { //if the length is less than 512 it is end of transfer
                                    keepReceiving = false;
                                }
                                else if (received.getLength() > 512) {
									sendError(4,received.getPort()); //if greater than 512, its an invalid block
									shutdown();
								}
                                block = nextBlock(block);
                                fileWriter.write(receivedBytes);
                                sendACK(received);
                            } else if (Arrays.equals(blockNumber, block)) {
                                sendACK(received); //send an ack to acknowledge data has been received
                            } else {
                            	sendError(4, received.getPort());
                            	shutdown();
							}

                        } else {
                            sendError(5, received.getPort());
                            shutdown();
                        }
                        break;
                    case "ERROR": //case to handle error within the client
                        keepReceiving = false;
                        out.println("Client had an ERROR");
                        shutdown();
                        break;
					case "INVALID": //if the opcode is invalid, send error code 4 and shutdown
						keepReceiving = false;
						out.println("opcode error");
						sendError(4, received.getPort());
						shutdown();
						break;
                    default:
                        keepReceiving = false;
                        out.println("There was an ERROR"); //print out an error has occured
                        shutdown();
                        break;
                }
            }
		}
	}
	
	private void sendACK(DatagramPacket packet) //class to send an acknowledgment
	{
		byte[] data = new byte[4]; //byte to contain the code which denotes an ack
		data[0] = 0;
		data[1] = 4;
		data[2] = block[0];
		data[3] = block[1];
		DatagramPacket ACK =  new DatagramPacket(data, data.length, packet.getAddress(), packet.getPort());
		lastPacket = ACK;

		try {
			socket.send(ACK);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private byte[] sendData(byte[] fileBytes, DatagramPacket packet) throws IOException //class to send a data packet
	{
		byte[] data = new byte[512]; //byte tp contain the code which denotes data
		data[0] = 0;
		data[1] = 3;
		data[2] = block[0];
		data[3] = block[1];
		int len;
		if(fileBytes.length < 508){
			len = fileBytes.length;
		} else {
			len = 508;
		}

		arraycopy(fileBytes, 0, data, 4, len);
		packet.setData(data, 0, data.length);
		packet.setAddress(address);
		packet.setPort(port);
		lastPacket = packet;
		socket.send(packet);
		byte[] changedFile;
		if(len >= 508){
			changedFile = new byte[fileBytes.length - len];

			arraycopy(fileBytes, 508, changedFile, 0, changedFile.length);
		} else {
			changedFile = new byte[1];
		}

		out.println(len + " " + fileBytes.length + " " + changedFile.length);

		return changedFile;
	}
	
	//Creates the DatagramPacket following the guidelines in the assignment document
	private byte[] createArray(String filename) { //creates an array of the file in bytes
		out.println(filename);
		myInputStream = null;
		File file = new File(filename);
		try{
			myInputStream = new FileInputStream(file);
			out.println(myInputStream.available());
			//change catch back to filenotfound exception
		} catch (IOException e) {
			sendError(1, REC_PORT);
			e.printStackTrace();
		}

		out.println(file.length());

		byte fileBytes[] = new byte[(int)file.length()];
		try{
			myInputStream.read(fileBytes);
		} catch (IOException e){
			e.printStackTrace();
		}

		//puts the final byte array into a new DatagramPacket and gives it the Address as well as the receiving port
		return fileBytes;
	}
	
	private String getFilename(DatagramPacket receivedPacket) throws FileNotFoundException //class to obtain the file name contained within the packet
	{
		byte[] content = receivedPacket.getData();
		int len = receivedPacket.getLength();
		int j;
		
		for(j = 2; j<len; j++){
			if (content[j] == 0) break;
    	}
    		
		if (j==len-1){
			sendError(1, port);
			throw new FileNotFoundException(); // didn't find a 0 byte
		}
		// otherwise, extract filename
		return new String(content,2,j-2);

	}

	private String getMode(DatagramPacket receivedPacket, String filename) //calss to setermine which mode is within the packet
	{
		byte[] content = receivedPacket.getData();
		int len = receivedPacket.getLength();
		int j;
		int startLength = 3+filename.length();

		for(j = startLength; j<len; j++){
			if (content[j] == 0) break;
		}

		if (j==len-1){
			sendError(4, port);
			shutdown();
		}
		// otherwise, extract filename
		return new String(content,startLength,j-startLength);

	}

	private byte[] unpackBlockNumber(DatagramPacket packet) //class to unpack the block number of the packet
	{
		byte[] packetData = packet.getData();
		byte[] data = new byte[2];
		data[0] = packetData[2];
		data[1] = packetData[3];

		return data;
	}

	private byte[] nextBlock(byte[] myBlock) //determine the next block within the packet
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

	private void shutdown() { //class to shut down the client connection
		socket.close();
		try {
			fileWriter.close();
			myInputStream.close();
			exit(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
