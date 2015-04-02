package jenkins.plugins.teamant.rtc;

import java.lang.reflect.Field;

import jenkins.plugins.teamant.rtc.exceptions.RTCConflictAttrException;
import jenkins.plugins.teamant.rtc.exceptions.RTCDependentAttrException;
import jenkins.plugins.teamant.rtc.exceptions.RTCMissingAttrException;


/**
 * Base task bean. Every Ant Task should implement the contained methods below.
 * The attributes of the implemented Ant Tasks should match exactly the ones
 * from the IBM Ant Task documentation. The exactiness is required because of
 * the reflection used to convert it to XML format.
 * 
 * @author rar6si
 * 
 */
public abstract class BaseTask {
    
    /**
     * Get the respective IBM Ant Task tag (i.e. startBuildActivity).
     * 
     * @return IBM Ant Task tag.
     */
    public abstract String getTaskDefName();
    
    /**
     * Get the respective IBM Ant Task classname (i.e.
     * com.ibm.team.build.ant.task.StartBuildActivityTask).
     * 
     * @return IBM Ant Task classname.
     */
    public abstract String getTaskDefClassname();
    
    /**
     * Evaluates the provided fields according to IBM Ant task rules. It checks
     * for required fields and for existence of fields that shouldn't coexist
     * (i.e. password and passwordFile).
     * 
     * @throws RTCMissingAttrException When one of the attributes is missing.
     * @throws RTCConflictAttrException When some of the attributes are conflicting.
     * @throws RTCDependentAttrException  When a provided attribute requires a missing one.
     */
    public abstract void eval() throws RTCMissingAttrException, RTCConflictAttrException, RTCDependentAttrException;

    /** 
     * {@inheritDoc}
     */
    @Override
    public String toString() {
	
	StringBuilder sbuild = new StringBuilder();

	// Add fields as attributes using reflection
	Field[] fields = this.getClass().getDeclaredFields();

	for (Field field : fields) {
	    // Make private field accessible...
	    field.setAccessible(true);
	    try {
		if (field.get(this) != null)
		sbuild.append(field.getName() + ": " + String.valueOf(field.get(this)) + "\n");
	    } catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
	
	return sbuild.toString();
    }
}