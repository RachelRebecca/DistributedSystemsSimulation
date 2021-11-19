package slaves;

import resources.Done;
import resources.Job;
import resources.JobStatuses;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class SlaveSendingThread extends Thread
{
    private Socket slaveSocket;
    private ArrayList<Job> completeJobs;
    private final Object completedJobList_LOCK;
    private Done done;


    //what goes into the constructor??
    public SlaveSendingThread(Socket socket, ArrayList<Job> jobsCompleted, Object completedJob_LOCK, Done finished)
    {
        slaveSocket = socket;
        completeJobs = jobsCompleted;
        completedJobList_LOCK = completedJob_LOCK;
        done = finished;
    }

    public void run()
    {
        try (ObjectOutputStream requestWriter = // stream to write text requests to server
                     new ObjectOutputStream(slaveSocket.getOutputStream()))
        {
            while (!done.getIsFinished())
            {
                Job myJob;
                int numDone;
                synchronized (completedJobList_LOCK)
                {
                    numDone = completeJobs.size();
                }

                if (numDone > 0)
                {
                    synchronized (completedJobList_LOCK)
                    {
                        myJob = completeJobs.get(0);
                        completeJobs.remove(0);

                    }
                    myJob.setStatus(JobStatuses.FINISHED_SEND_TO_MASTER);
                    requestWriter.writeObject(myJob);
                }

                if (done.getIsFinished())
                {
                    done.setFinished(true);
                }

            }
        }
        catch (Exception e)
        {
            System.out.println("Something happened");
            // something
        }

    }

}
