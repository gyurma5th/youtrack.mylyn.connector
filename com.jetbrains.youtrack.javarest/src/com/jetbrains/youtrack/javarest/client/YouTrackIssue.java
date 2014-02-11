package com.jetbrains.youtrack.javarest.client;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.core.runtime.CoreException;

import com.sun.jersey.api.client.ClientResponse;

/**
 * @author Alexander Marchuk
 * 
 */
@XmlRootElement(name = "issue")
public class YouTrackIssue {

  public static final String PROJECT_NAME_FIELD = "projectShortName";

  public static final String PROJECT_SUMMARY_FIELD = "summary";

  public static final String PROJECT_DESCRIPTION_FIELD = "description";

  private String id;

  private LinkedList<IssueSchemaField> fields;

  private LinkedList<YouTrackComment> comments;

  private LinkedList<IssueTag> tags;

  private LinkedList<IssueLink> links;

  private HashMap<String, YouTrackCustomField> customFieldsInfo;

  private HashMap<String, LinkedList<String>> customFieldsValues;

  private Map<String, String> singleFields;

  private boolean mapped = false;

  public YouTrackIssue(String newId) {
    this.setId(newId);
  }

  public YouTrackIssue() {
    setId(null);
    setField(new LinkedList<IssueSchemaField>());
    setComment(new LinkedList<YouTrackComment>());
    setTags(new LinkedList<IssueTag>());
    setLinks(new LinkedList<IssueLink>());
    setCustomFieldsInfo(new HashMap<String, YouTrackCustomField>());
    setCustomFieldsValues(new HashMap<String, LinkedList<String>>());
    setSingleFields(new HashMap<String, String>());
  }

  public void setId(String id) {
    this.id = id;
  }

  @XmlAttribute
  public String getId() {
    return id;
  }

  public void setField(LinkedList<IssueSchemaField> field) {
    this.setFields(field);
  }

  public void setComment(LinkedList<YouTrackComment> comments) {
    this.setComments(comments);
  }

  public void mapFields() {
    if (!mapped) {
      if (getFields().size() > 0) {
        for (IssueSchemaField field : getFields()) {
          if (field.getType().equals(IssueSchemaField.TYPE_LINK_FIELD)) {
            for (IssueSchemaValue linkValue : field.getValues()) {
              IssueLink link = new IssueLink();
              link.setRole(linkValue.getRole());
              link.setType(linkValue.getType());
              link.setValue(linkValue.getValue());
              addLink(link);
            }
          } else if (field.getType().equals(IssueSchemaField.TYPE_CUSTOM_FIELD)
              || field.getType().equals(IssueSchemaField.TYPE_CUSTOM_FIELD_SINCE_5_1)) {
            addCustomField(field.getName(), field.getStringValues(), null);
          } else if (field.getType().equals(IssueSchemaField.TYPE_SINGLE_FIELD)) {
            addSingleField(field.getName(), field.getStringValues().get(0));
          } else if (field.getType().equals(IssueSchemaField.TYPE_MULTIUSER_FIELD)) {
            LinkedList<String> multiuserValues = new LinkedList<String>();
            for (IssueSchemaValue value : field.getValues()) {
              multiuserValues.add(getMultiuserValue(value));
            }
            addCustomField(field.getName(), multiuserValues, null);
          }
        }
      }
      mapped = true;
    }
  }

  private String getMultiuserValue(IssueSchemaValue multiuserValue) {
    return multiuserValue.getFullName() + " (" + multiuserValue.getValue() + ")";
  }

  public static String getLoginFromMultiuserValue(String value) {
    try {
      if (value.lastIndexOf(" (") == -1) {
        return value;
      } else {
        return value.substring(value.lastIndexOf(" (") + 2, value.length() - 1);
      }
    } catch (IndexOutOfBoundsException e) {
      return value;
    }
  }

  public static String getFullnameFromMultiuserValue(String value) {
    return value.substring(0, value.lastIndexOf(" "));
  }

  public void fillCustomFieldsFromProject(YouTrackProject project, YouTrackClient client)
      throws CoreException {
    if (!project.isCustomFieldsUpdated()) {
      project.updateCustomFields(client);
    }
    for (YouTrackCustomField field : project.getCustomFields()) {
      getCustomFieldsInfo().put(field.getName(), field);
    }
  }

  @XmlElement(name = "comment")
  public LinkedList<YouTrackComment> getComments() {
    return comments;
  }

  public void setComments(LinkedList<YouTrackComment> comments) {
    this.comments = comments;
  }

  public static String getIdFromUrl(String issueURL) {
    return issueURL.substring(issueURL.lastIndexOf("/") + 1);
  }

  public static String getIdFromResponse(ClientResponse response) {
    return getIdFromUrl(response.getHeaders().get("Location").get(0));
  }

  public String getSingleField(String name) {
    if (singleFields.size() == 0) {
      this.mapFields();
    }
    if (singleFields.size() > 0 && singleFields.containsKey(name) && singleFields.get(name) != null) {
      return this.singleFields.get(name);
    } else {
      return null;
    }
  }

  public String getProjectName() {
    return getSingleField(PROJECT_NAME_FIELD);
  }

  public String getSummary() {
    return getSingleField(PROJECT_SUMMARY_FIELD);
  }

  public String getDescription() {
    return getSingleField(PROJECT_DESCRIPTION_FIELD) == null
        ? ""
        : getSingleField(PROJECT_DESCRIPTION_FIELD);
  }

  public void addTag(String tag) {
    if (getTags() != null) {
      IssueTag additionTag = new IssueTag();
      additionTag.setText(tag);
      getTags().add(additionTag);
    }
  }

  public LinkedList<IssueLink> getLinks() {
    return links != null ? links : new LinkedList<IssueLink>();
  }

  public void addLink(IssueLink link) {
    if (link != null) {
      getLinks().add(link);
    }
  }

  public void setLinks(LinkedList<IssueLink> links) {
    this.links = links;
  }

  public void addCustomField(String name, LinkedList<String> values, YouTrackCustomField field) {
    if (name != null) {
      getCustomFieldsInfo().put(name, field);
      getCustomFieldsValues().put(name, values);
    } else if (field != null && field.getName() != null) {
      getCustomFieldsInfo().put(field.getName(), field);
      getCustomFieldsValues().put(field.getName(), values);
    }
  }

  public void addCustomFieldValue(String name, LinkedList<String> values) {
    if (name != null) {
      getCustomFieldsValues().put(name, values);
      if (!getCustomFieldsInfo().containsKey(name)) {
        getCustomFieldsInfo().put(name, null);
      }
    }
  }

  public void addCustomFieldValue(String name, String value) {
    if (name != null) {
      LinkedList<String> values = new LinkedList<String>();
      values.add(value);
      getCustomFieldsValues().put(name, values);
      if (!getCustomFieldsInfo().containsKey(name)) {
        getCustomFieldsInfo().put(name, null);
      }
    }
  }

  public Map<String, String> getSingleFields() {
    return singleFields != null ? singleFields : new HashMap<String, String>();
  }

  public void addSingleField(String name, String value) {
    if (name != null) {
      getSingleFields().put(name, value);
    }
  }

  public void setSingleFields(Map<String, String> singleFields) {
    this.singleFields = singleFields;
  }

  public LinkedList<String> getCustomFieldValue(String name) {
    if (customFieldsValues.size() == 0) {
      this.mapFields();
    }
    if (customFieldsValues.size() > 0 && customFieldsValues.containsKey(name)
        && customFieldsValues.get(name) != null) {
      return customFieldsValues.get(name);
    } else {
      return null;
    }
  }

  public boolean isCustomFieldSingle(String name) {
    return customFieldsValues.containsKey(name) && customFieldsValues.get(name).size() == 1;
  }

  public String getSingleCustomFieldValue(String name) {
    if (customFieldsValues.size() == 0) {
      this.mapFields();
    }
    if (customFieldsValues != null && isCustomFieldSingle(name)
        && customFieldsValues.get(name) != null) {
      return customFieldsValues.get(name).get(0);
    } else {
      return null;
    }
  }

  public HashMap<String, YouTrackCustomField> getCustomFieldsInfo() {
    return customFieldsInfo != null ? customFieldsInfo : new HashMap<String, YouTrackCustomField>();
  }

  public YouTrackCustomField getCustomFieldInfo(String name) {
    return customFieldsInfo.containsKey(name) ? customFieldsInfo.get(name) : null;
  }

  public void setCustomFieldsInfo(HashMap<String, YouTrackCustomField> customFieldsInfo) {
    this.customFieldsInfo = customFieldsInfo;
  }

  public HashMap<String, LinkedList<String>> getCustomFieldsValues() {
    return customFieldsValues != null
        ? customFieldsValues
        : new HashMap<String, LinkedList<String>>();
  }

  public void setCustomFieldsValues(HashMap<String, LinkedList<String>> customFieldsValues) {
    this.customFieldsValues = customFieldsValues;
  }

  public boolean isCustomFieldsDataConsistent() {
    if (customFieldsInfo != null && customFieldsValues != null
        && customFieldsInfo.size() == customFieldsValues.size()) {
      for (String name : customFieldsValues.keySet()) {
        if (!customFieldsInfo.containsKey(name) || customFieldsInfo.get(name) == null) {
          return false;
        }
      }
      return true;
    } else {
      return false;
    }
  }

  public boolean isCustomFieldsDataConsistent(String name) {
    return customFieldsInfo != null && customFieldsValues != null
        && customFieldsInfo.containsKey(name) && customFieldsInfo.get(name) != null;
  }

  @XmlElement(name = "tag")
  public LinkedList<IssueTag> getTags() {
    return tags;
  }

  public LinkedList<String> getStringTags() {
    LinkedList<String> result = new LinkedList<String>();
    for (IssueTag tag : tags) {
      result.add(tag.getText());
    }
    return result;
  }

  public void setTags(LinkedList<IssueTag> tag) {
    this.tags = tag;
  }

  @XmlElement(name = "field")
  public LinkedList<IssueSchemaField> getFields() {
    return fields;
  }

  public void setFields(LinkedList<IssueSchemaField> fields) {
    this.fields = fields;
  }

}
