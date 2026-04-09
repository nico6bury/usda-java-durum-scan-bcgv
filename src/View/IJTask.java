package View;

import java.io.File;
import java.io.PrintStream;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.SwingWorker;

import IJM.AnalyzeParticles;
import IJM.Durum;
import SimpleResult.SimpleResult;

public class IJTask extends SwingWorker<SimpleResult<String>,String> {

    public List<File> imageQueue;
    public PrintStream processPrintStream;
    public Consumer<SimpleResult<String>> postProcessConsumer;
    public String[][] outputTable;

    /**
     * Make sure to set imageQueue and ijProcess props before calling this.
     */
    @Override
    protected SimpleResult<String> doInBackground() throws Exception {
        AnalyzeParticles.originalPrintStream = processPrintStream;
        this.outputTable = Durum.doProcessing(imageQueue, this::publish);
        return new SimpleResult<String>("null");
    }//end doInBackground()

    @Override
    protected void process(List<String> chunks) {
        for (String string : chunks) {
            processPrintStream.println(string);
        }//end looping over chunks
    }//end process()

    @Override
    protected void done() {
        try {
            get();
            this.postProcessConsumer.accept(new SimpleResult<>("Finished successfully."));
        } catch (Exception e) {
            e.printStackTrace();
            this.postProcessConsumer.accept(new SimpleResult<String>(e));
        }
    }//end done()

    public IJTask(
        List<File> imageQueue, PrintStream processPrintStream, 
        Consumer<SimpleResult<String>> postProcessConsumer
    ) {
        this.imageQueue = imageQueue;
        this.processPrintStream = processPrintStream;
        this.postProcessConsumer = postProcessConsumer;
    }//end 2-arg constructor
}//end class IJTask
