package master;

import resources.*;

import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;

public class MasterReceivingThreadFromSlave extends Thread
{
    private final Socket slaveSocket;
    private final TimeTrackerForSlave timeTrackerForSlaveA;
    private final TimeTrackerForSlave timeTrackerForSlaveB;
    private final Object timeTracker_LOCK;
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
        try (// stream to read object response from server
             ObjectInputStream objectInputStream = new ObjectInputStream(slaveSocket.getInputStream()))
        {
            Job receivedJob;
            while ((receivedJob = (Job) objectInputStream.readObject()) != null)
            {
                if (receivedJob.getStatus() == JobStatuses.FINISHED_SEND_TO_MASTER)
                {
                    receivedJob.setStatus(JobStatuses.FINISHED_SEND_TO_CLIENT);
                    synchronized (finishedJobs_LOCK)
                    {
                        finishedJobs.add(receivedJob);
                    }

                    TimeTrackerForSlave tempTracker = (receivedJob.getSlaveType().equals(SlaveTypes.A)) ?
                            timeTrackerForSlaveA : timeTrackerForSlaveB;

                    synchronized (timeTracker_LOCK)
                    {
                        updateTimeTracker(receivedJob, tempTracker);
                    }

                    System.out.println("Got a completed job from slave: " + receivedJob.getClient() + "." +
                            receivedJob.getType() + receivedJob.getId());
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("Master receiving to slave error: " + e.getMessage());
        }

    }

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
