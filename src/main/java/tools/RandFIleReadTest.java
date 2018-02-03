package tools;

import de.bwaldvogel.liblinear.Model;
import models.Pair2;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

/**
 * Created by qiguo on 17/9/26.
 */
public class RandFIleReadTest {
    /*
    public static Pair2[] getSortedFeature(String str, int []featIdx){
        StringTokenizer stk = new StringTokenizer(str, " ,");
        TreeSet<Pair2> set = new TreeSet<Pair2>(new Comparator<Pair2>(){
            public int compare(Pair2 p1, Pair2 p2){
                int res = p1.getIdx() < p2.getIdx()? -1: 1;
                return res;
            }
        });
        HashMap<Integer,Double> midMap = new HashMap<Integer, Double>();

        while(stk.hasMoreTokens()){
            String tmp = stk.nextToken();
            Pair2 p2 = new Pair2(tmp);
            if(p2.getIdx() >= featIdx[0] && p2.getIdx() < featIdx[2] ){
                midMap.put(p2.getIdx(), p2.getScore());
            }
            set.add(p2);
        }
        int deleted = 0;
        int offset = featIdx[1] - featIdx[0];
        Iterator<Map.Entry<Integer, Double>> iter = midMap.entrySet().iterator();
        while(iter.hasNext()){
            Map.Entry<Integer, Double>entry = iter.next();
            if(entry.getKey() >= featIdx[1] && entry.getKey() < featIdx[2]){
                int keyId = entry.getKey() - offset;
                if(midMap.containsKey(keyId)){
                    midMap.put(entry.getKey(), midMap.get(keyId));
                }else {
                    iter.remove();
                    ++deleted;
                }
            }
        }
        int start = featIdx[1];
        int end = featIdx[2];
        int featNodeNum = set.size();
        int featTotal = featNodeNum - deleted;
        Pair2[] feat = new Pair2[featTotal];
        int i = 0;
        for(Pair2 p : set){
            if(p.getIdx() >= start && p.getIdx() < end ){
                if(midMap.containsKey(p.getIdx())){
                    feat[i++] = new Pair2(p.getIdx(), midMap.get(p.getIdx()));
                    System.out.println(p.getIdx() +"\t"+midMap.get(p.getIdx()));
                }
            }else{
                feat[i++] = new Pair2(p.getIdx(), p.getScore());
                System.out.println(p.getIdx() +"\t"+midMap.get(p.getIdx()));

            }
        }
        return feat;
    }
    public static void main(String[] args)throws IOException{


        String path = "";
        Model md = Model.load(new File("/Users/qiguo/Documents/model"));
        double[] wei1 = md.getFeatureWeights();
        int[] lab = md.getLabels();
        System.out.println(lab[0]+" "+lab[1]);
        System.out.println(wei1[0]);
        HashMap<Integer, Double> weimap = new HashMap<Integer, Double>();
        BufferedReader bfr2 = new BufferedReader(new FileReader("/Users/qiguo/Documents/wei.txt"));

        String line = null;
        while((line = bfr2.readLine()) != null){
            StringTokenizer stk  = new StringTokenizer(line);
            int idx = Integer.parseInt(stk.nextToken());
            double wei = Double.parseDouble(stk.nextToken());
            weimap.put(idx, wei);
            //System.out.println(idx+"\t"+wei);
        }

        BufferedReader bfrlikes = new BufferedReader(new FileReader("/Users/qiguo/Documents/testlike.txt"));
        String lin2 = "";
        String likes = "";
        while((lin2 = bfrlikes.readLine()) != null){
            StringTokenizer stk = new StringTokenizer(lin2,"\t");
            String guid = stk.nextToken();
            likes = stk.nextToken();
            likes = likes.substring(1);
            likes = likes.substring(0, likes.length()-1);
        }
        BufferedReader bfrMov = new BufferedReader(new FileReader("/Users/qiguo/Documents/mvtest.txt"));
        String line3 = "";
        String mvfet = "";
        String mvid = "";
        while((line3 = bfrMov.readLine()) != null){
            StringTokenizer stk = new StringTokenizer(line3, "\t");
            mvid = stk.nextToken();
            mvfet = stk.nextToken();
            mvfet = mvfet.substring(1);
            mvfet = mvfet.substring(0, mvfet.length()-1);

        }
        String joinstr = mvfet+","+likes;
        int[] idx = {16143, 24115, 32085};
        Pair2[] p2ss = getSortedFeature(joinstr, idx);
        double res = 0.0;
        for(Pair2 p2 : p2ss){
            res += weimap.get(p2.getIdx())* p2.getScore();
            System.out.println(weimap.get(p2.getIdx()) +"\t"+p2.getIdx()+"\t"+p2.getScore());
        }

        System.out.println(mvid +" "+res);
        System.out.println(-res);

    } */
    public static void main(String[] args){
        /*
        long timestp =  1508719068931l;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String sd = sdf.format(new Date(Long.parseLong(String.valueOf(timestp))));
        System.out.println(sd);
        */
        String path = "/data/   tvapk/cuiliqing/ 201707019";
        StringTokenizer stk = new StringTokenizer(path, " \t");
        while(stk.hasMoreTokens()){
            System.out.println(stk.nextToken());
        }
    }
}
