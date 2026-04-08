package View;

import java.awt.Image;
import java.io.File;
import java.util.function.Consumer;

import javax.swing.ImageIcon;
import javax.swing.SwingWorker;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.process.ImageProcessor;

public class DisplayTask extends SwingWorker<ImagePlus[], Void> {
	File imageInput;
	Consumer<ImagePlus[]> postProcessConsumer;
	ImagePlus[] finishedIcons;

	public DisplayTask(File imageInput, Consumer<ImagePlus[]> postProcessConsumer) {
		this.imageInput = imageInput;
		this.postProcessConsumer = postProcessConsumer;
	}//end constructor

	@Override
	protected ImagePlus[] doInBackground() throws Exception {
		ImagePlus img = IJ.openImage(imageInput.getAbsolutePath());
		ImageProcessor imgPrc = img.getProcessor();
		imgPrc.flipHorizontal();
		img.setProcessor(imgPrc);
		IJ.run(img, "Rotate 90 Degrees Right", "");

		Roi[] rois = new Roi[4];
		rois[2] = new Roi(0, 0, img.getWidth() / 2,img.getHeight() / 2);
		rois[0] = new Roi(img.getWidth() - (img.getWidth() / 2), 0, img.getWidth() / 2, img.getHeight() / 2);
		rois[3] = new Roi(0, img.getHeight() - (img.getHeight() / 2), img.getWidth() / 2, img.getHeight() / 2);
		rois[1] = new Roi(img.getWidth() - (img.getWidth() / 2), img.getHeight() - (img.getHeight() / 2), img.getWidth() / 2, img.getHeight() / 2);
		
		ImagePlus[] imgsPlus = img.crop(rois);
		this.finishedIcons = imgsPlus;
		return imgsPlus;
	}//end doInBackground()

	@Override
	protected void done() {
		try {
			get();
			this.postProcessConsumer.accept(finishedIcons);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}//end done()
	
}
