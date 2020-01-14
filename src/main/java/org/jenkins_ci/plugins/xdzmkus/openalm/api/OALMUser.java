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
package org.jenkins_ci.plugins.xdzmkus.openalm.api;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.CheckForNull;

import hudson.Util;
import net.sf.json.JSONObject;

public class OALMUser
{
    private String id;
    private String uri;
    private String user_url;
    private String real_name;
    private String display_name;    
    private String username;
    private String email;
    
    private final String prefix;
    
    public OALMUser(JSONObject json, String prefix)
	{
    	this.prefix = Util.fixNull(prefix) + "USER_";
    	
    	if (json == null) return;

		if (json.has("id")) id = json.getString("id");
    	if (json.has("uri")) uri = json.getString("uri");
    	if (json.has("user_url")) user_url = json.getString("user_url");
    	if (json.has("real_name")) real_name = json.getString("real_name");
    	if (json.has("display_name")) display_name = json.getString("display_name");
    	if (json.has("username")) username = json.getString("username");
    	if (json.has("email")) email = json.getString("email");
	}
    
    public @CheckForNull String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public @CheckForNull String getUri()
    {
        return uri;
    }

    public void setUri(String uri)
    {
        this.uri = uri;
    }

	public @CheckForNull String getUser_url()
	{
		return user_url;
	}

	public void setUser_url(String userUrl)
	{
		this.user_url = userUrl;
	}

	public @CheckForNull String getReal_name()
	{
		return real_name;
	}

	public void setReal_name(String realName)
	{
		this.real_name = realName;
	}

	public @CheckForNull String getDisplay_name()
	{
		return display_name;
	}

	public void setDisplay_name(String displayName)
	{
		this.display_name = displayName;
	}

	public @CheckForNull String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	/**
	 * @return the email
	 */
	public String getEmail()
	{
		return email;
	}

	/**
	 * @param email the email to set
	 */
	public void setEmail(String email)
	{
		this.email = email;
	}

	public Map<String, String> getEnvVars()
	{
		HashMap<String, String> envMap = new HashMap<String, String>();
	
		envMap.put(prefix + "ID", Util.fixNull(getId()));		
		envMap.put(prefix + "REALNAME", Util.fixNull(getReal_name()));		
		envMap.put(prefix + "DISPLAYNAME", Util.fixNull(getDisplay_name()));		
		envMap.put(prefix + "USERNAME", Util.fixNull(getUsername()));		
		envMap.put(prefix + "EMAIL", Util.fixNull(getEmail()));		

		return envMap;
	}
}
