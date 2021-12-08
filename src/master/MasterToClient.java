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
                          ServerSocket serverSocket,
                          ArrayList<Job> unfinishedJobs, ArrayList<Job> finishedJobs,
                          Object unfinishedJob_LOCK, Object finishedJob_LOCK,
                          Done isDone)
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

                /*
                int mscSize;
                synchronized (masterSendingThreadToClient)
                {
                    mscSize = masterSendingThreadToClient.size();
                }

                int mrcSize;
                synchronized (masterSendingThreadToClient)
                {
                    mscSize = masterSendingThreadToClient.size();
                }
                 */

                synchronized (masterReceivingThreadFromClient_LOCK)
                {
                    MasterReceivingThreadFromClient mrc = new MasterReceivingThreadFromClient(
                            clientSocket, done, unfinishedJobs, unfinishedJob_LOCK);
                    masterReceivingThreadFromClient.add(mrc);
                    mrc.start();

                    System.out.println("Starting new receiving thread from client - MasterToClient");
                }
                synchronized (masterSendingThreadToClient_LOCK)
                {
                    MasterSendingThreadToClient msc = new MasterSendingThreadToClient(
                            clientSocket, done, finishedJobs, finishedJob_LOCK);
                    masterSendingThreadToClient.add(msc);
                    msc.start();
                    System.out.println("Starting new sending thread from client - MasterToClient");
                }

                if (done.getIsFinished())
                {
                    done.setFinished(true);
                    try
                    {
                        System.out.println("entering while loop - master sending thread ");
                        System.out.println("master sending thread to client size" + masterSendingThreadToClient.size());
                        System.out.println("master receiving thread from client size" + masterReceivingThreadFromClient.size());

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
                        System.out.println("MasterToClient joining catch" + e.getMessage());
                    }
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("Entering this try-catch - ALERT!");
            System.out.println(e.getMessage());
        }

        System.out.println("Exiting MasterToClient.");
    }


}
