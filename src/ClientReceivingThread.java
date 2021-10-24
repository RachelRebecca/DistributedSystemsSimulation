import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;

public class ClientReceivingThread extends Thread
{
    private Socket clientSocket;
    private ArrayList<String> unreceivedList;
    private final Object unreceived_LOCK;
    private ArrayList<String> unfinishedList;
    private final Object unfinished_LOCK;

    public ClientReceivingThread(Socket clientSocket, ArrayList<String> unreceivedList, Object unreceived_LOCK,
                                 ArrayList<String> unfinishedList, Object unfinished_LOCK)
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
        try (// stream to read text response from server
             BufferedReader responseReader = new BufferedReader(
                     new InputStreamReader(clientSocket.getInputStream()))
            )
        {
            String serverMessage;
            String jobTypeAndID;
            String jobClientTypeAndID;
            while ((serverMessage = responseReader.readLine()) != null)
            {
                jobTypeAndID = serverMessage.substring(2, serverMessage.length() - 3);
                jobClientTypeAndID = serverMessage.substring(0, serverMessage.length() - 3);
                // check if received or completed
                if (messageIsReceived(serverMessage))
                {
                    // move from unreceived to unfinished - synchronize (need to check what)
                    synchronized (unreceived_LOCK)
                    {
                        unreceivedList.remove(jobClientTypeAndID);
                    }

                    synchronized (unfinished_LOCK)
                    {
                        unfinishedList.add(jobClientTypeAndID);
                    }

                    // output message
                    System.out.println("Job " + jobTypeAndID + " was received.");
                }
                else    // todo: maybe add an if (messageIsCompleted)
                {
                    // remove from unfinished - synchronize (maybe)
                    synchronized (unfinished_LOCK)
                    {
                        unfinishedList.remove(jobClientTypeAndID);
                    }

                    // output message
                    System.out.println("Job " + jobTypeAndID + " was finished.");
                }
            }
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }

    private boolean messageIsReceived(String serverMessage)
    {
        // if final char is "2" it's from a message being received
        return Character.getNumericValue(serverMessage.charAt(serverMessage.length() - 1)) == 2;
    }
}
