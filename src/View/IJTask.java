package View;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;

import IJM.IJProcess;
import Utils.Result;

public class IJTask extends SwingWorker<Result<String>,Exception> {

    public List<File> imageQueue = new ArrayList<File>();
    public IJProcess ijProcess = new IJProcess();

    /**
     * Make sure to set imageQueue and ijProcess props before calling this.
     */
    @Override
    protected Result<String> doInBackground() throws Exception {
        return ijProcess.runMacro(imageQueue);
    }//end doInBackground()

    public IJTask(List<File> imageQueue, IJProcess ijProcess) {
        this.imageQueue = imageQueue;
        this.ijProcess = ijProcess;
    }//end 2-arg constructor
}//end class IJTask
