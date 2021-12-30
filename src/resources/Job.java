package resources;

import java.io.Serializable;

/**
 * Class representing a job
 * Jobs are created by User through Client, passed to Master, and sent to a Slave to complete
 * Slave then sends back to Master the finished job, and Master sends the job back to Client, completed.
 */
public class Job implements Serializable
{
    private int client;                     // Client ID of the Client that sent the job
    private JobTypes type;                  // Job Type (A, B, or null)
    private int id;                         // Job ID
    private JobStatuses status;             // Job status (where job is at each stage of its journey)
    private SlaveTypes slaveType;           // Slave Type (A, B, or null) of Slave assigned to job

    public Job (JobTypes jobType, int Id, JobStatuses jobStatus)
    {
        type = jobType;
        id = Id;
        status = jobStatus;
        slaveType = SlaveTypes.NULL;
    }

    // getters and setters
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
