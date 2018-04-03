import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.Scanner;

import static java.lang.System.*;

public class ErrorSimulator {

	private static final int TIMEOUT = 50;
	private static DatagramSocket recSocket, servSocket, sendSocket;
	private static final int REC_SOCK_PORT = 23;
	private static final int SERV_SOCK_PORT = 25;
	private static final int SEND_SOCK_PORT = 81;
	private static int clientPort;
	private static int serverPort;
	private static DatagramPacket clientPacket;
	private static DatagramPacket serverPacket;
	private static Scanner input;
	private static InetAddress serverAddress;
	private static InetAddress clientAddress;
	private static InetAddress ccAddress;
	private static String type = "";

	public static void main(String args[]) {

		clientPort = 24;
		serverPort = 69;

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

		input = new Scanner(in);

		out.println("Verbose mode(V) or Quiet mode(Q)?");

		type = input.next();

		if(!type.equalsIgnoreCase("V" )&& !type.equalsIgnoreCase ("Q")){
			exit(0);
		}


		out.println("What mode: ");
		out.println("0: Normal Mode");
		out.println("1: Lose a Packet");
		out.println("2: Delay a Packet");
		out.println("3: Duplicate a Packet");
		out.println("4: Incorrect TFTP opcode");
		out.println("5: Incorrect TID");

		int mode = input.nextInt();

		try {
			if (mode == 0) {
				normalMode();
            } else {
				out.println("What packet do you want to lose/delay/duplicate/incorrect opcode/incorrect TID? (block numbering starts at 0) ");
				byte[] blockNumber = getBlockNumber(input.nextInt());
				if (mode == 5) {
				    out.println("incorrect TID for clients packet or servers? [Client: 1 | Server: 2] (Only matters when the Client is receiving)");
				    int side = input.nextInt();
				    out.println("What TID would you like to give it? (port number)");
				    int TID = input.nextInt();
				    incorrectTID(blockNumber, side, TID);
                }
				out.println("Lose/Delay/Duplicate/Incorrect opcode clients packet or servers? [Client: 1 | Server: 2] ");
				int side = input.nextInt();

				if (mode == 1) {
					losePacket(blockNumber, side);
				} else if (mode == 4){
				    out.println("What opcode would you like to give it? (Invalid opcodes are greater than 5) ");
				    int opcode = input.nextInt();
				    incorrectOpcode(blockNumber, side, opcode);
                } else {
					out.println("How long to delay? (milliseconds)");
					int delay = input.nextInt();
					switch (mode) {
						case 2:
							delayPacket(blockNumber, side, delay);
						case 3:
							duplicatePacket(blockNumber, side, delay);

					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static void normalMode() throws IOException {

		out.println("Started Normal Mode");

		byte[] data;
		boolean cont;
		recSocket.setSoTimeout(TIMEOUT);
		servSocket.setSoTimeout(TIMEOUT);
		byte[] newArr;

		while (true) {
			//waits to receive a packet from the client

			try {
				newArr = new byte[512];
				clientPacket = new DatagramPacket(newArr, newArr.length);
				recSocket.receive(clientPacket);
				cont = true;
			} catch (SocketTimeoutException e) {
				cont = false;
			}
			if (cont) {
				clientPort = clientPacket.getPort();
				out.println(clientPort);
				data = new byte[512];
				arraycopy(clientPacket.getData(), clientPacket.getOffset(), data, 0, clientPacket.getLength());
				clientPacket.setData(data);
				out.println("received");
				clientPacket.setPort(serverPort);
				printPacket(clientPacket);
				//sends the packet on to the server
				servSocket.send(clientPacket);
			}
			try {
				newArr = new byte[512];
				serverPacket = new DatagramPacket(newArr, newArr.length);
				//waits to receive a packet from the server
				servSocket.receive(serverPacket);
				cont = true;
			} catch (SocketTimeoutException e) {
				cont = false;
			}
			if (cont) {
				serverPort = serverPacket.getPort();
				data = new byte[512];
				arraycopy(serverPacket.getData(), serverPacket.getOffset(), data, 0, serverPacket.getLength());
				serverPacket.setData(data);
				serverPacket.setPort(clientPort);
				printPacket(serverPacket);
				//opens a new socket to send back to the client
				sendSocket = new DatagramSocket(SEND_SOCK_PORT);
				printPacket(serverPacket);
				//sends packet from the server on to the client
				sendSocket.send(serverPacket);
				sendSocket.close();
			}

			//handleQuit();
		}
	}

	private static void losePacket(byte[] blockNumber, int side) throws IOException {
		out.println("Started losePacket ");

		byte[] data;
		boolean cont;
		boolean lost = false;
		recSocket.setSoTimeout(TIMEOUT);
		servSocket.setSoTimeout(TIMEOUT);
		byte[] newArr;
		byte[] packetBlock = null;

		while (true) {
			//waits to receive a packet from the client

			try {
				newArr = new byte[512];
				clientPacket = new DatagramPacket(newArr, newArr.length);
				recSocket.receive(clientPacket);
				packetBlock = unpackBlockNumber(clientPacket);
				cont = true;
			} catch (SocketTimeoutException e) {
				cont = false;
			}
			if (cont) {
				clientPort = clientPacket.getPort();
				out.println(clientPort);
				data = new byte[512];
				arraycopy(clientPacket.getData(), clientPacket.getOffset(), data, 0, clientPacket.getLength());
				clientPacket.setData(data);
				out.println("received");
				clientPacket.setPort(serverPort);
				printPacket(clientPacket);
				if((Arrays.equals(packetBlock, blockNumber) && side==1) && !lost){
					lost = true;
					out.println("Lost Client packet with block number " + blockNumber[0] + blockNumber[1]);
				} else {
					//sends the packet on to the server
					servSocket.send(clientPacket);
				}
			}
			try {
				newArr = new byte[512];
				serverPacket = new DatagramPacket(newArr, newArr.length);
				//waits to receive a packet from the server
				servSocket.receive(serverPacket);
				cont = true;
			} catch (SocketTimeoutException e) {
				cont = false;
			}
			if (cont) {
				serverPort = serverPacket.getPort();
				data = new byte[512];
				arraycopy(serverPacket.getData(), serverPacket.getOffset(), data, 0, serverPacket.getLength());
				serverPacket.setData(data);
				serverPacket.setPort(clientPort);
				printPacket(serverPacket);
				//opens a new socket to send back to the client
				sendSocket = new DatagramSocket(SEND_SOCK_PORT);
				printPacket(serverPacket);
				//sends packet from the server on to the client
				if((Arrays.equals(packetBlock, blockNumber) && side==2) && !lost){
					lost = true;
					out.println("Lost Server packet with block number " + blockNumber[0] + blockNumber[1]);
				} else {
					sendSocket.send(serverPacket);
				}
				sendSocket.close();
			}

			//handleQuit();
		}
	}

	private static void delayPacket(byte[] blockNumber, int side, int delay) throws IOException {
		out.println("Started delayPacket");

		byte[] data;
		boolean cont;
		boolean delayed = false;
		recSocket.setSoTimeout(TIMEOUT);
		servSocket.setSoTimeout(TIMEOUT);
		byte[] newArr;
		byte[] packetBlock = null;

		while (true) {
			//waits to receive a packet from the client

			try {
				newArr = new byte[512];
				clientPacket = new DatagramPacket(newArr, newArr.length);
				recSocket.receive(clientPacket);
				packetBlock = unpackBlockNumber(clientPacket);
				cont = true;
			} catch (SocketTimeoutException e) {
				cont = false;
			}
			if (cont) {
				clientPort = clientPacket.getPort();
				out.println(clientPort);
				data = new byte[512];
				arraycopy(clientPacket.getData(), clientPacket.getOffset(), data, 0, clientPacket.getLength());
				clientPacket.setData(data);
				out.println("received");
				clientPacket.setPort(serverPort);
				printPacket(clientPacket);
				if((Arrays.equals(packetBlock, blockNumber) && side==1) && !delayed){
					delayed = true;
					out.println("Delayed Client packet with block number " + blockNumber[0] + blockNumber[1] + " for " + delay + " milliseconds");
					try {
						Thread.sleep(delay);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					servSocket.send(serverPacket);
				} else {
					//sends the packet on to the server
					servSocket.send(clientPacket);
				}
			}
			try {
				newArr = new byte[512];
				serverPacket = new DatagramPacket(newArr, newArr.length);
				//waits to receive a packet from the server
				servSocket.receive(serverPacket);
				cont = true;
			} catch (SocketTimeoutException e) {
				cont = false;
			}
			if (cont) {
				serverPort = serverPacket.getPort();
				data = new byte[512];
				arraycopy(serverPacket.getData(), serverPacket.getOffset(), data, 0, serverPacket.getLength());
				serverPacket.setData(data);
				serverPacket.setPort(clientPort);
				printPacket(serverPacket);
				//opens a new socket to send back to the client
				sendSocket = new DatagramSocket(SEND_SOCK_PORT);
				printPacket(serverPacket);
				//sends packet from the server on to the client
				if((Arrays.equals(packetBlock, blockNumber) && side==2) && !delayed){
					delayed = true;
					out.println("Delayed Server packet with block number " + blockNumber[0] + blockNumber[1] + " for " + delay + " milliseconds");
					try {
						Thread.sleep(delay);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					sendSocket.send(serverPacket);
				} else {
					sendSocket.send(serverPacket);
				}
				sendSocket.close();
			}

			//handleQuit();
		}
	}

	private static void duplicatePacket(byte[] blockNumber, int side, int delay) throws IOException {

		out.println("Started duplicatePacket");

		byte[] data;
		boolean cont;
		boolean delayed = false;
		recSocket.setSoTimeout(TIMEOUT);
		servSocket.setSoTimeout(TIMEOUT);
		byte[] newArr;
		byte[] packetBlock = null;

		while (true) {
			//waits to receive a packet from the client

			try {
				newArr = new byte[512];
				clientPacket = new DatagramPacket(newArr, newArr.length);
				recSocket.receive(clientPacket);
				packetBlock = unpackBlockNumber(clientPacket);
				cont = true;
			} catch (SocketTimeoutException e) {
				cont = false;
			}
			if (cont) {
				clientPort = clientPacket.getPort();
				out.println(clientPort);
				data = new byte[512];
				arraycopy(clientPacket.getData(), clientPacket.getOffset(), data, 0, clientPacket.getLength());
				clientPacket.setData(data);
				out.println("received");
				clientPacket.setPort(serverPort);
				printPacket(clientPacket);
				if((Arrays.equals(packetBlock, blockNumber) && side==1) && !delayed){
					delayed = true;
					out.println("Duplicated Client packet with block number " + blockNumber[0] + blockNumber[1] + " for " + delay + " milliseconds");
					servSocket.send(serverPacket);
					try {
						Thread.sleep(delay);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					servSocket.send(serverPacket);
				} else {
					//sends the packet on to the server
					servSocket.send(clientPacket);
				}
			}
			try {
				newArr = new byte[512];
				serverPacket = new DatagramPacket(newArr, newArr.length);
				//waits to receive a packet from the server
				servSocket.receive(serverPacket);
				cont = true;
			} catch (SocketTimeoutException e) {
				cont = false;
			}
			if (cont) {
				serverPort = serverPacket.getPort();
				data = new byte[512];
				arraycopy(serverPacket.getData(), serverPacket.getOffset(), data, 0, serverPacket.getLength());
				serverPacket.setData(data);
				serverPacket.setPort(clientPort);
				printPacket(serverPacket);
				//opens a new socket to send back to the client
				sendSocket = new DatagramSocket(SEND_SOCK_PORT);
				printPacket(serverPacket);
				//sends packet from the server on to the client
				if((Arrays.equals(packetBlock, blockNumber) && side==2) && !delayed){
					delayed = true;
					out.println("Delayed Server packet with block number " + blockNumber[0] + blockNumber[1] + " for " + delay + " milliseconds");
					sendSocket.send(serverPacket);
					try {
						Thread.sleep(delay);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					sendSocket.send(serverPacket);
				} else {
					sendSocket.send(serverPacket);
				}
				sendSocket.close();
			}

			//handleQuit();
		}
	}

    private static void incorrectOpcode(byte[] blockNumber, int side, int opcode) throws IOException {
        out.println("Started incorrectOpcode");
        boolean changed = false;
        byte[] data;
        boolean cont;
        recSocket.setSoTimeout(TIMEOUT);
        servSocket.setSoTimeout(TIMEOUT);
        byte[] newArr;
        byte[] packetBlock = null;

        while (true) {
            //waits to receive a packet from the client

            try {
                newArr = new byte[512];
                clientPacket = new DatagramPacket(newArr, newArr.length);
                recSocket.receive(clientPacket);
                packetBlock = unpackBlockNumber(clientPacket);
                cont = true;
            } catch (SocketTimeoutException e) {
                cont = false;
            }
            if (cont) {
                clientPort = clientPacket.getPort();
                out.println(clientPort);
                data = new byte[512];
                arraycopy(clientPacket.getData(), clientPacket.getOffset(), data, 0, clientPacket.getLength());
                clientPacket.setData(data);
                out.println("received");
                clientPacket.setPort(serverPort);
                printPacket(clientPacket);
                if((Arrays.equals(packetBlock, blockNumber) && side==1) && !changed){
                    out.println("Gave packet with block number " + blockNumber[0] + blockNumber[1] + " an incorrect opcode");
                    changeOpcode(clientPacket, opcode);
                    changed = true;
                }
                servSocket.send(clientPacket);
            }
            try {
                newArr = new byte[512];
                serverPacket = new DatagramPacket(newArr, newArr.length);
                //waits to receive a packet from the server
                servSocket.receive(serverPacket);
                cont = true;
            } catch (SocketTimeoutException e) {
                cont = false;
            }
            if (cont) {
                serverPort = serverPacket.getPort();
                data = new byte[512];
                arraycopy(serverPacket.getData(), serverPacket.getOffset(), data, 0, serverPacket.getLength());
                serverPacket.setData(data);
                serverPacket.setPort(clientPort);
                printPacket(serverPacket);
                //opens a new socket to send back to the client
                sendSocket = new DatagramSocket(SEND_SOCK_PORT);
                printPacket(serverPacket);
                //sends packet from the server on to the client
                if((Arrays.equals(packetBlock, blockNumber) && side==2) && !changed){
                    out.println("Gave packet with block number " + blockNumber[0] + blockNumber[1] + " an incorrect opcode");
                    changeOpcode(clientPacket, opcode);
                    changed = true;
                }
                sendSocket.send(serverPacket);
                sendSocket.close();
            }

            //handleQuit();
        }
    }

    private static void incorrectTID(byte[] blockNumber, int side, int TID) throws IOException {
        out.println("Started incorrectOpcode");
        boolean changed = false;
        DatagramSocket newSocket;
        byte[] data;
        boolean cont;
        recSocket.setSoTimeout(TIMEOUT);
        servSocket.setSoTimeout(TIMEOUT);
        byte[] newArr;
        byte[] packetBlock = null;

        while (true) {
            //waits to receive a packet from the client

            try {
                newArr = new byte[512];
                clientPacket = new DatagramPacket(newArr, newArr.length);
                recSocket.receive(clientPacket);
                packetBlock = unpackBlockNumber(clientPacket);
                cont = true;
            } catch (SocketTimeoutException e) {
                cont = false;
            }
            if (cont) {
                clientPort = clientPacket.getPort();
                out.println(clientPort);
                data = new byte[512];
                arraycopy(clientPacket.getData(), clientPacket.getOffset(), data, 0, clientPacket.getLength());
                clientPacket.setData(data);
                out.println("received");
                clientPacket.setPort(serverPort);
                printPacket(clientPacket);
                if((Arrays.equals(packetBlock, blockNumber) && side==1) && !changed){
                    out.println("Gave packet with block number " + blockNumber[0] + blockNumber[1] + " an incorrect opcode");
                    newSocket = new DatagramSocket(TID);
                    newSocket.send(clientPacket);
                    changed = true;
                } else {
                    servSocket.send(clientPacket);
                }
            }
            try {
                newArr = new byte[512];
                serverPacket = new DatagramPacket(newArr, newArr.length);
                //waits to receive a packet from the server
                servSocket.receive(serverPacket);
                cont = true;
            } catch (SocketTimeoutException e) {
                cont = false;
            }
            if (cont) {
                serverPort = serverPacket.getPort();
                data = new byte[512];
                arraycopy(serverPacket.getData(), serverPacket.getOffset(), data, 0, serverPacket.getLength());
                serverPacket.setData(data);
                serverPacket.setPort(clientPort);
                printPacket(serverPacket);
                //opens a new socket to send back to the client
                printPacket(serverPacket);
                //sends packet from the server on to the client
                if((Arrays.equals(packetBlock, blockNumber) && side==2) && !changed){
                    out.println("Gave packet with block number " + blockNumber[0] + blockNumber[1] + " an incorrect opcode");
                    newSocket = new DatagramSocket(TID);
                    newSocket.send(serverPacket);
                    changed = true;
                    newSocket.close();
                } else {
                    sendSocket = new DatagramSocket(SEND_SOCK_PORT);
                    sendSocket.send(serverPacket);
                    sendSocket.close();
                }
            }

            //handleQuit();
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

	private static byte[] getBlockNumber(int blockNumber) {
		byte[] ret = new byte[2];
		ret[0] = (byte) (blockNumber/128);
		ret[1] = (byte) (blockNumber%128);
		return ret;
	}

	private static void handleQuit() {
		if(input.hasNext()) {
			String scannedInput = input.next();

			if (scannedInput.equalsIgnoreCase("q")) {
				out.println("user has terminated the server. Shutting down");
				shutdown();
			}
		}
	}

	private static void changeOpcode(DatagramPacket packet, int opcode){
	    byte[] data;

	    data = packet.getData();
	    data[1] = (byte) opcode;
	    packet.setData(data);
    }

	private static void shutdown(){
		recSocket.close();
		servSocket.close();
		sendSocket.close();
		input.close();
		exit(0);
	}

	//same method as the one found in the client class
	private static void printPacket(DatagramPacket p) {
		if (type.equals("V")) {
			byte[] receivedBytes = p.getData();
			out.println("Data being sent/received in bytes: ");

			for (byte element : receivedBytes) {
				out.print(element);
			}

			out.println();
			String receivedString = new String(receivedBytes);
			out.println("Data being sent/received: " + receivedString);
			out.println("from/to address: " + p.getAddress());
			out.println("Port Number: " + p.getPort());
		} else  {
			return;
		}

	}
}

