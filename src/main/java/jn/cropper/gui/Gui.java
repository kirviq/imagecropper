package jn.cropper.gui;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.image.BufferedImage;

@Slf4j
public class Gui {
	private static final int START_WIDTH = 600;
	private static final int START_HEIGHT = 400;

	private final ImagePanel imagePanel;
	private final RotateTracker rotateTracker = new RotateTracker();
	private final PanTracker panTracker = new PanTracker();
	private final ZoomTracker zoomTracker = new ZoomTracker();
	private final SelectTracker selectTracker = new SelectTracker();
	private final JLabel messageBar;

	@Builder
	public Gui(Command showNext, FileSaver saver) {
		BorderLayout layout = new BorderLayout();
		JPanel root = new JPanel(layout);

		{ // image area
			imagePanel = new ImagePanel();
			root.add(imagePanel, BorderLayout.CENTER);
			root.setMinimumSize(max(root.getMinimumSize(), new Dimension(100, 100)));
		}
		{ // panel with buttons
			Container panel = new Box(BoxLayout.X_AXIS);
			{ // actions
				addButton(panel, "save", () -> {
					saveArea(saver);
					imagePanel.clearSelection();
				});
				addButton(panel, "save and next", () -> { saveArea(saver); showNext.trigger(this); });
				addButton(panel, "next", () -> showNext.trigger(this));
			}
			{ // states
				new RadioBuilder(panel)
						.addButton("Pan", this::registerPan, 'p')
						.addButton("Rotate", this::registerRotate, 'r')
						.addButton("Zoom", this::registerZoom, 'z')
						.addButton("Select", this::registerSelect, 's');
			}
//			panel.add(rotate);
			root.add(panel, BorderLayout.NORTH);
			root.setMinimumSize(max(root.getMinimumSize(), panel.getPreferredSize()));
		}
		{ // message area
			messageBar = new JLabel();
			root.add(messageBar, BorderLayout.SOUTH);
		}
		{ // put everything in a frame
			JFrame frame = new JFrame("Image Cropper");
			frame.setContentPane(root);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(START_WIDTH, START_HEIGHT);
			frame.setMinimumSize(root.getMinimumSize());
			frame.setVisible(true);
		}

		{ // zooming
			imagePanel.addMouseWheelListener(e ->
					imagePanel.zoom(e.getX(), e.getY(), e.getWheelRotation() < 0 ? 2 : 0.5));
		}
	}

	private void saveArea(FileSaver saver) {
		if (saver != null) {
			saver.save(this, imagePanel.getSelectedRegion());
		}
	}

	private void registerPan() {
		synchronized (imagePanel) {
			rotateTracker.stop();
			zoomTracker.stop();
			selectTracker.stop();
			panTracker.start(imagePanel);
		}
	}
	private void registerRotate() {
		synchronized (imagePanel) {
			zoomTracker.stop();
			panTracker.stop();
			selectTracker.stop();
			rotateTracker.start(imagePanel);
		}
	}
	private void registerZoom() {
		synchronized (imagePanel) {
			panTracker.stop();
			rotateTracker.stop();
			selectTracker.stop();
			zoomTracker.start(imagePanel);
		}
	}
	private void registerSelect() {
		synchronized (imagePanel) {
			panTracker.stop();
			rotateTracker.stop();
			zoomTracker.stop();
			selectTracker.start(imagePanel);
		}
	}

	private Dimension max(Dimension s1, Dimension s2) {
		return new Dimension(Math.max(s1.width, s2.width), Math.max(s1.height, s2.height));
	}

	private void addButton(Container panel, String text, Runnable action) {
		JButton skip = new JButton(text);
		if (action != null) {
			skip.addActionListener(e -> action.run());
		}
		panel.add(skip);

//		Toolkit.getDefaultToolkit().addAWTEventListener(o -> {
//			if (o instanceof KeyEvent) {
//				KeyEvent e = (KeyEvent) o;
//				if (e.getKeyChar() == hotkey) {
//					switch (e.getID()) {
//						case KeyEvent.KEY_PRESSED:
//							button.setSelected(true);
//							button.getAction().actionPerformed(null);
//							break;
//						case KeyEvent.KEY_RELEASED:
//							defaultButton.setSelected(true);
//							defaultButton.getAction().actionPerformed(null);
//							break;
//					}
//				}
//			}
//		}, AWTEvent.KEY_EVENT_MASK);
	}

	public void showText(String text) {
		messageBar.setText(text);
	}

	public void setImage(BufferedImage image) {
		imagePanel.setImage(image);
	}
}
