package master;

import resources.*;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Creates Master
 * - makes three ServerSockets - for Client, SlaveA, and SlaveB
 * - starts and joins all threads
 */
public class Master
{
    public static void main(String[] args)
    {

        // Hard code in port number if necessary:
        // args = new String[] {"30121", "30122", "30123"};

        if (args.length != 3 || isNotInteger(args[0]) || isNotInteger(args[1]) || isNotInteger(args[2]))
        {
            System.err.println("Usage: java Master <client port number> <slave a port number> <slave b port number>");
            System.exit(1);
        }

        int clientPortNumber = Integer.parseInt(args[0]);
        int slaveAPortNumber = Integer.parseInt(args[1]);
        int slaveBPortNumber = Integer.parseInt(args[2]);

        // setup for threads

        // list of MasterReceivingFromClient and MasterSendingToClient threads
        // every time MasterToClient creates a new client, one new Receiving and Sending Thread gets added
        /*ArrayList<Thread> masterReceivingThreadFromClients = new ArrayList<>();
        Object masterReceivingThreadFromClient_LOCK = new Object();
        ArrayList<Thread> masterSendingThreadToClients = new ArrayList<>();
        Object masterSendingThreadToClient_LOCK = new Object();*/

        // list of unfinished jobs that gets filled by the Client - unfinished jobs are sent to the Slaves
        ArrayList<Job> unfinishedJobs = new ArrayList<>();
        Object unfinishedJob_LOCK = new Object();

        // list of finished jobs that gets filled by the Slaves - finished jobs are sent to the Client
        ArrayList<Job> finishedJobs = new ArrayList<>();
        Object finishedJob_LOCK = new Object();

        // TimeTracker objects - one for SlaveA and one for SlaveB,
        // storing the current total time each slave will need to complete all its jobs
        TimeTrackerForSlave timeTrackerA = new TimeTrackerForSlave(SlaveTypes.A);
        TimeTrackerForSlave timeTrackerB = new TimeTrackerForSlave(SlaveTypes.B);
        Object timeTracker_LOCK = new Object();

        Socket slaveA;
        Socket slaveB;

        Done isDone = new Done();
        Object done_LOCK = new Object();

        try
                (
                        ServerSocket serverSocket = new ServerSocket(clientPortNumber);

                        ServerSocket slaveServerSocket1 = new ServerSocket(slaveAPortNumber);
                        Socket slaveSocket1 = slaveServerSocket1.accept();

                        ServerSocket slaveServerSocket2 = new ServerSocket(slaveBPortNumber);
                        Socket slaveSocket2 = slaveServerSocket2.accept()
                )
        {
            // create and start a MasterToClient thread, which constantly accepts incoming Clients
            // and starts a new MasterSendingToClient and MasterReceivingFromClient threads for each connecting client
            MasterToClient mtc = new MasterToClient(/*masterReceivingThreadFromClients, masterReceivingThreadFromClient_LOCK,
                    masterSendingThreadToClients, masterSendingThreadToClient_LOCK, */serverSocket, unfinishedJobs,
                    unfinishedJob_LOCK, finishedJobs, finishedJob_LOCK, isDone, done_LOCK);
            mtc.start();

            //hardcode 2 slaves -> maybe change for extra credit
            slaveA = slaveSocket1;
            slaveB = slaveSocket2;

            // create and start two different MasterReceivingFromSlave threads (one for SlaveA and one for SlaveB)
            MasterReceivingThreadFromSlave receivingFromSlaveA = new MasterReceivingThreadFromSlave(slaveA,
                    timeTrackerA, timeTrackerB, timeTracker_LOCK, finishedJobs, finishedJob_LOCK);
            MasterReceivingThreadFromSlave receivingFromSlaveB = new MasterReceivingThreadFromSlave(slaveB,
                    timeTrackerA, timeTrackerB, timeTracker_LOCK, finishedJobs, finishedJob_LOCK);

            receivingFromSlaveA.start();
            receivingFromSlaveB.start();

            // create and start a MasterSendingToSlave thread
            MasterSendingThreadToSlave sThread = new MasterSendingThreadToSlave(slaveA, slaveB, timeTrackerA,
                    timeTrackerB, timeTracker_LOCK, unfinishedJobs, unfinishedJob_LOCK, isDone);
            sThread.start();

            while (!isDone.getIsFinished())
            {
                if (isDone.getClientNumber() == 0)
                {
                    isDone.setFinished(true);
                }
            }

            //join all threads
            try
            {
                receivingFromSlaveA.join();
                receivingFromSlaveB.join();
                mtc.join();
                sThread.join();
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
     * Check if arg is an integer
     *
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

