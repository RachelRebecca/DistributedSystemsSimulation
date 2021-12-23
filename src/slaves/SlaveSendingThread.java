package slaves;

import resources.*;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Slave sends jobs to Master using its Slave Socket
 * It only sends jobs that it has completed
 */
public class SlaveSendingThread extends Thread
{
    // Socket connecting Slave to Master
    private final Socket slaveSocket;

    // list of completed jobs which Slave is sending back to Master (shared memory)
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
            while (!done.isFinished())
            {
                Job myJob;

                // get length of completed jobs list
                int numDone;
                synchronized (completedJobList_LOCK)
                {
                    numDone = completeJobs.size();
                }

                // if there is a completed job, store it as myJob
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
            }
        }
        catch (Exception e)
        {
            System.out.println("Detected Master exit. Exiting Slave.");
            System.exit(0);
        }
    }
}
