package PersonalRecommend;

/**
 * Created by Tsui on 17/8/14.
 */
public class T2 {

    String key;
    Double val;

    public T2(String str){
        int idx = str.indexOf(":");
        this.key = str.substring(0, idx);
        this.val = Double.parseDouble(str.substring(idx+1)) ;
    }
    public T2(String item, Double val){
        this.key = item;
        this.val = val;
    }

    public String getKey(){
        return key;
    }
    public Double getVal(){
        return val;
    }

    public String toString(){
        return key + ":" + val;
    }

    /* //实现Comparable接口时 的 compareTo部分
    public int compareTo(T2 o) {
        int res = 0;
        if(this.getKey().equals(o.getKey())){
            res = 0;
        }else if(this.getVal() > o.getVal()){
            res = -1;
        }else {
            res = 1;
        }
        return res;
    }
    */
    //for test
    public static void main(String[] args){
        T2 t1 = new T2("123",34.0);
        System.out.println(t1);
    }
}
