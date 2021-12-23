package master;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class SlaveASetup extends Thread
{
    private final ArrayList<Socket> slaveAs;
    private final Object slaveA_LOCK;
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

            if (size > 0)
            {
                break;
            }

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
