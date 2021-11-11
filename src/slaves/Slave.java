package slaves;

import resources.*;

import java.io.ObjectInput;
import java.net.Socket;
import java.util.ArrayList;

public class Slave
{

    // IP address = "127.0.0.1"
    // port = 30122

    public static void main(String[] args)
    {
        if (args.length != 3 || !isInteger(args[1]) || (!args[2].equals(SlaveTypes.A.name()) && !args[2].equals(SlaveTypes.B.name())))
        {
            System.err.println("Usage: java Slave <host name> <port number> <slave type>");
            System.exit(1);
        }

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);
        SlaveTypes slaveType = null;
        if (args[2].equals(SlaveTypes.A.name()))
        {
            slaveType = SlaveTypes.A;
        }
        else if (args[2].equals(SlaveTypes.B.name()))
        {
            slaveType = SlaveTypes.B;
        }


        ArrayList<Thread> doJobThreads = new ArrayList<>();
        ArrayList<Thread> sendingThreads = new ArrayList<>();
        ArrayList<Thread> receivingThreads = new ArrayList<>();
        ArrayList<Job> incompleteJobList = new ArrayList<>();
        Object incompleteJob_LOCK = new Object();
        ArrayList<Job> completedJobList = new ArrayList<>();
        Object completedJobList_LOCK = new Object();
        Done done = new Done();

        try (Socket slaveSocket = new Socket(hostName, portNumber))
        {

            //can change the 1's later:
            for (int i = 0; i < 1; i++)
            {
                doJobThreads.add(new DoJobThread(incompleteJobList, incompleteJob_LOCK, completedJobList,
                        completedJobList_LOCK, done, slaveType));
                sendingThreads.add(new SlaveSendingThread(slaveSocket, completedJobList, completedJobList_LOCK, done));
                receivingThreads.add(new SlaveReceivingThread(slaveSocket, incompleteJobList, incompleteJob_LOCK, done));
            }

            for (Thread djThread : doJobThreads)
            {
                djThread.start();
            }
            for (Thread sThread : sendingThreads)
            {
                sThread.start();
            }
            for (Thread rThread : receivingThreads)
            {
                rThread.start();
            }

            while (!done.getIsFinished());

            try
            {
                for (Thread djThread : doJobThreads)
                {
                    djThread.join();
                }
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
