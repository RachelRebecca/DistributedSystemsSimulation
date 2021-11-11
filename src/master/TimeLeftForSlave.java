package master;

import resources.SlaveTypes;

public class TimeLeftForSlave
{
    private int time;
    private int aTime;
    private int bTime;
    private SlaveTypes slaveType;

    public TimeLeftForSlave(SlaveTypes slaveTypeChar)
    {
        slaveType = slaveTypeChar;
        setABTime();
        time = 0;
    }

    public void setABTime()
    {
        if (slaveType.equals(SlaveTypes.A))
        {
            aTime = 2000;
            bTime = 10000;
        }
        else if (slaveType.equals(SlaveTypes.B))
        {
            aTime = 10000;
            bTime = 2000;
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
