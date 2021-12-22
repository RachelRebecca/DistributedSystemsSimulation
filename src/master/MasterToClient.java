package master;

import resources.Done;
import resources.Job;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class MasterToClient extends Thread
{
    ArrayList<Thread> masterReceivingThreadFromClient = new ArrayList<>();
    Object masterReceivingThreadFromClient_LOCK = new Object();
    ArrayList<Thread> masterSendingThreadToClient = new ArrayList<>();
    Object masterSendingThreadToClient_LOCK = new Object();

    ServerSocket serverSocket;
    final ArrayList<Job> unfinishedJobs;
    final Object unfinishedJob_LOCK;
    final ArrayList<Job> finishedJobs;
    final Object finishedJob_LOCK;

    final ArrayList<Integer> clientsToClose;
    final Object clientsToClose_LOCK;

    final Done done;
    Object done_LOCK;

    ArrayList<Socket> clientSockets = new ArrayList<>();


    public MasterToClient(/*ArrayList<Thread> masterReceivingThreadFromClient, Object masterReceivingThreadFromClient_LOCK,
                          ArrayList<Thread> masterSendingThreadToClient, Object masterSendingThreadToClient_LOCK,*/
                          ServerSocket serverSocket, ArrayList<Job> unfinishedJobs, Object unfinishedJob_LOCK,
                          ArrayList<Job> finishedJobs, Object finishedJob_LOCK, Done isDone, Object done_LOCK)
    {
        /*this.masterReceivingThreadFromClient = masterReceivingThreadFromClient;
        this.masterReceivingThreadFromClient_LOCK = masterReceivingThreadFromClient_LOCK;
        this.masterSendingThreadToClient = masterSendingThreadToClient;
        this.masterSendingThreadToClient_LOCK = masterSendingThreadToClient_LOCK;*/

        this.serverSocket = serverSocket;

        this.unfinishedJobs = unfinishedJobs;
        this.unfinishedJob_LOCK = unfinishedJob_LOCK;
        this.finishedJobs = finishedJobs;
        this.finishedJob_LOCK = finishedJob_LOCK;

        clientsToClose = new ArrayList<>();
        clientsToClose_LOCK = new Object();

        done = isDone;
        this.done_LOCK = done_LOCK;
    }

    public void run()
    {
        try
        {
            while (!done.getIsFinished())
            {
                Socket clientSocket = serverSocket.accept();
                clientSockets.add(clientSocket);
                int clientNumber = clientSockets.size();

                synchronized (done_LOCK)
                {
                    done.addClient();
                }

//                synchronized (masterReceivingThreadFromClient_LOCK)
//                {
                    MasterReceivingThreadFromClient mrc = new MasterReceivingThreadFromClient(
                            clientSocket, done, done_LOCK, unfinishedJobs, unfinishedJob_LOCK, clientNumber);
                    masterReceivingThreadFromClient.add(mrc);
                    mrc.start();
//                }

//                synchronized (masterSendingThreadToClient_LOCK)
//                {
                    MasterSendingThreadToClient msc = new MasterSendingThreadToClient(
                            clientSocket, done, finishedJobs, finishedJob_LOCK, clientNumber);
                    masterSendingThreadToClient.add(msc);
                    msc.start();
//                }

                synchronized (clientsToClose_LOCK)
                {
                    while (clientsToClose.size() > 0)
                    {
                        int clientToClose = clientsToClose.get(0);
                        clientsToClose.remove(0);
                        try
                        {
                            masterReceivingThreadFromClient.get(clientToClose - 1).join();
                            masterSendingThreadToClient.get(clientToClose - 1).join();
                        }
                        catch (Exception e)
                        {
                            System.out.println("Problem closing threads when a client exited: " + e.getMessage());
                        }
                    }
                }

                // check clientToClose arrayList
                // if there's something in it
                    // close the relevant sending and receiving threads - NOT the client socket
                    // remove that client number from the arrayList

                if (done.getIsFinished())
                {
                    done.setFinished(true);
                    try
                    {
                        for (Thread sending : masterSendingThreadToClient)
                        {
                            sending.join();
                        }
                        for (Thread receiving : masterReceivingThreadFromClient)
                        {
                            receiving.join();
                        }
                    } catch (Exception e)
                    {
                        System.out.println(e.getMessage());
                    }
                }
            }
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }


}
