package in.haeg.datastructures;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class SelectedList<E> implements List<E> {
    private List<E> m_BaseList = new LinkedList<E>();
    private E       m_Selected = null;

    public SelectedList() {
    }

    public SelectedList(List<E> a_List) {
        m_BaseList = a_List;
    }

    public SelectedList(List<E> a_List, E a_Selected) {
        m_BaseList = a_List;
        m_Selected = a_Selected;
    }

    public void setSelected(E a_Selected) {
        if (m_BaseList.contains(a_Selected)) {
            m_Selected = a_Selected;
        }
    }

    public void setSelected(int a_SelectedIndex) {
        m_Selected = m_BaseList.get(a_SelectedIndex);
    }

    public E getSelected() {
        return m_Selected;
    }

    // Everything else is just delegated to the base list.

    @Override public boolean add(E a_e) {
        return m_BaseList.add(a_e);
    }

    @Override public void add(int a_index, E a_element) {
        m_BaseList.add(a_index, a_element);
    }

    @Override public boolean addAll(Collection<? extends E> a_c) {
        return m_BaseList.addAll(a_c);
    }

    @Override public boolean addAll(int a_index, Collection<? extends E> a_c) {
        return m_BaseList.addAll(a_index, a_c);
    }

    @Override public void clear() {
        m_BaseList.clear();
    }

    @Override public boolean contains(Object a_o) {
        return m_BaseList.contains(a_o);
    }

    @Override public boolean containsAll(Collection<?> a_c) {
        return m_BaseList.containsAll(a_c);
    }

    @Override public E get(int a_index) {
        return m_BaseList.get(a_index);
    }

    @Override public int indexOf(Object a_o) {
        return m_BaseList.indexOf(a_o);
    }

    @Override public boolean isEmpty() {
        return m_BaseList.isEmpty();
    }

    @Override public Iterator<E> iterator() {
        return m_BaseList.iterator();
    }

    @Override public int lastIndexOf(Object a_o) {
        return m_BaseList.lastIndexOf(a_o);
    }

    @Override public ListIterator<E> listIterator() {
        return m_BaseList.listIterator();
    }

    @Override public ListIterator<E> listIterator(int a_index) {
        return m_BaseList.listIterator(a_index);
    }

    @Override public boolean remove(Object a_o) {
        return m_BaseList.remove(a_o);
    }

    @Override public E remove(int a_index) {
        return m_BaseList.remove(a_index);
    }

    @Override public boolean removeAll(Collection<?> a_c) {
        return m_BaseList.removeAll(a_c);
    }

    @Override public boolean retainAll(Collection<?> a_c) {
        return m_BaseList.retainAll(a_c);
    }

    @Override public E set(int a_index, E a_element) {
        return m_BaseList.set(a_index, a_element);
    }

    @Override public int size() {
        return m_BaseList.size();
    }

    @Override public List<E> subList(int a_fromIndex, int a_toIndex) {
        return m_BaseList.subList(a_fromIndex, a_toIndex);
    }

    @Override public Object[] toArray() {
        return m_BaseList.toArray();
    }

    @Override public <T> T[] toArray(T[] a_a) {
        return m_BaseList.toArray(a_a);
    }

}
