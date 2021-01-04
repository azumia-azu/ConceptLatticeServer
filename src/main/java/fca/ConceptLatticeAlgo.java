package fca;

import fca.core.context.binary.BinaryContext;
import fca.core.lattice.ConceptLattice;
import fca.core.util.BasicSet;
import fca.exception.AlreadyExistsException;
import fca.exception.GTreeConstructionException;
import fca.exception.InvalidTypeException;
import fca.gui.lattice.element.GraphicalLattice;
import fca.gui.lattice.element.LatticeStructure;
import lombok.Getter;

import java.util.Hashtable;
import java.util.Vector;

@Getter
public class ConceptLatticeAlgo {

    /**
     * 概念格
     */
    private final ConceptLattice lattice;

    /**
     * 图形化的概念格
     */
    private GraphicalLattice graphicalLattice;

    public ConceptLatticeAlgo(String name, Vector<String> objects, Vector<String> attributes,
                              Vector<Vector<String>> values) throws AlreadyExistsException, InvalidTypeException {
        BinaryContext context = new BinaryContext(name, objects, attributes, values);
        lattice = new ConceptLattice(context);
        LatticeStructure struct = new LatticeStructure(lattice, context, LatticeStructure.BEST);
        graphicalLattice = new GraphicalLattice(lattice, struct);
    }

    public void zoomIn(String p, Hashtable<String,BasicSet> ws)
            throws GTreeConstructionException, AlreadyExistsException, InvalidTypeException {
        lattice.zoomIn(p, ws);
        LatticeStructure struct = new LatticeStructure(lattice,lattice.getContext(), LatticeStructure.BEST);
        graphicalLattice = new GraphicalLattice(lattice, struct);
    }

    public void zoomOut(String p, Hashtable<String, BasicSet> ws)
            throws GTreeConstructionException, AlreadyExistsException, InvalidTypeException{
        lattice.zoomOut(p, ws);
        LatticeStructure struct = new LatticeStructure(lattice,lattice.getContext(), LatticeStructure.BEST);
        graphicalLattice = new GraphicalLattice(lattice, struct);
    }
}
