package IJM;

import java.awt.Color;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

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

	public static String[][] doProcessing(List<File> imgFiles, Consumer<String> guiUpdater) {
		long startTimeTotal = System.currentTimeMillis();
		String[][] outputTable = new String[imgFiles.size()][6];

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
			guiUpdater.accept("\n\nCOULD NOT FIND JAR FILE FOR PATH BASE. THIS SHOULD NOT HAPPEN.\n\n");
			jar_location = "";
		}

		guiUpdater.accept(jar_location);
		outConf.path_base = jar_location;

		outConf.writeConfig();
		procConf.writeConfig();
		scanConf.writeConfig();

		// delete images from previous runs
		File dataBase = new File(outConf.path_base, "data\\");
		guiUpdater.accept(dataBase.getAbsolutePath());
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

			

			int iii = 0;
			for (File imgFile : imgFiles) {
				outputTable[iii][0] = imgFile.getName();

				guiUpdater.accept("\nStarting processing for " + imgFile.getName());
				long startTime = System.currentTimeMillis();
				KernelGrid kernGrid = new KernelGrid(imgFile, outConf);

				// major function call
				kernGrid.phase1FindQuadrants(outConf, procConf);

				guiUpdater.accept("Finished phase 1 of processing for " + imgFile.getName() + " in " + (System.currentTimeMillis() - startTime) / 1000. + " seconds.");
				if (outConf.print_singleton_counts) {
					guiUpdater.accept("Phase 1 found " + kernGrid.phase1Singletons.size() + " singleton kernels. " + kernGrid.phase1Merged.size() + " merged particles remain.");
					double percent = (double)(kernGrid.phase1Singletons.size()) / (double)(kernGrid.phase1Singletons.size() + kernGrid.phase1Merged.size()) * 100;
					guiUpdater.accept("Estimated percentage segmentation complete: " + String.format("%.2f", percent) + "%");
				}

				long startTime2 = System.currentTimeMillis();
				guiUpdater.accept("Getting ready to start phase 2 of processing, watershedding merged kernels.");
				
				// major function call
				kernGrid.phase2WatershedMerged(outConf, procConf);

				System.out.println("Finished phase 2 of processing for " + imgFile.getName() + " in " + (System.currentTimeMillis() - startTime2) / 1000. + " seconds.");
				if (outConf.print_singleton_counts) {
					guiUpdater.accept("Phase 2 found an additional " + kernGrid.phase2Singletons.size() + " singleton kernels. " + kernGrid.phase2Merged.size() + " merged particles remain.");
					double percent = (double)(kernGrid.phase1Singletons.size() + kernGrid.phase2Singletons.size()) / (double)(kernGrid.phase1Singletons.size() + kernGrid.phase2Singletons.size() + kernGrid.phase2Merged.size()) * 100;
					guiUpdater.accept("Estimated percentage segmentation complete: " + String.format("%.2f", percent) + "%");
				}

				long startTime3 = System.currentTimeMillis();
				guiUpdater.accept("Getting ready to start phase 3 of processing, clean-up of misc cases.");
				
				// major function call
				kernGrid.phase3CleanUpMiscCases(outConf, procConf);

				guiUpdater.accept("Finished phase 3 of processing for " + imgFile.getName() + " in " + (System.currentTimeMillis() - startTime3) / 1000. + " seconds.");
				if (outConf.print_singleton_counts) {
					guiUpdater.accept("Phase 3 found an additional " + kernGrid.phase3Singletons.size() + " singleton kernels. " + kernGrid.phase3Merged.size() + " merged or split particles remain.");
					double percent = (double)(kernGrid.phase1Singletons.size() + kernGrid.phase2Singletons.size() + kernGrid.phase3Singletons.size()) / (double)(kernGrid.phase1Singletons.size() + kernGrid.phase2Singletons.size() + kernGrid.phase3Singletons.size() + kernGrid.phase3Merged.size()) * 100;
					guiUpdater.accept("Estimated percentage segmentation complete: " + String.format("%.2f", percent) + "%");
				}

				long startTime4 = System.currentTimeMillis();
				guiUpdater.accept("Getting ready to start chalk processing.");

				// major function call
				Worksheet sk = wb.newWorksheet(imgFile.getName() + "-singles");
				sk.range(0,0,500,15).style().horizontalAlignment("center").set();
				int[][] chalkCounts = kernGrid.getChalk(outConf, procConf, sk);
				
				guiUpdater.accept("Finished finding chalk value for each kernel in " + imgFile.getName() + " in " + String.format("%.1f", (System.currentTimeMillis() - startTime4) / 1000.) + " seconds.");
				
				int[] lvlSums = new int[4];
				int totalSum = 0;

				// get total amount of seeds plus total per level
				for (int i = 0; i < chalkCounts.length - 1; i++) {
					int[] quadCounts = chalkCounts[i];
					for (int ii = 0; ii < 4 && ii < quadCounts.length; ii++) {
						lvlSums[ii] += quadCounts[ii];
						totalSum += quadCounts[ii];
					}
				}

				outputTable[iii][1] = totalSum + "";
				outputTable[iii][2] = String.format("%.1f", ((double)lvlSums[0] / (double)totalSum * 100));
				outputTable[iii][3] = String.format("%.1f", ((double)lvlSums[1] / (double)totalSum * 100));
				outputTable[iii][4] = String.format("%.1f", ((double)lvlSums[2] / (double)totalSum * 100));
				outputTable[iii][5] = String.format("%.1f", ((double)lvlSums[3] / (double)totalSum * 100));

				guiUpdater.accept("Writing to excel");
				// major function call
				Worksheet ws = wb.newWorksheet(imgFile.getName() + "-sum");
				kernGrid.writeToExcel(outConf, logSheetRow, ws, log, chalkCounts);
				
				// loop variable maintenance
				logSheetRow++;
				rowsSinceLastNew++;
				if (rowsSinceLastNew >= outConf.excel_log_rep_grouping) {
					logSheetRow++;
					rowsSinceLastNew = 0;
				}
				
				guiUpdater.accept("Finished all processing for " + imgFile.getName() + " in " + (System.currentTimeMillis() - startTime) / 1000. + " seconds.\n");
				IJ.runMacro("close(\"*\");");

				iii++;
			}//end processing each selected image file
		} catch (IOException ioe) {
				ioe.printStackTrace();
				return null;
		}//end trying to do stuff and catching IOExceptions

		guiUpdater.accept("\n\nFinished processing for all images after " + (System.currentTimeMillis() - startTimeTotal) / 1000. + " seconds.");

		return outputTable;
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

	public static RowSortComparator rsc = new RowSortComparator();
	
	public static class RowSortComparator implements Comparator<KernelEntry> {
		public int rowHeightTolerance = 50;

		@Override
		public int compare(KernelEntry k1, KernelEntry k2) {
			if (k1.quadrant == k2.quadrant) {
				if (k1.y == k2.y || Math.abs(k1.y - k2.y) < rowHeightTolerance) {
					return Integer.compare(k1.x,k2.x);
				} else {return Integer.compare(k1.y,k2.y);}
			} else {
				return Integer.compare(k1.quadrant.ordinal(), k2.quadrant.ordinal());
			}
		}
	}
}//end class Durum
