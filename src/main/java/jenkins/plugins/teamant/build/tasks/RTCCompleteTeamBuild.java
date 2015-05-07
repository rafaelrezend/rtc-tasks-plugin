package jenkins.plugins.teamant.build.tasks;

import hudson.CopyOnWrite;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Computer;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.tasks.Ant;
import hudson.util.ArgumentListBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;

import jenkins.model.Jenkins;
import jenkins.plugins.teamant.rtc.AntDocument;
import jenkins.plugins.teamant.rtc.exceptions.RTCConflictAttrException;
import jenkins.plugins.teamant.rtc.exceptions.RTCDependentAttrException;
import jenkins.plugins.teamant.rtc.exceptions.RTCMissingAttrException;
import jenkins.plugins.teamant.rtc.tasks.CompleteTeamBuildTask;

import org.jenkinsci.plugins.tokenmacro.MacroEvaluationException;
import org.jenkinsci.plugins.tokenmacro.TokenMacro;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * A buildstep wrapping any number of other buildsteps, controlling their
 * execution based on a defined condition.
 *
 */
public class RTCCompleteTeamBuild extends Recorder {

	private static final String PUBLISH_FILE_PREFIX = "rtccompleteteambuild_";

	private String buildResultUUID;

	private String antName = "Default Ant";

	/**
	 * @param label
	 *            RTC Build Result Activity label.
	 */
	@DataBoundConstructor
	public RTCCompleteTeamBuild(String buildResultUUID) {
		this.buildResultUUID = buildResultUUID;
	}

	/**
	 * @return Ant to invoke.
	 */
	Ant.AntInstallation getAnt() {
		for (Ant.AntInstallation i : getDescriptor().getInstallations()) {
			if (antName != null && antName.equals(i.getName()))
				return i;
		}
		return null;
	}

	/**
	 * @return
	 */
	public String getBuildResultUUID() {
		return buildResultUUID;
	}

	@Override
	public boolean perform(final AbstractBuild<?, ?> build,
			final Launcher launcher, final BuildListener listener)
			throws InterruptedException, IOException {

		// Only proceed in two conditions:
		// - There are no downstream builds.
		// - The build has failed.
		if (build.getDownstreamBuilds().size() > 0
				&& build.getResult() == Result.SUCCESS)
			return true;

		// Obtain environment variables from Jenkins environment
		EnvVars envs = build.getEnvironment(listener);

		// Get Ant executable path (String)
		// If no Ant is provided, it isn't possible to run any IBM Ant Task!
		String exe = getAntExe(launcher, listener, envs);
		if (exe == null) {
			// TODO Log issue here or inside the getAntExe method
			return false;
		}

		// Create publisher script file. It will first hold the start activity
		// then will be overwritten with the complete activity.
		FilePath antScriptFilePath = new FilePath(build.getWorkspace(),
				RTCCompleteTeamBuild.PUBLISH_FILE_PREFIX + envs.get("BUILD_ID"));

		String resolvedBuildResultUUID = "";

		try {
			resolvedBuildResultUUID = TokenMacro.expandAll(build, listener,
					getBuildResultUUID());
		} catch (MacroEvaluationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Write the Start activity file into the Jenkins workspace.
		try {
			writeCompleteTeamBuildFile(resolvedBuildResultUUID, envs,
					antScriptFilePath);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Run first Ant Task
		int exitcode = runCommand(launcher, listener, envs, exe,
				antScriptFilePath);
		if (exitcode != 0) {
			// TODO: log?
			// do something?
		}

		// Post-build step shouldn't affect the Build Result
		return true;
	}

	private int runCommand(final Launcher launcher,
			final BuildListener listener, EnvVars envs, String exe,
			FilePath scriptFilePath) throws IOException, InterruptedException {

		// Create command
		ArgumentListBuilder command = new ArgumentListBuilder();

		// Add the executable to command-line composer.
		command.add(exe);

		// Add RTC build toolkit path to the command line composer.
		command.add("-lib", envs.get("RTCBuildToolkit"));

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
		return launcher.launch().cmds(command).envs(envs).stdout(listener)
				.pwd(scriptFilePath.getParent()).join();
	}

	private void writeCompleteTeamBuildFile(String resolvedBuildResultUUID,
			EnvVars envs, FilePath scriptFilePath)
			throws IllegalAccessException, RTCMissingAttrException,
			RTCConflictAttrException, ParserConfigurationException,
			IOException, InterruptedException, TransformerException,
			RTCDependentAttrException {

		// Create CompleteTeamBuild task from environment variables
		CompleteTeamBuildTask teamBuild = new CompleteTeamBuildTask();
		teamBuild.setBuildResultUUID(resolvedBuildResultUUID);
		teamBuild.setRepositoryAddress(envs.get("repositoryAddress"));
		teamBuild.setUserId(envs.get("userId"));
		teamBuild.setPassword(envs.get("password"));

		// Create the Ant XML Document and add the start node and echo
		AntDocument antDoc = new AntDocument();
		antDoc.addNode(teamBuild);

		// Write this Ant XML Document to a file
		writeScriptFile(antDoc, scriptFilePath);
	}

	private String getAntExe(final Launcher launcher,
			final BuildListener listener, EnvVars envs) throws IOException,
			InterruptedException {
		// Retrieve Ant installation
		Ant.AntInstallation ai = getAnt();
		if (ai == null) {
			listener.getLogger().println("No Ant installation provided.");
			return null;
		}

		// Find the installation in the current machine
		ai = ai.forNode(Computer.currentComputer().getNode(), listener);
		ai = ai.forEnvironment(envs);

		// Add environment variables to the ant environment.
		ai.buildEnvVars(envs);

		// Retrieve the Ant executable
		String exe = ai.getExecutable(launcher);

		if (exe == null) {
			// Not found, fail the build.
			listener.getLogger().println("Ant executable not found.");
			return null;
		}
		return exe;
	}

	private void writeScriptFile(AntDocument antDoc, FilePath scriptFilePath)
			throws IOException, InterruptedException, TransformerException {

		StreamResult result = new StreamResult(scriptFilePath.write());
		antDoc.writeDocument(result);
		// Closing output stream. Otherwise, file can't be deleted later on.
		result.getOutputStream().close();
	}

	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

	@Extension
	public static final class DescriptorImpl extends
			BuildStepDescriptor<Publisher> {

		@CopyOnWrite
		private volatile Ant.AntInstallation[] installations = new Ant.AntInstallation[0];

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean isApplicable(
				@SuppressWarnings("rawtypes") Class<? extends AbstractProject> aClass) {
			// indicates that this builder can be used with all kinds of project
			// types
			return true;
		}

		/**
		 * This human readable name is used in the configuration screen.
		 */
		@Override
		public String getDisplayName() {
			return "RTC Complete Team Build";
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

	/**
	 * {@inheritDoc}
	 */
	public BuildStepMonitor getRequiredMonitorService() {
		// TODO Auto-generated method stub
		return BuildStepMonitor.NONE;
	}
}
