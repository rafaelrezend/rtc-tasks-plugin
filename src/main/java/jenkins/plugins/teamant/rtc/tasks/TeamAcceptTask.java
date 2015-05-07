/**
 * 
 */
package jenkins.plugins.teamant.rtc.tasks;

import jenkins.plugins.teamant.rtc.BaseTask;
import jenkins.plugins.teamant.rtc.exceptions.RTCConflictAttrException;
import jenkins.plugins.teamant.rtc.exceptions.RTCDependentAttrException;
import jenkins.plugins.teamant.rtc.exceptions.RTCMissingAttrException;

/**
 * @author rafaelrezende
 *
 */
public class TeamAcceptTask extends BaseTask {

	// required fields
	private String repositoryAddress;
	private String userId;

	// non-required fields
	private String workspaceName;
	private String workspaceUUID;
	private String certificateFile;
	private String buildResultUUID;
	private String changeSetFile;
	private String changesAcceptedProperty;
	private String failOnError;
	private String password;
	private String passwordFile;
	private String repositoriesFile;
	private String smartCard;
	private String snapshotName;
	private String snapshotUUIDProperty;
	private String verbose;

	/*
	 * (non-Javadoc)
	 * 
	 * @see jenkins.plugins.teamant.rtc.BaseTask#getTaskDefName()
	 */
	@Override
	public String getTaskDefName() {
		return "teamAccept";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jenkins.plugins.teamant.rtc.BaseTask#getTaskDefClassname()
	 */
	@Override
	public String getTaskDefClassname() {
		return "com.ibm.team.build.ant.task.TeamAcceptTask";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jenkins.plugins.teamant.rtc.BaseTask#eval()
	 */
	@Override
	public void eval() throws RTCMissingAttrException,
			RTCConflictAttrException, RTCDependentAttrException {
		// validate required fields
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
	 * @return the workspaceName
	 */
	public String getWorkspaceName() {
		return workspaceName;
	}

	/**
	 * @param workspaceName
	 *            the workspaceName to set
	 */
	public void setWorkspaceName(String workspaceName) {
		this.workspaceName = workspaceName;
	}

	/**
	 * @return the workspaceUUID
	 */
	public String getWorkspaceUUID() {
		return workspaceUUID;
	}

	/**
	 * @param workspaceUUID
	 *            the workspaceUUID to set
	 */
	public void setWorkspaceUUID(String workspaceUUID) {
		this.workspaceUUID = workspaceUUID;
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
	 * @return the changeSetFile
	 */
	public String getChangeSetFile() {
		return changeSetFile;
	}

	/**
	 * @param changeSetFile
	 *            the changeSetFile to set
	 */
	public void setChangeSetFile(String changeSetFile) {
		this.changeSetFile = changeSetFile;
	}

	/**
	 * @return the changesAcceptedProperty
	 */
	public String getChangesAcceptedProperty() {
		return changesAcceptedProperty;
	}

	/**
	 * @param changesAcceptedProperty
	 *            the changesAcceptedProperty to set
	 */
	public void setChangesAcceptedProperty(String changesAcceptedProperty) {
		this.changesAcceptedProperty = changesAcceptedProperty;
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
	 * @return the repositoriesFile
	 */
	public String getRepositoriesFile() {
		return repositoriesFile;
	}

	/**
	 * @param repositoriesFile
	 *            the repositoriesFile to set
	 */
	public void setRepositoriesFile(String repositoriesFile) {
		this.repositoriesFile = repositoriesFile;
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
	 * @return the snapshotName
	 */
	public String getSnapshotName() {
		return snapshotName;
	}

	/**
	 * @param snapshotName
	 *            the snapshotName to set
	 */
	public void setSnapshotName(String snapshotName) {
		this.snapshotName = snapshotName;
	}

	/**
	 * @return the snapshotUUIDProperty
	 */
	public String getSnapshotUUIDProperty() {
		return snapshotUUIDProperty;
	}

	/**
	 * @param snapshotUUIDProperty
	 *            the snapshotUUIDProperty to set
	 */
	public void setSnapshotUUIDProperty(String snapshotUUIDProperty) {
		this.snapshotUUIDProperty = snapshotUUIDProperty;
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
