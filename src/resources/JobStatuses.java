package resources;

public enum JobStatuses
{
    UNFINISHED_SEND_TO_MASTER ("job sent to master from client"), //client to master
//    ACK_MASTER_RECEIVED ("acknowledge master received job from client"), // m from client
    UNFINISHED_SEND_TO_SLAVE ("job sent to slave from master"),  //m to s
//    ACK_SLAVE_RECEIVED ("acknowledge slave received job from master"), //s from master
    FINISHED_SEND_TO_MASTER ("job sent to master from slave"), //s to m
//    ACK_MASTER_RECEIVED_FINISHED ("acknowledge master received from slave"), // m from slave
    FINISHED_SEND_TO_CLIENT ("finished job sent to client"); //m to client

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
