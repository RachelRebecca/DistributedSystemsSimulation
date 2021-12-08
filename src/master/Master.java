package master;

import org.w3c.dom.ls.LSOutput;
import resources.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Master
{
    public static void main(String[] args)
    {

        // Hard code in port number if necessary:
        //args = new String[] { "30121" };

        if (args.length != 3 || !isInteger(args[0]))
        {
            System.err.println("Usage: java EchoServer <client port number> <slave a port number> <slave b port number>");
            System.exit(1);
        }

        int portNumber = Integer.parseInt(args[0]);
        int slaveAPortNumber = Integer.parseInt(args[1]);
        int slaveBPortNumber = Integer.parseInt(args[2]);

        /*
        Master ArrayLists:
        1) Received but unfinished jobs
        2) finished jobs

        Receiver from client fills unfinished jobs
        Thread that sends to slave empties out unfinished jobs
        Thread that receives from slave fills finished jobs
        Thread that sends to client empties out finished jobs

        Add in acknowledgements later
         */

        // setup for threads
        ArrayList<Thread> masterReceivingThreadFromClient = new ArrayList<>();
        Object masterReceivingThreadFromClient_LOCK = new Object();
        ArrayList<Thread> masterSendingThreadToClient = new ArrayList<>();
        Object masterSendingThreadToClient_LOCK = new Object();

        ArrayList<Thread> masterReceivingThreadFromSlave = new ArrayList<>();
        Object masterReceivingThreadFromSlave_LOCK = new Object();
        ArrayList<Thread> masterSendingThreadToSlave = new ArrayList<>();
        Object masterSendingThreadToSlave_LOCK = new Object();

        ArrayList<Job> unfinishedJobs = new ArrayList<>();
        ArrayList<Job> finishedJobs = new ArrayList<>();
        Object unfinishedJob_LOCK = new Object();
        Object finishedJob_LOCK = new Object();

        TimeTrackerForSlave timeTrackerA = new TimeTrackerForSlave(SlaveTypes.A);
        TimeTrackerForSlave timeTrackerB = new TimeTrackerForSlave(SlaveTypes.B);

        Socket slaveA = new Socket();
        Socket slaveB = new Socket();

        Done isDone = new Done();

        // may be used in the future - for now we'll only have one of each
//        ArrayList<Thread> masterToClients = new ArrayList<>();
//        ArrayList<Thread> masterToSlaves = new ArrayList<>();

        // start the pre-determined number of client and slave maker threads (start with 1)
        // continue until done: done goes from master into each maker, and from each maker into each client/slave sending/receiving thread

        // TODO: GET RID OF ALL OF THIS!!!
        try
                (
                        // move these to threads for extra credit
                        // hardcode 2 slaves for regular
                        ServerSocket serverSocket = new ServerSocket(portNumber);
//                        Socket clientSocket = serverSocket.accept();
//                        ObjectOutputStream objectOutputStreamClient = new ObjectOutputStream(clientSocket.getOutputStream());
//                        ObjectInputStream objectInputStreamClient = new ObjectInputStream(clientSocket.getInputStream());

                        ServerSocket slaveServerSocket1 = new ServerSocket(slaveAPortNumber);
                        Socket slaveSocket1 = slaveServerSocket1.accept();
                        //ObjectOutputStream objectOutputStreamSlave1 = new ObjectOutputStream(slaveSocket1.getOutputStream());
                       // ObjectInputStream objectInputStreamSlave1 = new ObjectInputStream(slaveSocket1.getInputStream());

                        ServerSocket slaveServerSocket2 = new ServerSocket(slaveBPortNumber);
                        Socket slaveSocket2 = slaveServerSocket2.accept();
                        //ObjectOutputStream objectOutputStreamSlave2 = new ObjectOutputStream(slaveSocket2.getOutputStream());
                       // ObjectInputStream objectInputStreamSlave2 = new ObjectInputStream(slaveSocket2.getInputStream())
                )
        {
            MasterToClient clientMaker = new MasterToClient(masterReceivingThreadFromClient, masterReceivingThreadFromClient_LOCK,
                    masterSendingThreadToClient, masterSendingThreadToClient_LOCK, serverSocket, unfinishedJobs,
                    finishedJobs, unfinishedJob_LOCK, finishedJob_LOCK, isDone);

            //MasterSendingThreadToClient mtc = new MasterSendingThreadToClient(, isDone, finishedJobs, finishedJob_LOCK);
            //MasterReceivingThreadFromClient mrc = new MasterReceivingThreadFromClient();

            slaveA = slaveSocket1;
            slaveB = slaveSocket2;


            /*
            // properly assign slaveA and slaveB
            Job announcement1 = (Job) objectInputStreamSlave1.readObject();
            Job announcement2 = (Job) objectInputStreamSlave2.readObject(); // unused, to get rid of the type announcement job
            if ((announcement1.getStatus().equals(JobStatuses.IS_SLAVE_A)))
            {
                slaveA = slaveSocket1;
                slaveB = slaveSocket2;
                System.out.println("Slave A = socket1, slave B = socket2");
            }
            else
            {
                slaveA = slaveSocket2;
                slaveB = slaveSocket1;
                System.out.println("Slave A = socket2, slave B = socket1");
            }
             */
            MasterReceivingThreadFromSlave receivingFromSlaveA = new MasterReceivingThreadFromSlave (slaveA, isDone,
                    finishedJobs, finishedJob_LOCK);
            MasterReceivingThreadFromSlave receivingFromSlaveB = new MasterReceivingThreadFromSlave (slaveB, isDone,
                    finishedJobs, finishedJob_LOCK);

            receivingFromSlaveA.start();
            receivingFromSlaveB.start();
            clientMaker.start();

            MasterSendingThreadToSlave sThread = new MasterSendingThreadToSlave(slaveA, slaveB, timeTrackerA,
                        timeTrackerB, unfinishedJobs, unfinishedJob_LOCK, isDone);
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
                clientMaker.join(); // WE FORGOT TO DO THIS

                sThread.join();

            }
            catch (Exception e)
            {
                System.out.println("Master!!!!" + e.getMessage());
            }



//            MasterToSlave slaveMaker = new MasterToSlave(masterReceivingThreadFromSlave, masterReceivingThreadFromSlave_LOCK,
//                    masterSendingThreadToSlave, masterSendingThreadToSlave_LOCK, slaveServerSocket, unfinishedJobs, finishedJobs,
//                    unfinishedJob_LOCK, finishedJob_LOCK, timeTrackerA, timeTrackerB, )
//
//            public MasterToSlave(
//                Socket slaveSocketA, Socket slaveSocketB,
//                Done isDone)









//            while (true)
//            {
//                // get job from client
//                Job clientRequest = (Job) objectInputStreamClient.readObject();
//                System.out.println("Got a job from client: " + clientRequest.getType() + clientRequest.getId());
//
//                // choose slave
//
//                // send to slave
//                clientRequest.setStatus(JobStatuses.UNFINISHED_SEND_TO_SLAVE);
//                objectOutputStreamSlave.writeObject(clientRequest);
//                System.out.println("Sending to slave");
//
//                // wait to hear back from slave
//                Job finishedJob = (Job) objectInputStreamSlave.readObject();
//                System.out.println("Got a job from slave: " + finishedJob.getType() + finishedJob.getId() + finishedJob.getStatus());
//
//                // send finished job to client
//                finishedJob.setStatus(JobStatuses.FINISHED_SEND_TO_CLIENT);
//                objectOutputStreamClient.writeObject(finishedJob);
//                System.out.println("Sending to client");
//            }

        }
        catch (Exception e)		// generic exception in case of any issues that arise
        {
            System.out.println("Master IO error: " + e.getMessage());
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

