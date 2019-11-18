package org.jenkins_ci.plugins.xdzmkus.openalm;

import java.util.List;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import hudson.util.PersistedList;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

@Extension
public class OALMGlobalConfiguration extends GlobalConfiguration
{
	private boolean trustCA = false;;
	private List<OALMSite> sites = new PersistedList<>(this);

    public OALMGlobalConfiguration()
    {
    }

    @DataBoundConstructor
    public OALMGlobalConfiguration(boolean trustCA, List<OALMSite> sites)
    {
    	this.trustCA = trustCA;
    	this.sites = sites == null ? new PersistedList<>(this) : sites;
        load();
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
        // empty lists coming from the HTTP request are not set on bean by "req.bindJSON(this, json)"
        setSites(req.bindJSONToList(OALMSite.class, json.get("sites")));
        setTrustCA((boolean)json.get("trustCA"));
        return true;
    }
}
