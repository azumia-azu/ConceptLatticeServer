package cn.com.xidian.conceptlatticeserver.controller;

import cn.com.xidian.conceptlatticeserver.module.Operation;
import cn.com.xidian.conceptlatticeserver.module.ResponseFormat;
import cn.com.xidian.conceptlatticeserver.service.ServerService;
import fca.ConceptLatticeAlgo;
import com.alibaba.fastjson.JSONObject;
import fca.exception.AlreadyExistsException;
import fca.exception.GTreeConstructionException;
import fca.exception.InvalidTypeException;
import io.swagger.annotations.*;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;


@RestController
@RequestMapping(path = "/resources")
@Api(value = "Concept Lattice Server", produces = MediaType.APPLICATION_JSON_VALUE)
public class ServerController {

    @ResponseBody
    @GetMapping(path = "/graph")
    @ApiOperation(value = "获得图", httpMethod = "GET")
    @ApiResponse(code = 200, message = "OK", response = ResponseFormat.class)
    public ResponseFormat getGraphHandler(HttpServletRequest request) throws InvalidTypeException, IOException, GTreeConstructionException, AlreadyExistsException {
        return new ResponseFormat(200, "OK", ServerService.HandleGetGraphService(request.getSession()));
    }

    @ResponseBody
    @PostMapping(path = "/graph")
    @ApiOperation(value="操作图", httpMethod = "POST")
    @ApiResponse(code = 200, message = "OK", response = ResponseFormat.class)
    public ResponseFormat postGraphHandler(HttpServletRequest request, @RequestBody @ApiParam Operation object) throws Exception {
        var session = request.getSession();
        return new ResponseFormat(200, "OK", switch (object.getOperate()) {
            case "zoom_in" -> ServerService.ZoomIn(session, object.getData());
            case "zoom_out" -> ServerService.ZoomOut(session, object.getData());
            default -> throw new Exception();
        });
    }
}
