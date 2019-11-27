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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

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
import hudson.tasks.BuildWrapperDescriptor;
import hudson.util.ListBoxModel;
import jenkins.tasks.SimpleBuildWrapper;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

public class OALMWrapper extends SimpleBuildWrapper
{
	private static final Logger LOGGER = Logger.getLogger(OALMWrapper.class.getName());

	private static final String PREFIX = "OALM_";
	
	public final String siteName;
	
	public final boolean printReply;
	
	public final String artifactID;
	
    public final List<OALMArtifactIdPattern> artifactIdPatterns;

    @DataBoundConstructor
	public OALMWrapper(String siteName, boolean printReply, String artifactID, List<OALMArtifactIdPattern> artifactIdPatterns)
	{
		this.siteName = siteName;
		this.printReply = printReply;
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
		listener.getLogger().println("Retrieve OpenALM artifact details...");
		
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
				catch (URISyntaxException e)
				{
					listener.error("URL of OpenALM site (" + siteName + ") incorrect.");
					return;
				}
			}
		}
		
		if (client == null)
		{
			listener.error("Cannot determine OpenALM site (" + siteName + ").");
			return;
		}
		
		if (artifactID == null)
		{
			listener.error("ArtifactID is empty");
			return;
		}
		
		String artifact = initialEnvironment.expand(artifactID);

		try
		{
			for (OALMArtifactIdPattern pattern : artifactIdPatterns)
			{
				artifact = pattern.applyPattern(artifact);
			}

			JSONObject json = client.retrieveArtifact(artifact);
			if (printReply) listener.getLogger().println(json);
			
			OALMArtifact oalmArtifact = new OALMArtifact(json);
			
			injectEnvVars(context, oalmArtifact);
			
			build.addAction(new OALMBuildData(oalmArtifact,
					context.getEnv().getOrDefault(PREFIX + "SITE_URL", "") + 
					context.getEnv().getOrDefault(PREFIX + "ARTIFACT_URL", ""))
			);
			
		}
		catch (URISyntaxException | IOException | JSONException e)
		{
			listener.error("Retrieve artifact (" + artifact + ") details failed.");
			listener.error(e.getLocalizedMessage());
			return;
		}
	}

	private void injectEnvVars(Context context, OALMArtifact oalmArtifact)
	{
		Map<String, String> envMap = oalmArtifact.getEnvVars(PREFIX);
		
		for (Entry<String, String> i : envMap.entrySet())
		{
			context.env(i.getKey(), i.getValue());
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
