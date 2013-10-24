/**
 * @author: amarch
 */

package com.jetbrains.youtrack.javarest.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.Test;

import com.jetbrains.youtrack.javarest.client.YouTrackIssue;

public class CorrectJaxbBinding {

  @Test
  public void JaxbIssueUnmarshalling() {
    try {

      File file = new File("tests/com/jetbrains/youtrack/javarest/tests/test_issue.xml");
      JAXBContext jaxbContext = JAXBContext.newInstance(YouTrackIssue.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      YouTrackIssue issue = (YouTrackIssue) jaxbUnmarshaller.unmarshal(file);
      assertEquals("1-10", issue.getId());
      issue.mapFields();
      assertEquals("Mylyn Concepts and Usage", issue.getSingleField("summary"));
      assertEquals(1366278449653L, Long.parseLong(issue.getSingleField("created")));
      assertEquals("root", issue.getComments().get(0).getAuthorName());
    } catch (JAXBException e) {
      e.printStackTrace();
      fail("JAXB issue not created");
    }
  }


}
