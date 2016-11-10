package jn.cropper.gui;

import lombok.extern.slf4j.Slf4j;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

@Slf4j
class PanTracker extends MouseAdapter {
	private ImagePanel imagePanel;
	private int lastX;
	private int lastY;

	@Override
	public void mousePressed(MouseEvent e) {
		lastX = e.getX();
		lastY = e.getY();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		int newX = e.getX();
		int newY = e.getY();

		imagePanel.pan(lastX, lastY, newX, newY);
		lastX = newX;
		lastY = newY;
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
			lastX = lastY = 0;
		}
	}
}
