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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import org.jenkins_ci.plugins.xdzmkus.openalm.http.OALMClient;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.interceptor.RequirePOST;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernameListBoxModel;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;

import hudson.Extension;
import hudson.Util;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.security.ACL;
import hudson.security.AccessControlled;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;

public class OALMSite extends AbstractDescribableImpl<OALMSite>
{
	private static final Logger LOG = Logger.getLogger(OALMSite.class.getName());

	/**
	 * Name of OpenALM connection
	 */
	public String name;

	/**
	 * URL of OpenALM for Jenkins access, like <tt>http://openalm.yoursite.org/</tt>.
	 * Mandatory. Normalized to end with '/'
	 */
	public String url;

	/**
	 * The id of the credentials to use. Optional.
	 */
	public String credentialsId;

	@DataBoundConstructor
	public OALMSite(String name, String url, String credentialsId)
	{
		this.name = Util.fixNull(name).trim();
		this.url = Util.fixNull(url).trim();
		this.credentialsId = credentialsId;
	}

	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name the name to set
	 */
	@DataBoundSetter
	public void setName(String name)
	{
		this.name = Util.fixNull(name).trim();
	}

	/**
	 * @return the url
	 */
	public String getUrl()
	{
		return url;
	}

	/**
	 * @param url the url to set
	 */
	@DataBoundSetter
	public void setUrl(String url)
	{
		this.url = Util.fixNull(url).trim();
	}

	/**
	 * @return the credentialsId
	 */
	public String getCredentialsId()
	{
		return credentialsId;
	}

	/**
	 * @param credentialsId the credentialsId to set
	 */
	@DataBoundSetter
	public void setCredentialsId(String credentialsId)
	{
		this.credentialsId = credentialsId;
	}

	@Extension
	public static class DescriptorImpl extends Descriptor<OALMSite>
	{
		@Override
		public String getDisplayName()
		{
			return "OpenALM Site";
		}

		public ListBoxModel doFillCredentialsIdItems(@AncestorInPath ItemGroup<?> context, @QueryParameter String url,
				@QueryParameter String credentialsId)
		{
			AccessControlled _context = (context instanceof AccessControlled ? (AccessControlled) context : Jenkins.get());
			if (_context == null || !_context.hasPermission(Jenkins.ADMINISTER))
			{
				return new StandardUsernameListBoxModel().includeCurrentValue(credentialsId);
			}
			
			return new StandardUsernameListBoxModel()
					.includeEmptyValue()
					.includeMatchingAs(ACL.SYSTEM, context,
							StandardUsernameCredentials.class,
							URIRequirementBuilder.fromUri(url).build(),
							CredentialsMatchers.always())
					.includeCurrentValue(credentialsId);
		}

        /**
         * Checks if the user name and password are valid for given URL.
         */
        @RequirePOST
        public FormValidation doValidate(@QueryParameter String name,
        								 @QueryParameter String url,
                                         @QueryParameter String credentialsId,
                                         @AncestorInPath Item item)
        {
        	checkPermissions(item);
        	
            try
			{
            	OALMClient client = new OALMClient(url, credentialsId);
            	client.testConnection();
            }
            catch (URISyntaxException e)
			{
                return FormValidation.error(String.format("Malformed URL (%s)", url), e);
			}
            catch (IOException e)
			{
            	LOG.info(e.getLocalizedMessage());
            	return FormValidation.error("Connection failed to OpenALM Site");
			}
            
            return FormValidation.ok("Connection established successfully");
        }
        
        public FormValidation doCheckName(@QueryParameter String value)
        {
            return FormValidation.validateRequired(value);
        }

        public FormValidation doCheckUrl(@QueryParameter String value)
        {
        	FormValidation result = FormValidation.validateRequired(value);
        	
            if (FormValidation.Kind.OK == result.kind)
            {
                try
    			{
                	new URI(value.trim());
    			}
                catch (URISyntaxException e)
    			{
                	result = FormValidation.error(String.format("Malformed URL (%s)", value), e);
    			}
            };
            return result;
        }
        
        protected void checkPermissions(Item item)
        {
            if (item == null)
            {
                Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            }
            else
            {
                item.checkPermission(Item.CONFIGURE);
            }
        }
	}
}
