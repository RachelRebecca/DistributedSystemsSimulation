package client;

import resources.*;

import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;

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
            Job serverMessage;
            while ((serverMessage = (Job) objectInputStream.readObject()) != null)
            {
                System.out.println("Receiving thread: Got something from master!");

                // output message
                System.out.println("resources.Job " + serverMessage.getType() + serverMessage.getId() + " was finished.");
            }
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }
}
