import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class ErrorSimulator
{

	private static final int TIMEOUT = 500;
	private static DatagramSocket recSocket, servSocket, sendSocket;
	private static final int REC_SOCK_PORT = 23;
	private static final int SERV_SOCK_PORT = 25;
	private static final int SEND_SOCK_PORT = 81;
	private static int clientPort = 24;
	private static int serverPort = 69;
	private static DatagramPacket clientPacket, serverPacket;

	public static void main(String args[]) {

		try {
			recSocket = new DatagramSocket(REC_SOCK_PORT);
			servSocket = new DatagramSocket(SERV_SOCK_PORT);
			recSocket.setSoTimeout(TIMEOUT);
			servSocket.setSoTimeout(TIMEOUT);
		} catch (SocketException e) {
			e.printStackTrace();
		}

		byte[] data = new byte[512];
		clientPacket = new DatagramPacket(data, data.length);
		serverPacket = new DatagramPacket(data, data.length);

		Scanner input = new Scanner(System.in);

		System.out.println("What mode: ");
		System.out.println("0: Normal Mode");
		System.out.println("1: Lose a Packet");
		System.out.println("2: Delay a Packet");
		System.out.println("3: Duplicate a Packet");

		int mode = input.nextInt();

		try {
			if (mode == 0) {
				normalMode();
			} else {
				System.out.println("What packet do you want to lose/delay/duplicate? (packet numbering starts at 1) ");
				int packetNumber = input.nextInt();
				if (mode == 1) {
					losePacket(packetNumber);
				}
				System.out.println("How long to delay? (milliseconds)");
				int delay = input.nextInt();
				switch (mode) {
					case 2:
						delayPacket(packetNumber, delay);
					case 3:
						duplicatePacket(packetNumber, delay);

				}
			}
		} catch (IOException e){
			e.printStackTrace();
		}

	}
	
	private static void normalMode() throws IOException {

		System.out.println("Started Normal Mode");

		byte[] data;
		recSocket.setSoTimeout(0);
		servSocket.setSoTimeout(0);

		while(true) {
			//waits to receive a packet from the client

			recSocket.receive(clientPacket);
			clientPort = clientPacket.getPort();
			data = new byte[clientPacket.getLength()];
			System.arraycopy(clientPacket.getData(), clientPacket.getOffset(), data, 0, clientPacket.getLength());
			clientPacket.setData(data);
			System.out.println("received");
			clientPacket.setPort(serverPort);
			printPacket(clientPacket);
			//sends the packet on to the server
			servSocket.send(clientPacket);

			//waits to receive a packet from the server
			servSocket.receive(serverPacket);
			serverPort = serverPacket.getPort();
			data = new byte[serverPacket.getLength()];
			System.arraycopy(serverPacket.getData(), serverPacket.getOffset(), data, 0, serverPacket.getLength());
			serverPacket.setData(data);
			printPacket(serverPacket);
			serverPacket.setPort(clientPort);
			//opens a new socket to send back to the client
			sendSocket = new DatagramSocket(SEND_SOCK_PORT);
			printPacket(serverPacket);
			//sends packet from the server on to the client
			sendSocket.send(serverPacket);
			sendSocket.close();
		}
	}

	public static void losePacket(int packetNumber) throws IOException{

		System.out.println("Started losePacket");

		byte[] data;
		int counter = 0;
		boolean cont = true;
		recSocket.setSoTimeout(TIMEOUT);
		servSocket.setSoTimeout(TIMEOUT);

		while(true) {
			try {
				//waits to receive a packet from the client
				recSocket.receive(clientPacket);
				counter++;
				cont = true;
			} catch (SocketTimeoutException e){
				cont = false;
			}
			if(cont) {
				clientPort = clientPacket.getPort();
				data = new byte[clientPacket.getLength()];
				System.arraycopy(clientPacket.getData(), clientPacket.getOffset(), data, 0, clientPacket.getLength());
				clientPacket.setData(data);
				System.out.println("received");
				clientPacket.setPort(serverPort);
				printPacket(clientPacket);
				//sends the packet on to the server
				if (counter != packetNumber) {
					servSocket.send(clientPacket);
				}
			}

			try {
				//waits to receive a packet from the client
				recSocket.receive(clientPacket);
				counter++;
				cont = true;
			} catch (SocketTimeoutException e){
				cont = false;
			}
			if(cont) {
				serverPort = serverPacket.getPort();
				data = new byte[serverPacket.getLength()];
				System.arraycopy(serverPacket.getData(), serverPacket.getOffset(), data, 0, serverPacket.getLength());
				serverPacket.setData(data);
				printPacket(serverPacket);
				serverPacket.setPort(clientPort);
				//opens a new socket to send back to the client
				sendSocket = new DatagramSocket(SEND_SOCK_PORT);
				printPacket(serverPacket);
				//sends packet from the server on to the client
				if (counter != packetNumber) {
					sendSocket.send(serverPacket);
				}
				sendSocket.close();
			}
		}
	}

	public static void delayPacket(int packetNumber, int delay) throws IOException{

		System.out.println("Started delayPacket");

		byte[] data;
		int counter = 0;
		boolean cont = true;
		recSocket.setSoTimeout(TIMEOUT);
		servSocket.setSoTimeout(TIMEOUT);

		while(true) {

			try {
				//waits to receive a packet from the client
				recSocket.receive(clientPacket);
				counter++;
				cont = true;
			} catch (SocketTimeoutException e){
				cont = false;
			}
			if(cont) {
				clientPort = clientPacket.getPort();
				data = new byte[clientPacket.getLength()];
				System.arraycopy(clientPacket.getData(), clientPacket.getOffset(), data, 0, clientPacket.getLength());
				clientPacket.setData(data);
				System.out.println("received");
				clientPacket.setPort(serverPort);
				printPacket(clientPacket);
				//sends the packet on to the server
				if (counter == packetNumber) {
					try {
						Thread.sleep(delay);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				servSocket.send(clientPacket);
			}

			try {
				//waits to receive a packet from the server
				servSocket.receive(serverPacket);
				counter++;
				cont = true;
			} catch(SocketTimeoutException e){
				cont = false;
			}
			if(cont) {
				serverPort = serverPacket.getPort();
				data = new byte[serverPacket.getLength()];
				System.arraycopy(serverPacket.getData(), serverPacket.getOffset(), data, 0, serverPacket.getLength());
				serverPacket.setData(data);
				printPacket(serverPacket);
				serverPacket.setPort(clientPort);
				//opens a new socket to send back to the client
				sendSocket = new DatagramSocket(SEND_SOCK_PORT);
				printPacket(serverPacket);
				//sends packet from the server on to the client
				if (counter == packetNumber) {
					try {
						Thread.sleep(delay);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				sendSocket.send(serverPacket);
				sendSocket.close();
			}
		}
	}

	public static void duplicatePacket(int packetNumber, int delay) throws IOException{

		System.out.println("Started duplicatePacket");

		byte[] data;
		int counter = 0;
		boolean cont = true;
		recSocket.setSoTimeout(TIMEOUT);
		servSocket.setSoTimeout(TIMEOUT);

		while(true) {
			try {
				//waits to receive a packet from the client
				recSocket.receive(clientPacket);
				counter++;
				cont = true;
			} catch (SocketTimeoutException e){
				cont = false;
			}
			if(cont) {
				clientPort = clientPacket.getPort();
				data = new byte[clientPacket.getLength()];
				System.arraycopy(clientPacket.getData(), clientPacket.getOffset(), data, 0, clientPacket.getLength());
				clientPacket.setData(data);
				System.out.println("received");
				clientPacket.setPort(serverPort);
				printPacket(clientPacket);
				//sends the packet on to the server
				servSocket.send(clientPacket);
				if (counter == packetNumber) {
					try {
						Thread.sleep(delay);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					servSocket.send(clientPacket);
				}
			}

			try {
				//waits to receive a packet from the client
				recSocket.receive(clientPacket);
				counter++;
				cont = true;
			} catch (SocketTimeoutException e){
				cont = false;
			}
			if(cont) {
				serverPort = serverPacket.getPort();
				data = new byte[serverPacket.getLength()];
				System.arraycopy(serverPacket.getData(), serverPacket.getOffset(), data, 0, serverPacket.getLength());
				serverPacket.setData(data);
				printPacket(serverPacket);
				serverPacket.setPort(clientPort);
				//opens a new socket to send back to the client
				sendSocket = new DatagramSocket(SEND_SOCK_PORT);
				printPacket(serverPacket);
				//sends packet from the server on to the client
				sendSocket.send(serverPacket);
				if (counter == packetNumber) {
					try {
						Thread.sleep(delay);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					sendSocket.send(serverPacket);
				}
				sendSocket.close();
			}
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
}

