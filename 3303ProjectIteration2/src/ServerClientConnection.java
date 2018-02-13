import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.io.File;
import java.nio.file.Files;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class ServerClientConnection implements Runnable
{

    private DatagramPacket packet;
    private DatagramSocket socket;

    public ServerClientConnection(DatagramPacket p)
    {
        packet = p;
    }

    public void run()
    {



    }

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

    public static void saveFile(DatagramPacket packet) throws Exception
    {

        Scanner input = new Scanner(System.in);
        System.out.println("Save file as: ");
        String filename = input.next();
        input.close();
        if(filename.equals("exit") || filename.length()==0){
            System.exit(0);
        }

        byte[] receivedBytes = packet.getData();
        FileOutputStream fileWriter = null;
        try {
            fileWriter = new FileOutputStream(filename);
            fileWriter.write(receivedBytes);
        } finally {
            fileWriter.close();
        }


    }

}

//recSocket.receive(receivedPacket);
//data = new byte[receivedPacket.getLength()];
//System.arraycopy(receivedPacket.getData(), receivedPacket.getOffset(), data, 0, receivedPacket.getLength());
//receivedPacket.setData(data);
