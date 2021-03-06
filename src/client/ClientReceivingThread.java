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
    // Socket connecting Client to Master
    private final Socket clientSocket;

    // list of unfinished jobs (shared memory)
    private final ArrayList<Job> unfinishedList;
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
        try (// stream to read object response from Master
             ObjectInputStream objectInputStream = new ObjectInputStream(clientSocket.getInputStream()))
        {
            Job serverMessage; // set job to be whatever is being read from the Master
            while ((serverMessage = (Job) objectInputStream.readObject()) != null)
            {
                System.out.println("\nJob " + serverMessage.getType() + serverMessage.getId() + " was finished.");

                // If Client receives a finished job, remove it from the unfinished list based on its ID
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
            System.out.println("No longer connected to Master. ");
        }
    }
}
