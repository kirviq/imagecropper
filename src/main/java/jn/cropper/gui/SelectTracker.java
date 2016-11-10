package jn.cropper.gui;

import lombok.extern.slf4j.Slf4j;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

@Slf4j
class SelectTracker extends MouseAdapter {
	private ImagePanel imagePanel;
	private int selectedX1;
	private int selectedX2;
	private int selectedY1;
	private int selectedY2;

	@Override
	public void mousePressed(MouseEvent e) {
		selectedX1 = selectedX2 = e.getX();
		selectedY1 = selectedY2 = e.getY();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		selectedX1 = e.getX();
		selectedY2 = e.getY();

		imagePanel.markRange(selectedX1, selectedY1, selectedX2, selectedY2);
	}

	public void start(ImagePanel panel) {
		if (panel == imagePanel) {
			return;
		}
		stop();
		this.imagePanel = panel;
		panel.addMouseListener(this);
		panel.addMouseMotionListener(this);
	}

	public void stop() {
		if (imagePanel != null) {
			imagePanel.removeMouseListener(this);
			imagePanel.removeMouseMotionListener(this);
			imagePanel = null;
			selectedX1 = selectedY1 = selectedX2 = selectedY2 = 0;
		}
	}
}
