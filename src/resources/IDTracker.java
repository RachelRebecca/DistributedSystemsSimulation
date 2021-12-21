package resources;

public class IDTracker
{
    private int id;

    public IDTracker()
    {
        id = 1;
    }
    public void incrementID()
    {
        id++;
    }

    public int getID()
    {
        return id;
    }
}
