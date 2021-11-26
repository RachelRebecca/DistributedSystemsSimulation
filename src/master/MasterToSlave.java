package master;

import resources.Done;
import resources.Job;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class MasterToSlave extends Thread
{
    final ArrayList<Thread> masterReceivingThreadFromSlave;
    Object masterReceivingThreadFromSlave_LOCK;
    final ArrayList<Thread> masterSendingThreadToSlave;
    Object masterSendingThreadToSlave_LOCK;

    ServerSocket serverSocket;
    final Done receivingIsDone;
    final Done sendingIsDone;
    final ArrayList<Job> unfinishedJobs;
    final ArrayList<Job> finishedJobs;
    final Object unfinishedJob_LOCK;
    final Object finishedJob_LOCK;
    final TimeTrackerForSlave timeTrackerA;
    final TimeTrackerForSlave timeTrackerB;

    Socket slaveSocketA;
    Socket slaveSocketB;

    final Done done;


    public MasterToSlave(ArrayList<Thread> masterReceivingThreadFromSlave, Object masterReceivingThreadFromSlave_LOCK,
                          ArrayList<Thread> masterSendingThreadToSlave, Object masterSendingThreadToSlave_LOCK,
                          ServerSocket serverSocket, Done receivingIsDone, Done sendingIsDone,
                          ArrayList<Job> unfinishedJobs, ArrayList<Job> finishedJobs,
                          Object unfinishedJob_LOCK, Object finishedJob_LOCK,
                          TimeTrackerForSlave timeTrackerA, TimeTrackerForSlave timeTrackerB,
                          Socket slaveSocketA, Socket slaveSocketB,
                          Done isDone)
    {
        this.masterReceivingThreadFromSlave = masterReceivingThreadFromSlave;
        this.masterReceivingThreadFromSlave_LOCK = masterReceivingThreadFromSlave_LOCK;
        this.masterSendingThreadToSlave = masterSendingThreadToSlave;
        this.masterSendingThreadToSlave_LOCK = masterSendingThreadToSlave_LOCK;

        this.serverSocket = serverSocket;

        this.sendingIsDone = sendingIsDone;
        this.receivingIsDone = receivingIsDone;
        this.unfinishedJobs = unfinishedJobs;
        this.unfinishedJob_LOCK = unfinishedJob_LOCK;
        this.finishedJobs = finishedJobs;
        this.finishedJob_LOCK = finishedJob_LOCK;
        this.timeTrackerA = timeTrackerA;
        this.timeTrackerB = timeTrackerB;

        this.slaveSocketA = slaveSocketA;
        this.slaveSocketB = slaveSocketB;

        done = isDone;
    }

    @Override
    public void run()
    {
        while (!done.getIsFinished())
        {
            try
            {
                Socket slaveSocket = serverSocket.accept();
                synchronized (masterReceivingThreadFromSlave_LOCK)
                {
                    masterReceivingThreadFromSlave.add(new MasterReceivingThreadFromSlave(
                            slaveSocket, receivingIsDone, finishedJobs, finishedJob_LOCK));
                }
                synchronized (masterSendingThreadToSlave_LOCK)
                {
                    masterSendingThreadToSlave.add(new MasterSendingThreadToSlave(
                            slaveSocketA, slaveSocketB,
                            timeTrackerA, timeTrackerB,
                            unfinishedJobs, unfinishedJob_LOCK, sendingIsDone));
                }
            }
            catch (Exception e)
            {
                System.out.println(e.getMessage());
            }

            if (done.getIsFinished())
            {
                done.setFinished(true);
            }
        }
    }
}
