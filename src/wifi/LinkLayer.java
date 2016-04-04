package wifi;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Vector;
import rf.RF;

/**
 * Use this layer as a starting point for your project code.  See {@link Dot11Interface} for more
 * details on these routines.
 * @author richards
 */
public class LinkLayer implements Dot11Interface {
   private RF theRF;           // You'll need one of these eventually
   private short ourMAC;       // Our MAC address
   private PrintWriter output; // The output stream we'll write to
   
   //List of data that needs to be transmitted
   private Vector<byte[]> dataToTrans;
   private Vector<byte[]> dataRcvd;

   /**
    * Constructor takes a MAC address and the PrintWriter to which our output will
    * be written.
    * @param ourMAC  MAC address
    * @param output  Output stream associated with GUI
    */
   public LinkLayer(short ourMAC, PrintWriter output) {
      this.ourMAC = ourMAC;
      this.output = output;      
      theRF = new RF(null, null);
      
      //The sender thread
      this.dataToTrans = new Vector<byte[]>();
      Sender sender = new Sender(this.theRF, this.dataToTrans);
      (new Thread(sender)).start();
      
      //The receiver thread
      this.dataRcvd = new Vector<byte[]>();
      Receiver recvr = new Receiver(this.theRF, this.dataRcvd);
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
      byte[] toSend = PacketManipulator.buildDataPacket(dest, this.ourMAC, data, len);
      boolean successAdding = dataToTrans.add(toSend);

      if(successAdding)
    	  return len;
      else
    	  return -1;
   }

   /**
    * Recv method blocks until data arrives, then writes it an address info into
    * the Transmission object.  See docs for full description.
    */
   public int recv(Transmission t) {
      output.println("LinkLayer: Pretending to block on recv()");
      while(this.dataRcvd.isEmpty()){
    	  
    	  try{ 
				Thread.sleep(5); //Wait
			}
			catch(InterruptedException e){ //If interrupted during sleep
				output.println("Interrupted while waiting in recv() "+e);

			}
      }
      byte[] dataRcvd = this.dataRcvd.get(0);
      //add the info to the transmission object
      t.setSourceAddr(this.ourMAC);
      
      short destAddr = PacketManipulator.getDestAddr(dataRcvd);
      t.setDestAddr(destAddr);
      
      byte[] data = PacketManipulator.getData(dataRcvd);
      t.setBuf(data); //***Need to handle for the possible different buffer sizes
      
      
      
      
      return -1; //change this to the number of bytes rcvd 
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
