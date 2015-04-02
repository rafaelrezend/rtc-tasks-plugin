package jenkins.plugins.teamant.build.rtcbuildactivity;

import hudson.model.AbstractProject;
import hudson.model.Project;
import hudson.tasks.BuildStep;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper to work with {@link BuildStep}s wrapped by {@link RTCBuildActivity} or {@link SingleConditionalBuilder}.
 * 
 * @author Dominik Bartholdi (imod)
 * 
 */
public class RTCBuildActivityHelper {

    private RTCBuildActivityHelper() {
    }

    /**
     * Gets the list of all buildsteps wrapped within any {@link RTCBuildActivity} or {@link SingleConditionalBuilder} from within the given project. Keeps the API backward compatible (Internally
     * calls {@link #getConditionalBuildersFromMavenProject(AbstractProject)})
     * 
     * @see https://issues.jenkins-ci.org/browse/JENKINS-20543
     * @param p
     *            the project to get all wrapped builders for
     * @param type
     *            the type of builders to search for
     * @return a list of all buildsteps, never <code>null</code>
     */
    public static <T extends BuildStep> List<T> getContainedBuilders(Project<?, ?> p, Class<T> type) {
        return getContainedBuilders((AbstractProject<?, ?>) p, type);
    }

    /**
     * Gets the list of all buildsteps wrapped within any {@link RTCBuildActivity} or {@link SingleConditionalBuilder} from within the given project.
     * 
     * @param p
     *            the project to get all wrapped builders for
     * @param type
     *            the type of builders to search for
     * @return a list of all buildsteps, never <code>null</code>
     */
    public static <T extends BuildStep> List<T> getContainedBuilders(AbstractProject<?, ?> ap, Class<T> type) {

//        final boolean mavenIsInstalled = isMavenPluginInstalled();

        List<T> r = new ArrayList<T>();

        List<RTCBuildActivity> cbuilders = new ArrayList<RTCBuildActivity>();
        if (Project.class.isAssignableFrom(ap.getClass())) {
            Project<?, ?> p = (Project<?, ?>) ap;
            cbuilders.addAll(p.getBuildersList().getAll(RTCBuildActivity.class));
        }
//        else if (mavenIsInstalled) {
//            cbuilders.addAll(getConditionalBuildersFromMavenProject(ap));
//        }

        for (RTCBuildActivity rtcActivity : cbuilders) {
            final List<BuildStep> cbs = rtcActivity.getEnclosedSteps();
            if (cbs != null) {
                for (BuildStep buildStep : cbs) {
                    if (type.isInstance(buildStep)) {
                        r.add(type.cast(buildStep));
                    }
                }
            }
        }

        return r;
    }

//    private static List<RTCBuildActivity> getConditionalBuildersFromMavenProject(AbstractProject<?, ?> ap) {
//        List<RTCBuildActivity> r = new ArrayList<RTCBuildActivity>();
//        if (MavenModuleSet.class.isAssignableFrom(ap.getClass())) {
//            MavenModuleSet ms = (MavenModuleSet) ap;
//            r.addAll(ms.getPostbuilders().getAll(RTCBuildActivity.class));
//            r.addAll(ms.getPrebuilders().getAll(RTCBuildActivity.class));
//        }
//        return r;
//    }

//    /**
//     * Is the maven plugin installed and active?
//     * 
//     * @return
//     */
//    public static boolean isMavenPluginInstalled() {
//        final hudson.Plugin plugin = Jenkins.getInstance().getPlugin("maven-plugin");
//        return plugin != null ? plugin.getWrapper().isActive() : false;
//    }
}
