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
    private final Object timeTrackerForSlave_LOCK;
    private final ArrayList<Job> unfinishedJobs;
    private final Object unfinishedJobs_LOCK;
    private final Done done;

    private int slaveId;
    private SlaveTypes myType;

    private ArrayList<Socket> slaveSockets;
    private final Object slaveSockets_LOCK;

    public MasterSendingThreadToSlave(Socket slaveSocketA, Socket slaveSocketB,  ArrayList<Socket> slaveSockets,
                                      Object slaveSockets_LOCK, TimeTrackerForSlave timeTrackerA,
                                      TimeTrackerForSlave timeTrackerB,
                                      Object timeTrackerForSlave_lock, /*int slaveId, SlaveTypes myType,*/
                                      ArrayList<Job> unfinishedJobs, Object unfinishedJobs_LOCK, Done isDone)
    {
        slaveA = slaveSocketA;
        slaveB = slaveSocketB;
        this.slaveSockets = slaveSockets;
        this.slaveSockets_LOCK = slaveSockets_LOCK;

        timeTrackerForSlaveA = timeTrackerA;
        timeTrackerForSlaveB = timeTrackerB;
        this.timeTrackerForSlave_LOCK = timeTrackerForSlave_lock;
        this.unfinishedJobs = unfinishedJobs;
        this.unfinishedJobs_LOCK = unfinishedJobs_LOCK;
        done = isDone;

        /*
        this.slaveId = slaveId;
        this.myType = myType;
         */
    }

    @Override
    public void run()
    {
        try
                (ObjectOutputStream objectOutputStreamSlaveA = new ObjectOutputStream(slaveA.getOutputStream());
                ObjectOutputStream objectOutputStreamSlaveB = new ObjectOutputStream(slaveB.getOutputStream()))
        {
            while (!done.getIsFinished())
            {
                /*
                int slaveNumber;
                synchronized (slaveSockets_LOCK)
                {
                    slaveNumber = slaveSockets.size() - 1;
                }
                 */

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
                        System.out.println("current job [from master sending] is " + currJob.getType() + currJob.getId());
                        unfinishedJobs.remove(0);
                    }

                    int slave = LoadBalance.loadBalance(timeTrackerForSlaveA, timeTrackerForSlaveB, timeTrackerForSlave_LOCK,
                            currJob);
                    System.out.println("Sending to " + slave/*.name()*/);
                    //currJob.setSlaveType(slave);

                    /*
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
                     */
                }

                if (done.getIsFinished())
                {
                    done.setFinished(true);
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("Master sending to slave error: " + e.getMessage());
        }
    }

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
