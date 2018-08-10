package il.ac.bgu.cs.bp.visualrunningexamples;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static java.util.stream.Collectors.toList;
import javax.swing.table.AbstractTableModel;

/**
 * The table model describing a maze.
 * @author michael
 */
public class MazeTableModel extends AbstractTableModel {
    
    private List<String> rows;
    private int columnCount;
    private long[][] lastEntry;
    private long currentEntry;
    
    public static class CellValue {
        
        public enum Type {
            SPACE, WALL, TARGET
        }
        
        final public long age;
        final public char value;
        final public Type type;
        
        public CellValue( char aValue, long anAge ){
            age = anAge;
            value = aValue;
            type = (value==' '||value=='s') ? Type.SPACE
                                : (value=='t') ? Type.TARGET : Type.WALL;
        }

    }
    
    public MazeTableModel() {
        rows = Arrays.asList(" ");
        columnCount = 1;
    }
    
    public void setRows( List<String> newRows ){
        // find the longest trimmed row, padd all the rest.
        int min = Integer.MAX_VALUE;
        int max = 0;
        List<String> rowList = new ArrayList<>(newRows.size());
        for ( String s:newRows ) {
            s = s.trim();
            min = s.length() < min ? s.length() : min;
            max = s.length() > max ? s.length() : max;
            rowList.add(s);
        }
        
        if ( min == max ) {
            // easy case
            rows = rowList;
        } else {
            int fMax = max;
            rows = rowList.stream().map( s -> s.length()==fMax ? s : String.format("%-" + fMax + "s", s)).collect(toList());
        }
        columnCount = max;
        
        lastEntry = new long[rows.size()][];
        for ( int i=0; i<lastEntry.length; i++ ){
            lastEntry[i] = new long[columnCount];
        }
        currentEntry = 0;
        fireTableStructureChanged();
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return columnCount;
    }

    @Override
    public CellValue getValueAt(int rowIndex, int columnIndex) {
        return new CellValue( rows.get(rowIndex).charAt(columnIndex), currentEntry-lastEntry[rowIndex][columnIndex]);
    }
    
    public void addEntry( int row, int column ){
        lastEntry[row][column] = ++currentEntry;
    }
}
