package analyzer.common;

import java.io.Serializable;

public class MessageJtoJ implements Serializable {
    private String message;
    private String command;

    public MessageJtoJ(String m, String c)
    {
        message = m;
        command = c;
    }

    public String getMessage() { return message; }
    public String getCommand() { return command; }
    public void setMessage(String m) { message = m; }
    public void setCommand(String c) { message = c; }
}
