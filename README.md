# imagecropper
I needed a tiny tool to rotate and crop lots of images to train opencv.

Usage:
* check out repo
* mvn clean package
* start jar-with-dependencies
* pick root directory with source images (directory will be traversed recursively)
* pick target directory
* start cropping

Cropping:
* default mode is Pan, use the mouse to move the image
* Rotate:
   * select, or hold R key
   * click-and-drag to rotate image (center is center of window)
* Zoom:
    * either use mouse wheel OR
    * select or hold Z key
    * click and drag to zoom in (the further you drag, the closer you zoom)
* Select:
    * select or hold S key
    * click and drag to mark an area
* Save
    * save selected area (or visible area if nothing selected) to output directory. Filename will be <source image name>_crop<number>.<ext> to make name unique.
* Next
    * Load next image
* save and next
    * Exactly as the name suggests.
