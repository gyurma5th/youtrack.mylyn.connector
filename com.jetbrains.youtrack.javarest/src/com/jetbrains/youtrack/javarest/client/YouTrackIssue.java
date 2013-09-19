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
	public LinkedList<Field> fields;
	
	private LinkedList<YouTrackComment> comments;

//	@XmlTransient
	private Map<String, Object> properties = new HashMap<String, Object>();

	public YouTrackIssue(String newId) {
		this.setId(newId);
		setField(new LinkedList<Field>());
		setComment(new LinkedList<YouTrackComment>());
	}

	public YouTrackIssue() {
		setId(null);
		setField(new LinkedList<Field>());
		setComment(new LinkedList<YouTrackComment>());
	}

	public void setId(String id) {
		this.id = id;
	}

	@XmlAttribute
	public String getId() {
		return id;
	}

	public void setField(LinkedList<Field> field) {
		this.fields = field;
	}
	
	public void setComment(LinkedList<YouTrackComment> comments) {
		this.setComments(comments);
	}

	public Object property(String property) {
		if(properties.size() == 0){
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
			for (Field f : fields) {
				if(f.getValues().size() == 1) {
					properties.put(f.getName(), f.getValues().get(0));
				} else{
					properties.put(f.getName(), f.getValues());
				}
			}
		}
	}

	/*
	 * Add new property, not update
	 */
	public void addProperty(String newProperty, String value){
		if(newProperty != null && !properties.containsKey(newProperty)){
			properties.put(newProperty, (Object) value);
		}
	}
	
	public void updateProperty(String property, String newValue){
		if(properties.containsKey(property)){
			properties.put(property, newValue);
		}
	}
	
	public Map<String, Object> getProperties(){
		if(properties.size() == 0){
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
	
	public static String getIdFromUrl(String issueURL){
		return issueURL.substring(issueURL.lastIndexOf("/")+1);
	}
	
	public static String getIdFromResponse(ClientResponse response){
		return getIdFromUrl(response.getHeaders().get("Location").get(0));
	}
	
	public String getProjectName(){
		if(properties.size() == 0){
			this.mapProperties();
		}
		if(properties.size() > 0 && properties.containsKey("projectShortName") && properties.get("projectShortName") != null){
			return this.property("projectShortName").toString();
		} else {
			return null;
		}
	}
	
	public String getSummary(){
		if(properties.size() == 0){
			this.mapProperties();
		}
		if(properties.size() > 0 && properties.containsKey("summary") && properties.get("summary") != null){
			return this.property("summary").toString();
		} else {
			return null;
		}
	}
	
	public String getDescription(){
		if(properties.size() == 0){
			this.mapProperties();
		}
		if(properties.size() > 0 && properties.containsKey("description") && properties.get("description") != null){
			return this.property("description").toString();
		} else {
			return null;
		}
	}
	
}
