package il.ac.bgu.cs.bp.visualrunningexamples;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.Objects;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

/**
 * Renders the cells in the monitor table;
 * 
 * @author michael
 */
public class MazeTableCellRenderer implements TableCellRenderer {
    
    private final JLabel wallLbl, spaceLbl;
    
    public MazeTableCellRenderer() {
        Font f = new Font(Font.MONOSPACED, Font.PLAIN, 20);
        wallLbl = new JLabel();
        wallLbl.setHorizontalAlignment(SwingConstants.CENTER);
        wallLbl.setBackground(Color.BLACK);
        wallLbl.setBackground(Color.LIGHT_GRAY);
        wallLbl.setFont(f);
        wallLbl.setOpaque(true);
        
        spaceLbl = new JLabel();
        spaceLbl.setHorizontalAlignment(SwingConstants.CENTER);
        spaceLbl.setFont(f);
        spaceLbl.setOpaque(true);
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if ( value instanceof MazeTableModel.CellValue ) {
            MazeTableModel.CellValue cv = (MazeTableModel.CellValue) value;
            JLabel outputLabel = wallLbl;
            switch ( cv.type ) {
                case SPACE:
                    if ( cv.age == 0 ) {
                        spaceLbl.setBackground( Color.WHITE );
                    } else {
                        spaceLbl.setBackground( new Color(128, 128, Math.max(255-(int)(20*cv.age),0)));
                    }
                    outputLabel = spaceLbl;
                    break;
                case TARGET:
                    spaceLbl.setBackground(Color.YELLOW);
                    outputLabel = spaceLbl;
                    break;
                case WALL:
                    outputLabel = wallLbl;
                    break;
            }
            
            outputLabel.setText( String.valueOf(cv.value) );
            return outputLabel;
            
        } else {
            System.out.println("Value not a CellValue: " + value);
            return new JLabel("/!\\" + Objects.toString(value));
        }
    }
    
}
