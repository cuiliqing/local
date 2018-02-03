package XgboostModel;

import java.io.*;
import java.util.HashMap;

/**
 * Created by qiguo on 18/1/29.
 */
public class MovieTopClkRate2Name {
    HashMap<String, String> mvId2Name;

    public MovieTopClkRate2Name(String path)throws IOException{
        this.mvId2Name = getMvId2Name(path);
    }

    public HashMap<String, String> getMvId2Name(String pth)throws IOException{
        HashMap<String, String> mvMp = new HashMap<String, String>();

        String line = "";
        BufferedReader bfr = new BufferedReader(new FileReader(pth));
        while ((line = bfr.readLine()) != null){
            String[] res = line.split("\t");
            mvMp.put(res[0], res[1]);
        }
        bfr.close();
        return mvMp;
    }

    public static void main(String[] args)throws IOException{
        String inFile = args[0];
        String movieMp = args[1];
        String outFile = args[2];

        BufferedWriter bfw = new BufferedWriter(new FileWriter(outFile));
        HashMap<String, String> mvMp = new MovieTopClkRate2Name(movieMp).mvId2Name;

        BufferedReader bfr = new BufferedReader(new FileReader(inFile));
        String line = "";
        while((line = bfr.readLine()) != null){
            String mvPair = line.split("\t")[1];
            String mv1 = mvPair.split(":")[0];
            String mv2 = mvPair.split(":")[1];

            if(mvMp.containsKey(mv1)){
                line += "\t" + mvMp.get(mv1)+"\t";
            }
            if(mvMp.containsKey(mv2)){
                line += "\t" + mvMp.get(mv2);
            }

            bfw.write(line);
            bfw.flush();
            bfw.newLine();
        }
        bfr.close();
        bfw.close();
    }
}
