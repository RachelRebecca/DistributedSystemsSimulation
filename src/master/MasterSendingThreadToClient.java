package master;

import resources.*;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class MasterSendingThreadToClient extends Thread
{
    private Socket clientSocket;
    private ArrayList<Job> finishedJobs;
    private final Object finishedJobs_LOCK;
    private Done done;

    public MasterSendingThreadToClient(Socket clientSocket, Done done, ArrayList<Job> finishedJobs, Object finishedJobs_LOCK)
    {
        this.clientSocket = clientSocket;
        this.finishedJobs = finishedJobs;
        this.finishedJobs_LOCK = finishedJobs_LOCK;
        this.done = done;
    }

    @Override
    public void run()
    {
       try (ObjectOutputStream requestWriter = // stream to write text requests to server
            new ObjectOutputStream(clientSocket.getOutputStream()))
       {
           while (!done.getIsFinished())
           {
               Job currJob;
               int finishedJobsSize;

               synchronized (finishedJobs_LOCK)
               {
                   finishedJobsSize = finishedJobs.size();
               }

               if (finishedJobsSize > 0)
               {
                   synchronized (finishedJobs_LOCK)
                   {
                       currJob = finishedJobs.get(0);
                   }

                   //send finished job to client
                   requestWriter.writeObject(currJob);

                   currJob.setStatus(JobStatuses.FINISHED_SEND_TO_CLIENT);
                   synchronized (finishedJobs_LOCK)
                   {
                       finishedJobs.remove(0);
                       System.out.println(currJob.getType() + "" + currJob.getId()  + " was sent to client");
                   }
               }

               if (done.getIsFinished())
               {
                   done.setFinished(true);
               }
           }
       }
       catch (Exception e)
       {
           System.out.println("Master sending thread to client error" + e.getMessage());
           System.out.println(Arrays.toString(e.getStackTrace()));
       }
    }
}
