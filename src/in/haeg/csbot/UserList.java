package in.haeg.csbot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class UserList implements List<User> {

    List<User> m_Users;

    public UserList() {
        m_Users = new ArrayList<User>(60); // Probably shouldn't hardcode this to 60 but it's meant for the #cs-york channel which only has 30 members online on average
    }

    public UserList(int a_NumbUsers) {
        m_Users = new ArrayList<User>(a_NumbUsers);
    }

    // public UserList(String[] a_Users) {
    // m_Users = new ArrayList<User>(a_Users.length);
    // for (String str : a_Users) {
    // m_Users.add(new User(str));
    // }
    // }

    public UserList(List<User> a_Users) {
        m_Users = a_Users;
    }

    public List<User> getUsers() {
        return m_Users;
    }

    public User get(String nick) throws NickNotFoundException {
        for (User user : m_Users) {
            if (user.hasNick(nick)) {
                return user;
            }
        }
        throw new NickNotFoundException();
    }

    public boolean contains(String nick) {
        for (User user : m_Users) {
            if (user.hasNick(nick)) {
                return true;
            }
        }
        return false;
    }

    // And we delegate everything else to the m_Users list

    @Override public boolean add(User e) {
        return m_Users.add(e);
    }

    @Override public void add(int index, User element) {
        m_Users.add(index, element);
    }

    @Override public boolean addAll(Collection<? extends User> c) {
        return m_Users.addAll(c);
    }

    @Override public boolean addAll(int index, Collection<? extends User> c) {
        return m_Users.addAll(index, c);
    }

    @Override public void clear() {
        m_Users.clear();
    }

    @Override public boolean contains(Object o) {
        return m_Users.contains(o);
    }

    @Override public boolean containsAll(Collection<?> c) {
        return m_Users.containsAll(c);
    }

    @Override public User get(int index) {
        return m_Users.get(index);
    }

    @Override public int indexOf(Object o) {
        return m_Users.indexOf(o);
    }

    @Override public boolean isEmpty() {
        return m_Users.isEmpty();
    }

    @Override public Iterator<User> iterator() {
        return m_Users.iterator();
    }

    @Override public int lastIndexOf(Object o) {
        return m_Users.lastIndexOf(o);
    }

    @Override public ListIterator<User> listIterator() {
        return m_Users.listIterator();
    }

    @Override public ListIterator<User> listIterator(int index) {
        return m_Users.listIterator(index);
    }

    @Override public boolean remove(Object o) {
        return m_Users.remove(o);
    }

    @Override public User remove(int index) {
        return m_Users.remove(index);
    }

    @Override public boolean removeAll(Collection<?> c) {
        return m_Users.removeAll(c);
    }

    @Override public boolean retainAll(Collection<?> c) {
        return m_Users.retainAll(c);
    }

    @Override public User set(int index, User element) {
        return m_Users.set(index, element);
    }

    @Override public int size() {
        return m_Users.size();
    }

    @Override public List<User> subList(int fromIndex, int toIndex) {
        return m_Users.subList(fromIndex, toIndex);
    }

    @Override public Object[] toArray() {
        return m_Users.toArray();
    }

    @Override public <T> T[] toArray(T[] a) {
        return m_Users.toArray(a);
    }

}
