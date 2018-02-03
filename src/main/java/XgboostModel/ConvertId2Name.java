package XgboostModel;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

/**
 * Created by qiguo on 18/1/25.
 */
public class ConvertId2Name {
    HashMap<String, String> mvId2Name;

    HashMap<String, HashSet<String>> testMvset;

    public ConvertId2Name(String moviePath, String predCPath)throws IOException{
        this.mvId2Name = getMvMp(moviePath);
        this.testMvset = getTestMvset(predCPath);
    }

    HashMap<String, String>getMvMp(String path)throws IOException{
        HashMap<String, String> mvMp = new HashMap<String, String>();

        String line = "";
        BufferedReader bfr = new BufferedReader(new FileReader(path));
        while ((line = bfr.readLine()) != null){
            StringTokenizer stk = new StringTokenizer(line, "\t");
            String mvid = stk.nextToken();
            String name = stk.nextToken();
            mvMp.put(mvid, name);
        }
        return mvMp;
    }

    HashMap<String,HashSet<String>> getTestMvset(String testMvPredPath)throws IOException{
        HashMap<String, HashSet<String>> testmvSet = new HashMap<String, HashSet<String>>();
        BufferedReader bfr = new BufferedReader(new FileReader(testMvPredPath));
        String line = "";
        while((line = bfr.readLine()) != null){
            HashSet<String> set = new HashSet<String>();
            StringTokenizer stk = new StringTokenizer(line, ",\t");
            String mv1 = stk.nextToken();
            mv1 = mv1.substring(0, 32);
            while (stk.hasMoreTokens()){
                String mvid = stk.nextToken().substring(0, 32);
                set.add(mvid);
            }
            testmvSet.put(mv1, set);
        }
        return testmvSet;
    }

    public static void main(String[] args)throws IOException{

        String movietable = args[0];
        String pred = args[1];
        String CPredFile = args[2];
        String outFile = args[3];

        BufferedWriter bfw = new BufferedWriter(new FileWriter(outFile));
        ConvertId2Name c2name = new ConvertId2Name(movietable, CPredFile);
        HashMap<String, String> mvmap = c2name.mvId2Name;

        BufferedReader bfr = new BufferedReader(new FileReader(pred));
        String line = "";
        while ((line = bfr.readLine()) != null){

            StringTokenizer stk = new StringTokenizer(line, ",\t");
            String firmv = stk.nextToken();
            String out = mvmap.get(firmv) + "\t";

            while (stk.hasMoreTokens()){
                HashSet<String> mvSet = c2name.testMvset.get(firmv);

                String mvtmp = stk.nextToken();
                String id = mvtmp.substring(0, 32);
                if(mvSet.contains(id)){
                    String info = mvtmp.substring(33);
                    out += mvmap.get(id)+":" + info + "\t";
                }
            }
            bfw.write(out);
            bfw.flush();
            bfw.newLine();

        }
        bfw.close();
        bfr.close();

    }
}
