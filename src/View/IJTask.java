package View;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;

import IJM.Durum;
import SimpleResult.SimpleResult;

public class IJTask extends SwingWorker<SimpleResult<String>,Exception> {

    public List<File> imageQueue = new ArrayList<File>();

    /**
     * Make sure to set imageQueue and ijProcess props before calling this.
     */
    @Override
    protected SimpleResult<String> doInBackground() throws Exception {
        Durum.doProcessing(imageQueue);
        return new SimpleResult<String>("null");
    }//end doInBackground()

    public IJTask(List<File> imageQueue) {
        this.imageQueue = imageQueue;
    }//end 2-arg constructor
}//end class IJTask
