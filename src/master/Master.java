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
    private static final String cPort = "30121";
    private static final String aPort = "30122";
    private static final String bPort = "30123";

    public static void main(String[] args)
    {

        // Hard code in port number if necessary:
        // args = new String[] {"30121", "30122", "30123"};

        if (args.length != 3 || !isValidPorts(args[0], args[1], args[2]))
        {
            System.err.println("Usage: java Master <Client port number> <Slave A port number> <Slave B port number>");
            System.exit(1);
        }

        // Assign the port numbers using the command line
        int clientPortNumber = Integer.parseInt(args[0]);
        int slaveAPortNumber = Integer.parseInt(args[1]);
        int slaveBPortNumber = Integer.parseInt(args[2]);

        // setup for threads:

        // list of unfinished jobs that gets filled by the Client - unfinished jobs are sent to the Slaves
        ArrayList<Job> unfinishedJobs = new ArrayList<>();
        Object unfinishedJob_LOCK = new Object();

        // list of finished jobs that gets filled by the Slaves - finished jobs are sent to the Client
        ArrayList<Job> finishedJobs = new ArrayList<>();
        Object finishedJob_LOCK = new Object();

        // list of client sockets
        ArrayList<Socket> clientSockets = new ArrayList<>();
        Object clientSockets_LOCK = new Object();

        // TimeTracker objects - one for SlaveA and one for SlaveB,
        // storing the current total time each slave will need to complete all its jobs
        TimeTrackerForSlave timeTrackerA = new TimeTrackerForSlave(SlaveTypes.A);
        TimeTrackerForSlave timeTrackerB = new TimeTrackerForSlave(SlaveTypes.B);
        Object timeTracker_LOCK = new Object();

        //set up for SlaveASetup
        ArrayList<Socket> slaveAs = new ArrayList<>();
        Object slaveAs_LOCK = new Object();
        Socket slaveA;

        //set up for SlaveBSetup
        ArrayList<Socket> slaveBs = new ArrayList<>();
        Object slaveBs_LOCK = new Object();
        Socket slaveB;

        // Done Object to signal Master exiting to threads
        Done done = new Done();
        Object done_LOCK = new Object();

        try
                (
                        ServerSocket serverSocket = new ServerSocket(clientPortNumber);
                        ServerSocket slaveServerSocket1 = new ServerSocket(slaveAPortNumber);
                        ServerSocket slaveServerSocket2 = new ServerSocket(slaveBPortNumber)
                )
        {
            // create and start a MasterToClient thread, which constantly accepts incoming Clients
            // and starts a new MasterSendingToClient and MasterReceivingFromClient threads for each connecting client
            MasterToClient clientMaker = new MasterToClient(clientSockets, clientSockets_LOCK,
                    serverSocket, unfinishedJobs, unfinishedJob_LOCK, finishedJobs, finishedJob_LOCK,
                    done, done_LOCK);

            clientMaker.start();

            // use threads to set up both slaves
            SlaveSetup aMaker = new SlaveSetup(slaveAs, slaveAs_LOCK, slaveServerSocket1);
            SlaveSetup bMaker = new SlaveSetup(slaveBs, slaveBs_LOCK, slaveServerSocket2);

            aMaker.start();
            bMaker.start();

            // join Slave Maker Threads
            try
            {
                aMaker.join();
                bMaker.join();
            }
            catch (Exception e)
            {
                System.out.println(e.getMessage());
            }

            // assign Slave A and Slave B sockets using shared memory
            slaveA = slaveAs.get(0);
            slaveB = slaveBs.get(0);

            // make sure there is at least one client before continuing
            int size = 0;
            while (size == 0)
            {
                synchronized (clientSockets_LOCK)
                {
                    size = clientSockets.size();
                }
            }

            // create and start two different MasterReceivingFromSlave Threads (one for Slave A and one for Slave B)
            MasterReceivingThreadFromSlave receivingFromSlaveA = new MasterReceivingThreadFromSlave(slaveA,
                    timeTrackerA, timeTrackerB, timeTracker_LOCK, finishedJobs, finishedJob_LOCK);
            MasterReceivingThreadFromSlave receivingFromSlaveB = new MasterReceivingThreadFromSlave(slaveB,
                    timeTrackerA, timeTrackerB, timeTracker_LOCK, finishedJobs, finishedJob_LOCK);

            receivingFromSlaveA.start();
            receivingFromSlaveB.start();

            // create and start a MasterSendingToSlave Thread
            MasterSendingThreadToSlave sendingToSlave = new MasterSendingThreadToSlave(slaveA, slaveB, timeTrackerA,
                    timeTrackerB, timeTracker_LOCK, unfinishedJobs, unfinishedJob_LOCK, done);

            sendingToSlave.start();


            // keep checking until number of clients currently connected is zero, and then set done flag to true
            //while (!done.isFinished());
            //{
                /*synchronized (done_LOCK)
                {
                    if (done.getClientNumber() == 0)
                    {
                        break;
                        //done.setFinished(true);
                    }
                }*/
            //}

            // keep checking until number of exited clients is equal to number of connected clients
            while (!done.isFinished())
            {
                int clientSocketSize;
                synchronized (clientSockets_LOCK)
                {
                    clientSocketSize = clientSockets.size();
                }

                int clientExitNumber;
                synchronized (done_LOCK)
                {
                    clientExitNumber = done.getClientExitNumber();
                    if (done.atLeastOneJoined() && (clientExitNumber == clientSocketSize))
                    {
                        done.setFinished(true);
                        System.out.println("Set done to finished.");
                    }
                }
            }

            //join all the other Threads
            try
            {
                receivingFromSlaveA.join();
                receivingFromSlaveB.join();
                sendingToSlave.join();
                clientMaker.join();
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
     * Check if command line argument entered valid port numbers
     * @param client - attempted Client port number
     * @param slaveA - attempted Slave A port number
     * @param slaveB - attempted Slave B port number
     * @return boolean - true if all port numbers are valid, otherwise false
     */
    private static boolean isValidPorts(String client, String slaveA, String slaveB)
    {
        return (client.equals(cPort) && slaveA.equals(aPort) && slaveB.equals(bPort));
    }
}

