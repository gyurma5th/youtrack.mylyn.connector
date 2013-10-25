package com.jetbrains.youtrack.javarest.client;

import javax.xml.bind.annotation.XmlValue;

public class IssueTag {

  private String text;

  @XmlValue
  public String getText() {
    return text;
  }

  public void setText(String tag) {
    this.text = tag;
  }

}
