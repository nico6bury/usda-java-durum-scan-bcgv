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
    public int chalklvl4End = 100;

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
    public File getDirectoryLocation() {return null;}
    
}
