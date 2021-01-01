package cn.com.xidian.conceptlatticeserver.controller;

import cn.com.xidian.conceptlatticeserver.module.LatticeMap;
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
    @GetMapping(path = "/graph/static")
    @ApiOperation(value = "获得默认图", httpMethod = "GET")
    @ApiResponse(code = 200, message = "OK", response = ResponseFormat.class)
    public ResponseFormat getGraphStaticHandler(HttpServletRequest request) throws InvalidTypeException, IOException, GTreeConstructionException, AlreadyExistsException {
        return new ResponseFormat(200, "OK", ServerService.HandleGetGraphStaticService(request.getSession()));
    }

    @ResponseBody
    @GetMapping(path = "/graph")
    @ApiOperation(value = "获得默认图", httpMethod = "GET")
    @ApiResponse(code = 200, message = "OK", response = ResponseFormat.class)
    public ResponseFormat getGraphHandler(HttpServletRequest request, @RequestParam("name") String name) throws InvalidTypeException, IOException, GTreeConstructionException, AlreadyExistsException {
        return new ResponseFormat(200, "OK", ServerService.HandleGetGraphService(request.getSession(), name));
    }

    @ResponseBody
    @PostMapping
    @ApiOperation(value = "创建图", httpMethod = "POST")
    @ApiResponse(code = 200, message = "OK", response = ResponseFormat.class)
    public ResponseFormat setGraphHandler(HttpServletRequest request, @RequestBody @ApiParam LatticeMap map) throws InvalidTypeException, IOException, GTreeConstructionException, AlreadyExistsException {
        return new ResponseFormat(200, "OK", ServerService.HandleSetGraphService(request.getSession(), map));
    }

    @ResponseBody
    @PutMapping(path = "/graph")
    @ApiOperation(value="操作图", httpMethod = "PUT")
    @ApiResponse(code = 200, message = "OK", response = ResponseFormat.class)
    public ResponseFormat postGraphHandler(HttpServletRequest request, @RequestBody @ApiParam Operation object) throws Exception {
        var session = request.getSession();
        return new ResponseFormat(200, "OK", switch (object.getOperate()) {
            case "zoom_in" -> ServerService.ZoomIn(session, object.getData(), object.getName());
            case "zoom_out" -> ServerService.ZoomOut(session, object.getData(), object.getName());
            default -> throw new Exception();
        });
    }
}
