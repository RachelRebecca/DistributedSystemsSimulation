package master;

import resources.*;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class MasterSendingThreadToClient extends Thread
{
    private Socket clientSocket;
    //private ArrayList<Job> unsentList;
    //private Object unsentList_LOCK;
    //private ArrayList<Job> finishedList;
    //private Object finished_LOCK;
    private ArrayList<Job> finishedJobs;
    private final Object finishedJobs_LOCK;
    private Done done;

    public MasterSendingThreadToClient(Socket clientSocket, Done done, ArrayList<Job> finishedJobs, Object finishedJobs_LOCK)
    {
        this.clientSocket = clientSocket;
       // this.unsentList = unsentList;
        //this.unsentList_LOCK = unsentList_LOCK;
        //this.finishedList = finishedList;
        //this.finished_LOCK = finished_LOCK;
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
               //send acknowledgement to client that job has been received
               //currJob = unsentList.get(0);
               //unsentList.remove(0);

               //send finished job to client


               currJob = finishedJobs.get(0);
               if (currJob.getStatus() == JobStatuses.ACK_MASTER_RECEIVED)
               {
                   currJob.setStatus(JobStatuses.UNFINISHED_SEND_TO_SLAVE);
                   System.out.println(currJob.getId() + "" + currJob.getType() + " was received by master");
                   synchronized (finishedJobs_LOCK)
                   {
                       finishedJobs.remove(0);
                   }
               }
               else
               {
                    currJob.setStatus(JobStatuses.FINISHED_SEND_TO_CLIENT);
                    synchronized (finishedJobs_LOCK)
                    {
                        finishedJobs.remove(0);
                        System.out.println(currJob.getId() + "" + currJob.getType() + " was sent to client");
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
           System.out.println(e.getMessage());
       }

    }
}
