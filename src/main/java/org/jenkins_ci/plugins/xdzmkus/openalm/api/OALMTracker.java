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

public class OALMTracker
{
    private String id;
    private String uri;
    private String label;
    private OALMProject project;
    
    private final String prefix;

    public OALMTracker(JSONObject json, String prefix)
	{
    	this.prefix = Util.fixNull(prefix) + "TRACKER_";
    	
    	if (json == null) return;

		if (json.has("id")) id = json.getString("id");
    	if (json.has("uri")) uri = json.getString("uri");
    	if (json.has("label")) label = json.getString("label");
    	if (json.has("project")) project = new OALMProject(json.getJSONObject("project"), this.prefix );
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

    public @CheckForNull String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

	public @CheckForNull OALMProject getProject()
	{
		return project;
	}

	public void setProject(OALMProject project)
	{
		this.project = project;
	}
	
	public Map<String, String> getEnvVars()
	{
		HashMap<String, String> envMap = new HashMap<String, String>();
	
		envMap.put(prefix + "ID", Util.fixNull(getId()));
		envMap.put(prefix + "LABEL", Util.fixNull(getLabel()));
		envMap.put(prefix + "URI", Util.fixNull(getUri()));
		
		OALMProject project = getProject();
		if(project != null)
		{
			envMap.putAll(project.getEnvVars());
		}

		return envMap;
	}

}
