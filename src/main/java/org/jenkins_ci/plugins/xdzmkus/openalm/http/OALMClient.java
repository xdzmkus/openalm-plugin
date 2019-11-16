package org.jenkins_ci.plugins.xdzmkus.openalm.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.net.ssl.SSLContext;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.AbstractHttpMessage;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.jenkins_ci.plugins.xdzmkus.openalm.OALMGlobalConfiguration;

import hudson.ProxyConfiguration;
import hudson.Util;
import hudson.security.ACL;
import jenkins.model.Jenkins;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

public class OALMClient
{
	private static final Logger LOGGER = Logger.getLogger(OALMClient.class.getName());

	/**
	 * URL of OpenALM for Jenkins access, like <tt>http://openalm.yoursite.org/</tt>.
	 * Mandatory. Normalized to end with '/'
	 */
	public String url;

	/**
	 * The id of the credentials to use. Optional.
	 */
	public String credentialsId;

	private CloseableHttpClient httpclient;

	public OALMClient(String url, String credentialsId)
	{
		this.url = Util.fixNull(url).trim();
		this.credentialsId = credentialsId;
		this.httpclient = createProxyHttpClient();
	}

	public void testConnection() throws URISyntaxException, IOException
	{
		URI oalmUrl = new URI(url);

		try
		{
			HttpGet httpGet = new HttpGet(oalmUrl);

			fillRequestHeaders(httpGet);

			LOGGER.info("Executing request " + httpGet.getRequestLine());

			httpclient.execute(httpGet, new OALMResponseHandler());
		}
		finally
		{
			httpclient.close();
		}
	}

	public @CheckForNull JSONObject retrieveArtifact(String artifactID) throws URISyntaxException, IOException
	{
		String artifact = Util.fixEmptyAndTrim(artifactID);
		
		if (artifact == null) return null;
		
		URI oalmUrl = new URI(url + "/api/artifacts/" + artifact);

		try
		{
			HttpGet httpGet = new HttpGet(oalmUrl);
			
			fillRequestHeaders(httpGet);

			LOGGER.info("Executing request " + httpGet.getRequestLine());

			return JSONObject.fromObject(httpclient.execute(httpGet, new OALMResponseHandler()));
		}
		catch (JSONException e)
		{
			LOGGER.warning(e.getLocalizedMessage());
			return null;
		}
		finally
		{
			httpclient.close();
		}
	}
	
	protected CloseableHttpClient createProxyHttpClient()
	{
		HttpClientBuilder httpClientBuilder = HttpClients.custom();

		if(Jenkins.get() != null)
		{
			ProxyConfiguration proxyConfiguration = Jenkins.get().proxy;
			if (proxyConfiguration != null)
			{
				CredentialsProvider credsProvider = new BasicCredentialsProvider();
				HttpHost proxy = new HttpHost(proxyConfiguration.name, proxyConfiguration.port);

				if (StringUtils.isNotEmpty(proxyConfiguration.getUserName()))
				{
					credsProvider.setCredentials(
							new AuthScope(proxyConfiguration.name, proxyConfiguration.port),
							new UsernamePasswordCredentials(proxyConfiguration.getUserName(), proxyConfiguration.getPassword()));
				}

				httpClientBuilder.setProxy(proxy).setDefaultCredentialsProvider(credsProvider);
			}
		}

		if (OALMGlobalConfiguration.get().getTrustCA())
		{
			try
			{
				SSLContext sslcontext = SSLContexts.custom()
						.loadTrustMaterial(new TrustSelfSignedStrategy())
						.build();

				// Allow TLSv1 protocol only
				SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
						sslcontext,
						new String[] { "TLSv1" },
						null,
						SSLConnectionSocketFactory.getDefaultHostnameVerifier());

				httpClientBuilder.setSSLSocketFactory(sslsf);
			}
			catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e)
			{
				LOGGER.warning(e.getLocalizedMessage());
			}
		}

		return httpClientBuilder.build();
	}

	protected void fillRequestHeaders(AbstractHttpMessage request)
	{
		request.addHeader("Content-Type", "application/json");

		if (credentialsId != null)
		{
			com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials credentials =
					com.cloudbees.plugins.credentials.CredentialsMatchers.firstOrNull(
							com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(
									com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials.class,
									Jenkins.get(),
									ACL.SYSTEM,
									Collections.emptyList()),
							com.cloudbees.plugins.credentials.CredentialsMatchers.withId(credentialsId));
			if (credentials != null)
			{
				try
				{
					String encoded = URLEncoder.encode(credentials.getUsername() + ":" + credentials.getPassword().getPlainText(), StandardCharsets.UTF_8.displayName());
					request.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoded);
				}
				catch (UnsupportedEncodingException e)
				{
					LOGGER.warning(e.getLocalizedMessage());
				}
			}
		}
	}

	public static class OALMResponseHandler implements ResponseHandler<String>
	{
		@Override
		public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException
		{
			int status = response.getStatusLine().getStatusCode();
			if (status >= 200 && status < 300)
			{
				HttpEntity entity = response.getEntity();
				return entity != null ? EntityUtils.toString(entity) : null;
			}
			else
			{
				throw new IOException("Unexpected response status: " + status);
			}
		}
	}
}
