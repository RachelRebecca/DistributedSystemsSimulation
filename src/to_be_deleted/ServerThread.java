package to_be_deleted;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerThread implements Runnable
{
    //a reference to the server socket is passed in, all threads share it
    private ServerSocket serverSocket = null;

    int id;
    public ServerThread(ServerSocket s, int ID)
    {
        serverSocket = s;
        id = ID;
    }

    @Override
    public void run()
    {
        //this thread accepts its own client socket from the shared server socket
        try
                (
                        Socket clientSocket = serverSocket.accept();
                        PrintWriter responseWriter = new PrintWriter(clientSocket.getOutputStream(), true);
                        BufferedReader requestReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
                )
        {
            // stuff goes here
        }
        catch(IOException e)
        {
            System.out.println("Exception caught when trying to listen on port " + serverSocket.getLocalPort()
                    + " or listening for a connection");
            System.out.println(e.getMessage());
        }
    }
}
