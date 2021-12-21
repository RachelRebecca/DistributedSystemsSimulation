package slaves;

import resources.*;

import java.util.ArrayList;

public class SlaveDoJob extends Thread
{
    private ArrayList<Job> jobsToComplete;
    private final Object jobsToCompleteLock;
    private ArrayList<Job> jobsCompleted;
    private final Object jobsCompletedLock;
    private final int sleepA;
    private final int sleepB;
    private Done isDone;

    public SlaveDoJob(ArrayList<Job> jobsToComplete, Object jobsToCompleteLock, ArrayList<Job> jobsCompleted,
                      Object jobsCompletedLock, int sleepA, int sleepB, Done isDone)
    {
        this.jobsToComplete = jobsToComplete;
        this.jobsToCompleteLock = jobsToCompleteLock;
        this.jobsCompleted = jobsCompleted;
        this.jobsCompletedLock = jobsCompletedLock;
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
                int length;
                Job myJob;
                synchronized (jobsToCompleteLock)
                {
                    length = jobsToComplete.size();
                }
                if (length > 0)
                {
                    synchronized (jobsToCompleteLock)
                    {
                        myJob = jobsToComplete.get(0);
                        jobsToComplete.remove(0);
                    }

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

                    synchronized (jobsCompletedLock)
                    {
                        jobsCompleted.add(myJob);
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
