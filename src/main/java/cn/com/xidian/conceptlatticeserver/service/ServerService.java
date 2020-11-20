package cn.com.xidian.conceptlatticeserver.service;

import cn.com.xidian.conceptlatticeserver.module.ConceptLattice;
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
    public static JSONArray HandleGetGraphService(HttpSession session) throws InvalidTypeException, IOException, GTreeConstructionException, AlreadyExistsException {
        return GetGraph(session);
    }

    private static JSONArray GetGraph(HttpSession session) throws AlreadyExistsException, InvalidTypeException, GTreeConstructionException, IOException {
        var data = (ConceptLattice) session.getAttribute("graph");
        if (data == null) {
            data = ConceptLattice.InitWithFiles();
            session.setAttribute("graph", data);
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

    public static JSONArray ZoomIn(HttpSession session, JSONObject operation) throws InvalidTypeException, GTreeConstructionException, AlreadyExistsException, IOException {
        var data = (ConceptLattice) session.getAttribute("graph");
        if (data == null) {
            throw new NullPointerException();
        }

        data.getInstance().zoomIn(operation.getString("root"), parseTable((JSONArray) operation.get("sub")));
        session.setAttribute("graph", data);
        return GetGraph(session);
    }

    public static JSONArray ZoomOut(HttpSession session, JSONObject operation) throws InvalidTypeException, GTreeConstructionException, AlreadyExistsException, IOException {
        var data = (ConceptLattice) session.getAttribute("graph");
        if (data == null) {
            throw new NullPointerException();
        }

        data.getInstance().zoomOut(operation.getString("root"), parseTable((JSONArray) operation.get("sub")));
        session.setAttribute("graph", data);
        return GetGraph(session);
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
