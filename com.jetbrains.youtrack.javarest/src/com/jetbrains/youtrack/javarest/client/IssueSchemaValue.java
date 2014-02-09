package com.jetbrains.youtrack.javarest.client;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

public class IssueSchemaValue {

  @XmlValue
  private String value;

  @XmlAttribute(name = "type")
  private String type;

  @XmlAttribute(name = "role")
  private String role;

  @XmlAttribute(name = "fullName")
  private String fullName;

  public String getValue() {
    return value;
  }

  public String getType() {
    return type;
  }

  public String getRole() {
    return role;
  }

  public String getFullName() {
    return fullName;
  }
}
