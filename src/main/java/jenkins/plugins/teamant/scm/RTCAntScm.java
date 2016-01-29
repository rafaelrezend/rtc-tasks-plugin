/**
 * 
 */
package jenkins.plugins.teamant.scm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;

import jenkins.model.Jenkins;
import jenkins.plugins.teamant.build.tasks.RTCBuildActivity;
import jenkins.plugins.teamant.build.tasks.RTCBuildActivity.DescriptorImpl;
import jenkins.plugins.teamant.rtc.AntDocument;
import jenkins.plugins.teamant.rtc.exceptions.RTCConflictAttrException;
import jenkins.plugins.teamant.rtc.exceptions.RTCDependentAttrException;
import jenkins.plugins.teamant.rtc.exceptions.RTCMissingAttrException;
import jenkins.plugins.teamant.rtc.tasks.impl.TeamAcceptTask;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.Computer;
import hudson.model.TaskListener;
import hudson.model.Job;
import hudson.model.Run;
import hudson.scm.ChangeLogParser;
import hudson.scm.PollingResult;
import hudson.scm.RepositoryBrowser;
import hudson.scm.SCMRevisionState;
import hudson.scm.SCM;
import hudson.scm.SCMDescriptor;
import hudson.tasks.Ant;
import hudson.util.ArgumentListBuilder;

/**
 * @author rafaelrezende
 *
 */
public class RTCAntScm extends SCM {
	
	private static final String TEAM_ACCEPT_FILE_PREFIX = "rtcteamaccept_";
	
	private String buildToolkit;
	private String serverUri;
	private String buildWorkspace;
	private String userId;
	private String password;
	
	private String changesAcceptedProperty;
	
	// TODO remove it!
	private String antName = "Default Ant";
	
	@DataBoundConstructor
	public RTCAntScm(String buildToolkit, String serverUri,
			String buildWorkspace, String userId, String password) {
		super();
		this.buildToolkit = buildToolkit;
		this.serverUri = serverUri;
		this.buildWorkspace = buildWorkspace;
		this.userId = userId;
		this.password = password;
	}

	/**
	 * @return the buildToolkit
	 */
	public String getBuildToolkit() {
		return buildToolkit;
	}

	/**
	 * @return the serverUri
	 */
	public String getServerUri() {
		return serverUri;
	}

	/**
	 * @return the buildWorkspace
	 */
	public String getBuildWorkspace() {
		return buildWorkspace;
	}

	/**
	 * @return the userId
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/* (non-Javadoc)
	 * @see hudson.scm.SCM#createChangeLogParser()
	 */
	@Override
	public ChangeLogParser createChangeLogParser() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	/* (non-Javadoc)
	 * @see hudson.scm.SCM#calcRevisionsFromBuild(hudson.model.Run, hudson.FilePath, hudson.Launcher, hudson.model.TaskListener)
	 */
	@Override
	public SCMRevisionState calcRevisionsFromBuild(Run<?, ?> build,
			FilePath workspace, Launcher launcher, TaskListener listener)
			throws IOException, InterruptedException {
		// TODO check if it stands for this ant-based plugin.
		// From Team Concert SCM: our check for incoming changes uses the flow targets and does a real time compare
		// So for now we don't return a special revision state
		return SCMRevisionState.NONE;
	}

	/* (non-Javadoc)
	 * @see hudson.scm.SCM#checkout(hudson.model.Run, hudson.Launcher, hudson.FilePath, hudson.model.TaskListener, java.io.File, hudson.scm.SCMRevisionState)
	 */
	@Override
	public void checkout(Run<?, ?> build, Launcher launcher,
			FilePath workspace, TaskListener listener, File changelogFile,
			SCMRevisionState baseline) throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		super.checkout(build, launcher, workspace, listener, changelogFile, baseline);
	}

	/* (non-Javadoc)
	 * @see hudson.scm.SCM#compareRemoteRevisionWith(hudson.model.Job, hudson.Launcher, hudson.FilePath, hudson.model.TaskListener, hudson.scm.SCMRevisionState)
	 */
	@Override
	public PollingResult compareRemoteRevisionWith(Job<?, ?> project,
			Launcher launcher, FilePath workspace, TaskListener listener,
			SCMRevisionState baseline) throws IOException, InterruptedException {
		
		// Obtain environment variables from Jenkins environment
		//EnvVars envs = build???.getEnvironment(listener);
		
		long timestampId = System.currentTimeMillis();
		
		// Add an activity property ID identifier to connect the start and complete build activities
		changesAcceptedProperty = "RTCChangesAccepted_" + timestampId;
		
		// Get Ant executable path (String)
		// If no Ant is provided, it isn't possible to run any IBM Ant Task!
		String exe = getAntExe(launcher, listener);
		if (exe == null) {
		    // TODO Log issue here or inside the getAntExe method
			// TODO Which exception? Maybe this getAntExe should throw it right away when not found.
		    throw new IOException();
		}
		
		// Create publisher script file. It will first hold the start activity
		// then will be overwritten with the complete activity.
		// TODO Should it really be a timestamp?
		String teamAcceptFilename = RTCAntScm.TEAM_ACCEPT_FILE_PREFIX + timestampId; 
		
		FilePath antScriptFilePath = new FilePath(workspace, teamAcceptFilename);
		
		// Write the Start activity file into the Jenkins workspace.
		try {
		    writeTeamAcceptFile(antScriptFilePath);
		} catch (Exception e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
		
		// Run first Ant Task
		int exitcode = runCommand(launcher, listener, exe, antScriptFilePath);
		if (exitcode != 0) {
		    // TODO: log?
		    // do something?
		}
		
		// Regular expression that will extract the activityId provided by the
		// StartBuildActivity task.
		String regex = changesAcceptedProperty + "=(.*?);";
		
		// PARSE THE LATEST 5 LINES OF THE CONSOLE OUTPUT
		// IMPORTANT: IT WILL ONLY PARSE THE LATEST 5 LINES!
//		String changesAccepted = parseContent(StringUtils.join(build.getLog(5).toArray()), regex);
		
		return PollingResult.NO_CHANGES;
	}
	
	private int runCommand(final Launcher launcher,
			final TaskListener listener, String exe,
			FilePath scriptFilePath) throws IOException, InterruptedException {

		// Create command
		ArgumentListBuilder command = new ArgumentListBuilder();

		// Add the executable to command-line composer.
		command.add(exe);

		// Add RTC build toolkit path to the command line composer.
		command.add("-lib", this.buildToolkit);

		// Add publisher script to the command line
		command.add("-file", scriptFilePath.getRemote());

		// Fixing command line for windows
		if (!launcher.isUnix()) {
			command = command.toWindowsCommand();
			// For some reason, ant on windows rejects empty parameters but unix
			// does not. Add quotes for any empty parameter values:
			List<String> newArgs = new ArrayList<String>(command.toList());
			newArgs.set(newArgs.size() - 1, newArgs.get(newArgs.size() - 1)
					.replaceAll("(?<= )(-D[^\" ]+)= ", "$1=\"\" "));
			command = new ArgumentListBuilder(
					newArgs.toArray(new String[newArgs.size()]));
		}

		// Run the ant task and capture the exit code
		return launcher.launch().cmds(command).stdout(listener)
				.pwd(scriptFilePath.getParent()).join();
	}

	private void writeTeamAcceptFile(FilePath scriptFilePath) throws ParserConfigurationException, IllegalAccessException, RTCMissingAttrException, RTCConflictAttrException, RTCDependentAttrException, IOException, InterruptedException, TransformerException {

		// Create Team Accept task
		TeamAcceptTask accept = new TeamAcceptTask();
		accept.setRepositoryAddress(this.serverUri);
		accept.setUserId(this.userId);
		accept.setPassword(this.password);
		accept.setChangesAcceptedProperty(changesAcceptedProperty);
		
		AntDocument antDoc = new AntDocument();
		antDoc.addNode(accept);
		
		antDoc.addEcho(this.changesAcceptedProperty + "=${" + this.changesAcceptedProperty + "}");
		
		// Write this Ant XML Document to a file
		writeScriptFile(antDoc, scriptFilePath);
	}
	
	private void writeScriptFile(AntDocument antDoc, FilePath scriptFilePath)
			throws IOException, InterruptedException, TransformerException {

		StreamResult result = new StreamResult(scriptFilePath.write());
		antDoc.writeDocument(result);
		// Closing output stream. Otherwise, file can't be deleted later on.
		result.getOutputStream().close();
	}

	/* (non-Javadoc)
	 * @see hudson.scm.SCM#requiresWorkspaceForPolling()
	 */
	@Override
	public boolean requiresWorkspaceForPolling() {
		return true;
	}

	/* (non-Javadoc)
	 * @see hudson.scm.SCM#supportsPolling()
	 */
	@Override
	public boolean supportsPolling() {
		return true;
	}


	private String getAntExe(final Launcher launcher,
			final TaskListener listener) throws IOException,
			InterruptedException {
		// Retrieve Ant installation
		Ant.AntInstallation ai = getAnt();
		if (ai == null) {
			listener.getLogger().println("No Ant installation provided.");
			return null;
		}

		// Find the installation in the current machine
		ai = ai.forNode(Computer.currentComputer().getNode(), listener);

		// Retrieve the Ant executable
		String exe = ai.getExecutable(launcher);

		if (exe == null) {
			// Not found, fail the build.
			listener.getLogger().println("Ant executable not found.");
			return null;
		}
		return exe;
	}
	
	/**
	 * @return Ant to invoke.
	 */
	Ant.AntInstallation getAnt() {
		for (Ant.AntInstallation i : ((DescriptorImpl) getDescriptor()).getInstallations()) {
			if (antName != null && antName.equals(i.getName()))
				return i;
		}
		return null;
	}


	@Extension
	public static final class DescriptorImpl extends SCMDescriptor<RTCAntScm> {
		
		public DescriptorImpl() {
			super(RTCAntScm.class, RepositoryBrowser.class);
			load();
		}

		protected DescriptorImpl(Class<RTCAntScm> clazz,
				Class<? extends RepositoryBrowser> repositoryBrowser) {
			super(clazz, repositoryBrowser);
			// TODO Auto-generated constructor stub
		}

		@Override
		public String getDisplayName() {
			// TODO Auto-generated method stub
			return "Rational Team Concert (Ant)";
		}
		
		/**
		 * Provides a list of Ant installations to the user interface on
		 * Jenkins.
		 * 
		 * @return Array of Ant installations.
		 */
		public Ant.AntInstallation[] getInstallations() {
			return Jenkins.getInstance()
					.getDescriptorByType(Ant.DescriptorImpl.class)
					.getInstallations();
		}

	}

}
