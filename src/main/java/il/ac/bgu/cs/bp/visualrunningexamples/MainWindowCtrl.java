package il.ac.bgu.cs.bp.visualrunningexamples;

import java.awt.BorderLayout;
import java.awt.Font;
import java.util.Arrays;
import static java.util.stream.Collectors.joining;
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
    private JList logList;
    private final MazeRepo mazes = new MazeRepo();
    private final CodeRepo codes = new CodeRepo();
    
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
        
        mazeMonitorTable = new JTable();
        logList = new JList();
        topPanePanel = new JPanel(new BorderLayout() );
        topPanePanel.add(mazeMonitorTable, BorderLayout.NORTH);
        topPanePanel.add(new JScrollPane(logList), BorderLayout.CENTER);
        
        tabs.addTab("Run/Verify", addInsets(topPanePanel));
        
        addInsets(tabs);
        
    }
    
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
