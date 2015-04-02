package jenkins.plugins.teamant.build.rtcbuildactivity;

import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;

import jenkins.plugins.teamant.rtc.AntDocument;
import jenkins.plugins.teamant.rtc.exceptions.RTCConflictAttrException;
import jenkins.plugins.teamant.rtc.exceptions.RTCDependentAttrException;
import jenkins.plugins.teamant.rtc.exceptions.RTCMissingAttrException;
import jenkins.plugins.teamant.rtc.tasks.CompleteBuildActivityTask;
import jenkins.plugins.teamant.rtc.tasks.StartBuildActivityTask;

import org.w3c.dom.DOMException;

/**
 * @author rar6si
 *
 */
public class Main {

    /**
     * @param args arguments
     */
    public static void main(String[] args) {


	StartBuildActivityTask test = new StartBuildActivityTask();
	test.setBuildResultUUID("bla");
	test.setRepositoryAddress("blu");
	test.setUserId("rezende");
	test.setLabel("test");
	test.setPassword("passs");
	
	StartBuildActivityTask test2 = new StartBuildActivityTask();
	test2.setBuildResultUUID("bli");
	test2.setRepositoryAddress("ble");
	test2.setUserId("rezende");
	test2.setLabel("test2");
	test2.setPasswordFile("passssss");
	test2.setVerbose("true");
	
	CompleteBuildActivityTask complt1 = new CompleteBuildActivityTask();
	complt1.setActivityId("activ");
	complt1.setBuildResultUUID("build1");
	complt1.setRepositoryAddress("repo1");
	complt1.setUserId("rezende");
	complt1.setPassword("asdasd");
	
	System.out.println(test);
	System.out.println(test2);
	System.out.println(complt1);
	
	
	try {
	    AntDocument antDoc = new AntDocument();
	    antDoc.addNode(test);
	    antDoc.addNode(complt1);
	    antDoc.addNode(test2);
	    
	    antDoc.writeDocument(new StreamResult(
			new OutputStreamWriter(System.out, "UTF-8")));
	} catch (ParserConfigurationException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (IllegalArgumentException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (IllegalAccessException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (DOMException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (TransformerException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (RTCMissingAttrException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (RTCConflictAttrException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (RTCDependentAttrException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

}
