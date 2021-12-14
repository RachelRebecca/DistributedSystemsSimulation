package master;

import resources.*;

public class LoadBalance
{
    final static Integer longTime = 10000;
    final static Integer shortTime = 2000;

    public static SlaveTypes loadBalance(TimeTrackerForSlave timeTrackerForSlaveA, TimeTrackerForSlave timeTrackerForSlaveB,
                                         Object timeTrackerForSlave_LOCK, Job newJob)
    {
        int slaveATime = 0;
        int slaveBTime = 0;
        SlaveTypes slaveType;

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

        int comparison;
        synchronized (timeTrackerForSlave_LOCK)
        {
            comparison = (timeTrackerForSlaveA.getTime() + slaveATime
                    - (timeTrackerForSlaveB.getTime() + slaveBTime));
        }

        if (comparison < 0)
        {
            slaveType = SlaveTypes.A;
        }
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
        else
        {
            slaveType = SlaveTypes.B;
        }

        return slaveType;
    }

    public static SlaveTypes sendDoneJobAlgorithm(TimeTrackerForSlave timeTrackerForSlaveA, TimeTrackerForSlave timeTrackerForSlaveB,
                                            Object timeTrackerForSlave_LOCK)
    {
        // TODO: return the slave that will take the longest to complete
        SlaveTypes slaveType;
        int comparison;
        synchronized (timeTrackerForSlave_LOCK)
        {
            comparison = (timeTrackerForSlaveA.getTime() - (timeTrackerForSlaveB.getTime()));
        }

        if (comparison < 0)
        {
            slaveType = SlaveTypes.A;
        }
        else
        {
            // arbitrarily send to slave B if the two are equal
            slaveType = SlaveTypes.B;
        }

        return slaveType;
    }
}
