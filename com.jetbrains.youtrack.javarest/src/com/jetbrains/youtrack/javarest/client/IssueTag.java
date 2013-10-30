package com.jetbrains.youtrack.javarest.client;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

public class IssueTag {

  private String text;

  private String name;

  private boolean untagOnResolve;

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

  @XmlAttribute(name = "name")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @XmlAttribute(name = "untagOnResolve")
  public boolean isUntagOnResolve() {
    return untagOnResolve;
  }

  public void setUntagOnResolve(boolean untagOnResolve) {
    this.untagOnResolve = untagOnResolve;
  }

}
