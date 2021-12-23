package slaves;
import java.io.*;
import java.net.*;
import java.util.*;
import resources.*;

/**
 * Slave receives from Master using its Socket
 * It only receives jobs that need to be completed
 */
public class SlaveReceivingThread extends Thread
{
    // Socket connecting Slave to Master
    private final Socket slaveSocket;

    // list of incomplete jobs that slave needs to complete (shared memory)
    private final ArrayList<Job> incompleteJobs;
    private final Object incompleteList_LOCK;

    public SlaveReceivingThread(Socket socket, ArrayList<Job> jobsToComplete, Object incompleteJobs_LOCK)
    {
        slaveSocket = socket;
        incompleteJobs = jobsToComplete;
        incompleteList_LOCK = incompleteJobs_LOCK;
    }

    @Override
    public void run()
    {
        try (ObjectInputStream objectInput = new ObjectInputStream(slaveSocket.getInputStream()))
        {
            Job job;

            // assign the job to whatever is being read from the Master
            while ((job = (Job) objectInput.readObject()) != null)
            {
                // add it to the list of incomplete jobs
                synchronized (incompleteList_LOCK)
                {
                    incompleteJobs.add(job);
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("Detected Master exit. Exiting Slave.");
            System.exit(0);
        }
    }
}