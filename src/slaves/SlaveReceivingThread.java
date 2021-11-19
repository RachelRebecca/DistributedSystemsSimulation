package slaves;
import java.io.*;
import java.net.*;
import java.util.*;
import resources.*;

public class SlaveReceivingThread extends Thread
{
    private Socket slaveSocket;
    private ArrayList<Job> incompleteJobs;
    private final Object incompleteList_LOCK;
    private Done done;

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
            while (!done.getIsFinished())
            {
                Job job = (Job) objectInput.readObject();
                synchronized (incompleteList_LOCK)
                {
                    System.out.println("Adding incomplete job: " + job);
                    incompleteJobs.add(job);
                }

                if (done.getIsFinished())
                {
                    done.setFinished(true);
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("BUG!" + e.getMessage());
        }
    }
}