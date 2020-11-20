package fca.core.lattice;

import java.util.*;
import java.util.stream.BaseStream;

import fca.core.context.binary.BinaryContext;
import fca.core.util.BasicSet;
import fca.exception.AlreadyExistsException;
import fca.exception.GTreeConstructionException;
import fca.exception.InvalidTypeException;
import fca.exception.LMLogger;
import fca.messages.CoreMessages;

public class ConceptLattice {
	
	private BinaryContext context;
	private Vector<FormalConcept> concepts;
	
	private FormalConcept topConcept;
	
	private FormalConcept bottomConcept;
	
	private String name;
	
	private boolean generatorsCalculated;

	private Hashtable<FormalConcept,Set<FormalConcept>> ancestors = new Hashtable<>();

	private Hashtable<FormalConcept, Hashtable<String, FormalConcept>> wChildren = new Hashtable<>();

	private Hashtable<FormalConcept, Hashtable<String, FormalConcept>> wParents = new Hashtable<>();

	private GTree gTree;

	private BasicSet pIntent = new BasicSet();

	private BasicSet childrenLabels = new BasicSet();

	private Set<FormalConcept> newConcepts = new HashSet<>();
	
	public ConceptLattice(BinaryContext bc) {
		context = bc;
		generatorsCalculated = false;
		
		BordatAlgo algo = new BordatAlgo(context);
		
		concepts = algo.getConcepts();
		
		if (concepts.size() == 0) {
			topConcept = null;
			bottomConcept = null;
		} else {
			FormalConcept currentConcept = concepts.elementAt(0);
			while (currentConcept.getParents().size() > 0) {
				Vector<FormalConcept> parents = currentConcept.getParents();
				currentConcept = parents.elementAt(0);
			}
			topConcept = currentConcept;
			
			currentConcept = concepts.elementAt(0);
			while (currentConcept.getChildren().size() > 0) {
				Vector<FormalConcept> children = currentConcept.getChildren();
				currentConcept = children.elementAt(0);
			}
			bottomConcept = currentConcept;
		}
		
		name = bc.getName();
	}
	
	public ConceptLattice(FormalConcept fc, String n) {
		concepts = findConcepts(fc);
		
		topConcept = fc;
		
		FormalConcept currentConcept = fc;
		while (currentConcept.getChildren().size() > 0) {
			Vector<FormalConcept> children = currentConcept.getChildren();
			currentConcept = children.elementAt(0);
		}
		bottomConcept = currentConcept;
		
		name = n;
		context = getContext();
		generatorsCalculated = false;
	}
	
	public ConceptLattice(FormalConcept fc, String n, boolean bottomFirst) {
		concepts = findConcepts(fc, bottomFirst);
		
		if (bottomFirst) {
			bottomConcept = fc;
			
			FormalConcept currentConcept = fc;
			while (currentConcept.getParents().size() > 0) {
				Vector<FormalConcept> parent = currentConcept.getParents();
				currentConcept = parent.elementAt(0);
			}
			topConcept = currentConcept;
		} else {
			topConcept = fc;
			
			FormalConcept currentConcept = fc;
			while (currentConcept.getChildren().size() > 0) {
				Vector<FormalConcept> children = currentConcept.getChildren();
				currentConcept = children.elementAt(0);
			}
			bottomConcept = currentConcept;
		}
		
		name = n;
		context = getContext();
		generatorsCalculated = false;

	}
	
	/**
	 * Trouve tous les noeuds rejoignable a partir du noeud specifie, en incluant celui-ci
	 * @param node Le ConceptNode a partir duquel les noeuds doivent etre cherches
	 * @return Vector La liste des ConceptNode accessibles a partir du noeud specifie
	 */
	private Vector<FormalConcept> findConcepts(FormalConcept node) {
		return findConcepts(node, false);
	}
	
	/**
	 * Trouve tous les noeuds rejoignable a partir du noeud specifie, en incluant celui-ci
	 * @param node Le ConceptNode a partir duquel les noeuds doivent etre cherches
	 * @return Vector La liste des ConceptNode accessibles a partir du noeud specifie
	 */
	private Vector<FormalConcept> findConcepts(FormalConcept node, boolean bottomFirst) {
		Vector<FormalConcept> visitedNodes = new Vector<FormalConcept>();
		visitedNodes.add(node);
		
		if (!bottomFirst) {
			/* Ajout des enfants qui n'ont pas encore ete ajoutes */
			List<FormalConcept> children = node.getChildren();
			for (int i = 0; i < children.size(); i++) {
				FormalConcept currentChild = children.get(i);
				if (!visitedNodes.contains(currentChild))
					visitedNodes.add(currentChild);
				
				/* Ajout des noeuds visites a partir des enfants et qui n'ont pas encore ete ajoutes */
				Vector<FormalConcept> childNodes = findConcepts(currentChild, bottomFirst);
				for (int j = 0; j < childNodes.size(); j++) {
					FormalConcept currentNode = childNodes.elementAt(j);
					if (!visitedNodes.contains(currentNode))
						visitedNodes.add(currentNode);
				}
			}
		} else {
			/* Ajout des parents qui n'ont pas encore ete ajoutes */
			List<FormalConcept> parents = node.getParents();
			for (int i = 0; i < parents.size(); i++) {
				FormalConcept currentParent = parents.get(i);
				if (!visitedNodes.contains(currentParent))
					visitedNodes.add(currentParent);
				
				/* Ajout des noeuds visites a partir des parents et qui n'ont pas encore ete ajoutes */
				Vector<FormalConcept> parentsNodes = findConcepts(currentParent, bottomFirst);
				for (int j = 0; j < parentsNodes.size(); j++) {
					FormalConcept currentNode = parentsNodes.elementAt(j);
					if (!visitedNodes.contains(currentNode))
						visitedNodes.add(currentNode);
				}
			}
		}
		
		return visitedNodes;
	}
	
	public Vector<FormalConcept> getConcepts() {
		return concepts;
	}
	
	/**
	 * @return le supremum
	 */
	public FormalConcept getTopConcept() {
		return topConcept;
	}
	
	/**
	 * @return l'infimum
	 */
	public FormalConcept getBottomConcept() {
		return bottomConcept;
	}
	
	/**
	 * Permet d'obtenir le concept qui possede l'intention specifiee. Retourne null si aucun concept
	 * n'est trouve.
	 * @param intent Le BasicSet contenant l'intent recherche
	 * @return Le FormalConcept qui possede l'intention specifiee
	 */
	public FormalConcept getConceptWithIntent(BasicSet intent) {
		
		/* Le treillis ne possede aucun concept */
		if (topConcept == null)
			return null;
		
		/* Chaque concept du treillis a plus d'attributs que ceux recherches */
		BasicSet topIntent = topConcept.getIntent();
		if (intent.size() < topIntent.size())
			return null;
		
		/* L'intent contient des attributs qui ne font pas partie du treillis */
		BasicSet intersection = bottomConcept.getIntent().intersection(intent);
		if (intent.size() != intersection.size())
			return null;
		
		/* Le concept recherche est le supremum */
		if (intent.size() == topIntent.size() && intent.equals(topIntent))
			return topConcept;
		
		/* Le concept recherche n'est pas le supremum */
		else {
			/* Recherche du noeud */
			FormalConcept currentConcept = topConcept;
			BasicSet currentIntent = currentConcept.getIntent();
			BasicSet currentInter = intersection.intersection(currentIntent);
			BasicSet remainder = intersection.difference(currentIntent);
			
			while (currentConcept != null && currentInter.size() != intersection.size()
					&& currentInter.size() == currentIntent.size()) {
				
				Vector<FormalConcept> children = currentConcept.getChildren();
				currentConcept = null;
				
				for (int i = 0; i < children.size() && currentConcept == null; i++) {
					FormalConcept child = children.elementAt(i);
					BasicSet childIntent = child.getIntent();
					BasicSet childInter = intersection.intersection(childIntent);
					BasicSet childRemainder = childIntent.difference(childInter);
					
					if (childInter.size() > 0 && childRemainder.size() == 0) {
						currentConcept = child;
						currentIntent = currentConcept.getIntent();
						currentInter = intersection.intersection(currentIntent);
						remainder = intersection.difference(currentIntent);
					}
				}
			}
			
			/*
			 * Il y a trop d'attributs dans le premier concept trouve qui contient tous les
			 * attributs demandes
			 */
			if (currentInter.size() != currentIntent.size())
				return null;
			
			/* Les attributs ont tous ete trouves */
			if (remainder.size() == 0)
				return currentConcept;
		}
		return null;
	}
	
	/**
	 * @return le nom du treillis
	 */
	public String getName() {
		return name;
	}
	
	public int size() {
		return concepts.size();
	}
	
	@Override
	public String toString() {
		String str = CoreMessages.getString("Core.name") + ": " + context.getName() + "\n" + CoreMessages.getString("Core.conceptCount") + ": " + concepts.size() + "\n" + CoreMessages.getString("Core.attributes") + ": " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
				+ bottomConcept.getIntent().toString() + "\n" + CoreMessages.getString("Core.objects") + ": " + topConcept.getExtent().toString(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		for (int i = 0; i < concepts.size(); i++) {
			FormalConcept c = concepts.elementAt(i);
			
			str = str + "\n\n" + CoreMessages.getString("Core.concept") + " : " + c.getIntent().toString() + "\n" + CoreMessages.getString("Core.children") + " :"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			Vector<FormalConcept> children = c.getChildren();
			for (int j = 0; j < children.size(); j++)
				str = str + " " + (children.elementAt(j)).getIntent().toString(); //$NON-NLS-1$
			
			str = str + "\n" + CoreMessages.getString("Core.parents") + " :"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			Vector<FormalConcept> parents = c.getParents();
			for (int j = 0; j < parents.size(); j++)
				str = str + " " + (parents.elementAt(j)).getIntent().toString(); //$NON-NLS-1$
		}
		return str;
	}
	
	public boolean areGeneratorsCalculated() {
		return generatorsCalculated;
	}
	
	/**
	 * @param calculated
	 */
	public void setGeneratorsCalculated(boolean calculated) {
		generatorsCalculated = calculated;
	}

	public JenAlgorithm findGenerators() {
		return new JenAlgorithm(this);
	}
	
	public BinaryContext getContext() {
		Vector<String> attributes = new Vector<String>();
		attributes.addAll(getBottomConcept().getIntent());
		
		Vector<String> objects = new Vector<String>();
		objects.addAll(getTopConcept().getExtent());
		
		BinaryContext newContext = new BinaryContext(getName());
		try {
			for (int i = 0; i < objects.size(); i++)
				newContext.addObject(objects.elementAt(i));
			for (int i = 0; i < attributes.size(); i++)
				newContext.addAttribute(attributes.elementAt(i));
		} catch (AlreadyExistsException e) {
			// Never reach because the original BinaryContext is valid
			LMLogger.logSevere(e, false);
		}
		
		Vector<FormalConcept> conceptList = getConcepts();
		for (int i = 0; i < conceptList.size(); i++) {
			FormalConcept currentConcept = conceptList.elementAt(i);
			Iterator<String> intent = currentConcept.getIntent().iterator();
			
			while (intent.hasNext()) {
				String currAtt = intent.next();
				
				Iterator<String> extent = currentConcept.getExtent().iterator();
				while (extent.hasNext()) {
					String currObj = extent.next();
					
					try {
						newContext.setValueAt(BinaryContext.TRUE, currObj, currAtt);
					} catch (InvalidTypeException e) {
						// Never reach because the original BinaryContext is valid
						LMLogger.logSevere(e, false);
					}
				}
			}
		}
		
		return newContext;
	}

	public void zoomIn(String p, Hashtable<String, BasicSet> ws) throws GTreeConstructionException {
		// 初始化各个节点的祖先节点集合
		initAncestors();

		// 优先队列用于每次取最大的概念
		PriorityQueue<FormalConcept> m = new PriorityQueue<>(Comparator.comparingInt(o -> -o.extent.size()));
		// Queue<FormalConcept> m = new LinkedList<>();

		// g-tree用来对属性与子属性及对应的对象进行检索
		gTree = new GTree(context, p, ws, true);

		// g-tree的root p对应的属性,构建g-tree中的叶节点的标签集，即被分解成的属性集
		pIntent.add(p);
		for(GTree.GNode gNode: gTree.getChildren()) {
			childrenLabels.add(gNode.getLabel());
		}

		// 获取需要放大的最大的概念
		m.add(getConceptWithIntent(pIntent));

		FormalConcept currentConcept;
		while(!m.isEmpty()){
			currentConcept = m.poll();
			// 添加后继节点到m中
			for(FormalConcept c: currentConcept.getChildren()) {
				if(!m.contains(c)){
					m.add(c);
				}
			}

			// 构建合法的SplitC集合
			Vector<FormalConcept> splitFormalConcepts = new Vector<>();
			for(GTree.GNode gNode: gTree.getChildren()){
				BasicSet i = new BasicSet();
				i.add(gNode.getLabel());

				BasicSet e = new BasicSet();
				e.addAll(gNode.getObjects().intersection(currentConcept.extent));

				boolean isExtentValid = true;
				for(FormalConcept c : currentConcept.getChildren()){
					if(c.extent.isIncluding(e)){
						isExtentValid = false;
						break;
					}
				}

				if(isExtentValid){
					splitFormalConcepts.add(new FormalConcept(e, i));
				}
			}

			for(FormalConcept sfc: splitFormalConcepts){
				if(sfc.extent.equals(currentConcept.extent)){
					// 更新当前概念的内涵
					currentConcept.setIntent(currentConcept.intent.union(sfc.intent).difference(pIntent));

					setWChild(sfc.intent.first(), currentConcept, currentConcept);

					appendUpperChildren(currentConcept, currentConcept, sfc.intent);
				}else{
					// 创建新概念并添加到概念集中
					FormalConcept newFc = new FormalConcept(sfc.extent, currentConcept.intent.union(sfc.intent).difference(pIntent));
					newConcepts.add(newFc);
					concepts.add(newFc);

					setWChild(sfc.intent.first(), currentConcept, newFc);

					addEdge(newFc, currentConcept);

					appendUpperChildren(newFc, currentConcept, sfc.intent);
				}
			}

			// 判断当前概念是否需要被合并，若需要则在循环中进行合并
			FormalConcept mergeConcept = null;
			Vector<FormalConcept> parents = currentConcept.getParents();
			for(FormalConcept parent: parents){
				if(currentConcept.intent.equals(parent.intent.union(pIntent))){
					mergeConcept = parent;
					break;
				}
			}
			if(mergeConcept != null){
				merge(currentConcept, mergeConcept, null, pIntent, m);
			}
			// 否则直接将当前概念中的要被分解的属性删除
			else {
				currentConcept.setIntent(currentConcept.intent.difference(pIntent));
			}
		}
		// 重置上下文
		context = getContext();
	}

	public void zoomOut(String p, Hashtable<String, BasicSet> ws) throws GTreeConstructionException{
		// 初始化各个概念的祖先概念集合
		initAncestors();

		// 优先队列用于每次取最大的概念
		PriorityQueue<FormalConcept> m = new PriorityQueue<>(Comparator.comparingInt(o -> -o.extent.size()));

		// g-tree用来对属性与子属性及对应的对象进行检索
		gTree = new GTree(context, p, ws, false);

		// 构建g-tree中的叶节点的标签集，即被分解成的属性集
		pIntent.add(p);
		for(GTree.GNode gNode: gTree.getChildren()){
			childrenLabels.add(gNode.getLabel());
		}

		m.add(topConcept);

		FormalConcept currentConcept;
		while(!m.isEmpty()){
			currentConcept = m.poll();
			for(FormalConcept c: currentConcept.getChildren()) {
				if(!m.contains(c)){
					m.add(c);
				}
			}

			if(!concepts.contains(currentConcept)){
				continue;
			}

			if(currentConcept.extent.intersection(gTree.getRoot().getObjects()).equals(currentConcept.extent)){
				appendUpperChildren(currentConcept, currentConcept, pIntent);

				currentConcept.setIntent(currentConcept.intent.difference(childrenLabels).union(pIntent));

				setWChild(pIntent.first(), currentConcept, currentConcept);

				Vector<FormalConcept> mergeConcept = new Vector<>();
				for(FormalConcept s: currentConcept.getChildren()){
					for(GTree.GNode gNode: gTree.getChildren()){
						BasicSet set = new BasicSet();
						set.add(gNode.getLabel());
						if(s.intent.union(pIntent).difference(set).equals(currentConcept.intent)){
							mergeConcept.add(s);
							break;
						}
					}
				}

				for(FormalConcept mc: mergeConcept){
					merge(mc, currentConcept, pIntent, childrenLabels, m);
				}
			}else if(isSuccessorContainsNewConcept(currentConcept, gTree.getRoot().getObjects())){
				FormalConcept newFc = new FormalConcept(currentConcept.extent.intersection(gTree.getRoot().getObjects()),
						currentConcept.intent.difference(childrenLabels).union(pIntent));
				newConcepts.add(newFc);
				concepts.add(newFc);

				appendUpperChildren(newFc, currentConcept, pIntent);

				addEdge(newFc, currentConcept);

				setWChild(pIntent.first(), currentConcept, newFc);

				Vector<FormalConcept> mergeConcept = new Vector<>();
				for(FormalConcept s: currentConcept.getChildren()){
					if(s.equals(newFc)){
						continue;
					}

					for(GTree.GNode gNode: gTree.getChildren()){
						BasicSet set = new BasicSet();
						set.add(gNode.getLabel());
						if(s.intent.union(pIntent).difference(set).equals(newFc.intent)){
							mergeConcept.add(s);
							break;
						}
					}
				}

				for(FormalConcept mc: mergeConcept){
					merge(mc, newFc, pIntent, pIntent, m);
				}
			}
		}

		context = getContext();
	}

	private boolean isSuccessorContainsNewConcept(FormalConcept fc, BasicSet intent){
		Vector<FormalConcept> successorOfC = fc.getChildren();
		for(FormalConcept s : successorOfC){
			if(s.extent.isIncluding(fc.extent.intersection(intent))){
				return false;
			}
		}

		return true;
	}

	private void setWChild(String w, FormalConcept fc, FormalConcept childConcept){
		// 更新当前概念格的w-child
		Hashtable<String, FormalConcept> wChildrenMap = wChildren.getOrDefault(fc, new Hashtable<>());
		wChildrenMap.put(w, childConcept);
		wChildren.put(fc, wChildrenMap);

		// 更新子概念格的w-parent
		Hashtable<String, FormalConcept> wParentsMap = wParents.getOrDefault(childConcept, new Hashtable<>());
		wParentsMap.put(w, fc);
		wParents.put(childConcept, wParentsMap);
	}

	private FormalConcept getWChild(FormalConcept fc, String w){
		Hashtable<String, FormalConcept> childrenMap = wChildren.getOrDefault(fc, new Hashtable<>());
		return childrenMap.get(w);
	}

	private void appendUpperChildren(FormalConcept fc, FormalConcept parentConcept, BasicSet w){
		Set<FormalConcept> upperParents = new HashSet<>();

		// 找到所有节点的wParent
		Set<FormalConcept> wParents = new HashSet<>();
		findWParents(fc, w.first(), wParents);

		// 公共节点
		Vector<FormalConcept> commonParents = new Vector<>();
		findUpperParents(parentConcept, upperParents);
		for(FormalConcept up: upperParents){
			if(wParents.contains(up)){
				commonParents.add(up);
			}
		}
		Set<FormalConcept> minimalParents = findMinimalParents(commonParents);
		// 当当前节点为新创建节点
		if(newConcepts.contains(fc)){
			/*if(w.equals(pIntent)) {
				for (FormalConcept cp : commonParents) {
					FormalConcept wChild = getWChild(cp, w.first());
					addEdge(fc, wChild);
				}
			}*/
			for (FormalConcept mp : minimalParents) {
				FormalConcept wChildOfMp = getWChild(mp, w.first());
				addEdge(fc, wChildOfMp);
			}
		}else {
			// Set<FormalConcept> minimalParents = findMinimalParents(commonParents);
			for(FormalConcept mp: minimalParents){
				FormalConcept wChildOfMp = getWChild(mp,w.first());
				if(wChildOfMp != mp){
					if(fc.getParents().contains(mp)){
						deleteEdge(fc, mp);
					}

					addEdge(fc, wChildOfMp);
				}
			}
		}
	}

	private void initAncestors(){
		findAncestors(bottomConcept);
	}

	private void findAncestors(FormalConcept fc){
		Vector<FormalConcept> parents = fc.getParents();
		for(FormalConcept c: parents){
			findAncestors(c);
		}
		Set<FormalConcept> set = new HashSet<>();
		recurseFindAncestors(fc, set);
		ancestors.put(fc, set);
	}

	private void recurseFindAncestors(FormalConcept fc, Set<FormalConcept> set){
		Vector<FormalConcept> parents = fc.getParents();
		set.addAll(parents);

		for(FormalConcept p: parents){
			recurseFindAncestors(p, set);
		}
	}

	private void findUpperParents(FormalConcept fc, Set<FormalConcept> upperParents){
		upperParents.addAll(ancestors.get(fc));
	}

	private void findWParents(FormalConcept fc, String w, Set<FormalConcept> parents){
		for(Map.Entry<FormalConcept, Hashtable<String,FormalConcept>> entry: wParents.entrySet()){
			Hashtable<String,FormalConcept> wParentsMap = entry.getValue();
			if(wParentsMap.containsKey(w)){
				parents.add(wParentsMap.get(w));
			}
		}
	}

	private Set<FormalConcept> findMinimalParents(Vector<FormalConcept> commonParents){
		// 对共有的概念格按大小进行排序
		commonParents.sort(Comparator.comparingInt(o -> o.extent.size()));

		// 找到共有概念中的所有minimal概念
		Vector<FormalConcept> temp = new Vector<>();
		for(FormalConcept cp: commonParents){
			if(temp.isEmpty()){
				temp.add(cp);
			}else {
				boolean existsSmallerConcept = false;
				for(FormalConcept mp: temp){
					// 概念格中小于的定义
					if(cp.extent.isIncluding(mp.extent) && mp.intent.isIncluding(cp.intent)){
						existsSmallerConcept = true;
						break;
					}
				}

				if(!existsSmallerConcept){
					temp.add(cp);
				}
			}
		}

		return new HashSet<>(temp);
	}

	private void merge(FormalConcept fc, FormalConcept parentConcept, BasicSet w, BasicSet p, PriorityQueue<FormalConcept> m){
		Vector<FormalConcept> successorOfC = fc.getChildren();
		for(FormalConcept s: successorOfC){
			if(!isSuccessorHasParent(fc, parentConcept, s)){
				if(newConcepts.contains(parentConcept)){
					for(FormalConcept c: fc.getChildren()){
						if(!m.contains(c)){
							m.add(c);
						}
					}
				}else {
					addEdge(s, parentConcept);
				}
			}

			for(String attr: childrenLabels.union(p)){
				Hashtable<String, FormalConcept> wChildrenMapOfC = wChildren.getOrDefault(fc, new Hashtable<>());

				for(String key: wChildrenMapOfC.keySet()){
					if(key.equals(attr)){
						setWChild(attr, parentConcept, wChildrenMapOfC.get(attr));
					}
				}
			}
		}

		deleteConcept(fc);
	}

	private  boolean isSuccessorHasParent(FormalConcept fc, FormalConcept parentConcept, FormalConcept childConcept){
		Vector<FormalConcept> successorOfParent = new Vector<>(parentConcept.getChildren());
		successorOfParent.remove(fc);

		for(FormalConcept s: successorOfParent){
			if(s.extent.isIncluding(childConcept.extent)){
				return true;
			}
		}

		return false;
	}

	private void deleteConcept(FormalConcept fc){
		Vector<FormalConcept> parents = fc.getParents();
		for(FormalConcept p: parents){
			p.getChildren().remove(fc);
		}

		Vector<FormalConcept> children = fc.getChildren();
		for(FormalConcept c: children){
			c.getParents().remove(fc);
		}

		// 从概念列表中删除当前概念
		concepts.remove(fc);

		// 从w-parents中删除当前概念
		Hashtable<String,FormalConcept> wParentsOfC = wParents.remove(fc);
		if(wParentsOfC != null){
			for(FormalConcept p: wParentsOfC.values()){
				Hashtable<String,FormalConcept> wChildrenOfParent = wChildren.get(p);
				if(wChildrenOfParent != null){
					Set<String> keys = wChildrenOfParent.keySet();
					for(String key: keys){
						if(fc == wChildrenOfParent.get(key)){
							wChildrenOfParent.remove(key);
						}
					}
				}
			}
		}

		// 从w-children中删除当前概念
		Hashtable<String,FormalConcept> wChildrenOfC = wChildren.remove(fc);
		if(wChildrenOfC != null) {
			for (FormalConcept c : wChildrenOfC.values()) {
				Hashtable<String, FormalConcept> wParentOfChild = wParents.get(c);
				if (wParentOfChild != null) {
					Set<String> keys = wParentOfChild.keySet();
					for (String key : keys) {
						if (fc == wParentOfChild.get(key)) {
							wParentOfChild.remove(key);
						}
					}
				}
			}
		}
	}

	private void deleteEdge(FormalConcept fc, FormalConcept parentConcept){
		fc.getParents().remove(parentConcept);
		parentConcept.getChildren().remove(fc);
	}

	private void addEdge(FormalConcept fc, FormalConcept parentConcept){
		if(parentConcept.getChildren().contains(fc) || fc.getParents().contains(parentConcept)){
			return;
		}

		fc.getParents().add(parentConcept);
		parentConcept.getChildren().add(fc);
	}
}