/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package View;

import Config.OutputConfig;
import Config.ProcessingConfig;
import Config.ScanConfig;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import java.awt.Cursor;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

import IJM.IJProcess;
import Scan.Scan;
import SimpleResult.SimpleResult;
import Utils.Constants;
import Utils.Result;
import Utils.Result.ResultType;
import ij.IJ;

/**
 *
 * @author Nicholas.Sixbury
 */
public class MainWindow extends javax.swing.JFrame {

    protected Scan scan = null;
    private JFileChooser selectFilesChooser = new JFileChooser();
    private File lastScannedFile = null;
    /**
     * Holds the images to eventually process. This should only contain images that have yet to be processed.
     */
    private List<File> imageQueue = new ArrayList<File>();
    /**
     * Holds all images added, regardless of whether or not they've been processed
     */
    private List<File> allImages = new ArrayList<File>();
    // where displayed image was last selected from
    private LastSelectedFrom lastSelectedFrom = LastSelectedFrom.NoSelection;
    // dialog boxes we can re-use
    // private AreaFlagDialog areaFlagDialog = new AreaFlagDialog(this, true);
    // private ThresholdDialog thresholdDialog = new ThresholdDialog(this, true);
    // progress bar for imagej processing
    ProgressMonitor progressMonitor;
    // task for background work
    IJTask ijTask;
    PrintStream ps;
    ScanConfig scanConfig = new ScanConfig();
    OutputConfig outputConfig = new OutputConfig();
    ProcessingConfig procConfig = new ProcessingConfig();

    /**
     * enum added for use in keeping track of whether displayed image was selected from QueueList or OutputTable
     */
    private enum LastSelectedFrom {
        QueueList,
        OutputTable,
        NoSelection
    }

    /**
     * Creates new form MainWindow
     */
    public MainWindow() {
        // read config files
        scanConfig.readConfig();
        outputConfig.readConfig();
        procConfig.readConfig();
        
        // try and figure out if we should use dark mode
        boolean useDarkMode = false;
        // set the application theme / look and feel
        if (useDarkMode) { FlatDarkLaf.setup(); }
        else { FlatLightLaf.setup(); }

        // set up file listeners
        selectFilesChooser.addActionListener(selectFilesListener);
        selectFilesChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        selectFilesChooser.setMultiSelectionEnabled(true);
        // ijProcFileChooser.addActionListener(ijProcFileListener);
        
                initComponents();

        // maximize the window
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);

        // build title block
        StringBuilder tb = new StringBuilder();
        tb.append(Constants.LOCATION);
        tb.append("\t");
        tb.append(Constants.PROGRAM_NAME);
        tb.append("\n");
        tb.append(Constants.DATE());
        tb.append("\t");
        tb.append(Constants.VERSION);
        tb.append("\t");
        tb.append(Constants.PEOPLE);
        tb.append("\n");
        tb.append("To process images from EPSON v850 Scanner\n");
        tb.append("To collect transmissive images of durum samples in a big clear-ish grid\n");
        tb.append("Process image to estimate %chalk");
        uxTitleBlockTxt.setText(tb.toString());

        // configure the table model
        ListSelectionListener lsl = new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int idx = uxOutputTable.getSelectedRow();
                    String selected_filename = uxOutputTable.getModel().getValueAt(idx, 0).toString();
                    // call method to update image label
                    updateImageDisplay(selected_filename);
                    // update properties display
                    uxImagePropertiesTxt.setText("Image: " + selected_filename + "\nSelected From: OutputTable[" + idx + "]");
                    // update flags and such
                    lastSelectedFrom = LastSelectedFrom.OutputTable;
                }//end if the value is done adjusting
            }//end valueChanged(e)
        };
        uxOutputTable.getSelectionModel().addListSelectionListener(lsl);

        // mess with the jtable so that column text is centered
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment( JLabel.CENTER );
        uxOutputTable.setDefaultRenderer(String.class, centerRenderer);

        // set up uxStatusTxt to get redirected all the console output
        PrintStream printStream = new PrintStream(new uxStatusTxtIOStream(uxStatusTxt));
        System.setOut(printStream);
        System.setErr(printStream);
        this.ps = printStream;

        ijTask = new IJTask(imageQueue, printStream);
    }//end MainWindow constructor

    /**
     * Shows a pretty generic message box giving the name and message of supplied error. Uses JOptionPane
     * @param e The exception that was generated.
     */
    protected static void showGenericExceptionMessage(Exception e) {
        JOptionPane.showMessageDialog(null, "While attempting the chosen command, the program encountered an exception of type " + e.getClass().getName() + ".\nThe exception message was " + e.getMessage(), "Unhandled Exception Caught.", JOptionPane.ERROR_MESSAGE);
    }//end genericExceptionMessage(e)

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane4 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jMenuItem1 = new javax.swing.JMenuItem();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel3 = new javax.swing.JPanel();
        jSplitPane2 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        uxStatusTxt = new javax.swing.JTextArea();
        uxConnectToScannerBtn = new javax.swing.JButton();
        uxScanBigBtn = new javax.swing.JButton();
        uxScanQueueBtn = new javax.swing.JButton();
        uxAddFilesBtn = new javax.swing.JButton();
        uxProcessAllBtn = new javax.swing.JButton();
        uxEmptyQueueBtn = new javax.swing.JButton();
        jScrollPane6 = new javax.swing.JScrollPane();
        uxTitleBlockTxt = new javax.swing.JTextArea();
        uxOverwriteName = new javax.swing.JTextField();
        uxShouldOverwriteName = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        uxQueueList = new javax.swing.JList<>();
        jPanel4 = new javax.swing.JPanel();
        jSplitPane3 = new javax.swing.JSplitPane();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        uxImagePropertiesTxt = new javax.swing.JTextArea();
        uxPrevImageBtn = new javax.swing.JButton();
        uxNextImageBtn = new javax.swing.JButton();
        uxOpenFileBtn = new javax.swing.JButton();
        uxImageLabel = new javax.swing.JLabel();
        uxClearOutputBtn = new javax.swing.JButton();
        uxOpenOutputFile = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        uxOutputTable = new javax.swing.JTable();
        jMenuBar1 = new javax.swing.JMenuBar();
        uxInitMenu = new javax.swing.JMenu();
        uxConnectScannerBtn = new javax.swing.JMenuItem();
        uxResetScanner = new javax.swing.JMenuItem();
        uxRunMenu = new javax.swing.JMenu();
        uxScanBtn = new javax.swing.JMenuItem();
        uxIjBtn = new javax.swing.JMenuItem();

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane4.setViewportView(jTable1);

        jMenuItem1.setText("jMenuItem1");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("USDA-ARS-FlourScan-Java");

        jSplitPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jSplitPane1.setDividerLocation(690);

        jPanel3.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        jSplitPane2.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jSplitPane2.setDividerLocation(450);
        jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        uxStatusTxt.setEditable(false);
        uxStatusTxt.setColumns(20);
        uxStatusTxt.setFont(uxStatusTxt.getFont());
        uxStatusTxt.setRows(5);
        jScrollPane1.setViewportView(uxStatusTxt);

        uxConnectToScannerBtn.setFont(uxConnectToScannerBtn.getFont().deriveFont(uxConnectToScannerBtn.getFont().getSize()+2f));
        uxConnectToScannerBtn.setText("Connect to Scanner");
        uxConnectToScannerBtn.setToolTipText("Attempts to make a connection to the scanner, if one is connected.");
        uxConnectToScannerBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                uxConnectToScannerBtnActionPerformed(evt);
            }
        });

        uxScanBigBtn.setFont(uxScanBigBtn.getFont().deriveFont(uxScanBigBtn.getFont().getSize()+2f));
        uxScanBigBtn.setText("Scan");
        uxScanBigBtn.setToolTipText("Scans and saves an image without adding it to the queue.");
        uxScanBigBtn.setEnabled(false);
        uxScanBigBtn.setVisible(false);
        uxScanBigBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                uxScanBigBtnActionPerformed(evt);
            }
        });

        uxScanQueueBtn.setFont(uxScanQueueBtn.getFont().deriveFont(uxScanQueueBtn.getFont().getSize()+2f));
        uxScanQueueBtn.setText("Scan + Add to Queue");
        uxScanQueueBtn.setToolTipText("Scans and saves an image, then adds it to the processing queue.");
        uxScanQueueBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                uxScanQueueBtnActionPerformed(evt);
            }
        });

        uxAddFilesBtn.setFont(uxAddFilesBtn.getFont().deriveFont(uxAddFilesBtn.getFont().getSize()+2f));
        uxAddFilesBtn.setText("Add Files to Queue");
        uxAddFilesBtn.setToolTipText("Select image files to add to the queue for processing.");
        uxAddFilesBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                uxAddFilesBtnActionPerformed(evt);
            }
        });

        uxProcessAllBtn.setFont(uxProcessAllBtn.getFont().deriveFont(uxProcessAllBtn.getFont().getSize()+2f));
        uxProcessAllBtn.setText("Process Queue");
        uxProcessAllBtn.setToolTipText("Processes all images in the queue, displaying results in the output table.");
        uxProcessAllBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                uxProcessAllBtnActionPerformed(evt);
            }
        });

        uxEmptyQueueBtn.setFont(uxEmptyQueueBtn.getFont().deriveFont(uxEmptyQueueBtn.getFont().getSize()+2f));
        uxEmptyQueueBtn.setText("Empty Queue");
        uxEmptyQueueBtn.setToolTipText("Clears all files from the processing queue.");
        uxEmptyQueueBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                uxEmptyQueueBtnActionPerformed(evt);
            }
        });

        uxTitleBlockTxt.setEditable(false);
        uxTitleBlockTxt.setColumns(20);
        uxTitleBlockTxt.setFont(uxTitleBlockTxt.getFont());
        uxTitleBlockTxt.setRows(5);
        jScrollPane6.setViewportView(uxTitleBlockTxt);

        uxOverwriteName.setFont(uxOverwriteName.getFont().deriveFont(uxOverwriteName.getFont().getSize()+2f));

        uxShouldOverwriteName.setFont(uxShouldOverwriteName.getFont());
        uxShouldOverwriteName.setText("Auto-Gen Image Name");
        uxShouldOverwriteName.setToolTipText("If selected, then the program will automatically generate a name for scanned images.");
        uxShouldOverwriteName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                uxShouldOverwriteNameActionPerformed(evt);
            }
        });

        jLabel1.setFont(jLabel1.getFont().deriveFont(jLabel1.getFont().getSize()+1f));
        jLabel1.setText("Scanned Image Name");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1)
            .addComponent(jScrollPane6)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(uxConnectToScannerBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(uxScanBigBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(uxScanQueueBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(uxShouldOverwriteName))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(uxAddFilesBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(uxProcessAllBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(uxEmptyQueueBtn)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(uxOverwriteName)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(uxConnectToScannerBtn)
                    .addComponent(uxScanBigBtn)
                    .addComponent(uxScanQueueBtn)
                    .addComponent(uxShouldOverwriteName))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(uxAddFilesBtn)
                    .addComponent(uxProcessAllBtn)
                    .addComponent(uxEmptyQueueBtn))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(uxOverwriteName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addContainerGap(173, Short.MAX_VALUE))
        );

        jSplitPane2.setTopComponent(jPanel1);

        jPanel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        uxQueueList.setFont(uxQueueList.getFont().deriveFont(uxQueueList.getFont().getSize()+2f));
        uxQueueList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        uxQueueList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                uxQueueListValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(uxQueueList);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 671, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE)
                .addContainerGap())
        );

        jSplitPane2.setRightComponent(jPanel2);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane2)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane2, javax.swing.GroupLayout.Alignment.TRAILING)
        );

        jSplitPane1.setLeftComponent(jPanel3);

        jPanel4.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        jSplitPane3.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jSplitPane3.setDividerLocation(450);
        jSplitPane3.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jPanel5.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        uxImagePropertiesTxt.setEditable(false);
        uxImagePropertiesTxt.setColumns(1);
        uxImagePropertiesTxt.setFont(uxImagePropertiesTxt.getFont().deriveFont(uxImagePropertiesTxt.getFont().getSize()+1f));
        uxImagePropertiesTxt.setRows(1);
        uxImagePropertiesTxt.setEnabled(false);
        uxImagePropertiesTxt.setPreferredSize(new java.awt.Dimension(102, 84));
        jScrollPane3.setViewportView(uxImagePropertiesTxt);

        uxPrevImageBtn.setFont(uxPrevImageBtn.getFont().deriveFont(uxPrevImageBtn.getFont().getSize()+2f));
        uxPrevImageBtn.setText("Previous Image");
        uxPrevImageBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                uxPrevImageBtnActionPerformed(evt);
            }
        });

        uxNextImageBtn.setFont(uxNextImageBtn.getFont().deriveFont(uxNextImageBtn.getFont().getSize()+2f));
        uxNextImageBtn.setText("Next Image");
        uxNextImageBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                uxNextImageBtnActionPerformed(evt);
            }
        });

        uxOpenFileBtn.setFont(uxOpenFileBtn.getFont().deriveFont(uxOpenFileBtn.getFont().getSize()+2f));
        uxOpenFileBtn.setText("Open in File Explorer");
        uxOpenFileBtn.setToolTipText("Opens the Selected Image in File Explorer");
        uxOpenFileBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                uxOpenFileBtnActionPerformed(evt);
            }
        });

        uxImageLabel.setFont(uxImageLabel.getFont().deriveFont(uxImageLabel.getFont().getSize()+2f));
        uxImageLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        uxClearOutputBtn.setFont(uxClearOutputBtn.getFont().deriveFont(uxClearOutputBtn.getFont().getSize()+2f));
        uxClearOutputBtn.setText("Clear Output");
        uxClearOutputBtn.setToolTipText("Clears data from the Output Table below");
        uxClearOutputBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                uxClearOutputBtnActionPerformed(evt);
            }
        });

        uxOpenOutputFile.setFont(uxOpenOutputFile.getFont().deriveFont(uxOpenOutputFile.getFont().getSize()+2f));
        uxOpenOutputFile.setText("Open Output Dir");
        uxOpenOutputFile.setToolTipText("Opens directory containing output files in File Explorer");
        uxOpenOutputFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                uxOpenOutputFileActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(uxOpenFileBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 239, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(uxPrevImageBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(uxNextImageBtn))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(uxOpenOutputFile)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(uxClearOutputBtn))
                    .addComponent(jScrollPane3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(uxImageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 335, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(uxPrevImageBtn)
                            .addComponent(uxNextImageBtn))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(uxOpenFileBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(uxOpenOutputFile)
                            .addComponent(uxClearOutputBtn))
                        .addGap(0, 177, Short.MAX_VALUE))
                    .addComponent(uxImageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        jSplitPane3.setTopComponent(jPanel5);

        jPanel6.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        uxOutputTable.setFont(uxOutputTable.getFont());
        uxOutputTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "FileID", "TH", "left cnt", "rght cnt", "left L*", "rght L*", "Avg L*", "left %A", "rght %A", "Avg %A", "Flag"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        uxOutputTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        uxOutputTable.setColumnSelectionAllowed(true);
        uxOutputTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        uxOutputTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        uxOutputTable.setShowGrid(true);
        jScrollPane5.setViewportView(uxOutputTable);
        uxOutputTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        if (uxOutputTable.getColumnModel().getColumnCount() > 0) {
            uxOutputTable.getColumnModel().getColumn(0).setPreferredWidth(150);
            uxOutputTable.getColumnModel().getColumn(1).setPreferredWidth(50);
        }

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 590, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE)
                .addContainerGap())
        );

        jSplitPane3.setRightComponent(jPanel6);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane3)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane3)
        );

        jSplitPane1.setRightComponent(jPanel4);

        uxInitMenu.setText("Init");
        uxInitMenu.setFocusable(false);
        uxInitMenu.setFont(uxInitMenu.getFont().deriveFont(uxInitMenu.getFont().getSize()+2f));

        uxConnectScannerBtn.setFont(uxConnectScannerBtn.getFont().deriveFont(uxConnectScannerBtn.getFont().getSize()+2f));
        uxConnectScannerBtn.setText("Connect Scanner");
        uxConnectScannerBtn.setToolTipText("Attempt to establish a connection to a scanner.");
        uxConnectScannerBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                uxConnectScannerBtnActionPerformed(evt);
            }
        });
        uxInitMenu.add(uxConnectScannerBtn);

        uxResetScanner.setFont(uxResetScanner.getFont().deriveFont(uxResetScanner.getFont().getSize()+2f));
        uxResetScanner.setText("Reset Scanner");
        uxResetScanner.setToolTipText("Resets the connection to the scanner by attempting to close and reopen a connection.");
        uxResetScanner.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                uxResetScannerActionPerformed(evt);
            }
        });
        uxInitMenu.add(uxResetScanner);

        jMenuBar1.add(uxInitMenu);

        uxRunMenu.setText("Run");
        uxRunMenu.setFont(uxRunMenu.getFont().deriveFont(uxRunMenu.getFont().getSize()+2f));

        uxScanBtn.setFont(uxScanBtn.getFont().deriveFont(uxScanBtn.getFont().getSize()+2f));
        uxScanBtn.setText("Scan");
        uxScanBtn.setToolTipText("Scans and saves an image without adding it to the queue.");
        uxScanBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                uxScanBtnActionPerformed(evt);
            }
        });
        uxRunMenu.add(uxScanBtn);

        uxIjBtn.setFont(uxIjBtn.getFont().deriveFont(uxIjBtn.getFont().getSize()+2f));
        uxIjBtn.setText("ImageJ Process");
        uxIjBtn.setToolTipText("Processes the images in the queue, displaying results in the output table.");
        uxIjBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                uxIjBtnActionPerformed(evt);
            }
        });
        uxRunMenu.add(uxIjBtn);

        jMenuBar1.add(uxRunMenu);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSplitPane1)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSplitPane1)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public class uxStatusTxtIOStream extends OutputStream {
        private javax.swing.JTextArea uxStatusTxt;

        public uxStatusTxtIOStream(javax.swing.JTextArea uxStatusTxt) {
            this.uxStatusTxt = uxStatusTxt;
        }//end constructor

        @Override
        public void write(int b) throws IOException {
            // use invokeLater to deal with the threading stuff
            SwingUtilities.invokeLater(() -> {
                // Convert byte to char and append to JTextArea
                uxStatusTxt.append(String.valueOf((char)b));
                // Automatically scroll to the bottom
                uxStatusTxt.setCaretPosition(uxStatusTxt.getDocument().getLength());
                uxStatusTxt.paintImmediately(uxStatusTxt.getBounds());
            });
        }
        
    }

    private void uxScanBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_uxScanBtnActionPerformed
        // PerformScan();
        uxScanQueueBtnActionPerformed(evt);
    }//GEN-LAST:event_uxScanBtnActionPerformed

    private void uxIjBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_uxIjBtnActionPerformed
        uxProcessAllBtnActionPerformed(evt);
    }//GEN-LAST:event_uxIjBtnActionPerformed

    private ActionListener selectFilesListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println(e.getActionCommand());
            if (e.getActionCommand() == "ApproveSelection") {
                File[] selectedFiles = selectFilesChooser.getSelectedFiles();
                // uxScannedFileTxt.setText(lastScannedFile.getPath());
                for (int i = 0; i < selectedFiles.length; i++) {
                    uxStatusTxt.append("\"" + selectedFiles[i].getAbsolutePath() + "\"\n");
                    imageQueue.add(selectedFiles[i]);
                    allImages.add(selectedFiles[i]);
                }//end adding each selected file to the queue
            }//end if selection was approved
            else if (e.getActionCommand() == "CancelSelection") {
                uxStatusTxt.append("File selection cancelled.\n");
            }//end else if selection was cancelled
        }//end actionPerformed
    };

    private void uxConnectScannerBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_uxConnectScannerBtnActionPerformed
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        // check for scanner already initialized
        if (scan != null) {
            JOptionPane.showMessageDialog(this, "A scanner is already connected. Please disconnect the current scanner before connecting a new one.", "Scanner already connected", JOptionPane.ERROR_MESSAGE);
        }//end if scan isn't null
        
        scan = new Scan();
        // try to access the scanner source
        Result<ResultType> initScannerResult = scan.initScanner();
        if (initScannerResult.isErr()) {
            showGenericExceptionMessage(initScannerResult.getError());
            // reset scanner to null
            scan = null;
        }//end if we encountered an error while detecting the connected scanner
        else {uxStatusTxt.append("Connected to scanner.\n");}
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_uxConnectScannerBtnActionPerformed

    private void uxResetScannerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_uxResetScannerActionPerformed
        if (scan == null) {
            JOptionPane.showMessageDialog(this, "The scanner is already disconnected. It can't be reset further.", "Scanner already disconnected", JOptionPane.ERROR_MESSAGE);
        }//end if scan is already null
        else {
            Result<ResultType> closeResult = scan.closeScanner();
            if (closeResult.isErr()) {
                showGenericExceptionMessage(closeResult.getError());
            }//end if closing scanner resulted in error
            scan = null;
        }//end else we need to reset scanner connection
    }//GEN-LAST:event_uxResetScannerActionPerformed

    /**
     * This method performs the operation of scanning an image. It then returns the resulting file if successful, or the exception wrapped in a result if not.
     * @return Returns a Result wrapped File.
     */
    private Result<File> PerformScan() {
        System.out.println("You clicked the \"Scan\" button.");
        // try to set scanner settings
        Result<ResultType> setScanSettingResult = scan.setScanSettings(this.scanConfig);
        if (setScanSettingResult.isErr()) {
            showGenericExceptionMessage(setScanSettingResult.getError());
            // reset scan to null
            scan = null;
        }//end if we encountered an error while setting scan settings
        // try to scan something with the scanner
        Result<String> scanResult = scan.runScanner(uxOverwriteName.getText(), !uxShouldOverwriteName.isSelected());
        if (scanResult.isOk()) {
            String result = scanResult.getValue();
            if (scanConfig.unsharp_skip == true) {
                lastScannedFile = new File(result);
                return new Result<File>(lastScannedFile);
            }//end if we should just skip the unsharp process
            else {
                SimpleResult<String> unsharpResult = IJProcess.doUnsharpCorrection(result, scanConfig.unsharp_sigma, scanConfig.unsharp_weight, scanConfig.unsharp_rename);
                if (unsharpResult.isOk()) {
                    lastScannedFile = new File(unsharpResult.getValue());
                    return new Result<File>(lastScannedFile);
                }//end if we have an ok result
                else {
                    return new Result<File>(unsharpResult.getError());
                }//end else we have an error to show
            }//end else we should do the unsharp correction
        }//end else if scan result is ok
        else {
            return new Result<File>(scanResult.getError());
        }//end if we have an error to show
    }//end method PerformScan()

    /**
     * This method should be called whenever the image queue is updated, in order to show the changes in the list.
     */
    private void UpdateQueueList() {
        uxQueueList.removeAll();
        String[] imageArray = new String[imageQueue.size()];
        for(int i = 0; i < imageArray.length; i++) {
            imageArray[i] = imageQueue.get(i).getName();
        }//end adding each image file to the array
        uxQueueList.setListData(imageArray);
    }//end UpdateQueueList()

    /**
     * Figures out where currently selected image is located, finds image after it, then changes selection of displayed image.
     */
    private void uxNextImageBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_uxNextImageBtnActionPerformed
        if (lastSelectedFrom == LastSelectedFrom.QueueList) {
            int curIdx = uxQueueList.getSelectedIndex();
            if (curIdx + 1 < uxQueueList.getModel().getSize() && curIdx + 1 >= 0) {
                uxQueueList.setSelectedIndex(curIdx + 1);
            }//end if there's another index to go to
        }//end if last selection was from queue list
        else if (lastSelectedFrom == LastSelectedFrom.OutputTable) {
            int curIdx = uxOutputTable.getSelectedRow();
            if (curIdx + 1 < uxOutputTable.getRowCount() && curIdx + 1 >= 0) {
                uxOutputTable.changeSelection(curIdx + 1, 0, false, false);
            }//end if there's another index to go to
        }//end else if last selection was from output table
    }//GEN-LAST:event_uxNextImageBtnActionPerformed

    /**
     * Figures out where currently selected image is located, finds image before it, then changes selection of displayed image.
     */
    private void uxPrevImageBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_uxPrevImageBtnActionPerformed
        if (lastSelectedFrom == LastSelectedFrom.QueueList) {
            int curIdx = uxQueueList.getSelectedIndex();
            if (curIdx - 1 >= 0) {
                uxQueueList.setSelectedIndex(curIdx - 1);
            }//end if there's another index to go to
        }//end if last selection was from queue list
        else if (lastSelectedFrom == LastSelectedFrom.OutputTable) {
            int curIdx = uxOutputTable.getSelectedRow();
            if (curIdx - 1 >= 0) {
                uxOutputTable.changeSelection(curIdx - 1, 0, false, false);
            }//end if there's another index to go to
        }//end else if last selection was from output table
    }//GEN-LAST:event_uxPrevImageBtnActionPerformed

    /**
     * Finds which image is currently selected and displayed, 
     * finds the path of that image, and 
     * then opens + selects that file in file explorer
     */
    private void uxOpenFileBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_uxOpenFileBtnActionPerformed
        // open the file in file explorer
        try {
            String selectedValue = "";
            if (lastSelectedFrom == LastSelectedFrom.QueueList) {selectedValue = uxQueueList.getSelectedValue();}
            else if (lastSelectedFrom == LastSelectedFrom.OutputTable) {selectedValue = uxOutputTable.getModel().getValueAt(uxOutputTable.getSelectedRow(), 0).toString();}
            File imageMatch = getSelectedFileFromAll(selectedValue);
            if (imageMatch == null) {JOptionPane.showMessageDialog(this, "Could not find file that matches selected image, or no image selected.");}
            Runtime.getRuntime().exec("explorer.exe /select," + imageMatch.getAbsolutePath());
        }//end trying to open file explorer
        catch(Exception e) {System.out.println("Couldn't open file explorer"); uxStatusTxt.append("Couldn't open file explorer\n");}
    }//GEN-LAST:event_uxOpenFileBtnActionPerformed

    /**
     * Displays the newly selected file image and results to the right. 
     * Due to how all the architecture is currently set up, this is somewhat awkward.
     * @param evt The list selection events
     */
    private void uxQueueListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_uxQueueListValueChanged
        if (!evt.getValueIsAdjusting() && uxQueueList.getSelectedIndex() != -1) {
            // update the displayed image
            updateImageDisplay(uxQueueList.getSelectedValue());
            // update image properties text
            uxImagePropertiesTxt.setText("Image: " + uxQueueList.getSelectedValue() + "\nSelected From: QueueList[" + uxQueueList.getSelectedIndex() + "]");
            // update some flags and such
            lastSelectedFrom = LastSelectedFrom.QueueList;
        }//end if we're not in a series of adjustments?
    }//GEN-LAST:event_uxQueueListValueChanged

    /**
     * This method updates the image label with the image which has the specified filename (as from File.getName()).
     * This is somewhat awkward, as we have to search through a list of all files to find the one with a matching name.
     * It's also possible that this could cause problems if two images are loaded with the same filename (but different directories).
     * @param filename The filename (as from File.getName()) of the image to display.
     */
    private void updateImageDisplay(String filename) {
        // pick throug the list of files that have been loaded into queue to find the one that matches the selected file name
        File imageMatch = getSelectedFileFromAll(filename);
        // edge case validation
        if (imageMatch == null) {JOptionPane.showMessageDialog(this, "Could not find matching file for selection."); return;}
        // display the image in the label
        ImageIcon icon = scaleImageToIcon(imageMatch);
        if (icon == null) {JOptionPane.showMessageDialog(this, "Could not read selected image to buffer."); return;}
        uxImageLabel.setIcon(icon);
    }//end updateImageDisplay(filename)

    /**
     * A helper method written for uxQueueListValueChange(). This method loops through all 
     * the files in the queue until it finds one whose name matches the file name selected in uxQueueList.
     * @return This method returns the File that matches if found, or null if it couldn't find a match.
     */
    private File getSelectedFileFromAll(String filename) {
        // search through imageQueue for File which matches
        File imageMatch = null;
        for (File this_image : allImages) {
            if (this_image.getName().equals(filename)) {
                imageMatch = this_image;
                break;
            }//end if we found a match
        }//end looping over all images
        return imageMatch;
    }//end getSelectedFileFromQueue()

    /**
     * This method was written as a helper method for uxQueueListValueChanged(). This method reads an image File into memory as
     * a BufferedImage, and then converts that image into an Icon which has been scaled down to fit in the window. 
     * @param imageFile The File representing an image file to be opened and displayed.
     * @return Returns an ImageIcon if the file is found. Otherwise, returns null if we can't open the image.
     */
    private ImageIcon scaleImageToIcon(File imageFile) {
        BufferedImage buf_img = IJ.openImage(imageFile.getAbsolutePath()).getBufferedImage();
        if (buf_img == null) {return null;}
        // It would maybe be good to improve image scaling at some point
        /*
         * It would maybe be good to improve image scaling at some point, as currently, 
         * in order to resize the image, you have to select a different file, which is pretty jank.
         * 
         * The reason we scale the image to less than the size of the label container is that if you set the iamge to the same
         * width and height as that of the container, then the image will be slightly larger than the label, so every time a new
         * image is selected, the size of the label will just continually grow in size. But, if you set the size to 95% or 99%
         * the size of the label, then that doesn't happen for some reason.
         */
        int imgWidth = buf_img.getWidth();
        int imgHeight = buf_img.getHeight();
        if (imgWidth > uxImageLabel.getWidth()) {
            int newImgWidth = (int)((double)uxImageLabel.getWidth() * 0.85);
            int newImgHeight = newImgWidth * imgHeight / imgWidth;
            imgWidth = newImgWidth;
            imgHeight = newImgHeight;
        }//end if we need to scale down because of width
        if (imgHeight > uxImageLabel.getHeight()) {
            int newImgHeight = (int)((double)uxImageLabel.getHeight() * 0.85);
            int newImgWidth = imgWidth * newImgHeight / imgHeight;
            imgHeight = newImgHeight;
            imgWidth = newImgWidth;
        }//end if we need to scale down because of height
        return new ImageIcon(new ImageIcon(buf_img).getImage().getScaledInstance(imgWidth, imgHeight, Image.SCALE_DEFAULT));
    }//end scaleImageToIcon(imageFile)

    // /**
    //  * Updates the output table with the provided results from image processing.
    //  * Working with JTables is kinda jank, so column ordering is hardcoded here.
    //  * This is also where the string formatting for numeric columns in the table is handled.
    //  * @param groupedResults A grouped list of SumResults, likely generated by passing IJM.IJProcess.lastProcResult to IJM.SumResult.GroupResultsByFile().
    //  */
    // private void updateOutputTable(List<List<SumResult>> groupedResults) {
    //     DefaultTableModel this_table_model = (DefaultTableModel)uxOutputTable.getModel();
    //     for (List<SumResult> resultGroup : groupedResults) {
    //         // check that we have valid left and right results
    //         SumResult left = null;
    //         SumResult right = null;
    //         for (SumResult tempResult : resultGroup) {
    //             if (tempResult.leftOrRight == LeftOrRight.Left) {left = tempResult;}
    //             else if (tempResult.leftOrRight == LeftOrRight.Right) {right = tempResult;}
    //         }//end categorizing all of result group
    //         if (left != null && right != null) {
    //             double this_avg_l = (left.l_mean + right.l_mean) / 2.0;
    //             double this_avg_area = (left.percent_area + right.percent_area) / 2.0;
    //             Object[] this_row = new Object[11];
    //             this_row[0] = left.file.getName();
    //             this_row[1] = left.threshold;
    //             this_row[2] = left.count;
    //             this_row[3] = right.count;
    //             this_row[4] = String.format("%3.1f", left.l_mean);
    //             this_row[5] = String.format("%3.1f", right.l_mean);
    //             this_row[6] = String.format("%3.1f", this_avg_l);
    //             this_row[7] = String.format("%4.3f", left.percent_area);
    //             this_row[8] = String.format("%4.3f", right.percent_area);
    //             this_row[9] = String.format("%4.3f", this_avg_area);
    //             if (this_avg_area > areaFlagDialog.firstFlag) {this_row[10] = "x";}
    //             if (this_avg_area > areaFlagDialog.secondFlag) {this_row[10] = "xx";}
    //             this_table_model.addRow(this_row);
    //         }//end if we have proper left and right result
    //         else {
    //             // TODO: Handle exception case
    //         }//end else we need to figure out what to do
    //     }//end looping over each result group
    // }//end updateOutputTable(groupedResults)

    /**
     * Clears the output table
     */
    private void uxClearOutputBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_uxClearOutputBtnActionPerformed
        DefaultTableModel this_table_model = (DefaultTableModel)uxOutputTable.getModel();
        this_table_model.setRowCount(0);
    }//GEN-LAST:event_uxClearOutputBtnActionPerformed

    /**
     * This function finds the directory of output files and opens it in file explorer.
     */
    private void uxOpenOutputFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_uxOpenOutputFileActionPerformed
        String jar_location;
        try {
            // Get path of output folder
            jar_location = new File(IJProcess.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile().toString();
            String output_folder_storage = jar_location + File.separator + Constants.IMAGEJ_OUTPUT_FOLDER_NAME;
            Runtime.getRuntime().exec("explorer.exe " + output_folder_storage);
        } catch (Exception e) {
            showGenericExceptionMessage(e);
        }//end catching any file-related exceptions
    }//GEN-LAST:event_uxOpenOutputFileActionPerformed

    private void uxShouldOverwriteNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_uxShouldOverwriteNameActionPerformed
        if (uxShouldOverwriteName.isSelected()) {uxOverwriteName.setEnabled(false);}
        else {uxOverwriteName.setEnabled(true);}
    }//GEN-LAST:event_uxShouldOverwriteNameActionPerformed

    private void uxEmptyQueueBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_uxEmptyQueueBtnActionPerformed
        imageQueue.clear();
        UpdateQueueList();
    }//GEN-LAST:event_uxEmptyQueueBtnActionPerformed

    /**
     * This method contains the code for initiating processing of all images in the queue and updating things afterwards.
     * @param evt
     */
    private void uxProcessAllBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_uxProcessAllBtnActionPerformed
        if (imageQueue == null || imageQueue.size() == 0) {
            JOptionPane.showMessageDialog(this, "Please select the image file generated by the scanner.", "No scanned image selected", JOptionPane.ERROR_MESSAGE);
        }//end if last scanned file is null
        // else if () {
            //     JOptionPane.showMessageDialog(this, "Please select a scanned image that exists. \nFile " + lastScannedFile.getAbsolutePath() + "\n does not exist.", "Scanned image file does not exist.", JOptionPane.ERROR_MESSAGE);
            // }//end if last scanned file doesn't exist
        else {
            try {
                // tell user we're about to do processing
                // JOptionPane.showMessageDialog(this, "Please wait. Your images will now be processed.");
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                // ijProcess.th01 = thresholdDialog.thresholdToReturn;
                // set up progress bar
                progressMonitor = new ProgressMonitor(this, "Progress!", "", 0, 5);
                progressMonitor.setProgress(3);
                progressMonitor.setMillisToDecideToPopup(0);
                progressMonitor.setMillisToPopup(0);
                // actually run the imagej stuff

                // roll over area flag stuff to the processing
                // IJProcess.lower_flag_thresh = areaFlagDialog.firstFlag;
                // IJProcess.upper_flag_thresh = areaFlagDialog.secondFlag;

                ijTask = new IJTask(imageQueue, ps);
                ijTask.execute();
                // SimpleResult<String> outputData = ijTask.doInBackground();
                if (ijTask.isDone()) {
                    setCursor(Cursor.getDefaultCursor());
                }//end if the task is done
                // SwingUtilities.invokeLater(
                    //     () -> JOptionPane.showMessageDialog(this, "Your images have finsihed processing.")
                    // );
                // progressDialog.setVisible(false);
                // if (outputData.isErr()) {
                //     outputData.getError().printStackTrace();
                //     showGenericExceptionMessage(outputData.getError());
                // }//end if we couldn't get output data
                int prev_row_count = uxOutputTable.getRowCount();
                // group together SumResults which came from the same file path
                // List<List<SumResult>> groupedResults = SumResult.groupResultsByFile(ijProcess.lastProcResult);
                // process sumResults into string columns
                // updateOutputTable(groupedResults);
                // clear queue now that it's been processed
                if (ijTask.isDone()) {
                    imageQueue.clear();
                    UpdateQueueList();
                }
                // see about updating selections
                if (prev_row_count < uxOutputTable.getRowCount()) {
                    uxOutputTable.changeSelection(prev_row_count, 0, false, false);
                }//end if we have a new row to select
                // make sure cursor is updated
                setCursor(Cursor.getDefaultCursor());
            } catch (Exception e) {
                e.printStackTrace();
                showGenericExceptionMessage(e);
            }//end catching URISyntaxException
        }//end else we should probably be able to process the file
    }//GEN-LAST:event_uxProcessAllBtnActionPerformed

    /**
     * Shows file chooser for adding files to processing queue.
     * @param evt
     */
    private void uxAddFilesBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_uxAddFilesBtnActionPerformed
        int prev_list_count = uxQueueList.getModel().getSize();
        // adding selected files to queue should be handled by selectFIlesListener
        selectFilesChooser.showOpenDialog(this);
        UpdateQueueList();
        // make sure that new images in queue list show up to the right
        if (prev_list_count != uxQueueList.getModel().getSize()) {
            uxQueueList.setSelectedValue(uxQueueList.getModel().getElementAt(prev_list_count), true);
        }
    }//GEN-LAST:event_uxAddFilesBtnActionPerformed

    private void uxScanQueueBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_uxScanQueueBtnActionPerformed
        // ensure there is a valid filename to use
        if (!uxShouldOverwriteName.isSelected()) {
            if (uxOverwriteName.getText().equals("")) {
                JOptionPane.showMessageDialog(this, "Please select a name for the scanned image.", "Image name left blank", JOptionPane.ERROR_MESSAGE);
                return;
            }//end if overwrite name is empty
        }//end if we aren't overwriting the name

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        Result<File> scanResult = PerformScan();
        if (scanResult.isErr()) {showGenericExceptionMessage(scanResult.getError());}
        else if (scanResult.isOk()) {
            imageQueue.add(scanResult.getValue());
            allImages.add(scanResult.getValue());
            UpdateQueueList();
            // hopefully ensure that scanned image shows up immediately for user
            uxQueueList.setSelectedValue(scanResult.getValue(), true);
        }//end else if we can add something to the queue

        // ensure scan name is reset if we used it
        if (!uxShouldOverwriteName.isSelected()) {
            uxOverwriteName.setText("");
        }//end if we used the overwrite name

        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_uxScanQueueBtnActionPerformed

    private void uxScanBigBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_uxScanBigBtnActionPerformed
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        Result<File> scanResult = PerformScan();
        if (scanResult.isErr()) {showGenericExceptionMessage(scanResult.getError());}
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_uxScanBigBtnActionPerformed

    private void uxConnectToScannerBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_uxConnectToScannerBtnActionPerformed
        // just trigger the top menu code
        uxConnectScannerBtnActionPerformed(evt);
    }//GEN-LAST:event_uxConnectToScannerBtnActionPerformed

    /**
     * THIS is the MAIN METHOD that the program should start from.
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainWindow().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JSplitPane jSplitPane3;
    private javax.swing.JTable jTable1;
    private javax.swing.JButton uxAddFilesBtn;
    private javax.swing.JButton uxClearOutputBtn;
    private javax.swing.JMenuItem uxConnectScannerBtn;
    private javax.swing.JButton uxConnectToScannerBtn;
    private javax.swing.JButton uxEmptyQueueBtn;
    private javax.swing.JMenuItem uxIjBtn;
    private javax.swing.JLabel uxImageLabel;
    private javax.swing.JTextArea uxImagePropertiesTxt;
    private javax.swing.JMenu uxInitMenu;
    private javax.swing.JButton uxNextImageBtn;
    private javax.swing.JButton uxOpenFileBtn;
    private javax.swing.JButton uxOpenOutputFile;
    private javax.swing.JTable uxOutputTable;
    private javax.swing.JTextField uxOverwriteName;
    private javax.swing.JButton uxPrevImageBtn;
    private javax.swing.JButton uxProcessAllBtn;
    private javax.swing.JList<String> uxQueueList;
    private javax.swing.JMenuItem uxResetScanner;
    private javax.swing.JMenu uxRunMenu;
    private javax.swing.JButton uxScanBigBtn;
    private javax.swing.JMenuItem uxScanBtn;
    private javax.swing.JButton uxScanQueueBtn;
    private javax.swing.JCheckBox uxShouldOverwriteName;
    private javax.swing.JTextArea uxStatusTxt;
    private javax.swing.JTextArea uxTitleBlockTxt;
    // End of variables declaration//GEN-END:variables
}
