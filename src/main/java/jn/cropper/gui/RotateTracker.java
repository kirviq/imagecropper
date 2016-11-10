package jn.cropper.gui;

import lombok.extern.slf4j.Slf4j;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

@Slf4j
class RotateTracker extends MouseAdapter {
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
		int centerX = imagePanel.getWidth() / 2;
		int centerY = imagePanel.getHeight() / 2;

		double a = dist(lastX, lastY, newX, newY);
		double b = dist(centerX, centerY, lastX, lastY);
		double c = dist(centerX, centerY, newX, newY);
		double angle = Math.acos(((Math.pow(b, 2) + Math.pow(c, 2) - Math.pow(a, 2)) / (2 * b * c)));
		angle = angle * getSign(newX, newY, centerX, centerY);
		imagePanel.rotate(centerX, centerY, angle);
		lastX = newX;
		lastY = newY;
	}

	private double getSign(int nx, int ny, int cx, int cy) {
		int x = nx - cx;
		int y = ny - cy;

		boolean upperHalf = y < 0;
		boolean centerPart = Math.abs(x) < Math.abs(y);
		boolean leftHalf = x < 0;
		if (upperHalf && centerPart) {
			return nx < lastX ? -1 : 1;
		} else if (!upperHalf && centerPart) {
			return nx > lastX ? -1 : 1;
		} else if (leftHalf){
			return ny < lastY ? 1 : -1;
		} else {
			return ny > lastY ? 1 : -1;
		}
	}


	private static double dist(double x1, double y1, double x2, double y2) {
		return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
	}

	public void start(ImagePanel panel) {
		if (imagePanel == panel) {
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
