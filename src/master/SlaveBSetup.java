package master;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SlaveBSetup extends Thread
{
    Socket slaveBSocket = null;
    ServerSocket slaveBServerSocket;

    public SlaveBSetup(Socket slaveBSocket, ServerSocket slaveBServerSocket)
    {
        this.slaveBSocket = slaveBSocket;
        this.slaveBServerSocket = slaveBServerSocket;
    }

    @Override
    public void run()
    {
        while (slaveBSocket == null)
        {
            try
            {
                slaveBSocket = slaveBServerSocket.accept();
            } catch (IOException e)
            {
                continue;
            }
        }
    }
}
