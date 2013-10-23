package com.jetbrains.youtrack.javarest.client;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.sun.jersey.api.client.ClientResponse;

@XmlRootElement(name = "issue")
public class YouTrackIssue {

  private String id;

  @XmlElement(name = "field")
  public LinkedList<IssueSchemaField> fields;

  private LinkedList<YouTrackComment> comments;

  private LinkedList<IssueLink> links;

  private Map<String, YouTrackCustomField> customFields;

  private Map<String, String> singleFields;

  @XmlElement(name = "tag")
  private LinkedList<String> tags;

  private Map<String, Object> properties = new HashMap<String, Object>();

  public YouTrackIssue(String newId) {
    this.setId(newId);
  }

  public YouTrackIssue() {
    setId(null);
    setField(new LinkedList<IssueSchemaField>());
    setComment(new LinkedList<YouTrackComment>());
    setLinks(new LinkedList<IssueLink>());
    setCustomFields(new HashMap<String, YouTrackCustomField>());
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
    this.fields = field;
  }

  public void setComment(LinkedList<YouTrackComment> comments) {
    this.setComments(comments);
  }

  public Object property(String property) {
    if (properties.size() == 0) {
      this.mapProperties();
    }
    if (properties.containsKey(property)) {
      return properties.get(property);
    } else {
      return "";
    }
  }

  public void mapProperties() {
    if (fields.size() > 0) {
      for (IssueSchemaField f : fields) {
        if (f.getValues().size() == 1) {
          properties.put(f.getName(), f.getValues().get(0).getValue());
        } else {
          properties.put(f.getName(), f.getStringValues());
        }
      }
    }
  }

  public void mapFields() {
    if (fields.size() > 0) {
      for (IssueSchemaField field : fields) {
        if (field.getType().equals(IssueSchemaField.TYPE_LINK_FIELD)) {
          for (IssueSchemaValue linkValue : field.getValues()) {
            IssueLink link = new IssueLink();
            link.setRole(linkValue.getRole());
            link.setType(linkValue.getType());
            link.setValue(linkValue.getValue());
            addLink(link);
          }
        } else if (field.getType().equals(IssueSchemaField.TYPE_CUSTOM_FIELD)) {
          addCustomField(field.getName(), null);
        } else if (field.getType().equals(IssueSchemaField.TYPE_SINGLE_FIELD)) {
          addSingleField(field.getName(), field.getValues().get(0).getValue());
        }
      }
    }
  }

  /*
   * Add new property, not update
   */
  public void addProperty(String newProperty, Object value) {
    if (newProperty != null && !properties.containsKey(newProperty)) {
      properties.put(newProperty, value);
    }
  }

  public void updateProperty(String property, Object newValue) {
    if (properties.containsKey(property)) {
      properties.put(property, newValue);
    }
  }

  public Map<String, Object> getProperties() {
    if (properties.size() == 0) {
      this.mapProperties();
    }
    return this.properties;
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

  public String getProjectName() {
    if (properties.size() == 0) {
      this.mapProperties();
    }
    if (properties.size() > 0 && properties.containsKey("projectShortName")
        && properties.get("projectShortName") != null) {
      return this.property("projectShortName").toString();
    } else {
      return null;
    }
  }

  public String getSummary() {
    if (properties.size() == 0) {
      this.mapProperties();
    }
    if (properties.size() > 0 && properties.containsKey("summary")
        && properties.get("summary") != null) {
      return this.property("summary").toString();
    } else {
      return null;
    }
  }

  public String getDescription() {
    if (properties.size() == 0) {
      this.mapProperties();
    }
    if (properties.size() > 0 && properties.containsKey("description")
        && properties.get("description") != null) {
      return this.property("description").toString();
    } else {
      return null;
    }
  }

  public LinkedList<String> getTags() {
    return tags;
  }

  public void addTag(String tag) {
    if (tags != null) {
      tags.add(tag);
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

  public Map<String, YouTrackCustomField> getCustomFields() {
    return customFields != null ? customFields : new HashMap<String, YouTrackCustomField>();
  }

  public void addCustomField(String name, YouTrackCustomField field) {
    if (name != null) {
      getCustomFields().put(name, field);
    } else if (field != null && field.getName() != null) {
      getCustomFields().put(field.getName(), field);
    }
  }

  public void setCustomFields(Map<String, YouTrackCustomField> customFields) {
    this.customFields = customFields;
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

}
