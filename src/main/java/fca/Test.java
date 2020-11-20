package fca;

import fca.core.util.BasicSet;
import fca.exception.AlreadyExistsException;
import fca.exception.GTreeConstructionException;
import fca.exception.InvalidTypeException;
import fca.gui.lattice.element.GraphicalLattice;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;

public class Test {
    public static void main(String[] args) throws AlreadyExistsException, InvalidTypeException, GTreeConstructionException, IOException {
        System.out.println((new File("").getAbsolutePath()));
        File file = new File("./ConceptLatticeAlgo/static/test7_copy.slf");
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

        br.readLine();
        int objCount = Integer.parseInt(br.readLine());
        int attrCount = Integer.parseInt(br.readLine());

        Vector<String> objects = new Vector<>();
        br.readLine();
        for(int i = 0; i < objCount; i++){
            objects.add(br.readLine());
        }

        Vector<String> attributes = new Vector<>();
        br.readLine();
        for(int i = 0; i < attrCount; i++){
            attributes.add(br.readLine());
        }

        Vector<Vector<String>> values = new Vector<>();
        br.readLine();
        for(int i = 0; i < objCount; i++){
            String line = br.readLine();
            String[] str = line.split("[ ]+");

            Vector<String> rowValues = new Vector<>();
            for(String s: str){
                if(s.equals("1")) {
                    rowValues.add("true");
                }else{
                    rowValues.add("");
                }
            }
            values.add(rowValues);
        }

        ConceptLatticeAlgo latticeAlgo = new ConceptLatticeAlgo("test", objects, attributes, values);
        // GraphicalLattice originalGraphicalLattice = (GraphicalLattice) latticeAlgo.getGraphicalLattice().clone();

        Hashtable<String,Hashtable<String,BasicSet>> map = getSplit();
        String p = map.keys().nextElement();
        Hashtable<String, BasicSet> ws = map.get(p);

        // latticeAlgo.zoomIn(p, ws);
        latticeAlgo.zoomOut(p,ws);



    }

    private static Hashtable<String, Hashtable<String, BasicSet>> getSplit() throws IOException {
        File file = new File("./ConceptLatticeAlgo/static/cut.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

        String line = br.readLine();
        String[] ss = line.split("[ ]+");

        String root = ss[0];
        int cutCount = Integer.parseInt(ss[1]);

        Hashtable<String,BasicSet> cutMap = new Hashtable<>();
        for(int i = 0; i < cutCount; i++){
            String s = br.readLine();
            String[] ss1 = s.split("[ ]+");
            String[] ss2 = ss1[1].split(",");

            BasicSet set = new BasicSet();
            set.addAll(Arrays.asList(ss2));

            cutMap.put(ss1[0], set);
        }

        Hashtable<String,Hashtable<String,BasicSet>> ret = new Hashtable<>();
        ret.put(root, cutMap);

        return ret;
    }
}
