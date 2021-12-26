package resources;

public class Done
{
    boolean finished;
   // int clientNumber;
    //int clientExitNumber;
   // boolean atLeastOneJoined;

    public Done()
    {
        finished = false;
        //clientNumber = 0;
       // clientExitNumber = 0;
        // atLeastOneJoined = false;
    }

    public void setFinished(boolean isFinished)
    {
        finished = isFinished;
    }

    public boolean isFinished()
    {
        return finished;
    }

    /*
    public int getClientNumber()
    {
        return clientNumber;
    }

    public void addClient()
    {
        clientNumber++;
    }

    public void removeClient()
    {
        clientNumber--;
    }
     */

    /*
    public void addClientExitNumber()
    {
        clientExitNumber ++;
    }

    public int getClientExitNumber()
    {
        return clientExitNumber;
    }

    public void setAtLeastOneJoined(boolean joined)
    {
        atLeastOneJoined = joined;
    }

    public boolean atLeastOneJoined()
    {
        return atLeastOneJoined;
    }
     */
}
