package IJM;

import java.io.File;
import java.util.List;

import ij.ImagePlus;
import ij.gui.Roi;

public class KernelEntry {
    public enum Quadrant {
        AA, BB, CC, DD
    }//end enum Quadrant

    public Quadrant quadrant;
    public int phase1Position = -1;
    public int phase2Position = -1;
    public int phase3Position = -1;
    public List<String> flags;
    public double kernelArea = -0.001;
    
    public File originalFile;
    /**
     * Roi from before rotating the kernel image
     */
    public Roi originalRoi;

    /**
     * Cleaned image that has probably already been rotated. Might depend on config settings.
     * Shouldn't assume that cleanedImage is compatible with originalRoi.
     */
    public ImagePlus cleanedImage;

    /**
     * If it is discovered that that this KernelEntry contains multiple kernels
     * during phase 2 and onward, those kernels will be stored as new entries
     * in discoveredComponents. That way, we'll store what we've detected during
     * each phase. In this way, each KernelEntry is technically a tree, though
     * most kernel entries only have a root node.
     */
    // public List<KernelEntry> discoveredComponents;

    /**
     * Returns position information containing quadrant and phase 1-3 position.
     * With all three phases, should look something like AA;5;9;2
     * @return Returns position information based on fields
     */
    public String getPositionString() {
        StringBuilder sb = new StringBuilder();
        // add possible quadrant information
        if (quadrant != null) {sb.append(quadrant.name());}
        else {sb.append("||");}
        // add possible phase 1 information
        if (phase1Position >= 0) {sb.append(';'); sb.append(phase1Position);}
        else {return sb.toString();}
        // add posible phase 2 information
        if (phase2Position >= 0) {sb.append(';'); sb.append(phase2Position);}
        else {return sb.toString();}
        // add possible phase 3 information
        if (phase3Position >= 0) {sb.append(';'); sb.append(phase3Position);}
        else {return sb.toString();}
        // return if we've gotten all possible information
        return sb.toString();
    }//end getPositionString()

    public KernelEntry(
        File originalFile, Quadrant quadrant, int phase1Position,
        Roi originalRoi, ImagePlus cleanedImage
    ) {
        this.originalFile = originalFile;
        this.quadrant = quadrant;
        this.phase1Position = phase1Position;
        this.originalRoi = originalRoi;
        this.cleanedImage = cleanedImage;
    }//end constructor

    /**
     * Copy constructor, which does its best to make a deep copy of another KernelEntry.
     * Ignores flags and discoveredComponents.
     * @param other another KernelEntry to copy values from
     */
    public KernelEntry(KernelEntry other) {
        this.originalFile = other.originalFile;
        this.quadrant = other.quadrant;
        this.phase1Position = other.phase1Position;
        this.phase2Position = other.phase2Position;
        this.phase3Position = other.phase3Position;
        this.originalRoi = other.originalRoi;
        this.cleanedImage = other.cleanedImage;
    }//end copy-constructor
}//end class KernelEntry
