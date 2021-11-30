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
        while (!done.getIsFinished())
        {
            try
            {
                Socket clientSocket = serverSocket.accept();
                synchronized (masterReceivingThreadFromClient_LOCK)
                {
                    masterReceivingThreadFromClient.add(new MasterReceivingThreadFromClient(
                            clientSocket, done, unfinishedJobs, unfinishedJob_LOCK));
                }
                synchronized (masterSendingThreadToClient_LOCK)
                {
                    masterSendingThreadToClient.add(new MasterSendingThreadToClient(
                            clientSocket, done, finishedJobs, finishedJob_LOCK));
                }
            }
            catch (Exception e)
            {
                System.out.println(e.getMessage());
            }

            if (done.getIsFinished())
            {
                done.setFinished(true);
            }
        }
    }

}
