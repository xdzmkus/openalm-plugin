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

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

@Extension
public class OALMGlobalConfiguration extends GlobalConfiguration
{
	private boolean trustCA = false;;
	private List<OALMSite> sites = Collections.emptyList();

	public OALMGlobalConfiguration()
	{
		load();
	}
			
	@DataBoundConstructor
    public OALMGlobalConfiguration(boolean trustCA, List<OALMSite> sites)
    {
        load();
    	this.trustCA = trustCA;
    	this.sites = sites == null ? Collections.emptyList() : sites;
    }

    public List<OALMSite> getSites()
    {
        return sites;
    }

    @DataBoundSetter
    public void setSites(List<OALMSite> sites)
    {
        this.sites = sites;
        save();
    }

    public boolean getTrustCA()
    {
        return trustCA;
    }

    @DataBoundSetter
    public void setTrustCA(boolean trustCA)
    {
        this.trustCA = trustCA;
        save();
    }

    @Nonnull
    public static OALMGlobalConfiguration get()
    {
        return (OALMGlobalConfiguration) Jenkins.get().getDescriptorOrDie(OALMGlobalConfiguration.class);
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException
    {
        if (!json.has("sites"))
        {
        	// empty lists coming from the HTTP request are not set on bean by "req.bindJSON(this, json)"
        	this.sites = Collections.emptyList();
        }
        super.configure(req, json);
        return true;
    }
}
