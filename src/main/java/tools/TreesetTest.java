package tools;

import PersonalRecommend.T2;

import java.util.TreeSet;

/**
 * Created by qiguo on 18/2/1.
 */
public class TreesetTest {
    public static void main(String[] args){

        TreeSet<T2> canSet = new TreeSet<T2>();

        T2 t21 = new T2("cui", 1.0);
        canSet.add(t21);
        T2 t22 = new T2("li", 2.0);
        canSet.add(t22);
        T2 t23 = new T2("cui", 1.0);
        canSet.add(t23);

        for(T2 t : canSet){
            System.out.println(t.toString());
        }

    }
}
