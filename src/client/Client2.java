package client;

import resources.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;

public class Client2
{

    // IP address = "127.0.0.1"
    // port = 30121

    public static void main(String[] args)
    {
        if (args.length != 3 || isNotInteger(args[1]) || isNotInteger(args[2]))
        {
            System.err.println("Usage: java Client <host name> <port number> <id>");
            System.exit(1);
        }

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);
        int jobId = 1;
        int clientId = Integer.parseInt(args[2]);

        ArrayList<Job> unsentList = new ArrayList<>();
        Object unsentList_LOCK = new Object();
        Done done = new Done();

        try
                (
                        Socket clientSocket = new Socket(hostName, portNumber);
                        BufferedReader stdIn = // standard input stream to get user's requests
                                new BufferedReader(
                                        new InputStreamReader(System.in))
                )
        {
            ClientSendingThread sendingThread = new ClientSendingThread(clientSocket, unsentList, unsentList_LOCK, done);
            ClientReceivingThread receivingThread = new ClientReceivingThread(clientSocket);

            sendingThread.start();
            receivingThread.start();

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
                        Job job = new Job(clientId, JobTypes.A, jobId, JobStatuses.UNFINISHED_SEND_TO_MASTER);
                        synchronized (unsentList_LOCK)
                        {
                            unsentList.add(job);
                        }
                        System.out.println("Created new A job: " + job.getType() + job.getId());
                        break;
                    }
                    case "B":
                    case "b":
                    {
                        Job job = new Job(clientId, JobTypes.B, jobId, JobStatuses.UNFINISHED_SEND_TO_MASTER);
                        synchronized (unsentList_LOCK)
                        {
                            unsentList.add(job);
                        }
                        System.out.println("Created new B job: " + job.getType() + job.getId());
                        break;
                    }
                }
                jobId++;
            }

            done.setFinished(true);

            try
            {
                sendingThread.join();
                receivingThread.join();
            }
            catch (Exception e)
            {
                System.out.println(e.getMessage());
            }
            System.exit(0);
        }
        catch (Exception e)
        {
            System.out.println("Error: " + e.getMessage());
        }
    }


    /**
     * Check if arg is an integer
     * @param arg (String)
     * @return boolean if arg can be parsed as an integer
     */
    private static boolean isNotInteger(String arg)
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
        return !isInteger;
    }
}
