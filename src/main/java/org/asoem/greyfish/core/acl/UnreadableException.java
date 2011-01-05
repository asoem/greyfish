package org.asoem.sico.core.acl;

@SuppressWarnings("serial")
public class UnreadableException extends Exception {
/**
* Constructs an <code>UnreadableException</code> with the specified detail
* message.
* @param the detail message.
*/
  UnreadableException(String msg) {
    super(msg);
  }

}