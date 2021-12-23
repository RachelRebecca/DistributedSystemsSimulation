package master;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Creates a Slave A Socket
 */
public class SlaveASetup extends Thread
{
    // list of Slave A Sockets connected to the Master (shared memory)
    private final ArrayList<Socket> slaveAs;
    private final Object slaveA_LOCK;

    // ServerSocket to connect the Slave to the Master using its unique port number
    private final ServerSocket slaveAServerSocket;

    public SlaveASetup(ArrayList<Socket> slaveAs, Object slaveA_LOCK, ServerSocket slaveAServerSocket)
    {
        this.slaveAs = slaveAs;
        this.slaveA_LOCK = slaveA_LOCK;
        this.slaveAServerSocket = slaveAServerSocket;
    }

    @Override
    public void run()
    {
        while (true)
        {
            int size;
            synchronized (slaveA_LOCK)
            {
                size = slaveAs.size();
            }

            // if there's more than one Slave A, break
            if (size > 0)
            {
                break;
            }

            // Make a Slave A Socket
            synchronized (slaveA_LOCK)
            {
                try
                {
                    Socket slaveA = slaveAServerSocket.accept();
                    System.out.println("Got a Slave A socket");
                    slaveAs.add(slaveA);
                }
                catch (IOException e)
                {
                    System.out.println(e.getMessage());
                }
            }
        }
    }
}
