package IJM;

import java.io.File;

import SimpleResult.SimpleResult;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

public class IJProcess {

    /**
     * This method performs the unsharp mask filter for files scanned through twain instead of the EPSON Scan Utility.  
     * By default, this method will save the new image next to the original, with "-unsharp_sigma-[sigma]_weight-[weight]" appended to the filename.
     * @param filepath The filepath of the image to process
     * @param sigma The sigma (radius) value to use for the unsharp filter
     * @param weight The mask weight to use for the unsharp filter. It must be between 0.1 and 0.9
     * @param rename_file Whether or not we should rename the unsharp masked file.
     * @return Returns either the filepath for the resulting file, or some error.
     */
    public static SimpleResult<String> doUnsharpCorrection(String filepath, double sigma, double weight, boolean rename_file) {
        // open img and run the unsharp mask
        ImagePlus img = IJ.openImage(filepath);
        IJ.run(img, "Unsharp Mask...", "radius=" + sigma + " mask=" + weight);
        // figure out path to save img to, then save it there
        String baseDir = filepath.substring(0, filepath.lastIndexOf(File.separator) + 1);
        String baseName = filepath.substring(filepath.lastIndexOf(File.separator) + 1, filepath.lastIndexOf("."));
        String baseExt = filepath.substring(filepath.lastIndexOf("."));
        
        String newName;
        if (rename_file) {
            newName = baseName + String.format("_unsharp_sigma-[%2.1f]_weight-[%2.1f]", sigma, weight);
        } else {
            newName = baseName;
        }//end else we don't rename the file
        
        String newPath = baseDir + newName + baseExt;

        IJ.save(img, newPath);
        // return filename as string, probably
        return new SimpleResult<String>(newPath);
    }//end doUnsharpCorrection(filepath, sigma, weight)

/**
	 * Removes overly blue pixels by setting color value
	 * to 0,0,0. Mutates img parameter.
	 * @param img The input img, from which to remove blue
	 */
	public static void removeBlue(ImagePlus img) {
		ImageProcessor proc = img.getProcessor();
		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++){
				int[] pixel = img.getPixel(x,y);
				int R = pixel[0];
				int G = pixel[1];
				int B = pixel[2];
				if (B > (R + G) * 3 / 5) {
					proc.set(x,y,0);
				}//end if pixel appears blue
			}//end looping over y values
		}//end looping over x values
		img.setProcessor(proc);
	}//end removeBlue

    /**
	 * Removes pixels outside a range by setting them to 0.  
	 * Uses HSB color space for constraints.
	 * Mutates the img parameter.
	 * @param img The image to process. This parameter is mutated.
	 * @param min int[3], minimum value (0-255) for H,S,B
	 * @param max int[3], maximum value (0-255) for H,S,B
	 * @param filter PassOrNot[3], for H,S,B, either "pass", or inverts
	 */
	public static void colorThHSB(ImagePlus img, int[] min, int[] max, PassOrNot[] filter) {
		ColorProcessor prc = img.getProcessor().convertToColorProcessor();
		ImageStack hsb = prc.getHSBStack();
		ImageProcessor h = hsb.getProcessor(1);
		ImageProcessor s = hsb.getProcessor(2);
		ImageProcessor b = hsb.getProcessor(3);
		for (int x = 0; x < prc.getWidth(); x++) {
			for (int y = 0; y < prc.getHeight(); y++) {
				int H = h.getPixel(x,y);
				int S = s.getPixel(x,y);
				int B = b.getPixel(x,y);
				boolean hin = H >= min[0] && H <= max[0];
				boolean sin = S >= min[1] && S <= max[1];
				boolean bin = B >= min[2] && B <= max[2];
				if (filter[0] != PassOrNot.Pass) {hin = !hin;}
				if (filter[1] != PassOrNot.Pass) {sin = !sin;}
				if (filter[2] != PassOrNot.Pass) {bin = !bin;}
				if (!hin || !sin || !bin) {
					prc.putPixel(x, y, new int[] {0,0,0});
					// prc.set(x,y,0);
				}//end if pixel is outside constraints
			}//end looping over y coords for pixels
		}//end looping over x coords for pixels
		img.setProcessor(prc);
	}//end colorThHSB

	/**
	 * Removes pixels outside a range by setting them to 0.  
	 * Uses HSB color space for constraints.
	 * Mutates the img parameter.
	 * @param img The image to process. This parameter is mutated.
	 * @param min int[3], minimum value (0-255) for H,S,B
	 * @param max int[3], maximum value (0-255) for H,S,B
	 * @param filter boolean[3], for H,S,B, true indicates to cut out pixels outside threshold, false is reverse for that channel.
	 * @param flipThreshold If this is true, then the thresholded region is flipped to be whatever area is not covered by the thresholds given.
	 */
	public static void colorThHSB(ImagePlus img, int[] min, int[] max, boolean[] filter, boolean flipThreshold) {
		ColorProcessor prc = img.getProcessor().convertToColorProcessor();
		ImageStack hsb = prc.getHSBStack();
		ImageProcessor h = hsb.getProcessor(1);
		ImageProcessor s = hsb.getProcessor(2);
		ImageProcessor b = hsb.getProcessor(3);
		for (int x = 0; x < prc.getWidth(); x++) {
			for (int y = 0; y < prc.getHeight(); y++) {
				int H = h.getPixel(x,y);
				int S = s.getPixel(x,y);
				int B = b.getPixel(x,y);
				boolean hin = H >= min[0] && H <= max[0];
				boolean sin = S >= min[1] && S <= max[1];
				boolean bin = B >= min[2] && B <= max[2];
				if (!filter[0]) {hin = !hin;}
				if (!filter[1]) {sin = !sin;}
				if (!filter[2]) {bin = !bin;}
				if (!hin || !sin || !bin) {
					if (!flipThreshold) {
						prc.set(x,y,0);
					}
				}//end if pixel is outside constraints
				else {
					if (flipThreshold) {
						prc.set(x,y,0);
					}
				}
			}//end looping over y coords for pixels
		}//end looping over x coords for pixels
		img.setProcessor(prc);
	}//end colorThHSB

	public enum PassOrNot {
		Pass,
		Stop,
	}//end enum PassOrNot

	/**
	 * Converts rgb value to CIEL*a*b* using conversion formulas
	 * originally taken from https://www.easyrgb.com/en/math.php.
	 * @param r red value of pixel
	 * @param g green value of pixel
	 * @param b blue value of pixel
	 * @return Returns array with [L*,a*,b*]
	 */
	public static double[] convertRgbToLab(int r, int g, int b) {
		/*
		 * Conversion formulas taken from https://www.easyrgb.com/en/math.php
		 */
		// convert to XYZ for this pixel
		double sR = ((double)r / 255);
		double sG = ((double)g / 255);
		double sB = ((double)b / 255);
		if (sR > 0.04045) {sR = Math.pow((sR + 0.055) / 1.055, 2.4);}
		else {sR = sR / 12.92;}
		if (sG > 0.04045) {sG = Math.pow((sG + 0.055) / 1.055, 2.4);}
		else {sG = sG / 12.92;}
		if (sB > 0.04045) {sB = Math.pow((sB + 0.055) / 1.055, 2.4);}
		else {sB = sB / 12.92;}
		sR = sR * 100;
		sG = sG * 100;
		sB = sB * 100;
		double X = sR * 0.4124 + sG * 0.3576 + sB * 0.1805;
		double Y = sR * 0.2126 + sG * 0.7152 + sB * 0.0722;
		double Z = sR * 0.0193 + sG * 0.1192 + sB * 0.9505;
		// convert from XYZ to Lab based on equal observer
		double varX = X / 100;
		double varY = Y / 100;
		double varZ = Z / 100;
		if (varX > 0.008856) {varX = Math.pow(varX, 1.0/3.0);}
		else {varX = (7.787 * varX) + (16.0 / 116.0);}
		if (varY > 0.008856) {varY = Math.pow(varY, 1.0/3.0);}
		else {varY = (7.787 * varY) + (16.0 / 116.0);}
		if (varZ > 0.008856) {varZ = Math.pow(varZ, 1.0/3.0);}
		else {varZ = (7.787 * varZ) + (16.0 / 116.0);}
		double CIE_L = (116 * varY) - 16;
		double CIE_a = 500 * (varX - varY);
		double CIE_b = 200 * (varY - varZ);
		// return the values
		return new double[] {CIE_L,CIE_a,CIE_b};
	}//end convertRgbToLab(r,b,g)

	/**
	 * Normalizes lab from l [0 - 100] ab [-128 - 127]
	 * to lab [0 - 255].
	 * @param lab double[3], [L*,a*,b*]
	 */
	public static void normalizeLab(double[] lab) {
		lab[0] = (255 * lab[0]) / 100;
		lab[1] = lab[1] + 128;
		lab[2] = lab[2] + 128;
	}//end normalizeLab()

    /**
	 * Removes pixels outside a range by setting them to 0.  
	 * Uses RGB color space for constraints.
	 * Mutates the img parameter.
	 * @param img The image to process. This parameter is mutated.
	 * @param min int[3], minimum value (0-255) for R,G,B
	 * @param max int[3], maximum value (0-255) for R,G,B
	 * @param filter boolean[3], for R,G,B, true indicates to cut out pixels outside the threshold, false does the reverse for that channel
	 * @param flipThreshold If this is true, then the thresholded region is flipped to be whatever area is not covered by the thresholds given.
	 */
	public static void colorThRGB(ImagePlus img, int[] min, int[] max, boolean[] filter, boolean flipThreshold) {
		ImageProcessor prc = img.getProcessor();
		for (int x = 0; x < prc.getWidth(); x++) {
			for (int y = 0; y < prc.getHeight(); y++) {
				int[] rgb = prc.getPixel(x, y, null);
				int r = rgb[0];
				int g = rgb[1];
				int b = rgb[2];
				boolean rin = r >= min[0] && r <= max[0];
				boolean gin = g >= min[1] && g <= max[1];
				boolean bin = b >= min[2] && b <= max[2];
				if (!filter[0]) {rin = !rin;}
				if (!filter[1]) {gin = !gin;}
				if (!filter[2]) {bin = !bin;}
				if (!rin || !gin || !bin) {
					if (!flipThreshold) {
						prc.set(x,y,0);
					}
				}//end if pixel is outside constraints
				else {
					if (flipThreshold) {
						prc.set(x,y,0);
					}
				}
			}//end looping over y coords for pixels
		}//end looping over x coords for pixels
		img.setProcessor(prc);
	}//end colorThRGB
}//end class IJProcess
