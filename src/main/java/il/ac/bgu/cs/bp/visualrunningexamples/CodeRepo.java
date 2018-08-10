package il.ac.bgu.cs.bp.visualrunningexamples;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static java.util.stream.Collectors.joining;

/**
 * Loads code resources.
 * 
 * @author michael
 */
public class CodeRepo {
    
    static final private Map<String, String> TITLE_RESOURCE_TO_NAME = new HashMap<>();
    
    static {
        TITLE_RESOURCE_TO_NAME.put("Negative Model", "MazesNegative.js");
        TITLE_RESOURCE_TO_NAME.put("Positive Model", "MazesPositive.js");
        TITLE_RESOURCE_TO_NAME.put("VerificationAdditions", "VerificationAdditions.js");
    }
    
    private Map<String,String> codeByName = new HashMap<>();
    
    public CodeRepo() {
        TITLE_RESOURCE_TO_NAME.keySet().forEach(k -> {
            String resName = TITLE_RESOURCE_TO_NAME.get(k);
            try( InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resName);
             BufferedReader rdr = new BufferedReader( new InputStreamReader(in) ) 
            ) {
                codeByName.put(k, rdr.lines().collect(joining("\n")));
            } catch (IOException iox ) {
                System.err.println("Error loading code '" + resName + "'");
                System.exit(-2);
            }
        });
    }
    
    public List<String> getModelNames() {
        return Arrays.asList("Negative Model", "Positive Model");
    }
    
    public String get(String title) {
        return codeByName.get(title);
    }
}
