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
public class ScanConfig implements ConfigStore {
    
    public double unsharp_sigma = 1.5;
    public double unsharp_weight = 0.5;
    public boolean unsharp_skip = false;
    public boolean unsharp_rename = false;
    
    public double scan_x1 = 1.05;
    public double scan_y1 = 8.98;
    public double scan_x2 = 3.05;
    public double scan_y2 = 9.98;
    
    public SimpleResult<String> readConfig() {
        return ConfigScribe.readConfig(this);
    }//end readConfig()

    public SimpleResult<String> writeConfig() {
        return ConfigScribe.writeConfig(this);
    }//end writeConfig()
    
    @Override
    public String getConfigFilename() {return "durum-scan-settings.conf";}
    
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
