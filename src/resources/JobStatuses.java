package resources;

/**
 * Enum class for current Job statuses, used to track where the job is at each stage
 */
public enum JobStatuses
{
    UNFINISHED_SEND_TO_MASTER ("job sent to master from client"),   //client to master
    UNFINISHED_SEND_TO_SLAVE ("job sent to slave from master"),     //master to slave
    FINISHED_SEND_TO_MASTER ("job sent to master from slave"),      //slave to master
    FINISHED_SEND_TO_CLIENT ("finished job sent to client"),        //master to client
    CLIENT_DONE ("client is done");

    private final String status;

    JobStatuses(String statuses)
    {
        status = statuses;
    }

    public String toString()
    {
        return status;
    }
}
