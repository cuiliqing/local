package XgboostModel;

import ml.dmlc.xgboost4j.java.*;
import models.Pair2;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by qiguo on 17/12/15.
 */
public class XgboostTest {

    HashMap<String, Object> Param;
    public XgboostTest(){
        this.Param = getParam();
    }

    public static HashMap getParam(){
        HashMap<String, Object> param = new HashMap<String, Object>();
        param.put("eta", 0.01);
        param.put("max_depth", 4);
        param.put("eval_metric", "auc");
        param.put("silent", 1);
        param.put("objective", "reg:logistic");
        param.put("lambda", 5);

        return param;
    }


        /**
         * loglikelihoode loss obj function
         */
    public static class LogRegObj implements IObjective {
        private static final Log logger = LogFactory.getLog(LogRegObj.class);

        /**
         * simple sigmoid func
         *
         * @param input
         * @return Note: this func is not concern about numerical stability, only used as example
         */
        public float sigmoid(float input) {
            float val = (float) (1 / (1 + Math.exp(-input)));
            return val;
        }

        public float[][] transform(float[][] predicts) {
            int nrow = predicts.length;
            float[][] transPredicts = new float[nrow][1];

            for (int i = 0; i < nrow; i++) {
                transPredicts[i][0] = sigmoid(predicts[i][0]);
            }

            return transPredicts;
        }


        public List<float[]> getGradient(float[][] predicts, DMatrix dtrain) {
            int nrow = predicts.length;
            List<float[]> gradients = new ArrayList<float[]>();
            float[] labels;
            try {
                labels = dtrain.getLabel();
            } catch (XGBoostError ex) {
                logger.error(ex);
                return null;
            }
            float[] grad = new float[nrow];
            float[] hess = new float[nrow];

            float[][] transPredicts = transform(predicts);

            for (int i = 0; i < nrow; i++) {
                float predict = transPredicts[i][0];
                grad[i] = predict - labels[i];
                hess[i] = predict * (1 - predict);
            }

            gradients.add(grad);
            gradients.add(hess);
            return gradients;
        }
    }

    /**
     * user defined eval function.
     * NOTE: when you do customized loss function, the default prediction value is margin
     * this may make buildin evalution metric not function properly
     * for example, we are doing logistic loss, the prediction is score before logistic transformation
     * the buildin evaluation error assumes input is after logistic transformation
     * Take this in mind when you use the customization, and maybe you need write customized
     * evaluation function
     */
    public static class EvalError implements IEvaluation {
        private static final Log logger = LogFactory.getLog(EvalError.class);

        String evalMetric = "custom_auc";

        public EvalError() {

        }


        public String getMetric() {
            return evalMetric;
        }


        public float eval(float[][] predicts, DMatrix dmat) {
            float[] lab;
            try{
                lab = dmat.getLabel();
            }catch (XGBoostError ex){
                logger.error(ex);
                return -1f;
            }
            long num = predicts.length;
            long pos = 0;
            long neg = 0;
            float auc = 0.0f;

            TreeSet<Pair2> setp = new TreeSet<Pair2>(new Comparator<Pair2>() {
                public int compare(Pair2 p1, Pair2 p2){
                    int res = p1.getScore() > p2.getScore()? -1: 1;         //降序排列
                    return res;
                }
            });
            for(int i = 0; i < num; i++){
                setp.add(new Pair2(i, predicts[i][0]));
            }

            long rank = num;             // 从大到小排序，首个rank＝总样本个数;
            float pre_sum = 0.0f;           //统计到当前位置，预测相同的rank之和；
            long count = 0;             // 统计到当前位置，预测值相同的个数；
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
                    auc += pos_count * pre_sum / (count * 1.0f);
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
            auc += pos_count * pre_sum / (count * 1.0f);
            auc -= pos *(pos+1) * 0.5f;
            auc /=  pos * neg;

            return auc;
        }
    }


    public static void main(String[] args)throws XGBoostError,IOException{

        String trainPath = args[0];
        String testPath = args[1];
        String modelPath = args[2];
        String outPred = args[3];

        DMatrix trainMtr = new DMatrix(trainPath);
        DMatrix testMtr = new DMatrix(testPath);
        Map watches = new HashMap<String, DMatrix>();
        watches.put("train", trainMtr);
        watches.put("test", testMtr);

        int round = 200;

        XgboostTest xgbst = new XgboostTest();
        //IObjective obj = new LogRegObj();
        //IEvaluation eval = new EvalError();
        Booster booster = XGBoost.train(trainMtr, xgbst.Param, round, watches, null, null);

        float[][] pred = booster.predict(testMtr);
        booster.saveModel(modelPath);
        int row = pred.length;
        int len = pred[0].length;
        BufferedWriter bfw = new BufferedWriter(new FileWriter(outPred));
        for(int i = 0; i< row; i++){
            String line = "";
            for(int j= 0; j< len; j++){
                line += pred[i][j] +"\t";
            }
            bfw.write(line);
            bfw.flush();
            bfw.newLine();
        }
        bfw.close();
    }

}
