package master;

import resources.*;

/**
 * Class to store the current time for a Slave to complete its job
 */
public class TimeTrackerForSlave
{

    private int time;       // total time on slave
    private int aTime;      // time it takes to complete an A job
    private int bTime;      // time it takes to complete a B job
    private final SlaveTypes slaveType; // A or B

    public TimeTrackerForSlave(SlaveTypes slaveTypeChar)
    {
        slaveType = slaveTypeChar;
        setABTime();
        time = 0;
    }

    /**
     * Set the A job time and B job time for the Slave
     */
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

    /**
     * Getter for total current time on the slave
     * @return total time on the slave
     */
    public int getTime()
    {
        return time;
    }

    /**
     * Add time for an A job
     */
    public void addA()
    {
        time += aTime;
    }

    /**
     * Add time for a B job
     */
    public void addB()
    {
        time += bTime;
    }

    /**
     * Remove A job time
     */
    public void removeA()
    {
        time -= aTime;
    }

    /**
     * Remove B job time
     */
    public void removeB()
    {
        time -= bTime;
    }
}
