package models;

import java.util.Comparator;
import java.util.StringTokenizer;
import java.util.TreeSet;


/**
 * Created by Tsui on 17/8/3.
 */
public class Pair2 {
    private int idx;
    private double score;

    public Pair2(int idx, double score){
        this.idx = idx;
        this.score = score;
    }
    public Pair2(String str){
        String[] pStr = str.split(":");
        this.idx = Integer.parseInt(pStr[0]);
        this.score = Double.parseDouble(pStr[1]);
    }
    public String toString(){
        return idx + ":" + score;
    }

    public int getIdx(){
        return idx;
    }
    public double getScore(){
        return score;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public void setScore(double score) {
        this.score = score;
    }

    // for test
    public static void main(String[] args){
        TreeSet<Pair2> p = new TreeSet<Pair2>(new Comparator<Pair2>() {
            public int compare(Pair2 p1, Pair2 p2){
                int res = p1.getIdx() > p2.getIdx()? -1: 1;
                return res;
            }
        });
        p.add(new Pair2(1, 0.2));
        p.add(new Pair2(3, 0.4));
        p.add(new Pair2(4, 0.4));
        for(Pair2 pair2 : p){
            System.out.println(pair2.toString());
        }
        String str1 = "0";
        double v = Double.parseDouble(str1);
        System.out.println(v);
        String str ="label美国:1.00,动画:0.82,动作:0.78,科幻:0.78,大陆:0.74,改编:0.53,剧情:0.49,温情:0.40,冒险:0.38,3D:0.36,成长:0.32,人性:0.32,烂片:0.29,奇幻:0.24,二次元:0.15,西游记:0.13,超级英雄:0.05,";
        if(str.startsWith("label")){
            str = str.substring(5);
            StringTokenizer stk = new StringTokenizer(str,",");
            while(stk.hasMoreTokens()){
                String s = stk.nextToken();
                int idx = s.indexOf(":");
                System.out.println(s.substring(idx+1));
            }
        }


    }

}
