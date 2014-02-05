package com.jetbrains.youtrack.javarest.utils;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "user")
public class UserValue extends BundleValue {

  @XmlAttribute(name = "login")
  private String value;

  public String getValue() {
    return value;
  }
}