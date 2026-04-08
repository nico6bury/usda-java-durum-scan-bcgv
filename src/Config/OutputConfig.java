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
    
    public boolean save_imgs_gen = true;
    public boolean save_roi_points = false;
    public boolean save_particle_results = false;
    public boolean print_kern_estimates = false;
    public boolean print_roi_counts = false;
    public boolean print_singleton_counts = true;
    public boolean rotate_singleton_kern_imgs = true;
    public boolean rotate_merged_kern_imgs = false;
    public boolean save_chalk_images = false;
    public int excel_log_rep_grouping = 2;
    public boolean excel_log_shorten_hyphenated_names = true;
    public int avg_kernel_area = 2304;
    public String path_baseCOMMENT = "This setting is ignored and overwritten by the program. The value saved here is the directory used as a base by the program when outputting files.";
    public String path_base = "";
    public boolean delete_tape = true;

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
        lines.add("TODO: PLACEHOLDER HEADER");
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
