package Utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Constants {
    // header info
    public static String LOCATION = "USDA-ARS Manhattan, KS";
    public static String DATE() {
        DateTimeFormatter month_year = DateTimeFormatter.ofPattern("MMM/yyyy");
        LocalDateTime currentDateTime = LocalDateTime.now();
        return currentDateTime.format(month_year);
    }//end DATE()
    public static String PEOPLE = "N_Sixbury/W_Rust/D_Brabec";
    public static String PROGRAM_NAME = "Durum Scan & Analysis";
    public static String VERSION = "v. 0.1.0";
    public static String SCANNED_IMAGES_FOLDER_NAME = "scanned-images";
    public static String IMAGEJ_OUTPUT_FOLDER_NAME = "output-folder";
}//end class Constants
