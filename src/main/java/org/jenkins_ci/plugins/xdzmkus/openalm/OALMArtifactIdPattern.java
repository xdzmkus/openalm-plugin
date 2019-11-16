package org.jenkins_ci.plugins.xdzmkus.openalm;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;

public class OALMArtifactIdPattern extends AbstractDescribableImpl<OALMArtifactIdPattern>
{
	private Pattern pattern;
	
	@DataBoundConstructor
	public OALMArtifactIdPattern(String pattern)
	{
		this.pattern = Pattern.compile(pattern);
	}

	public String getPattern()
	{
		return pattern.pattern();
	}
	
	public String applyPattern(String artifactID)
	{
		Matcher matcher = pattern.matcher(artifactID);
		return matcher.matches() ? matcher.group() : artifactID;
	}
	@Extension
    public static class DescriptorImpl extends Descriptor<OALMArtifactIdPattern>
	{
		@Override
		public String getDisplayName()
		{
			return "OpenALM artifactID pattern";
		}

	}
	
}
