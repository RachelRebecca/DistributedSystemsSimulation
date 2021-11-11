package resources;

public enum JobStatuses
{
    UNFINISHED_SEND_TO_MASTER, //client to master
    ACK_MASTER_RECEIVED, // m from client
    UNFINISHED_SEND_TO_SLAVE,  //m to s
    ACK_SLAVE_RECEIVED, //s from master
    FINISHED_SEND_TO_MASTER, //s to m
    ACK_MASTER_RECEIVED_FINISHED, // m from slave
    FINISHED_SEND_TO_CLIENT, //m to client
}
