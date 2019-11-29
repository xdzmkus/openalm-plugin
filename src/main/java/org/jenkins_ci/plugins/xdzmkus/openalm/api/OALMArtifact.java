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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.CheckForNull;

import hudson.Util;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class OALMArtifact
{
	private String id;
	private String uri;
	private String xref;
	private OALMTracker tracker;
	private OALMProject project;
	private String html_url;
	private OALMUser submitted_by_user;
	private String status;
	private String title;
	private List<OALMUser> assignees;
	
    private final String prefix;

    public OALMArtifact(JSONObject json, String prefix)
	{
    	this.prefix = Util.fixNull(prefix) + "ARTIFACT_";

    	if (json == null) return;
		
    	if (json.has("id")) id = json.getString("id");
    	if (json.has("uri")) uri = json.getString("uri");
    	if (json.has("xref")) xref = json.getString("xref");
    	if (json.has("tracker")) tracker = new OALMTracker(json.getJSONObject("tracker"), this.prefix);
    	if (json.has("project")) project = new OALMProject(json.getJSONObject("project"), this.prefix);
    	if (json.has("html_url")) html_url = json.getString("html_url");
    	if (json.has("submitted_by_user")) submitted_by_user = new OALMUser(json.getJSONObject("submitted_by_user"), this.prefix + "SUBMITTED_" );
    	if (json.has("status")) status = json.getString("status");
    	if (json.has("title")) title = json.getString("title");
    	if (json.has("assignees"))
    	{
    		assignees = new ArrayList<OALMUser>();
    		JSONArray users = json.getJSONArray("assignees");
    		if (users.size() == 1)
    		{
    			assignees.add(new OALMUser(users.getJSONObject(0), this.prefix + "ASSIGNED_"));
    		}
    		else
    		{
	    		for (int i = 0; i < users.size(); i++)
	    		{
	    			assignees.add(new OALMUser(users.getJSONObject(0), this.prefix + i + "_ASSIGNED_"));
	    		}
    		}
    	}
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

	public @CheckForNull String getXref()
	{
		return xref;
	}

	public void setXref(String xref)
	{
		this.xref = xref;
	}

	public @CheckForNull OALMTracker getTracker()
	{
		return tracker;
	}

	public void setTracker(OALMTracker tracker)
	{
		this.tracker = tracker;
	}

	public @CheckForNull OALMProject getProject()
	{
		return project;
	}

	public void setProject(OALMProject project)
	{
		this.project = project;
	}

	public @CheckForNull String getHtml_url()
	{
		return html_url;
	}

	public void setHtm_url(String htmlUrl)
	{
		this.html_url = htmlUrl;
	}

	public @CheckForNull OALMUser getSubmitted_by_user()
	{
		return submitted_by_user;
	}

	public void setSubmitted_by_user(OALMUser submitted_by_user)
	{
		this.submitted_by_user = submitted_by_user;
	}

	public @CheckForNull String getStatus()
	{
		return status;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public @CheckForNull String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public @CheckForNull List<OALMUser> getAssignees()
	{
		return assignees;
	}

	public void setAssignees(List<OALMUser> assignees)
	{
		this.assignees = assignees;
	}
	
	public Map<String, String> getEnvVars()
	{
		HashMap<String, String> envMap = new HashMap<String, String>();
	
		envMap.put(prefix + "ID", Util.fixNull(getId()));		
		envMap.put(prefix + "URL", Util.fixNull(getHtml_url()));		
		envMap.put(prefix + "URI", Util.fixNull(getUri()));		
		envMap.put(prefix + "XREF", Util.fixNull(getXref()));		
		envMap.put(prefix + "STATUS", Util.fixNull(getStatus()));		
		envMap.put(prefix + "TITLE", Util.fixNull(getTitle()));		
		
		OALMTracker tracker = getTracker();
		if(tracker != null)
		{
			envMap.putAll(tracker.getEnvVars());
		}
		
		OALMProject project = getProject();
		if(project != null)
		{
			envMap.putAll(project.getEnvVars());
		}

		OALMUser submitted = getSubmitted_by_user();
		if(submitted != null)
		{
			envMap.putAll(submitted.getEnvVars());
		}
		
		List<OALMUser> assignees = getAssignees();
		if(assignees != null)
		{
			for (int i = 0; i < assignees.size(); i++)
			{
				OALMUser assigner = assignees.get(i);
				if(assigner != null)
				{
					envMap.putAll(assigner.getEnvVars());
				}
			}
		}

		return envMap;
	}

}
