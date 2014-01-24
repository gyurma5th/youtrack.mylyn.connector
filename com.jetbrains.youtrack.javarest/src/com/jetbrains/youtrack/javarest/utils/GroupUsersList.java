package com.jetbrains.youtrack.javarest.utils;

import java.util.LinkedList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "userRefs")
public class GroupUsersList {

  @XmlElement(name = "user", type = UserValue.class)
  private LinkedList<UserValue> users;

  public LinkedList<UserValue> getUsers() {
    return users;
  }

  public LinkedList<String> getValues() {
    LinkedList<String> values = new LinkedList<String>();
    if (users != null) {
      for (UserValue value : users) {
        values.add(value.getValue());
      }
    }
    return values;
  }
}
