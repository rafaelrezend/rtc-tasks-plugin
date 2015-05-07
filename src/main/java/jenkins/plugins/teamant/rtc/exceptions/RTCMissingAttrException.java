package jenkins.plugins.teamant.rtc.exceptions;

import jenkins.plugins.teamant.rtc.BaseTask;

/**
 * Exception when the ANT Task evaluation fails due to missing attributes which
 * are required for the Ant Task.
 * 
 * @author rar6si
 * 
 */
public class RTCMissingAttrException extends Exception {

	/** Default serial version UID */
	private static final long serialVersionUID = 1L;

	private static final String ERROR_MESSAGE = "Missing required attribute %s in the Ant task %s.";
	private static final String MUTEX_ERROR_MESSAGE = "Missing attribute %s or %s in the Ant task %s. One of them should be provided.";

	/**
	 * Exception raised when a required attribute of the IBM Ant Task is
	 * missing.
	 * 
	 * @param clss
	 *            Ant Task bean.
	 * @param attribute
	 *            Missing attribute.
	 */
	public RTCMissingAttrException(Class<? extends BaseTask> clss,
			String attribute) {
		super(String.format(ERROR_MESSAGE, attribute, clss.getName()));
	}

	/**
	 * Exception raised when none of a pair of mutually exclusive attributes of
	 * the IBM Ant Task is missing (i.e. password and passwordFile).
	 * 
	 * @param clss
	 *            Ant Task bean.
	 * @param attrA
	 *            One of the missing mutex attribute.
	 * @param attrB
	 *            The other missing mutex attribute.
	 */
	public RTCMissingAttrException(Class<? extends BaseTask> clss,
			String attrA, String attrB) {
		super(String.format(MUTEX_ERROR_MESSAGE, attrA, attrB, clss.getName()));
	}
}