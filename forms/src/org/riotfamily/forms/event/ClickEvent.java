package org.riotfamily.forms.event;

import org.riotfamily.forms.element.core.Button;

/**
 *
 */
public class ClickEvent {

	private Button source;

	public ClickEvent(Button source) {
		this.source = source;
	}

	public Button getSource() {
		return source;
	}
}
