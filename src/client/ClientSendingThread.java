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
    private final Socket clientSocket;

    //list of jobs that haven't yet been sent to Master (shared memory)
    private final ArrayList<Job> unsentList;
    private final Object unsent_LOCK;

    private final Done done;

    public ClientSendingThread(Socket clientSocket, ArrayList<Job> unsentList, Object unsent_LOCK, Done isDone)
    {
        this.clientSocket = clientSocket;
        this.unsentList = unsentList;
        this.unsent_LOCK = unsent_LOCK;
        this.done = isDone;
    }

    @Override
    public void run()
    {
        try
                (ObjectOutputStream requestWriter = // stream to write text requests to server
                     new ObjectOutputStream(clientSocket.getOutputStream()))
        {
            while (!done.getIsFinished())
            {
                Job currJob;

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
                }

                if (done.getIsFinished())
                {
                    done.setFinished(true);
                }
            }
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }
}
