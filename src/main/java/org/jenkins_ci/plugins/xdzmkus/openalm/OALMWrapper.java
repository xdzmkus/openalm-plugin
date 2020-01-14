/*MIT License

Copyright (c) 2019 Dzmitry Kushniaruk

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package org.jenkins_ci.plugins.xdzmkus.openalm;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;

import org.jenkins_ci.plugins.xdzmkus.openalm.api.OALMArtifact;
import org.jenkins_ci.plugins.xdzmkus.openalm.http.OALMClient;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.scm.ChangeLogSet;
import hudson.tasks.BuildWrapperDescriptor;
import hudson.util.ListBoxModel;
import jenkins.scm.RunWithSCM;
import jenkins.tasks.SimpleBuildWrapper;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

public class OALMWrapper extends SimpleBuildWrapper
{
	private static final Logger LOGGER = Logger.getLogger(OALMWrapper.class.getName());

	private static final String PREFIX = "OALM_";
	
	public final String siteName;
	
	public final boolean printReply;
	
	public final boolean useCustomArtifactID;
	
	public final String artifactID;
	
    public final List<OALMArtifactIdPattern> artifactIdPatterns;

    @DataBoundConstructor
	public OALMWrapper(String siteName, boolean printReply, boolean useCustomArtifactID, String artifactID, List<OALMArtifactIdPattern> artifactIdPatterns)
	{
		this.siteName = siteName;
		this.printReply = printReply;
		this.useCustomArtifactID = useCustomArtifactID;
		this.artifactID = Util.fixEmptyAndTrim(artifactID);
		this.artifactIdPatterns = artifactIdPatterns == null ? Collections.emptyList() : artifactIdPatterns;
	}

	/* (non-Javadoc)
	 * @see jenkins.tasks.SimpleBuildWrapper#setUp(jenkins.tasks.SimpleBuildWrapper.Context, hudson.model.Run, hudson.FilePath, hudson.Launcher, hudson.model.TaskListener, hudson.EnvVars)
	 */
	@Override
	public void setUp(Context context, Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener,
			EnvVars initialEnvironment) throws IOException, InterruptedException
	{
		listener.getLogger().println("[OpenALM] Retrieve OpenALM artifact details...");
		
		OALMClient client = null;
		
		for (OALMSite site : OALMGlobalConfiguration.get().getSites())
		{
			if (site.getName().equals(siteName))
			{
				try
				{
					client = new OALMClient(site.getUrl(), site.getCredentialsId());
					context.env(PREFIX + "SITE_URL", site.getUrl());		
					break;
				}
				catch (URISyntaxException ex)
				{
					LOGGER.log(Level.WARNING, "URL of OpenALM site (" + siteName + ") incorrect.", ex);
				}
			}
		}
		
		if (client == null)
		{
			listener.error("[OpenALM] Cannot determine OpenALM site (" + siteName + ").");
			return;
		}
		
		String artifact = getArtifactIdForRequest(build, initialEnvironment);
		
		if (artifact == null)
		{
			listener.getLogger().println("[OpenALM] Cannot determine artifact ID.");
			return;
		}
			
		try
		{
			listener.getLogger().println("[OpenALM] Artifact ID: " + artifact);

			JSONObject json = client.retrieveArtifact(artifact);

			if (printReply && json != null) listener.getLogger().println(json);
			
			OALMArtifact oalmArtifact = new OALMArtifact(json, PREFIX);
			
			injectEnvVars(context, oalmArtifact);
			
			String artifactUrl = context.getEnv().getOrDefault(PREFIX + "SITE_URL", "") +
								 context.getEnv().getOrDefault(PREFIX + "ARTIFACT_URL", "");

			build.addAction(new OALMBuildData(oalmArtifact, artifactUrl));
			build.addAction(new OALMBuildBadgeAction(artifactUrl, artifact));

			listener.getLogger().println("[OpenALM] Environment variables are set:");
			
			for (Entry<String, String> env : oalmArtifact.getEnvVars().entrySet())
			{
				listener.getLogger().println(env.getKey() + "=" + env.getValue());
			}

			return;
		}
		catch (URISyntaxException | JSONException ex)
		{
			LOGGER.warning(ex.getMessage());
		}
		catch (IOException ex)
		{
			listener.getLogger().println("[OpenALM] " + ex.getMessage());
		}

		listener.error("[OpenALM] Retrieving artifact (" + artifact + ") details failed.");
	}

    /**
     * Get artifact ID for http request. 
     * Try to parse commit message or specified parameter and apply splitting patterns.
     *
     * @param build a build being run
     * 
	 * @param initialEnvironment the environment variables set at the outset
     *
     * @return String for http request or null
     */
	protected @CheckForNull String getArtifactIdForRequest(Run<?, ?> build, EnvVars initialEnvironment)
	{
		String artifact = null;
		
		if (useCustomArtifactID)
		{
			if (artifactID != null)
			{
				artifact = initialEnvironment.expand(artifactID);
			}
		}
		else
		{
			ChangeLogSet<? extends ChangeLogSet.Entry> changeSets = getChangeSet(build);

			if(!changeSets.isEmptySet())
			{
				// look for the last commit
				Iterator<? extends ChangeLogSet.Entry> it = changeSets.iterator();
				ChangeLogSet.Entry entry = it.next();
				while (it.hasNext()) entry = it.next();
				artifact = entry.getMsg();
			}
		}
		
		if (artifact == null || artifact.isEmpty())
			return null;
		
		for (OALMArtifactIdPattern pattern : artifactIdPatterns)
		{
			artifact = pattern.applyPattern(artifact);
		}
		
		if (!artifact.matches("\\d+"))
			return null;
		
		return artifact;
	}
	
    /**
     * Inject environment variables related to artifact. 
     *
     * @param context a way of collecting modifications to the environment for nested steps
     * 
     * @param oalmArtifact received artifact details from site
     * 
     */
	protected void injectEnvVars(Context context, OALMArtifact oalmArtifact)
	{
		Map<String, String> envMap = oalmArtifact.getEnvVars();
		
		for (Entry<String, String> i : envMap.entrySet())
		{
			context.env(i.getKey(), i.getValue());
		}
	}

    /**
     * Get list of changes contained commit message. 
     *
     * @param build a build being run
     * 
     * @return set of scm changes for build
     */
    protected ChangeLogSet<? extends ChangeLogSet.Entry> getChangeSet(Run<?,?> build)
    {
        if (build instanceof RunWithSCM<?,?>)
        {
            for (ChangeLogSet<? extends ChangeLogSet.Entry> c : ((RunWithSCM<?,?>) build).getChangeSets())
            {
            	return c;
            }
        }

        return ChangeLogSet.createEmpty(build);
    }

	@Extension
	public static final class DescriptorImpl extends BuildWrapperDescriptor
	{
		@Override
		public String getDisplayName()
		{
			return "Parse commit message and retrieve OpenALM artifact details";
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
