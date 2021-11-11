import resources.Done;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;

public class Client
{

    // IP address = "127.0.0.1"
    // port = 30121

    private static int id = 1;
    private static ArrayList<Job> unsentList;
    private static Object unsentList_LOCK;
    private static ArrayList<Job> unreceivedList;
    private static Object unreceivedList_LOCK;
    private static ArrayList<Job> unfinishedList;
    private static Object unfinishedList_LOCK;


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
        unsentList = new ArrayList<>();
        unsentList_LOCK = new Object();
        unreceivedList = new ArrayList<>();
        unreceivedList_LOCK = new Object();
        unfinishedList = new ArrayList<>();
        unfinishedList_LOCK = new Object();
        Done done = new Done();

        try
                (
                        Socket clientSocket = new Socket(hostName, portNumber);
                        BufferedReader stdIn = // standard input stream to get user's requests
                            new BufferedReader(
                                 new InputStreamReader(System.in))
                )
        {
            //(arbitrarily) making 1 (want to ultimately switch to 3) of each kind of thread
            for (int i=0; i < 1; i++)
            {
                sendingThreads.add(new ClientSendingThread(clientSocket, unsentList, unsentList_LOCK,
                      unreceivedList, unreceivedList_LOCK, done));
                receivingThreads.add(new ClientReceivingThread(clientSocket, unreceivedList, unreceivedList_LOCK,
                      unfinishedList, unfinishedList_LOCK));

            }

            for (Thread sThread: sendingThreads)
            {
                sThread.start();
            }

            for (Thread rThread: receivingThreads)
            {
                rThread.start();
            }

            label:
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

                switch (jobType)
                {
                    case "0":
                        break label;
                    case "A":
                    case "a":
                    {
                        Job job = new Job(1, JobTypes.A, id, JobStatuses.UNFINISHED_SEND_TO_MASTER);
                        synchronized (unsentList_LOCK)
                        {
                            unsentList.add(job);
                        }
                        System.out.println("Created new A job: " + job.getId());
                        break;
                    }
                    case "B":
                    case "b":
                    {
                        Job job = new Job(1, JobTypes.B, id, JobStatuses.UNFINISHED_SEND_TO_MASTER);
                        synchronized (unsentList_LOCK)
                        {
                            unsentList.add(job);
                        }
                        System.out.println("Created new B job");
                        break;
                    }
                }
                id++;
            }

            done.setFinished(true);
            System.out.println("resources.Done has been updated in Client.");

            try
            {
                for (Thread sThread : sendingThreads)
                {
                    sThread.join();
                }
                for (Thread rThread : receivingThreads)
                {
                    rThread.join();
                }
            }
            catch (Exception e)
            {
                System.out.println(e.getMessage());
            }
            System.out.println("Exiting. Thank you for your participation.");
            System.exit(0);
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
