package jenkins.plugins.teamant.rtc.exceptions;

import jenkins.plugins.teamant.rtc.tasks.BaseTask;

/**
 * Exception when the ANT Task evaluation fails due to conflicts resulting from
 * parameters that are mutually exclusive. Some parameters (i.e. password and
 * password file) shouldn't coexist in a valid input.
 * 
 * @author rar6si
 * 
 */
public class RTCConflictAttrException extends Exception {

	/** Default serial version UID */
	private static final long serialVersionUID = 1L;

	private static final String ERROR_MESSAGE = "Conflicting attributes %s and %s in the Ant task %s. Either one of them should be provided, but not both.";

	/**
	 * Exception raised when both of a mutually exclusive pair of attributes of
	 * the IBM Ant Task is provided.
	 * 
	 * @param clss
	 *            Ant Task bean.
	 * @param attrA
	 *            One of the missing mutex attribute.
	 * @param attrB
	 *            The other missing mutex attribute.
	 */
	public RTCConflictAttrException(Class<? extends BaseTask> clss,
			String attrA, String attrB) {
		super(String.format(ERROR_MESSAGE, attrA, attrB, clss.getName()));
	}
}