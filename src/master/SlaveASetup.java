package master;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class SlaveASetup extends Thread
{
    ArrayList<Socket> slaveAs;
    Object slaveA_LOCK;
    ServerSocket slaveAServerSocket;

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
                    System.out.println("got a slave A socket");
                    slaveAs.add(slaveA);
                } catch (IOException e)
                {
                    System.out.println("problem with slave a maker");
                    continue;
                }
            }
        }
    }
}
