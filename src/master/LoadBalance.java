package master;

import resources.*;

public class LoadBalance
{
    final static Integer longTime = 10000;
    final static Integer shortTime = 2000;

    public static SlaveTypes loadBalance(TimeTrackerForSlave timeTrackerForSlaveA, TimeTrackerForSlave timeTrackerForSlaveB, Job newJob)
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

        int comparison = (timeTrackerForSlaveA.getTime() + slaveATime
                - (timeTrackerForSlaveB.getTime() + slaveBTime));

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
}
