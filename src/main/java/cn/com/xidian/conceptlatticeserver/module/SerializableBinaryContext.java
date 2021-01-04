package cn.com.xidian.conceptlatticeserver.module;

import fca.exception.AlreadyExistsException;
import fca.exception.InvalidTypeException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Vector;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SerializableBinaryContext implements Serializable {
    private String name;
    private Vector<String> objects;
    private Vector<String> attributes;
    private Vector<Vector<String>> relations;

    public SerializableBinaryContext(fca.core.context.binary.BinaryContext context) {
        name = context.getName();
        objects = context.getObjects();
        attributes = context.getAttributes();
        relations = context.getValues();
    }

}
