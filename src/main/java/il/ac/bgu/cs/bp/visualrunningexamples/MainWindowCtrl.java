package il.ac.bgu.cs.bp.visualrunningexamples;

import java.awt.BorderLayout;
import java.awt.Font;
import java.util.Arrays;
import static java.util.stream.Collectors.joining;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

/**
 * Simple class running a BPjs program that selects "hello world" events.
 * @author michael
 */
public class MainWindowCtrl {
    
    private JFrame window;
    private JTabbedPane tabs;
    private RSyntaxTextArea codeEditor;
    private JTextArea mazeEditor;
    private JComboBox<String> programCB, mazeCB;
    private JTable mazeMonitorTable;
    private JList logList;
    private final MazeRepo mazes = new MazeRepo();
    
    private void start() {
        createComponents();
        addListeners();
        
        window = new JFrame("BPjs - Visual Running Examples");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        mazeCB.setSelectedIndex(0);
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
    }
    
    private void createComponents() {
        tabs = new JTabbedPane();
        
        
        codeEditor = new RSyntaxTextArea();
        codeEditor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
        codeEditor.setRows(40);
        codeEditor.setColumns(80);
         
        programCB = new JComboBox<>(new String[]{"Negative Modelling", "Positive Modeling"});
        
        JPanel pnl = new JPanel( new BorderLayout() );
        pnl.add(new JLabel("BP Model"), BorderLayout.WEST);
        pnl.add( programCB, BorderLayout.CENTER );
        
        JPanel topPanePanel = new JPanel( new BorderLayout() );
        topPanePanel.add( pnl, BorderLayout.NORTH );
        topPanePanel.add( new JScrollPane(codeEditor), BorderLayout.CENTER );
        
        tabs.addTab("BProgram", topPanePanel);
        
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
        tabs.addTab("Maze", topPanePanel);
        
        mazeMonitorTable = new JTable();
        logList = new JList();
        topPanePanel = new JPanel(new BorderLayout() );
        topPanePanel.add(mazeMonitorTable, BorderLayout.NORTH);
        topPanePanel.add(new JScrollPane(logList), BorderLayout.CENTER);
        
        tabs.addTab("Run/Verify", topPanePanel);
        
    }
    
    public static void main(String[] args) throws InterruptedException {
        
        MainWindowCtrl mw = new MainWindowCtrl();
        
        SwingUtilities.invokeLater(()->{
            new MainWindowCtrl().start();
        });
        
    }
    
}
