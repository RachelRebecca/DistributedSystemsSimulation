package master;

import resources.Done;
import resources.Job;
import resources.JobStatuses;

import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;

public class MasterReceivingThreadFromSlave extends Thread
{
    private Socket slaveSocket;
    private Done done;
    private ArrayList<Job> finishedJobs;
    private final Object finishedJobs_LOCK;


    public MasterReceivingThreadFromSlave(Socket slaveSocket, Done done, ArrayList<Job> finishedJobs, Object finishedJobs_LOCK)
    {
        this.slaveSocket = slaveSocket;
        this.finishedJobs = finishedJobs;
        this.finishedJobs_LOCK = finishedJobs_LOCK;
        this.done = done;
    }

    @Override
    public void run()
    {
        try (// stream to read object response from server
             ObjectInputStream objectInputStream = new ObjectInputStream(slaveSocket.getInputStream()))
        {
            Job receivedJob;
            while ((receivedJob = (Job) objectInputStream.readObject()) != null)
            {
                if (receivedJob.getStatus() == JobStatuses.FINISHED_SEND_TO_MASTER)
                {
                    receivedJob.setStatus(JobStatuses.ACK_MASTER_RECEIVED_FINISHED);
                    synchronized (finishedJobs_LOCK)
                    {
                        finishedJobs.add(receivedJob);
                    }
                    System.out.println("Got a completed job from slave: " + receivedJob.getType() + receivedJob.getId());
                }
            }


        } catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }
}
