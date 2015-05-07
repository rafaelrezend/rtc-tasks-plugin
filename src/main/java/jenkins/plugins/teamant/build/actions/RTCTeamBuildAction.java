package jenkins.plugins.teamant.build.actions;

import hudson.EnvVars;
import hudson.model.Action;
import hudson.model.EnvironmentContributingAction;
import hudson.model.AbstractBuild;

import java.util.HashMap;
import java.util.Map;

/**
 * @author rar6si
 *
 */
public class RTCTeamBuildAction implements Action,
		EnvironmentContributingAction {

	private final Map<String, String> buildProperties = new HashMap<String, String>();

	public RTCTeamBuildAction() {
	}

	public RTCTeamBuildAction(String key, String value) {
		if (key != null && value != null) {
			addProperty(key, value);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getIconFileName() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDisplayName() {
		// TODO Auto-generated method stub
		return "RTC Team Build Action";
	}

	/**
	 * {@inheritDoc}
	 */
	public String getUrlName() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void buildEnvVars(AbstractBuild<?, ?> build, EnvVars env) {
		for (Map.Entry<String, String> entry : buildProperties.entrySet()) {
			env.put(entry.getKey(), entry.getValue());
		}
	}

	public void addProperty(String key, String value) {
		this.buildProperties.put(key, value);
	}

	public RTCTeamBuildAction merge(RTCTeamBuildAction action) {
		this.buildProperties.putAll(action.getBuildProperties());
		return this;
	}

	/**
	 * @return the buildProperties
	 */
	public Map<String, String> getBuildProperties() {
		return buildProperties;
	}
}
