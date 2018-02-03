package XgboostModel;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.StringTokenizer;

/**
 * Created by qiguo on 18/1/29.
 *
 */
public class XgbPosNegSample {
    HashSet<String> movieSet;

    public XgbPosNegSample(String moviePath)throws IOException{
        this.movieSet = getMovieSet(moviePath);
    }

    public HashSet<String>getMovieSet(String mvPath)throws IOException{
        HashSet<String> outset = new HashSet<String>();
        String line = "";
        BufferedReader bfr = new BufferedReader(new FileReader(mvPath));
        while ((line = bfr.readLine()) != null){
            StringTokenizer stk = new StringTokenizer(line, "\t");
            String mvid = stk.nextToken();
            while (stk.hasMoreTokens()){
                String tagStr = stk.nextToken();
                if(tagStr.startsWith("tags")){
                    String[] tags = tagStr.split(",");
                    for(String s : tags){
                        if(s.equals("情色")){
                            outset.add(mvid);
                        }
                    }
                }
            }
        }
        return outset;
    }


    public static void main(String[] args)throws IOException{





    }

}
