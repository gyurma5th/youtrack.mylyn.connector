package com.jetbrains.youtrack.javarest.client;

import javax.xml.bind.annotation.XmlAttribute;

public class IssueLinkType {

  private String name;

  private String inwardName;

  private String outwardName;

  private boolean directed;

  public IssueLinkType() {}

  @XmlAttribute(name = "name")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @XmlAttribute(name = "directed")
  public boolean isDirected() {
    return directed;
  }

  public void setDirected(boolean directed) {
    this.directed = directed;
  }

  @XmlAttribute(name = "inwardName")
  public String getInwardName() {
    return inwardName;
  }

  public void setInwardName(String inwardName) {
    this.inwardName = inwardName;
  }

  @XmlAttribute(name = "outwardName")
  public String getOutwardName() {
    return outwardName;
  }

  public void setOutwardName(String outwardName) {
    this.outwardName = outwardName;
  }


}
