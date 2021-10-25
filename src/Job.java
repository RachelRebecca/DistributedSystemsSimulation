public class Job {
    private int client;
    private JobTypes type;
    private int Id;
    private JobStatuses status;

    public Job (int client, JobTypes type, int Id, JobStatuses status)
    {
        this.client = client;
        this.type = type;
        this.Id = Id;
        this.status = status;
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
        return Id;
    }

    public JobStatuses getStatus()
    {
        return status;
    }

    public void setClient(int client)
    {
        this.client = client;
    }

    public void setType(JobTypes type)
    {
        this.type = type;
    }

    public void setId(int id)
    {
        Id = id;
    }

    public void setStatus(JobStatuses status)
    {
        this.status = status;
    }
}
