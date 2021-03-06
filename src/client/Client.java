package client;

import resources.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Creates Client
 * - make a Client Socket, and get the jobs from the user
 * - start and join Client Threads
 */
public class Client
{

    // IP address = "127.0.0.1"
    // port = 30121

    private static final String cPort = "30121";

    public static void main(String[] args)
    {
        if (args.length != 2 || !isValidPort(args[1]))
        {
            System.err.println("Usage: java Client <host name> <port number>");
            System.exit(1);
        }

        // Assign host name and port number using command line
        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);

        int jobId = 1;

        // list of jobs that haven't been sent yet to Master
        ArrayList<Job> unsentList = new ArrayList<>();
        Object unsentList_LOCK = new Object();

        // list of jobs that haven't yet been finished
        ArrayList<Job> unfinishedList = new ArrayList<>();
        Object unfinishedList_LOCK = new Object();

        // Done Object to signal to Threads when exiting
        Done done = new Done();
        Object done_LOCK = new Object();

        try
                (
                        Socket clientSocket = new Socket(hostName, portNumber);
                        BufferedReader stdIn = // standard input stream to get user's requests
                            new BufferedReader(new InputStreamReader(System.in))
                )
        {
            //each client has exactly one sending thread, and one receiving thread
            ClientSendingThread sendingThread = new ClientSendingThread(clientSocket, unsentList, unsentList_LOCK,
                    unfinishedList, unfinishedList_LOCK, done, done_LOCK);
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
                        Job job = new Job(JobTypes.A, jobId, JobStatuses.UNFINISHED_SEND_TO_MASTER);
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
                        Job job = new Job(JobTypes.B, jobId, JobStatuses.UNFINISHED_SEND_TO_MASTER);
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

            System.out.println("Thank you. We will exit when all your jobs are done.");

            // get size of unfinished list
            // when size is zero, there are no more unfinished jobs and Client can safely exit
            int size;
            do
            {
                Thread.sleep(1000);

                synchronized (unfinishedList_LOCK)
                {
                    size =  unfinishedList.size();
                }
            }
            while (size > 0);

            // set Client Done flag to true (i.e. ready to end Threads)
            synchronized (done_LOCK)
            {
                done.setFinished(true);
            }

            // join all Threads for the client
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
            System.out.println(e.getMessage());
        }
    }

    /**
     * Check if arg is a valid port number
     * @param arg (String)
     * @return boolean if arg is valid client port number
     */
    private static boolean isValidPort(String arg)
    {
       return (arg.equals(cPort));
    }
}
