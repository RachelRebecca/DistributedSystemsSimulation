package slaves;

import resources.*;

import java.net.Socket;
import java.util.ArrayList;

/**
 * Creates Slave
 * - gets Slave Type (A or B) from command line
 * - makes a Slave Socket
 * - Starts and joins Slave Threads
 */
public class Slave
{

    // IP address = "127.0.0.1"
    // port = 30122 for A, 30123 for B

    private static int aTime;
    private static int bTime;

    private static final String aPort = "30122";
    private static final String bPort = "30123";

    public static void main(String[] args)
    {
        if (args.length != 2 || !isValidPort(args[1]))
        {
            System.err.println("Usage: java Slave <host name> <port number>");
            System.exit(1);
        }

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);

        //Set the Slave Type to be A or B using port number
        SlaveTypes slaveType = SlaveTypes.NULL;
        if (args[1].equals(aPort))
        {
            slaveType = SlaveTypes.A;
        }
        else if (args[1].equals(bPort))
        {
            slaveType = SlaveTypes.B;
        }

        // List of jobs that haven't been completed yet by the slave
        ArrayList<Job> incompleteJobList = new ArrayList<>();
        Object incompleteJob_LOCK = new Object();

        // List of jobs that have been completed by the slave, and which needs to be sent back to the Master
        ArrayList<Job> completedJobList = new ArrayList<>();
        Object completedJobList_LOCK = new Object();

        // Done Object to signal when Threads should exit
        Done done = new Done();
        Object done_LOCK = new Object();

        // set slave's A job time and B job time
        setABTime(slaveType);

        System.out.println("Slave " + slaveType + " Port Number: " + portNumber);

        try (Socket slaveSocket = new Socket(hostName, portNumber))
        {
            // create sending and receiving Threads for Slave
            SlaveSendingThread sendingThread = new SlaveSendingThread(slaveSocket, completedJobList,
                    completedJobList_LOCK, done, done_LOCK);
            SlaveReceivingThread receivingThread = new SlaveReceivingThread(slaveSocket, incompleteJobList, incompleteJob_LOCK);

            // there is only ever one DoJob Thread, otherwise a slave could do 2 jobs at once
            Thread doJobThread = new SlaveDoJob(incompleteJobList, incompleteJob_LOCK, completedJobList,
                    completedJobList_LOCK, aTime, bTime, done, done_LOCK);

            // start all Threads
            sendingThread.start();
            receivingThread.start();
            doJobThread.start();

            // join all Threads
            try
            {
                doJobThread.join();
                sendingThread.join();
                receivingThread.join();
            }
            catch (Exception e)
            {
                System.out.println(e.getMessage());
            }
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Check if arg is a valid Slave port number
     * @param arg (String)
     * @return boolean if arg is one of the valid Slave port numbers
     */
    private static boolean isValidPort(String arg)
    {
        return (arg.equals(aPort) || arg.equals(bPort));
    }

    /**
     * Set the time needed to add to a slave for an A and B job
     * @param slaveType - the slave type
     */
    private static void setABTime(SlaveTypes slaveType)
    {
        int shortTime = 2000;
        int longTime = 10000;

        // Slave A, Job A -> 2000, Slave A, Job B -> 10000
        if (slaveType.equals(SlaveTypes.A))
        {
            aTime = shortTime;
            bTime = longTime;
        }
        // Slave B, Job A -> 10000, Slave B, Job B -> 2000
        else if (slaveType.equals(SlaveTypes.B))
        {
            aTime = longTime;
            bTime = shortTime;
        }
    }
}
