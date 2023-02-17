package org.jargo.internal;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

class Stack<E> {

  private final Deque<E> stack = new ArrayDeque<>();

  public void push(E e) {
    stack.push(e);
  }

  public E pop() {
    return stack.pop();
  }

  public E peek() {
    if (stack.isEmpty()) throw new NoSuchElementException();
    return stack.peek();
  }

  public String describe() {
    StringBuilder sb = new StringBuilder();
    Iterator<E> it = stack.descendingIterator();
    while (it.hasNext()) {
      sb.append(it.next().toString());
      if (it.hasNext()) {
        sb.append("->");
      }
    }
    return sb.toString();
  }

  @Override
  public String toString() {
    return stack.toString();
  }
}
