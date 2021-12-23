package slaves;

import resources.*;

import java.util.ArrayList;

/**
 * Class simulating Slave doing its job by sleeping for a certain amount of time
 */
public class SlaveDoJob extends Thread
{
    // list of incomplete jobs from which Slave gets its next job to complete (shared memory)
    private final ArrayList<Job> incompleteJobs;
    private final Object incompleteJobs_LOCK;

    // list of completed jobs to send back to Master after Slave completes job (shared memory)
    private final ArrayList<Job> completedJobs;
    private final Object completedJobs_LOCK;

    // time Slave takes to do an A job
    private final int sleepA;

    // time Slave takes to do a B job
    private final int sleepB;

    private final Done isDone;

    public SlaveDoJob(ArrayList<Job> incompleteJobs, Object incompleteJobs_LOCK, ArrayList<Job> completedJobs,
                      Object completedJobs_LOCK, int sleepA, int sleepB, Done isDone)
    {
        this.incompleteJobs = incompleteJobs;
        this.incompleteJobs_LOCK = incompleteJobs_LOCK;
        this.completedJobs = completedJobs;
        this.completedJobs_LOCK = completedJobs_LOCK;
        this.sleepA = sleepA;
        this.sleepB = sleepB;
        this.isDone = isDone;
    }

    @Override
    public void run()
    {
        try
        {
            while (!isDone.getIsFinished())
            {
                Job myJob;

                // get size of incomplete jobs list
                int length;
                synchronized (incompleteJobs_LOCK)
                {
                    length = incompleteJobs.size();
                }

                //if there is a job that needs to be completed, assign it to myJob
                if (length > 0)
                {
                    synchronized (incompleteJobs_LOCK)
                    {
                        myJob = incompleteJobs.get(0);
                        incompleteJobs.remove(0);
                    }

                    // If it's an A job, do the A sleep, otherwise do the B sleep
                    if (myJob.getType() == JobTypes.A)
                    {
                        System.out.println("Doing an A sleep for job " + myJob.getClient() + "." + myJob.getType()
                                + myJob.getId() + "\n");
                        Thread.sleep(sleepA);
                    }
                    else
                    {
                        System.out.println("Doing a B sleep for job " + myJob.getClient() + "." + myJob.getType()
                                + myJob.getId() + "\n");
                        Thread.sleep(sleepB);
                    }

                    // add the completed job to the list of completed jobs
                    synchronized (completedJobs_LOCK)
                    {
                        completedJobs.add(myJob);
                    }
                }

                if (isDone.getIsFinished())
                {
                    isDone.setFinished(true);
                }
            }
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }
}
