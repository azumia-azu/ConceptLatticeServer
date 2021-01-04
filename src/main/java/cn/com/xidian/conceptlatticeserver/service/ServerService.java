package cn.com.xidian.conceptlatticeserver.service;

import cn.com.xidian.conceptlatticeserver.module.ConceptLattice;
import cn.com.xidian.conceptlatticeserver.module.LatticeMap;
import cn.com.xidian.conceptlatticeserver.module.SerializableBinaryContext;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import fca.core.util.BasicSet;
import fca.exception.AlreadyExistsException;
import fca.exception.GTreeConstructionException;
import fca.exception.InvalidTypeException;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Hashtable;

@Service
public class ServerService {
    public static JSONArray HandleGetGraphStaticService(HttpSession session) throws InvalidTypeException, IOException, GTreeConstructionException, AlreadyExistsException {
        return getGraphJSON(session, "graph-static");
    }

    public static JSONArray HandleGetGraphService(HttpSession session, String name) throws InvalidTypeException, IOException, GTreeConstructionException, AlreadyExistsException {
        return getGraphJSON(session, name);
    }

    public static JSONArray HandleSetGraphService(HttpSession session, LatticeMap map) throws AlreadyExistsException, InvalidTypeException, IOException, GTreeConstructionException {
        session.setAttribute(map.getName(), new SerializableBinaryContext(map.getName(), map.getObjects(), map.getAttributes(), map.getRelations() ));
        return getGraphJSON(session, map.getName());
    }

    private static ConceptLattice getGraph(HttpSession session, String name) throws AlreadyExistsException, InvalidTypeException {
        var context = (SerializableBinaryContext) session.getAttribute(name);
        return new ConceptLattice(context);
    }

    private static void setGraph(HttpSession session, String name, ConceptLattice graph) {
        session.setAttribute(name, new SerializableBinaryContext(graph.getInstance().getLattice().getContext()));
    }

    private static JSONArray getGraphJSON(HttpSession session, String name) throws AlreadyExistsException, InvalidTypeException, IOException, GTreeConstructionException {
        ConceptLattice data = null;
        if (name.equals("graph-staitc")) {
            data = ConceptLattice.InitWithFiles();
        } else {
            data = getGraph(session, name);
        }
        var nodeList = data.getInstance().getGraphicalLattice().getNodesList();
        var res = new JSONArray();
        for (var node : nodeList) {
            var elem = new JSONObject();
            elem.put("attribute", JSONArray.toJSON(node.getNestedConcept().getIntent()));
            elem.put("objects", JSONArray.toJSON(node.getNestedConcept().getExtent()));
            res.add(elem);
        }

        return res;
    }

    public static JSONArray ZoomIn(HttpSession session, JSONObject operation, String name) throws InvalidTypeException, GTreeConstructionException, AlreadyExistsException, IOException {
        var data = getGraph(session, name);

        data.getInstance().zoomIn(operation.getString("root"), parseTable((JSONArray) operation.get("sub")));
        setGraph(session, name, data);
        return getGraphJSON(session, name);
    }

    public static JSONArray ZoomOut(HttpSession session, JSONObject operation, String name) throws InvalidTypeException, GTreeConstructionException, AlreadyExistsException, IOException {
        var data = getGraph(session, name);

        data.getInstance().zoomOut(operation.getString("root"), parseTable((JSONArray) operation.get("sub")));
        setGraph(session, name, data);
        return getGraphJSON(session, name);
    }

    private static Hashtable<String, BasicSet> parseTable(JSONArray arr) {
        var ret = new Hashtable<String, BasicSet>();
        for (var obj : arr) {
            var json = (JSONObject) obj;
            var set = new BasicSet();

            for (var object: json.getJSONArray("objects")) {
                set.add((String) object);
            }

            ret.put(json.getString("root"), set);
        }

        return ret;
    }
}
