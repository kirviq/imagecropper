package jn.cropper;

import jn.cropper.gui.DirectoryPicker;
import jn.cropper.gui.Gui;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Slf4j
public class Main {
	private static final int PREREAD_BUFFER_SIZE = 2;
	private static String currentFileName;
	private static File outputDir;

	public static void main(String[] args) {
		System.setProperty("awt.useSystemAAFontSettings","on");
		System.setProperty("swing.aatext", "true");
		File baseDir = selectInputDir();
		outputDir = selectSaveDir();
		BlockingQueue<ImageWithName> files = getFiles(baseDir);
		Gui gui = Gui.builder()
				.showNext(g -> showNext(files, g))
				.saver((g, image) -> {
					if (currentFileName == null || outputDir == null) {
						return;
					}
					try {
						File file = getNewFile();
						ImageIO.write(image, "jpg", file);
						g.showText("saved as " + file.getName());
					} catch (IOException e) {
						log.error("trouble saving image", e);
						g.showText("got " + e.getClass().getName());
					}
				})
				.build();
		showNext(files, gui);
	}

	private static void showNext(BlockingQueue<ImageWithName> files, Gui g) {
		try {
			ImageWithName take = files.take();
			currentFileName = take.name;
			g.setImage(take.image);
		} catch (InterruptedException e) {
			log.error("interrupted", e);
		}
	}

	private static File getNewFile() {
		String baseName = currentFileName.substring(0, currentFileName.lastIndexOf('.')) + "_crop";
		File f = new File(outputDir, baseName + ".jpg");
		for (int i = 1; f.exists(); i++) {
			f = new File(outputDir, baseName + "_" + i + ".jpg");
		}
		return f;
	}

	private static File selectInputDir() {
		return DirectoryPicker.builder().title("Select Input Directory").build().pickFile();
	}
	private static File selectSaveDir() {
		return DirectoryPicker.builder().title("Select Target Directory").build().pickFile();
	}

	private static BlockingQueue<ImageWithName> getFiles(File baseDir) {
		BlockingQueue<ImageWithName> files = new ArrayBlockingQueue<>(PREREAD_BUFFER_SIZE);
		new Thread(() -> {
			try {
				Files.walk((baseDir == null ? new File("/opt/stuff/TD-Fotos") : baseDir).toPath())
						.map(Path::toFile)
						.filter(File::isFile)
						.filter(f -> f.getName().matches("(?i).*\\.jpe?g"))
						.map(Main::readImage)
						.forEach((e) -> {
							try {
								files.put(e);
							} catch (InterruptedException e1) {
								log.warn("interrupted");
							}
						});
				while (true) {
					files.add(new ImageWithName(imageWithText("no more images"), ""));
				}
			} catch (IOException e) {
				throw new RuntimeException("trouble reading image", e);
			}
		}).start();
		return files;
	}

	@AllArgsConstructor
	private static class ImageWithName {
		BufferedImage image;
		String name;
	}
	private static ImageWithName readImage(File file) {
		try {
			return new ImageWithName(ImageIO.read(file), file.getName());
		} catch (IOException e) {
			log.error("trouble reading image", e);
			return new ImageWithName(imageWithText("got " + e.getClass().getName()), "");
		}
	}
	private static BufferedImage imageWithText(String text) {
		BufferedImage image = new BufferedImage(170, 30, BufferedImage.TYPE_INT_RGB);
		Graphics graphics = image.getGraphics();
		graphics.setColor(Color.LIGHT_GRAY);
		graphics.fillRect(0, 0, 200, 50);
		graphics.setColor(Color.BLACK);
		graphics.setFont(new Font("Arial Black", Font.BOLD, 20));
		graphics.drawString(text, 10, 25);
		return image;
	}
}
