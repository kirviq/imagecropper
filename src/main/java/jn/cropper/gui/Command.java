package jn.cropper.gui;

@FunctionalInterface
public interface Command {
	void trigger(Gui gui);
}
