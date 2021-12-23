package master;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Creates a Slave B Socket
 */
public class SlaveBSetup extends Thread
{
    // list of Slave B Sockets connected to the Master (shared memory)
    private final ArrayList<Socket> slaveBs;
    private final Object slaveB_LOCK;

    // ServerSocket to connect the Slave to the Master using its unique port number
    private final ServerSocket slaveBServerSocket;

    public SlaveBSetup(ArrayList<Socket> slaveBs, Object slaveB_LOCK, ServerSocket slaveBServerSocket)
    {
        this.slaveBs = slaveBs;
        this.slaveB_LOCK = slaveB_LOCK;
        this.slaveBServerSocket = slaveBServerSocket;
    }

    @Override
    public void run()
    {
        while (true)
        {
            int size;
            synchronized (slaveB_LOCK)
            {
                size = slaveBs.size();
            }

            // if there's more than one Slave B, break
            if (size > 0)
            {
                break;
            }

            // Make a Slave B Socket
            synchronized (slaveB_LOCK)
            {
                try
                {
                    Socket slaveB = slaveBServerSocket.accept();
                    System.out.println("Got a slave B socket");
                    slaveBs.add(slaveB);
                }
                catch (IOException e)
                {
                    System.out.println(e.getMessage());
                }
            }
        }
    }
}
