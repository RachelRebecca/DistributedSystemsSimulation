import java.net.*;
import java.io.*;
import java.util.*;

public class ClientThread3 extends Thread
{
    public ClientThread3(Socket clientSocket, Object socket_LOCK)
    {
        //waits for job completed confirmation from master - use clientSocket, socket_LOCK
        System.out.println("Job is complete");
        //should be a way to parse fullID from message from master
        // so it can read to user: "Job" + fullID + "is complete"
    }
}
