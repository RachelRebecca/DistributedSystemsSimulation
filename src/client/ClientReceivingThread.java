package client;

import resources.*;

import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Thread receives from Master using the client socket
 * It only receives finished jobs that have already been completed
 */

public class ClientReceivingThread extends Thread
{
    private final Socket clientSocket;
    private ArrayList<Job> unfinishedList;
    private final Object unfinished_LOCK;

    public ClientReceivingThread(Socket clientSocket, ArrayList<Job> unfinishedList, Object unfinished_LOCK)
    {
        this.clientSocket = clientSocket;
        this.unfinishedList = unfinishedList;
        this.unfinished_LOCK = unfinished_LOCK;
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

                synchronized (unfinished_LOCK)
                {
                    Job toRemove = null;
                    for (Job job : unfinishedList)
                    {
                        if (job.getId() == serverMessage.getId())
                        {
                            toRemove = job;
                            break;
                        }
                    }
                    unfinishedList.remove(toRemove);
                }
            }
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }
}
