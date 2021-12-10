package resources;

public enum JobStatuses
{
    UNFINISHED_SEND_TO_MASTER ("job sent to master from client"), //client to master
    UNFINISHED_SEND_TO_SLAVE ("job sent to slave from master"),  //m to s
    FINISHED_SEND_TO_MASTER ("job sent to master from slave"), //s to m
    FINISHED_SEND_TO_CLIENT ("finished job sent to client"), //m to client
    IS_SLAVE_A ("slave is type A"), //slave announcement to m
    IS_SLAVE_B ("slave is type B"), //slave announcement to m,
    CLIENT_DONE ("client is done"),
    DONE ("exit program");

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
