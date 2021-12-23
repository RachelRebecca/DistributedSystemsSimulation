package master;

import resources.*;

/**
 * Class which calculates the Load Balancing algorithm
 */
public class LoadBalance
{
    final static Integer longTime = 10000;
    final static Integer shortTime = 2000;

    /**
     * If the time it would take to add an A job to Slave A would exceed
     * the time it would take for Slave B to do the job, send it to Slave B, despite using longTime.
     * Vice versa for sending a B job to Slave B
     *
     * @param timeTrackerForSlaveA - the current time on Slave A
     * @param timeTrackerForSlaveB - the current time on Slave B
     * @param timeTrackerForSlave_LOCK - the lock object to synchronize
     * @param newJob - the new job attempting to be added
     * @return the SlaveType of the slave which the load balancing algorithm chooses to send the job to
     */
    public static SlaveTypes loadBalance(TimeTrackerForSlave timeTrackerForSlaveA, TimeTrackerForSlave timeTrackerForSlaveB,
                                         Object timeTrackerForSlave_LOCK, Job newJob)
    {
        int slaveATime = 0;
        int slaveBTime = 0;
        SlaveTypes slaveType;

        // Assign job times for Slave A and Slave B
        if (newJob.getType().equals(JobTypes.A))
        {
            slaveATime = shortTime;
            slaveBTime = longTime;
        }
        else if (newJob.getType().equals(JobTypes.B))
        {
            slaveATime = longTime;
            slaveBTime = shortTime;
        }

        // compare the two slave objects to see which one has the greater amount of time if it takes on the new job
        int comparison;
        synchronized (timeTrackerForSlave_LOCK)
        {
            comparison = (timeTrackerForSlaveA.getTime() + slaveATime
                    - (timeTrackerForSlaveB.getTime() + slaveBTime));
        }

        // If Slave A time is less than Slave B time, give the job to Slave A
        if (comparison < 0)
        {
            slaveType = SlaveTypes.A;
        }
        // If Slave A time and Slave B time are exactly equal, give an A job to Slave A and a B Job to Slave B
        else if (comparison == 0)
        {
            if (newJob.getType().equals(JobTypes.A))
            {
                slaveType = SlaveTypes.A;
            }
            else
            {
                slaveType = SlaveTypes.B;
            }
        }
        // If Slave B time is less than Slave A time, give the job to Slave B
        else
        {
            slaveType = SlaveTypes.B;
        }

        return slaveType;
    }
}
