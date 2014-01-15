package com.jetbrains.mylyn.yt.core;

import org.eclipse.mylyn.tasks.core.data.TaskAttribute;

public enum YouTrackAttribute {
  SUMMARY(
      TaskAttribute.SUMMARY,
      TaskAttribute.TYPE_SHORT_RICH_TEXT,
      "Summary:",
      YouTrackTaskDataHandler.SINGLE_FIELD_KIND,
      true),
  DATE_CREATION(
      TaskAttribute.DATE_CREATION,
      TaskAttribute.TYPE_DATETIME,
      "Created:",
      YouTrackTaskDataHandler.SINGLE_FIELD_KIND,
      true),
  TASK_KEY(
      TaskAttribute.TASK_KEY,
      TaskAttribute.TYPE_SHORT_TEXT,
      "Issue key:",
      YouTrackTaskDataHandler.SINGLE_FIELD_KIND,
      true),
  UPDATED_DATE(
      TaskAttribute.DATE_MODIFICATION,
      TaskAttribute.TYPE_DATETIME,
      "Updated:",
      YouTrackTaskDataHandler.SINGLE_FIELD_KIND,
      true),
  DESCRIPTION(
      TaskAttribute.DESCRIPTION,
      TaskAttribute.TYPE_LONG_RICH_TEXT,
      "Description:",
      YouTrackTaskDataHandler.SINGLE_FIELD_KIND,
      true),
  WIKIFY_DESCRIPTION(
      YouTrackTaskDataHandler.WIKIFY_DESCRIPTION,
      YouTrackTaskDataHandler.TYPE_HTML,
      "Wikify Description:",
      YouTrackTaskDataHandler.SINGLE_FIELD_KIND,
      true),
  USER_REPORTER(
      TaskAttribute.USER_REPORTER,
      TaskAttribute.TYPE_SHORT_TEXT,
      "Reporter:",
      YouTrackTaskDataHandler.SINGLE_FIELD_KIND,
      true),
  USER_ASSIGNED(
      TaskAttribute.USER_ASSIGNED,
      TaskAttribute.TYPE_SHORT_TEXT,
      "Assignee:",
      YouTrackTaskDataHandler.SINGLE_FIELD_KIND,
      true),
  RESOLVED(
      TaskAttribute.STATUS,
      TaskAttribute.TYPE_BOOLEAN,
      "Resolved:",
      YouTrackTaskDataHandler.SINGLE_FIELD_KIND,
      false),
  PRIORITY_LEVEL(
      TaskAttribute.PRIORITY,
      TaskAttribute.TYPE_SHORT_TEXT,
      "Priority level:",
      YouTrackTaskDataHandler.SINGLE_FIELD_KIND,
      true),
  PROJECT(
      TaskAttribute.PRODUCT,
      TaskAttribute.TYPE_SINGLE_SELECT,
      "Project:",
      YouTrackTaskDataHandler.SINGLE_FIELD_KIND,
      true),
  COMMENT_NEW(
      YouTrackTaskDataHandler.COMMENT_NEW,
      TaskAttribute.TYPE_LONG_RICH_TEXT,
      "New comment:",
      YouTrackTaskDataHandler.SINGLE_FIELD_KIND,
      false);


  private String name;

  private String type;

  private String label;

  private String kind;

  private boolean readOnly;

  YouTrackAttribute(String name, String type, String label, String kind, boolean readOnly) {
    this.name = name;
    this.setType(type);
    this.setLabel(label);
    this.setKind(kind);
    this.setReadOnly(readOnly);
  }

  public String getName() {
    return name;
  }

  public static YouTrackAttribute getTypeByName(String name) {
    for (YouTrackAttribute type : values()) {
      if (type.getName().equals(name)) {
        return type;
      }
    }
    return null;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getKind() {
    return kind;
  }

  public void setKind(String kind) {
    this.kind = kind;
  }

  public boolean isReadOnly() {
    return readOnly;
  }

  public void setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
  }

}
