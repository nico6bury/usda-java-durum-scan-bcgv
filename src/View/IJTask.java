package View;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;

import IJM.AnalyzeParticles;
import IJM.Durum;
import SimpleResult.SimpleResult;

public class IJTask extends SwingWorker<SimpleResult<String>,String> {

    public List<File> imageQueue;
    public PrintStream processPrintStream;

    /**
     * Make sure to set imageQueue and ijProcess props before calling this.
     */
    @Override
    protected SimpleResult<String> doInBackground() throws Exception {
        AnalyzeParticles.originalPrintStream = processPrintStream;
        Durum.doProcessing(imageQueue, this::publish);
        return new SimpleResult<String>("null");
    }//end doInBackground()

    @Override
    protected void process(List<String> chunks) {
        for (String string : chunks) {
            processPrintStream.println(string);
        }//end looping over chunks
    }


    public IJTask(List<File> imageQueue, PrintStream processPrintStream) {
        this.imageQueue = imageQueue;
        this.processPrintStream = processPrintStream;
    }//end 2-arg constructor
}//end class IJTask
