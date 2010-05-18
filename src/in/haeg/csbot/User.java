package in.haeg.csbot;

import in.haeg.datastructures.SelectedList;

import java.util.List;

import org.schwering.irc.lib.IRCUser;

public class User {
    private SelectedList<String> m_Nicknames;
    private String               m_Username;
    private String               m_Host;
    private String               m_Server;
    private String               m_RealName;
    private int                  m_Karma = 0;
    private boolean              m_IsOperator;

    public User(String a_Nickname) {
        m_Nicknames = new SelectedList<String>();
        m_Nicknames.add(a_Nickname);
        m_Nicknames.setSelected(a_Nickname);
    }

    public User(String a_Nickname, String a_Username, String a_RealName, String a_Host, String a_Server) {
        m_Nicknames = new SelectedList<String>();
        m_Nicknames.add(a_Nickname);
        m_Nicknames.setSelected(a_Nickname);
        m_Username = a_Username;
        m_RealName = a_RealName;
        m_Host = a_Host;
        m_Server = a_Server;
    }

    public User(IRCUser a_IRCUser) {
        m_Nicknames = new SelectedList<String>();
        m_Nicknames.add(a_IRCUser.getNick());
        m_Nicknames.setSelected(a_IRCUser.getNick());
        m_Username = a_IRCUser.getUsername();
        m_Host = a_IRCUser.getHost();
    }

    public boolean hasNick(String a_Nick) {
        for (String nickname : m_Nicknames) {
            if (nickname.equals(a_Nick)) {
                return true;
            }
        }
        return false;
    }

    public void addNick(String a_Nick) {
        m_Nicknames.add(a_Nick);
    }

    public void addLatestNick(String a_Nick) {
        m_Nicknames.add(a_Nick);
        m_Nicknames.setSelected(a_Nick);
    }

    public String getUsername() {
        return m_Username;
    }

    public String getHost() {
        return m_Host;
    }

    public void setRealName(String realName) {
        m_RealName = realName;
    }

    public String getRealName() {
        return m_RealName;
    }

    public boolean isOperator() {
        return m_IsOperator;
    }

    public String getNickname() {
        return m_Nicknames.getSelected();
    }

    public List<String> getNicknames() {
        return m_Nicknames;
    }

    public void setNicknames(SelectedList<String> a_Nicknames) {
        m_Nicknames = a_Nicknames;
    }

    public void setSelectedNick(String a_Nickname) {
        m_Nicknames.setSelected(a_Nickname);
    }

    public void setUsername(String a_Username) {
        m_Username = a_Username;
    }

    public void setHost(String a_Host) {
        m_Host = a_Host;
    }

    public void setOperator(boolean a_IsOperator) {
        m_IsOperator = a_IsOperator;
    }

    /* Karma */
    public Integer getKarma() {
        return m_Karma;
    }

    public void resetKarma() {
        m_Karma = 0;
    }

    public void incrementKarma() {
        m_Karma++;
    }

    public void decrementKarma() {
        m_Karma--;
    }

    public void setServer(String server) {
        m_Server = server;
    }

    public String getServer() {
        return m_Server;
    }

}
