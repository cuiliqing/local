package models;

import de.bwaldvogel.liblinear.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import models.ModelLR.*;

import static models.ModelLR.getProblem;

/**
 * Created by Tsui on 17/8/8.

 */
public class Main {
    /*
    * */
    public static void main(String[] args) throws IOException{

        /**
         * out预测文件路径
         * 预测文件（predict 输出的预测值,第一列为预测的标签类，第二列为对应1/0的概率，第三列为对应0/1的概率 ）
        * */
        String file = args[0];
        int num = Integer.parseInt(args[1]);            // out 输出文件第一行为labels 0 1 舍去此行的行数

        BufferedReader bfr = new BufferedReader(new FileReader(file));
        double[] pred = new double[num];
        double[] lab = new double[num];
        String line = null;
        int idx = 0;
        int location1 = 0;
        while ((line = bfr.readLine()) != null) {
            if(line.trim().startsWith("label")){
                String s = line.trim().substring(7,8);
                location1 = (Integer.parseInt(s) == 1) ? 1 : 2;
                continue;
            }
            String[] strs = line.split(" ");
            lab[idx] = Double.parseDouble(strs[0]);
            pred[idx++] = Double.parseDouble(strs[location1]);     //预测1 的概率

        }
        bfr.close();

        double auc = new Roc_AUC(pred, lab).CalculateAUC();

        System.out.println("auc: "+ auc );
    }
}
