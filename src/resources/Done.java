package resources;

public class Done
{
    // boolean flag to determine if a program and Threads should terminate
    boolean finished;

    public Done()
    {
        finished = false;
    }

    public void setFinished(boolean isFinished)
    {
        finished = isFinished;
    }

    public boolean isFinished()
    {
        return finished;
    }

}
