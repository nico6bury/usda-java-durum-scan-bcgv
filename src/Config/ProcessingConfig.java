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
public class ProcessingConfig implements ConfigStore {
    
    public int chalklvl1End = 12;
    public int chalklvl2End = 25;
    public int chalklvl3End = 50;
    public int chalklvl4End = 101;

    public int phase1_threshold_merged_feret_low = 85;
    public int phase1_threshold_merged_feret_high = 105;
    public double phase1_threshold_merged_round = 0.3;

    public int phase2_threshold_split_area = 1850;
    public int phase2_threshold_split_feret = 65;
    public int phase2_threshold_split_minor_major = 40;
    public int phase2_threshold_split_hard_area = 600;
    public int phase2_threshold_merged_area = 1950;
    public double phase2_threshold_merged_solidity = 0.91;

    public int phase3_threshold_split_area = 1850;
    public int phase3_threshold_split_feret = 55;
    public int phase3_threshold_split_minor_major = 40;
    public int phase3_threshold_split_hard_area = 600;
    public int phase3_threshold_merged_area = 1750;
    public double phase3_threshold_merged_solidity = 0.87;
    public int phase3_threshold_merged_perim = 200;
    public int phase3_threshold_merged_hard_area = 2800;

    public int phase1Thresh1_R_Min = 0;
    public int phase1Thresh1_R_Max = 255;
    public int phase1Thresh1_G_Min = 0;
    public int phase1Thresh1_G_Max = 255;
    public int phase1Thresh1_B_Min = 90;
    public int phase1Thresh1_B_Max = 255;
    public boolean phase1Thresh1_R_Pass = true;
    public boolean phase1Thresh1_G_Pass = true;
    public boolean phase1Thresh1_B_Pass = true;
    public boolean phase1Thresh1FlipThreshold = false;

    public int phase1Thresh2_H_Min = 15;
    public int phase1Thresh2_H_Max = 145;
    public int phase1Thresh2_S_Min = 0;
    public int phase1Thresh2_S_Max = 75;
    public int phase1Thresh2_B_Min = 80;
    public int phase1Thresh2_B_Max = 255;
    public boolean phase1Thresh2_H_Pass = false;
    public boolean phase1Thresh2_S_Pass = true;
    public boolean phase1Thresh2_B_Pass = true;
    public boolean phase1Thresh2FlipThreshold = false;

    public int chalkChalkThresh_H_Min = 130;
    public int chalkChalkThresh_H_Max = 225;
    public int chalkChalkThresh_S_Min = 0;
    public int chalkChalkThresh_S_Max = 255;
    public int chalkChalkThresh_B_Min = 175;
    public int chalkChalkThresh_B_Max = 255;
    public boolean chalkChalkThresh_H_Pass = false;
    public boolean chalkChalkThresh_S_Pass = true;
    public boolean chalkChalkThresh_B_Pass = true;
    public boolean chalkChalkThreshFlipThreshold = false;
    
    public String phase1Measurements = "area centroid bounding shape feret's redirect=None decimal=2";
	public String phase1ParticlesParam = "size=1000-75000 circularity=0.05-1.00 show=Masks exclude include add";

	public String phase2Measurements = "area centroid perimeter bounding fit shape feret's add redirect=None decimal=2";
	public String phase2ParticlesParam = "size=150-6000 circularity=0.2-1.00 show[Overlay Masks] clear composite add";
	
    public String phase3Measurements = "area centroid perimeter bounding fit shape feret's add redirect=None decimal=2";
	public String phase3ParticlesParam = "size=150-6000 circularity=0.2-1.00 show[Overlay Masks] clear composite add";
	
    public String chalkChalkMeasurements = "area centroid perimeter fit shape redirect=None decimal=2";
	public String chalkChalkParam = "size=20-10000 circularity=0.1-1.00 show=[Overlay Masks] display";

    public String delete_tapeCOMMENT = "If this is set to true, then will attempt to remove tape in the upper right " +
    "\n# corner of the image. Specifically, a rectangle reaching from x 2223 to the right of the image and from y 0 to y 300.";
    public boolean delete_tape = true;

    public SimpleResult<String> readConfig() {
        return ConfigScribe.readConfig(this);
    }//end readConfig()

    public SimpleResult<String> writeConfig() {
        return ConfigScribe.writeConfig(this);
    }//end writeConfig()
    
    @Override
    public String getConfigFilename() {return "processing-settings.conf";}
    
    @Override
    public List<String> getConfigHeader() {
        List<String> lines = new ArrayList<String>();
        lines.add("This is one of the configuration files for the DurumScan program, written by Nicholas Sixbury. ");
        lines.add("Since this is the processing-settings file, you can find settings related to the image processing ");
        lines.add("done by the program, all of which CAN affect the percent chalk or chalk level summary output from");
        lines.add("the program. For settings related to how the output is formatted, check out the output-settings file.");
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
        lines.add("In this file, there are a few collections of settings that bear extra explanation:");
        lines.add("Within the program, there are a number of times in which images will be thresholded based ");
        lines.add("on color. Pixels outside the color threshold are removed from the image before particle analysis, ");
        lines.add("in order to aid in segmentation of the kernels from each other and from the background. ");
        lines.add("Each threshold is split up into several separate settings in this file. Each threshold will ");
        lines.add("have Min, Max, and Pass for either H, S, and B OR R, G, and B, following either the HSB or ");
        lines.add(" RGB color schemes. Each threshold also has a setting attached, called \"flipThreshold\", which ");
        lines.add("essentially flips the entire threshold around by saving only what's outside the threshold ");
        lines.add("instead of what's inside the threshold. As for the \"Pass\" settings, if these are set to true");
        lines.add("for a particular part of the color scale, then values between the mind and max will be ");
        lines.add("considered inside the threshold, whereas setting pass to false for that part of the scale ");
        lines.add("will have the program consider values outside of the min and max to be inside the threshold. ");
        lines.add("The names of each of these threshold-variables has been standardized to make them easy to ");
        lines.add("distinguish, as they all contain the string \"Thresh\", and all settings which belong to");
        lines.add("the same threshold will have the same prefix, such as phase1Thresh1 or chalkChalkThresh.");
        lines.add("It's important to be very careful when changing these settings, as even changing them ");
        lines.add("a little bit could have a big impact on chalk and kernel detection. ");
        lines.add("It's also important to note that regardless of the norm for RGB or HSB, each section");
        lines.add("of the scale is always on a scale of 0-255, since that's how imagej does things.");
        lines.add("");
        return lines;
    }//end getConfigHeader()
    
    @Override
	public File getDirectoryLocation() {
		File dirLoc = new File(System.getProperty("user.home"), "AppData\\Local\\ARS-SPIERU\\durum-scan-bcgv");
		if (!dirLoc.exists()) {
			if (dirLoc.mkdir()) {
				return dirLoc;
			} else {return null;}
		} else {return dirLoc;}
	}//end getDirectoryLocation()
    
}
