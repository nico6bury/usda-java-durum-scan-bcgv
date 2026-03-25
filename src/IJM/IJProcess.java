package IJM;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import IJM.SumResult.LeftOrRight;
import Utils.Constants;
import Utils.Result;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.measure.ResultsTable;

/**
 * This class keeps track of everything related to running files through the imagej flour macros.
 * It also handles all the calls to imagej.
 * @author Nicholas Sixbury
 */
public class IJProcess {
    /**
     * Should be the directory where the jar is located
     */
    // File base_macro_dir;
    // File base_macro_file;
    // default lower size limit for analyze particles
    int szMin = 2;
    // default upper size limit for analyze particles
    int defSizeLimit = 1000;
    // upper threshold for analyze particles
    public int th01 = 160;
    public List<SumResult> lastProcResult;
    public static double lower_flag_thresh = 0.05;
    public static double upper_flag_thresh = 0.10;

    /**
     * Constructs the class
     */
    public IJProcess() {
        // String macro_folder = jar_location + File.separator + "macros";
        // base_macro_dir = new File(macro_folder);
        // String macro_file = macro_folder + File.separator + "NS-FlourScan-Main.ijm";
        // base_macro_file = new File(macro_file);
    }//end constructor

    /**
     * This method does all the necessary fileIO to determine the path for an output file. 
     * @param ensureDirectoryExists If this is true, then this method will create a new directory if it doesn't already exist.
     * @return Returns a resulting path as a File if successful. Otherwise, returns the exception that prevented success.
     */
    public static Result<File> getOutputFilePath(boolean ensureDirectoryExists) {
        // get path of the jar as base directory
        String jar_location;
        try {
            jar_location = new File(IJProcess.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile().toString();
            String output_folder_storage = jar_location + File.separator + Constants.IMAGEJ_OUTPUT_FOLDER_NAME;
            File output_folder_storage_file = new File(output_folder_storage);
            if (!output_folder_storage_file.exists() && ensureDirectoryExists) {
                output_folder_storage_file.mkdir();
            }//end if we need to make our output folder

            LocalDateTime currentDateTime = LocalDateTime.now();
            DateTimeFormatter year = DateTimeFormatter.ofPattern("yyyy");
            DateTimeFormatter month = DateTimeFormatter.ofPattern("MMM");
            DateTimeFormatter day = DateTimeFormatter.ofPattern("dd");
            DateTimeFormatter hour = DateTimeFormatter.ofPattern("kk");
            DateTimeFormatter min = DateTimeFormatter.ofPattern("mm");
            DateTimeFormatter sec = DateTimeFormatter.ofPattern("ss");
            // DateTimeFormatter dir_formatter = DateTimeFormatter.ofPattern("yyyy-MM");
            // DateTimeFormatter file_formatter = DateTimeFormatter.ofPattern("MM-d_H:m");
            File newDirectory = new File(output_folder_storage_file.getAbsolutePath() + File.separator + "flour-scan-" + currentDateTime.format(year) + "-" + currentDateTime.format(month));
            // create the directory if it doesn't exist
            if (ensureDirectoryExists && !newDirectory.exists()) {
                newDirectory.mkdir();
            }//end if new directory needs to be created
            String newExtension = ".OUT.csv";
            String current_time_stamp = currentDateTime.format(month) + "-" + currentDateTime.format(day) + "_" + currentDateTime.format(hour) + ";" + currentDateTime.format(min) + ";" + currentDateTime.format(sec);
            String newFileName = current_time_stamp + newExtension;
            File outputFile = new File(newDirectory.getAbsolutePath() + File.separator + newFileName);

            return new Result<File>(outputFile);
        } catch (Exception e) {
            return new Result<File>(e);
        }//end catching any exceptions
    }//end getOutputFilePath(ensureDirectoryExists)

    /**
     * Builds all the text for the output file, and then writes that text to a file. 
     * Uses path from IJProcess.getOutputFilePath().
     * @param inputList The list of SumResult objects that represent the processed data. This will be formatted and written to the file.
     * @return Returns a result containing the full string output, or a wrapped error if something prevents the output file from being written.
     */
    public static Result<String> makeOutputFile(List<SumResult> inputList) {
        // save to output file
        Result<File> outputFileResult = getOutputFilePath(true);
        if (outputFileResult.isErr()) {
            return new Result<>(outputFileResult.getError());
        }//end if we couldn't get output file path
        // otherwise, continue as normal
        File outputFile = outputFileResult.getValue();

        if (!outputFile.exists()) {
            outputFile.getParentFile().mkdirs();
        }//end if we need to make the resulting directories
        PrintWriter pw;
        try {
            pw = new PrintWriter(outputFile);
        } catch (FileNotFoundException e) {
            return new Result<>(e);
        }//end catching FileNotFoundExceptions

        // print first section of header
        pw.printf("%s  %s\n%s\n", Constants.PROGRAM_NAME, Constants.VERSION, Constants.LOCATION + "    " + Constants.DATE() + "    " + Constants.PEOPLE);

        // print second section of header
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        pw.printf("Data Processed: %s\n", 
                dateFormat.format(cal.getTime()));

        // print third line of header
        pw.printf("flag lower threshold: %f     flag upper threshold: %f\n", lower_flag_thresh, upper_flag_thresh);

        // print headers for the columns we're about to print
        pw.print("FileID,TH,DateTime,left CT,rght CT,left L*,rght L*,Avg L*,left %A,rgt %A,Avg %A,Flag\n");
        
        StringBuilder data_output = new StringBuilder();
        // build timestamp for output file
        LocalDateTime curDateTime = LocalDateTime.now();
        DateTimeFormatter timestamp = DateTimeFormatter.ofPattern("yyyy-MM-d H:m:s");
        // group sumresults into our grouped lists
        List<List<SumResult>> groupedResults = SumResult.groupResultsByFile(inputList);
        // build output for all the images processed
        for (int i = 0; i < groupedResults.size(); i++) {
            List<SumResult> group = groupedResults.get(i);
            if (group.size() == 2) {
                SumResult left = null;
                SumResult rght = null;
                // figure out which is left or right
                for (SumResult sr : group) {
                    if (sr.leftOrRight == LeftOrRight.Left) {left = sr;}
                    else if (sr.leftOrRight == LeftOrRight.Right) {rght = sr;}
                }//end categorizing each sum result in the group
                if (left == null || rght == null) {continue;}
                // print out the columns
                double avg_percent_area = (left.percent_area + rght.percent_area) / 2.0;
                double avg_l = (left.l_mean + rght.l_mean) / 2.0;
                String flag = "";
                if (avg_percent_area > lower_flag_thresh) {flag = "x";}
                if (avg_percent_area > upper_flag_thresh) {flag = "xx";}
                data_output.append(String.format("%s,%d,%s,%d,%d,%3.1f,%3.1f,%3.1f,%4.3f,%4.3f,%4.3f,%s\n", left.file.getName(), left.threshold, curDateTime.format(timestamp), left.count, rght.count, left.l_mean, rght.l_mean, avg_l, left.percent_area, rght.percent_area, avg_percent_area, flag));
            }//end if we have the expected group size
            else {
                // else we need to deal with this somehow
            }//end else we have an unexpected group size
        }//end looping over all the stuff to print out
        
        // print output for all images
        pw.print(data_output.toString());

        // close output file
        pw.close();

        return new Result<>(data_output.toString());
    }//end makeOutputFile(inputList, icl)

    /**
     * This method performs the unsharp mask filter for files scanned through twain instead of the EPSON Scan Utility.  
     * By default, this method will save the new image next to the original, with "-unsharp_sigma-[sigma]_weight-[weight]" appended to the filename.
     * @param filepath The filepath of the image to process
     * @param sigma The sigma (radius) value to use for the unsharp filter
     * @param weight The mask weight to use for the unsharp filter. It must be between 0.1 and 0.9
     * @param rename_file Whether or not we should rename the unsharp masked file.
     * @return Returns either the filepath for the resulting file, or some error.
     */
    public static Result<String> doUnsharpCorrection(String filepath, double sigma, double weight, boolean rename_file) {
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
        return new Result<String>(newPath);
    }//end doUnsharpCorrection(filepath, sigma, weight)

    /**
     * Runs the code to process a list of images. Actually just passes everything to IJProcess.MainMacro()
     * @param files_to_process The list of image Files to process.
     * @return Returns a result that will contain the full string written to an output file, or an error if something prevented completion.
     */
    public Result<String> runMacro(List<File> files_to_process) {
        try {
            return MainMacro(files_to_process);
        } catch (Exception e) {
            return new Result<>(e);
        }//end if we catch any exceptions
    }//end runMacro()

    /**
     * The main method for organizing and processing images.
     * @param files_to_process The list of image Files to process
     * @return Returns a result that will contain the full string written to an output file, or an error if something prevented completion.
     */
    public Result<String> MainMacro(List<File> files_to_process) {
        // int baseThreshold = 160;
        List<SumResult> runningSum = new ArrayList<SumResult>();
        // maximum supported resolution of twain, less than max of actual epson capabilities
        // int splitWidth = 2368;
        // int splitHeight = 1184;
        // list of images that have already been split
        List<File> split_files = new ArrayList<File>();
        // String baseMacroDir = base_macro_dir.getAbsolutePath() + File.separator;
        // List<ImagePlus> imagesProcessed = new ArrayList<ImagePlus>();
        for (int i = 0; i < files_to_process.size(); i++) {
            File file = files_to_process.get(i);
            String sliceBase = file.getName().substring(0, file.getName().length() - 4);

            // actually start processing
            ImagePlus this_image = IJ.openImage(file.getAbsolutePath());

            // split the current image in two and process both halves
            List<ImagePlus> imagesToSplit = new ArrayList<>();
            imagesToSplit.add(this_image);
            List<ImagePlus> splitImages = PicSplitter(imagesToSplit);

            // left image
            // analyze particles info
            SumResult leftResult = ijmProcessFile(splitImages.get(0), file);
            leftResult.leftOrRight = LeftOrRight.Left;
            leftResult.slice = sliceBase + "-L";
            // Lab info
            double[] leftL = LabProcesser(splitImages.get(0));
            leftResult.l_mean = leftL[0];
            leftResult.l_stdv = leftL[1];
            runningSum.add(leftResult);
            // imagesProcessed.add(splitImages.get(0));

            // right image
            // analyze particles info
            SumResult rightResult = ijmProcessFile(splitImages.get(1), file);
            rightResult.leftOrRight = LeftOrRight.Right;
            rightResult.slice = sliceBase + "-R";
            // Lab info
            double[] rightL = LabProcesser(splitImages.get(1));
            rightResult.l_mean = rightL[0];
            rightResult.l_stdv = rightL[1];
            runningSum.add(rightResult);
            // imagesProcessed.add(splitImages.get(1));
        }//end looping over each file we want to split

        for (int i = 0; i < split_files.size(); i++) {
            File file = files_to_process.get(i);
            String sliceBase = file.getName().substring(0, file.getName().length() - 4);

            // actually start processing
            ImagePlus this_image = IJ.openImage(file.getAbsolutePath());
            
            // process the current image with analyze particles and Lab
            SumResult this_result = ijmProcessFile(this_image, file);
            this_result.slice = sliceBase;
            double[] l_info = LabProcesser(this_image);
            this_result.l_mean = l_info[0];
            this_result.l_stdv = l_info[1];
            runningSum.add(this_result);
        }//end loping over each file we want to process

        // close all the images, or at least the stack
        IJ.run("Close All");
        // output the output file
        Result<String> outputFileResult = makeOutputFile(runningSum);
        lastProcResult = runningSum;
        // return the rows of data that wil show up in the output file
        return outputFileResult;
    }//end Main Macro converted from ijm

    /**
     * Runs the commands that perform the actual imagej processing calls on the image.
     * @param img The image to process, loaded into imagej as an ImagePlus.
     * @param file The file from which img was loaded.
     * @return Returns a single SumResult, holding processed data from img.
     */
    public SumResult ijmProcessFile(ImagePlus img, File file) {
        // duplicate the image so we don't contaminate it
        ImagePlus img_dup = img.duplicate();
        // contents of processFile() from the macro:
        IJ.run(img_dup, "Sharpen", "");
        IJ.run(img_dup, "Smooth", "");
        IJ.run(img_dup, "8-bit", "");
        IJ.setThreshold(img_dup, 0, th01);
        IJ.run(img_dup, "Convert to Mask", "");

        // set the scale so it doesn't measure in mm or something
        IJ.run(img_dup, "Set Scale...", "distance=0 known=0 unit=pixel global");

        // reset results table
        ResultsTable curTable = ResultsTable.getResultsTable("Summary");
        if (curTable != null) {curTable.reset();}

        // specify the measure data to recieve from analyze particles
        IJ.run(img_dup, "Set Measurements...", "area perimeter bounding redirect=None decimal=1");

        // convert particle size controls based on dpi of image, assuming it's 2 x 1 inches
        double pixels_adjusted_min = ((double)szMin * 1200.0) / (double)img_dup.getHeight();
        double pixels_adjusted_max = ((double)defSizeLimit * 1200) / (double)img_dup.getHeight();

        // analyze particles to get summary of specks
        IJ.run(img_dup, "Analyze Particles...", "size=" + pixels_adjusted_min + "-" + pixels_adjusted_max + " show=[Overlay Masks] clear summarize");

        // collect recent analyze particles run from summary table
        ResultsTable sumTable = ResultsTable.getResultsTable("Summary");
        // this should contain "Slice", "Count", "Total Area", "Average Size", "%Area", "Perim."
        // String[] headings = sumTable.getColumnHeadings().split("\t");
        String slice = sumTable.getStringValue("Slice", 0);
        int count = (int)sumTable.getValue("Count", 0);
        int total_area = (int)sumTable.getValue("Total Area", 0);
        double prcnt_area = sumTable.getValue("%Area", 0);
        IJ.runMacro("selectWindow(\"Summary\");run(\"Close\");");
        SumResult this_result = new SumResult(file, slice, count, total_area, prcnt_area);
        this_result.threshold = th01;

        return this_result;
    }//end ijmProcessFile()

    /**
     * This method is named after an imagej macro file.
     * When given a list of images, it will split each one in half, 
     * creating a left and right version of each image.
     * @param images_to_process The list of images to split in half.
     * @return Returns a list of images split in half. The size should be double that of images_to_process.
     */
    public List<ImagePlus> PicSplitter(List<ImagePlus> images_to_process) {
        List<ImagePlus> splitImages = new ArrayList<ImagePlus>();

        for (int i = 0; i < images_to_process.size(); i++) {
            ImagePlus this_image1 = images_to_process.get(i);
            ImagePlus this_image2 = this_image1.duplicate();
            // get dimensions so we know where to split
            int imgWidth = this_image1.getWidth();
            int imgHeight = this_image1.getHeight();
            
            Roi[] leftRois = new Roi[1];
            // define the left split
            leftRois[0] = new Roi(0, 0, imgWidth / 2, imgHeight);
            
            Roi[] rightRois = new Roi[1];
            // define the right split
            rightRois[0] = new Roi(imgWidth / 2, 0, imgWidth / 2, imgHeight);

            // get the left and right splits
            ImagePlus[] results1 = this_image1.crop(leftRois);
            ImagePlus[] results2 = this_image2.crop(rightRois);
            splitImages.add(results1[0]);
            splitImages.add(results2[0]);
        }//end looping over images to process
        
        return splitImages;
    }//end Pic Splitter macro converted from ijm

    /**
     * Named after Lab Processor macro converted from ijm.
     * Although the original macro used stack measurements to get Lab values,
     * there were issues integrating the same approach with java, so instead
     * this method manually computes Lab statistics by converting each pixel to Lab from RGB
     * and using a library to do statistical operations on the list of Lab pixel values.
     * @param img The image to get L info from.
     * @return Returns the image statistics for the L slice of the Lab stack. Index 0 contains mean of L* values, and Index 1 contains stdev of L* values.
     */
    public double[] LabProcesser(ImagePlus img) {
        // set the right scale
        IJ.run(img, "Set Scale...", "distance=1 known=1 unit=[] global");
        // for each pixel, get L* value, add to list
        // ImageConverter ic = new ImageConverter(img);
        // ic.convertToRGB();
        double[] L_vals = new double[img.getWidth() * img.getHeight()];
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                int[] pixel = img.getPixel(x, y);
                // get RGB from this pixel
                int R = pixel[0];
                int G = pixel[1];
                int B = pixel[2];
                /*
                 * Conversion formulas taken from https://www.easyrgb.com/en/math.php
                 */
                // convert to XYZ for this pixel
                double sR = ((double)R / 255);
                double sG = ((double)G / 255);
                double sB = ((double)B / 255);
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
                // double CIE_a = 500 * (varX - varY);
                // double CIE_b = 200 * (varY - varZ);
                // add L* value to the array, pretending that it's a 2d array so we don't have to keep track of a separate index
                L_vals[x * img.getWidth() + y] = CIE_L;
            }//end going up down
        }//end going side to side

        DescriptiveStatistics ds = new DescriptiveStatistics(L_vals);
        double[] mean_and_stdev = new double[2];
        mean_and_stdev[0] = ds.getMean();
        mean_and_stdev[1] = ds.getStandardDeviation();

        return mean_and_stdev;
    }//end Lab Processor macro converted from ijm
}//end class IJProcess
