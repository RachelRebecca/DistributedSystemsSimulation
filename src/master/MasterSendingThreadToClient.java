package master;

import resources.*;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Master sends finished jobs to Client using the client's socket
 * One SendingThread is made for each Client connected to the Master
 * Removes finished jobs from shared memory
 */
public class MasterSendingThreadToClient extends Thread
{
    // Socket connecting Client to Master
    private final Socket clientSocket;

    // list of finished jobs to send to the client (shared memory)
    private final ArrayList<Job> finishedJobs;
    private final Object finishedJobs_LOCK;

    private final Done done;

    // the ID of the Client which the Master is sending jobs to using this thread
    private final int clientNumber;

    public MasterSendingThreadToClient(Socket clientSocket, Done done, ArrayList<Job> finishedJobs,
                                       Object finishedJobs_LOCK, int clientNumber)
    {
        this.clientSocket = clientSocket;
        this.finishedJobs = finishedJobs;
        this.finishedJobs_LOCK = finishedJobs_LOCK;
        this.done = done;
        this.clientNumber = clientNumber;
    }

    @Override
    public void run()
    {
       try (ObjectOutputStream requestWriter = new ObjectOutputStream(clientSocket.getOutputStream()))
       {
           while (!done.isFinished())
           {
               Job currJob = null;

               int finishedJobsSize;
               // if size of finished job list is greater than 1 (i.e. there is a finished job),
               // if the client ID on the job is equal to the client ID of the Client this thread is sending to,
               // remove the job
               synchronized (finishedJobs_LOCK)
               {
                   finishedJobsSize = finishedJobs.size();
                   if (finishedJobsSize > 0)
                   {
                       // check if the current available finished job to send
                       // came from the client this sending thread was created for
                       if (finishedJobs.get(0).getClient() == clientNumber)
                       {
                           currJob = finishedJobs.get(0);
                           finishedJobs.remove(0);
                       }
                   }
               }

               if (finishedJobsSize > 0 && currJob != null)
               {
                   //update status and send finished job to client
                   currJob.setStatus(JobStatuses.FINISHED_SEND_TO_CLIENT);
                   requestWriter.writeObject(currJob);
                   System.out.println("Finished job " + currJob.getClient() + "." + currJob.getType() + "" + currJob.getId()
                           + " was sent to Client " + currJob.getClient() + ".\n");
               }
           }
       }
       catch (Exception e)
       {
           System.out.println("Detected Client exit.");
       }
    }
}
