package slaves;

import resources.*;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class SlaveSendingThread extends Thread
{
    private Socket slaveSocket;
    private ArrayList<Job> completeJobs;
    private final Object completedJobList_LOCK;
    private Done done;
    private boolean isFirstRun = true;
    private SlaveTypes slaveType;


    //what goes into the constructor?? <-- Chaya's comment :)
    public SlaveSendingThread(Socket socket, ArrayList<Job> jobsCompleted, Object completedJob_LOCK, Done finished, SlaveTypes slaveType)
    {
        slaveSocket = socket;
        completeJobs = jobsCompleted;
        completedJobList_LOCK = completedJob_LOCK;
        done = finished;
        this.slaveType = slaveType;
    }

    public void run()
    {
        try (ObjectOutputStream requestWriter = // stream to write text requests to server
                     new ObjectOutputStream(slaveSocket.getOutputStream()))
        {

            if (slaveType.equals(SlaveTypes.A))
            {
                System.out.println("I'm an A Slave");
                requestWriter.writeObject(new Job(-1, JobTypes.NULL, -1, JobStatuses.IS_SLAVE_A));
            }
            else
            {
                System.out.println("I'm a B Slave");
                requestWriter.writeObject(new Job(-1, JobTypes.NULL, -1, JobStatuses.IS_SLAVE_B));
            }


            while (!done.getIsFinished())
            {
                Job myJob;
                /*
                if (isFirstRun)
                {
                    // send identification job
                    if (slaveType.equals(SlaveTypes.A))
                    {
                        requestWriter.writeObject(new Job(-1, JobTypes.NULL, -1, JobStatuses.IS_SLAVE_A));
                    }
                    else
                    {
                        requestWriter.writeObject(new Job(-1, JobTypes.NULL, -1, JobStatuses.IS_SLAVE_B));
                    }
                    isFirstRun = false;
                    continue;
                }
                 */
                int numDone;
                synchronized (completedJobList_LOCK)
                {
                    numDone = completeJobs.size();
                }

                if (numDone > 0)
                {
                    System.out.print("There's a finished job!");
                    synchronized (completedJobList_LOCK)
                    {
                        myJob = completeJobs.get(0);
                        completeJobs.remove(0);
                    }
                    System.out.println("" + myJob.getType() + myJob.getId());
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
            System.out.println("Something happened");
            // something
        }

    }

}
