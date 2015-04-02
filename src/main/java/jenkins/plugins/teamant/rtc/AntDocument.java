package jenkins.plugins.teamant.rtc;

import java.io.IOException;
import java.lang.reflect.Field;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import jenkins.plugins.teamant.rtc.exceptions.RTCConflictAttrException;
import jenkins.plugins.teamant.rtc.exceptions.RTCDependentAttrException;
import jenkins.plugins.teamant.rtc.exceptions.RTCMissingAttrException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Represents a single instance of an Ant document (XML). Every Ant script
 * should instantiate its own AntDocument.
 * 
 * @author rar6si
 * 
 */
public class AntDocument {

    /**
     * XML Document structure.
     */
    Document doc;
    /**
     * Target node within the XML document where the Ant Tasks are written
     * dynamically.
     */
    Node target;

    /**
     * Default constructor. Instantiate the DOM Document.
     * 
     * @throws ParserConfigurationException
     *             Error parsing content.
     */
    public AntDocument() throws ParserConfigurationException {

	// Get instance of XML Document
	generateDocument();

	// Create headers and footers for the Ant script (project name, target
	// and so on)
	createDocumentStructure();
    }

    /**
     * Invoke the Document factory to provide a new instance of an XML Document
     * to the AntDocument.
     * 
     * @throws ParserConfigurationException
     *             Error parsing the content.
     */
    private void generateDocument() throws ParserConfigurationException {
	// Get instance of document builder factory.
	DocumentBuilderFactory docFactory = DocumentBuilderFactory
		.newInstance();
	// Get a new document builder.
	DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
	this.doc = docBuilder.newDocument();
    }

    /**
     * Create the skeleton of a Ant XML script, with a project name and target.
     */
    private void createDocumentStructure() {

	// Add the root tag project
	Element rootElement = this.doc.createElement("project");
	rootElement.setAttribute("default", "execTasks");
	rootElement.setAttribute("name", "rtc-ant-tasks");
	rootElement.setAttribute("basedir", ".");
	this.doc.appendChild(rootElement);

	// Add the target tag
	this.target = this.doc.createElement("target");
	((Element)this.target).setAttribute("name", "execTasks");
	rootElement.appendChild(target);

    }

    /**
     * Add a task to the AntDocument target. It first evaluates if the content
     * is compliant with the respective Ant task requirements (i.e. required
     * fields), then serializes the task into an XML node, adds it to the target
     * and adds the respective Ant Task dependency if necessary.
     * 
     * @param task
     *            Ant Task bean.
     * @throws RTCMissingAttrException
     *             Ant Task bean missing field.
     * @throws IllegalAccessException
     *             Illegal Access exception.
     * @throws ParserConfigurationException
     *             Error parsing the content.
     * @throws RTCConflictAttrException
     *             Ant Task bean conflicting fields.
     * @throws RTCDependentAttrException
     *             Ant Task bean missing dependent field.
     */
    public void addNode(BaseTask task) throws RTCMissingAttrException,
	    IllegalAccessException, ParserConfigurationException,
	    RTCConflictAttrException, RTCDependentAttrException {

	// Evaluate attributes of the task
	task.eval();
	
	// Add Echo message
	addEcho("Executing " + task.getTaskDefName());

	// Serialize it into XML
	Element elemTask = serialize(task);

	// Add it to the target tag of the XML doc
	this.target.appendChild(elemTask);

	// Check if the task dependency is already added to the document
	NodeList taskClasses = this.doc.getElementsByTagName("taskdef");
	boolean exists = false;
	for (int i = 0; i < taskClasses.getLength(); i++) {
	    Node taskClass = taskClasses.item(i);
	    if (taskClass.getNodeType() == Node.ELEMENT_NODE) {
		if (((Element) taskClass).getAttribute("name").equals(
			task.getTaskDefName())) {
		    exists = true;
		    break;
		}
	    }
	}

	// Add task dependency if it does not exist
	if (!exists) {
	    Element elemTaskDep = serializeTaskDependency(task);
	    this.doc.getDocumentElement().appendChild(elemTaskDep);
	}
    }
    
    /**
     * Add an <echo/> tag in the Ant XML Script. Note: A escaping backslash "\"
     * may be required dependending on the content passed to this message.
     * 
     * @param message
     *            Message to be echoed in the Ant XML.
     */
    public void addEcho(String message) {
	
	Element echo = this.doc.createElement("echo");
	echo.setAttribute("message", message);
	
	// Add it to the target tag of the XML doc
	this.target.appendChild(echo);
	
    }

    /**
     * Converts the task dependency provided by the Ant Task bean into an XML
     * node.
     * 
     * @param task
     *            Ant Task bean.
     * @return XML Node with the Ant Task dependency.
     */
    private Element serializeTaskDependency(BaseTask task) {

	Element elemTaskDep = this.doc.createElement("taskdef");
	elemTaskDep.setAttribute("name", task.getTaskDefName());
	elemTaskDep.setAttribute("classname", task.getTaskDefClassname());

	return elemTaskDep;
    }

    /**
     * Serialize the Ant Task bean into an XML element.
     * 
     * @return XML Node of the task
     * @throws RTCMissingAttrException
     *             Ant Task bean missing field.
     * @throws IllegalAccessException
     *             Illegal Access exception.
     * @throws ParserConfigurationException
     *             Error parsing the content.
     */
    private Element serialize(BaseTask task) throws IllegalAccessException,
	    ParserConfigurationException {

	// Create startBuildActivity element
	Element elemTask = this.doc.createElement(task.getTaskDefName());

	// Add fields as attributes using reflection
	Field[] fields = task.getClass().getDeclaredFields();

	for (Field field : fields) {
	    // Make private field accessible...
	    field.setAccessible(true);
	    if (field.get(task) != null)
		elemTask.setAttribute(field.getName(),
			String.valueOf(field.get(task)));
	}

	return elemTask;
    }

    /**
     * Write the XML Document to an OutputStream (i.e. System.out)
     * 
     * @param result
     *            StreamResult where the XML Document will be written.
     * @throws IOException
     *             Error writing to the OutputStream.
     * @throws TransformerException
     *             Error converting the XML Document to the XML format.
     */
    public void writeDocument(StreamResult result) throws IOException,
	    TransformerException {
	TransformerFactory tf = TransformerFactory.newInstance();
	Transformer transformer = tf.newTransformer();
	transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
	transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	transformer.setOutputProperty(
		"{http://xml.apache.org/xslt}indent-amount", "4");

	transformer.transform(new DOMSource(this.doc), result);
    }
}
