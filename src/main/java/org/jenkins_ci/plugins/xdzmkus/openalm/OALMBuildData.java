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

import javax.annotation.CheckForNull;

import org.jenkins_ci.plugins.xdzmkus.openalm.api.OALMArtifact;

import hudson.Util;
import hudson.model.Run;
import jenkins.model.RunAction2;

public class OALMBuildData implements RunAction2
{
	private transient @CheckForNull Run<?, ?> build;

	public final String url;
	public final OALMArtifact artifact;
	
	public OALMBuildData(OALMArtifact oalmArtifact, String url)
	{
		this.url = Util.fixNull(url);
		this.artifact = oalmArtifact;
	}

	@Override
	public String getIconFileName()
	{
        return jenkins.model.Jenkins.RESOURCE_PATH+"/plugin/openalm/icons/tuleap-32x32.png";
	}

	@Override
	public String getDisplayName()
	{
		return "OpenALM artifact";
	}

	@Override
	public String getUrlName()
	{
		return "openalm";
	}

	@Override
	public void onAttached(Run<?, ?> run)
	{
		this.build = run;
	}

	@Override
	public void onLoad(Run<?, ?> run)
	{
		this.build = run;
	}

}
