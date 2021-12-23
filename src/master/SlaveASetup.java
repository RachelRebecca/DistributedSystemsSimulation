package master;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SlaveASetup extends Thread
{
    Socket slaveASocket = null;
    ServerSocket slaveAServerSocket;

    public SlaveASetup(Socket slaveA, ServerSocket slaveAServerSocket)
    {
        this.slaveASocket = slaveA;
        this.slaveAServerSocket = slaveAServerSocket;
    }

    @Override
    public void run()
    {
        while (slaveASocket == null)
        {
            try
            {
                slaveASocket = slaveAServerSocket.accept();
            } catch (IOException e)
            {
                continue;
            }
        }
    }
}
