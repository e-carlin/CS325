package wifi;
import java.io.PrintWriter;


import java.util.Arrays;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;
import rf.RF;



/**
 * Use this layer as a starting point for your project code.  See {@link Dot11Interface} for more
 * details on these routines.
 * @author richards
 */
public class LinkLayer implements Dot11Interface {
	private static RF theRF;           // You'll need one of these eventually
	private short ourMAC;       // Our MAC address
	private PrintWriter output; // The output stream we'll write to

	//Data shared with threads
	private ConcurrentLinkedQueue<byte[]> dataToTrans; //Outgoing data app->transmit
	private ConcurrentLinkedQueue<byte[]> rcvdACK;
	private Vector<byte[]> dataRcvd; //Incoming data recv->app

	/**
	 * Constructor takes a MAC address and the PrintWriter to which our output will
	 * be written.
	 * @param ourMAC  MAC address
	 * @param output  Output stream associated with GUI
	 */
	public LinkLayer(short ourMAC, PrintWriter output) {
		this.ourMAC = ourMAC;
		this.output = output;      
		this.theRF = new RF(null, null);

		//The sender thread
		this.dataToTrans = new ConcurrentLinkedQueue<byte[]>();
		Sender sender = new Sender(this.theRF, this.dataToTrans, null);
		(new Thread(sender)).start();

		//The receiver thread
		this.dataRcvd = new Vector<byte[]>();
		this.rcvdACK = new ConcurrentLinkedQueue<byte[]>();
		Receiver recvr = new Receiver(this.theRF, this.dataRcvd, this.ourMAC, this.rcvdACK);
		(new Thread(recvr)).start();

		output.println("LinkLayer: Constructor ran.");
	}

	/**
	 * Send method takes a destination, a buffer (array) of data, and the number
	 * of bytes to send.  See docs for full description.
	 */
	public int send(short dest, byte[] data, int len) {
		output.println("LinkLayer: Sending "+len+" bytes to "+dest);

		//Construct the data packet
		byte[] toSend = PacketManipulator.buildDataPacket(dest, this.ourMAC, data, len, 0);

		//add the packet to the shared Vector
		boolean successAdding = dataToTrans.add(toSend);

		if(successAdding) //success adding to the vector
			return len;
		else
			return -1;
	}

	/**
	 * Recv method blocks until data arrives, then writes info into
	 * the Transmission object.  See docs for full description.
	 */
	public int recv(Transmission t) {
		output.println("LinkLayer: Blocking on recv()");
		while(this.dataRcvd.isEmpty()){ //While there is no new data rcvd (block)

			try{ 
				Thread.sleep(100); //Wait
			}
			catch(InterruptedException e){ //If interrupted during sleep
				output.println("Interrupted while blocking in recv() "+e);

			}
		}

		//There is new info so process it
		byte[] dataRcvd = this.dataRcvd.get(0);
		
		//add the info to the transmission object
		short destAddr = PacketManipulator.getDestAddr(dataRcvd);
		t.setDestAddr(destAddr);

		short sourceAddr = PacketManipulator.getSourceAddr(dataRcvd);
		t.setSourceAddr(sourceAddr);

		byte[] data = PacketManipulator.getData(dataRcvd);
		t.setBuf(data); 

		this.dataRcvd.remove(0); //delete the packet we just processed

		return data.length;
	}
	
	/**
	 * This function returns the RF layers clock time
	 * @return the clock time
	 */
	public static long clock(){
		return theRF.clock();
	}
	/**
	 * Returns a current status code.  See docs for full description.
	 */
	public int status() {
		output.println("LinkLayer: Faking a status() return value of 0");
		return 0;
	}

	/**
	 * Passes command info to your link layer.  See docs for full description.
	 */
	public int command(int cmd, int val) {
		output.println("LinkLayer: Sending command "+cmd+" with value "+val);
		return 0;
	}
}
