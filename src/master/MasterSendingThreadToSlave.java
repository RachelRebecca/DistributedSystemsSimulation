package master;

import java.net.ServerSocket;

public class MasterSendingThreadToSlave extends Thread
{

    ServerSocket slaveServerSocket;
    TimeTrackerForSlave timeTrackerForSlaveA;
    TimeTrackerForSlave TimeTrackerForSlaveB;

    public MasterSendingThreadToSlave(ServerSocket serverSocket, TimeTrackerForSlave timeTrackerA, TimeTrackerForSlave timeTrackerB)
    {
        slaveServerSocket = serverSocket;
        timeTrackerForSlaveA = timeTrackerA;
        TimeTrackerForSlaveB = timeTrackerB;
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
