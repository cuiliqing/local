package models;

import java.io.*;
import java.nio.Buffer;
import java.util.Comparator;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Parameter;
import de.bwaldvogel.liblinear.Problem;
import de.bwaldvogel.liblinear.SolverType;
/*
* 输入： 测试集
* 输出：AUC 系数
*
* */

public class ModelLR {

    private static final int featnum = 40674;
    public static Problem getProblem(String path, int rowNum, int ftNum) throws
            IOException {

        double[] tar = new double[rowNum];
        String line = null;
        int idx = 0;
        BufferedReader br = new BufferedReader(new FileReader(path));    //测试集路径

        Feature[][] featMtrx = new Feature[rowNum][];   //写入行数

        while ((line = br.readLine()) != null) {
            StringTokenizer sTo = new StringTokenizer(line);
            int fetnum = sTo.countTokens() - 1;      //每个样本的node个数  第一个为lab，node个数为sTo.countTokens()-1
            int fetidx = 0;
            Feature[] featrow = new FeatureNode[fetnum];

            String label = sTo.nextToken();
            double lab = Double.parseDouble(label);
            tar[idx] = lab;

            while (sTo.hasMoreTokens()) {

                String tok = sTo.nextToken();
                Pair2 p = new Pair2(tok);
                featrow[fetidx++] = new FeatureNode(p.getIdx(), p.getScore());
            }

            featMtrx[idx++] = featrow;
        }
        Problem prob = new Problem();
        prob.x = featMtrx;
        prob.l = rowNum;
        prob.n = ftNum;
        prob.y = tar;
        br.close();
        return prob;
    }

    public static void main(String[] args) throws IOException{

        String modelpath =  args[0];                           //加载模型文件
        Model model = Linear.loadModel(new File(modelpath));
        /**
         * 由于label 标签有顺序，01位置不固定
         * 获取1 在坐标位置Linear.predictProbability(model, tstPro.x[i], score)中
         * score数组的位置
        **/
        int[] labels = model.getLabels();
        int loc1;
        loc1 = labels[0] == 1 ? 0 : 1;

        String tstPath = args[1];                     // 测试数据路径
        int rowNum = Integer.parseInt(args[2]);          //行数

        Problem tstPro = getProblem(tstPath, rowNum, featnum); //

        double[] pred = new double[tstPro.l];

        for(int i = 0; i < tstPro.l; i++){
            double[] score = new double[2];
            Linear.predictProbability(model, tstPro.x[i], score);
            pred[i] = score[loc1];                             // 预测为1 的概率
        }
        /*
        String outpath = "/data5/cuiliqing/tar_labely.txt";
        BufferedWriter bfw = new BufferedWriter(new FileWriter(outpath));
        int cnt = 0;
        for(double v : pred){
            bfw.write( tstPro.y[cnt++] + " " + v);
            bfw.newLine();
        }
        bfw.close();
        */
        double auc = new Roc_AUC(pred, tstPro.y).CalculateAUC();

        System.out.println("all is done");
        System.out.println("auc: "+ auc );
    }

}
