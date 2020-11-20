package fca.core.lattice;

import fca.core.context.binary.BinaryContext;
import fca.core.util.BasicSet;
import fca.exception.GTreeConstructionException;

import java.util.*;

public class GTree {

    /**
     * G-Tree的根节点
     */
    private GNode root;

    /**
     * G-Tree的子节点
     */
    private Vector<GNode> children;

    /**
     * G-Tree构造器
     * @param bc 上下文
     * @param p  被划分的属性
     * @param ws 被划分的子属性以及子属性对应的对象
     * @param zoomIn 是否处于zoom in
     * @throws GTreeConstructionException
     */
    public GTree(BinaryContext bc, String p, Hashtable<String, BasicSet> ws, boolean zoomIn) throws GTreeConstructionException {
        if(zoomIn) {
            // 判断bc是否含有p属性
            if (bc.getAttributeIndex(p) < 0) {
                throw new GTreeConstructionException("Cannot find attribute p in binary context");
            }

            // 判断属性p划分的子属性对应的extent是否合法，即满足两两之间不能存在交集，同时所有extent
            // 的并集会等于属性p对应的extents
            Collection<BasicSet> wObjectsCol = ws.values();
            for (BasicSet set1 : wObjectsCol) {
                for (BasicSet set2 : wObjectsCol) {
                    if (set1 != set2 && !set1.intersection(set2).isEmpty()) {
                        throw new GTreeConstructionException("Invalid cut for attribute p");
                    }
                }
            }

            BasicSet pObjects = bc.getObjectsFor(p);
            BasicSet wObjectsUnion = null;
            for (BasicSet set : wObjectsCol) {
                if (wObjectsUnion == null) {
                    wObjectsUnion = set;
                } else {
                    wObjectsUnion = wObjectsUnion.union(set);
                }
            }

            if (!pObjects.equals(wObjectsUnion)) {
                throw new GTreeConstructionException("Invalid cut for attribute p");
            }

            root = new GNode(p, pObjects);
            children = new Vector<>();
            for (String label : ws.keySet()) {
                children.add(new GNode(label, ws.get(label)));
            }
        }else{
            BasicSet rootObjects = new BasicSet();

            children = new Vector<>();
            for(String w: ws.keySet()){
                if(bc.getAttributeIndex(w) < 0){
                    throw new GTreeConstructionException("Cannot find attribute p in binary context");
                }

                BasicSet objects = ws.get(w);

                children.add(new GNode(w, objects));
                rootObjects = rootObjects.union(objects);
            }

            root = new GNode(p, rootObjects);

            // 判断合并的属性的对象集是否有交集，若有则不合法
            for(String w1: ws.keySet()){
                for(String w2: ws.keySet()){
                    if(!w1.equals(w2) && !ws.get(w1).intersection(ws.get(w2)).isEmpty()){
                        throw new GTreeConstructionException("Invalid merge for some attributes has common objects");
                    }
                }
            }
        }
    }

    public GNode getRoot() {
        return root;
    }

    public void setRoot(GNode root) {
        this.root = root;
    }

    public Vector<GNode> getChildren() {
        return children;
    }

    public void setChildren(Vector<GNode> children) {
        this.children = children;
    }

    public static class GNode{
        // 节点对应的属性
        private String label;

        // 节点属性对应的外延
        private BasicSet objects;

        public GNode(String label, BasicSet objects){
            this.label = label;
            this.objects = objects.clone();
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public BasicSet getObjects() {
            return objects;
        }

        public void setObjects(BasicSet objects) {
            this.objects = objects;
        }
    }
}
