package slaves;
import java.io.*;
import java.net.*;
import java.util.*;
import resources.*;

public class SlaveReceivingThread extends Thread
{
    private final Socket slaveSocket;
    private final ArrayList<Job> incompleteJobs;
    private final Object incompleteList_LOCK;
    private final Done done;

    public SlaveReceivingThread(Socket socket, ArrayList<Job> jobsToComplete, Object incompleteJobs_LOCK, Done finished)
    {
        slaveSocket = socket;
        incompleteJobs = jobsToComplete;
        incompleteList_LOCK = incompleteJobs_LOCK;
        done = finished;
    }

    @Override
    public void run()
    {
        try (ObjectInputStream objectInput = new ObjectInputStream(slaveSocket.getInputStream()))
        {
            Job job;
            while (!done.getIsFinished())
            {
                // assign the job to whatever is being read from the Master
                job = (Job) objectInput.readObject();

                // add it to the list of incomplete jobs
                synchronized (incompleteList_LOCK)
                {
                    incompleteJobs.add(job);
                }

                if (done.getIsFinished())
                {
                    done.setFinished(true);
                    objectInput.close();
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