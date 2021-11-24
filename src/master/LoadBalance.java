package master;

import resources.*;

public class LoadBalance
{
    public static SlaveTypes loadBalance(TimeTrackerForSlave timeTrackerForSlaveA, TimeTrackerForSlave timeTrackerForSlaveB, Job newJob)
    {
        int slaveATime=0;
        int slaveBTime=0;
        SlaveTypes slaveType = SlaveTypes.NULL;

        if (newJob.getType().equals(JobTypes.A))
        {
            slaveATime = 2000;
            slaveBTime = 10000;
        }
        else if (newJob.getType().equals(JobTypes.B))
        {
            slaveATime = 10000;
            slaveBTime = 2000;
        }

        int comparison = (timeTrackerForSlaveA.getTime() + slaveATime
                - (timeTrackerForSlaveB.getTime() + slaveBTime));

        JobTypes type = newJob.getType();
        if (comparison < 0)
        {
            // send to slave A    //how do you send to slave A??
            slaveType = SlaveTypes.A;
            if (newJob.getType().equals(JobTypes.A))
            {
                timeTrackerForSlaveA.addA(); // this should be in MasterSendingThreadToSlave
            } else
            {
                timeTrackerForSlaveA.addB(); // this should be in MasterSendingThreadToSlave
            }


        } else if (comparison == 0)
        {
            if (newJob.getType().equals(JobTypes.A))
            {
                slaveType = SlaveTypes.A;
                timeTrackerForSlaveA.addA();
            } else
            {
                slaveType = SlaveTypes.B;
                timeTrackerForSlaveB.addB();
            }
        } else
        {
            slaveType = SlaveTypes.B;

            if (newJob.getType().equals(JobTypes.A))
            {
                timeTrackerForSlaveB.addA();
            } else
            {
                timeTrackerForSlaveB.addB();
            }
        }

        return slaveType;

    }
}
