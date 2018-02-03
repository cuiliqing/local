package mathSts;

import models.Pair2;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Created by qiguo on 17/8/22.
 */
public class VarMean {
    HashMap<Integer, Double> map;
    public VarMean(String path)throws IOException{
        HashMap<Integer, Double> map = new HashMap<Integer, Double>();
        for(int i = 17516; i<=36441; i++){
            map.put(i, 0.0);
        }
        BufferedReader br = new BufferedReader(new FileReader(path));
        String line ="";
        while((line = br.readLine()) != null){
            StringTokenizer stok =  new StringTokenizer(line,"\t");
            stok.nextToken();
            String likes = stok.nextToken().trim();
            likes = likes.substring(1);
            likes = likes.substring(0,likes.length()-1);
            StringTokenizer stk1 = new StringTokenizer(likes, ",");
            while(stk1.hasMoreTokens()){
                String likePair = stk1.nextToken().trim();
                Pair2 p2 = new Pair2(likePair);
                map.put(p2.getIdx(), p2.getScore()+ map.get(p2.getIdx()));
            }

        }
        br.close();
        this.map = map;
    }

    public HashMap<Integer, Double> getMean(HashMap<Integer, Double> sumMap){
        HashMap<Integer, Double> map = new HashMap<Integer, Double>();
        for(Map.Entry<Integer, Double>entry : sumMap.entrySet()){
            map.put(entry.getKey(), entry.getValue() / 664231.0);
        }
        return map;
    }
    public HashMap<Integer, Double> getVar(HashMap<Integer, Double> meanMap, String Path) throws IOException{
        HashMap<Integer, Double> varMap= new HashMap<Integer, Double>();
        for(int i = 17516;i<=36441; i++){
            varMap.put(i, 0.0);
        }
        BufferedReader br = new BufferedReader(new FileReader(Path));
        String line ="";
        while((line = br.readLine()) != null){
            StringTokenizer stok =  new StringTokenizer(line,"\t");
            stok.nextToken();
            String likes = stok.nextToken().trim();
            likes = likes.substring(1);
            likes = likes.substring(0,likes.length()-1);
            StringTokenizer stk1 = new StringTokenizer(likes, ",");
            while(stk1.hasMoreTokens()){
                String likePair = stk1.nextToken().trim();
                Pair2 p2 = new Pair2(likePair);
                double res = p2.getScore() - meanMap.get(p2.getIdx());
                varMap.put(p2.getIdx(), varMap.get(p2.getIdx()) + res*res / 664231.0);
            }

        }
        br.close();
        return varMap;
    }
    public static void main(String[] args)throws IOException{
        String likesPath = args[0];
        VarMean varmn = new VarMean(likesPath);
        HashMap<Integer, Double> meanRes = varmn.getMean(varmn.map);
        String varPath = args[1];
        //System.out.println(meanRes.toString());
        HashMap<Integer, Double> varRes = varmn.getVar(varmn.map, likesPath);
        //System.out.println(varRes.toString());
        BufferedWriter bw = new BufferedWriter(new FileWriter(varPath));
        for(Map.Entry<Integer, Double> entry : varRes.entrySet()){
            bw.write(entry.toString());
            bw.newLine();
        }
        bw.flush();
        bw.close();
    }
}
