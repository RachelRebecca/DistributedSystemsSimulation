package resources;

import java.io.Serializable;

public class Job implements Serializable
{
    private int client;
    private SlaveTypes slaveType;
    private JobTypes type;
    private int id;
    private JobStatuses status;

    public Job (int clientNumber, JobTypes jobType, int Id, JobStatuses jobStatus)
    {
        client = clientNumber;
        type = jobType;
        id = Id;
        status = jobStatus;
        slaveType = SlaveTypes.NULL;
    }

    public int getClient()
    {
        return client;
    }

    public JobTypes getType()
    {
        return type;
    }

    public int getId()
    {
        return id;
    }

    public JobStatuses getStatus()
    {
        return status;
    }

    public SlaveTypes getSlaveType()
    {
        return slaveType;
    }

    public void setClient(int clientNumber)
    {
        client = clientNumber;
    }

    public void setType(JobTypes jobType)
    {
        type = jobType;
    }

    public void setId(int Id)
    {
        id = Id;
    }

    public void setStatus(JobStatuses jobStatus)
    {
        status = jobStatus;
    }

    public void setSlaveType(SlaveTypes st)
    {
        slaveType = st;
    }

}
