package org.riotfamily.forms.ajax;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.riotfamily.common.markup.TagWriter;
import org.riotfamily.forms.Element;
import org.riotfamily.forms.element.DHTMLElement;
import org.riotfamily.forms.event.FormListener;
import org.riotfamily.forms.resource.FormResource;
import org.riotfamily.forms.resource.ResourceElement;


/**
 * FormListener implementation used by the 
 * {@link org.riotfamily.forms.ajax.AjaxFormController AjaxFormController} to
 * notify the client of structural changes. It creates an XML document that
 * contains the modifications to be performed on the client side DOM.
 */
public class AjaxResponse implements FormListener {

	private Log log = LogFactory.getLog(AjaxResponse.class);
	
	private PrintWriter writer;
	
	private LinkedHashSet resources = new LinkedHashSet();
	
	private List propagations = new LinkedList();
	
	private List dhtmlElements = new LinkedList();
	
	private Element focusedElement;
	
	public AjaxResponse(HttpServletResponse response) throws IOException {
		response.setContentType("text/xml;charset=UTF-8");
		response.setCharacterEncoding("UTF-8");
		setCacheControlHeaders(response);
		this.writer = response.getWriter();
		writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		
		writer.println("<!DOCTYPE html " +
				"PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" " +
				"\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");
				
		writer.print("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
	}
	
	protected void setCacheControlHeaders(HttpServletResponse response) {
		response.setHeader("Pragma", "no-cache");
		response.setHeader("Expires", "0");
		response.setHeader("Cache-Control", "no-store");
	}
	
	protected void elementError(Element element) {
		if (element.getForm().getErrors().getErrors(element) != null) {
			TagWriter tag = new TagWriter(writer);
			tag.start("error");
			tag.attribute("ref", element.getId());
			tag.attribute("valid", element.getForm().getErrors().hasErrors(element) ? 0 : 1);
			tag.body();	
			element.getForm().getErrors().renderErrors(element);		
			tag.end();		
		}
	}
	
	public void elementChanged(Element element) {
		TagWriter tag = new TagWriter(writer);
		tag.start("replace");
		log.debug("Replacing element " + element.getId());
		tag.attribute("ref", element.getId());
		tag.body();		
		element.render(writer);		
		tag.end();		
		elementError(element);
	}

	public void elementRemoved(Element element) {
		TagWriter tag = new TagWriter(writer);
		tag.startEmpty("remove");
		tag.attribute("ref", element.getId());
		tag.end();
	}

	public void elementAdded(Element element) {
		TagWriter tag = new TagWriter(writer);
		tag.start("insert");
		tag.attribute("ref", element.getParent().getId());
		tag.body();
		element.render(writer);
		tag.end();
	}
	
	public void elementFocused(Element element) {
		log.debug("Focus requested for: " + element.getId());
		focusedElement = element;
	}
	
	public void elementEnabled(Element element) {
		TagWriter tag = new TagWriter(writer);
		tag.startEmpty("enable");
		tag.attribute("ref", element.getId());
		tag.attribute("state", element.isEnabled() ? 1 : 0);
		tag.end();
	}
	
	public void elementRendered(Element element) {
		log.debug("Element rendered: " + element);
		if (element instanceof JavaScriptEventAdapter) {
			JavaScriptEventAdapter adapter = (JavaScriptEventAdapter) element;
			EventPropagation.addPropagations(adapter, propagations);
		}
		if (element instanceof ResourceElement) {
			ResourceElement re = (ResourceElement) element;
			Collection res = re.getResources();
			if (res != null) {
				resources.addAll(res);
			}
		}
		if (element instanceof DHTMLElement) {
			log.debug("DHTML element registered");
			dhtmlElements.add(element);
		}
	}
	
	public void refresh(Element element) {
		TagWriter tag = new TagWriter(writer);
		tag.startEmpty("refresh");
		tag.attribute("ref", element.getId());
		tag.end();
	}
	
	public void alert(String message) {
		TagWriter tag = new TagWriter(writer);
		tag.start("eval");
		tag.body("alert('" + message + "');");
		tag.end();
	}
	
	protected void renderPropagations() {
		Iterator it = propagations.iterator();
		while (it.hasNext()) {
			EventPropagation p = (EventPropagation) it.next();
			renderPropagation(p);
		}
	}
	
	protected void renderPropagation(EventPropagation propagation) {
		log.debug("Propagating " + propagation.getType() + 
				" events for element " + propagation.getId());
		
		TagWriter tag = new TagWriter(writer);
		tag.startEmpty("propagate");
		tag.attribute("ref", propagation.getId());
		tag.attribute("type", propagation.getType());
		tag.end();
	}
	
	protected void renderScripts() {
		Iterator it = dhtmlElements.iterator();
		while (it.hasNext()) {
			DHTMLElement e = (DHTMLElement) it.next();
			String script = e.getInitScript();
			if (script != null) {
				log.debug("Evaluating init script ...");
				TagWriter tag = new TagWriter(writer);
				tag.start("eval").body();
				if (e.getPrecondition() != null) {
					writer.print("Resources.waitFor('");
					writer.print(e.getPrecondition());
					writer.print("', function() {");
					writer.print(script);
					writer.print("})");
				}
				else {
					writer.print(script);
				}
				tag.end();
			}
		}
	}
	
	protected void renderResources() {
		TagWriter tag = new TagWriter(writer);
		tag.start("eval").body();
		ArrayList loadedResources = new ArrayList();
		Iterator it = resources.iterator();
		while (it.hasNext()) {
			FormResource res = (FormResource) it.next();
			res.renderLoadingCode(writer, loadedResources);
			loadedResources.add(res);
		}
		tag.end();
	}
	
	protected void renderFocus() {
		if (focusedElement != null) {
			log.debug("Focusing element " + focusedElement.getId());
			TagWriter tag = new TagWriter(writer);
			tag.startEmpty("focus");
			tag.attribute("ref", focusedElement.getId());
			tag.end();
		}
	}
	
	protected void onClose() {
		renderResources();
		renderFocus();
		renderPropagations();
		renderScripts();
	}
	
	public void close() {
		onClose();
		writer.println("</html>");
		writer.flush();
		writer.close();
	}
	
}
