package jenkins.plugins.teamant.rtc.tasks;

import jenkins.plugins.teamant.rtc.BaseTask;
import jenkins.plugins.teamant.rtc.exceptions.RTCConflictAttrException;
import jenkins.plugins.teamant.rtc.exceptions.RTCDependentAttrException;
import jenkins.plugins.teamant.rtc.exceptions.RTCMissingAttrException;

/**
 * @author rar6si
 *
 */
public class StartTeamBuildTask extends BaseTask {
    
    // required fields
    private String repositoryAddress;
    private String resultUUIDProperty;
    private String userId;
    
    // non-required fields
    private String autoComplete;
    private String buildDefinitionId;
    private String certificateFile;
    private String engineId;
    private String failOnError;
    private String label;
    private String password;
    private String passwordFile;
    private String requestUUID;
    private String resultUUIDFile;
    private String smartCard;
    private String verbose;
    
    /** 
     * {@inheritDoc}
     */
    @Override
    public String getTaskDefName() {
	return "startTeamBuild";
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public String getTaskDefClassname() {
	return "com.ibm.team.build.ant.task.StartBuildTask";
    }

    /** 
     * {@inheritDoc}
     * @throws RTCDependentAttrException 
     */
    @Override
    public void eval() throws RTCMissingAttrException, RTCConflictAttrException, RTCDependentAttrException {
	
	// either buildDefinitionId or requestUUID must be given...
	if (buildDefinitionId == null && requestUUID == null)
	    throw new RTCMissingAttrException(this.getClass(), "buildDefinitionId", "requestUUID");
	// ... but not both.
	if (buildDefinitionId != null && requestUUID != null)
	    throw new RTCConflictAttrException(this.getClass(), "buildDefinitionId", "requestUUID");

	// validate required attributes
	if (repositoryAddress == null)
	    throw new RTCMissingAttrException(this.getClass(),
		    "repositoryAddress");
	if (resultUUIDProperty == null)
	    throw new RTCMissingAttrException(this.getClass(),
		    "resultUUIDProperty");
	if (userId == null)
	    throw new RTCMissingAttrException(this.getClass(), "userId");

	// validate password and password file
	// either of them should be provided.
	if (password == null && passwordFile == null)
	    throw new RTCMissingAttrException(this.getClass(), "password",
		    "passwordFile");
	// but not both at the same time
	if (password != null && passwordFile != null)
	    throw new RTCConflictAttrException(this.getClass(), "password", "passwordFile");
	
	// engineId attribute is only allowed if buildDefinitionId is provided.
	if (engineId != null && buildDefinitionId == null)
	    throw new RTCDependentAttrException(this.getClass(), "engineId", "buildDefinitionId");
	
    }

    /**
     * @return the repositoryAddress
     */
    public String getRepositoryAddress() {
        return repositoryAddress;
    }

    /**
     * @param repositoryAddress the repositoryAddress to set
     */
    public void setRepositoryAddress(String repositoryAddress) {
        this.repositoryAddress = repositoryAddress;
    }

    /**
     * @return the resultUUIDProperty
     */
    public String getResultUUIDProperty() {
        return resultUUIDProperty;
    }

    /**
     * @param resultUUIDProperty the resultUUIDProperty to set
     */
    public void setResultUUIDProperty(String resultUUIDProperty) {
        this.resultUUIDProperty = resultUUIDProperty;
    }

    /**
     * @return the userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @param userId the userId to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * @return the autoComplete
     */
    public String getAutoComplete() {
        return autoComplete;
    }

    /**
     * @param autoComplete the autoComplete to set
     */
    public void setAutoComplete(String autoComplete) {
        this.autoComplete = autoComplete;
    }

    /**
     * @return the buildDefinitionId
     */
    public String getBuildDefinitionId() {
        return buildDefinitionId;
    }

    /**
     * @param buildDefinitionId the buildDefinitionId to set
     */
    public void setBuildDefinitionId(String buildDefinitionId) {
        this.buildDefinitionId = buildDefinitionId;
    }

    /**
     * @return the certificateFile
     */
    public String getCertificateFile() {
        return certificateFile;
    }

    /**
     * @param certificateFile the certificateFile to set
     */
    public void setCertificateFile(String certificateFile) {
        this.certificateFile = certificateFile;
    }

    /**
     * @return the engineId
     */
    public String getEngineId() {
        return engineId;
    }

    /**
     * @param engineId the engineId to set
     */
    public void setEngineId(String engineId) {
        this.engineId = engineId;
    }

    /**
     * @return the failOnError
     */
    public String getFailOnError() {
        return failOnError;
    }

    /**
     * @param failOnError the failOnError to set
     */
    public void setFailOnError(String failOnError) {
        this.failOnError = failOnError;
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param label the label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
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
     * @param passwordFile the passwordFile to set
     */
    public void setPasswordFile(String passwordFile) {
        this.passwordFile = passwordFile;
    }

    /**
     * @return the requestUUID
     */
    public String getRequestUUID() {
        return requestUUID;
    }

    /**
     * @param requestUUID the requestUUID to set
     */
    public void setRequestUUID(String requestUUID) {
        this.requestUUID = requestUUID;
    }

    /**
     * @return the resultUUIDFile
     */
    public String getResultUUIDFile() {
        return resultUUIDFile;
    }

    /**
     * @param resultUUIDFile the resultUUIDFile to set
     */
    public void setResultUUIDFile(String resultUUIDFile) {
        this.resultUUIDFile = resultUUIDFile;
    }

    /**
     * @return the smartCard
     */
    public String getSmartCard() {
        return smartCard;
    }

    /**
     * @param smartCard the smartCard to set
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
     * @param verbose the verbose to set
     */
    public void setVerbose(String verbose) {
        this.verbose = verbose;
    }

}
