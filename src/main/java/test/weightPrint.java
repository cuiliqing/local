package test;

import de.bwaldvogel.liblinear.Model;
import models.Pair2;
import org.omg.CORBA.INTERNAL;

import java.io.*;
import java.nio.Buffer;
import java.util.Comparator;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.TreeSet;

/**
 * Created by qiguo on 17/9/13.
 */
public class weightPrint {
    HashMap<Integer, String>tagsMap;
    HashMap<Integer, String>likesMap;

    public weightPrint(String p1, String p2)throws IOException{
        this.tagsMap = getTagsMap(p1);
        this.likesMap = getLikesMap(p2);
    }

    public static void main(String[] args)throws IOException{
        String modelPath = "/Users/qiguo/Documents/fearureData/weiv2.txt";

        weightPrint wp = new weightPrint("/Users/qiguo/Documents/fearureData/movieTagKeys.txt",
                "/Users/qiguo/Documents/fearureData/likesKeys.txt" );
        int joinStartId = 25071;
        int joinEndId = 33327;
        BufferedReader bfr = new BufferedReader(new FileReader(modelPath));
        String line = "";
        TreeSet<Pair2> ts1 = new TreeSet<Pair2>(new Comparator<Pair2>() {
            public int compare(Pair2 p1, Pair2 p2){
                int res = p1.getScore() > p2.getScore()? -1: 1;
                return res;
            }
        });
        TreeSet<Pair2> ts2 = new TreeSet<Pair2>(new Comparator<Pair2>() {
            public int compare(Pair2 p1, Pair2 p2){
                int res = p1.getScore() > p2.getScore()? -1: 1;
                return res;
            }
        });
        TreeSet<Pair2> ts3 = new TreeSet<Pair2>(new Comparator<Pair2>() {
            public int compare(Pair2 p1, Pair2 p2){
                int res = p1.getScore() > p2.getScore()? -1: 1;
                return res;
            }
        });

        while((line = bfr.readLine()) != null){
            StringTokenizer stk = new StringTokenizer(line);
            int id = Integer.parseInt(stk.nextToken());
            double wei = Double.parseDouble(stk.nextToken());
            if(id >= 16812 && id <=25070){
                if(ts1.size() < 200){
                    ts1.add(new Pair2(id, wei));
                }
                else {
                    if(wei > ts1.last().getScore()){
                        ts1.pollLast();
                        ts1.add(new Pair2(id, wei));
                    }
                }

            }else if(id >= 25071 && id <= 33327){
                if(ts2.size() < 200){
                    ts2.add(new Pair2(id, wei));
                }
                else {
                    if(wei > ts2.last().getScore()){
                        ts2.pollLast();
                        ts2.add(new Pair2(id, wei));
                    }
                }
            }else if( id >= 33328){
                if(ts3.size() < 200){
                    ts3.add(new Pair2(id, wei));
                }
                else {
                    if(wei > ts3.last().getScore()){
                        ts3.pollLast();
                        ts3.add(new Pair2(id, wei));
                    }
                }
            }
        }

        BufferedWriter bfw1 = new BufferedWriter(new FileWriter("/Users/qiguo/Documents/fearureData/tagswei.txt"));
        for(Pair2 p2 : ts1){
            int id = p2.getIdx();
            String str = id +"\t"+ wp.tagsMap.get(id)+"\t"+p2.getScore();
            bfw1.write(str);
            bfw1.newLine();
        }
        bfw1.close();

        BufferedWriter bfw2 = new BufferedWriter(new FileWriter("/Users/qiguo/Documents/fearureData/likeswei.txt"));
        for(Pair2 p2 : ts3){
            int id = p2.getIdx();
            String str = id +"\t"+ wp.likesMap.get(id)+"\t"+p2.getScore();
            bfw2.write(str);
            bfw2.newLine();
        }
        bfw2.close();

        BufferedWriter bfw3 = new BufferedWriter(new FileWriter("/Users/qiguo/Documents/fearureData/joinswei.txt"));
        for(Pair2 p2 : ts2){
            int id = p2.getIdx();
            String str = id +"\t"+ wp.tagsMap.get(id-8259)+"\t"+p2.getScore();
            bfw3.write(str);
            bfw3.newLine();
        }
        bfw3.close();

        ts1.clear();
        ts2.clear();
        ts3.clear();

    }


    HashMap<Integer, String> getTagsMap(String path)throws IOException{
        HashMap<Integer, String>tagsMap = new HashMap<Integer, String>();
        BufferedReader bfr = new BufferedReader(new FileReader(path));

        String line = null;
        int loc = 16812;
        while((line = bfr.readLine()) != null){
            int idx = line.indexOf(" ");
            String fet = line.substring(idx+3);
            tagsMap.put(loc++, fet);
        }
        bfr.close();
        return tagsMap;
    }


    HashMap<Integer, String>getLikesMap(String path)throws IOException{

        HashMap<Integer, String>likesMap = new HashMap<Integer, String>();
        int loc = 33328;
        BufferedReader bfr = new BufferedReader(new FileReader(path));
        String line = null;
        while((line = bfr.readLine()) != null){
            int idx = line.indexOf(" ");
            String fet = line.substring(idx+1);
            likesMap.put(loc++, fet);
        }
        bfr.close();
        return likesMap;
    }

}
