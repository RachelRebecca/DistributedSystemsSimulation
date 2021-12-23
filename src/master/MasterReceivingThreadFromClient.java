package master;

import resources.*;

import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Master receives unfinished jobs from the Client using the client socket
 * Adds unfinished jobs to shared memory
 */
public class MasterReceivingThreadFromClient extends Thread
{
    // Socket connecting Client to Master
    private final Socket clientSocket;

    private final Done done;
    private final Object done_LOCK;

    // list of unfinished jobs (shared memory)
    // received by Client and sent to Slave to complete
    private final ArrayList<Job> unfinishedJobs;
    private final Object unfinishedJob_LOCK;

    private final int clientNumber;


    public MasterReceivingThreadFromClient(Socket clientSocket, Done done, Object done_LOCK, ArrayList<Job> unfinishedJobs, Object unfinishedJob_LOCK,
                                           int clientNumber)
    {
        this.clientSocket = clientSocket;
        this.unfinishedJobs = unfinishedJobs;
        this.unfinishedJob_LOCK = unfinishedJob_LOCK;
        this.done = done;
        this.clientNumber = clientNumber;
        this.done_LOCK = done_LOCK;
    }

    @Override
    public void run()
    {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(clientSocket.getInputStream()))
        {
            Job receivedJob; // set job to whatever is being read from the Client

            while ((receivedJob = (Job) objectInputStream.readObject()) != null)
            {
                // set job client number
                receivedJob.setClient(clientNumber);

                // if the client is exiting, decrement the number of clients connected in the done shared memory
                if (receivedJob.getStatus() == JobStatuses.CLIENT_DONE)
                {
                    synchronized (done_LOCK)
                    {
                        done.removeClient();
                    }
                }
                else if (receivedJob.getStatus() == JobStatuses.UNFINISHED_SEND_TO_MASTER)
                {
                    //update job status
                    receivedJob.setStatus(JobStatuses.UNFINISHED_SEND_TO_SLAVE);

                    // add the job to the list of unfinished jobs
                    synchronized (unfinishedJob_LOCK)
                    {
                        unfinishedJobs.add(receivedJob);
                        System.out.println("Got a job from Client " + receivedJob.getClient() + ": "
                                + receivedJob.getClient() + "." + receivedJob.getType() + receivedJob.getId() + "\n");
                    }
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("Detected Client exit.");
        }
    }
}
