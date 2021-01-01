package cn.com.xidian.conceptlatticeserver.service;

import cn.com.xidian.conceptlatticeserver.module.ConceptLattice;
import cn.com.xidian.conceptlatticeserver.module.LatticeMap;
import cn.com.xidian.conceptlatticeserver.module.Operation;
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
        return GetGraph(session, "graph-static");
    }

    public static JSONArray HandleGetGraphService(HttpSession session, String name) throws InvalidTypeException, IOException, GTreeConstructionException, AlreadyExistsException {
        return GetGraph(session, name);
    }

    public static JSONArray HandleSetGraphService(HttpSession session, LatticeMap map) throws AlreadyExistsException, InvalidTypeException, IOException, GTreeConstructionException {
        var data = ConceptLattice.InitNew(map.getName(), map.getObjects(), map.getAttributes(), map.getRelations());
        session.setAttribute(map.getName(), data);
        return GetGraph(session, map.getName());
    }

    private static JSONArray GetGraph(HttpSession session, String name) throws AlreadyExistsException, InvalidTypeException, GTreeConstructionException, IOException {
        var data = (ConceptLattice) session.getAttribute(name);
        if (data == null && name.equals("graph-static")) {
            data = ConceptLattice.InitWithFiles();
            session.setAttribute(name, data);
        } else {
            return null;
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
        var data = (ConceptLattice) session.getAttribute(name);
        if (data == null) {
            throw new NullPointerException();
        }

        data.getInstance().zoomIn(operation.getString("root"), parseTable((JSONArray) operation.get("sub")));
        session.setAttribute(name, data);
        return GetGraph(session, name);
    }

    public static JSONArray ZoomOut(HttpSession session, JSONObject operation, String name) throws InvalidTypeException, GTreeConstructionException, AlreadyExistsException, IOException {
        var data = (ConceptLattice) session.getAttribute(name);
        if (data == null) {
            throw new NullPointerException();
        }

        data.getInstance().zoomOut(operation.getString("root"), parseTable((JSONArray) operation.get("sub")));
        session.setAttribute(name, data);
        return GetGraph(session, name);
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
