package master;

import resources.*;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class MasterSendingThreadToClient extends Thread
{
    private final Socket clientSocket;
    private final ArrayList<Job> finishedJobs;
    private final Object finishedJobs_LOCK;
    private final Done done; // TODO: final?
    private final int clientNumber;

    public MasterSendingThreadToClient(Socket clientSocket, Done done, ArrayList<Job> finishedJobs, Object finishedJobs_LOCK, int clientNumber)
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
       try (ObjectOutputStream requestWriter = // stream to write text requests to server
            new ObjectOutputStream(clientSocket.getOutputStream()))
       {
           while (!done.getIsFinished())
           {
               Job currJob = null;
               int finishedJobsSize;

               synchronized (finishedJobs_LOCK)
               {
                   finishedJobsSize = finishedJobs.size();
                   if (finishedJobsSize > 0)
                   {
                       if (finishedJobs.get(0).getClient() == clientNumber)
                       {
                           currJob = finishedJobs.get(0);
                           finishedJobs.remove(0);
                       }
                   }
               }

               if (finishedJobsSize > 0 && currJob != null)
               {
                   //send finished job to client
                   currJob.setStatus(JobStatuses.FINISHED_SEND_TO_CLIENT);
                   requestWriter.writeObject(currJob);
                   System.out.println(currJob.getType() + "" + currJob.getId()  + " was sent to client " +
                           currJob.getClient() + "\n");
               }

               if (done.getIsFinished())
               {
                   done.setFinished(true);
               }
           }
       }
       catch (Exception e)
       {
           System.out.println();
       }
    }
}
