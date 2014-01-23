package com.jetbrains.youtrack.javarest.client;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "issueLinkPrototypes")
public class IssueLinkTypesList {

  private LinkedList<IssueLinkType> types;

  @XmlElement(name = "issueLinkType")
  public LinkedList<IssueLinkType> getTypes() {
    return types;
  }

  public void setTypes(LinkedList<IssueLinkType> types) {
    this.types = types;
  }

  public String[] getAllLinkTypeCommands() {
    Set<String> commands = null;
    if (types != null) {
      commands = new HashSet<String>();
      for (IssueLinkType type : types) {
        commands.add(type.getInwardName());
        commands.add(type.getOutwardName());
      }
    }
    return commands.toArray(new String[0]);
  }
}
