package master;

import resources.Done;
import resources.Job;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class MasterToClient extends Thread
{
    final ArrayList<Thread> masterReceivingThreadFromClient;
    Object masterReceivingThreadFromClient_LOCK;
    final ArrayList<Thread> masterSendingThreadToClient;
    Object masterSendingThreadToClient_LOCK;

    ServerSocket serverSocket;
    final ArrayList<Job> unfinishedJobs;
    final ArrayList<Job> finishedJobs;
    final Object unfinishedJob_LOCK;
    final Object finishedJob_LOCK;

    final Done done;


    public MasterToClient(ArrayList<Thread> masterReceivingThreadFromClient, Object masterReceivingThreadFromClient_LOCK,
                          ArrayList<Thread> masterSendingThreadToClient, Object masterSendingThreadToClient_LOCK,
                          ServerSocket serverSocket, ArrayList<Job> unfinishedJobs, Object unfinishedJob_LOCK,
                          ArrayList<Job> finishedJobs, Object finishedJob_LOCK, Done isDone)
    {
        this.masterReceivingThreadFromClient = masterReceivingThreadFromClient;
        this.masterReceivingThreadFromClient_LOCK = masterReceivingThreadFromClient_LOCK;
        this.masterSendingThreadToClient = masterSendingThreadToClient;
        this.masterSendingThreadToClient_LOCK = masterSendingThreadToClient_LOCK;

        this.serverSocket = serverSocket;

        this.unfinishedJobs = unfinishedJobs;
        this.unfinishedJob_LOCK = unfinishedJob_LOCK;
        this.finishedJobs = finishedJobs;
        this.finishedJob_LOCK = finishedJob_LOCK;

        done = isDone;
    }

    public void run()
    {
        try
        {
            while (!done.getIsFinished())
            {
                Socket clientSocket = serverSocket.accept();

                synchronized (masterReceivingThreadFromClient_LOCK)
                {
                    MasterReceivingThreadFromClient mrc = new MasterReceivingThreadFromClient(
                            clientSocket, done, unfinishedJobs, unfinishedJob_LOCK);
                    masterReceivingThreadFromClient.add(mrc);
                    mrc.start();
                }

                synchronized (masterSendingThreadToClient_LOCK)
                {
                    int clientNumber = masterSendingThreadToClient.size() + 1;
                    MasterSendingThreadToClient msc = new MasterSendingThreadToClient(
                            clientSocket, done, finishedJobs, finishedJob_LOCK, clientNumber);
                    masterSendingThreadToClient.add(msc);
                    msc.start();
                }

                if (done.getIsFinished())
                {
                    done.setFinished(true);
                    try
                    {
                        for (Thread msc : masterSendingThreadToClient)
                        {
                            msc.join();
                        }
                        for (Thread mrc : masterReceivingThreadFromClient)
                        {
                            mrc.join();
                        }
                    } catch (Exception e)
                    {
                        System.out.println("MasterToClient joining catch: " + e.getMessage());
                    }
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("MasterToClient outer exception: " + e.getMessage());
        }
    }


}
