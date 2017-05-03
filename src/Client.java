import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Scanner;

import util.FileReader;
import util.Log;
import util.Var;

public class Client {
	private static final String MODE = "netASCII";
	
	private DatagramSocket socket;
	private InetSocketAddress addrHost, addrServer;
	private boolean running;
	private boolean testMode;
	
	public Client() throws SocketException {
		socket = new DatagramSocket();
		addrHost = new InetSocketAddress("localhost", Var.PORT_CLIENT);
		addrServer = new InetSocketAddress("localhost", Var.PORT_SERVER);
		this.testMode = false;
	}
	
	public Client(boolean testMode) throws SocketException {
		socket = new DatagramSocket();
		addrHost = new InetSocketAddress("localhost", Var.PORT_CLIENT);
		addrServer = new InetSocketAddress("localhost", Var.PORT_SERVER);
		this.testMode = testMode;
	}
	
	public void run() throws IOException {
		InetSocketAddress address;
		DatagramPacket packet;
		running = true;
		Log.out("Starting Client");
		if (testMode) {
			address = addrHost;
		} else {
			address = addrServer;
		}
		while(true) {
			ArrayList<String> userData = getRequestData();
			if (userData.get(0).equals("R")) {
				packet = makePacket(address,Var.READ, userData.get(1).getBytes(), Var.ZERO,MODE.getBytes(), Var.ZERO);
				Log.packet("Client Sending READ", packet);
			} else {
				packet = makePacket(address, Var.WRITE, userData.get(1).getBytes(), Var.ZERO,MODE.getBytes(), Var.ZERO);
				Log.packet("Client Sending WRITE", packet);
			}
			socket.send(packet);
			
			socket.receive(packet);
			if (userData.get(0).equals("R")) {
				readMode(packet);
			} else {
				writeMode(packet, userData.get(1));
			}
			Log.packet("Client Receive", packet);
		}
	}
	
	private void readMode(DatagramPacket packet) {
		InetSocketAddress address = new InetSocketAddress(packet.getAddress(), packet.getPort());
		
	}
	
	private void writeMode(DatagramPacket packet, String fileName) throws IOException {
		InetSocketAddress address = new InetSocketAddress(packet.getAddress(), packet.getPort());
		FileReader file = new FileReader(fileName);
		byte[] blockNum = new byte[2];
		byte[] data = file.read();
		int dataLength = data.length;
		packet = makePacket(address,Var.DATA,blockNum, data);
		socket.receive(packet);
		if (packet.getData()[1] == Var.ACK[1]){
			if()
		}
		
	}
	
	private byte[] bytesIncrement(byte[] data) {
		if (data[1] == 0xff) {
			data[0]++;
			data[1] = 0x00;
		} else {
			data[1]++;
		}
		return data;
	}
	/**
	 * Prompts and collects the data from the user of filename and read or write
	 * @return ArrayList where first element is if its read or write and second element is filename
	 */
	private ArrayList<String> getRequestData() {
		Scanner reader = new Scanner(System.in);
		ArrayList<String> data = new ArrayList<String>();
		String rorW = getUserInput("Read or Write or Shutdown ('R' or 'W' or 'S'): ", reader);
		if (rorW.equals("S")) {
			close();
		}
		String file = getUserInput("Filename: ", reader);
		data.add(rorW);
		data.add(file);
		reader.close();
		return data;
	}
	
	/**
	 * Prompts the user for input with the given prompt
	 * @param prompt string to display to the user
	 * @return the input from the user
	 */
	private String getUserInput(String prompt, Scanner reader) {
		System.out.print(prompt);
		String s = reader.nextLine();
		System.out.println();
		return s;
	}
	
	private DatagramPacket makePacket(InetSocketAddress sendAddr, byte[]... bytes) {
		// Get the required length of the byte array.
		int length = 0;
		for (byte[] b : bytes) {
			length += b.length;
			if (length > Var.BUF_SIZE) {
				// If the length is too much then return;
				return null;
			}
		}
		
		// Create the buffer to hold the full array.
		byte[] buffer = new byte[length];
		
		// Copy each byte array into the buffer.
		int i = 0;
		for (byte[] b : bytes) {
			System.arraycopy(b, 0, buffer, i, b.length);
			i += b.length;
		}
		
		// Create a packet from the buffer (using the host address) and return it.
		return new DatagramPacket(buffer, buffer.length, sendAddr);
	}
	
	public void close() {
		if (running) {
			running = false;
			socket.close();
			System.exit(0);
		}
	}

	public boolean isClosed() {
		return !running;
	}

	public static void main(String[] args) throws SocketException, IOException {
		Log.enable(false);
		boolean test = false;
		if (args.length > 0) {
			int i;
			for (i = 0; i < args.length; i++) {
	            if(args[i].equals("v") || args[i].equals("V")) {
	            	Log.enable(true);
	            }
	            if(args[i].equals("t") || args[i].equals("T")) {
	            	test = true;
	            }
	        }
		}
		new Client(test).run();
	}

}
