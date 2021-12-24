package master;

import resources.*;

import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Master receives finished jobs from the Slaves using that slave's specific slave socket
 * Adds finished jobs to shared memory
 */
public class MasterReceivingThreadFromSlave extends Thread
{
    // Socket connecting Slave to Master
    private final Socket slaveSocket;

    // Time Tracker objects for each slave
    private final TimeTrackerForSlave timeTrackerForSlaveA;
    private final TimeTrackerForSlave timeTrackerForSlaveB;
    private final Object timeTracker_LOCK;

    // list of finished jobs completed by Slave (shared memory)
    private final ArrayList<Job> finishedJobs;
    private final Object finishedJobs_LOCK;

    public MasterReceivingThreadFromSlave(Socket slaveSocket, TimeTrackerForSlave timeTrackerForSlaveA,
                                          TimeTrackerForSlave timeTrackerForSlaveB, Object timeTracker_LOCK,
                                          ArrayList<Job> finishedJobs, Object finishedJobs_LOCK)
    {
        this.slaveSocket = slaveSocket;
        this.finishedJobs = finishedJobs;
        this.timeTrackerForSlaveA = timeTrackerForSlaveA;
        this.timeTrackerForSlaveB = timeTrackerForSlaveB;
        this.timeTracker_LOCK = timeTracker_LOCK;
        this.finishedJobs_LOCK = finishedJobs_LOCK;
    }

    @Override
    public void run()
    {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(slaveSocket.getInputStream()))
        {
            Job receivedJob; // fill with jobs sent to Master from the Slave
            while ((receivedJob = (Job) objectInputStream.readObject()) != null)
            {
                if (receivedJob.getStatus() == JobStatuses.FINISHED_SEND_TO_MASTER)
                {
                    //update job status
                    receivedJob.setStatus(JobStatuses.FINISHED_SEND_TO_CLIENT);

                    //add the job to the list of finished jobs that need to be sent to the Client
                    synchronized (finishedJobs_LOCK)
                    {
                        finishedJobs.add(receivedJob);
                    }

                    //set the temporary TimeTracker object as whichever slave sent the completed job
                    TimeTrackerForSlave tempTracker = (receivedJob.getSlaveType().equals(SlaveTypes.A)) ?
                            timeTrackerForSlaveA : timeTrackerForSlaveB;

                    // remove the job time of the completed job specific to that slave
                    synchronized (timeTracker_LOCK)
                    {
                        updateTimeTracker(receivedJob, tempTracker);
                    }

                    System.out.println("Got a completed job from Slave " + receivedJob.getSlaveType() + ": "
                            + receivedJob.getClient() + "." + receivedJob.getType() + receivedJob.getId() + "\n");
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("Detected Slave exit. ");
        }

    }

    /**
     * Removes the time on the selected slave for the completed job
     * @param currJob - the current, finished job to be removed from the total work time the slave currently has
     * @param timeTrackerForSlave - the TimeTrackerForSlave object for the specific slave who completed the job
     */
    public static void updateTimeTracker(Job currJob, TimeTrackerForSlave timeTrackerForSlave)
    {
        if (currJob.getType().equals(JobTypes.A))
        {
            timeTrackerForSlave.removeA();
        }
        else
        {
            timeTrackerForSlave.removeB();
        }
    }
}
