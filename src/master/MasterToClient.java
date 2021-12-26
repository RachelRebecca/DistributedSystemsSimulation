package master;

import resources.Done;
import resources.Job;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Thread constantly makes new client sockets as any new Client connects to the Master
 * Starts new MasterReceivingFromClient and MasterSendingToClient threads for each new client socket
 * Updates shared memory
 */
public class MasterToClient extends Thread
{
    // ArrayList of the current MasterReceivingFromClient and MasterSendingToClient threads
    private final ArrayList<Thread> masterReceivingThreadFromClient;
    private final ArrayList<Thread> masterSendingThreadToClient;

    // list of client sockets that connected to Master
    private final ArrayList<Socket> clientSockets;
    private final Object clientSockets_LOCK;

    // Master's ServerSocket
    private final ServerSocket serverSocket;

    // list of unfinished jobs received by Client
    private final ArrayList<Job> unfinishedJobs;
    private final Object unfinishedJob_LOCK;

    // list of finished jobs to send back to Client
    private final ArrayList<Job> finishedJobs;
    private final Object finishedJob_LOCK;

    // Done Object signaling Threads when to exit
    private final Done done;
    private final Object done_LOCK;

    private boolean continueLoop;

    public MasterToClient(ArrayList<Socket> clientSockets, Object clientSockets_LOCK,
                          ServerSocket serverSocket, ArrayList<Job> unfinishedJobs, Object unfinishedJob_LOCK,
                          ArrayList<Job> finishedJobs, Object finishedJob_LOCK, Done done, Object done_LOCK)
    {
        this.clientSockets = clientSockets;
        this.clientSockets_LOCK = clientSockets_LOCK;
        this.serverSocket = serverSocket;
        this.unfinishedJobs = unfinishedJobs;
        this.unfinishedJob_LOCK = unfinishedJob_LOCK;
        this.finishedJobs = finishedJobs;
        this.finishedJob_LOCK = finishedJob_LOCK;

        masterReceivingThreadFromClient = new ArrayList<>();
        masterSendingThreadToClient = new ArrayList<>();

        this.done = done;
        this.done_LOCK = done_LOCK;

        continueLoop = true;
    }

    public void run()
    {
        try
        {
            while (continueLoop)
            {
                // accept a new client Socket
                Socket clientSocket = serverSocket.accept();

                // assign the clientNumber
                int clientNumber;
                synchronized (clientSockets_LOCK)
                {
                    clientSockets.add(clientSocket);
                    clientNumber = clientSockets.size();
                }

                // Create and start a new MasterReceivingFromClient and MasterSendingToClient thread for this client
                MasterReceivingThreadFromClient mrc = new MasterReceivingThreadFromClient(
                        clientSocket, unfinishedJobs, unfinishedJob_LOCK, clientNumber);
                masterReceivingThreadFromClient.add(mrc);
                mrc.start();

                MasterSendingThreadToClient msc = new MasterSendingThreadToClient(
                        clientSocket, done, finishedJobs, finishedJob_LOCK, clientNumber);
                masterSendingThreadToClient.add(msc);
                msc.start();

                synchronized (done_LOCK)
                {
                    if (done.isFinished())
                    {
                        continueLoop = false;
                    }
                }
            }

            for (Thread receiving : masterReceivingThreadFromClient)
            {
                try
                {
                    receiving.join();
                } catch (Exception e)
                {
                    System.out.println(e.getMessage());
                }
            }

            for (Thread sending : masterSendingThreadToClient)
            {
                try
                {
                    sending.join();
                } catch (Exception e)
                {
                    System.out.println(e.getMessage());
                }
            }
        }

        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }
}
