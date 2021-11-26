package client;

import resources.*;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class ClientSendingThread extends Thread
{
    private final Socket clientSocket;
    private final ArrayList<Job> unsentList;
    private final Object unsent_LOCK;
    private final Done done;

    public ClientSendingThread(Socket clientSocket, ArrayList<Job> unsentList, Object unsent_LOCK, Done isDone) {
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
                     new ObjectOutputStream(clientSocket.getOutputStream())
                )
        {
            while (!done.getIsFinished())
            {
                Job currJob;
                int unsentSize;
                synchronized (unsent_LOCK)
                {
                    unsentSize = unsentList.size();
                }
                if (unsentSize > 0)
                {
                    synchronized (unsent_LOCK)
                    {
                        currJob = unsentList.get(0);
                        unsentList.remove(0);
                    }

                    //send to master
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
        System.out.println("exiting thread.");
    }
}
