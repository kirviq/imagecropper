package jn.cropper.gui;

import java.awt.image.BufferedImage;

@FunctionalInterface
public interface FileSaver {
	void save(Gui gui, BufferedImage image);
}
