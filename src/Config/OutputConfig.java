/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Config;

import ConfigScribe.ConfigScribe;
import ConfigScribe.ConfigStore;
import SimpleResult.SimpleResult;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Nicholas.Sixbury
 */
public class OutputConfig implements ConfigStore {
    
    public String save_imgs_genCOMMENT = "If set to true, then images of each kernel or particle at each stage will be saved to a folder." +
    "\n# Images from previous processing will not be kept unless moved out of the data folder.";
    public boolean save_imgs_gen = true;
    
    public String save_particle_resultsCOMMENT = "If true, then will save the results table from imagej to a txt file in the data directory." +
    "\n# Gets results from phase 1, phase 2, and phase 3";
    public boolean save_particle_results = false;
    
    public String print_kern_estimatesCOMMENT = "If this is set to true, then during phase 1, before merged kernels have been segmented, we will print estimates to the console of how many singleton kernels we expect to be in each quadrant, based on the size of each particle." +
    "\n# This function also prints the average kernel area found during this process.";
    public boolean print_kern_estimates = false;
    
    public String print_roi_countsCOMMENT = "If set to true, then during phase 1 and 2, the number of particles found will be printed to the console.";
    public boolean print_roi_counts = false;
    
    public String print_singleton_countsCOMMENT = "If set to true, then while the program is running, it will print the number of kernels segmented after each step to the console.";
    public boolean print_singleton_counts = true;
    
    public String rotate_singleton_kern_imgsCOMMENT = "If this is set to true, then while saving images of singleton kernels (assuming save_imgs_gen is true), each one will be rotated to be roughly horizontal.";
    public boolean rotate_singleton_kern_imgs = true;
    
    public String rotate_merged_kern_imgsCOMMENT = "If this is set to true, then while saving images of merged kernels (assuming save_imgs_gen is true), each one will be rotated to be roughly horizontal.";
    public boolean rotate_merged_kern_imgs = false;
    
    public String save_chalk_images_COMMENT = "If this is true, then while finding chalk for each singleton kernel, the program will save an image of both the kernel and the chalk region." +
    "\n# The chalk image name will include the chalk area in pixels and the chalk percent, while the kernel image will have the kernel area in pixels." +
    "\n# Each image will also have the chalk percent as an unlabelled number at the beginning of the name, causing the images to be sorted by chalk percent in the file explorer.";
    public boolean save_chalk_images = false;
    
    public String excel_log_rep_groupingCOMMENT = "The number of rows in the log sheet in the excel log to print before having a blank line." +
    "\n# Example: setting this to 1 will have a blank line after each image, while setting it to 2 will have a blank line after every other image.";
    public int excel_log_rep_grouping = 2;
    
    public String excel_log_shorten_hyphenated_namesCOMMENT = "If set to true, then when writing the names of files to the log sheet in the excel output, if an image name contains a hyphen, then will print only the part of the name adjacent to the last two hyphens. " +
    "\n# Example: an image name of SG-s22-11-002 would be shortened to s22-11-002" + 
    "\n# If the program encounters an error during the chalk step with some images which include hyphens, try setting this to false.";
    public boolean excel_log_shorten_hyphenated_names = true;
    
    public String avg_kernel_areaCOMMENT = "The average area of kernels. Used with the print_kern_estimates to estimate the number of kernels in merged particles before segmentation. Doesn't affect any processing that happens after segmentation.";
    public int avg_kernel_area = 2304;
    
    public String path_baseCOMMENT = "This setting is ignored and overwritten by the program. The value saved here is the directory used as a base by the program when outputting files." +
    "\n# As a result, you can use this to find out where the program is located, as well as where it saves files.";
    public String path_base = "";

    public SimpleResult<String> readConfig() {
        return ConfigScribe.readConfig(this);
    }//end readConfig()

    public SimpleResult<String> writeConfig() {
        return ConfigScribe.writeConfig(this);
    }//end writeConfig()
    
    @Override
    public String getConfigFilename() {return "output-settings.conf";}
    
    @Override
    public List<String> getConfigHeader() {
        List<String> lines = new ArrayList<String>();
        lines.add("This is one of the configuration files for the DurumScan program, written by Nicholas Sixbury. ");
        lines.add("Since this is the output-settings file, you can find settings related to program output, which DON'T ");
        lines.add("affect the final output for percent chalk. To find settings related to the image processing ");
        lines.add("thresholds and the levels used for chalk categorization, check out the processing-settings config file. ");
        lines.add("");
        lines.add("In this file, lines not parsed as variable serialization are automatically ignored.");
        lines.add("Because of this, any line starting with \'#\' will automatically be interpretted as");
        lines.add("a comment. Any comments will be untouched in the config file, so feel free to add");
        lines.add("your own comments.");
        lines.add("");
        lines.add("If this config file is ever deleted, then it should be re-generated on program");
        lines.add("startup. All parameters will be set to default, and all default comments will be");
        lines.add("added to the new config file, including this header comment.");
        lines.add("");
        return lines;
    }//end getConfigHeader()
    
    @Override
	public File getDirectoryLocation() {
		File dirLoc = new File(System.getProperty("user.home"), "AppData\\Local\\ARS-SPIERU\\\\durum-scan-bcgv");
		if (!dirLoc.exists()) {
			if (dirLoc.mkdir()) {
				return dirLoc;
			} else {return null;}
		} else {return dirLoc;}
	}//end getDirectoryLocation()
    
}
