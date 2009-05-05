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
 * Portions created by the Initial Developer are Copyright (C) 2007
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Felix Gnass [fgnass at neteye dot de]
 *
 * ***** END LICENSE BLOCK ***** */
package org.riotfamily.pages.riot.command;

import org.riotfamily.core.screen.list.command.CommandContext;
import org.riotfamily.core.screen.list.command.CommandResult;
import org.riotfamily.core.screen.list.command.impl.support.AbstractBatchCommand;
import org.riotfamily.core.screen.list.command.result.RefreshListResult;
import org.riotfamily.pages.model.Page;
import org.riotfamily.pages.model.Site;
import org.riotfamily.pages.model.SiteMapItem;

public class TranslatePageCommand extends AbstractBatchCommand<Page> {

	@Override
	public String getAction(CommandContext context) {
		Site site = (Site) context.getParent();
		if (site == null || site.getMasterSite() == null) {
			return null;
		}
		return super.getAction(context);
	}

	@Override
	protected String getIcon(String action) {
		return "page_add";
	}
	
	@Override
	protected CommandResult execute(CommandContext context, Page page,
			int index, int selectionSize) {

		SiteMapItem parent = (SiteMapItem) context.getParent();
		parent.addPage(new Page(page));
		return new RefreshListResult();
	}

}
