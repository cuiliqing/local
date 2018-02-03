package tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.StringTokenizer;

/**
 * Created by Tsui on 18/1/9.
 */
public class ExtShowStsFeatures {
    HashSet<String> RelatedItemMvSet;

    public ExtShowStsFeatures(String mvSetPath)throws IOException{
        this.RelatedItemMvSet = getMvSetFromLocal(mvSetPath);
    }

    public HashSet<String> getMvSetFromLocal(String path)throws IOException{
        HashSet<String> mvSet = new HashSet<String>();

        BufferedReader bfr = new BufferedReader(new FileReader(path));
        String line = "";
        while((line = bfr.readLine()) != null){
            mvSet.add(line.trim());
        }
        bfr.close();
        return mvSet;
    }

    public static void main(String[] args)throws IOException{
        String mvSetPath = args[0];
        String input = args[1];
        String output = args[2];

        BufferedReader bfr = new BufferedReader(new FileReader(input));
        String line = "";
        HashSet<String> mvset = new ExtShowStsFeatures(mvSetPath).RelatedItemMvSet;

        while((line = bfr.readLine()) != null){
            StringTokenizer stk = new StringTokenizer(line,"\t");
            stk.nextToken();
            while (stk.hasMoreTokens()){
                String mvid = stk.nextToken();
                String infoStr = stk.nextToken();
                int idx = infoStr.indexOf(":");
                if(mvset.contains(mvid)){
                    String times = infoStr.substring(idx + 1);
                }
            }
        }

    }
}
