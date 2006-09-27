package org.riotfamily.pages.menu;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.riotfamily.common.web.util.ServletMappingHelper;
import org.riotfamily.pages.member.MemberBinder;
import org.riotfamily.pages.member.MemberBinderAware;
import org.riotfamily.pages.member.WebsiteMember;
import org.riotfamily.pages.member.support.NullMemberBinder;
import org.riotfamily.pages.mvc.cache.AbstractCachingPolicyController;
import org.springframework.web.servlet.ModelAndView;

/**
 * Abstract base class for controllers that render navigation menus.
 */
public abstract class AbstractMenuController 
		extends AbstractCachingPolicyController
		implements MemberBinderAware {

	private Log log = LogFactory.getLog(AbstractMenuController.class);
	
	private MenuBuilder menuBuilder;
	
	private String viewName;
	
	private ServletMappingHelper servletMappingHelper;
	
	private String contextPath;
	
	private String servletPrefix;
	
	private String servletSuffix;

	private boolean includeQueryStringInCacheKey = false;
	
	private boolean includeMemberRoleInCacheKey = true;
	
	private MemberBinder memberBinder = new NullMemberBinder();
	
	public AbstractMenuController() {
		servletMappingHelper = new ServletMappingHelper();
		servletMappingHelper.setUseOriginalRequest(true);
	}
	
	public void setMenuBuilder(MenuBuilder menuBuilder) {
		this.menuBuilder = menuBuilder;
	}
	
	public void setViewName(String viewName) {
		this.viewName = viewName;
	}

	public void setIncludeQueryStringInCacheKey(boolean includeQueryStringInCacheKey) {
		this.includeQueryStringInCacheKey = includeQueryStringInCacheKey;
	}

	public void setIncludeMemberRoleInCacheKey(boolean includeMemberRoleInCacheKey) {
		this.includeMemberRoleInCacheKey = includeMemberRoleInCacheKey;
	}

	public void setMemberBinder(MemberBinder memberBinder) {
		this.memberBinder = memberBinder;
	}

	public void appendCacheKeyInternal(StringBuffer key, 
			HttpServletRequest request) {
		
		super.appendCacheKeyInternal(key, request);
		if (includeQueryStringInCacheKey && request.getQueryString() != null) {
			key.append('?');
			key.append(request.getQueryString());
		}
		if (includeMemberRoleInCacheKey) {
			WebsiteMember member = memberBinder.getMember(request);
			if (member != null) {
				key.append("#role=");
				key.append(member.getRole());
			}
		}
	}
	
	public long getLastModified(HttpServletRequest request) {
		return menuBuilder.getLastModified(request);
	}
	
	public long getTimeToLive(HttpServletRequest request) {
		return 0;
	}
	
	public ModelAndView handleRequest(HttpServletRequest request, 
			HttpServletResponse response) throws Exception {
		
		List items = menuBuilder.buildMenu(request);
		log.debug("MenuItems (before processing): " + items);
		items = processItems(items, request);
		log.debug("MenuItems (after processing): " + items);
		completeLinks(items, request, response);
		ModelAndView mv = new ModelAndView(viewName);
		if (items != null) {
			mv.addObject("items", items);
		}
		return mv;
	}
	
	protected abstract List processItems(List items, 
			HttpServletRequest request);
	
	protected void completeLinks(Collection items, HttpServletRequest request, 
			HttpServletResponse response) {
		
		if (items == null) {
			return;
		}
		Iterator it = items.iterator();
		while (it.hasNext()) {
			MenuItem item = (MenuItem) it.next();
			StringBuffer link = new StringBuffer();
			link.append(getContextPath(request));
			link.append(getServletPrefix(request));
			link.append(item.getLink());
			link.append(getServletSuffix(request));
			item.setLink(response.encodeURL(link.toString()));
			completeLinks(item.getChildItems(), request, response);
		}
	}

	protected String getContextPath(HttpServletRequest request) {
		if (contextPath == null) {
			contextPath = servletMappingHelper.getContextPath(request);
		}
		return contextPath;
	}
	
	protected String getServletPrefix(HttpServletRequest request) {
		if (servletPrefix == null) {
			servletPrefix = servletMappingHelper.getServletPrefix(request);
		}
		return servletPrefix;
	}
	
	protected String getServletSuffix(HttpServletRequest request) {
		if (servletSuffix == null) {
			servletSuffix = servletMappingHelper.getServletSuffix(request);
		}
		return servletSuffix;
	}
}
