package resources;

public class Done
{
    boolean finished;
    int clientNumber;

    public Done()
    {
        finished = false;
        clientNumber = 0;
    }

    public void setFinished(boolean isFinished)
    {
        finished = isFinished;
    }

    public boolean getIsFinished()
    {
        return finished;
    }

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
}
