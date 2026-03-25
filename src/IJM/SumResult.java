package IJM;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SumResult {
    public File file;
    public String slice;
    public int count;
    public int total_area;
    public double percent_area;
    public double l_mean;
    public double l_stdv;
    public LeftOrRight leftOrRight = LeftOrRight.Unknown;
    /**
     * The upper threshold used in image processing
     */
    public int threshold;

    public SumResult() {}

    public SumResult(String slice, int count, int total_area, double percent_area) {
        this.slice = slice;
        this.count = count;
        this.total_area = total_area;
        this.percent_area = percent_area;
    }//end constructor

    public SumResult(File file, String slice, int count, int total_area, double percent_area) {
        this.file = file;
        this.slice = slice;
        this.count = count;
        this.total_area = total_area;
        this.percent_area = percent_area;
    }//end constructor

    public SumResult(String slice, int count, int total_area, double percent_area, double l_mean, double l_stdv) {
        this.slice = slice;
        this.count = count;
        this.total_area = total_area;
        this.percent_area = percent_area;
        this.l_mean = l_mean;
        this.l_stdv = l_stdv;
    }//end constructor
    
    public enum LeftOrRight {
        Left,
        Right,
        Unknown,
    }//end enum LeftOrRight

    /**
     * This method was written as a helper method for uxQueueListValueChanged(), but it could also be helpful for other processes.
     * When given a list of SumResults, it will group together SumResults which have the same absolute file path.
     * @param sumResults The list of SumResults to group.
     * @return Returns a list of lists of SumResults. The inner lists are the groupings.
     */
    public static List<List<SumResult>> groupResultsByFile(List<SumResult> sumResults) {
        List<List<SumResult>> groupedResults = new ArrayList<List<SumResult>>();
        List<String> filenames_indexed = new ArrayList<String>();
        for (int i = 0; i < sumResults.size(); i++) {
            SumResult this_result = sumResults.get(i);
            // check if we need to create new list in groupedResults or just add to existing
            if (filenames_indexed.contains(this_result.file.getAbsolutePath())) {
                // find out which index of grouped results contains the right filename
                // theoretically, this should be the last index in groupedResults,
                // so loop backwards through groupedResults to search
                for (int j = groupedResults.size() - 1; j >= 0; j--) {
                    List<SumResult> thisGroupResults = groupedResults.get(j);
                    SumResult this_grouped_result = thisGroupResults.get(0);
                    if (this_grouped_result.file.getAbsolutePath().equals(this_result.file.getAbsolutePath())) {
                        // add this_result to proper place in groupedResults,
                        groupedResults.get(j).add(this_result);
                        // then break out of inner loop
                        break;
                    }//end if we found matching filename
                }//end looping backwards through groupedResults
                // at this point, we should have added this_result already
            }//end if we can add this file to an existing list in groupedResults
            else {
                List<SumResult> temp_result_list = new ArrayList<SumResult>();
                temp_result_list.add(this_result);
                groupedResults.add(temp_result_list);
                filenames_indexed.add(this_result.file.getAbsolutePath());
            }//end else we must add a new entry
        }//end looping through each sum result
        return groupedResults;
    }//end GroupResultsByFile
}//end struct SumResult

