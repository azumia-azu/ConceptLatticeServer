package cn.com.xidian.conceptlatticeserver.module;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.bytebuddy.implementation.bind.annotation.Default;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@ApiModel
public class Operation {
    @NotNull(message = "操作不能为空")
    @ApiModelProperty(value = "operate", example = "zoom_in")
    private String operate;

    @ApiModelProperty(value = "name", example = "the name of graph, default: graph-static")
    private String name = "graph-static";

@ApiModelProperty(value = "data", example ="{\n" +
        "   \"root\" : \"d\",\n" +
        "   \"sub\" : [\n" +
        "       {\n" +
        "           \"root\" : \"d1\",\n" +
        "           \"objects\" : [\n" +
        "               \"2\",\n" +
        "               \"3\",\n" +
        "               \"6\",\n" +
        "               \"11\"\n" +
        "           ]\n" +
        "       },\n" +
        "       {\n" +
        "           \"root\" : \"d2\",\n" +
        "           \"objects\" : [\n" +
        "               \"8\",\n" +
        "               \"10\",\n" +
        "               \"13\",\n" +
        "               \"14\",\n" +
        "               \"15\"\n" +
        "           ]\n" +
        "       }\n" +
        "   ]\n" +
        "}")
    private JSONObject data;
}
