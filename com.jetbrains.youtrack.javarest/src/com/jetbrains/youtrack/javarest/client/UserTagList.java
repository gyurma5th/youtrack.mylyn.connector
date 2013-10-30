package com.jetbrains.youtrack.javarest.client;

import java.util.LinkedList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "tags")
public class UserTagList {

  private LinkedList<IssueTag> tags;

  @XmlElement(name = "search")
  public LinkedList<IssueTag> getTags() {
    return tags;
  }

  public void setTags(LinkedList<IssueTag> tags) {
    this.tags = tags;
  }

  public String[] getOptions() {
    String[] options = null;
    if (tags != null) {
      options = new String[tags.size()];
      for (IssueTag tag : tags) {
        options[tags.indexOf(tag)] = tag.getName();
      }
    }
    return options;
  }
}
