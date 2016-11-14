package com.jfx.ts.gui;

import java.util.*;

public class CircularArrayList<E> extends AbstractList<E> implements RandomAccess {

    private int n; // buffer length
    private List<E> buf; // a List implementing RandomAccess
    private int leader = 0;
    private int size = 0;

    public CircularArrayList(int capacity) {
        n = capacity + 1;
        buf = new ArrayList<E>(Collections.nCopies(n, (E) null));
    }

    public int capacity() {
        return n - 1;
    }

    public void setCapacity(int capacity) {
        if (capacity() != capacity) {
            CircularArrayList<E> list = new CircularArrayList<>(capacity);
            list.addAll(this);
            n = list.n;
            buf = list.buf;
            leader = list.leader;
            size = list.size;
        }
    }

    private int wrapIndex(int i) {
        int m = i % n;
        if (m < 0) { // modulus can be negative
            m += n;
        }
        return m;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public E get(int i) {
        if (i < 0 || i >= n - 1) throw new IndexOutOfBoundsException();

        if (i > size()) throw new NullPointerException("Index is greater than size.");

        return buf.get(wrapIndex(leader - size + i));
    }

    @Override
    public E set(int i, E e) {
        if (i < 0 || i >= n - 1) {
            throw new IndexOutOfBoundsException();
        }
        if (i == size()) // assume leader's position as invalid (should use insert(e))
            throw new IndexOutOfBoundsException("The size of the list is " + size() + " while the index was " + i
                    + ". Please use insert(e) method to fill the list.");
        return buf.set(wrapIndex(leader - size + i), e);
    }

    @Override
    public void add(int index, E element) {
        insertElementAt(index, element);
    }

    public void insertElementAt(int i, E obj) {
        if (i == size) {
            insert(obj);
        } else {
            if (i < 0 || i >= n - 1) {
                throw new IndexOutOfBoundsException();
            }
            if (i == size()) // assume leader's position as invalid (should use insert(e))
                throw new IndexOutOfBoundsException("The size of the list is " + size() + " while the index was " + i
                        + ". Please use insert(e) method to fill the list.");
            //
            if (size < capacity()) {
                insert(buf.get(wrapIndex(leader - 1)));
            }
            for (int p = size - 1; p > i; p--) {
                buf.set(wrapIndex(leader - size + p), buf.get(wrapIndex(leader - size + p - 1)));
            }
            buf.set(wrapIndex(leader - size + i), obj);
        }
    }

    @Override
    public boolean add(E e) {
        insert(e);
        return true;
    }

    public void insert(E e) {
        int s = size();
        buf.set(wrapIndex(leader), e);
        leader = wrapIndex(++leader);
        buf.set(leader, null);
        if (s == n - 1)
            return; // we have replaced the eldest element.
        this.size++;
    }

    @Override
    public void clear() {
        int cnt = wrapIndex(leader - size());
        for (; cnt != leader; cnt = wrapIndex(++cnt))
            this.buf.set(cnt, null);
        this.size = 0;
    }

    public E removeOldest() {
        int i = wrapIndex(leader + 1);

        for (; ; i = wrapIndex(++i)) {
            if (buf.get(i) != null) break;
            if (i == leader)
                throw new IllegalStateException("Cannot remove element."
                        + " CircularArrayList is empty.");
        }

        this.size--;
        return buf.set(i, null);
    }

    @Override
    public String toString() {
        int i = wrapIndex(leader - size);
        StringBuilder str = new StringBuilder(size);

        for (; i != leader; i = wrapIndex(++i)) {
            str.append(buf.get(i));
        }
        return str.toString();
    }

    public E getOldest() {
        int i = wrapIndex(leader + 1);

        for (; ; i = wrapIndex(++i)) {
            if (buf.get(i) != null) break;
            if (i == leader)
                throw new IllegalStateException("Cannot remove element."
                        + " CircularArrayList is empty.");
        }

        return buf.get(i);
    }

    public E getNewest() {
        int i = wrapIndex(leader - 1);
        if (buf.get(i) == null)
            throw new IndexOutOfBoundsException("Error while retrieving the newest element. The Circular Array list is empty.");
        return buf.get(i);
    }

    public static void main(String[] args) {
        CircularArrayList<Integer> a = new CircularArrayList<>(3);
        a.insert(1);
        a.insert(3);
        a.insertElementAt(1, 2);
        for(Integer i : a) {
            System.out.println(""+i);
        }
        a.insert(5);
    }
}