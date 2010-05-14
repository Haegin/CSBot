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

    public static void main(String[] args) {
        m_Log = Logger.getLogger(Main.class.getName());
        m_Log.setLevel(Level.FINEST);
        m_Users = new UserList();

        m_Conn = new IRCConnection(Constants.HOST, Constants.PORT, Constants.PASSWORD, Constants.NICK_NAME, Constants.USER_NAME, Constants.REAL_NAME);
        m_Conn.setColors(false);
        m_Conn.setPong(true);
        try {
            m_Conn.connect();

            m_Conn.addIRCEventListener(new IRCEventListener() {

                @Override public void unknown(String a_prefix, String a_command, String a_middle, String a_trailing) {
                    m_Log.log(Level.INFO, "Received unknown message: " + a_prefix + ", " + a_command + ", " + a_middle + ", " + a_trailing);
                }

                @Override public void onTopic(String a_chan, IRCUser a_user, String a_topic) {
                }

                @Override public void onReply(int a_num, String a_value, String a_msg) {

                    switch (a_num) {
                        // If the reply is part of a who request we need to record all the users if they don't already exist.
                        case IRCUtil.RPL_WHOREPLY:
                            System.out.println("REPLY: " + a_value + " =][= " + a_msg);
                            String[] values = a_value.split(" "); // Split the value up into its constituent parts
                            // a_value is in the form "<bot nick> <channel> <user> <host> <server> <nick> <H|G>[*][@|+] <hopcount> <realname>"
                            processUser(values[2], values[3], values[4], values[5], values[6], a_msg);

                            // If we get anything else then we don't need to do anything
                        default:
                            break;
                    }
                }

                @Override public void onRegistered() {
                }

                @Override public void onQuit(IRCUser a_user, String a_msg) {
                    if (a_user.getNick().equals("prettygreat") && a_msg.contains("egen...")) {
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

            m_Conn.doJoin(Constants.CHANNEL);
            m_Conn.doWho(Constants.CHANNEL);
            m_Conn.doPrivmsg(Constants.CHANNEL, "Hi, I'm a fairly basic karma bot. Tell Haegin if you want me to be able to do more than just count karma");

        } catch (IOException ex) {
            m_Log.log(Level.WARNING, "Couldn't connect to server " + Constants.HOST);
        }
    }

    protected static void processUser(String user, String host, String server, String nick, String flags, String real) {
        // Don't bother adding the new user if they already exist in the list.
        if (!m_Users.contains(nick)) {
            User newUser = new User(nick, user, real, host, server);
            if (flags.contains("@")) {
                newUser.setOperator(true);
            }
            m_Users.add(newUser);
        }
    }

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

    // TODO: Check to make sure nobody else has the new nick
    protected static void updateNick(String a_OldNick, String a_NewNick) {
        if (m_Users.contains(a_OldNick)) {
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

    private static String karmacipient(String a_Message, String a_Suffix) {
        String karmacipient = a_Message.substring(0, a_Message.lastIndexOf(a_Suffix)).trim();
        karmacipient = karmacipient.replaceAll("[#:,.!\"$%&*()?+/]", "");
        if (karmacipient.contains(" ")) { // get the last word before the '--'
            karmacipient = karmacipient.substring(karmacipient.lastIndexOf(" ") + 1).trim();
        }
        return karmacipient;
    }

}
