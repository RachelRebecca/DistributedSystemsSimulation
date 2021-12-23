package master;

import resources.Done;
import resources.Job;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class MasterToClient extends Thread
{
    private final ArrayList<Thread> masterReceivingThreadFromClient = new ArrayList<>();
    private final ArrayList<Thread> masterSendingThreadToClient = new ArrayList<>();

    private final ServerSocket serverSocket;
    private final ArrayList<Job> unfinishedJobs;
    private final Object unfinishedJob_LOCK;
    private final ArrayList<Job> finishedJobs;
    private final Object finishedJob_LOCK;

    private final ArrayList<Integer> clientsToClose;
    private final Object clientsToClose_LOCK;

    private final Done done;
    private final Object done_LOCK;

    private final ArrayList<Socket> clientSockets;
    private final Object clientSockets_LOCK;


    public MasterToClient(ArrayList<Socket> clientSockets, Object clientSockets_LOCK,
                          ServerSocket serverSocket, ArrayList<Job> unfinishedJobs, Object unfinishedJob_LOCK,
                          ArrayList<Job> finishedJobs, Object finishedJob_LOCK, Done isDone, Object done_LOCK)
    {
        this.serverSocket = serverSocket;

        this.unfinishedJobs = unfinishedJobs;
        this.unfinishedJob_LOCK = unfinishedJob_LOCK;
        this.finishedJobs = finishedJobs;
        this.finishedJob_LOCK = finishedJob_LOCK;

        clientsToClose = new ArrayList<>();
        clientsToClose_LOCK = new Object();

        done = isDone;
        this.done_LOCK = done_LOCK;

        this.clientSockets = clientSockets;
        this.clientSockets_LOCK = clientSockets_LOCK;
    }

    public void run()
    {
        try
        {
            while (!done.getIsFinished())
            {
                Socket clientSocket = serverSocket.accept();
                int clientNumber;
                synchronized (clientSockets_LOCK)
                {
                    clientSockets.add(clientSocket);
                    clientNumber = clientSockets.size();
                }

                synchronized (done_LOCK)
                {
                    done.addClient();
                }

                MasterReceivingThreadFromClient mrc = new MasterReceivingThreadFromClient(
                        clientSocket, done, done_LOCK, unfinishedJobs, unfinishedJob_LOCK, clientNumber);
                masterReceivingThreadFromClient.add(mrc);
                mrc.start();

                MasterSendingThreadToClient msc = new MasterSendingThreadToClient(
                        clientSocket, done, finishedJobs, finishedJob_LOCK, clientNumber);
                masterSendingThreadToClient.add(msc);
                msc.start();


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
                    for (Thread sending : masterSendingThreadToClient)
                    {
                        try
                        {
                            sending.join();
                        }
                        catch (Exception e)
                        {
                            System.out.println(e.getMessage());
                        }
                    }

                    for (Thread receiving : masterReceivingThreadFromClient)
                    {
                        try
                        {
                            receiving.join();
                        }
                        catch (Exception e)
                        {
                            System.out.println(e.getMessage());
                        }
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
