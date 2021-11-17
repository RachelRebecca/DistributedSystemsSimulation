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
        int slavePortNumber = 30122;

        try
                (
                        ServerSocket serverSocket = new ServerSocket(portNumber);
                        Socket clientSocket = serverSocket.accept();
                        ObjectOutputStream objectOutputStreamClient = new ObjectOutputStream(clientSocket.getOutputStream());
                        ObjectInputStream objectInputStreamClient = new ObjectInputStream(clientSocket.getInputStream());

                        ServerSocket slaveServerSocket = new ServerSocket(slavePortNumber);
                        Socket slaveSocket = slaveServerSocket.accept();
                        ObjectOutputStream objectOutputStreamSlave = new ObjectOutputStream(slaveSocket.getOutputStream());
                        ObjectInputStream objectInputStreamSlave = new ObjectInputStream(slaveSocket.getInputStream())

                )
        {
            while (true)
            {
                // get job from client
                Job clientRequest = (Job) objectInputStreamClient.readObject();
                System.out.println("Got a job from client: " + clientRequest.getType() + clientRequest.getId());

                // send ack
                clientRequest.setStatus(JobStatuses.ACK_MASTER_RECEIVED);
                objectOutputStreamClient.writeObject(clientRequest);
                System.out.println("Sending acknowledgement");

                // choose slave

                // send to slave
                clientRequest.setStatus(JobStatuses.UNFINISHED_SEND_TO_SLAVE);
                objectOutputStreamSlave.writeObject(clientRequest);
                System.out.println("Sending to slave");

                // wait to hear back from slave
                Job finishedJob = (Job) objectInputStreamSlave.readObject();
                System.out.println("Got a job from slave: " + finishedJob.getType() + finishedJob.getId());

                // send finished job to client
                finishedJob.setStatus(JobStatuses.FINISHED_SEND_TO_CLIENT);
                objectOutputStreamClient.writeObject(finishedJob);
                System.out.println("Sending to client");
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

