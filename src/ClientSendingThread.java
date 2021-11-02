import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ClientSendingThread extends Thread
{
    private Socket clientSocket;
    private  ArrayList<Job> unsentList = new ArrayList<>();
    private final Object unsent_LOCK;
    private ArrayList<Job> unreceivedList;
    private final Object unreceived_LOCK;
    private Done done;

    public ClientSendingThread(Socket clientSocket, ArrayList<Job> unsentList, Object unsent_LOCK,
                               ArrayList<Job> unreceivedList, Object unreceived_LOCK, Done isDone) {
        this.clientSocket = clientSocket;
        this.unreceivedList = unsentList;
        this.unsent_LOCK = unsent_LOCK;
        this.unreceivedList = unreceivedList;
        this.unreceived_LOCK = unreceived_LOCK;
        this.done = isDone;
    }

    @Override
    public void run()
    {
        try
                (PrintWriter requestWriter = // stream to write text requests to server
                     new PrintWriter(clientSocket.getOutputStream(), true)
                )
        {
            while (!done.getIsFinished())
            {
                Job currJob = null;
                if (unsentList.size() > 0)
                {
                    synchronized (unsent_LOCK)
                    {
                        currJob = unsentList.get(0);
                        unsentList.remove(0);
                    }
                    //send to master
                    requestWriter.println(currJob);
                }

                synchronized (unreceived_LOCK)
                {
                    unreceivedList.add(currJob);
                }
            }
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }


}
