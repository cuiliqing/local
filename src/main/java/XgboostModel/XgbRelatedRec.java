package XgboostModel;

import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.DMatrix;
import ml.dmlc.xgboost4j.java.XGBoost;
import ml.dmlc.xgboost4j.java.XGBoostError;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Tsui on 17/12/21.
 */
public class XgbRelatedRec {
    HashMap<String, Object> Param;
    public XgbRelatedRec(){
        this.Param = getParam();
    }

    public static HashMap getParam(){
        HashMap<String, Object> param = new HashMap<String, Object>();
        param.put("eta", 0.01);
        param.put("max_depth", 4);
        param.put("eval_metric", "auc"); // auc,rmse
        param.put("silent", 0);
        param.put("objective", "reg:logistic");
        //param.put("objective", "binary:logistic");
        param.put("lambda", 5);
        param.put("gamma", 0.1); //
        param.put("colsample_bytree", 0.7); //0.8
        param.put("min_child_weight", 1);
        //param.put("max_delta_step", 1.0);
        param.put("subsample", 0.7);
        //param.put("scale_pos_weight", 1);
        param.put("seed", 10);

        return param;
    }

    public static void main(String[] args)throws XGBoostError,IOException {

        String trainPath = args[0];
        String testPath = args[1];
        String modelPath = args[2];

        DMatrix trainMtr = new DMatrix(trainPath);
        DMatrix testMtr = new DMatrix(testPath);
        Map watches = new HashMap<String, DMatrix>();
        watches.put("train", trainMtr);
        watches.put("test", testMtr);

        int round = 500; // 1000

        XgbRelatedRec xgbst = new XgbRelatedRec();

        Booster booster = XGBoost.train(trainMtr, xgbst.Param, round, watches, null, null);

        booster.saveModel(modelPath);
        //String featuresStr = args[3];
        //booster.getModelDump(featuresStr, true);  // featureStr 直接是保存交叉检验时的保存路径，

        String featuresStr = args[3];
        String[] res = booster.getModelDump(null, true);  // featureStr 直接是保存交叉检验时的保存路径，

        BufferedWriter bfw = new BufferedWriter(new FileWriter(featuresStr));
        for(String s : res){
            bfw.write(s);
            bfw.flush();
            bfw.newLine();
        }
        bfw.close();

    }
}
