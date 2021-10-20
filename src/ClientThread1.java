import java.net.*;
import java.io.*;
import java.util.*;

public class ClientThread1 extends Thread
{
    public ClientThread1(String clientLetter, String jobType, int id, Object id_LOCK, Socket clientSocket, Object socket_LOCk,
                         ArrayList<String> unreceivedList, Object unreceived_LOCK)
    {
        String fullID = clientLetter + "." + jobType + "." + id;

        // id++ - somehow that has to affect the other ID. Do this using ID_LOCK
        //send fullID to master -- in a try/catch?
            //BE AWARE THAT WE NEED TO FIGURE OUT THREAD.SLEEP SITUATION
        unreceivedList.add(fullID); //this has to update to all other threads
    }
}
