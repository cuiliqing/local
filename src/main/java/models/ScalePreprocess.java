package models;

import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.FeatureNode;
import javafx.util.Pair;

import java.io.*;
import java.util.HashMap;
import java.util.StringTokenizer;

/**
 * Created by Tsui on 17/8/21.
 */
public  class ScalePreprocess {

    public HashMap<Integer, Double> movieItemFeatMap;

    public ScalePreprocess(String dataPath)throws IOException{
        HashMap<Integer, Double> map = new HashMap<Integer, Double>();
        for(int idx = 1; idx <= 17515; idx++){
            map.put(idx, 0.0);
        }
        BufferedReader bf = new BufferedReader(new FileReader(dataPath));
        String line="";
        while((line = bf.readLine()) != null){
            StringTokenizer stok = new StringTokenizer(line);
            stok.nextToken();
            while(stok.hasMoreTokens()){
                String p2Str = stok.nextToken().trim();
                Pair2 p2 = new Pair2(p2Str);
                int featIdx = p2.getIdx();
                double val = p2.getScore();
                if(featIdx < 17516){
                    if(map.containsKey(featIdx)){
                        if(map.get(featIdx) < val){
                            map.put(featIdx, val);
                        }
                    }
                }

            }
        }
        bf.close();
        this.movieItemFeatMap = map;
    }

    public  HashMap<Integer, Double> getMovieItemFeatMap() {
        return movieItemFeatMap;
    }

    public static void main(String[] args)throws IOException{
        String libdataPath = args[0];
        String scaledataPath = args[1];
        File outFile = new File(scaledataPath);
        if(!outFile.exists()){
            outFile.createNewFile();
        }
        ScalePreprocess sc = new ScalePreprocess(libdataPath);

        BufferedReader bf1 = new BufferedReader(new FileReader(libdataPath));
        BufferedWriter bw = new BufferedWriter(new FileWriter(scaledataPath));
        String line = "";
        while((line = bf1.readLine()) != null){
            StringTokenizer stk = new StringTokenizer(line);
            String lineNew = stk.nextToken() + " ";
            while(stk.hasMoreTokens()){
                String p2Str = stk.nextToken().trim();
                Pair2 p2 = new Pair2(p2Str);
                if(p2.getIdx() <= 17515){
                    if(sc.movieItemFeatMap.get(p2.getIdx()) != 0.0){
                        double val = p2.getScore()/(sc.movieItemFeatMap.get(p2.getIdx()));
                        p2.setScore(val);
                        lineNew += p2.toString() + " ";
                    }
                } else {
                    lineNew += p2Str + " ";
                }

            }
            bw.write(lineNew);
            bw.flush();
            bw.newLine();
        }
        bf1.close();
        bw.close();
    }

}
