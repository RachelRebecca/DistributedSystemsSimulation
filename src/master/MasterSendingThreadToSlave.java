package master;

import java.net.ServerSocket;

public class MasterSendingThreadToSlave extends Thread
{

    ServerSocket slaveServerSocket;
    TimeTrackerForSlave timeTrackerForSlaveA;
    TimeTrackerForSlave getTimeTrackerForSlaveB;

    public MasterSendingThreadToSlave(ServerSocket serverSocket, TimeTrackerForSlave timeTrackerA, TimeTrackerForSlave timeTrackerB)
    {
        slaveServerSocket = serverSocket;
        timeTrackerForSlaveA = timeTrackerA;
        getTimeTrackerForSlaveB = timeTrackerB;
    }

    @Override
    public void run()
    {
        loadBalance();
    }

    private void loadBalance()
    {

    }
}
