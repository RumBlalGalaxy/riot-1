package org.riotfamily.riot.editor.ui;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.riotfamily.common.collection.FlatMap;
import org.riotfamily.common.util.ResourceUtils;
import org.riotfamily.common.web.mapping.UrlMapping;
import org.riotfamily.common.web.mapping.UrlMappingAware;
import org.riotfamily.riot.editor.CustomEditorDefinition;
import org.riotfamily.riot.editor.EditorRepository;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;

public class CustomEditorController implements EditorController, 
		UrlMappingAware, BeanNameAware {

	private EditorRepository editorRepository;
	
	private String editorIdAttribute = "editorId";
	
	private UrlMapping urlMapping;
	
	private String beanName;
	
	private String viewName = ResourceUtils.getPath(
			CustomEditorController.class, "CustomEditorView.ftl");
	
	
	public CustomEditorController(EditorRepository repository) {
		this.editorRepository = repository;
	}

	public void setEditorIdAttribute(String editorIdAttribute) {
		this.editorIdAttribute = editorIdAttribute;
	}
	
	public void setViewName(String viewName) {
		this.viewName = viewName;
	}

	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	public void setUrlMapping(UrlMapping urlMapping) {
		this.urlMapping = urlMapping;
	}

	public final ModelAndView handleRequest(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		String editorId = (String) request.getAttribute(editorIdAttribute);
		Assert.notNull(editorId, "No editorId in request scope");
		
		CustomEditorDefinition editor = (CustomEditorDefinition) 
				editorRepository.getEditorDefinition(editorId);
		
		Assert.notNull(editor, "No such editor: " + editorId);
		
		FlatMap model = new FlatMap();
		model.put("editorId", editorId);
		model.put("editorUrl", editor.getUrl());
		
		return new ModelAndView(viewName, model);
	}
	
	public Class getDefinitionClass() {
		return CustomEditorDefinition.class;
	}
	
	public String getUrl(String editorId, String objectId, String parentId) {
		FlatMap attrs = new FlatMap();
		attrs.put(editorIdAttribute, editorId);
		return urlMapping.getUrl(beanName, attrs);
	}
}
