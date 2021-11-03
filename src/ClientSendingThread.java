import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class ClientSendingThread extends Thread
{
    private Socket clientSocket;
    private  ArrayList<Job> unsentList;
    private final Object unsent_LOCK;
    private ArrayList<Job> unreceivedList;
    private final Object unreceived_LOCK;
    private Done done;

    public ClientSendingThread(Socket clientSocket, ArrayList<Job> unsentList, Object unsent_LOCK,
                               ArrayList<Job> unreceivedList, Object unreceived_LOCK, Done isDone) {
        this.clientSocket = clientSocket;
        this.unsentList = unsentList;
        this.unsent_LOCK = unsent_LOCK;
        this.unreceivedList = unreceivedList;
        this.unreceived_LOCK = unreceived_LOCK;
        this.done = isDone;
    }

    @Override
    public void run()
    {
        System.out.println("Starting run of sending thread");
        try
                (ObjectOutputStream requestWriter = // stream to write text requests to server
                     new ObjectOutputStream(clientSocket.getOutputStream())
                )
        {
            while (!done.getIsFinished())
            {
                Job currJob;
                if (unsentList.size() > 0)
                {
                    System.out.println("There is an unsent job: unsent.size is " + unsentList.size());
                    synchronized (unsent_LOCK)
                    {
                        currJob = unsentList.get(0);
                        unsentList.remove(0);
                    }
                    //send to master
                    System.out.println("Sending to master");
                    requestWriter.writeObject(currJob);

                    synchronized (unreceived_LOCK)
                    {
                        unreceivedList.add(currJob);
                    }
                }

                if (done.getIsFinished())
                {
                    System.out.println("Done is finished.");
                    done.setFinished(true);
                }
            }
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        System.out.println("exiting thread.");
    }
}
