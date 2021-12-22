package master;

import resources.*;

import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Master receives unfinished jobs from the Client using the client socket
 * Adds unfinished jobs to shared memory
 */
public class MasterReceivingThreadFromClient extends Thread
{
    private final Socket clientSocket;
    private final Done done;
    private final Object done_LOCK;
    private final ArrayList<Job> unfinishedJobs;
    private final Object unfinishedJob_LOCK;
    private int clientNumber;


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
        try (// stream to read object response from server
             ObjectInputStream objectInputStream = new ObjectInputStream(clientSocket.getInputStream()))
        {
            Job receivedJob; // set job to whatever is being read from the Client

            while ((receivedJob = (Job) objectInputStream.readObject()) != null)
            {
                if (receivedJob.getStatus() == JobStatuses.CLIENT_DONE)
                {
                    synchronized (done_LOCK)
                    {
                        done.removeClient();
                        // add my client number to the clientToClose arrayList
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
                    }
                    System.out.println("Got a job from client: " + receivedJob.getClient() + "." + receivedJob.getType()
                            + receivedJob.getId() + "\n");
                }
            }
        }
        catch (Exception e)
        {
            System.out.print("");
        }
    }
}
