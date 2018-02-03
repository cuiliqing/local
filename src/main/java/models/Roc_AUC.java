package models;


import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

import static java.lang.Math.abs;


public class Roc_AUC {
    double[] pre;
    double[] lab;
    public Roc_AUC(double[] pred, double[] labels){
        this.pre = pred;
        this.lab = labels;
    }

    public double CalculateAUC(){
        int num =  lab.length;
        long pos = 0;
        long neg = 0;
        double auc = 0.0;

        TreeSet<Pair2> setp = new TreeSet<Pair2>(new Comparator<Pair2>() {
            public int compare(Pair2 p1, Pair2 p2){
                int res = p1.getScore() > p2.getScore()? -1: 1;         //降序排列
                return res;
            }
        });
        for(int i = 0; i < num; i++){
            setp.add(new Pair2(i, pre[i]));
        }

        int rank = num;             // 从大到小排序，首个rank＝总样本个数;
        double pre_sum = 0.0f;           //统计到当前位置，预测相同的rank之和；
        int count = 0;             // 统计到当前位置，预测值相同的个数；
        long pos_count = 0;         // 统计相同预测值的个数中 正例的个数；
        double prev = setp.first().getScore();

        Iterator it = setp.iterator();

        while(it.hasNext()){
            Pair2 p = (Pair2) it.next();
            if(lab[p.getIdx()] > 0){
                pos += 1;
            }
            else{
                neg += 1;
            }
            if( prev != p.getScore() ){
                auc += pos_count * pre_sum / (count * 1.0);
                count = 1;
                pre_sum = rank--;
                prev = p.getScore();

                if(lab[p.getIdx()] > 0){
                    pos_count = 1;
                }else {
                    pos_count = 0;
                }
            }else {
                pre_sum += rank--;
                count += 1;
                if(lab[p.getIdx()] > 0){
                    pos_count += 1;
                }
            }
        }
        auc += pos_count * pre_sum / (count * 1.0);
        auc -= pos *(pos+1) * 0.5;
        auc /=  (pos * neg * 1.0);


        return auc;
    }

    public static void main(String[] args){
        double[] y = {1, 1, 0, 1 };
        double[] pred = {0.1, 0.4, 0.35, 0.8};
        double auc = new Roc_AUC(pred, y).CalculateAUC();
        System.out.println("auc:" + auc);
    }
}
