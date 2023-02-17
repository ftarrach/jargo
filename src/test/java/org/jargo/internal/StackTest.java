package org.jargo.internal;

import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class StackTest {

  @Test
  public void testStack() {
    Stack<String> stack = new Stack<>();
    assertEquals("[]", stack.toString());
    assertThrows(NoSuchElementException.class, stack::peek);
    assertThrows(NoSuchElementException.class, stack::pop);
    stack.push("foo");
    assertEquals("[foo]", stack.toString());
    stack.push("bar");
    assertEquals("[bar, foo]", stack.toString());
    assertEquals("bar", stack.peek());
    assertEquals("bar", stack.pop());
    assertEquals("[foo]", stack.toString());
    assertEquals("foo", stack.pop());
    assertEquals("[]", stack.toString());
  }

}
