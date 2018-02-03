package XgboostModel;

import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.DMatrix;
import ml.dmlc.xgboost4j.java.XGBoost;
import ml.dmlc.xgboost4j.java.XGBoostError;

/**
 * Created by qiguo on 17/12/21.
 */
public class XgbstPredTest {
    public static void main(String[] args)throws XGBoostError{
        Booster bst = XGBoost.loadModel("/Users/qiguo/Documents/XgbTest/XgModel_logist");
        DMatrix dtest = new DMatrix("/Users/qiguo/Documents/XgbTest/Xgtest.txt");
        System.out.print(dtest.getLabel()[0] + "\t" + dtest.rowNum() + "\t" );

        float[][] res = bst.predict(dtest);
        System.out.println(res[0].length);
        for(int i = 0; i < res.length; i++){
            System.out.println(res[i][0]);
        }
    }
}
