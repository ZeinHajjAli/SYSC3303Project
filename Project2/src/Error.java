
public class Error {
	private ErrorCodes errorcode;
	private PacketType packetType;
	private int blockNumber;
	
	
	public Error(ErrorCodes error, PacketType type) {
		errorcode = error;
		packetType = type;
		// TODO Auto-generated constructor stub
	}
	
 /*
  * getters and setters
  */
	
	public PacketType getPacketType() {
		return packetType;
	}
	
	public int getBlockNumber() {
		return blockNumber;
	}
	
	public ErrorCodes getErrorcode() {
		return errorcode;
	}

}
