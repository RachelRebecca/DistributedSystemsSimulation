package client;

import resources.*;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

/**
 * Thread sends to Master using client socket
 * It processes the list of unsent jobs and the first one to the Master
 */
public class ClientSendingThread extends Thread
{
    //the Socket connecting the Client to the Master
    private final Socket clientSocket;

    //list of jobs that haven't yet been sent to Master (shared memory)
    private final ArrayList<Job> unsentList;
    private final Object unsent_LOCK;

    // list of jobs that have not yet been received by the Client from the Master (shared memory)
    private final ArrayList<Job> unfinishedList;
    private final Object unfinished_LOCK;

    // Done object - the signal to exit Thread
    private final Done done;

    public ClientSendingThread(Socket clientSocket, ArrayList<Job> unsentList, Object unsent_LOCK,
                               ArrayList<Job> unfinishedList, Object unfinished_LOCK, Done done)
    {
        this.clientSocket = clientSocket;
        this.unsentList = unsentList;
        this.unsent_LOCK = unsent_LOCK;
        this.unfinishedList = unfinishedList;
        this.unfinished_LOCK = unfinished_LOCK;
        this.done = done;
    }

    @Override
    public void run()
    {
        try (// stream to write text requests to Master
             ObjectOutputStream requestWriter = new ObjectOutputStream(clientSocket.getOutputStream()))
        {
            while (!done.isFinished())
            {
                Job currJob;

                // get size of unsent list
                int unsentSize;
                synchronized (unsent_LOCK)
                {
                    unsentSize = unsentList.size();
                }

                // if size is greater than zero, there is a job that needs to be sent to Master
                if (unsentSize > 0)
                {
                    //get the first job on the unsent list and remove it from the unsent list
                    synchronized (unsent_LOCK)
                    {
                        currJob = unsentList.get(0);
                        unsentList.remove(0);
                    }

                    // send the job to the Master
                    requestWriter.writeObject(currJob);

                    // add the job to the unfinished list
                    synchronized (unfinished_LOCK)
                    {
                        unfinishedList.add(currJob);
                    }
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("No longer connected to Master. ");
        }
    }
}
