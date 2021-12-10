package slaves;

import resources.*;

import java.net.Socket;
import java.util.ArrayList;

public class SlaveB
{

    // IP address = "127.0.0.1"
    // port = 30122

    private static int aTime;
    private static int bTime;

    public static void main(String[] args)
    {
        if (args.length != 3 || !isInteger(args[1]) || (!args[2].equals(SlaveTypes.A.name()) && !args[2].equals(SlaveTypes.B.name())))
        {
            System.err.println("Usage: java Slave <host name> <port number> <slave type>");
            System.exit(1);
        }

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);
        SlaveTypes slaveType = SlaveTypes.NULL;
        if (args[2].equals(SlaveTypes.A.name()))
        {
            slaveType = SlaveTypes.A;
        }
        else if (args[2].equals(SlaveTypes.B.name()))
        {
            slaveType = SlaveTypes.B;
        }

        ArrayList<Job> incompleteJobList = new ArrayList<>();
        Object incompleteJob_LOCK = new Object();
        ArrayList<Job> completedJobList = new ArrayList<>();
        Object completedJobList_LOCK = new Object();
        Done done = new Done();

        setABTime(slaveType);

        System.out.println("Slave " + slaveType + " portNumber: " + portNumber);

        try (Socket slaveSocket = new Socket(hostName, portNumber))
        {

            SlaveSendingThread sendingThread = new SlaveSendingThread(slaveSocket, completedJobList, completedJobList_LOCK, done, slaveType);
            SlaveReceivingThread receivingThread = new SlaveReceivingThread(slaveSocket, incompleteJobList, incompleteJob_LOCK, done);

            // there is only ever one doJob thread, otherwise a slave could do 2 jobs at once
            Thread doJobThread = new SlaveDoJob(incompleteJobList, incompleteJob_LOCK, completedJobList,
                    completedJobList_LOCK, aTime, bTime, done);

            sendingThread.start();
            receivingThread.start();
            doJobThread.start();

//            Thread.sleep(10000);
//            done.setFinished(true);

            try
            {
                doJobThread.join();
                sendingThread.join();
                receivingThread.join();
            }
            catch (Exception e)
            {
                System.out.println("In inner exception");
//                System.out.println(e.getMessage());
            }
        }
        catch (Exception e)
        {
            System.out.println("In outer exception");
//            System.out.println(e.getMessage());
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

    private static void setABTime(SlaveTypes slaveType)
    {
        if (slaveType.equals(SlaveTypes.A))
        {
            aTime = 2000;
            bTime = 10000;
        }
        else if (slaveType.equals(SlaveTypes.B))
        {
            aTime = 10000;
            bTime = 2000;
        }
    }
}
