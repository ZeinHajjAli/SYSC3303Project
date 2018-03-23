package ServerFiles;

import java.io.*;
import java.net.*;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;
import java.util.Arrays;

import static java.lang.System.*;

public class ClientConnection extends Thread {

	private DatagramPacket request;
	private DatagramSocket socket;
	private byte[] block = {0,0};
	private FileOutputStream fileWriter;
	private static FileInputStream myInputStream;
	private static DatagramPacket lastPacket;
	private static int port;
	private static final int TIMEOUT = 100;
	private static final String ServPath = ".\\src\\Serv\\";
	private static int REC_PORT = 25;
	private static InetAddress localhost;

	ClientConnection(DatagramPacket request){

		this.request = request;
        int random = (int )(Math.random() * 6000 + 5000);

		try {
			socket = new DatagramSocket(random);
			socket.setSoTimeout(TIMEOUT);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	public void run(){

		out.println("ClientConnection running");

		port = request.getPort();
		String type = validatePacket(request);
		String file = null;

		try {
			file = getFilename(request);
		} catch (FileNotFoundException e) {
			sendError(1, port);
			e.printStackTrace();
		}
		file = encodeFilename(file);

		try {
			localhost = InetAddress.getByName("127.0.0.1");
			switch (type) {
				case "WRQ":
					sendACK(request);
					out.println("writeRequest");
					writeRequest(file);
					break;
				case "RRQ":
					out.println("Read Request");
					readRequest(file);
					break;
				default:
					shutdown();
					break;
			}
		} catch (IOException e) {
			String msg = e.getMessage();

			if (msg.equals("There is not enough space on the disk") || msg.equals("Not enough space") || msg.equals("No space left on device")) {
				sendError(3, port);
			} else if (e.getClass().equals(AccessDeniedException.class)) {
				sendError(2, port);
			} else if (e.getClass().equals(FileAlreadyExistsException.class)) {
				sendError(6, port);
			}
			e.printStackTrace();
		}
	}

	private String validatePacket(DatagramPacket packet)
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
	
	private void readRequest(String filename) throws IOException{
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
					fileBytes = sendData(fileBytes, received);
					first = false;
				} else {
					switch (validatePacket(received)) {
						case "ACK":

							if (received.getPort() == REC_PORT) {
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

	private void sendError(int code, int port)
	{
		String errorMessage;
		byte[] data;
		switch(code){
			case 0: errorMessage =	"UNKNOWN ERROR";
			case 1: errorMessage =	"File Not Found";
			case 2: errorMessage =	"Access Violation";
			case 3: errorMessage =	"Disk Full/Allocation Exceeded";
			case 4: errorMessage =	"Illegal TFTP Operation";
			case 5: errorMessage =	"Unknown Transfer ID";
			case 6: errorMessage =	"File Already Exists";
			case 7: errorMessage =	"No Such User";
			default: errorMessage =	"Unknown Error";
		}
		byte[] message = errorMessage.getBytes();
		data = new byte[5 + message.length];
		data[0] = 0;
		data[1] = 5;
		data[2] = 0;
		data[3] = (byte) code;

		arraycopy(message, 0, data, 4, message.length + 4 - 4);
		data[data.length-1] = 0;
		DatagramPacket send = new DatagramPacket(data, data.length, new InetSocketAddress("localhost",port));
		lastPacket = send;

		try {
			socket.send(send);
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
	}

	private void writeRequest(String filename) throws IOException{
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
                        if (received.getPort() == port) {
                            byte[] receivedBytes = unpackReadData(received);
                            byte[] blockNumber = unpackBlockNumber(received);

                            if (Arrays.equals(blockNumber, nextBlock(block))) {
                                if (received.getLength() < 512) {
                                    keepReceiving = false;
                                }
                                else if (received.getLength() > 512) {
									sendError(4,received.getPort()); //if greater than 512, its an inval
									shutdown();
								}
                                block = nextBlock(block);
                                fileWriter.write(receivedBytes);
                                sendACK(received);
                            } else if (Arrays.equals(blockNumber, block)) {
                                sendACK(received);
                            } else {
                            	sendError(4, received.getPort());
                            	shutdown();
							}

                        } else {
                            sendError(5, received.getPort());
                            shutdown();
                        }
                        break;
                    case "ERROR":
                        keepReceiving = false;
                        out.println("Client had an ERROR");
                        shutdown();
                        break;
					case "INVALID":
						keepReceiving = false;
						out.println("opcode error");
						sendError(4, received.getPort());
						shutdown();
						break;
                    default:
                        keepReceiving = false;
                        out.println("There was an ERROR");
                        shutdown();
                        break;
                }
            }
		}
	}
	
	private void sendACK(DatagramPacket packet)
	{
		byte[] data = new byte[4];
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

	private byte[] sendData(byte[] fileBytes, DatagramPacket packet) throws IOException
	{
		byte[] data = new byte[512];
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
		packet.setAddress(localhost);
		lastPacket = packet;
		packet.setPort(REC_PORT);
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
	private byte[] createArray(String filename) {
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
	
	private String getFilename(DatagramPacket receivedPacket) throws FileNotFoundException
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

	private byte[] unpackBlockNumber(DatagramPacket packet)
	{
		byte[] packetData = packet.getData();
		byte[] data = new byte[2];
		data[0] = packetData[2];
		data[1] = packetData[3];

		return data;
	}

	private byte[] nextBlock(byte[] myBlock)
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

	private void shutdown() {
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
