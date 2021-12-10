package master;

import resources.*;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Master
{
    public static void main(String[] args)
    {

        // Hard code in port number if necessary:
        //args = new String[] {"30121", "30122", "30123"};

        if (args.length != 3 || isNotInteger(args[0]) || isNotInteger(args[1]) || isNotInteger(args[2]))
        {
            System.err.println("Usage: java EchoServer <client port number> <slave a port number> <slave b port number>");
            System.exit(1);
        }

        int portNumber = Integer.parseInt(args[0]);
        int slaveAPortNumber = Integer.parseInt(args[1]);
        int slaveBPortNumber = Integer.parseInt(args[2]);

        // setup for threads
        ArrayList<Thread> masterToClientThreads = new ArrayList<>();

        ArrayList<Thread> masterReceivingThreadFromClient = new ArrayList<>();
        Object masterReceivingThreadFromClient_LOCK = new Object();
        ArrayList<Thread> masterSendingThreadToClient = new ArrayList<>();
        Object masterSendingThreadToClient_LOCK = new Object();

        ArrayList<Job> unfinishedJobs = new ArrayList<>();
        ArrayList<Job> finishedJobs = new ArrayList<>();
        Object unfinishedJob_LOCK = new Object();
        Object finishedJob_LOCK = new Object();

        TimeTrackerForSlave timeTrackerA = new TimeTrackerForSlave(SlaveTypes.A);
        TimeTrackerForSlave timeTrackerB = new TimeTrackerForSlave(SlaveTypes.B);
        Object timeTracker_LOCK = new Object();

        Socket slaveA;
        Socket slaveB;

        Done isDone = new Done();

        try
                (
                        // hardcode 2 slaves for regular
                        ServerSocket serverSocket = new ServerSocket(portNumber);

                        ServerSocket slaveServerSocket1 = new ServerSocket(slaveAPortNumber);
                        Socket slaveSocket1 = slaveServerSocket1.accept();

                        ServerSocket slaveServerSocket2 = new ServerSocket(slaveBPortNumber);
                        Socket slaveSocket2 = slaveServerSocket2.accept()
                )
        {
            MasterToClient mtc = new MasterToClient(masterReceivingThreadFromClient, masterReceivingThreadFromClient_LOCK,
                    masterSendingThreadToClient, masterSendingThreadToClient_LOCK, serverSocket, unfinishedJobs,
                    finishedJobs, unfinishedJob_LOCK, finishedJob_LOCK, isDone);
            masterToClientThreads.add(mtc);

            slaveA = slaveSocket1;
            slaveB = slaveSocket2;

            MasterReceivingThreadFromSlave receivingFromSlaveA = new MasterReceivingThreadFromSlave(slaveA,
                    timeTrackerA, timeTrackerB, timeTracker_LOCK, finishedJobs, finishedJob_LOCK);
            MasterReceivingThreadFromSlave receivingFromSlaveB = new MasterReceivingThreadFromSlave(slaveB,
                    timeTrackerA, timeTrackerB, timeTracker_LOCK, finishedJobs, finishedJob_LOCK);

            receivingFromSlaveA.start();
            receivingFromSlaveB.start();

            for (Thread cm : masterToClientThreads)
            {
                cm.start();
            }

            MasterSendingThreadToSlave sThread = new MasterSendingThreadToSlave(slaveA, slaveB, timeTrackerA,
                    timeTrackerB, timeTracker_LOCK, unfinishedJobs, unfinishedJob_LOCK, isDone);
            sThread.start();

            while (!isDone.getIsFinished())
            {
                if (isDone.getIsFinished())
                {
                    isDone.setFinished(true);
                }
            }

            try
            {
                receivingFromSlaveA.join();
                receivingFromSlaveB.join();

                for (Thread cm : masterToClientThreads)
                {
                    try
                    {
                        cm.join();
                    }
                    catch (Exception e)
                    {
                        System.out.println("Joining client makers: " + e.getMessage());
                    }
                }

                sThread.join();

            }
            catch (Exception e)
            {
                System.out.println("Master!!!!" + e.getMessage());
            }
        }
        catch (Exception e)        // generic exception in case of any issues that arise
        {
            System.out.println("Master IO error: " + e.getMessage());
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
        } catch (Exception e)
        {
            isInteger = false;
        }
        return !isInteger;
    }
}

