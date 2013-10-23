/**
@author: amarch
*/

package com.jetbrains.youtrack.javarest.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.Test;

import com.jetbrains.youtrack.javarest.client.IssueSchemaField;
import com.jetbrains.youtrack.javarest.client.YouTrackIssue;

public class CorrectIssueJaxbBinding {
	
	@Test
	public void JaxbIssueUnmarshalling(){
		try {
			 
			File file = new File("tests/com/jetbrains/youtrack/javarest/tests/test_issue.xml");
			JAXBContext jaxbContext = JAXBContext.newInstance(YouTrackIssue.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			YouTrackIssue issue = (YouTrackIssue) jaxbUnmarshaller.unmarshal(file);
			assertEquals("1-10", issue.getId());
			issue.mapProperties();
			assertEquals("Mylyn Concepts and Usage", issue.property("summary").toString());
			assertEquals(1366278449653L, Long.parseLong(issue.property("created").toString()));
			assertEquals("root", issue.getComments().get(0).getAuthorName());
			for(IssueSchemaField field : issue.fields){
				if(field.getName().equals("summary")){
					assertEquals("SingleField", field.getType());
				}
				if(field.getName().equals("Type")){
					assertEquals("CustomField", field.getType());
				}
			}
		  } catch (JAXBException e) {
			e.printStackTrace();
			fail("JAXB issue not created");
		  }
	}
	

}
