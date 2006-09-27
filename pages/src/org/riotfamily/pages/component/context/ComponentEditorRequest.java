package org.riotfamily.pages.component.context;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.riotfamily.common.collection.IteratorEnumeration;
import org.springframework.util.Assert;

public class ComponentEditorRequest extends HttpServletRequestWrapper {

	private PageRequestContext context;
	
	private HashMap attributes = new HashMap();
	
	public ComponentEditorRequest(HttpServletRequest request, 
			PageRequestContext context) {
		
		super(request);
		this.context = context;
	}
	
	public String getMethod() {
		return context.getMethod();
	}
	
	public String getPathInfo() {
		return context.getPathInfo();
	}
	
	public String getServletPath() {
		return context.getServletPath();
	}
	
	public String getQueryString() {
		return context.getQueryString();
	}
	
	public String getRequestURI() {
		return context.getRequestURI();
	}
	
	public StringBuffer getRequestURL() {
		StringBuffer url = new StringBuffer(getScheme());
		url.append("://").append(getServerName()).append(':').append(getServerPort());
		url.append(getRequestURI());
		return url;
	}
	
	public String getPathTranslated() {
		return (getPathInfo() != null ? getRealPath(getPathInfo()) : null);
	}
	
	public String getParameter(String name) {
		Assert.notNull(name, "Parameter name must not be null");
		String[] arr = (String[]) context.getParameters().get(name);
		return (arr != null && arr.length > 0 ? arr[0] : null);
	}

	public Enumeration getParameterNames() {
		return Collections.enumeration(context.getParameters().keySet());
	}

	public String[] getParameterValues(String name) {
		Assert.notNull(name, "Parameter name must not be null");
		return (String[]) context.getParameters().get(name);
	}

	public Map getParameterMap() {
		return Collections.unmodifiableMap(context.getParameters());
	}
	
	public Object getAttribute(String name) {
		if (name.startsWith("javax.servlet.")) {
			return super.getAttribute(name);
		}
		if (attributes.containsKey(name)) {
			return attributes.get(name);
		}
		return context.getAttributes().get(name);
	}

	public Enumeration getAttributeNames() {
		HashSet names = new HashSet();
		names.addAll(context.getAttributes().keySet());
		Iterator it = attributes.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry attribute = (Map.Entry) it.next();
			if (attribute.getValue() != null) {
				names.add(attribute.getKey());
			}
			else {
				// A null value in the local attributes map means that 
				// the attribute has been removed ...
				names.remove(attribute.getKey());
			}
		}
		return new IteratorEnumeration(names.iterator());
	}
	
	public void setAttribute(String name, Object value) {
		Assert.notNull(name, "Attribute name must not be null");
		attributes.put(name, value);
	}

	public void removeAttribute(String name) {
		Assert.notNull(name, "Attribute name must not be null");
		// Instead of removing the attribute we set it to null, which marks 
		// it as removed ...
		attributes.put(name, null);
	}

}