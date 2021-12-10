package client;

import resources.*;

import java.io.ObjectInputStream;
import java.net.Socket;

/**
 * Thread receives from Master using the client socket
 * It only receives finished jobs that have already been completed
 */

public class ClientReceivingThread extends Thread
{
    private final Socket clientSocket;

    public ClientReceivingThread(Socket clientSocket)
    {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run()
    {
        try (// stream to read object response from server
             ObjectInputStream objectInputStream = new ObjectInputStream(clientSocket.getInputStream())
            )
        {
            Job serverMessage; // set job to be whatever is being read from the Master
            while ((serverMessage = (Job) objectInputStream.readObject()) != null)
            {
                System.out.println("\njob " + serverMessage.getType() + serverMessage.getId() + " was finished.");
            }
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }
}
