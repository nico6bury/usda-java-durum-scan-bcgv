package IJM;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URL;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.plugin.frame.RoiManager;
import ij.process.ImageConverter;

public class AnalyzeParticles {
    /**
     * Returns File that points to the location of the jar file the program is running from.
     * This function is used to locate directories adjacent to the jar file.
     * @return Returns null if unable to retrieve file for some reason.
     */
    protected static File jarFile() {
        File jarFile;
        URL jarurl = AnalyzeParticles.class.getProtectionDomain().getCodeSource().getLocation();
        try {
            jarFile = new File(jarurl.toURI());
        } catch (URISyntaxException e) {e.printStackTrace(); return null;}
        return jarFile;
    }//end jarFile()

    /**
     * Uses macro system to analyze particles.
     * @param image The to analyze for particles. Not mutated, but you should threshold beforehand, No need to convert to gray 8-bit image.
     * @param measurmentsParam measurements parameter to use with Set Measurements
     * @param particlesParam particles parameter to use with Analyze Particles. Please make sure to include add in order to use roi manager results
     * @return Returns path to rois from roi manager
     */
    public static RoiManager analyzeParticles(
        ImagePlus image,
        String measurmentsParam,
        String particlesParam
    ) {
        // String[] returnStrs = new String[2];
        // setup temp file to save images to
        File jarFile = jarFile();
        String tmpPth = jarFile.getParent() + "\\tmp.tif";
        File tmpFile = new File(tmpPth);
        tmpFile.deleteOnExit();

        // // get second temporary path for mask
        // String tmpPth2 = jarFile.getParent() + "\\tmp2.tif";
        // File tmpFile2 = new File(tmpPth2);
        // tmpFile2.deleteOnExit();

        // get third temporary path for roi
        String tmpPth3 = jarFile.getParent() + "\\tmp3.zip";
        File tmpFile3 = new File(tmpPth3);
        tmpFile3.deleteOnExit();
        
        ImagePlus tmp = image.duplicate();
        ImageConverter ic = new ImageConverter(tmp);
        ic.convertToGray8();

        IJ.save(tmp, tmpFile.getAbsolutePath());
        String macro = 
            "setBatchMode(true);\n" + 
            "run(\"Set Measurements...\", \"" + measurmentsParam + "\");\n" +
            "setThreshold(1,255);\n" + 
            "title = getTitle();\n" + 
            "run(\"Analyze Particles...\", \"" + particlesParam + "\");\n" + 
            // "selectWindow(\"Mask of \" + title)\n" + 
            // "save(\"" + duplicateBackslashes(tmpPth2) + "\");\n" +
            "close(\"*\")\n" +
            "if (roiManager(\"Count\") > 0) {" +
            "roiManager(\"Save\", \"" + duplicateBackslashes(tmpPth3) + "\");" +
            "} else {File.delete(\"" + duplicateBackslashes(tmpPth3) + "\");}" +
            "\n"
        ;
        // redirect console spam from imagej
        disableConsoleSpam();
        // actually do the macro stuff
        IJ.open(tmpFile.getAbsolutePath());
        IJ.runMacro(macro);
        // reset output stream
        enableConsoleSpam();
        // return handle to roi set
        // returnStrs[0] = tmpPth2;
        RoiManager rm = new RoiManager(false);
        if (tmpFile3.exists()) {rm.open(tmpPth3); tmpFile3.delete();}
        return rm;
    }//end method analyzeParticles

    // public static ImagePlus maskParticles(ImagePlus img, String maskPath) {
    //     String tmpPth = jarFile().getParent() + "\\tmpMask.tif";
    //     File tmpFile = new File(tmpPth);
    //     tmpFile.deleteOnExit();
    //     String tmpPth2 = jarFile().getParent() + "\\tmpImg.tif";
    //     File tmpFile2 = new File(tmpPth2);
    //     tmpFile2.deleteOnExit();

    //     IJ.save(img, tmpPth2);

    //     String macro = 
    //         "setBatchMode(true);\n" +
    //         "open(\"" + duplicateBackslashes(tmpPth2) + "\");\n" +
    //         "img = getTitle();\n" +
    //         "open(\"" + duplicateBackslashes(maskPath) + "\");" +
    //         "mask = getTitle();\n" +
    //         "imageCalculator(\"Subtract create\", img, mask);\n" +
    //         "selectImage(\"Result of \" + img);\n" +
    //         "save(\"" + duplicateBackslashes(tmpPth) + "\");\n" +
    //         "close(\"*\")\n" +
    //         "\n"
    //     ;

    //     IJ.runMacro(macro);

    //     if (tmpFile.exists()) {
    //         ImagePlus tmp = IJ.openImage(tmpFile.getAbsolutePath());
    //         return tmp;
    //     } else {return null;}
    // }//end maskParticles

    /**
     * Disables usual System.out.println output.
     * Useful for calls to libraries which like to spam console output with no off switch.
     * @see enableConsoleSpam()
     */
    protected static void disableConsoleSpam() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        System.setOut(ps);
    }//end disableConsoleSpam()

    /**
     * Re-enables the usual System.out.println output.
     * Used after you're done calling something which spams the console, after using disableConsoleSpam()
     * @see disableConsoleSpam()
     */
    protected static void enableConsoleSpam() {
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
    }//end enableConsoleSpam()

    /**
     * Returns a str which is identical to str, except that each backslash character
     * is duplicated.
     * @param str The string to apply the function to. Immutable parameter.
     * @return Returns str with duplicated backslashes.
     */
    protected static String duplicateBackslashes(String str) {
        char[] chars = str.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < chars.length; i++) {
            sb.append(chars[i]);
            if (chars[i] == '\\') {sb.append('\\');}
        }
        return sb.toString();
    }//end duplicateBackslashes

    /**
     * Converts bounding rectangle measurements into a roi array, as one
     * might receive from the roi manager.
     * @param rt The results table with the bounding box measurements
     * @return returns a roi array, or null if the bounding columns are not available
     */
    public static Roi[] getRoisFromBounds(ResultsTable rt) {
        Roi[] rois = new Roi[rt.size()];
        int xi = rt.getColumnIndex("BX");
        int yi = rt.getColumnIndex("BY");
        int wi = rt.getColumnIndex("Width");
        int hi = rt.getColumnIndex("Height");
        if (
            xi == ResultsTable.COLUMN_NOT_FOUND || 
            yi == ResultsTable.COLUMN_NOT_FOUND || 
            wi == ResultsTable.COLUMN_NOT_FOUND || 
            hi == ResultsTable.COLUMN_NOT_FOUND
        ) {return null;}
        for (int i = 0; i < rt.size(); i++) {
            rois[i] = new Roi(
                rt.getValueAsDouble(xi, i),
                rt.getValueAsDouble(yi, i),
                rt.getValueAsDouble(wi, i),
                rt.getValueAsDouble(hi, i)
            );
        }//end getting rois from each row of the results table
        return rois;
    }//end getRoisFromBounds()
}//end class ParticleAnalyzer
