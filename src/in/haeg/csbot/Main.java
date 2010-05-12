package in.haeg.csbot;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.schwering.irc.lib.IRCConnection;
import org.schwering.irc.lib.IRCEventListener;
import org.schwering.irc.lib.IRCModeParser;
import org.schwering.irc.lib.IRCUser;
import org.schwering.irc.lib.IRCUtil;

public class Main {
    private static IRCConnection m_Conn;
    private static Logger        m_Log;
    private static UserList      m_Users;

    /**
     * The main run loop. This handles all the incoming and outgoing IRC messages using the IRCLib Java library.
     * 
     * @param args
     *            Whatever command line arguments are passed in. None are used even if they are supplied.
     */
    public static void main(String[] args) {
        m_Log = Logger.getLogger(Main.class.getName());
        m_Log.setLevel(Level.FINEST);
        m_Users = new UserList();

        m_Conn = new IRCConnection(Constants.HOST, Constants.PORT, Constants.PASSWORD, Constants.NICK_NAME, Constants.USER_NAME, Constants.REAL_NAME);
        m_Conn.setColors(false);
        m_Conn.setPong(true);
        try {
            m_Conn.connect();
            m_Conn.doJoin(Constants.CHANNEL);
            m_Conn.doPrivmsg(Constants.CHANNEL, "Hi, I'm a fairly basic karma bot. Tell Haegin if you want me to be able to do more than just count karma");

            m_Conn.addIRCEventListener(new IRCEventListener() {

                @Override public void unknown(String a_prefix, String a_command, String a_middle, String a_trailing) {
                    m_Log.log(Level.INFO, "Received unknown message: " + a_prefix + ", " + a_command + ", " + a_middle + ", " + a_trailing);
                }

                @Override public void onTopic(String a_chan, IRCUser a_user, String a_topic) {
                }

                @Override public void onReply(int a_num, String a_value, String a_msg) {
                    // System.out.println("REPLY: " + a_value + " =][= " + a_msg);

                    switch (a_num) {
                        // If the reply is a list of names then we want to process them and add them to the list of karma names
                        case IRCUtil.RPL_NAMREPLY:
                            processNames(a_msg.split("[ \t]+"));
                            break;

                        // If the reply is part of a whois then we want to create the user if necessary and update the relevant details
                        case IRCUtil.RPL_WHOISUSER:
                            processUser(a_value.replaceAll("\\*:~", "").split("[ \t]+"));
                            break;

                        case IRCUtil.RPL_WHOISCHANNELS:
                            processWhoisChannels(a_value);
                            break;

                        // If we get anything else then we don't need to do anything
                        default:
                            break;
                    }
                }

                @Override public void onRegistered() {
                }

                @Override public void onQuit(IRCUser a_user, String a_msg) {
                    if (a_user.getNick().equals("prettygreat")) {
                        m_Conn.doPrivmsg("#cs-york", "...dary");
                    }
                }

                @Override public void onPrivmsg(String a_target, IRCUser a_user, String a_msg) {

                    if (a_target.equals(Constants.CHANNEL)) {
                        if (a_msg.contains("++")) {
                            incrementKarma(a_msg, a_user.getNick());
                        }
                        // This isn't an else as something like "++ Foo --" should decrement Foo (assuming Foo is a nickname)
                        if (a_msg.contains("--")) {
                            decrementKarma(a_msg, a_user.getNick());
                        }
                    }

                    // Actions
                    if ((a_target.equals(Constants.NICK_NAME) || a_target.equals(Constants.CHANNEL)) && a_msg.startsWith("!")) {
                        if (a_msg.startsWith("!karma")) {
                            showKarma(a_msg, a_user.getNick());
                        }

                        if (m_Users.contains(a_user.getNick())) {
                            try {
                                if (m_Users.get(a_user.getNick()).isOperator()) {
                                    if (a_msg.startsWith("!resetKarma")) {
                                        resetKarma(a_msg.split(" ")[1]);
                                    } else if (a_msg.startsWith("!begone")) {
                                        m_Conn.doQuit("As you wish, sir."); // TODO: add the ability to send a custom quit message.
                                    }
                                }
                            } catch (NickNotFoundException ex) {
                                // Shouldn't happen
                            }
                        }

                    }
                }

                @Override public void onPing(String a_ping) {
                }

                @Override public void onPart(String a_chan, IRCUser a_user, String a_msg) {
                }

                @Override public void onNotice(String a_target, IRCUser a_user, String a_msg) {
                    System.out.println("NOTICE: " + a_msg);
                }

                @Override public void onNick(IRCUser a_user, String a_newNick) {
                    updateNick(a_user.getNick(), a_newNick);
                }

                @Override public void onMode(IRCUser a_user, String a_passiveNick, String a_mode) {
                }

                @Override public void onMode(String a_chan, IRCUser a_user, IRCModeParser a_modeParser) {
                    for (int modeIndex = 1; modeIndex <= a_modeParser.getCount(); modeIndex++) {
                        if (a_modeParser.getModeAt(modeIndex) == 'o') {
                            char operator = a_modeParser.getOperatorAt(modeIndex);
                            if (operator == '+') { // Someone gained ops
                                addOps(a_modeParser.getArgAt(modeIndex));
                            } else if (operator == 's') { // Someone lost ops
                                removeOps(a_modeParser.getArgAt(modeIndex));
                            }
                        }
                    }
                }

                @Override public void onKick(String a_chan, IRCUser a_user, String a_passiveNick, String a_msg) {
                }

                @Override public void onJoin(String a_chan, IRCUser a_user) {
                    m_Conn.doWhois(a_user.getNick());
                }

                @Override public void onInvite(String a_chan, IRCUser a_user, String a_passiveNick) {
                    m_Conn.doWhois(a_user.getNick());
                }

                @Override public void onError(int a_num, String a_msg) {
                    System.out.println("ERROR: " + a_msg);
                }

                @Override public void onError(String a_msg) {
                    System.out.println("ERROR: " + a_msg);
                }

                @Override public void onDisconnected() {
                    System.out.println("DISCO");
                }
            });

        } catch (IOException ex) {
            m_Log.log(Level.WARNING, "Couldn't connect to server " + Constants.HOST);
        }
    }

    /**
     * Processes the channel list section of a whois response. This records the user as a channel op if they are an op on Constants.CHANNEL
     * 
     * @param a_value
     *            The whois channel response as from IRCLib
     */
    protected static void processWhoisChannels(String a_value) {
        String nick = a_value.split(" ")[0].trim();
        if (a_value.contains("@" + Constants.CHANNEL)) {
            if (m_Users.contains(nick)) {
                try {
                    m_Users.get(nick).setOperator(true);
                } catch (NickNotFoundException ex) {
                    // Shouldn't ever occur
                }
            }
        }
    }

    /**
     * Informs the channel of the given users karma. The message is set to the whole channel but will hilight the user that made the inquiry.
     * 
     * @param a_msg
     *            the message the user sent that began with !karma
     * @param a_RespondTo
     *            the user who made the inquiry
     */
    protected static void showKarma(String a_msg, String a_RespondTo) {
        String karmacipient = a_msg.split("[ \t]")[1];
        if (m_Users.contains(karmacipient)) {
            try {
                m_Conn.doPrivmsg(Constants.CHANNEL, a_RespondTo + ": " + karmacipient + "'s karma is currently " + m_Users.get(karmacipient).getKarma());
            } catch (NickNotFoundException ex) {
                // Shouldn't ever occur
            }
        }
    }

    /**
     * Adds a new nick to a user determined by their old nick. The new nick is set as the selected nick.
     * 
     * @param a_OldNick
     *            the nick the user has switched from.
     * @param a_NewNick
     *            the nick the user has switched to.
     */
    // TODO: Check to make sure nobody else has the new nick
    protected static void updateNick(String a_OldNick, String a_NewNick) {
        if (m_Users.contains(a_OldNick) && !m_Users.contains(a_NewNick)) {
            try {
                User user = m_Users.get(a_OldNick);
                if (user.hasNick(a_NewNick)) {
                    user.setSelectedNick(a_NewNick);
                } else {
                    user.addLatestNick(a_NewNick);
                }
            } catch (NickNotFoundException ex) {
                // Shouldn't ever occur
            }
        } else {
            m_Conn.doWhois(a_NewNick);
        }
    }

    /**
     * Processes the user information of a whois response to get the users nickname, username and host information.
     * 
     * @param a_UserInfo
     *            the userinfo from the whois response as found in IRCLib
     */
    protected static void processUser(String[] a_UserInfo) {
        System.out.println(a_UserInfo[1]);
        if (a_UserInfo.length >= 4) {
            String nickname = a_UserInfo[1];
            String username = a_UserInfo[2];
            String host = a_UserInfo[3];

            if (m_Users.contains(nickname)) {
                try {
                    User user = m_Users.get(nickname);
                    user.setHost(host);
                    user.setUsername(username);
                } catch (NickNotFoundException ex) {
                    // This should never happen as we have checked using contains but if it does happen we'll log it.
                    m_Log.log(Level.WARNING, "Something has gone wrong as this should never happen. There must be a bug somewhere.");
                }
            } else {
                User user = new User(nickname, username, host);
                m_Users.add(user);
            }
        }
    }

    /**
     * Processes the response to a names request to send out whois requests to get user information.
     * 
     * @param a_Names
     *            the nicknames from the name request
     */
    // TODO: replace using names with using who to get all the information at once.
    protected static void processNames(String[] a_Names) {
        for (String nick : a_Names) {
            try {
                Thread.sleep(2000); // Ugly hack - need to make this far superior...
            } catch (InterruptedException ex) {
            }
            m_Conn.doWhois(nick.replaceAll("[@\\+%]", ""));
        }
    }

    /**
     * Set a user to be marked as a channel operator. Note this doesn't change anything on the IRC side, just the internal user records.
     * 
     * @param nick
     *            the user to be marked as operator.
     */
    protected static void addOps(String nick) {
        if (m_Users.contains(nick)) {
            try {
                m_Users.get(nick).setOperator(true);
            } catch (NickNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * Marks a user as not a channel operator. Note this doesn't change anything on the IRC side, just the internal user records.
     * 
     * @param nick
     *            the user to be marked as not an operator.
     */
    protected static void removeOps(String nick) {
        if (m_Users.contains(nick)) {
            try {
                m_Users.get(nick).setOperator(false);
            } catch (NickNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * Increments a users karma score.
     * 
     * @param a_Message
     *            The message a_FromNick sent that initiated the karma incrementation.
     * @param a_FromNick
     *            The user who sent the message.
     */
    protected static void incrementKarma(String a_Message, String a_FromNick) {
        String karmacipient = karmacipient(a_Message, "++");
        if (!a_FromNick.equals(karmacipient) && m_Users.contains(karmacipient)) { // Don't allow anyone to karma themselves
            try {
                m_Users.get(karmacipient).incrementKarma();
                m_Conn.doPrivmsg(Constants.CHANNEL, karmacipient + "'s karma is now " + m_Users.get(karmacipient).getKarma());
                System.out.println("Incrementing " + karmacipient + "'s karma");
            } catch (NickNotFoundException ex) {
                // This won't happen due to the contains guard above
            }
        }
    }

    /**
     * Decrements a users karma score.
     * 
     * @param a_Message
     *            The message a_FromNick sent that intiated the karma decrementation.
     * @param a_FromNick
     *            The user who sent the message.
     */
    protected static void decrementKarma(String a_Message, String a_FromNick) {
        String karmacipient = karmacipient(a_Message, "--");
        if (!karmacipient.equals("") && !a_FromNick.equals(karmacipient) && m_Users.contains(karmacipient)) { // Don't allow anyone to karma themselves
            try {
                m_Users.get(karmacipient).decrementKarma();
                m_Conn.doPrivmsg(Constants.CHANNEL, karmacipient + "'s karma is now " + m_Users.get(karmacipient).getKarma());
                System.out.println("Decrementing " + karmacipient + "'s karma");
            } catch (NickNotFoundException ex) {
                // This won't happen due to the contains guard above
            }
        }
    }

    /**
     * Resets a users karma to zero.
     * 
     * @param a_nick
     *            The user for whom karma should be reset to zero.
     */
    protected static void resetKarma(String a_nick) {
        if (m_Users.contains(a_nick)) {
            try {
                m_Users.get(a_nick).resetKarma();
                m_Conn.doPrivmsg(Constants.CHANNEL, "The karma for " + a_nick + " has been reset to zero.");
            } catch (NickNotFoundException ex) {
                // Shouldn't happen.
            }
        }
    }

    // private static String karmacipient(String a_Message, String a_Suffix) {
    // Pattern patt = Pattern.compile("([a-zA-Z][a-zA-Z0-9[-\\[\\]`\\^\\{\\}]]*)[#:,\\.!\"$%&\\*\\(\\)\\?\\+/ \t]*" + Pattern.quote(a_Suffix));
    // System.out.println(patt.toString());
    // Matcher match = patt.matcher(a_Message);
    // System.out.println(a_Message);
    // if (match.groupCount() > 0) {
    // return match.group(1);
    // } else {
    // return "";
    // }
    // }

    /**
     * Used to parse a karma increment or decrement message to find the user who needs their karma altering.
     * 
     * @param a_Message
     *            The message that was originally sent.
     * @param a_Suffix
     *            The suffix, either '++' or '--' depending on whether the karma should be incremented or decremented.
     * @return the nick of the user to act upon.
     */
    private static String karmacipient(String a_Message, String a_Suffix) {
        String karmacipient = a_Message.substring(0, a_Message.lastIndexOf(a_Suffix)).trim();
        karmacipient = karmacipient.replaceAll("[#:,.!\"$%&*()?+/]", "");
        if (karmacipient.contains(" ")) { // get the last word before the '--'
            karmacipient = karmacipient.substring(karmacipient.lastIndexOf(" ") + 1).trim();
        }
        return karmacipient;
    }

}
