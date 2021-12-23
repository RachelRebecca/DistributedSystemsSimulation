package slaves;

import resources.*;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class SlaveSendingThread extends Thread
{
    private final Socket slaveSocket;
    private final ArrayList<Job> completeJobs;
    private final Object completedJobList_LOCK;
    private final Done done;

    public SlaveSendingThread(Socket socket, ArrayList<Job> jobsCompleted, Object completedJob_LOCK, Done finished)
    {
        slaveSocket = socket;
        completeJobs = jobsCompleted;
        completedJobList_LOCK = completedJob_LOCK;
        done = finished;
    }

    public void run()
    {
        try (ObjectOutputStream requestWriter = new ObjectOutputStream(slaveSocket.getOutputStream()))
        {
            while (!done.getIsFinished())
            {
                Job myJob;

                // get length of completed jobs list
                int numDone;
                synchronized (completedJobList_LOCK)
                {
                    numDone = completeJobs.size();
                }

                // if there is a completed job, store it to myJob
                if (numDone > 0)
                {
                    synchronized (completedJobList_LOCK)
                    {
                        myJob = completeJobs.get(0);
                        completeJobs.remove(0);
                    }

                    //update status and send the completed job to the master
                    System.out.println("Sending a job: " + myJob.getClient() + "." + myJob.getType() + myJob.getId() +"\n");
                    myJob.setStatus(JobStatuses.FINISHED_SEND_TO_MASTER);
                    requestWriter.writeObject(myJob);
                }

                if (done.getIsFinished())
                {
                    done.setFinished(true);
                    requestWriter.close();
                }

            }
        }
        catch (Exception e)
        {
            System.out.println("Detected Master exit. Exiting Slave.");
            System.exit(0);
        }
    }
}
