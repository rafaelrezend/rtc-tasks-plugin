package jenkins.plugins.teamant.rtc.exceptions;

import jenkins.plugins.teamant.rtc.BaseTask;

/**
 * Exception when the ANT Task evaluation fails due to missing attributes that
 * are required for another attribute in the same Ant Task.
 * 
 * @author rar6si
 * 
 */
public class RTCDependentAttrException extends Exception {

	/** Default serial version UID */
	private static final long serialVersionUID = 1L;

	private static final String ERROR_MESSAGE = "Missing dependency attribute in the Ant task %s. The attribute %s is only allowed if %s is provided.";

	/**
	 * Exception raised when one provided attribute depends on another attribute
	 * that hasn't been provided.
	 * 
	 * @param clss
	 *            Ant Task bean.
	 * @param attrA
	 *            Provided attribute.
	 * @param attrB
	 *            Missing attribute required for the provided one.
	 */
	public RTCDependentAttrException(Class<? extends BaseTask> clss,
			String attrA, String attrB) {
		super(String.format(ERROR_MESSAGE, clss.getName(), attrA, attrB));
	}
}