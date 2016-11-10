package jn.cropper.gui;

import lombok.extern.slf4j.Slf4j;

import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

@SuppressWarnings("NonSerializableFieldInSerializableClass") // Why The Fuck are JPanels serializable?
@Slf4j
class ImagePanel extends JPanel {

	private BufferedImage image;
	private final AffineTransform transformation = new AffineTransform();
	private double baseScale;
	private Rectangle marked;

	public ImagePanel() {
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				synchronized (transformation) {
					double newBaseScale = calcBaseScale();
					double currentCustomScale = transformation.getScaleX();
					double newCustomScale = currentCustomScale / baseScale * newBaseScale;
					transformation.setToScale(newCustomScale, newCustomScale);
					baseScale = newBaseScale;
				}
			}
		});
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (image != null) {
			int myWidth = getWidth();
			int myHeight = getHeight();
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, myWidth, myHeight);
			synchronized (transformation) {
				((Graphics2D) g).drawImage(image, transformation, this);
			}
		}
		if (marked != null) {
			g.setColor(new Color(255, 255, 255, 128));
			g.fillRect(0, 0, getWidth(), marked.y);
			g.fillRect(0, marked.y + marked.height, getWidth(), getHeight() - marked.y + marked.height);
			g.fillRect(0, marked.y, marked.x, marked.height);
			g.fillRect(marked.x + marked.width, marked.y, getWidth() - marked.x - marked.width, marked.height);
//			g.fillRect(marked.x, 0, getWidth() - marked.x, marked.y);
//			g.fillRect(marked.x + marked.width, marked.y, getWidth() - marked.width, getHeight() - marked.y);
//			g.fillRect(0, marked.y, getWidth() - marked.x - marked.width, getHeight() - marked.y - marked.height);
			g.setColor(Color.GREEN);
			g.drawRect(marked.x, marked.y, marked.width, marked.height);
		}
	}

	@Override
	public Dimension getPreferredSize() {
		if (image != null) {
			return new Dimension(image.getWidth(), image.getHeight());
		} else {
			return super.getPreferredSize();
		}
	}

	void rotate(int x, int y, double radian) {
		try {
			Point focus = new Point(x, y);
			synchronized (transformation) {
				Point2D anchor = transformation.inverseTransform(focus, null);
				transformation.rotate(radian, anchor.getX(), anchor.getY());
			}
		} catch (NoninvertibleTransformException e) {
			log.error("trouble rotating", e);
		}
		repaint();
	}

	void setImage(BufferedImage image) {
		this.image = image;
		synchronized (transformation) {
			baseScale = calcBaseScale();
			transformation.setToIdentity();
			transformation.setToScale(baseScale, baseScale);
			marked = null;
		}
		repaint();
	}

	private double calcBaseScale() {
		if (image == null) {
			return 1;
		}
		return Math.min(getWidth() / (double) image.getWidth(), getHeight() / (double) image.getHeight());
	}

	public void zoom(int x, int y, double rate) {
		try {
			Point focus = new Point(x, y);
			synchronized (transformation) {
				Point2D before = transformation.inverseTransform(focus, null);
				transformation.scale(rate, rate);
				Point2D after = transformation.inverseTransform(focus, null);
				transformation.translate(after.getX() - before.getX(), after.getY() - before.getY());
			}
			repaint();
		} catch (NoninvertibleTransformException e) {
			log.error("trouble zooming", e);
		}
	}

	public void pan(int fromX, int fromY, int toX, int toY) {
		try {
			synchronized (transformation) {
				Point2D from = transformation.inverseTransform(new Point(fromX, fromY), null);
				Point2D to = transformation.inverseTransform(new Point(toX, toY), null);
				transformation.translate(to.getX() - from.getX(), to.getY() - from.getY());
			}
			repaint();
		} catch (NoninvertibleTransformException e) {
			log.error("trouble panning", e);
		}
	}

	public void markRange(int x1, int y1, int x2, int y2) {
		marked = new Rectangle(
				Math.min(x1, x2), Math.min(y1, y2),
				Math.abs(x1 - x2), Math.abs(y1 - y2));
		repaint();
	}

	public BufferedImage getSelectedRegion() {
		Rectangle region = marked == null ? new Rectangle(0, 0, getWidth(), getHeight()) : marked;
		synchronized (transformation) {
			try {
				Point2D origin = transformation.inverseTransform(new Point(0, 0), null);
				Point2D ol = transformation.inverseTransform(new Point(region.x, region.y), null);
				Point2D or = transformation.inverseTransform(new Point(region.x + region.width, region.y), null);
				Point2D ur = transformation.inverseTransform(new Point(region.x + region.width, region.y + region.height), null);

				AffineTransform saveTransform = new AffineTransform(transformation);
				saveTransform.translate(origin.getX(), origin.getY());
				double inverseScale = 1 / saveTransform.getScaleX();
				saveTransform.scale(inverseScale, inverseScale);
				saveTransform.translate(- ol.getX(), - ol.getY());
				BufferedImage result = new BufferedImage((int) dist(ol, or), (int) dist(or, ur), image.getType());

				Graphics graphics = result.getGraphics();
				graphics.setColor(Color.CYAN);
				graphics.fillRect(0, 0, result.getWidth(), result.getHeight());
				((Graphics2D) graphics).drawImage(image, saveTransform, null);
				return result;
			} catch (NoninvertibleTransformException e) {
				throw new RuntimeException("trouble saving region", e);
			}
		}
	}

	private static double dist(Point2D p1, Point2D p2) {
		return Math.sqrt(Math.pow(p1.getX() - p2.getX(), 2) + Math.pow(p1.getY() - p2.getY(), 2));
	}

	public void clearSelection() {
		marked = null;
		repaint();
	}
}