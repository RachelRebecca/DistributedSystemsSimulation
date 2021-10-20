package to_be_deleted;

import java.net.*;
import java.util.*;
import java.io.*;

public class ClientThread2 extends Thread
{
    public ClientThread2(String fullID, Socket clientSocket, Object socket_LOCK,
                         ArrayList<String> unfinishedList, Object unfinished_LOCK)
    {
        // wait for acknowledgement that job has been received by master - use ClientSocket and socket_LOCK
        System.out.println("Message " + fullID + "has been sent.");
        unfinishedList.add(fullID); //update for to_be_deleted.Client as well? Use LOCK
    }
}
