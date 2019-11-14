package org.jenkins_ci.plugins.xdzmkus.openalm;

import java.util.List;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundSetter;

import hudson.Extension;
import hudson.util.PersistedList;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;

@Extension
public class OALMGlobalConfiguration extends GlobalConfiguration
{
	public boolean trustCA = false;
	public List<OALMSite> sites = new PersistedList<>(this);

    public OALMGlobalConfiguration()
    {
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

}
