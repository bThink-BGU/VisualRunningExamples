package il.ac.bgu.cs.bp.visualrunningexamples;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static java.util.stream.Collectors.toList;

/**
 * This class parses the included "mazes.txt" file and makes it available as a 
 * map of string -> maze.
 * 
 * @author michael
 */
public class MazeRepo {
    
    private final Map<String, String[]> mazes = new HashMap<>();
    
    public MazeRepo() {
        try( InputStream in =Thread.currentThread().getContextClassLoader().getResourceAsStream("mazes.txt");
             BufferedReader rdr = new BufferedReader( new InputStreamReader(in) ) 
            ) {
            
            ArrayList<String> currentMazeLines = new ArrayList<>();
            String mazeName = null;
            String curLine = null;
            boolean inMaze = false;
            while ( (curLine=rdr.readLine()) != null ) {
                if ( ! inMaze ) {
                    curLine = curLine.trim();
                    if ( !curLine.isEmpty() ) {
                        mazeName = curLine;
                        inMaze = true;
                    }
                    
                } else {
                    if ( curLine.trim().equals("---") ){
                        mazes.put(mazeName, currentMazeLines.toArray(new String[currentMazeLines.size()]));
                        System.out.println("Stored '" + mazeName + "' (" + currentMazeLines.size() + " lines)" );
                        
                        currentMazeLines.clear();
                        mazeName=null;
                        inMaze = false;
                    } else {
                        currentMazeLines.add(curLine);
                    }
                }
            }
            
            if (mazeName != null ) {
                mazes.put(mazeName, currentMazeLines.toArray(new String[currentMazeLines.size()]));
            }
            
        } catch (IOException iox) {
            System.err.println("Error reading the 'mazes.txt' resource. ");
            iox.printStackTrace(System.err);
            System.exit(-1);
        }
        
    }
    
    public List<String> getMazeNames() {
        return mazes.keySet().stream().sorted().collect( toList() );
    }
    
    public String[] get( String key ) {
        return mazes.get(key);
    }
}
