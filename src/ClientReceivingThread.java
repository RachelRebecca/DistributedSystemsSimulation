import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ClientReceivingThread extends Thread
{
    private Socket clientSocket;
    private ArrayList<String> unreceivedList;
    private Object unreceived_LOCK;
    private ArrayList<String> unfinishedList;
    private Object unfinished_LOCK;

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
                     new InputStreamReader(clientSocket.getInputStream())
             ))
        {
            String serverMessage;
            String jobTypeAndID;
            while ((serverMessage = responseReader.readLine()) != null)
            {
                jobTypeAndID = serverMessage.substring(1, serverMessage.length() - 2);
                // check if received or completed
                if (messageIsReceived(serverMessage))
                {
                    // move from unreceived to unfinished - synchronize (need to check what)

                    // output message
                    System.out.println("Job " + jobTypeAndID + " was received.");
                }
                else    // maybe add an if (messageIsCompleted)
                {
                    // remove from unfinished - synchronize (maybe)

                    // output message
                    System.out.println("Job " + jobTypeAndID + " was finished.");
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private boolean messageIsReceived(String serverMessage)
    {
        // if final char is "2" it's from a message being received
        char finalCharacter = serverMessage.charAt(serverMessage.length() - 1);
        int finalInteger = Character.getNumericValue(finalCharacter);
        return finalInteger == 2;
    }
}
