package dep;

public class FileCorruptException extends Exception{
    public FileCorruptException (String s)
    {
        // Call constructor of parent Exception
        super(s);
    }
}
