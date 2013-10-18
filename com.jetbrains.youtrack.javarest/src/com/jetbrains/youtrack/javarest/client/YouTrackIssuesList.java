/**
@author: amarch
 */

package com.jetbrains.youtrack.javarest.client;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "issues")
public class YouTrackIssuesList {

    private List<YouTrackIssue> issues = new LinkedList<YouTrackIssue>();

    public YouTrackIssuesList() {
    }

    public YouTrackIssuesList(List<YouTrackIssue> issues) {
	this.issues = issues;
    }

    @XmlElement(name = "issue", type = YouTrackIssue.class)
    public List<YouTrackIssue> getIssues() {
	return issues;
    }

    public void setIssues(List<YouTrackIssue> Issues) {
	this.issues = Issues;
    }

}
