/**
 * 
 */
package org.jenkins_ci.plugins.xdzmkus.openalm;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

import org.jenkins_ci.plugins.xdzmkus.openalm.http.OALMClient;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildWrapperDescriptor;
import hudson.util.ListBoxModel;
import jenkins.tasks.SimpleBuildWrapper;
import net.sf.json.JSONObject;

/**
 * @author xdku
 *
 */
public class OALMWrapper extends SimpleBuildWrapper
{
	private static final Logger LOGGER = Logger.getLogger(OALMWrapper.class.getName());

	public String siteName;
	
	public String artifactID;
	
    public List<OALMArtifactIdPattern> artifactIdPatterns = new ArrayList<OALMArtifactIdPattern>();

    @DataBoundConstructor
	public OALMWrapper(String siteName, String artifactID, List<OALMArtifactIdPattern> artifactIdPatterns)
	{
		this.siteName = siteName;
		this.artifactID = Util.fixEmptyAndTrim(artifactID);
		this.artifactIdPatterns = artifactIdPatterns;
	}

	/* (non-Javadoc)
	 * @see jenkins.tasks.SimpleBuildWrapper#setUp(jenkins.tasks.SimpleBuildWrapper.Context, hudson.model.Run, hudson.FilePath, hudson.Launcher, hudson.model.TaskListener, hudson.EnvVars)
	 */
	@Override
	public void setUp(Context context, Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener,
			EnvVars initialEnvironment) throws IOException, InterruptedException
	{
		LOGGER.info("OALM check....");
		
		OALMClient client = null;
		
		for (OALMSite site : OALMGlobalConfiguration.get().getSites())
		{
			if (site.getName().equals(siteName))
			{
				client = new OALMClient(site.getUrl(), site.getCredentialsId());
				break;
			}
		}
		
		if (client == null)
		{
			listener.error("Cannot determine OpenALM site (" + siteName + ").");
			build.setResult(Result.UNSTABLE);
			return;
		}
		
		if (artifactID == null)
		{
			listener.error("ArtifactID is empty");
			build.setResult(Result.UNSTABLE);
			return;
		}
		
		String artifact = artifactID;

		try
		{
			for (OALMArtifactIdPattern pattern : artifactIdPatterns)
			{
				artifact = pattern.applyPattern(artifact);
			}
			JSONObject json = client.retrieveArtifact(artifact);
			listener.getLogger().println(json);
		}
		catch (URISyntaxException | IOException e)
		{
			listener.error("Retrieve artifact (" + artifact + ") details failed.");
			listener.error(e.getLocalizedMessage());
			build.setResult(Result.UNSTABLE);
			return;
		}
	}

	@Extension
	public static final class DescriptorImpl extends BuildWrapperDescriptor
	{
		@Override
		public String getDisplayName()
		{
			return "Retrieve OALM artifact info";
		}
		
		@Override
		public boolean isApplicable(AbstractProject<?, ?> item)
		{
			return true;
		}
		
		public ListBoxModel doFillSiteNameItems()
		{
			ListBoxModel m = new ListBoxModel();

			for (OALMSite site : OALMGlobalConfiguration.get().getSites())
			{
				m.add(site.getName());
			}
			return m;

		}
		
	}
}
