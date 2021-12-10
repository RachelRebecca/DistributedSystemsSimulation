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
    private int clientNumber;

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
//                   synchronized (finishedJobs_LOCK)
//                   {
//                       currJob = finishedJobs.get(0);
//                   }

                   //send finished job to client
                   currJob.setStatus(JobStatuses.FINISHED_SEND_TO_CLIENT);
                   requestWriter.writeObject(currJob);

//                   synchronized (finishedJobs_LOCK)
//                   {
//                       finishedJobs.remove(0);
//                       System.out.println(currJob.getType() + "" + currJob.getId()  + " was sent to client");
//                   }

                   System.out.println(currJob.getType() + "" + currJob.getId()  + " was sent to client");
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
