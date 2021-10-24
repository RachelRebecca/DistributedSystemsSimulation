import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ClientSendingThread extends Thread {
    private Socket clientSocket;
    private  ArrayList<String> unsentList = new ArrayList<>();
    private Object unsent_LOCK;
    private ArrayList<String> unreceivedList = new ArrayList<>();
    private Object unreceived_LOCK;

    public ClientSendingThread(Socket clientSocket, ArrayList<String> unsentList, Object unsent_LOCK,
                               ArrayList<String> unreceivedList, Object unreceived_LOCK) {
        this.clientSocket = clientSocket;
        this.unreceivedList = unsentList;
        this.unsent_LOCK = unsent_LOCK;
        this.unreceivedList = unreceivedList;
        this.unreceived_LOCK = unreceived_LOCK;
    }

    public void run() {
        try (PrintWriter requestWriter = // stream to write text requests to server
                     new PrintWriter(clientSocket.getOutputStream(), true);
        ) {
            while (true) {
                String currJob = "";
                synchronized (unsent_LOCK) {
                    if (unsentList.size() > 0) {
                        currJob = unsentList.get(0);
                        unsentList.remove(0);
                    }
                }
                //send to master

                synchronized (unreceived_LOCK) {
                    unreceivedList.add(currJob);
                }

            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


}
