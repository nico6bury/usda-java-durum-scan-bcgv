package IJM;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.dhatim.fastexcel.Workbook;
import org.dhatim.fastexcel.Worksheet;

import Config.OutputConfig;
import Config.ProcessingConfig;
import Config.ScanConfig;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

public class Durum {
	// private static String path_base = "C:\\Users\\nicholas.sixbury\\Documents\\Programs\\java-ij-testing\\";
	// /**Whether or not to save images of quadrants and particles to image files */
	// private static boolean save_imgs_gen = true;
	// /**Used for estimating kernels in a merged particle */
	// private static double avg_kernel_area = 2304;
	// /**Whether we should save roi points to a file */
	// private static boolean save_roi_points = true;
	// /**Whether we should generate and print estimates of how many true kernels are present
	//  * based on merged particle size.
	//  */
	// private static boolean print_kern_estimates = false;
	// /**Whether we should print counts of the particles found in images */
	// private static boolean print_roi_counts = false;
	// /**
	//  * Whether or not cleanCroppedRois (method used for kernel images AND points)
	//  * should rotate kernels during the cleaning.
	//  */
	// private static boolean rotate_kern_imgs = true;
	/**
	 * If this is false, then (as of writing), roi points will match the kernel orientation
	 * shown in quad images. If this is true, then roi points will match the kernel orientation
	 * shown in singleton and merged kernel images.
	 */
	// private static boolean rotate_kern_b4_roi_point_output = true;

	private static void recursiveDeleteDirectory(File dir) {
		File[] files = dir.listFiles();
		if (files == null) {return;}
		for (File file : files) {
			if (!file.isDirectory()) {file.delete();}
			else {
				recursiveDeleteDirectory(file);
			}//end else we need to delete stuff inside before deleting the directory
		}//end deleting every file in directory
		dir.delete();
	}//end recursiveDeleteDirectory()

	public static void doProcessing(List<File> imgFiles) {
		long startTimeTotal = System.currentTimeMillis();

		OutputConfig outConf = new OutputConfig();
		ProcessingConfig procConf = new ProcessingConfig();
		ScanConfig scanConf = new ScanConfig();
		outConf.readConfig();
		procConf.readConfig();
		scanConf.readConfig();

		String jar_location;
		try {
			jar_location = new File(IJProcess.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile().toString();
		} catch (URISyntaxException e) {
			e.printStackTrace();
			System.err.println("\n\nCOULD NOT FIND JAR FILE FOR PATH BASE. THIS SHOULD NOT HAPPEN.\n\n");
			jar_location = "";
		}

		System.out.println(jar_location);
		outConf.path_base = jar_location;

		outConf.writeConfig();
		procConf.writeConfig();
		scanConf.writeConfig();

		// delete images from previous runs
		File dataBase = new File(outConf.path_base, "data\\");
		System.out.println(dataBase.getAbsolutePath());
		if (!dataBase.exists()) {dataBase.mkdir();}
		for(File file : dataBase.listFiles()) {recursiveDeleteDirectory(file);}

		File outputFile = new File(outConf.path_base, "data\\chalkOutput.xlsx");
		try (OutputStream os = Files.newOutputStream(outputFile.toPath()); Workbook wb = new Workbook(os,"java-ij-testing",null);) {
			// excel set-up
			wb.setGlobalDefaultFont("Calibri", 12);

			Worksheet log = wb.newWorksheet("log");
			Worksheet conf = wb.newWorksheet("conf");

			logAndConfigSheets(log, conf, outConf, procConf);

			int logSheetRow = 1;
			int rowsSinceLastNew = 0;

			for (File imgFile : imgFiles) {

				System.out.println("\nStarting processing for " + imgFile.getName());
				long startTime = System.currentTimeMillis();
				KernelGrid kernGrid = new KernelGrid(imgFile, outConf);

				// major function call
				kernGrid.phase1FindQuadrants(outConf, procConf);

				System.out.println("Finished phase 1 of processing for " + imgFile.getName() + " in " + (System.currentTimeMillis() - startTime) / 1000. + " seconds.");
				if (outConf.print_singleton_counts) {
					System.out.println("Phase 1 found " + kernGrid.phase1Singletons.size() + " singleton kernels. " + kernGrid.phase1Merged.size() + " merged particles remain.");
					double percent = (double)(kernGrid.phase1Singletons.size()) / (double)(kernGrid.phase1Singletons.size() + kernGrid.phase1Merged.size()) * 100;
					System.out.println("Estimated percentage segmentation complete: " + String.format("%.2f", percent) + "%");
				}

				long startTime2 = System.currentTimeMillis();
				System.out.println("Getting ready to start phase 2 of processing, watershedding merged kernels.");
				
				// major function call
				kernGrid.phase2WatershedMerged(outConf, procConf);

				System.out.println("Finished phase 2 of processing for " + imgFile.getName() + " in " + (System.currentTimeMillis() - startTime2) / 1000. + " seconds.");
				if (outConf.print_singleton_counts) {
					System.out.println("Phase 2 found an additional " + kernGrid.phase2Singletons.size() + " singleton kernels. " + kernGrid.phase2Merged.size() + " merged particles remain.");
					double percent = (double)(kernGrid.phase1Singletons.size() + kernGrid.phase2Singletons.size()) / (double)(kernGrid.phase1Singletons.size() + kernGrid.phase2Singletons.size() + kernGrid.phase2Merged.size()) * 100;
					System.out.println("Estimated percentage segmentation complete: " + String.format("%.2f", percent) + "%");
				}

				long startTime3 = System.currentTimeMillis();
				System.out.println("Getting ready to start phase 3 of processing, clean-up of misc cases.");
				
				// major function call
				kernGrid.phase3CleanUpMiscCases(outConf, procConf);

				System.out.println("Finished phase 3 of processing for " + imgFile.getName() + " in " + (System.currentTimeMillis() - startTime3) / 1000. + " seconds.");
				if (outConf.print_singleton_counts) {
					System.out.println("Phase 3 found an additional " + kernGrid.phase3Singletons.size() + " singleton kernels. " + kernGrid.phase3Merged.size() + " merged or split particles remain.");
					double percent = (double)(kernGrid.phase1Singletons.size() + kernGrid.phase2Singletons.size() + kernGrid.phase3Singletons.size()) / (double)(kernGrid.phase1Singletons.size() + kernGrid.phase2Singletons.size() + kernGrid.phase3Singletons.size() + kernGrid.phase3Merged.size()) * 100;
					System.out.println("Estimated percentage segmentation complete: " + String.format("%.2f", percent) + "%");
				}

				long startTime4 = System.currentTimeMillis();
				System.out.println("Getting ready to start chalk processing.");

				Worksheet ws = wb.newWorksheet(imgFile.getName());
				// major function call
				int[][] chalkCounts = kernGrid.getChalk(outConf, procConf, ws);

				System.out.println("Finished finding chalk value for each kernel in " + imgFile.getName() + " in " + String.format("%.1f", (System.currentTimeMillis() - startTime4) / 1000.) + " seconds.");
				
				System.out.println("Writing to excel");
				// major function call
				kernGrid.writeToExcel(outConf, logSheetRow, ws, log, chalkCounts);
				
				// loop variable maintenance
				logSheetRow++;
				rowsSinceLastNew++;
				if (rowsSinceLastNew >= outConf.excel_log_rep_grouping) {
					logSheetRow++;
					rowsSinceLastNew = 0;
				}
				

				System.out.println("Finished all processing for " + imgFile.getName() + " in " + (System.currentTimeMillis() - startTime) / 1000. + " seconds.\n");
				IJ.runMacro("close(\"*\");");
			}//end processing each selected image file

		} catch (IOException ioe) {
				ioe.printStackTrace();
		}//end trying to do stuff and catching IOExceptions

		System.out.println("\n\nFinished processing for all images after " + (System.currentTimeMillis() - startTimeTotal) / 1000. + " seconds.");
	}//end method doProcessing()

	protected static void logAndConfigSheets(Worksheet log, Worksheet configs, OutputConfig outConf, ProcessingConfig procConf) {
		log.setZoom(200);
			
		// styling
		log.range(0,0,15,15).style().horizontalAlignment("center").set();
		log.range(1,5,15,15).style().format("0.0%").set();
		log.range(1,1,15,1).style().format("m/d/yyyy").set();
		log.range(1,2,15,2).style().format("h:mm:ss AM/PM").set();
		
		log.width(1,11);
		log.width(2,11);
		log.width(3,11.7);
		log.width(4,4.5);
		log.width(5,8.4);
		log.width(6,8.4);
		log.width(7,8.4);
		log.width(8,8.4);

		// headers
		log.value(0,0, "Filename");
		log.value(0,1, "Date");
		log.value(0,2, "Time");
		log.value(0,3,"Total Seeds");
		log.value(0,4,"");
		log.value(0,5,"%lvl1");
		log.value(0,6,"%lvl2");
		log.value(0,7,"%lvl3");
		log.value(0,8,"%lvl4");

		// configs sheet settings
		configs.setZoom(200);

		// headers
		configs.value(0,0, "Output Configurations");
		configs.value(1,0, "Setting");
		configs.value(1,1, "Value");

		// values
		Field[] outConfFields = outConf.getClass().getFields();
		int fieldIdx = 0;
		for (int rowIdx = 2; fieldIdx < outConfFields.length; rowIdx++) {
			configs.value(rowIdx,0, outConfFields[fieldIdx].getName());
			try {
				configs.value(rowIdx, 1, outConfFields[fieldIdx].get(outConf).toString());
			} catch (Exception e) {
				configs.value(rowIdx,1, "Couldn't access output configuration value. " + e.getMessage() + "    " + e.getStackTrace());
			}
			fieldIdx++;
		}//end adding each of the output configuration values

		// headers again
		configs.value(3 + outConfFields.length, 0, "Processing Configuration");
		configs.value(4 + outConfFields.length, 0, "Setting");
		configs.value(4 + outConfFields.length, 1, "Value");

		// values again
		Field[] procConfFields = procConf.getClass().getFields();
		fieldIdx = 0;
		for (int rowIdx = 5 + outConfFields.length; fieldIdx < procConfFields.length; rowIdx++) {
			configs.value(rowIdx, 0, procConfFields[fieldIdx].getName());
			try {
				configs.value(rowIdx, 1, procConfFields[fieldIdx].get(procConf).toString());
			} catch (Exception e) {
				configs.value(rowIdx,1, "Couldn't access processing configuration value. " + e.getMessage() + "    " + e.getStackTrace());
			}
			fieldIdx++;
		}//end adding each of the processing configuration values

	}

	/**
	 * Uses Bill's methods to clean an image down to just the roi
	 * without using rectangular bounds
	 * @param img full image which roi was generated from
	 * @param roi roi for a single area (kernel) [NOTE: The roi parameter is mutated!]
	 * @param angle angle by which to rotate img, probably degrees
	 * @return returns an image of just the roi area of the image
	 */
	protected static ImagePlus cleanCroppedRoi(boolean rotate_kern_imgs, ImagePlus img, Roi roi, double angle) {
		img.setRoi(roi);
		// ImageProcessor ip = img.getProcessor();
		// ip.rotate(angle);

		Rectangle bounds = roi.getBounds();
		ImageProcessor newIp = new ColorProcessor((int)(bounds.width * 1.5),(int)(bounds.height * 1.5));
		ImagePlus newImp = new ImagePlus("sub ", newIp);
		newIp.setColor(Color.BLACK);
		newIp.fill();
		newImp.setProcessor(newIp);
		img.copy();
		newImp.paste();
		roi.setLocation(0, 0);
		newImp.resetRoi();
		newIp = newImp.getProcessor();
		newIp.setBackgroundColor(Color.BLACK);
		if (rotate_kern_imgs && angle != 0) {
			newIp.rotate(angle);
		}//end if we want to roate all the kernel images to be the same orientation

		// newIp.setRoi(bounds.x, bounds.y, bounds.width, bounds.height);
		// newIp = newIp.crop();
		newImp.setProcessor(newIp);
		newImp.updateAndDraw();
		return newImp;
	}//end cleanCroppedRoi()

	// protected static Roi[] createQuadrants(ImagePlus img, File inputFile) {
	// 	// just use naive split method, halve overall dimensions
	// 	Roi[] quads = new Roi[4];
	// 	quads[2] = new Roi(0, 0, img.getWidth() / 2,img.getHeight() / 2);
	// 	quads[0] = new Roi(img.getWidth() - (img.getWidth() / 2), 0, img.getWidth() / 2, img.getHeight() / 2);
	// 	quads[3] = new Roi(0, img.getHeight() - (img.getHeight() / 2), img.getWidth() / 2, img.getHeight() / 2);
	// 	quads[1] = new Roi(img.getWidth() - (img.getWidth() / 2), img.getHeight() - (img.getHeight() / 2), img.getWidth() / 2, img.getHeight() / 2);

	// 	if (save_imgs_gen) {
	// 		ImagePlus[] quadrant_imgs = img.crop(quads);
	// 		for (int i = 0; i < quadrant_imgs.length; i++) {
	// 			IJ.save(quadrant_imgs[i], path_base + "data\\quadrants\\" + (char)(65 + i) + (char)(65 + i) + ".tif");
	// 		}//end saving each quadrant image
	// 	}//end if we should be saving images

	// 	ImagePlus[] quadImgs = img.crop(quads);
	// 	int runningSingletonTotalArea = 0;
	// 	int runningSingletonCount = 0;
	// 	for(int q = 0; q < quadImgs.length; q++) {
	// 		if (print_kern_estimates) {
	// 			System.out.println("\nQuadrant " + (char)(65 + q) + " Row Counts:");
	// 		}//end if we should print kern number estimates
	// 		// for now, just re-find kernels within each quadrant
	// 		String letter = ((char)(65 + q)) + "";
	// 		String prefix = "-" + inputFile.getName().replace(".tif", "") + "-" + letter + letter;
	// 		RoiManager quadKerns = getKernels1(quadImgs[q], save_imgs_gen, true, false, prefix, print_roi_counts);
	// 		ResultsTable rt = ResultsTable.getResultsTable();
	// 		double[] feretAngleCol = rt.getColumn("FeretAngle");
	// 		double[] roundCol = rt.getColumn("Round");
	// 		double[] feretCol = rt.getColumn("Feret");
	// 		double[] areaCol = rt.getColumn("Area");

	// 		if (save_roi_points) {
	// 			// use cleanCroppedRois to create rotated rois
	// 			ImagePlus[] cleanedImages = new ImagePlus[quadKerns.getCount()];
	// 			Roi[] cleanedRois = new Roi[quadKerns.getCount()];
	// 			for (int i = 0; i < cleanedImages.length; i++) {
	// 				double angle = feretAngleCol[i];
	// 				ImagePlus cleanedImage = cleanCroppedRoi(quadImgs[q], quadKerns.getRoi(i), angle);
	// 				cleanedImages[i] = cleanedImage;
	// 			}//end looping over each kernel

	// 			// create a custom macro to process all the cleaned images into rois at once
	// 			StringBuilder macro = new StringBuilder();
	// 			macro.append("setBatchMode(true);\n");
	// 			macro.append("run(\"Set Measurements...\", \" redirect=None decimal=0\");\n");

	// 			// add lines that need to be added for each file
	// 			for (int i = 0; i < cleanedImages.length; i++) {
	// 				// get path for the current index image
	// 				String tmpPth = AnalyzeParticles.jarFile().getParent() + "\\tmp" + i + ".tif";
	// 				// make sure our temporary file doesn't hang around after program completion
	// 				File tmpFile = new File(tmpPth);
	// 				tmpFile.deleteOnExit();

	// 				// make sure the file is converted to grayscale and present at location
	// 				ImageConverter ic = new ImageConverter(cleanedImages[i]);
	// 				ic.convertToGray8();
	// 				IJ.save(cleanedImages[i],tmpPth);

	// 				macro.append("open(\"" + AnalyzeParticles.duplicateBackslashes(tmpPth) + "\");\n");
	// 				macro.append("setThreshold(1,255);\n");
	// 				macro.append("run(\"Analyze Particles...\", \"size=10-Infinity show=Nothing exclude add\");\n");

	// 			}//end adding lines for each of the cleaned images
				
	// 			// add lines to macro that allow us to extract the roi
	// 			String tmpRoiPth = AnalyzeParticles.jarFile().getParent() + "\\bulk-rois.zip";
	// 			File tmpRoiZipFile = new File(tmpRoiPth);
	// 			tmpRoiZipFile.deleteOnExit();
	// 			macro.append("close(\"*\")\n");
	// 			macro.append("roiManager(\"Save\", \"" + AnalyzeParticles.duplicateBackslashes(tmpRoiPth) + "\");\n");
				
	// 			// actually run the macro and collect rois from the file we set up
	// 			IJ.runMacro(macro.toString());
	// 			RoiManager bulkRoisCollector = new RoiManager(false);
	// 			bulkRoisCollector.open(tmpRoiPth);
	// 			// TODO: Add something to the macro to ensure we only save 1 roi per image
	// 			cleanedRois = bulkRoisCollector.getRoisAsArray();

	// 			RoiPoints[] roiPointsData = new RoiPoints[quadKerns.getCount()];
	// 			for (int j = 0; j < quadKerns.getCount() && j < cleanedImages.length && j < cleanedRois.length; j++) {
	// 				RoiPoints roiPoints = getRoiPoints(cleanedImages[j], cleanedRois[j]);
	// 				roiPointsData[j] = roiPoints;
	// 			}
	// 			// actually print roi point data to a file
	// 			saveRoiPoints(roiPointsData, prefix);
	// 		}//end if we should save csvs with roi point information

	// 		if (print_kern_estimates) {
	// 			Roi[] roiarray = quadKerns.getRoisAsArray();
	// 			int curRow = 1;
	// 			int rowTol = 50;
	// 			int singletonCount = 0;
	// 			int mergedCount = 0;
	// 			int suspectedKernelCount = 0;
	// 			List<Integer> rowCounts = new ArrayList<Integer>();
	// 			for (int i = 0; i < roiarray.length; i++) {
	// 				// figure out Y positions of cur and last roi
	// 				double n2Y = roiarray[i].getContourCentroid()[1];
	// 				double n1Y;
	// 				if (i == 0) {n1Y = n2Y;} else {n1Y = roiarray[i-1].getContourCentroid()[1];}
	// 				// if cur is a new row, file off info for the last row
	// 				if (n2Y - n1Y > rowTol) {
	// 					rowCounts.add(singletonCount + mergedCount);
	// 					System.out.println("Row " + curRow + " has " + singletonCount + " singleton particles and " + mergedCount + " merged particles. The suspected total kernel count is " + (singletonCount + suspectedKernelCount) + ".");
	// 					curRow++;
	// 					singletonCount = 0;
	// 					mergedCount = 0;
	// 					suspectedKernelCount = 0;
	// 				}//end if we're on a new row
	// 				// general counting/handling for cur roi
	// 				double round = roundCol[i];
	// 				double feret = feretCol[i];
	// 				double area = areaCol[i];
	// 				if (isMerged(round, feret)) {
	// 					mergedCount++;
	// 					double per = area / avg_kernel_area;
	// 					per = Math.round(per);
	// 					suspectedKernelCount += per;
	// 				} else {
	// 					singletonCount++;
	// 					runningSingletonTotalArea += area;
	// 					runningSingletonCount++;
	// 				}//end else we have a singleton
	// 				// make sure to save row info for the last row
	// 				if (i == roiarray.length - 1) {
	// 					rowCounts.add(singletonCount + mergedCount);
	// 					System.out.println("Row " + curRow + " has " + singletonCount + " singleton particles and " + mergedCount + " merged particles. The suspected total kernel count is " + (singletonCount + suspectedKernelCount) + ".");
	// 				}//end if we're on the last roi
	// 			}//end counting rois in rows
	// 		}//end if we should print (or calculate) kernel estimates

	// 		quadKerns.reset();
	// 		rt.reset();
	// 	}//end looking at each quadrant separately
	// 	if (print_kern_estimates) {
	// 		int singletonAverage = runningSingletonTotalArea / runningSingletonCount;
	// 		System.out.println("Average singleton kernel area for this image is " + singletonAverage + "\n\n");
	// 	}//end if we should print kernel estimates
		
	// 	return quads;
	// }//end createQuadrants()

	// protected static void saveRoiPoints(RoiPoints[] roiPointsData, String imgPrefix) {
	// 	File roiPointsFile = new File(path_base, "data\\rois\\" + imgPrefix + "-roi-points.csv");
	// 	// build the stuff to write to the file
	// 	StringBuilder sb  = new StringBuilder();

	// 	// save the roi information
	// 	// TODO: Find a way to sort top and bottom data programmatically
	// 	// sb.append("Kernel,L X,L Y,R X,R Y\n");
	// 	sb.append("Kernel,X,Y\n");
	// 	for(int i = 0; i < roiPointsData.length; i++) {
	// 		if (roiPointsData[i] != null) {
	// 			for (int ii = 0; ii < roiPointsData[i].points.size(); ii++) {
	// 				Point p = roiPointsData[i].points.get(ii);
	// 				sb.append(imgPrefix + (i + 1) + "," + p.x + "," + p.y + "\n");
	// 			}//end looping over each point within current roi
	// 			sb.append(",,\n");
	// 		}//end if we don't have null for the roi points
	// 	}//end looping over each roi, basically, through the point data for each

	// 	// write the file
	// 	try (
	// 		FileWriter fw = new FileWriter(roiPointsFile)) {
	// 		fw.write(sb.toString());
	// 		fw.close();
	// 	} catch (IOException e) {
	// 		System.err.println("Failed to write file " + roiPointsFile.getAbsolutePath() + ". Got IOException with message: " + e.getMessage());
	// 		e.printStackTrace();
	// 	}//end trying to catch any IOException that might occur
	// }//end saveRoiPoints()

	// /**
	//  * Gets information about the points making up the outline of a roi.
	//  * @param img The image which the rois refer to
	//  * @param roi A roi (for img) to get point data from
	//  * @return Returns a single RoiPoints object with points from outline of roi
	//  */
	// protected static RoiPoints getRoiPoints(ImagePlus img, Roi roi) {
	// 	List<Point> pointsToReturn = new ArrayList<Point>();
	// 	List<Point> sortedPoints = new ArrayList<Point>();
	// 	for (Point p : roi) {sortedPoints.add(p);}
	// 	sortedPoints.sort(pxyc);
	// 	for (int i = 1; i < sortedPoints.size(); i++) {
	// 		// gather info for first pass: left,current,right,top,bottom
	// 		// reminder: points use image coords (so, origin is in top left instead of bottom left)
	// 		Point curPoint = sortedPoints.get(i);
	// 		// find point to left (if there is one) and see if we have a likely outline
	// 		Point left = sortedPoints.get(i-1);
	// 		if (left.y != curPoint.y) {pointsToReturn.add(curPoint); continue;}
	// 		// find point to right (if there is one) and see if we have a likely outline
	// 		Point right = null;
	// 		if (i+1 < sortedPoints.size()) {right = sortedPoints.get(i+1);}
	// 		if (right == null || right.y != curPoint.y) {pointsToReturn.add(curPoint); continue;}
	// 		// find point above (if there is one) and see if we have a likely outline
	// 		Point top = null;
	// 		for (int j = i - 2; j >= 0 && j < sortedPoints.size(); j--) {
	// 			Point jpoint = sortedPoints.get(j);
	// 			// look for point with x equal to cur and y just one above cur (lower y)
	// 			// also use guard rails to make sure we don't spend too much time searching
	// 			if (jpoint.x == curPoint.x && jpoint.y == curPoint.y - 1) {top = jpoint; break;}
	// 			else if (jpoint.y < curPoint.y - 1) {break;}
	// 		}//end looping backwards through sortedPoints to look for the point above current
	// 		if (top == null) {pointsToReturn.add(curPoint); continue;}
	// 		// find point below (if there is one) and see if we have a likely outline
	// 		Point bot = null;
	// 		for (int j = i + 2; j < sortedPoints.size(); j++) {
	// 			Point jpoint = sortedPoints.get(j);
	// 			// look for point with x equal to cur and y just one below cur (higher y)
	// 			// also use guard rails to make sure we don't spend too much time searching
	// 			if (jpoint.x == curPoint.x && jpoint.y == curPoint.y + 1) {bot = jpoint; break;}
	// 			else if (jpoint.y > curPoint.y + 1) {break;}
	// 		}//end looping forwards through sortedPoints to look for the point below current
	// 		if (bot == null) {pointsToReturn.add(curPoint); continue;}
	// 	}//end looping over each point, trying to figure out which are
	// 	// pointsToReturn.sort(pxyc);
	// 	return new RoiPoints(img, roi, pointsToReturn);
	// }//end getRoiPoints()

	// protected static class RoiPoints {
	// 	/** The original image this roi points points to, when combined with the originalRoi */
	// 	protected ImagePlus originalImg;
	// 	/** The original roi this RoiPoints was created from, when combined with originalImg */
	// 	protected Roi originalRoi;
	// 	/** The points making up this roi. 
	// 	 * Some points might be omitted, depending on the producer of this object. */
	// 	protected List<Point> points;
		
	// 	/**
	// 	 * Constructs a RoiPoints object with all intact information.
	// 	 * @param originalImage The original image which can be used with originalRoi to obtain the points in question
	// 	 * @param originalRoi The original roi used to get the points in question with the original image
	// 	 * @param points The points that make up the roi. Some points may be omitted depending on the producer of this object.
	// 	 */
	// 	protected RoiPoints(ImagePlus originalImage, Roi originalRoi, List<Point> points) {
	// 		this.originalImg = originalImage;
	// 		this.originalRoi = originalRoi;
	// 		this.points = points;
	// 	}//end 3-arg constructor

	// 	/**
	// 	 * Constructs a RoiPoints object with everything set to null.
	// 	 */
	// 	protected static RoiPoints empty() {
	// 		return new RoiPoints(null, null, null);
	// 	}//end construction for empty RoiPoints
	// 	/**
	// 	 * @return Returns true if all fields of this object are null, false otherwise.
	// 	 */
	// 	protected boolean isEmpty() {
	// 		return this.originalImg == null && this.originalRoi == null && this.points == null;
	// 	}//end isEmpty()
	// 	/**
	// 	 * @return Returns true if at least one field of this object is null, false otherwise
	// 	 */
	// 	protected boolean hasEmpty() {
	// 		return this.originalImg == null || this.originalRoi == null || this.points == null;
	// 	}//end hasEmpty()
	// }//end class RoiPoints

	/**
	 * Splits one list in half, returing one list with the first half and 
	 * a second with the second half of the original list.
	 * @param <T> The type of elements in the list
	 * @param list The list to split
	 * @return Returns a list containing two lists.
	 */
	public static <T> List<List<T>> splitList(List<T> list) {
		List<T> list1 = new ArrayList<T>();
		List<T> list2 = new ArrayList<T>();

		for (int i = 0; i < list.size(); i++) {
			if (i <= list.size() / 2) {
				list1.add(list.get(i));
			} else {
				list2.add(list.get(i));
			}
		}//end adding elements of list to split lists

		List<List<T>> lists = new ArrayList<List<T>>();
		lists.add(list1);
		lists.add(list2);
		return lists;
	}//end splitList

	private static PointXYComparator pxyc = new PointXYComparator();
	private static class PointXYComparator implements Comparator<Point> {
		@Override
		public int compare(Point p1, Point p2) {
			if (p1.y == p2.y) {
				return Integer.compare(p1.x,p2.x);
			} else {return Integer.compare(p1.y,p2.y);}
		}
	}

	// private static PointYXComparator pyxc = new PointYXComparator();
	// private static class PointYXComparator implements Comparator<Point> {
	// 	@Override
	// 	public int compare(Point p1, Point p2) {
	// 		if (p1.x == p2.x) {
	// 			return Integer.compare(p1.y,p2.y);
	// 		} else {return Integer.compare(p1.x,p2.x);}
	// 	}
	// }
}//end class Durum
