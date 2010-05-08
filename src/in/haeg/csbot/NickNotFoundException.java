package in.haeg.csbot;

public class NickNotFoundException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -2991148622207908574L;

    public NickNotFoundException() {
    }

    public NickNotFoundException(String a_Message) {
        super(a_Message);
    }
}
