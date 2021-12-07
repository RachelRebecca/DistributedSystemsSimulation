package master;

import resources.*;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class MasterSendingThreadToSlave extends Thread
{

    private final Socket slaveA;
    private final Socket slaveB;
    private final TimeTrackerForSlave timeTrackerForSlaveA;
    private final TimeTrackerForSlave timeTrackerForSlaveB;
    private final ArrayList<Job> unfinishedJobs;
    private final Object unfinishedJobs_LOCK;
    private final Done done;

    public MasterSendingThreadToSlave(Socket slaveSocketA, Socket slaveSocketB,
                                      TimeTrackerForSlave timeTrackerA, TimeTrackerForSlave timeTrackerB,
                                      ArrayList<Job> unfinishedJobs, Object unfinishedJobs_LOCK, Done isDone)
    {
        slaveA = slaveSocketA;
        slaveB = slaveSocketB;
        timeTrackerForSlaveA = timeTrackerA;
        timeTrackerForSlaveB = timeTrackerB;
        this.unfinishedJobs = unfinishedJobs;
        this.unfinishedJobs_LOCK = unfinishedJobs_LOCK;
        done = isDone;
    }

    @Override
    public void run()
    {
        try
                (ObjectOutputStream objectOutputStreamSlaveA = new ObjectOutputStream(slaveA.getOutputStream());
                ObjectOutputStream objectOutputStreamSlaveB = new ObjectOutputStream(slaveB.getOutputStream())
                )
        {
            while (!done.getIsFinished())
            {
                // check if there is a job to send
                Job currJob;
                int unsentSize;
                synchronized (unfinishedJobs_LOCK)
                {
                    unsentSize = unfinishedJobs.size();
                }

                if (unsentSize > 0)
                {
                    // if there is, get the first job, send it to the best slave, and update the slave time tracker
                    synchronized (unfinishedJobs_LOCK)
                    {
                        currJob = unfinishedJobs.get(0);
                        System.out.println("current job [from master sending] is " + currJob);
                        unfinishedJobs.remove(0);
                    }

                    SlaveTypes slave = LoadBalance.loadBalance(timeTrackerForSlaveA, timeTrackerForSlaveB, currJob);
                    System.out.println("Sending to " + slave.name());
                    if (slave.equals(SlaveTypes.A))
                    {
                        objectOutputStreamSlaveA.writeObject(currJob);
                        updateTimeTracker(currJob, timeTrackerForSlaveA);
                    }
                    else
                    {
                        objectOutputStreamSlaveB.writeObject(currJob);
                        updateTimeTracker(currJob, timeTrackerForSlaveB);
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
        System.out.println("exiting thread.");
    }

    //shouldn't this be SLAVETYPE not jobtype?
    public static void updateTimeTracker(Job currJob, TimeTrackerForSlave timeTrackerForSlave)
    {
        if (currJob.getType().equals(JobTypes.A))
        {
            timeTrackerForSlave.addA();
        }
        else
        {
            timeTrackerForSlave.addB();
        }
    }
}
