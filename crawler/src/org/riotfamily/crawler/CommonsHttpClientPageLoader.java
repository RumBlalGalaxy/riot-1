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
package org.riotfamily.crawler;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.riotfamily.common.log.RiotLog;
import org.riotfamily.common.util.FormatUtils;

/**
 * PageLoader implementation that uses the Jakarta Commons HttpClient.
 * 
 * @author Felix Gnass [fgnass at neteye dot de]
 */
public class CommonsHttpClientPageLoader implements PageLoader {

	private RiotLog log = RiotLog.get(CommonsHttpClientPageLoader.class);

    private HttpClient client;

    private boolean textHtmlOnly = true;
    
    public CommonsHttpClientPageLoader() {
    	HttpConnectionManagerParams params = new HttpConnectionManagerParams();
		params.setConnectionTimeout((int) FormatUtils.parseMillis("10s"));
		params.setSoTimeout((int) FormatUtils.parseMillis("5s"));
		params.setStaleCheckingEnabled(true);
		params.setDefaultMaxConnectionsPerHost(256);
		params.setMaxTotalConnections(256);
		
		HttpConnectionManager connectionMangager = new MultiThreadedHttpConnectionManager();
		connectionMangager.setParams(params);
		client = new HttpClient(connectionMangager);
    }

	public void setTextHtmlOnly(boolean textHtmlOnly) {
		this.textHtmlOnly = textHtmlOnly;
	}

	public PageData loadPage(Href href) {
		String url = href.getResolvedUri();
		PageData pageData = new PageData(href);
		log.info("Loading page: " + url);
		GetMethod method = new GetMethod(url);
		HttpMethodParams params = new HttpMethodParams();
		params.setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
		method.setParams(params);
		method.setFollowRedirects(false);

		try {
			int statusCode = client.executeMethod(method);
			pageData.setStatusCode(statusCode);
			if (statusCode == HttpStatus.SC_OK) {
				if (accept(method)) {
					pageData.setContent(method.getResponseBodyAsStream(),
							method.getResponseCharSet());
					
					Header[] headers = method.getResponseHeaders();
					for (int i = 0; i < headers.length; i++) {
						pageData.addHeader(headers[i].getName(), headers[i].getValue());
					}
				}
			}
			else {
				log.info("Status: " + statusCode);
				Header location = method.getResponseHeader("Location");
				if (location != null) {
					pageData.setRedirectUrl(location.getValue());
				}
				else {
					pageData.setError(method.getStatusText());
				}
			}
		}
		catch (Exception e) {
			pageData.setError(e.getMessage());
			log.warn(e.getMessage());
		}
		finally {
			try {
				method.releaseConnection();
			}
			catch (Exception e) {
			}
		}
		return pageData;
	}

	protected boolean accept(GetMethod method) {
		if (textHtmlOnly) {
			Header contentType = method.getResponseHeader("Content-Type");
			String mimeType = contentType.getValue();
			return mimeType != null && mimeType.startsWith("text/html");
		}
		return true;
	}

}
