package client;

import client.ClientReceivingThread;
import client.ClientSendingThread;
import resources.*;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

// todo: DELETE
public class ClientToMaster extends Thread
{

    final ArrayList<Thread> clientSendingThreads;
    Object clientSendingThreads_LOCK;
    final ArrayList<Thread> clientReceivingThreads;
    Object clientReceivingThreads_LOCK;
    final ArrayList<Job> unsentList;
    Object unsent_LOCK;

    final Done done;
    final Done sendingThreadDone;
    final ServerSocket serverSocket;

    public ClientToMaster(Done isDone, ServerSocket serverSocket,
                       ArrayList<Thread> clientSendingThreads, ArrayList<Thread> clientReceivingThreads,
                       Object clientSendingThreads_LOCK, Object clientReceivingThreads_LOCK,
                       ArrayList<Job> unsentList, Object unsent_LOCK,
                       Done sendingThreadDone)
    {
        done = isDone;
        this.serverSocket = serverSocket;

        this.clientSendingThreads = clientSendingThreads;
        this.clientReceivingThreads = clientReceivingThreads;
        this.unsentList = unsentList;
        this.unsent_LOCK = unsent_LOCK;
        this.sendingThreadDone = sendingThreadDone;
    }

    @Override
    public void run()
    {
        while (!done.getIsFinished())
        {
            try
            {
                // todo: redo all this
                Socket clientSocket = serverSocket.accept();
                synchronized (clientSendingThreads_LOCK)
                {
                    clientSendingThreads.add(new ClientSendingThread(clientSocket, unsentList, unsent_LOCK, sendingThreadDone));
                }
                synchronized (clientReceivingThreads_LOCK)
                {
                    clientReceivingThreads.add(new ClientReceivingThread(clientSocket));
                }
//                ObjectOutputStream objectOutputStreamClient = new ObjectOutputStream(clientSocket.getOutputStream());
//                ObjectInputStream objectInputStreamClient = new ObjectInputStream(clientSocket.getInputStream());
            }
            catch (Exception e)
            {
                System.out.println(e.getMessage());
            }
        }
    }
}
