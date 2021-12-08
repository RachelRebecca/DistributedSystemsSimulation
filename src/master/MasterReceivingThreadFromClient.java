package master;

import resources.*;

import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;

public class MasterReceivingThreadFromClient extends Thread
{
    private final Socket clientSocket;
    private final Done done;
    private final ArrayList<Job> unfinishedJobs;
    private final Object unfinishedJob_LOCK;


    public MasterReceivingThreadFromClient(Socket clientSocket, Done done, ArrayList<Job> unfinishedJobs, Object unfinishedJob_LOCK)
    {
        this.clientSocket = clientSocket;
        this.unfinishedJobs = unfinishedJobs;
        this.unfinishedJob_LOCK = unfinishedJob_LOCK;
        this.done = done;
    }

    @Override
    public void run()
    {
        try (// stream to read object response from server
             ObjectInputStream objectInputStream = new ObjectInputStream(clientSocket.getInputStream()))
        {
            System.out.println("Entered Master Receiving Thread from Client");
            Job receivedJob;
            while ((receivedJob = (Job) objectInputStream.readObject()) != null)
            {
                if (receivedJob.getStatus() == JobStatuses.UNFINISHED_SEND_TO_MASTER)
                {
                    receivedJob.setStatus(JobStatuses.UNFINISHED_SEND_TO_SLAVE);
                    synchronized (unfinishedJob_LOCK)
                    {
                        unfinishedJobs.add(receivedJob);
                    }
                    System.out.println("Got a job from client: " + receivedJob.getType() + receivedJob.getId());
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("Master receiving thread from client error" + e.getMessage());
        }
    }
}
