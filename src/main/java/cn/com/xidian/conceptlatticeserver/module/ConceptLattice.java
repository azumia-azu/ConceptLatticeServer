package cn.com.xidian.conceptlatticeserver.module;

import fca.ConceptLatticeAlgo;
import fca.core.util.BasicSet;
import fca.exception.AlreadyExistsException;
import fca.exception.GTreeConstructionException;
import fca.exception.InvalidTypeException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.*;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ConceptLattice {
    private ConceptLatticeAlgo instance;

    public static void main(String[] args) {
        System.out.println(new File("").getAbsolutePath());
    }

    public static ConceptLattice InitWithFiles() throws InvalidTypeException, IOException, GTreeConstructionException, AlreadyExistsException {
        return new ConceptLattice(readLocal());
    }

    public static ConceptLattice InitNew(String name, Vector<String> object, Vector<String> attribute, Vector<Vector<String>> values) throws AlreadyExistsException, InvalidTypeException {
        return new ConceptLattice(new ConceptLatticeAlgo(name, object, attribute, values));
    }

    public ConceptLattice(SerializableBinaryContext context) throws AlreadyExistsException, InvalidTypeException {
        instance = new ConceptLatticeAlgo(context.getName(), context.getObjects(), context.getAttributes(), context.getRelations());
    }

    private static ConceptLatticeAlgo readLocal() throws InvalidTypeException, GTreeConstructionException, AlreadyExistsException, IOException {
        File file = new File("./src/main/resources/static/test7_copy.slf");
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

        return new ConceptLatticeAlgo("test", objects, attributes, values);
    }

    private static Hashtable<String, Hashtable<String, BasicSet>> getSplit() throws IOException {
        File file = new File("./src/main/resources/static/cut.txt");
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

