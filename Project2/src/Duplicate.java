/*
 * this class simulates a duplicate packet(error code 6)
 */
import java.io.*;
import java.net.*;
public class Duplicate extends Thread  {
	private DatagramPacket packet;
	private DatagramSocket socket;
	private 	Error error;
	private int serverPort;
	byte data[] = new byte[512];
	
	public Duplicate(Error error) {
		this.error= error;
		// TODO Auto-generated constructor stub
	}

}
