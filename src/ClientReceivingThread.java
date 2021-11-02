import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;

public class ClientReceivingThread extends Thread
{
    private Socket clientSocket;
    private ArrayList<Job> unreceivedList;
    private final Object unreceived_LOCK;
    private ArrayList<Job> unfinishedList;
    private final Object unfinished_LOCK;

    public ClientReceivingThread(Socket clientSocket, ArrayList<Job> unreceivedList, Object unreceived_LOCK,
                                 ArrayList<Job> unfinishedList, Object unfinished_LOCK)
    {
        this.clientSocket = clientSocket;
        this.unreceivedList = unreceivedList;
        this.unreceived_LOCK = unreceived_LOCK;
        this.unfinishedList = unfinishedList;
        this.unfinished_LOCK = unfinished_LOCK;
    }

    @Override
    public void run()
    {
        try (// stream to read object response from server
             ObjectInputStream objectInputStream = new ObjectInputStream(clientSocket.getInputStream())
            )
        {
            Job serverMessage;
            while ((serverMessage = (Job) objectInputStream.readObject()) != null)
            {
                // check if received or completed
                if (serverMessage.getStatus() == JobStatuses.ACK_MASTER_RECEIVED)
                {
                    // move from unreceived to unfinished - synchronize (need to check what)
                    synchronized (unreceived_LOCK)
                    {
                        for (Job job : unreceivedList)
                        {
                            if (job.getClient() == serverMessage.getClient() &&
                                job.getType() == serverMessage.getType() &&
                                job.getId() == serverMessage.getId())
                            {
                                unreceivedList.remove(job);
                                break;
                            }
                        }
                    }

                    synchronized (unfinished_LOCK)
                    {
                        unfinishedList.add(serverMessage);
                    }

                    // output message
                    System.out.println("Job " + serverMessage.getType() + serverMessage.getId() + " was received.");
                }
                else    // todo: maybe add an if (messageIsCompleted)
                {
                    // remove from unfinished - synchronize (maybe)
                    synchronized (unfinished_LOCK)
                    {
                        unfinishedList.remove(serverMessage);
                    }

                    // output message
                    System.out.println("Job " + serverMessage.getType() + serverMessage.getId() + " was finished.");
                }
            }
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }
}
