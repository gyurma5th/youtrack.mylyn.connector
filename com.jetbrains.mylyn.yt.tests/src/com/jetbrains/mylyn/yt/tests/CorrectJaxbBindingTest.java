package com.jetbrains.mylyn.yt.tests;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import junit.framework.TestCase;

import com.jetbrains.youtrack.javarest.client.IssueSchemaField;
import com.jetbrains.youtrack.javarest.client.YouTrackIssue;

public class CorrectJaxbBindingTest extends TestCase {

  public void testJaxbIssueUnmarshallingTest() throws Exception {
    try {
      File file = new File("src/com/jetbrains/mylyn/yt/tests/test_issue.xml");
      JAXBContext jaxbContext = JAXBContext.newInstance(YouTrackIssue.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      YouTrackIssue issue = (YouTrackIssue) jaxbUnmarshaller.unmarshal(file);
      assertEquals("1-10", issue.getId());
      issue.mapFields();
      assertEquals("Mylyn Concepts and Usage", issue.getSingleField("summary"));
      assertEquals(1366278449653L, Long.parseLong(issue.getSingleField("created")));
      assertEquals("root", issue.getComments().get(0).getAuthorName());
      assertEquals(4, issue.getTags().size());
      for (IssueSchemaField field : issue.getFields()) {
        if (field.getName().equals("summary")) {
          assertEquals("SingleField", field.getType());
        }
        if (field.getName().equals("Type")) {
          assertEquals("CustomField", field.getType());
        }
      }
    } catch (JAXBException e) {
      e.printStackTrace();
      fail("JAXB issue not created");
    }
  }
}
