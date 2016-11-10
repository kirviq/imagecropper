package jn.cropper.gui;

import lombok.RequiredArgsConstructor;

import javax.swing.*;
import java.awt.AWTEvent;
import java.awt.Container;
import java.awt.Toolkit;
import java.awt.event.*;

@RequiredArgsConstructor
class RadioBuilder {
	private final Container container;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private JRadioButton defaultButton;

	public RadioBuilder addButton(String text, Runnable action, char hotkey) {
		JRadioButton button = new JRadioButton();
		button.setAction(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				action.run();
			}
		});
		button.setText(text);
		container.add(button);
		buttonGroup.add(button);
		if (defaultButton == null) {
			button.setSelected(true);
			defaultButton = button;
			action.run();
		}
		if (hotkey > 0) {

			Toolkit.getDefaultToolkit().addAWTEventListener(o -> {
				if (o instanceof KeyEvent) {
					KeyEvent e = (KeyEvent) o;
					if (e.getKeyChar() == hotkey) {
						switch (e.getID()) {
							case KeyEvent.KEY_PRESSED:
								button.setSelected(true);
								button.getAction().actionPerformed(null);
								break;
							case KeyEvent.KEY_RELEASED:
								defaultButton.setSelected(true);
								defaultButton.getAction().actionPerformed(null);
								break;
						}
					}
				}
			}, AWTEvent.KEY_EVENT_MASK);
		}
		return this;
	}
}
