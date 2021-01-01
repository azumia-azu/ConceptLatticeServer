package cn.com.xidian.conceptlatticeserver.module;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Vector;

@Getter
@Setter
@ApiModel
public class LatticeMap implements Serializable {
    @NotNull(message = "图名不能为空")
    @ApiModelProperty(value = "name", example = "32ffw3-kjio44-b7rane-pkan8b")
    private String name;

    @ApiModelProperty(value = "objects", example ="'[1, 2, 3, 4, 5, 6] (Array)'")
    private Vector<String> objects;

    @ApiModelProperty(value = "attributes", example = "'[a, b, c, d, e] (Array)'")


    private Vector<String> attributes;

    @ApiModelProperty(value = "relations", example = "'[\n" +
        "[1, 0, 0 ,1, 1,]\n" +
        "[0, 1, 1, 0, 0],\n" +
        "[0, 1, 1, 0, 0],\n" +
        "[0, 1, 1, 0, 0],\n" +
        "[0, 1, 1, 0, 0],\n" +
        "[0, 1, 1, 0, 0],\n" +
        "... etc. objects for row, attributes for col, 0/1 for has connected or not as [][]String Array'")
    private Vector<Vector<String>> relations;

    public Vector<Vector<String>> getRelations() {
        Vector<Vector<String>> values = new Vector<>();
        for(int i = 0; i < objects.size(); i++){
            Vector<String> rowValues = new Vector<>();
            for(String s: relations.get(i)){
                if(s.equals("1")) {
                    rowValues.add("true");
                }else{
                    rowValues.add("");
                }
            }
            values.add(rowValues);
        }

        return values;
    }
}
