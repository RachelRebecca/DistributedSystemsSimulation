package master;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Creates a Slave Socket
 */
public class SlaveSetup extends Thread
{
    // list of Slave Sockets connected to the Master (shared memory)
    private final ArrayList<Socket> slaves;
    private final Object slave_LOCK;

    // ServerSocket to connect the Slave to the Master using its unique port number
    private final ServerSocket slaveServerSocket;

    public SlaveSetup(ArrayList<Socket> slaves, Object slave_LOCK, ServerSocket slaveServerSocket)
    {
        this.slaves = slaves;
        this.slave_LOCK = slave_LOCK;
        this.slaveServerSocket = slaveServerSocket;
    }

    @Override
    public void run()
    {
        while (true)
        {
            int size;
            synchronized (slave_LOCK)
            {
                size = slaves.size();
            }

            // if there's more than one Slave, break
            if (size > 0)
            {
                break;
            }

            // Make a Slave Socket
            synchronized (slave_LOCK)
            {
                try
                {
                    Socket slave = slaveServerSocket.accept();
                    slaves.add(slave);
                }
                catch (IOException e)
                {
                    System.out.println(e.getMessage());
                }
            }
        }
    }
}
