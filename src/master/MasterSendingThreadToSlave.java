package master;

import resources.*;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Master sends incomplete jobs to the Slave to complete
 * Adds jobs to the unfinished list shared memory
 */
public class MasterSendingThreadToSlave extends Thread
{
    // Socket objects connecting Slave A and Slave B to the Master
    private final Socket slaveA;
    private final Socket slaveB;

    // TimeTracker objects to store current time on each slave
    private final TimeTrackerForSlave timeTrackerForSlaveA;
    private final TimeTrackerForSlave timeTrackerForSlaveB;
    private final Object timeTrackerForSlave_LOCK;

    // list of unfinished jobs that need to be completed by slave (shared memory)
    private final ArrayList<Job> unfinishedJobs;
    private final Object unfinishedJobs_LOCK;

    private final Done done;

    public MasterSendingThreadToSlave(Socket slaveSocketA, Socket slaveSocketB,
                                      TimeTrackerForSlave timeTrackerA, TimeTrackerForSlave timeTrackerB,
                                      Object timeTrackerForSlave_lock,
                                      ArrayList<Job> unfinishedJobs, Object unfinishedJobs_LOCK, Done done)
    {
        slaveA = slaveSocketA;
        slaveB = slaveSocketB;
        timeTrackerForSlaveA = timeTrackerA;
        timeTrackerForSlaveB = timeTrackerB;
        this.timeTrackerForSlave_LOCK = timeTrackerForSlave_lock;
        this.unfinishedJobs = unfinishedJobs;
        this.unfinishedJobs_LOCK = unfinishedJobs_LOCK;
        this.done = done;
    }

    @Override
    public void run()
    {
        try (ObjectOutputStream objectOutputStreamSlaveA = new ObjectOutputStream(slaveA.getOutputStream());
             ObjectOutputStream objectOutputStreamSlaveB = new ObjectOutputStream(slaveB.getOutputStream()))
        {
            while (!done.isFinished())
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
                    // if there is, store the first job as currJob
                    synchronized (unfinishedJobs_LOCK)
                    {
                        currJob = unfinishedJobs.get(0);
                        unfinishedJobs.remove(0);
                    }

                    // Use Load Balancing Algorithm to determine which slave should be sent currJob
                    SlaveTypes slave = LoadBalance.loadBalance(timeTrackerForSlaveA, timeTrackerForSlaveB,
                            timeTrackerForSlave_LOCK, currJob);
                    System.out.println("Sending job " + currJob.getClient() + "." + currJob.getType() + currJob.getId()
                            + " to Slave " + slave.name() + "\n");
                    currJob.setSlaveType(slave);

                    // Send the job to the slave using the specific slave's ObjectOutputStream
                    // Update that slave's TimeTracker object to account for the new job
                    if (slave.equals(SlaveTypes.A))
                    {
                        objectOutputStreamSlaveA.writeObject(currJob);
                        synchronized (timeTrackerForSlave_LOCK)
                        {
                            updateTimeTracker(currJob, timeTrackerForSlaveA);
                        }
                    }
                    else
                    {
                        objectOutputStreamSlaveB.writeObject(currJob);
                        synchronized (timeTrackerForSlave_LOCK)
                        {
                            updateTimeTracker(currJob, timeTrackerForSlaveB);
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("Detected Slave exit. ");
        }
    }

    /**
     * Update Time Tracker Object belonging to the slave based on the current job type
     *      - An object for Slave A, Job A or Slave B, Job B adds 2000
     *      - An object for Slave A, Job B or Slave B, Job A adds 10000
     * @param currJob - the current job
     * @param timeTrackerForSlave - the object for the specific slave being updated
     */
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
