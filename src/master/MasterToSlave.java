package master;

import resources.Done;
import resources.IDTracker;
import resources.Job;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class MasterToSlave extends Thread
{
    ArrayList<Thread> masterReceivingThreadFromSlave = new ArrayList<>();
    //Object masterReceivingThreadFromSlave_LOCK;
    ArrayList<Thread> masterSendingThreadToSlave = new ArrayList<>();
    //Object masterSendingThreadToSlave_LOCK;

    private ServerSocket serverSocket;
    private final ArrayList<Job> unfinishedJobs;
    private final ArrayList<Job> finishedJobs;
    private final Object unfinishedJob_LOCK;
    private final Object finishedJob_LOCK;
    private final TimeTrackerForSlave timeTrackerA;
    private final TimeTrackerForSlave timeTrackerB;
    private final Object timeTracker_LOCK;
    private IDTracker idTracker;
    private final Object idTracker_LOCK;

    private ArrayList<Socket> slaveSockets;
    private final Object slaveSockets_LOCK;

    Socket slaveSocketA;
    Socket slaveSocketB;

    final Done done;


    public MasterToSlave(/*ArrayList<Thread> masterReceivingThreadFromSlave, Object masterReceivingThreadFromSlave_LOCK,
                          ArrayList<Thread> masterSendingThreadToSlave, Object masterSendingThreadToSlave_LOCK,*/
                          ServerSocket serverSocket,  ArrayList<Socket> slaveSockets,
                          Object slaveSockets_LOCK,
                          ArrayList<Job> unfinishedJobs, ArrayList<Job> finishedJobs,
                          Object unfinishedJob_LOCK, Object finishedJob_LOCK,
                          IDTracker idTracker, Object idTracker_LOCK,
                          /*ArrayList of TimeTrackers*/
                          TimeTrackerForSlave timeTrackerA, TimeTrackerForSlave timeTrackerB, Object timeTracker_LOCK,
                          Socket slaveSocketA, Socket slaveSocketB,
                          Done isDone)
    {

        /*this.masterReceivingThreadFromSlave = masterReceivingThreadFromSlave;
        this.masterReceivingThreadFromSlave_LOCK = masterReceivingThreadFromSlave_LOCK;
        this.masterSendingThreadToSlave = masterSendingThreadToSlave;
        this.masterSendingThreadToSlave_LOCK = masterSendingThreadToSlave_LOCK;*/

        this.serverSocket = serverSocket;
        this.slaveSockets = slaveSockets;
        this.slaveSockets_LOCK = slaveSockets_LOCK;

        this.unfinishedJobs = unfinishedJobs;
        this.unfinishedJob_LOCK = unfinishedJob_LOCK;
        this.finishedJobs = finishedJobs;
        this.finishedJob_LOCK = finishedJob_LOCK;
        this.timeTrackerA = timeTrackerA;
        this.timeTrackerB = timeTrackerB;
        this.timeTracker_LOCK = timeTracker_LOCK;

        this.idTracker = idTracker;
        this.idTracker_LOCK = idTracker_LOCK;

        this.slaveSocketA = slaveSocketA;
        this.slaveSocketB = slaveSocketB;

        done = isDone;
    }

    @Override
    public void run()
    {
        try
        {
            while (!done.getIsFinished())
            {
                Socket slaveSocket = serverSocket.accept();
                synchronized (slaveSockets_LOCK)
                {
                    slaveSockets.add(slaveSocket);
                }

                /*
                int slaveId;
                synchronized (idTracker_LOCK)
                {
                    slaveId = idTracker.getID();
                    idTracker.incrementID();
                }
                 */

                // TODO: THIS SHOULD ONLY GET FROM CURRENT SLAVE SOCKET

                //synchronized (masterReceivingThreadFromSlave_LOCK)
                //{
                    MasterReceivingThreadFromSlave mrs = new MasterReceivingThreadFromSlave(
                            slaveSocket, timeTrackerA, timeTrackerB, timeTracker_LOCK,
                            finishedJobs, finishedJob_LOCK);
                    masterReceivingThreadFromSlave.add(mrs);
                    mrs.start();

                //}
               // synchronized (masterSendingThreadToSlave_LOCK)
              //  {
                // TODO: THIS SHOULD ONLY SEND TO CURRENT SLAVE SOCKET

                MasterSendingThreadToSlave mss = new MasterSendingThreadToSlave(
                            slaveSocketA, slaveSocketB, slaveSockets, slaveSockets_LOCK,
                            timeTrackerA, timeTrackerB, timeTracker_LOCK,
                            unfinishedJobs, unfinishedJob_LOCK, done);
                    masterSendingThreadToSlave.add(mss);
                    mss.start();
               // }
            }

            if (done.getIsFinished())
            {
                done.setFinished(true);
            }
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }
}
