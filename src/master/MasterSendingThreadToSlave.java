package master;

import resources.*;
import java.net.ServerSocket;

public class MasterSendingThreadToSlave extends Thread
{

    ServerSocket slaveServerSocket;
    TimeTrackerForSlave timeTrackerForSlaveA;
    TimeTrackerForSlave timeTrackerForSlaveB;

    public MasterSendingThreadToSlave(ServerSocket serverSocket, TimeTrackerForSlave timeTrackerA, TimeTrackerForSlave timeTrackerB)
    {
        slaveServerSocket = serverSocket;
        timeTrackerForSlaveA = timeTrackerA;
        timeTrackerForSlaveB = timeTrackerB;
    }

    @Override
    public void run() {
    }

    private void loadBalance(Job newJob)
    {
        int slaveATime=0;
        int slaveBTime=0;

        if (newJob.getType().equals(JobTypes.A))
        {
            slaveATime = 2000;
            slaveBTime = 10000;
        }
        else if (newJob.getType().equals(JobTypes.B))
        {
            slaveATime = 10000;
            slaveBTime = 2000;
        }

        int comparison = (timeTrackerForSlaveA.getTime() + slaveATime
                            - (timeTrackerForSlaveB.getTime() + slaveBTime));

        JobTypes type = newJob.getType();
        if (comparison < 0)
        {
            // send to slave A    //how do you send to slave A??

            if (newJob.getType().equals(JobTypes.A))
            {
                timeTrackerForSlaveA.addA();
            } else
            {
                timeTrackerForSlaveA.addB();
            }


        } else if (comparison == 0)
        {
            if (newJob.getType().equals(JobTypes.A))
            {
                // send to A
                timeTrackerForSlaveA.addA();
            } else
            {
                // send to B
                timeTrackerForSlaveB.addB();
            }
        } else
        {
            // send to slave B

            if (newJob.getType().equals(JobTypes.A))
            {
                timeTrackerForSlaveB.addA();
            } else
            {
                timeTrackerForSlaveB.addB();
            }
        }

    }


}
