/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Riot.
 *
 * The Initial Developer of the Original Code is
 * Neteye GmbH.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Felix Gnass [fgnass at neteye dot de]
 *
 * ***** END LICENSE BLOCK ***** */
package org.riotfamily.common.web.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.riotfamily.common.web.util.ServletUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContextException;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerMapping;

/**
 * HandlerMapping that works like Spring's BeanNameUrlHandlerMapping and
 * can expose parts of the matched URL as request attributes.
 * <p>The handler name <code>/foo/bar/@{some}/@{value}</code> would be
 * equivalent to <code>/foo/bar/&#42;/&#42;</code>, the last two wildcards
 * would be exposed as attributes "some" and "value".
 */
public class AdvancedBeanNameHandlerMapping 
		extends AbstractReverseHandlerMapping {

	private final Map handlerMap = new HashMap();

    private HashMap patternsByAntPath = new HashMap();

    private HashMap patternsByBeanName = new HashMap();

    private PathMatcher pathMatcher = new AntPathMatcher();
	
	private boolean stripServletMapping = true;
	
	public void setStripServletMapping(boolean stripServletMapping) {
		this.stripServletMapping = stripServletMapping;
	}
	
	protected boolean isStripServletMapping() {
		return this.stripServletMapping;
	}
	
    /**
	 * <strong>Copied from BeanNameUrlHandlerMapping</strong>
	 */
	public void initApplicationContext() throws ApplicationContextException {
		super.initApplicationContext();
    	String[] beanNames = getApplicationContext().getBeanDefinitionNames();

		// Take any bean name or alias that begins with a slash.
		for (int i = 0; i < beanNames.length; i++) {
			String[] urls = checkForUrl(beanNames[i]);
			if (urls.length > 0) {
				if (logger.isDebugEnabled()) {
					logger.debug("Found URL mapping [" + beanNames[i] + "]");
				}
				ArrayList patterns = new ArrayList();
				// Create a mapping to each part of the path.
				for (int j = 0; j < urls.length; j++) {
					String attributePattern = urls[j];
					String antPattern = AttributePattern.convertToAntPattern(attributePattern);
					registerHandler(antPattern, beanNames[i]);
					AttributePattern p = new AttributePattern(attributePattern);
					patternsByAntPath.put(antPattern, p);
					patterns.add(p);
				}
				patternsByBeanName.put(beanNames[i], patterns);
			}
			else {
				if (logger.isDebugEnabled()) {
					logger.debug("Rejected bean name '" + beanNames[i] + "'");
				}
			}
		}
	}
	
	/**
	 * Check name and aliases of the given bean for URLs,
	 * detected by starting with "/".
	 * <p><strong>Copied from BeanNameUrlHandlerMapping</strong>
	 */
	private String[] checkForUrl(String beanName) {
		List urls = new ArrayList();
		if (beanName.startsWith("/")) {
			urls.add(beanName);
		}
		String[] aliases = getApplicationContext().getAliases(beanName);
		for (int j = 0; j < aliases.length; j++) {
			if (aliases[j].startsWith("/")) {
				urls.add(aliases[j]);
			}
		}
		return StringUtils.toStringArray(urls);
	}
		
	/**
	 * Register the given handler instance for the given URL path.
	 * <p><strong>Copied from AbstractUrlHandlerMapping</strong>
	 * @param urlPath URL the bean is mapped to
	 * @param handler the handler instance
	 * @throws BeansException if the handler couldn't be registered
	 */
	private void registerHandler(String urlPath, Object handler) throws BeansException {
		Object mappedHandler = this.handlerMap.get(urlPath);
		if (mappedHandler != null) {
			throw new ApplicationContextException(
					"Cannot map handler [" + handler + "] to URL path [" + urlPath +
					"]: there's already handler [" + mappedHandler + "] mapped");
		}

		// Eagerly resolve handler if referencing singleton via name.
		if (handler instanceof String) {
			String handlerName = (String) handler;
			if (getApplicationContext().isSingleton(handlerName)) {
				handler = getApplicationContext().getBean(handlerName);
			}
		}

		if (urlPath.equals("/*")) {
			setDefaultHandler(handler);
		}
		else {
			this.handlerMap.put(urlPath, handler);
			if (logger.isDebugEnabled()) {
				logger.debug("Mapped URL path [" + urlPath
						+ "] onto handler [" + handler + "]");
			}
		}
	}

	protected String getLookupPath(HttpServletRequest request) {
		if (stripServletMapping) {
			return ServletUtils.getPathWithoutServletMapping(request);
		}
		else {
			return ServletUtils.getPathWithinApplication(request);
		}
	}
	
	/**
	 * Look up a handler for the given request, falling back to the default
	 * handler if no specific one is found.
	 * @param request current HTTP request
	 * @return the looked up handler instance, or the default handler
	 */
	public Object getHandlerInternal(HttpServletRequest request)
			throws Exception {

		String lookupPath = getLookupPath(request);
		if (logger.isDebugEnabled()) {
			logger.debug("Looking up handler for [" + lookupPath + "]");
		}

		return lookupHandler(lookupPath, request);
	}

	/**
	 * Look up a handler instance for the given URL path.
	 * <p>Supports direct matches, e.g. a registered "/test" matches "/test",
	 * and various Ant-style pattern matches, e.g. a registered "/t*" matches
	 * both "/test" and "/team". For details, see the AntPathMatcher class.
	 * <p>Looks for the most exact pattern, where most exact is defined as
	 * the longest path pattern.
	 * <p><strong>Copied from AbstractUrlHandlerMapping</strong>
	 * @param urlPath URL the bean is mapped to
	 * @return the associated handler instance, or <code>null</code> if not found
	 * @see org.springframework.util.AntPathMatcher
	 */
	protected Object lookupHandler(String urlPath, HttpServletRequest request) {
		// direct match?
		Object handler = handlerMap.get(urlPath);
		if (handler == null) {
			// pattern match?
			String bestMatch = null;
			for (Iterator it = handlerMap.keySet().iterator(); it.hasNext();) {
				String path = (String) it.next();
				if (pathMatcher.match(path, urlPath) &&
						(bestMatch == null || bestMatch.length() <= path.length())) {

					bestMatch = path;
				}
			}
			if (bestMatch != null) {
				exposeAttributes(bestMatch, urlPath, request);
				exposePathWithinMapping(pathMatcher.extractPathWithinPattern(bestMatch, urlPath), request);
				handler = handlerMap.get(bestMatch);
			}
		}
		else {
			exposePathWithinMapping(urlPath, request);
		}
		return handler;
	}
	
    
    /**
	 * <strong>Copied from AbstractUrlHandlerMapping</strong>
	 */
	protected void exposePathWithinMapping(String pathWithinMapping, HttpServletRequest request) {
		request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, pathWithinMapping);
	}
	
	protected void exposeAttributes(String antPattern, String urlPath,
			HttpServletRequest request) {

		AttributePattern pattern = (AttributePattern) patternsByAntPath.get(antPattern);
		pattern.expose(urlPath, request);

	}
	
	protected String addServletMappingIfNecessary(String path, 
			HttpServletRequest request) {
		
		if (path != null && isStripServletMapping()) {
			return ServletUtils.addServletMapping(path, request);
		}
		return path;
	}
	
	protected List getPatternsForHandler(String beanName, 
			HttpServletRequest request) {
		
		return (List) patternsByBeanName.get(beanName);
	}
	
}
