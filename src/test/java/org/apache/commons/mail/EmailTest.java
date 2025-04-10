package org.apache.commons.mail;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import javax.mail.MessagingException;
import javax.mail.Session;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EmailTest {

	private static final String[] TEST_EMAILS = { "ab@ab.com","a.b@c.org", 
			"asdfghjklpzx@cvbnm.com.bd"};
			
	/* Concrete Email class for testing*/
	private EmailConcrete email;
	
	@Before
	public void setUpEmailTest() throws Exception{
		email = new EmailConcrete();
		email.setMailSession(Session.getDefaultInstance(new Properties())); // Use Properties instead of HashMap
	}
	
	@After
	public void tearDownEmailTest() throws Exception{
		
	}
	
	@Test
	public void testAddBcc() throws Exception {
		email.addBcc(TEST_EMAILS);
		
		assertEquals(3, email.getBccAddresses().size());
	}
	
	@Test
    public void testAddCc() throws EmailException {
        // Arrange
        String ccEmail = "cc@example.com";

        // Act
        Email result = email.addCc(ccEmail);

        // Assert
        assertNotNull("The returned Email object should not be null", result);
    }
	
    @Test
    public void testAddReplyTo_ValidEmailAndName() throws EmailException {
        // Arrange
        String emailAddress = "reply@example.com";
        String name = "John Doe";

        // Act
        Email result = email.addReplyTo(emailAddress, name);

        // Assert
        assertNotNull("The returned Email object should not be null", result);
    }
    
    @Test
    public void testSetFrom_ValidEmail() throws EmailException {
        // Arrange
        String fromEmail = "from@example.com";

        // Act
        Email result = email.setFrom(fromEmail);

        // Assert
        assertNotNull("The returned Email object should not be null", result);
        assertEquals("The from email should be set correctly", fromEmail, email.getFromAddress().getAddress());
    }
    
    @Test
    public void testAddHeader_ValidNameAndValue() {
        // Arrange
        String name = "X-Custom-Header";
        String value = "CustomValue";

        // Act
        email.addHeader(name, value);

        // Assert
        assertTrue("Header should be added to the map", email.getHeaders().containsKey(name));
        assertEquals("Header value should match", value, email.getHeaders().get(name));
    }

    @Test
    public void testAddHeader_NullName() {
        // Arrange
        String name = null;
        String value = "CustomValue";

        // Act & Assert
        try {
            email.addHeader(name, value);
            fail("Expected IllegalArgumentException to be thrown for null name");
        } catch (IllegalArgumentException e) {
            assertEquals("name can not be null or empty", e.getMessage());
        }
    }
    
    @Test
    public void testBuildMimeMessage_FirstCall() throws EmailException {
        // Arrange
        email.setFrom("from@example.com");
        email.addTo("to@example.com");
        email.setSubject("Test Subject");
        email.setContent("Test Content", "text/plain");

        // Act
        email.buildMimeMessage();

        // Assert
        assertNotNull("MimeMessage should be built", email.getMimeMessage());
    }

    @Test(expected = IllegalStateException.class)
    public void testBuildMimeMessage_SecondCall() throws EmailException {
        // Arrange
        email.setFrom("from@example.com");
        email.addTo("to@example.com");
        email.setSubject("Test Subject");
        email.setContent("Test Content", "text/plain");

        // Act
        email.buildMimeMessage();
        email.buildMimeMessage(); // Second call should throw IllegalStateException

        // Assert is handled by the expected exception
    }

    @Test
    public void testBuildMimeMessage_MissingFromAddress() {
        // Arrange
        try {
			email.addTo("to@example.com");
		} catch (EmailException e1) {
			
			e1.printStackTrace();
		}
        email.setSubject("Test Subject");
        email.setContent("Test Content", "text/plain");

        // Act & Assert
        try {
            email.buildMimeMessage();
            fail("Expected EmailException to be thrown for missing from address");
        } catch (EmailException e) {
            assertEquals("From address required", e.getMessage());
        }
    }

    @Test
    public void testBuildMimeMessage_MissingRecipients() {
        // Arrange
        try {
			email.setFrom("from@example.com");
		} catch (EmailException e1) {
			
			e1.printStackTrace();
		}
        email.setSubject("Test Subject");
        email.setContent("Test Content", "text/plain");

        // Act & Assert
        try {
            email.buildMimeMessage();
            fail("Expected EmailException to be thrown for missing recipients");
        } catch (EmailException e) {
            assertEquals("At least one receiver address required", e.getMessage());
        }
    }

    @Test
    public void testBuildMimeMessage_SubjectAndContent() throws EmailException {
        // Arrange
        email.setFrom("from@example.com");
        email.addTo("to@example.com");
        String subject = "Test Subject";
        String content = "Test Content";
        email.setSubject(subject);
        email.setContent(content, "text/plain");

        // Act
        email.buildMimeMessage();

        // Assert
        try {
			assertEquals("Subject should match", subject, email.getMimeMessage().getSubject());
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        try {
			assertTrue("Content should match", email.getMimeMessage().getContent().toString().contains(content));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    @Test
    public void testBuildMimeMessage_Headers() throws EmailException {
        // Arrange
        email.setFrom("from@example.com");
        email.addTo("to@example.com");
        email.setSubject("Test Subject");
        email.setContent("Test Content", "text/plain");

        String headerName = "X-Custom-Header";
        String headerValue = "CustomValue";
        email.addHeader(headerName, headerValue);

        // Act
        email.buildMimeMessage();

        // Assert
        String[] headers = null;
		try {
			headers = email.getMimeMessage().getHeader(headerName);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        assertNotNull("Header should be added", headers);
        assertEquals("Header value should match", headerValue, headers[0]);
    }

    /*@Test
    public void testBuildMimeMessage_PopBeforeSmtp() throws EmailException {
        GreenMail greenMail = new GreenMail(ServerSetup.POP3);
    greenMail.start();
        
    try {
        // Arrange
        Email email = new SimpleEmail();
        email.setHostName("localhost");
        email.setPopBeforeSmtp(true, "localhost", "username", "password");
        email.buildMimeMessage();
        email.setPopBeforeSmtp(true, "pop.example.com", "username", "password");

        // Act
        email.buildMimeMessage();

        // Assert
        // assertNotNull(email.getMimeMessage());
    } finally {
        greenMail.stop();
    }
    }*/
    
    @Test
    public void testGetHostName_SessionIsNotNullAndMailHostIsSet() {
        // Arrange
        Properties props = new Properties();
        props.put(EmailConstants.MAIL_HOST, "smtp.example.com"); // Set MAIL_HOST in properties
        Session session = Session.getInstance(props); // Use getInstance instead of getDefaultInstance
        email.setMailSession(session); // Set session with MAIL_HOST

        // Act
        String hostName = email.getHostName();

        // Assert
        assertEquals("Host name should match MAIL_HOST when session is not null and MAIL_HOST is set", "smtp.example.com", hostName);
    }
    
    @Test
    public void testGetHostName() {
        EmailConcrete defaultEmail = new EmailConcrete();
        
        // Arrange
        assertNull("Default host name should be null", defaultEmail.getHostName());

        // Act
        defaultEmail.setHostName("custom.host.name.com");

        // Assert
        assertEquals("Host name should be the value set", "custom.host.name.com", defaultEmail.getHostName());
    }
    
    @Test
    public void testGetSentDate_SentDateIsNull() {
        // Arrange
        EmailConcrete email = new EmailConcrete();
        email.setSentDate(null); // Explicitly set sentDate to null

        // Act
        Date result = email.getSentDate();

        // Assert
        assertNotNull("Returned date should not be null", result); 
              
    }
 
    @Test
    public void testGetSentDate_SentDateIsNotNull() {
        // Arrange
        EmailConcrete email = new EmailConcrete();
        Date sentDate = new Date(1672531200000L); // Set a specific date (January 1, 2023)
        email.setSentDate(sentDate);

        // Act
        Date result = email.getSentDate();

        // Assert
        assertNotNull("Returned date should not be null", result);
        assertEquals("Returned date should match the sentDate", sentDate.getTime(), result.getTime());
    }

    @Test
    public void testGetSocketConnectionTimeout_DefaultValue() {
        // Arrange
        // No explicit setting of socketConnectionTimeout

        // Act
        int timeout = email.getSocketConnectionTimeout();

        // Assert
        assertEquals("Default socket connection timeout is 60000", 60000, timeout);
    }

}
