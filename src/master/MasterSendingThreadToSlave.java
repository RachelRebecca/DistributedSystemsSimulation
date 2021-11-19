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

        int comparison = (timeTrackerForSlaveA.getTime() + timeTrackerForSlaveA.add(newJob.getType()))
                            - (timeTrackerForSlaveB.getTime() + timeTrackerForSlaveB.add(newJob.getType()));

        // basically for each slave, add the time currently on the slave plus the time that would be added
        // by the given job type (which is different for slave A and slave B) and subtract to compare -
        // for example if job type is A, slaveANewJobTime would be 2000 and slaveBNewJobTime would be 10000)
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
