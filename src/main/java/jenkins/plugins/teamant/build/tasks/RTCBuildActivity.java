package jenkins.plugins.teamant.build.rtcbuildactivity;

import hudson.CopyOnWrite;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Computer;
import hudson.model.Descriptor;
import hudson.tasks.BuildStep;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.tasks.Ant;
import hudson.util.ArgumentListBuilder;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;

import jenkins.model.Jenkins;
import jenkins.plugins.teamant.build.actions.RTCBuildActivityAction;
import jenkins.plugins.teamant.rtc.AntDocument;
import jenkins.plugins.teamant.rtc.exceptions.RTCConflictAttrException;
import jenkins.plugins.teamant.rtc.exceptions.RTCDependentAttrException;
import jenkins.plugins.teamant.rtc.exceptions.RTCMissingAttrException;
import jenkins.plugins.teamant.rtc.tasks.CompleteBuildActivityTask;
import jenkins.plugins.teamant.rtc.tasks.StartBuildActivityTask;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.tokenmacro.MacroEvaluationException;
import org.jenkinsci.plugins.tokenmacro.TokenMacro;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * A buildstep wrapping any number of other buildsteps, controlling their execution based on a defined condition.
 *
 */
public class RTCBuildActivity extends Builder {
    
    private static final String PUBLISH_FILE_PREFIX = "rtcactivity_";
    private String activityIdProperty;
    
    private String label;
    private String buildResultUUID;
    private List<BuildStep> enclosedSteps;
    private String antName = "Default Ant";


    /**
     * @param label RTC Build Result Activity label.
     * @param enclosedSteps List of enclosed build steps.
     */
    @DataBoundConstructor
    public RTCBuildActivity(String label, String buildResultUUID, List<BuildStep> enclosedSteps) {
        this.label = label;
        this.buildResultUUID = buildResultUUID;
        this.enclosedSteps = enclosedSteps;
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
    public String getLabel() {
        return label;
    }
    
    public String getBuildResultUUID() {
	return buildResultUUID;
    }
    
    /**
     * @return
     */
    public List<BuildStep> getEnclosedSteps() {
        if(enclosedSteps == null){
            enclosedSteps = new ArrayList<BuildStep>();
        }
        return enclosedSteps;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener) throws InterruptedException, IOException {
	
	// Obtain environment variables from Jenkins environment
	EnvVars envs = build.getEnvironment(listener);
	
	// Add an activity property ID identifier to connect the start and complete build activities
	activityIdProperty = "RTCActivityId_" + System.currentTimeMillis();
	
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
		RTCBuildActivity.PUBLISH_FILE_PREFIX + envs.get("BUILD_ID"));
	
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
	    writeStartBuildActivityFile(resolvedBuildResultUUID, envs, antScriptFilePath);
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	
	// Run first Ant Task
	int exitcode = runCommand(launcher, listener, envs, exe, antScriptFilePath);
	if (exitcode != 0) {
	    // TODO: log?
	    // do something?
	}
	
	// Regular expression that will extract the activityId provided by the
	// StartBuildActivity task.
	String regex = activityIdProperty + "=(.*?);";
	
	// PARSE THE LATEST 5 LINES OF THE CONSOLE OUTPUT
	// IMPORTANT: IT WILL ONLY PARSE THE LATEST 5 LINES!
	String activityId = parseContent(StringUtils.join(build.getLog(5).toArray()), regex);
	
	// Run inner build steps
	boolean shouldContinue = true;
	for (BuildStep buildStep : enclosedSteps) {
            if (!shouldContinue) {
                break;
            }
            
            // This works as a recursive call, because the RTCBuildActivity builder can have its own type as children.
            // Therefore, the activityId above is rewritten after every iteration, to keep the consistency of who is the right father's ID.
            // The ID should be written only when it isn't null.
            if (activityId != null) {
        	
        	// Get the RTC Team Build Action from the current build or a new one
        	RTCBuildActivityAction baAction = build.getAction(RTCBuildActivityAction.class);
        	if (baAction == null)
        	    baAction = new RTCBuildActivityAction();
        	
        	// Add the action with the new parameters
        	build.replaceAction(baAction.merge(new RTCBuildActivityAction("RTCParentActivityId", activityId)));
            }
            
            // TODO Update the 
            shouldContinue = buildStep.perform(build, launcher, listener);
        }
	
	// Replace the previous action for an empty one.
	build.replaceAction(new RTCBuildActivityAction());
	
	try {
	    writeCompleteBuildActivityFile(resolvedBuildResultUUID, envs, antScriptFilePath, activityId);
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	
	// Run closing Ant Task. Make sure the starting task worked.
	if (exitcode == 0) {
	    exitcode = runCommand(launcher, listener, envs, exe,
		    antScriptFilePath);
	    if (exitcode != 0) {
		// TODO: log?
		// do something?
	    }
	}
	
        return shouldContinue;
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
	return launcher.launch().cmds(command).envs(envs)
		.stdout(listener).pwd(scriptFilePath.getParent())
		.join();
    }

    private void writeStartBuildActivityFile(String resolvedBuildResultUUID, EnvVars envs,
	    FilePath scriptFilePath) throws IllegalAccessException,
	    RTCMissingAttrException, RTCConflictAttrException,
	    ParserConfigurationException, IOException, InterruptedException,
	    TransformerException, RTCDependentAttrException {
	
	// Create StartBuildActivity task from environment variables
	StartBuildActivityTask start = new StartBuildActivityTask();
	start.setLabel(getLabel());
	start.setRepositoryAddress(envs.get("repositoryAddress"));
	start.setActivityIdProperty("Test");
	start.setUserId(envs.get("userId"));
	start.setPassword(envs.get("password"));
	start.setBuildResultUUID(resolvedBuildResultUUID);
	
	// This is null when the activity has no parent activity.
	start.setParentActivityID(envs.get("RTCParentActivityId"));
	
	// Provide the property that will hold the Activity ID
	// Meant to be "UNIQUE", therefore, hashcode.
	start.setActivityIdProperty(this.activityIdProperty);
	
	// Create the Ant XML Document and add the start node and echo
	AntDocument antDoc = new AntDocument();
	antDoc.addNode(start);
	
	// Echo is important because it's the only mean to capture the Activity
	// ID when the Activity is going to be completed.
	antDoc.addEcho(this.activityIdProperty + "=${" + this.activityIdProperty + "};");
	
	// Write this Ant XML Document to a file
	writeScriptFile(antDoc, scriptFilePath);
    }
    
    private void writeCompleteBuildActivityFile(String resolvedBuildResultUUID, EnvVars envs,
	    FilePath scriptFilePath, String activityId) throws IllegalAccessException,
	    RTCMissingAttrException, RTCConflictAttrException,
	    ParserConfigurationException, IOException, InterruptedException,
	    TransformerException, RTCDependentAttrException {
	
	// Create CompleteBuildActivity task from environment variables
	CompleteBuildActivityTask complete = new CompleteBuildActivityTask();
	complete.setActivityId(activityId);
	complete.setBuildResultUUID(resolvedBuildResultUUID);
	complete.setRepositoryAddress(envs.get("repositoryAddress"));
	complete.setUserId(envs.get("userId"));
	complete.setPassword(envs.get("password"));
	
	// Create the Ant XML Document and add the start node and echo
	AntDocument antDoc = new AntDocument();
	antDoc.addNode(complete);
	
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
    
    private static String parseContent(String content, String regex) {
	// this pattern is specific for the given website
	Pattern pattProduct = Pattern.compile(regex, Pattern.DOTALL);
	// create a matcher from the given pattern for the URL content
	Matcher matcher = pattProduct.matcher(content);
	// find the first pattern match and return null if nothing has been
	// found
	if (!matcher.find()) {
	    return null;
	}
	// return the value in between (.*?)
	return matcher.group(1);
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
	
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
            return "RTC activity";
        }

        public List<? extends Descriptor<? extends BuildStep>> getBuilderDescriptors(AbstractProject<?, ?> project) {
            final List<BuildStepDescriptor<? extends Builder>> builders = new ArrayList<BuildStepDescriptor<? extends Builder>>();
            for (Descriptor<Builder> descriptor : Builder.all()) {
//                if (descriptor instanceof RTCBuildActivity.DescriptorImpl) {
//                    continue;
//                }
                if (!(descriptor instanceof BuildStepDescriptor)) {
                    continue;
                }
                BuildStepDescriptor<? extends Builder> buildStepDescriptor = (BuildStepDescriptor) descriptor;
                if (buildStepDescriptor.isApplicable(project.getClass()) && hasDbc(buildStepDescriptor.clazz)){
                    builders.add(buildStepDescriptor);
                }
            }
            return builders; 
        }
        
        private boolean hasDbc(final Class<?> clazz) {
            for (Constructor<?> constructor : clazz.getConstructors()) {
                if (constructor.isAnnotationPresent(DataBoundConstructor.class))
                    return true;
            }
            return false;
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
