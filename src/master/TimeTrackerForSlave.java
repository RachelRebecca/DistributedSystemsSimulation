package master;

import resources.*;

/**
 * Class to store the current time for a Slave to complete its job
 */
public class TimeTrackerForSlave
{
    private int time;
    private int aTime;
    private int bTime;
    private final SlaveTypes slaveType;

    public TimeTrackerForSlave(SlaveTypes slaveTypeChar)
    {
        slaveType = slaveTypeChar;
        setABTime();
        time = 0;
    }

    public void setABTime()
    {
        int shortTime = 2000;
        int longTime = 10000;

        // Slave A, Job A = 2000, Slave A, Job B = 10000
        if (slaveType.equals(SlaveTypes.A))
        {
            aTime = shortTime;
            bTime = longTime;
        }
        // Slave B, Job A = 10000, Slave B, Job B = 2000
        else if (slaveType.equals(SlaveTypes.B))
        {
            aTime = longTime;
            bTime = shortTime;
        }
    }

    public int getTime()
    {
        return time;
    }

    public void addA()
    {
        time += aTime;
    }

    public void addB()
    {
        time += bTime;
    }

    public void removeA()
    {
        time -= aTime;
    }

    public void removeB()
    {
        time -= bTime;
    }
}
