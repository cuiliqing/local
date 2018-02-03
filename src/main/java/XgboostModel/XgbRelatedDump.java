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
 * Created by qiguo on 17/12/25.
 */
public class XgbRelatedDump {
    HashMap<String, Object> Param;
    public XgbRelatedDump(){
        this.Param = getParam();
    }

    public static HashMap getParam(){
        HashMap<String, Object> param = new HashMap<String, Object>();
        param.put("eta", 0.01);
        param.put("max_depth", 4);
        param.put("eval_metric", "auc"); // auc,rmse
        param.put("silent", 1);
        //param.put("objective", "reg:logistic");
        param.put("objective", "binary:logistic");
        param.put("lambda", 5);

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

        int round = 1000; // 1000

        XgbRelatedDump xgbst = new XgbRelatedDump();

        Booster booster = XGBoost.train(trainMtr, xgbst.Param, round, watches, null, null);

        //booster.saveModel(modelPath);
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
