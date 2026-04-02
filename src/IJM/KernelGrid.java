package IJM;

import java.awt.Color;
import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.dhatim.fastexcel.Worksheet;

import Config.OutputConfig;
import Config.ProcessingConfig;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.plugin.ImageCalculator;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;

public class KernelGrid {
	/**
	 * Collection of kernels,
	 * each kernel needs the following data:
	 *  - actual image (ImagePlus)
	 *  - original filepath of the image it came from
	 *  - positioning data:
	 *      - quadrant (AA vs BB)
	 *      - phase 1 positioning (AA05 or BB47)
	 *      - phase 2 positioning (AA05;1 or AA05;2)
	 *      - phase 3 positioning (AA05;1;1 or AA05;1;2)
	 *  - flag information (mostly just whether it has failed the latest phase of processing)
	 *  - roi information (needs to be array due to )
	 */

	/**
	 * File holding path to the original full, unmodified image that we're getting information from
	 */
	File originalImageFile;
	/**
	 * The image of the full grid, already flipped and rotated in the constructor
	 */
	ImagePlus fullImage;

	/**
	 * Roi relative to fullImage, initialized by phase1FindQuadrants.
	 * Should be in order of AA,BB,CC,DD
	 */
	Roi[] quadRois;

	/**
	 * list of all the kernel entries, initialized around the end of phase 1.
	 * Due to the way KernelEntry.discoveredComponents is meant to function,
	 * with each KernelEntry technically being a tree, kernelEntries is
	 * technically a forest.
	 */
	// List<KernelEntry> kernelEntries = new ArrayList<KernelEntry>();

	List<KernelEntry> phase1Singletons = null;

	List<KernelEntry> phase1Merged = null;

	/**
	 * Not duplicated from phase1Singletons
	 */
	List<KernelEntry> phase2Singletons = null;

	List<KernelEntry> phase2Merged = null;

	/**
	 * Not duplicated from phase2Singletons
	 */
	List<KernelEntry> phase3Singletons = null;

	List<KernelEntry> phase3Merged = null;

	/**
	 * Constructs a KernelGrid, setting up certain variables and prepping things for later processing.
	 * @param imgFile The image file to process
	 */
	public KernelGrid(File imgFile, OutputConfig outConf) {
		// open and save reference to the image, make sure it's flipped and rotated
		this.originalImageFile = imgFile;
		System.out.println("Adding file " + imgFile.getName() + " to a kernel grid for processing.");
		ImagePlus image = IJ.openImage(imgFile.getAbsolutePath());
		if (outConf.delete_tape) {
			deleteTape(image);
		}//end if we should delete the tape in the corner of the image
		ImageProcessor prc = image.getProcessor();
		prc.flipHorizontal();
		image.setProcessor(prc);
		IJ.run(image, "Rotate 90 Degrees Right", "");

		this.fullImage = image;
	}//end constructor

	/**
	 * Temporary function which uses hard-coded coordinates to delete the tape in the upper right of quadrant AA
	 * @param image
	 */
	protected void deleteTape(ImagePlus image) {
		ImageProcessor prc = image.getProcessor();
		for (int x = 2223; x < image.getWidth(); x++) {
			for (int y = 0; y < Math.min(image.getHeight(), 300); y++) {
				prc.putPixel(x,y,0);
			}//end looping over y values
		}//end looping over x values
	}//end deleteTape()

	protected RoiManager getKernels(
		ImagePlus image, String imgPrefix, String quadrantCode, OutputConfig outConf, ProcessingConfig procConf,
		boolean save_imgs, boolean save_results, boolean reset_results, boolean make_entries
	) {
		ImagePlus img = image.duplicate();
		// try to get rid of the scanner border
		IJProcess.colorThRGB(
			img,
			new int[] {procConf.phase1Thresh1_R_Min,procConf.phase1Thresh1_G_Min,procConf.phase1Thresh1_B_Min},
			new int[] {procConf.phase1Thresh1_R_Max,procConf.phase1Thresh1_G_Max,procConf.phase1Thresh1_B_Max},
			new boolean[] {procConf.phase1Thresh1_R_Pass,procConf.phase1Thresh1_G_Pass,procConf.phase1Thresh1_B_Pass},
			procConf.phase1Thresh1FlipThreshold
		);
		// try to get just the whole kernels
		IJProcess.colorThHSB(
			img, 
			new int[] {procConf.phase1Thresh2_H_Min,procConf.phase1Thresh2_S_Min,procConf.phase1Thresh2_B_Min},
			new int[] {procConf.phase1Thresh2_H_Max,procConf.phase1Thresh2_S_Max,procConf.phase1Thresh2_B_Max},
			new boolean[] {procConf.phase1Thresh2_H_Pass,procConf.phase1Thresh2_S_Pass,procConf.phase1Thresh2_B_Pass},
			procConf.phase1Thresh2FlipThreshold
		);
		
		// set up results, roi, particle analysis to try for whole kernels
		ResultsTable rt = new ResultsTable();
		RoiManager rm = AnalyzeParticles.analyzeParticles(img, procConf.phase1Measurements, procConf.phase1ParticlesParam);
		rt = ResultsTable.getResultsTable();

		if (outConf.print_roi_counts) {
			System.out.println("Detected " + rt.size() + " particles");
			System.out.println("Roi Manager had " + rm.getCount() + " rois");
		}//end if we should print how many particles/rois were found


		// start trying to do stuff based on the particles we found
		if (save_imgs || save_results || make_entries) {

			// get directories in order
			File path_start = new File(outConf.path_base,"data\\" + imgPrefix + "\\" + quadrantCode);
			File sng_path = new File(path_start, "singleton-11");
			File mrg_path = new File(path_start, "merged-22");
			if (outConf.save_imgs_gen) {
				if (!sng_path.exists()) {sng_path.mkdirs();}
				if (!mrg_path.exists()) {mrg_path.mkdirs();}
			}//end if we should be saving images

			KernelEntry.Quadrant quad;
			if (quadrantCode.equals("AA")) {quad = KernelEntry.Quadrant.AA;}
			else if (quadrantCode.equals("BB")) {quad = KernelEntry.Quadrant.BB;}
			else if (quadrantCode.equals("CC")) {quad = KernelEntry.Quadrant.CC;}
			else if (quadrantCode.equals("DD")) {quad = KernelEntry.Quadrant.DD;}
			else {quad = null;}

			if (save_imgs || make_entries) {
				// get columns with data for determining merged or not
				Roi[] roiArray = rm.getRoisAsArray();
				int aIdx = rt.getColumnIndex("Area");
				int rIdx = rt.getColumnIndex("Round");
				int fIdx = rt.getColumnIndex("Feret");
				int faIdx = rt.getColumnIndex("FeretAngle");
				int xIdx = rt.getColumnIndex("X");
				int yIdx = rt.getColumnIndex("Y");
				for (int i = 0; i < rt.size(); i++) {
					double area = rt.getValueAsDouble(aIdx, i);
					double round = rt.getValueAsDouble(rIdx, i);
					double feret = rt.getValueAsDouble(fIdx, i);
					double angle = rt.getValueAsDouble(faIdx, i);
					ImagePlus cleaned;
					if (isMerged(procConf, round, feret)) {
						cleaned = Durum.cleanCroppedRoi(outConf.rotate_merged_kern_imgs, image, roiArray[i], angle);
						// if (outConf.rotate_merged_kern_imgs) {cleaned = Durum.cleanCroppedRoi(image, roiArray[i], angle);}
						// else {cleaned = Durum.cleanCroppedRoi(image, roiArray[i], 0);}
					}else {
						cleaned = Durum.cleanCroppedRoi(outConf.rotate_singleton_kern_imgs, image, roiArray[i], angle);
						// if (outConf.rotate_singleton_kern_imgs) {cleaned = Durum.cleanCroppedRoi(image, roiArray[i], angle);}
						// else {cleaned = Durum.cleanCroppedRoi(image, roiArray[i], 0);}
					}//end else we have a singleton image
					if (make_entries) {
						KernelEntry kern = new KernelEntry(originalImageFile, quad, i+1, roiArray[i], cleaned);
						kern.kernelArea = area;
						kern.x = (int)rt.getValueAsDouble(xIdx, i);
						kern.y = (int)rt.getValueAsDouble(yIdx, i);
						if (isMerged(procConf, round, feret)) {
							this.phase1Merged.add(kern);
						}//end if the kernel is considered merged, flag it as such
						else {
							this.phase1Singletons.add(kern);
						}//end if this kernel is considered a singleton
					}//end if we need to be getting KernelEntries
					// feret length for 300 dpi
					if (save_imgs) {
						String path_begin;
						String path_mid = imgPrefix;
						String path_mid2;
						String path_end = quadrantCode + (i+1) + ".tif";
						if (isMerged(procConf, round, feret)) {
							path_begin = "merged-22\\";
							path_mid2 = "-mrg-";
						} else {
							path_begin = "singleton-11\\";
							path_mid2 = "-sng-";
						}
						File tmpFile = new File(path_start, path_begin + path_mid + path_mid2 + path_end);
						IJ.save(cleaned, tmpFile.getAbsolutePath());
					}//end if we should be saving these images
				}//end looping over each line in the results table
			}//end if we're going to save images from the rois

			if (save_results && save_imgs) {
				rt.save(new File(path_start, imgPrefix + "-" + quadrantCode + "-phase1-kernel-results.csv").getAbsolutePath());
			}//end if results should be saved before reset
			
		}//end if we want to do additional stuff with the results

		if (reset_results) {
			rt.reset();
		}//end if results should be reset between quads
		
		return rm;
	}//end getKernels()

	public void phase1FindQuadrants(OutputConfig outConf, ProcessingConfig procConf) {
		ImagePlus img = fullImage.duplicate();

		phase1Merged = new ArrayList<>();
		phase1Singletons = new ArrayList<>();

		// just use naive split method, halve overall dimensions
		Roi[] quads = new Roi[4];
		quads[2] = new Roi(0, 0, img.getWidth() / 2,img.getHeight() / 2);
		quads[0] = new Roi(img.getWidth() - (img.getWidth() / 2), 0, img.getWidth() / 2, img.getHeight() / 2);
		quads[3] = new Roi(0, img.getHeight() - (img.getHeight() / 2), img.getWidth() / 2, img.getHeight() / 2);
		quads[1] = new Roi(img.getWidth() - (img.getWidth() / 2), img.getHeight() - (img.getHeight() / 2), img.getWidth() / 2, img.getHeight() / 2);
		this.quadRois = quads;

		ImagePlus[] quadImgs = img.crop(quads);
		int runningSingletonTotalArea = 0;
		int runningSingletonCount = 0;
		for (int q = 0; q < quadImgs.length; q++) {
			String letter = ((char)(65 + q)) + "";
			if (outConf.print_kern_estimates) {
				System.out.println("\nQuadrant " + letter + " Row Counts:");
			}//end if we should print a little header for upcoming kernel count estimates

			String prefix = this.originalImageFile.getName().replace(".tif", "");

			if (outConf.save_imgs_gen) {
				File dir = new File(outConf.path_base, "data\\" + prefix + "\\");
				if (!dir.exists()) {dir.mkdirs();}
				File path = new File(dir, letter + letter + ".tif");
				
				IJ.save(quadImgs[q], path.getAbsolutePath());
			}//end if we should be saving images of the 4 quadrants we're splitting things into

			// quadKerns is roiManager gotten from running quadrant through getKernels1
			RoiManager quadKerns = getKernels(quadImgs[q], prefix, letter + letter, outConf, procConf, outConf.save_imgs_gen, outConf.save_particle_results, false, true);
			ResultsTable rt = ResultsTable.getResultsTable();

			if (rt.size() == 0) {continue;}

			double[] roundCol = rt.getColumn("Round");
			double[] areaCol = rt.getColumn("Area");
			double[] feretCol = rt.getColumn("Feret");

			if (outConf.print_kern_estimates) {
				Roi[] roiarray = quadKerns.getRoisAsArray();
				int curRow = 1;
				int rowTol = 50;
				int singletonCount = 0;
				int mergedCount = 0;
				int suspectedKernelCount = 0;
				List<Integer> rowCounts = new ArrayList<Integer>();
				for (int i = 0; i < roiarray.length; i++) {
					// figure out Y positions of cur and last roi
					double n2Y = roiarray[i].getContourCentroid()[1];
					double n1Y;
					if (i == 0) {n1Y = n2Y;} else {n1Y = roiarray[i-1].getContourCentroid()[1];}
					// if cur is  anew row, file off info for the last row
					if (n2Y - n1Y > rowTol) {
						rowCounts.add(singletonCount + mergedCount);
						System.out.println("Row " + curRow + " has " + singletonCount + " singleton particles and " + mergedCount + " merged particles. The suspected total kernel count is " + (singletonCount + suspectedKernelCount) + ".");
						curRow++;
						singletonCount = 0;
						mergedCount = 0;
						suspectedKernelCount = 0;
					}//end if we're on a new row
					// general counting/handling for cur roi
					double round = roundCol[i];
					double feret = feretCol[i];
					double area = areaCol[i];
					if (isMerged(procConf, round, feret)) {
						mergedCount++;
						double per = area / outConf.avg_kernel_area;
						per = Math.round(per);
						suspectedKernelCount += per;
					} else {
						singletonCount++;
						runningSingletonTotalArea += area;
						runningSingletonCount++;
					}//end else we have a singleton
					// make sure to save row info for the last row
					if (i == roiarray.length - 1) {
						rowCounts.add(singletonCount + mergedCount);
						System.out.println("Row " + curRow + " has " + singletonCount + " singleton particles and " + mergedCount + " merged particles. The suspected total kernel count is " + (singletonCount + suspectedKernelCount) + ".");
					}//end if we're on the last roi
				}//end counting rois in rows
			}//end if we should print kernel estimates
			if (outConf.print_kern_estimates) {
				int singletonAverage = runningSingletonTotalArea / runningSingletonCount;
				System.out.println("Average singleton kernel area for this image is " + singletonAverage + "\n\n");
			}//end if we should print kernel estimates

			quadKerns.reset();
			rt.reset();
		}//end looping over each quadrant

	}//end phase1FindQuadrants()

	public void phase2WatershedMerged(OutputConfig outConf, ProcessingConfig procConf) {
		phase2Merged = new ArrayList<>();
		phase2Singletons = new ArrayList<>();

		for (int i = 0; i < this.phase1Merged.size(); i++) {
			KernelEntry thisEntry = phase1Merged.get(i);
			// actually get into processing
			ImagePlus curImg = phase1Merged.get(i).cleanedImage;
			// File path_base = new File(conf.path_base, (i+1) + "debug2.tif");
			// IJ.save(curImg, path_base.getAbsolutePath());

			ImagePlus curImgDup1 = curImg.duplicate();

			IJ.run(curImgDup1,"Gaussian Blur...","sigma=1");

			IJ.run(curImgDup1, "Convert to Mask", "");
			
			IJ.run(curImgDup1, "Watershed", "");

			// use image calculator to put original image + watershedded mask to get watershedded original
			ImageCalculator ic = new ImageCalculator();
			ImagePlus watershedded = ic.run("AND create", curImg, curImgDup1);

			// send current image to particle analysis to get the watershedded rois
			ResultsTable rt = new ResultsTable();

			RoiManager rm = AnalyzeParticles.analyzeParticles(watershedded, procConf.phase2Measurements, procConf.phase2ParticlesParam);
			rt = ResultsTable.getResultsTable();

			if (outConf.print_roi_counts) {
				System.out.println("Counts for phase 2 particle " + (i+1) + ": " + rt.size() + " rm:" + rm.getCount());
			}//end if we should be printing roi counts

			// for each particle, either get correct kernels or flag for third pass
			int aColIdx = rt.getColumnIndex("Area");
			int sColIdx = rt.getColumnIndex("Solidity");
			int fColIdx = rt.getColumnIndex("Feret");
			int miColIdx = rt.getColumnIndex("Minor");
			int maColIdx = rt.getColumnIndex("Major");
			int faColIdx = rt.getColumnIndex("FeretAngle");
			int xColIdx = rt.getColumnIndex("X");
			int yColIdx = rt.getColumnIndex("Y");
			int mergedCount = 0;
			int splitCount = 0;
			int trueCount = 0;
			for (int j = 0; j < rt.size(); j++) {
				boolean isMerged = isProbMerged(procConf, rt.getValueAsDouble(aColIdx, j), rt.getValueAsDouble(sColIdx, j));
				boolean isSplit = isProbSplit(procConf, rt.getValueAsDouble(aColIdx, j), rt.getValueAsDouble(fColIdx, j), rt.getValueAsDouble(sColIdx, j), rt.getValueAsDouble(miColIdx, j), rt.getValueAsDouble(maColIdx, j));
				if (isMerged) {mergedCount++;}
				else if (isSplit) {splitCount++;}
				else {trueCount++;}
			}//end looping over each of the particles detected after watershed
			// get path information that will be useful either way
			String prefix = thisEntry.originalFile.getName().replace(".tif", "");
			String quadrantCode = thisEntry.quadrant.name();
			File path_start = new File(outConf.path_base, "data\\" + prefix + "\\" + quadrantCode + "\\merged-22\\");
			File singleton_base = new File(path_start, "singleton-22");
			File merged_base = new File(path_start, "merged-33");
			if (outConf.save_imgs_gen) {
				if (!singleton_base.exists()) {singleton_base.mkdirs();}
				if (!merged_base.exists()) {merged_base.mkdirs();}
			}//end if we're supposed to be saving stuff

			// second merged folder which has merged particles for each quadrant, so image-wide
			File merged_base_2 = new File(outConf.path_base, "data\\" + "\\merged-33");
			if (outConf.save_imgs_gen) {
				if (!merged_base_2.exists()) {merged_base_2.mkdir();}
			}//end if we're actually supposed to be saving stuff

			// thisEntry.discoveredComponents = new ArrayList<KernelEntry>();
			// actually save or do stuff with the kernel/particle images
			if (mergedCount + splitCount > 0) {
				if (trueCount > 0) {

					ImagePlus phase3Image = curImg.duplicate();
					ImageProcessor phase3Ip = phase3Image.getProcessor();
					phase3Ip.setColor(Color.BLACK);

					KernelEntry phase3Entry = new KernelEntry(thisEntry);
					
					for (int r = 0; r < rm.getCount(); r++) {
						Roi roi = rm.getRoi(r);
						// do stuff depending on whether this component of the particle is correct
						boolean isMerged = isProbMerged(procConf, rt.getValueAsDouble(aColIdx, r), rt.getValueAsDouble(sColIdx, r));
						boolean isSplit = isProbSplit(procConf, rt.getValueAsDouble(aColIdx, r), rt.getValueAsDouble(fColIdx, r), rt.getValueAsDouble(sColIdx, r), rt.getValueAsDouble(miColIdx, r), rt.getValueAsDouble(maColIdx, r));
						if (isMerged || isSplit) {
							phase3Entry.phase2Position = r + 1;
						} else {
							// make the image and component entry 
							double feretAngle = rt.getValueAsDouble(faColIdx, r);
							ImagePlus cleanedImage = Durum.cleanCroppedRoi(outConf.rotate_singleton_kern_imgs, curImg, (Roi)roi.clone(), feretAngle);
							KernelEntry thisComponentEntry = new KernelEntry(
								thisEntry.originalFile,
								thisEntry.quadrant,
								thisEntry.phase1Position,
								roi,
								cleanedImage
							);
							thisComponentEntry.kernelArea = rt.getValueAsDouble(aColIdx, r);
							thisComponentEntry.x = (int)rt.getValueAsDouble(xColIdx, r);
							thisComponentEntry.y = (int)rt.getValueAsDouble(yColIdx, r);
							thisComponentEntry.phase2Position = (r+1);
							// remove correctly segmented kernels from the overall image
							phase3Image.setRoi(roi);
							phase3Ip.fill(roi);
							phase3Image.setProcessor(phase3Ip);
							phase3Image.resetRoi();
							// save image of the individual kernel
							if (outConf.save_imgs_gen) {
								File tmpFile = new File(singleton_base, prefix + "-sng-" + thisComponentEntry.getPositionString());
								IJ.save(cleanedImage, tmpFile.getAbsolutePath() + ".tif");
							}//end if we should be saving images

							// thisEntry.discoveredComponents.add(thisComponentEntry);
							phase2Singletons.add(thisComponentEntry);
						}//end else we have a correct kernel
					}//end looping over each of the kernels after we know that at least one KernelEntry was correctly segmented
					// phase3Image.updateAndDraw();

					// finish up the kernel entry to pass to phase 3
					phase3Entry.cleanedImage = phase3Image;

					// thisEntry.discoveredComponents.add(phase3Entry);
					phase2Merged.add(phase3Entry);

					// save the image with just the kernels that we couldn't correctly segment
					if (outConf.save_imgs_gen) {
						File tmpFile = new File(merged_base, prefix + "-mrgOrSplit-" + phase3Entry.getPositionString());
						IJ.save(phase3Image, tmpFile.getAbsolutePath() + ".tif");
						File tmpFile2 = new File(merged_base_2, prefix + "-mrgOrSplit-" + phase3Entry.getPositionString());
						IJ.save(phase3Image, tmpFile2.getAbsolutePath() + ".tif");
					}//end if we should be saving images
					
				
				} else {
					KernelEntry dupEntry = new KernelEntry(thisEntry);
					dupEntry.phase2Position = 1;

					// thisEntry.discoveredComponents.add(dupEntry);
					phase2Merged.add(dupEntry);

					if (outConf.save_imgs_gen) {
						File tmpFile = new File(merged_base, prefix + "-mrg-" + thisEntry.getPositionString());
						IJ.save(thisEntry.cleanedImage, tmpFile.getAbsolutePath() + ".tif");
						File tmpFile2 = new File(merged_base_2, prefix + "-mrg-" + thisEntry.getPositionString());
						IJ.save(thisEntry.cleanedImage, tmpFile2.getAbsolutePath() + ".tif");
					}//end if we should be saving images
				}//end else the particle is going to phase 3
				
			}//end if one is either merged or split
			else {
				// for each of the correctly segmented kernels, need to basically copy and paste them into a new black image
				// then save them as new discovered component kernel entries with the original as the root

				for (int r = 0; r < rm.getCount(); r++) {
					Roi roi = rm.getRoi(r);
					double feretAngle = rt.getValueAsDouble(faColIdx, r);
					ImagePlus cleanedImage = Durum.cleanCroppedRoi(outConf.rotate_singleton_kern_imgs, curImg, roi, feretAngle);
					KernelEntry thisComponentEntry = new KernelEntry(
						thisEntry.originalFile,
						thisEntry.quadrant,
						thisEntry.phase1Position,
						roi,
						cleanedImage
					);
					thisComponentEntry.phase2Position = (r+1);
					thisComponentEntry.kernelArea = rt.getValueAsDouble(aColIdx, r);
					thisComponentEntry.x = (int)rt.getValueAsDouble(xColIdx, r);
					thisComponentEntry.y = (int)rt.getValueAsDouble(yColIdx, r);

					// thisEntry.discoveredComponents.add(thisComponentEntry);
					phase2Singletons.add(thisComponentEntry);

					if (outConf.save_imgs_gen) {
						File tmpFile = new File(singleton_base, prefix + "-sng-" + thisComponentEntry.getPositionString());
						IJ.save(cleanedImage, tmpFile.getAbsolutePath() + ".tif");
					}//end if we should save images
				}//end looping over each of the kernels after we know the whole KernelEntry was correctly segmented

			}//end else add each segmented particle under the original entry

			if (outConf.save_imgs_gen && outConf.save_particle_results) {
				rt.save(new File(path_start, prefix + "-" + thisEntry.getPositionString() + "-phase2-kernel-results.csv").getAbsolutePath());
			}//end if we're supposed to save the results table

			// make sure to clean up roiManager and Results table
			rt.reset();
			rm.reset();
		}//end doing watershedding on the kernel entries that need additional segmenting
	}//end phase2WatershedMerged()

	public void phase3CleanUpMiscCases(OutputConfig outConf, ProcessingConfig procConf) {
		phase3Merged = new ArrayList<>();
		phase3Singletons = new ArrayList<>();

		// for now, just do naive case of seeing if stuff is merged or split
		for (KernelEntry thisEntry : this.phase2Merged) {
			// get path information for saving images to places
			String prefix = thisEntry.originalFile.getName().replace(".tif", "");
			String quadrantCode = thisEntry.quadrant.name();
			File path_start = new File(outConf.path_base, "data\\" + prefix + "\\" + quadrantCode + "\\merged-33");
			File singleton_base = new File(path_start, "singleton-33");
			File merged_base = new File(path_start, "merged-44");
			File split_base = new File(path_start, "split-44");
			if (outConf.save_imgs_gen) {
				if (!singleton_base.exists()) {singleton_base.mkdirs();}
				if (!merged_base.exists()) {merged_base.mkdirs();}
				if (!split_base.exists()) {split_base.mkdirs();}
			}//end if we should actually be saving images

			// second singleton folder whichin second merged folder
			File singleton_base_2 = new File(outConf.path_base, "data\\" + "\\merged-33\\singleton-33");
			File merged_base_2 = new File(outConf.path_base, "data\\" + "\\merged-33\\merged-44");
			File split_base_2 = new File(outConf.path_base, "data\\" + "\\merged-33\\split-44");
			if (outConf.save_imgs_gen) {
				if (!singleton_base_2.exists()) {singleton_base_2.mkdir();}
				if (!merged_base_2.exists()) {merged_base_2.mkdir();}
				if (!split_base_2.exists()) {split_base_2.mkdir();}
			}//end if we should actually save the images

			// time to do the erode steps on a copy of thisEntry's image and see what happens
			ImagePlus dup = thisEntry.cleanedImage.duplicate();
			ImagePlus binDup = dup.duplicate();
			
				// before binary erode, need 8-bit threshold to ensure all of the kernel makes it into binary
				// if no threshold is set, then "Convert to Mask" will poorly guess what's in threshold
			
			IJ.run(binDup, "8-bit", "");
			binDup.getProcessor().setThreshold(1, 255);
			IJ.run(binDup, "Convert to Mask", "");
			
				// use binary erode and dilate to cut away chunks of the particles and thin them down a bit

			IJ.run(binDup, "Erode", "");
			IJ.run(binDup, "Erode", "");
			IJ.run(binDup, "Erode", "");
			IJ.run(binDup, "Dilate", "");

				// use image calculator to turn slimmed down binary AND original color image into slimmed down color image

			ImageCalculator ic = new ImageCalculator();
			ImagePlus erodedImage = ic.run("AND create", dup, binDup);

			ResultsTable rt = new ResultsTable();
			RoiManager rm = AnalyzeParticles.analyzeParticles(erodedImage, procConf.phase3Measurements, procConf.phase3ParticlesParam);
			rt = ResultsTable.getResultsTable();

			// if we just had junk, should be empty after erodes, so ignore and continue to next kernelEntry
			if (rm.getCount() == 0) {continue;}

			int aColIdx = rt.getColumnIndex("Area");
			int sColIdx = rt.getColumnIndex("Solidity");
			int fColIdx = rt.getColumnIndex("Feret");
			int miColIdx = rt.getColumnIndex("Minor");
			int maColIdx = rt.getColumnIndex("Major");
			int faColIdx = rt.getColumnIndex("FeretAngle");
			int prColIdx = rt.getColumnIndex("Perim.");
			int xColIdx = rt.getColumnIndex("X");
			int yColIdx = rt.getColumnIndex("Y");
			int mergedCount = 0;
			int splitCount = 0;
			for (int j = 0; j < rt.size(); j++) {
				boolean isMerged = isProbMerged3(procConf, rt.getValueAsDouble(aColIdx, j), rt.getValueAsDouble(sColIdx, j), rt.getValueAsDouble(prColIdx, j));
				boolean isSplit = isProbSplit3(procConf, rt.getValueAsDouble(aColIdx, j), rt.getValueAsDouble(fColIdx, j), rt.getValueAsDouble(sColIdx, j), rt.getValueAsDouble(miColIdx, j), rt.getValueAsDouble(maColIdx, j));
				if (isMerged) {mergedCount++;}
				else if (isSplit) {splitCount++;}
			}//end looping over each of the particles detected after watershed

			if (mergedCount + splitCount > 0) {
				for (int r = 0; r < rm.getCount(); r++) {
					boolean isMerged = isProbMerged3(procConf, rt.getValueAsDouble(aColIdx, r), rt.getValueAsDouble(sColIdx, r), rt.getValueAsDouble(prColIdx, r));
					boolean isSplit = isProbSplit3(procConf, rt.getValueAsDouble(aColIdx, r), rt.getValueAsDouble(fColIdx, r), rt.getValueAsDouble(sColIdx, r), rt.getValueAsDouble(miColIdx, r), rt.getValueAsDouble(maColIdx, r));
					
					// get entry stuff set up regardless of bin
					Roi roi = rm.getRoi(r);
					double feretAngle = rt.getValueAsDouble(faColIdx, r);
					ImagePlus cleanedImage = Durum.cleanCroppedRoi(outConf.rotate_merged_kern_imgs, thisEntry.cleanedImage.duplicate(), roi, feretAngle);
					KernelEntry thisComponentEntry = new KernelEntry(thisEntry);
					thisComponentEntry.phase3Position = (r+1);
					thisComponentEntry.kernelArea = rt.getValueAsDouble(aColIdx, r);
					thisComponentEntry.x = (int)rt.getValueAsDouble(xColIdx, r);
					thisComponentEntry.y = (int)rt.getValueAsDouble(yColIdx, r);
					thisComponentEntry.cleanedImage = erodedImage;

					// separate into the three bins: singleton, split, and merged
					if (isSplit || isMerged) {
						phase3Merged.add(thisComponentEntry);
						
						if (outConf.save_imgs_gen) {
							if (isSplit) {
								File tmpFile = new File(split_base, prefix + "-splt-" + thisComponentEntry.getPositionString());
								IJ.save(cleanedImage, tmpFile.getAbsolutePath() + ".tif");
								File tmpFile2 = new File(split_base_2, prefix + "-splt-" + thisComponentEntry.getPositionString());
								IJ.save(cleanedImage, tmpFile2.getAbsolutePath() + ".tif");
							}//end if we have a split kernel
							if (isMerged) {
								File tmpFile = new File(merged_base, prefix + "-mrg-" + thisComponentEntry.getPositionString());
								IJ.save(cleanedImage, tmpFile.getAbsolutePath() + ".tif");
								File tmpFile2 = new File(merged_base_2, prefix + "-mrg-" + thisComponentEntry.getPositionString());
								IJ.save(cleanedImage, tmpFile2.getAbsolutePath() + ".tif");
							}//end if we have a merged kernel
						}//end if we should be saving any images

					} else {
						phase3Singletons.add(thisComponentEntry);
						if (outConf.save_imgs_gen) {
							File tmpFile = new File(singleton_base, prefix + "-sng-" + thisComponentEntry.getPositionString());
							IJ.save(cleanedImage, tmpFile.getAbsolutePath() + ".tif");
							File tmpFile2 = new File(singleton_base_2, prefix + "-sng-" + thisComponentEntry.getPositionString());
							IJ.save(cleanedImage, tmpFile2.getAbsolutePath() + ".tif");
						}//end if we should actually save the images
					}//end if we have a singleton alongside some other stuff

				}//end looping over any particles that are present, on the offchance there's more than one
			}//end if we had at least one merged or split kernel
			else {
				for (int r = 0; r < rm.getCount(); r++) {
					Roi roi = rm.getRoi(r);
					double feretAngle = rt.getValueAsDouble(faColIdx, r);
					ImagePlus cleanedImage = Durum.cleanCroppedRoi(outConf.rotate_singleton_kern_imgs, thisEntry.cleanedImage, roi, feretAngle);
					KernelEntry thisComponentEntry = new KernelEntry(thisEntry);
					thisComponentEntry.phase3Position = (r+1);
					thisComponentEntry.kernelArea = rt.getValueAsDouble(aColIdx, r);
					thisComponentEntry.x = (int)rt.getValueAsDouble(xColIdx, r);
					thisComponentEntry.y = (int)rt.getValueAsDouble(yColIdx, r);
					thisComponentEntry.cleanedImage = erodedImage;

					// thisEntry.discoveredComponents = new ArrayList<>();
					// thisEntry.discoveredComponents.add(thisComponentEntry);
					phase3Singletons.add(thisComponentEntry);

					if (outConf.save_imgs_gen) {
						File tmpFile = new File(singleton_base, prefix + "-sng-" + thisComponentEntry.getPositionString());
						IJ.save(cleanedImage, tmpFile.getAbsolutePath() + ".tif");
						File tmpFile2 = new File(singleton_base_2, prefix + "-sng-" + thisComponentEntry.getPositionString());
						IJ.save(cleanedImage, tmpFile2.getAbsolutePath() + ".tif");
					}//end if we should actually save the images
				}//end looping through all the singleton rois we got so we can save all of them
			}//end else we had all correct kernels for this entry

			if (outConf.save_imgs_gen && outConf.save_particle_results) {
				rt.save(new File(path_start, prefix + "-" + thisEntry.getPositionString() + "-phase3-kernel-results.csv").getAbsolutePath());
			}//end we're supposed to save images

			// make sure we reset resultsTable and RoiManager
			rt.reset();
			rm.reset();
		}//end looping through each of the KernelEntries
	}//end phase3CLeanUpMiscCases()

	public int[][] getChalk(OutputConfig outConf, ProcessingConfig procConf, Worksheet ws) {
		// first construct a list of all singleton KernEntries
		List<KernelEntry> singletons = new ArrayList<>();
		singletons.addAll(phase1Singletons);
		singletons.addAll(phase2Singletons);
		singletons.addAll(phase3Singletons);

		if (singletons.size() < 1) {return null;}

		// lvl1,2,3,4,and invalid level
		int[] aaCounts = new int[] {0,0,0,0,0};
		int[] bbCounts = new int[] {0,0,0,0,0};
		int[] ccCounts = new int[] {0,0,0,0,0};
		int[] ddCounts = new int[] {0,0,0,0,0};
		int[] invalidCounts = new int[] {0,0,0,0,0};

		int[][] allCounts = new int[][] {aaCounts,bbCounts,ccCounts,ddCounts,invalidCounts};

		File chalkImgBase = new File(outConf.path_base, "data\\" + singletons.get(0).originalFile.getName().replace(".tif","") + "\\chalk_images");
		chalkImgBase.mkdir();

		for (int i = 0; i < singletons.size(); i++) {			
			// kernel results

			double kernelArea = singletons.get(i).kernelArea;

			// chalk results
			ImagePlus chalkThreshImg = singletons.get(i).cleanedImage.duplicate();

			IJProcess.colorThHSB(chalkThreshImg,
				new int[] {procConf.chalkChalkThresh_H_Min,procConf.chalkChalkThresh_S_Min,procConf.chalkChalkThresh_B_Min},
				new int[] {procConf.chalkChalkThresh_H_Max,procConf.chalkChalkThresh_S_Max,procConf.chalkChalkThresh_B_Max},
				new boolean[] {procConf.chalkChalkThresh_H_Pass,procConf.chalkChalkThresh_S_Pass,procConf.chalkChalkThresh_B_Pass},
				procConf.chalkChalkThreshFlipThreshold
			);

			ResultsTable rt = new ResultsTable();
			AnalyzeParticles.analyzeParticles(chalkThreshImg, 
				procConf.chalkChalkMeasurements, procConf.chalkChalkParam
			);
			rt = ResultsTable.getResultsTable();

			double chalkArea = 0;
			for (int r = 0; r < rt.size(); r++) {
				chalkArea += rt.getValue("Area", r);
			}//end getting chalk area of each particle that might have been present

			// now for doing something with the chalk results we got...

			double chalkPercent = chalkArea / kernelArea * 100.;

			if (outConf.save_chalk_images) {
				String kernInfo = "-krnArea_" + String.format("%.0f", kernelArea);
				String chalkInfo = "-chkArea_" + String.format("%.0f", chalkArea) + "-chkPerc_" + String.format("%.1f", chalkPercent);
				File kernThreshImgFile = new File(chalkImgBase, String.format("%.1f", chalkPercent) + singletons.get(i).getPositionString() + "-krn" + kernInfo + ".tif");
				File chalkThreshImgFile = new File(chalkImgBase, String.format("%.1f", chalkPercent) + singletons.get(i).getPositionString() + "-chk" + chalkInfo + ".tif");
				
				IJ.save(singletons.get(i).cleanedImage, kernThreshImgFile.getAbsolutePath());
				IJ.save(chalkThreshImg, chalkThreshImgFile.getAbsolutePath());

			}//end if we're saving debug images of chalk thresholds

			int lvlIndex = 4;
			if (chalkPercent < procConf.chalklvl1End) {lvlIndex = 0;} 
			else if (chalkPercent < procConf.chalklvl2End) {lvlIndex = 1;} 
			else if (chalkPercent < procConf.chalklvl3End) {lvlIndex = 2;} 
			else if (chalkPercent < procConf.chalklvl4End) {lvlIndex = 3;}

			int quadIndex = 4;
			if (singletons.get(i).quadrant != null) {quadIndex = singletons.get(i).quadrant.ordinal();}

			if (quadIndex == 4 || lvlIndex == 4) {
				System.out.println("Invalid Kernel Found at position:" + singletons.get(i).getPositionString());
			}

			allCounts[quadIndex][lvlIndex]++;
			
			rt.reset();

		}//end looping over each of the singletons
		
		return allCounts;
		
	}//end getChalk()

	public void writeToExcel(
		OutputConfig outConf, int logSheetRow,
		Worksheet ws, Worksheet log, int[][] allCounts
	) {
		// styling
		ws.range(0,0,10,10).style().horizontalAlignment("center").set();
		ws.range(1,6,6,6).style().format("0.0%").set();

		for (int c = 0; c <= 6; c++) {
			ws.width(c, 8.43);
		}//end making sure that each column has the same length

		ws.setZoom(200);

		// add row and column headers
		ws.value(0,1,"AA");
		ws.value(0,2,"BB");
		ws.value(0,3,"CC");
		ws.value(0,4,"DD");
		ws.value(0,5,"Sum");
		ws.value(0,6,"Percent");
		ws.value(1,0,"lvl1");
		ws.value(2,0,"lvl2");
		ws.value(3,0,"lvl3");
		ws.value(4,0,"lvl4");
		ws.value(5,0,"Sum");

		// add the actual counts
		for (int i = 0; i < allCounts.length - 1; i++) {
			int[] quadCounts = allCounts[i];
			for (int j = 0; j < quadCounts.length - 1; j++) {
				ws.value(j+1,i+1,quadCounts[j]);
			}//end looping over quadCounts
		}//end looping over allCounts

		int[] lvlSums = new int[4];
		int totalSum = 0;

		// basically just print all the values from allCounts to the right positions
		for (int i = 0; i < allCounts.length - 1; i++) {
			int[] quadCounts = allCounts[i];
			int quadSum = 0;
			for (int ii = 0; ii < 4 && ii < quadCounts.length; ii++) {
				lvlSums[ii] += quadCounts[ii];
				totalSum += quadCounts[ii];
				quadSum += quadCounts[ii];
			}

			ws.value(5,i+1,quadSum);
			ws.formula(5,6, "F6/F$6");
			
		}

		// add in the sums for each level and quadrant, plus percent for each level
		for (int i = 0; i < lvlSums.length; i++) {
			double percent = (double)lvlSums[i] / (double)totalSum;
			// values for per-image sheet
			ws.value(i+1,5, lvlSums[i]);
			ws.value(i+1,6, percent);
			// values for the log/sum sheet
			log.value(logSheetRow,i+5, percent);
		}
		ws.value(5,5,totalSum);
		log.value(logSheetRow, 3, totalSum);

		// do the rest of the log stuff for this image/kernGrid
		LocalDateTime now = LocalDateTime.now();
		log.value(logSheetRow,1,now);
		log.value(logSheetRow,2,now);
		
		// try and figure out what to put for the name of this file in the log sheet
		String imgName = originalImageFile.getName();
		if (imgName.contains(".")) {imgName = imgName.substring(0,imgName.lastIndexOf("."));}
		if (imgName.contains("-") && outConf.excel_log_shorten_hyphenated_names) {
			String[] splitParts = imgName.split("-");
			imgName = splitParts[splitParts.length-2] + "-" + splitParts[splitParts.length-1];
			if (splitParts.length > 2) {
				imgName = splitParts[splitParts.length-3] + "-" + imgName;
			}
		}
		log.value(logSheetRow,0,imgName);

	}//end writeToExcel()

	/**
	 * Used in phase 1
	 * @return true = merged
	 */
	public static boolean isMerged(ProcessingConfig conf, double round, double feret) {
		return (
			(round < conf.phase1_threshold_merged_round && feret > conf.phase1_threshold_merged_feret_low)
			|| feret > conf.phase1_threshold_merged_feret_high
		);
	}//end isMerged()

	/**
	 * Used in phase 2
	 * @return true = merged
	 */
	public static boolean isProbMerged(ProcessingConfig conf, double area, double solidity) {
		return area > conf.phase2_threshold_merged_area && solidity < conf.phase2_threshold_merged_solidity;
	}//end isProbMerged()

	/**
	 * Used in phase 2
	 * @return true = split
	 */
	public static boolean isProbSplit
	(
		ProcessingConfig conf,
		double area, double feret, double solidity, double minor, double major
	) {
		if (area < conf.phase2_threshold_split_hard_area) {return true;}
		boolean maybeSplit = area < conf.phase2_threshold_split_area && feret < conf.phase2_threshold_split_feret;
		if (maybeSplit) {
			double ratio = minor / major * 100;
			if (ratio > conf.phase2_threshold_split_minor_major) {maybeSplit = true;}
			else {maybeSplit = false;}
		}//end if it might be split, but we should check the ratio first
		return maybeSplit;
	}//end isProbSplit()

	/**
	 * Used in phase 3
	 * @return true = merged
	 */
	public static boolean isProbMerged3(ProcessingConfig conf, double area, double solidity, double perim) {
		if (area > conf.phase3_threshold_merged_hard_area) {return true;}
		return solidity < conf.phase3_threshold_merged_solidity && (area > conf.phase3_threshold_merged_area || perim > conf.phase3_threshold_merged_perim);
	}//end isProbMerged()

	/**
	 * Used in phase 3
	 * @return true = split
	 */
	public static boolean isProbSplit3
	(
		ProcessingConfig conf,
		double area, double feret, double solidity, double minor, double major
	) {
		if (area < conf.phase3_threshold_split_hard_area) {return true;}
		boolean maybeSplit = area < conf.phase3_threshold_split_area && feret < conf.phase3_threshold_split_feret;
		if (maybeSplit) {
			double ratio = minor / major * 100;
			if (ratio > conf.phase3_threshold_split_minor_major) {maybeSplit = true;}
			else {maybeSplit = false;}
		}//end if it might be split, but we should check the ratio first
		return maybeSplit;
	}//end isProbSplit()

}//end class KernelGrid
