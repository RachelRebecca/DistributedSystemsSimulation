package master;

import resources.*;

import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;

public class MasterReceivingThreadFromClient extends Thread
{
    private Socket clientSocket;
    private Done done;
    private ArrayList<Job> unsentList;
    private final Object unsentList_LOCK;


    public MasterReceivingThreadFromClient(Socket clientSocket, Done done, ArrayList<Job> unsentList, Object unsentList_LOCK)
    {
        this.clientSocket = clientSocket;
        this.unsentList = unsentList;
        this.unsentList_LOCK = unsentList_LOCK;
        this.done = done;
    }

    @Override
    public void run()
    {
        try (// stream to read object response from server
             ObjectInputStream objectInputStream = new ObjectInputStream(clientSocket.getInputStream()))
        {
            Job receivedJob;
            while ((receivedJob = (Job) objectInputStream.readObject()) != null)
            {
                if (receivedJob.getStatus() == JobStatuses.UNFINISHED_SEND_TO_MASTER)
                {
                    receivedJob.setStatus(JobStatuses.ACK_MASTER_RECEIVED);
                    synchronized (unsentList_LOCK)
                    {
                        unsentList.add(receivedJob);
                    }
                    System.out.println("Got a job from client: " + receivedJob.getType() + receivedJob.getId());
                }
            }
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }
}
