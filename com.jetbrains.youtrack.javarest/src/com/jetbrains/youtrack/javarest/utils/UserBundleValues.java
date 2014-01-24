/**
 * @author: amarch
 */

package com.jetbrains.youtrack.javarest.utils;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "userBundle")
public class UserBundleValues extends BundleValues {

  @XmlElement(name = "user", type = UserValue.class)
  private LinkedList<UserValue> bundleUserValues;

  public LinkedList<UserValue> getUsers() {
    return bundleUserValues;
  }

  @XmlElement(name = "userGroup", type = UserGroupValue.class)
  private LinkedList<UserGroupValue> bundleUserGroupValues;

  public LinkedList<UserGroupValue> getUserGroupValues() {
    return bundleUserGroupValues;
  }

  private LinkedList<UserValue> usersFromGroups;

  public void addUsersFromGroup(LinkedList<UserValue> groupUsers) {
    if (usersFromGroups == null) {
      usersFromGroups = new LinkedList<UserValue>();
    }
    if (groupUsers != null && groupUsers.size() > 0) {
      usersFromGroups.addAll(groupUsers);
    }
  }

  @Override
  public LinkedList<String> getValues() {
    LinkedList<String> values = new LinkedList<String>();
    Set<String> uniqueUsers = new HashSet<String>();

    if (bundleUserValues != null) {
      for (BundleValue value : bundleUserValues) {
        uniqueUsers.add(value.getValue());
      }
    }

    if (usersFromGroups != null) {
      for (BundleValue value : usersFromGroups) {
        uniqueUsers.add(value.getValue());
      }
    }

    values.addAll(uniqueUsers);
    return values;
  }

}
