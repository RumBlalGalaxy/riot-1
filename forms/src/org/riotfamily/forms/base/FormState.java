/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.riotfamily.forms.base;

import javax.servlet.http.HttpSession;

import org.riotfamily.forms.client.Html;
import org.riotfamily.forms.client.ResourceManager;
import org.riotfamily.forms.value.TypeInfo;
import org.riotfamily.forms.value.Value;

public interface FormState {

	void populate(Value value);

	String render();

	Element.State getElementState(String stateId);

	void put(HttpSession session);

	Html newHtml();

	TypeInfo getTypeInfo();

	String id();

	FormServices getServices();
	
	ResourceManager getResourceManager();
	
}