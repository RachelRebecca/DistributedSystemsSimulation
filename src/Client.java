import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Client
{
    private static final String clientLetter = "C";
    private static int id = 1;

    public static void main(String[] args)
    {
        if (args.length != 2 || !isInteger(args[1]))
        {
            System.err.println("Usage: java EchoClient <host name> <port number>");
            System.exit(1);
        }

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);

        ArrayList<Thread> sendingThreads = new ArrayList<>();
        ArrayList<Thread> receivingThreads = new ArrayList<>();
        ArrayList<String> unsentStringIds = new ArrayList<>();
        Object unsentList_LOCK = new Object();
        ArrayList<String> unreceivedStringIds = new ArrayList<>();
        Object unreceivedList_LOCK = new Object();
        ArrayList<String> unfinishedStringIds = new ArrayList<>();
        Object unfinishedList_LOCK = new Object();
        Object socket_LOCK = new Object();

        try
                (
                        Socket clientSocket = new Socket(hostName, portNumber);
                        PrintWriter requestWriter = // stream to write text requests to server
                            new PrintWriter(clientSocket.getOutputStream(), true);
                        BufferedReader responseReader = // stream to read text response from server
                            new BufferedReader(
                                 new InputStreamReader(clientSocket.getInputStream()));
                        BufferedReader stdIn = // standard input stream to get user's requests
                            new BufferedReader(
                                 new InputStreamReader(System.in))
                )
        {
            //arbitrarily making 3 of each kind of thread
            for (int i=0; i < 3; i++)
            {
                sendingThreads.add(new ClientSendingThread(clientSocket, unsentStringIds, unsentList_LOCK,
                        unreceivedStringIds, unreceivedList_LOCK));
                receivingThreads.add(new ClientReceivingThread(clientSocket, unreceivedStringIds, unreceivedList_LOCK,
                        unfinishedStringIds, unfinishedList_LOCK));
            }

            for (Thread sThread: sendingThreads)
            {
                sThread.start();
            }

            for (Thread rThread: receivingThreads)
            {
                rThread.start();
            }

            while (true)
            {
                System.out.println ("Please enter job type A or B or exit by entering 0");
                String jobType = stdIn.readLine();
                while (!jobType.equalsIgnoreCase("A")
                        && !jobType.equalsIgnoreCase("B")
                        && !jobType.equals("0"))
                {
                    System.out.println ("User input not accepted: please enter job type A or B or exit by entering 0");
                    jobType = stdIn.readLine();
                }

                if (jobType.equals("0"))
                {
                    break;
                }
                else if (jobType.equals("A") || jobType.equals("a"))
                {
                    String fullID = clientLetter + ".A." + id;
                    unsentStringIds.add(fullID);
                }
                else if (jobType.equals("B") || jobType.equals("b"))
                {
                    String fullID = clientLetter + ".B." + id;
                    unsentStringIds.add(fullID);
                }
                id++;
            }

            for (Thread sThread: sendingThreads)
            {
                sThread.join();
            }
            for (Thread rThread: receivingThreads)
            {
                rThread.join();
            }
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }


    /**
     * Check if arg is an integer
     * @param arg (String)
     * @return boolean if arg can be parsed as an integer
     */
    private static boolean isInteger(String arg)
    {
        boolean isInteger = true;
        try
        {
            Integer.parseInt(arg);
        }
        catch (Exception e)
        {
            isInteger = false;
        }
        return isInteger;
    }
}
