package jenkins.plugins.teamant.rtc.tasks.impl;

import jenkins.plugins.teamant.rtc.exceptions.RTCConflictAttrException;
import jenkins.plugins.teamant.rtc.exceptions.RTCMissingAttrException;
import jenkins.plugins.teamant.rtc.tasks.BaseTask;

/**
 * @author rar6si
 *
 */
public class CompleteBuildActivityTask extends BaseTask {

	// required fields
	private String activityId;
	private String buildResultUUID;
	private String repositoryAddress;
	private String userId;

	// non-required fields
	private String certificateFile;
	private String failOnError;
	private String password;
	private String passwordFile;
	private String smartCard;
	private String verbose;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getTaskDefName() {
		return "completeBuildActivity";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getTaskDefClassname() {
		return "com.ibm.team.build.ant.task.CompleteBuildActivityTask";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void eval() throws RTCMissingAttrException, RTCConflictAttrException {

		// validate required attributes
		if (activityId == null)
			throw new RTCMissingAttrException(this.getClass(), "activityId");
		if (buildResultUUID == null)
			throw new RTCMissingAttrException(this.getClass(),
					"buildResultUUID");
		if (repositoryAddress == null)
			throw new RTCMissingAttrException(this.getClass(),
					"repositoryAddress");
		if (userId == null)
			throw new RTCMissingAttrException(this.getClass(), "userId");

		// validate password and password file
		// either of them should be provided.
		if (password == null && passwordFile == null)
			throw new RTCMissingAttrException(this.getClass(), "password",
					"passwordFile");
		// but not both at the same time
		if (password != null && passwordFile != null)
			throw new RTCConflictAttrException(this.getClass(), "password",
					"passwordFile");

	}

	/**
	 * @return the activityId
	 */
	public String getActivityId() {
		return activityId;
	}

	/**
	 * @param activityId
	 *            the activityId to set
	 */
	public void setActivityId(String activityId) {
		this.activityId = activityId;
	}

	/**
	 * @return the buildResultUUID
	 */
	public String getBuildResultUUID() {
		return buildResultUUID;
	}

	/**
	 * @param buildResultUUID
	 *            the buildResultUUID to set
	 */
	public void setBuildResultUUID(String buildResultUUID) {
		this.buildResultUUID = buildResultUUID;
	}

	/**
	 * @return the repositoryAddress
	 */
	public String getRepositoryAddress() {
		return repositoryAddress;
	}

	/**
	 * @param repositoryAddress
	 *            the repositoryAddress to set
	 */
	public void setRepositoryAddress(String repositoryAddress) {
		this.repositoryAddress = repositoryAddress;
	}

	/**
	 * @return the userId
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * @param userId
	 *            the userId to set
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}

	/**
	 * @return the certificateFile
	 */
	public String getCertificateFile() {
		return certificateFile;
	}

	/**
	 * @param certificateFile
	 *            the certificateFile to set
	 */
	public void setCertificateFile(String certificateFile) {
		this.certificateFile = certificateFile;
	}

	/**
	 * @return the failOnError
	 */
	public String getFailOnError() {
		return failOnError;
	}

	/**
	 * @param failOnError
	 *            the failOnError to set
	 */
	public void setFailOnError(String failOnError) {
		this.failOnError = failOnError;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the passwordFile
	 */
	public String getPasswordFile() {
		return passwordFile;
	}

	/**
	 * @param passwordFile
	 *            the passwordFile to set
	 */
	public void setPasswordFile(String passwordFile) {
		this.passwordFile = passwordFile;
	}

	/**
	 * @return the smartCard
	 */
	public String getSmartCard() {
		return smartCard;
	}

	/**
	 * @param smartCard
	 *            the smartCard to set
	 */
	public void setSmartCard(String smartCard) {
		this.smartCard = smartCard;
	}

	/**
	 * @return the verbose
	 */
	public String getVerbose() {
		return verbose;
	}

	/**
	 * @param verbose
	 *            the verbose to set
	 */
	public void setVerbose(String verbose) {
		this.verbose = verbose;
	}

}
