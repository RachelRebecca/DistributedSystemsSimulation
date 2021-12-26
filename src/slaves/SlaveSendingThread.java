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
    private final ArrayList<Job> finishedJobs;
    private final Object finishedJobs_LOCK;

    // Done Object - signal when to exit Thread
    private final Done done;
    private final Object done_LOCK;

    // boolean flag to continue while loop
    private boolean continueLoop;

    public SlaveSendingThread(Socket socket, ArrayList<Job> finishedJobs, Object finishedJobs_LOCK,
                              Done done, Object done_LOCK)
    {
        slaveSocket = socket;
        this.finishedJobs = finishedJobs;
        this.finishedJobs_LOCK = finishedJobs_LOCK;
        this.done = done;
        this.done_LOCK = done_LOCK;
        this.continueLoop = true;
    }

    public void run()
    {
        try (ObjectOutputStream requestWriter = new ObjectOutputStream(slaveSocket.getOutputStream()))
        {
            while (continueLoop)
            {
                Job myJob;

                // get length of completed jobs list
                int numDone;
                synchronized (finishedJobs_LOCK)
                {
                    numDone = finishedJobs.size();
                }

                // if there is a completed job, store it as myJob
                if (numDone > 0)
                {
                    synchronized (finishedJobs_LOCK)
                    {
                        myJob = finishedJobs.get(0);
                        finishedJobs.remove(0);
                    }

                    //update status and send the completed job to the master
                    System.out.println("Sending a job: " + myJob.getClient() + "." + myJob.getType() + myJob.getId() +"\n");
                    myJob.setStatus(JobStatuses.FINISHED_SEND_TO_MASTER);
                    requestWriter.writeObject(myJob);
                }

                synchronized (done_LOCK)
                {
                    if (done.isFinished())
                    {
                        continueLoop = false;
                    }
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
