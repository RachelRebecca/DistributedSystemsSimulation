package master;

import resources.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Master
{
    public static void main(String[] args)
    {

        // Hard code in port number if necessary:
        //args = new String[] { "30121" };

        if (args.length != 1 || !isInteger(args[0]))
        {
            System.err.println("Usage: java EchoServer <port number>");
            System.exit(1);
        }

        int portNumber = Integer.parseInt(args[0]);

        try
                (
                        ServerSocket serverSocket = new ServerSocket(portNumber);
                        Socket clientSocket = serverSocket.accept();
                        ObjectOutputStream objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                        ObjectInputStream objectInputStream = new ObjectInputStream(clientSocket.getInputStream())
                )
        {
            while (true)
            {
                // get job from client
                Job clientRequest = (Job) objectInputStream.readObject();

                System.out.println("Got a job: " + clientRequest.getType() + clientRequest.getId());

                // if job is unfinished, send back finished
                if (clientRequest.getStatus() == JobStatuses.UNFINISHED_SEND_TO_MASTER)
                {
                    System.out.println("Sending back job acknowledgement");
                    clientRequest.setStatus(JobStatuses.ACK_MASTER_RECEIVED);
                    objectOutputStream.writeObject(clientRequest);
                }
                else
                {
                    System.out.println("Something weird happened");
                }
            }

        }
        catch (Exception e)		// generic exception in case of any issues that arise
        {
            System.out.println(
                    "Exception caught when trying to listen on port " + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        }

    }

    /**
     * Check if arg is an integer
     * @param arg (String)
     * @return boolean if arg can be parsed as an integer
     */
    private static boolean isInteger(String arg)
    {
        boolean isInteger = true;
        try
        {
            Integer.parseInt(arg);
        }
        catch (Exception e)
        {
            isInteger = false;
        }
        return isInteger;
    }

}

