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
		return matcher.find() ? matcher.group() : artifactID;
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
