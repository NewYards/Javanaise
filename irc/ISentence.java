package irc;
import jvn.*;

public interface ISentence {
    @WriteOperation
    public void write(String text);
    @ReadOperation
    public String read();
}
