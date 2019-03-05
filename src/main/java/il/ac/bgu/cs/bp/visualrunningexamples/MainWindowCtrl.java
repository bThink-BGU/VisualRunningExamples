package il.ac.bgu.cs.bp.visualrunningexamples;

import il.ac.bgu.cs.bp.bpjs.analysis.DfsBProgramVerifier;
import il.ac.bgu.cs.bp.bpjs.analysis.DfsTraversalNode;
import il.ac.bgu.cs.bp.bpjs.analysis.ExecutionTraceInspections;
import il.ac.bgu.cs.bp.bpjs.analysis.VerificationResult;
import il.ac.bgu.cs.bp.bpjs.analysis.violations.Violation;
import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.StringBProgram;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.PausingEventSelectionStrategyDecorator;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.SimpleEventSelectionStrategy;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

/**
 * Simple class running a BPjs program that selects "hello world" events.
 * @author michael
 */
public class MainWindowCtrl {
    
    private JFrame window;
    private JTabbedPane tabs;
    private RSyntaxTextArea programEditor, additionsEditor;
    private JTextArea mazeEditor;
    private JComboBox<String> programCB, mazeCB;
    private JTable mazeMonitorTable;
    MazeTableModel mazeTableModel;
    private JList logList;
    private final DefaultListModel<String> logModel = new DefaultListModel<>();
    private final MazeRepo mazes = new MazeRepo();
    private final CodeRepo codes = new CodeRepo();
    private JButton runBtn, verifyBtn, stopBtn;
    private BProgramRunner bprogramRunner = null;
    private boolean mazeChanged = true;
    private boolean drawLines = false;
    private boolean isRunning = false;
    
    private void start() {
        createComponents();
        addListeners();
        
        window = new JFrame("BPjs - Visual Running Examples");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        mazeCB.setSelectedIndex(0);
        programCB.setSelectedIndex(0);
        additionsEditor.setText(codes.get("VerificationAdditions"));
        window.getContentPane().add(tabs);
        window.pack();
        window.setVisible(true);
        
    }
    
    private void runBprogram() {
        
        setInProgress(true);
        drawLines=false;
        mazeTableCellRenderer.setShowAnyAge(false);
        
        // Setup the b-program from the source
        BProgram bprog = new StringBProgram(programEditor.getText() );
        bprog.setWaitForExternalEvents( true );
        bprogramRunner = new BProgramRunner(bprog);
        
        // add the maze
        String mazeJs = mazeTableModel.getRows().stream().map( r -> "\"" + r + "\"").collect( joining(",", "maze=[", "];"));
        bprog.prependSource(mazeJs);
        
        bprogramRunner.addListener(new BProgramRunnerListenerImpl(this));
        
        // set pausing ESS
        PausingEventSelectionStrategyDecorator pausingESS =
                new PausingEventSelectionStrategyDecorator(new SimpleEventSelectionStrategy());
        bprog.setEventSelectionStrategy(pausingESS);
        
        pausingESS.setListener( pess -> {
            try {
                Thread.sleep(150);
                pess.unpause();
            } catch (InterruptedException ex) {
                System.err.println("Interrupted during event pause");
            }
        });
            
        // cleanup
        logModel.clear();
        mazeTableModel.resetCellEntries();
        
        // go!
        new Thread(()->{
            try{
                bprogramRunner.run();
            } catch ( Exception e ) {
                e.printStackTrace(System.out);
                addToLog(e.getMessage());
                setInProgress(false);
            }
        }).start();
    }
    
    private void verifyBProgram() {
        setInProgress(true);
        stopBtn.setEnabled(false);
        
        BProgram bprog = new StringBProgram(programEditor.getText() );
        
        // add the maze
        String mazeJs = mazeTableModel.getRows().stream().map( r -> "\"" + r + "\"").collect( joining(",", "maze=[", "];"));
        bprog.prependSource(mazeJs);
        
        // Mix in the assumptions and requirements
        bprog.appendSource(additionsEditor.getText());
        
        DfsBProgramVerifier vfr = new DfsBProgramVerifier();
        vfr.setIterationCountGap(100);
        
        vfr.addInspection( ExecutionTraceInspections.FAILED_ASSERTIONS );
        
        vfr.setProgressListener( new DfsBProgramVerifier.ProgressListener() {
            @Override
            public void started(DfsBProgramVerifier v) {
                addToLog("Verification started");
            }

            @Override
            public void iterationCount(long count, long statesHit, DfsBProgramVerifier v) {
                addToLog(" ~ " + count + " iterations, " + statesHit + " states visited.");
            }

            @Override
            public void maxTraceLengthHit(List<DfsTraversalNode> trace, DfsBProgramVerifier v) {
                addToLog(" (max trace length hit)");
            }

            @Override
            public void done(DfsBProgramVerifier v) {
                addToLog("Verification done");
                setInProgress(false);
            }

            @Override
            public boolean violationFound(Violation aViolation, DfsBProgramVerifier vfr) {
                return false; // do not continue searching,
            }
        });
        
        logModel.clear();
        mazeTableModel.resetCellEntries();
        
        // go!
        new Thread(()->{
            try{
                VerificationResult res = vfr.verify(bprog);
                SwingUtilities.invokeLater(()->setVerificationResult(res));
            } catch ( Exception e ) {
                e.printStackTrace(System.out);
                addToLog(e.getMessage());
                setInProgress(false);
            }
        }).start();
        
    }
    
    private void setVerificationResult(VerificationResult res) {
        mazeTableCellRenderer.setShowAnyAge(true);
        addToLog("---");
        addToLog("Verification completed: " + (res.isViolationFound()? "" : "no ") + "counterexample found.");
        addToLog(String.format("Visited %,d states.", res.getScannedStatesCount()));
        addToLog(String.format("%,d milliseconds", res.getTimeMillies()));
        drawLines = true;
        res.getViolation().ifPresent( violation -> {
            addToLog("Violation: " + violation.decsribe() );
            List<String> entryEventNames = violation.getCounterExampleTrace().getNodes().stream()
                .filter( node->node.getEvent().isPresent() )
                .map( node -> node.getEvent().get().getName() )
                .filter( s->s.startsWith("Enter") )
                .collect( toList() );

            entryEventNames.forEach( s -> {
               String[] comps = s.substring(7).split(",");
                comps[1] = comps[1].replace(")", "");
                mazeTableModel.addCellEntry(Integer.valueOf(comps[1]), Integer.valueOf(comps[0])); 
            });

            mazeTableModel.fireTableDataChanged();
        });
    }
    
    void setInProgress(boolean inProgress) {
        SwingUtilities.invokeLater(()->{
            isRunning = inProgress;
            stopBtn.setEnabled(inProgress);
            runBtn.setEnabled(!inProgress);
            verifyBtn.setEnabled(!inProgress);
        });
    }
    
    void addToLog( String msg ) {
        SwingUtilities.invokeLater(()->{
            if ( logModel.size() > 30*1024 ) {
                logModel.clear();
            }
            logModel.addElement(msg);
            logList.scrollRectToVisible(logList.getCellBounds(logModel.getSize()-1, logModel.getSize()));
        });
    }
    
    private void addListeners() {
        mazes.getMazeNames().forEach( mazeCB::addItem );
        mazeCB.addActionListener( a -> {
            String value = mazeCB.getSelectedItem().toString();
            String[] maze = mazes.get(value);
            mazeCB.setPopupVisible(false);
            mazeEditor.setText(Arrays.asList(maze).stream().collect( joining("\n")));
        });
        
        codes.getModelNames().forEach( programCB::addItem );
        programCB.addActionListener( a -> {
            String value = programCB.getSelectedItem().toString();
            programCB.setPopupVisible(false);
            programEditor.setText(codes.get((String) programCB.getSelectedItem()));
            programEditor.setCaretPosition(0);
        });
        
        tabs.addChangeListener( changeEvt -> {
            if ( tabs.getSelectedIndex() == 2 ) {
                // we moved to the monitor
                if ( mazeChanged ) {
                    mazeTableModel.setRows(Arrays.asList(mazeEditor.getText().split("\\n")));
                    mazeChanged = false;
                }
            }
        });
        
        runBtn.addActionListener(e->runBprogram());
        stopBtn.addActionListener( c -> {
            if ( bprogramRunner!=null ) {
                bprogramRunner.halt();
            }
            setInProgress(false);
        });
        verifyBtn.addActionListener( a->verifyBProgram() );
        DocumentListener documentListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                mazeChanged=true;
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                mazeChanged=true;
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                mazeChanged=true;
            }
        };
        
        mazeEditor.getDocument().addDocumentListener(documentListener);
        
        mazeMonitorTable.addMouseListener( new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = mazeMonitorTable.rowAtPoint(e.getPoint());
                int col = mazeMonitorTable.columnAtPoint(e.getPoint());
                
                if ( isRunning ) {
                    BEvent enterEvent = new BEvent("Enter (" + col + ","  + row + ")");
                    System.out.println("enqueuing " + enterEvent);
                    bprogramRunner.getBProgram().enqueueExternalEvent(enterEvent);
                }
            }
            
        });
    }
    
    private void createComponents() {
        tabs = new JTabbedPane();
        
        ////
        // Code tab
        programEditor = new RSyntaxTextArea();
        programEditor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
        programEditor.setRows(40);
        programEditor.setColumns(80);
         
        programCB = new JComboBox<>();
        
        JTabbedPane bprogTabs = new JTabbedPane();
        
        JPanel pnl = new JPanel( new BorderLayout() );
        pnl.add(new JLabel("BP Model"), BorderLayout.WEST);
        pnl.add( programCB, BorderLayout.CENTER );
        
        JPanel topPanePanel = new JPanel( new BorderLayout() );
        topPanePanel.add( pnl, BorderLayout.NORTH );
        topPanePanel.add( new JScrollPane(programEditor), BorderLayout.CENTER );
        bprogTabs.addTab("Model", addInsets(topPanePanel));
        
        
        additionsEditor = new RSyntaxTextArea();
        additionsEditor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
        additionsEditor.setRows(40);
        additionsEditor.setColumns(80);
        
        bprogTabs.addTab("Verification Additions", addInsets(new JScrollPane(additionsEditor)));
        
        tabs.addTab("BProgram", addInsets(bprogTabs));
        
        //
        ////
        // maze tab
        
        mazeCB = new JComboBox<>();
        mazeCB.setEditable(false);
        
        pnl = new JPanel( new BorderLayout() );
        pnl.add(new JLabel("Maze"), BorderLayout.WEST);
        pnl.add( mazeCB, BorderLayout.CENTER );
        
        mazeEditor = new JTextArea();
        mazeEditor.setFont( new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        topPanePanel = new JPanel( new BorderLayout() );
        topPanePanel.add( pnl, BorderLayout.NORTH );
        topPanePanel.add( new JScrollPane(mazeEditor), BorderLayout.CENTER );
        tabs.addTab("Maze", addInsets(topPanePanel));
        
        //
        ////
        // monitor tab
        Box buttons = Box.createHorizontalBox();
        buttons.add( Box.createGlue() );
        runBtn = new JButton("Run");
        verifyBtn = new JButton("Verify");
        stopBtn = new JButton("Stop");
        stopBtn.setEnabled(false);
        
        buttons.add(verifyBtn);
        buttons.add(runBtn);
        buttons.add(stopBtn);
        
        mazeTableModel = new MazeTableModel();
        mazeMonitorTable = new JTable(mazeTableModel){
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if ( drawLines ) {
                    List<MazeTableModel.Entry> navigationLocations = mazeTableModel.getNavigationLocations();
                    double cellWidth = getWidth()/(double)getColumnCount();
                    double cellHeight = getHeight()/(double)getRowCount();

                    Graphics2D g2 = (Graphics2D) g;
                    g2.setStroke( new BasicStroke(5) );
                    g2.setColor( new Color(0,0,0,.5f) );

                    int[] xPoints = new int[navigationLocations.size()];
                    int[] yPoints = new int[navigationLocations.size()];
                    
                    int i=0;
                    for ( MazeTableModel.Entry e : navigationLocations ) {
                        xPoints[i] = (int)(e.x*cellWidth + cellWidth/2);
                        yPoints[i] = (int)(e.y*cellHeight + cellHeight/2);
                        i++;
                    }
                    g2.drawPolyline(xPoints, yPoints, i);
                }
            }
            
        };
        mazeMonitorTable.setFillsViewportHeight(true);
        mazeMonitorTable.setIntercellSpacing(new Dimension(0,0));
        mazeMonitorTable.setCellSelectionEnabled(false);
        mazeMonitorTable.setRowSelectionAllowed(false);
        mazeMonitorTable.setFocusable(false);
        mazeMonitorTable.setRowHeight(35);
        mazeTableCellRenderer = new MazeTableCellRenderer();
        mazeMonitorTable.setDefaultRenderer(Object.class, mazeTableCellRenderer);
        
        logList = new JList(logModel);
        Box top = Box.createVerticalBox();
        top.add(buttons);
        top.add(mazeMonitorTable);
        topPanePanel = new JPanel(new BorderLayout() );
        topPanePanel.add(top, BorderLayout.NORTH);
        topPanePanel.add(new JScrollPane(logList), BorderLayout.CENTER);
        
        tabs.addTab("Run/Verify", addInsets(topPanePanel));
        
        addInsets(tabs);
        
    }
    private MazeTableCellRenderer mazeTableCellRenderer;
    
    public static void main(String[] args) throws InterruptedException {
        
        MainWindowCtrl mw = new MainWindowCtrl();
        
        SwingUtilities.invokeLater(()->{
            new MainWindowCtrl().start();
        });
        
    }
    
    private final Border insetBorder = new EmptyBorder(3,3,3,3);
    
    private <T extends JComponent> T addInsets( T c ) {
        c.setBorder(insetBorder);
        return c;
    }

    
}
