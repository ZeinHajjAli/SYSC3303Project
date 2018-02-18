import java.io.*;
import java.net.*;


public class ClientConnection2 extends Thread {

	 DatagramPacket request;
	 DatagramSocket socket;

	public ClientConnection2(DatagramPacket request){
		this.request = request;
		try {
			socket = new DatagramSocket();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void run(){
		String type = validatePacket(request);
		String file = null;
		if (type == "WRQ"){
			sendACK(socket, request);
			try {
				file = getFilename(request);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			writeRequest(socket, file);
		} else if(type == "RRQ"){
			try {
				file = getFilename(request);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			readRequest(socket, request, file);
		}
		
		
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
	
	public static void readRequest(DatagramSocket socket, DatagramPacket packet, String filename){
		byte data[] = new byte[512];
		DatagramPacket received = new DatagramPacket(data, data.length);
		byte[] fileBytes = null;

		try {
			fileBytes = createArray(filename);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		boolean keepSending = true;

		sendData(fileBytes, packet, socket);
		
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
	
	public String getFilename(DatagramPacket receivedPacket) throws FileNotFoundException{
		
		
		byte[] content;
	
		
		int len;
		int j;
		
		content = new byte[512];
		receivedPacket = new DatagramPacket(content, content.length);

		content = receivedPacket.getData();
		len = receivedPacket.getLength();
		
		for(j = 2; j<len; j++){
			if (content[j] == 0) break;
    	}
    		
		if (j==len-1) throw new FileNotFoundException(); // didn't find a 0 byte
		// otherwise, extract filename
		String filename = new String(content,2,j-2);
        return filename;

	}
}
