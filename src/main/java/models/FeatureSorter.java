package models;

import java.io.*;
import java.util.Comparator;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

/**
 * Created by Tsui on 17/8/9.
 */
public class FeatureSorter {
    public void transferData(String inputfile, String outfile) throws IOException{
        BufferedReader bfr = new BufferedReader(new FileReader(inputfile));

        BufferedWriter bfw = new BufferedWriter(new FileWriter(outfile));

        String line  = "";
        while((line = bfr.readLine()) != null) {

            double lab = 0.0;
            String liblinearformat = "";

            StringTokenizer sTo = new StringTokenizer(line);

            TreeSet<Pair2> pairSet = new TreeSet<Pair2>(new Comparator<Pair2>() {
                public int compare(Pair2 p1, Pair2 p2) {
                    int res = p1.getIdx() < p2.getIdx() ? -1 : 1;
                    return res;
                }
            });
            HashSet<Integer> idxSet = new HashSet<Integer>();

            sTo.nextToken();              //guid

            while (sTo.hasMoreTokens()) {
                String tok = sTo.nextToken();

                if (tok.startsWith("0:[") || tok.startsWith("1:[")) {

                    lab = Double.parseDouble(tok.substring(0, 1));
                    liblinearformat += lab + " ";

                    String str = tok.substring(3);
                    int strlen = str.length();
                    str = str.substring(0, strlen - 1);
                    Pair2 p2 = new Pair2(str);
                    if (!idxSet.contains(p2.getIdx())) {
                        idxSet.add(p2.getIdx());
                        pairSet.add(p2);

                    }
                } else if (tok.startsWith("[")) {
                    String tmp = tok.substring(1);
                    int len = tmp.length();
                    tmp = tmp.substring(0, len - 1);
                    Pair2 p2 = new Pair2(tmp);
                    if (!idxSet.contains(p2.getIdx())) {
                        idxSet.add(p2.getIdx());
                        pairSet.add(p2);

                    }
                } else {
                    int len = tok.length();
                    String tmp = tok.substring(0, len - 1);
                    Pair2 p2 = new Pair2(tmp);
                    if (!idxSet.contains(p2.getIdx())) {
                        idxSet.add(p2.getIdx());
                        pairSet.add(p2);

                    }
                }

            }
            for(Pair2 p: pairSet){
                liblinearformat += p.toString()+ " ";
            }
            bfw.write(liblinearformat);
            bfw.flush();
            bfw.newLine();

        }
        bfr.close();
        bfw.close();
    }

    public static void main(String[] args)throws IOException{
        String input = args[0];    //   输入文件格式：guid 0／1：［....
        String output = args[1];    // 输出文件格式（liblinear数据格式）： 0/1 arr1：val arr2:val...
        new FeatureSorter().transferData(input, output);
    }

}
