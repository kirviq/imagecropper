package jn.cropper.gui;

import lombok.extern.slf4j.Slf4j;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

@Slf4j
class ZoomTracker extends MouseAdapter {
	private ImagePanel imagePanel;
	private int startX;
	private int startY;
	private double alreadyZoomed;

	@Override
	public void mousePressed(MouseEvent e) {
		startX = e.getX();
		startY = e.getY();
		alreadyZoomed = 1;
		log.info("zooming from {} / {}", startX, startY);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		double dist = dist(startX, startY, e.getX(), e.getY());
		double newZoom = 1 + dist * 0.1;
		imagePanel.zoom(imagePanel.getWidth() / 2, imagePanel.getHeight() / 2, newZoom / alreadyZoomed);log.info("zoom to {}", newZoom);
		alreadyZoomed = newZoom;
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
			startX = startY = 0;
		}
	}

	private static double dist(double x1, double y1, double x2, double y2) {
		return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
	}
}
