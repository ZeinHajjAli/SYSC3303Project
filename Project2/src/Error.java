
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
	public void setErrorcode(ErrorCodes errorcode) {
		this.errorcode = errorcode;
	}
	public PacketType getPacketType() {
		return packetType;
	}
	public void setPacketType(PacketType packetType) {
		this.packetType = packetType;
	}
	public int getBlockNumber() {
		return blockNumber;
	}
	public void setBlockNumber(int blockNumber) {
		this.blockNumber = blockNumber;
	}
	public ErrorCodes getErrorcode() {
		return errorcode;
	}

}
