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
    
    private int minimalAgeColorComp = 255;
    
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
                    if ( cv.age == -1 ) {
                        spaceLbl.setBackground( Color.WHITE );
                    } else {
                        int v = Math.min(128+(int)(15*cv.age),minimalAgeColorComp);
                        spaceLbl.setBackground( new Color(v, v, 255));
                    }
                    spaceLbl.setText(cv.age == 0 ? "•" : String.valueOf(cv.value));
                    outputLabel = spaceLbl;
                    break;
                    
                case TARGET:
                    spaceLbl.setBackground(Color.YELLOW);
                    spaceLbl.setText(cv.age == 0 ? "•" : "t");
                    outputLabel = spaceLbl;
                    break;
                    
                case WALL:
                    wallLbl.setText(String.valueOf(cv.value));
                    outputLabel = wallLbl;
                    break;
            }
            
            
            return outputLabel;
            
        } else {
            System.out.println("Value not a CellValue: " + value);
            return new JLabel("/!\\" + Objects.toString(value));
        }
    }

    public void setShowAnyAge(boolean anyAge) {
        minimalAgeColorComp = anyAge ? 230 : 255;
    }
    
}
