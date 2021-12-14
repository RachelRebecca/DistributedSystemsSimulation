package client;

import resources.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Creates Client
 * - make a Client Socket, and get the jobs from the User
 * - start and join Client Threads
 */
public class Client
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

        // list of jobs that haven't been sent yet to Master
        ArrayList<Job> unsentList = new ArrayList<>();
        Object unsentList_LOCK = new Object();

        ArrayList<Job> unfinishedList = new ArrayList<>();
        Object unfinishedList_LOCK = new Object();

        Done done = new Done();

        try
                (
                        Socket clientSocket = new Socket(hostName, portNumber);
                        BufferedReader stdIn = // standard input stream to get user's requests
                            new BufferedReader(
                                 new InputStreamReader(System.in))
                )
        {
            //each client has exactly one sending thread, and one receiving thread
            ClientSendingThread sendingThread = new ClientSendingThread(clientSocket, unsentList, unsentList_LOCK,
                    unfinishedList, unfinishedList_LOCK, done);
            ClientReceivingThread receivingThread = new ClientReceivingThread(clientSocket, unfinishedList, unfinishedList_LOCK);

            //start threads
            sendingThread.start();
            receivingThread.start();

            //get user job request (either A, B, or 0 to exit)
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
                        // create a new A job, add it to the list of unsent jobs
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
                        // create a new B job, add it to the list of unsent jobs
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

            //WHEN MASTER SENDS CLIENT DONE JOB, THEN
            done.setFinished(true);
            System.out.println("Thank you. We will exit when all your jobs are done.");

            /*// send a done job
            Job job = new Job(clientId, JobTypes.NULL, jobId + 1, JobStatuses.CLIENT_DONE);
            synchronized (unsentList_LOCK)
            {
                unsentList.add(job);
            }*/

            int size;
            do
            {
                synchronized (unfinishedList_LOCK)
                {
                    size =  unfinishedList.size();
                }
            }
            while (size > 0);

            // join all threads for the client
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
