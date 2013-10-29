package com.jetbrains.youtrack.javarest.client;

import javax.xml.bind.annotation.XmlValue;

public class IssueTag {

  private String text;

  public IssueTag() {}

  public IssueTag(String text) {
    setText(text);
  }

  @XmlValue
  public String getText() {
    return text;
  }

  public void setText(String tag) {
    this.text = tag;
  }

  public int hashCode() {
    return text.hashCode();
  }

}
