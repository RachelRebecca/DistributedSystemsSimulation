package to_be_deleted;

import java.util.*;
import java.io.*;
import java.net.*;

public class ClientClass
{
    private static String clientLetter;
    private static ArrayList<Thread> thread1;
    private static ArrayList<Thread> thread2;
    private static ArrayList<Thread> thread3;
    private static ArrayList<String> unreceivedStringIds;
    private static Object unreceivedList_LOCK;
    private static ArrayList<String> unfinishedStringIds;
    private static Object unfinishedList_LOCK;
    private static int idNumber;
    private static Object idNumber_LOCK;
    private static Object socket_LOCK;

    public static void main(String[] args)
    {
        if (args.length != 2 || !isInteger(args[1])) {
            System.err.println("Usage: java EchoClient <host name> <port number>");
            System.exit(1);
        }

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);

        clientLetter = "C"; //hardcoded this for now
        thread1 = new ArrayList<>();
        thread2 = new ArrayList<>();
        thread3 = new ArrayList<>();
        unreceivedStringIds = new ArrayList<>();
        unreceivedList_LOCK = new Object();
        unfinishedStringIds = new ArrayList<>();
        unfinishedList_LOCK = new Object();
        idNumber = 1;
        idNumber_LOCK = new Object();
        socket_LOCK = new Object();

        try
                (
                        Socket clientSocket = new Socket(hostName, portNumber);
                        PrintWriter requestWriter = // stream to write text requests to server
                                new PrintWriter(clientSocket.getOutputStream(), true);
                        BufferedReader responseReader = // stream to read text response from server
                                new BufferedReader(
                                        new InputStreamReader(clientSocket.getInputStream()));
                        BufferedReader stdIn = // standard input stream to get user's requests
                                new BufferedReader(
                                        new InputStreamReader(System.in))
                )
        {
            System.out.println("Please send the next job type: ");
            String jobType = stdIn.readLine();
            while (!jobType.equalsIgnoreCase("A")
                    && !jobType.equalsIgnoreCase("B"))
            {
                System.out.println ("Please enter job type A or B");
                jobType = stdIn.readLine();
            }

            //Really this should be in a loop?
                //For thread1Num? in CapsNumClients?????
            ClientThread1 ct1 = new ClientThread1(clientLetter, jobType, idNumber, idNumber_LOCK, clientSocket, socket_LOCK,
                    unreceivedStringIds, unreceivedList_LOCK);
            thread1.add(ct1);

            //for (Thread t: thread1){t.start();}

            joinThreadsInPool(thread1);

            for (String fullID : unreceivedStringIds)
            {
                thread2.add(new ClientThread2(fullID, clientSocket, socket_LOCK,
                        unfinishedStringIds, unfinishedList_LOCK));
            }

            joinThreadsInPool(thread2);

            for (String fullID : unfinishedStringIds)
            {
                thread3.add(new ClientThread3(clientSocket, socket_LOCK));
            }

            //THIS CAN BE A METHOD!
            joinThreadsInPool(thread3);

        }
        catch (Exception e)
        {
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

    /**
     * Join all threads in an ArrayList of threads
     * @param threadPool ArrayList of threads to loop through and try to join
     */
    private static void joinThreadsInPool(ArrayList<Thread> threadPool)
    {
        for (Thread t : threadPool)
        {
            try
            {
                t.join();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }
}
