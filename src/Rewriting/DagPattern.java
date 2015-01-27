package Rewriting;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;

public class DagPattern 
{		
	protected Node m_root;
	protected Node m_out;
	protected int m_size;

	protected ArrayList<Node> m_mainBranchNodesList;
	protected Node[] m_allNodesArray;
	
	private DagPattern()
	{
		m_root = null;
		m_out = null;
		m_size = 0;
	}

	//only used by view generator and parser
	public DagPattern(Node root, Node out, int size)
	{
		m_root = root;
		m_out = out;
		m_size = size;
		m_allNodesArray = new Node[m_size];
		m_mainBranchNodesList = new ArrayList<Node>();
		computeMainBranchNodesList();
		computeAllNodesArray();
	}
	
	private void computeMainBranchNodesList()
	{	
		m_mainBranchNodesList.clear();
		boolean[] seen = new boolean[m_size];
		for (int i = 0; i<m_size; ++i) seen[i] = false;
	
		int first = 0;
		int last = 0;
		m_mainBranchNodesList.add(m_root);
		seen[m_root.getIndex()] = true;
		
		while (first<=last)
		{
			ArrayList<NodeEdgeRecord> mbChildren = m_mainBranchNodesList.get(first).getMBChildren();
			for (int i = 0; i< mbChildren.size(); ++i) 
			{
				Node childNode = mbChildren.get(i).m_node;
				if (seen[childNode.getIndex()]) continue;
				m_mainBranchNodesList.add(childNode);
				seen[childNode.getIndex()] = true;
				last++;
			}
			first++;
		}
	}
	
	private DagPattern(DagPattern toCopy, Node start, Node end, boolean[] reachable)
	{
		m_size = toCopy.m_size;
		m_allNodesArray = new Node[m_size];
		m_mainBranchNodesList = new ArrayList<Node>();

		//make a copy of all valid (reachable) nodes
		for (int i = 0; i < m_size; ++i) 
			if (reachable[i]) m_allNodesArray[i] = new Node(toCopy.m_allNodesArray[i]);
			else m_allNodesArray[i] = null;

		//add necessary children/parents links
		for (int i = 0; i < m_size; i++) if (m_allNodesArray[i] != null) 
		{
			ArrayList<NodeEdgeRecord> children = toCopy.m_allNodesArray[i].getChildren();
			for (int j = 0; j < children.size(); ++j)
			{
				NodeEdgeRecord crtRec = children.get(j);
				int index = crtRec.m_node.getIndex();
				if (m_allNodesArray[index] == null) continue; //this child is not valid
				m_allNodesArray[i].addChild(m_allNodesArray[index], crtRec.m_edgeType);
			}		
		}

		this.m_root = m_allNodesArray[start.getIndex()];
		this.m_root.setRoot(true);

		this.m_out = m_allNodesArray[end.getIndex()];
		this.m_out.setOut(true);

		computeMainBranchNodesList();
	}
	
	
	private void computeAllNodesArray()
	{
		for (int i = 0; i< m_size; i++) m_allNodesArray[i]=null;
		computeAllNodesArray(m_root);
	}


	private void computeAllNodesArray(Node crtNode)
	{
		if (m_allNodesArray[crtNode.getIndex()]!=null) return;
		m_allNodesArray[crtNode.getIndex()] = crtNode;		

		ArrayList<NodeEdgeRecord> children = crtNode.getChildren();
		for (int i=0; i<children.size(); i++) 
			computeAllNodesArray(children.get(i).m_node);
	}
	
	
	/*
	 * Constructs the lossless prefix of length from the given dag 
	 */
	public static DagPattern getLosslessPrefix(DagPattern toCopy, int length)
	{
		DagPattern lp = new DagPattern();
		
		lp.m_size = toCopy.m_size;
		lp.m_allNodesArray = new Node[lp.m_size];
		lp.m_mainBranchNodesList = new ArrayList<Node>();

		//make a copy of all valid nodes
		for (int i = 0; i < lp.m_size; ++i) 
			if (null != toCopy.m_allNodesArray[i])
				lp.m_allNodesArray[i] = new Node(toCopy.m_allNodesArray[i]);
			else 
				lp.m_allNodesArray[i] = null;
		
		//turn into predicates the nodes that need that
		for (int i = length; i< toCopy.m_mainBranchNodesList.size(); ++i)
			lp.m_allNodesArray[toCopy.m_mainBranchNodesList.get(i).getIndex()].setAsPred();

		//add necessary children/parents links
		for (int i = 0; i < lp.m_size; i++) if (lp.m_allNodesArray[i] != null)
		{
			ArrayList<NodeEdgeRecord> children = toCopy.m_allNodesArray[i].getChildren();
			for (int j = 0; j < children.size(); ++j)
			{
				NodeEdgeRecord crtRec = children.get(j);
				int childindex = crtRec.m_node.getIndex();
				lp.m_allNodesArray[i].addChild(lp.m_allNodesArray[childindex], crtRec.m_edgeType);
			}		
		}

		lp.m_root = lp.m_allNodesArray[toCopy.m_root.getIndex()];
		lp.m_root.setRoot(true);

		lp.m_out = lp.m_allNodesArray[toCopy.m_mainBranchNodesList.get(length-1).getIndex()];
		lp.m_out.setOut(true);

		lp.computeMainBranchNodesList();
		
		return lp;
	}
	

	public static DagPattern buildFromDag(DagPattern toCopy, Node start, Node end, boolean keepOnlyMB)
	{
		//position "reachable" mask
		boolean[] reachable = new boolean[toCopy.m_size];
		for (int i = 0; i < toCopy.m_size; ++i) reachable[i] = false;
		toCopy.walkDagDownwards(start, reachable, keepOnlyMB);

		boolean[] tmp = new boolean[toCopy.m_size];
		for (int i = 0; i < toCopy.m_size; ++i) tmp[i] = false;
		toCopy.walkDagUpwards(end, tmp);

		for (int i = 0; i < toCopy.m_size; ++i) if (reachable[i] && toCopy.m_allNodesArray[i].isMB())
			if (!tmp[i]) reachable[i] = false;

		return new DagPattern(toCopy, start, end, reachable);
	}
	
	public static DagPattern buildFromDag(DagPattern toCopy)
	{
		return buildFromDag(toCopy, toCopy.m_root, toCopy.m_out, false);
	}

	public static DagPattern buildFromDag(DagPattern toCopy, Node n)
	{
		return buildFromDag(toCopy, n, toCopy.m_out, false);
	}
	
	public static DagPattern buildFromPath(DagPattern toCopy, ArrayList<NodeEdgeRecord> path, boolean keepOnlyMB)
	{
		boolean[] reachable = new boolean[toCopy.m_size];
		for (int i = 0; i<toCopy.m_size; ++i)  reachable[i] = false;
		for (int i = 0; i< path.size(); ++i)
		{
			reachable[path.get(i).m_node.getIndex()] = true;
			ArrayList<NodeEdgeRecord> children = path.get(i).m_node.getChildren();
			for (int j = 0; j< children.size(); ++j)
			{
				if (! keepOnlyMB && !children.get(j).m_node.isMB()) toCopy.walkDagDownwards(children.get(j).m_node, reachable, false);
			}
		}

		return new DagPattern(toCopy, path.get(0).m_node, path.get(path.size()-1).m_node, reachable);
	}
	
	public static DagPattern treeFromPathWithPredicates(DagPattern toCopy, ArrayList<Node> path)
	{
		boolean[] reachable = new boolean[toCopy.m_size];
		for (int i = 0; i<toCopy.m_size; ++i) reachable[i] = false;
		
		for (int i = 0; i< path.size(); ++i)
		{
			reachable[path.get(i).getIndex()] = true;
			ArrayList<Node> predChildren = path.get(i).getSlashPredChildren();
			for (int j = 0; j< predChildren.size(); ++j)
				toCopy.walkDagDownwards(predChildren.get(j), reachable, false);
			
			predChildren = path.get(i).getDescPredChildren();
			for (int j = 0; j< predChildren.size(); ++j)
				toCopy.walkDagDownwards(predChildren.get(j), reachable, false);
		}

		return new DagPattern(toCopy, path.get(0), path.get(path.size()-1), reachable);
	}
	
	/*
	public void pruneRedundantPredicates()
	{
		ArrayList<Node> mbNodes = getMainBranchNodesList();
		
		for (int i = 0; i<mbNodes.size(); ++i)
		{
			Node mbNode = mbNodes.get(i);
			ArrayList<NodeEdgeRecord> children = mbNode.getChildren();
			
			for (int j = 0; j< children.size(); ++j) 
			{
				Node predNode = children.get(j).m_node;
				if (predNode.isMB()) continue;
				
				DagPattern possiblyPruned = DagPattern.buildFromDag(this);
				Node newmbNode = possiblyPruned.m_allNodesArray[mbNode.getIndex()];
				Node newpredNode = possiblyPruned.m_allNodesArray[predNode.getIndex()];
				
				newmbNode.removeChild(newpredNode, children.get(j).m_edgeType);
				possiblyPruned.computeAllNodesArray();
				
				if (this.treeHasMappingTo(possiblyPruned, true, true))
				{
					mbNode.removeChild(predNode, children.get(j).m_edgeType);
					this.computeAllNodesArray();
				}
			}
		}
	}*/
	
	
	public void pruneRedundantPredicates(Node n)
	{
		DagPattern initNodeDag = DagPattern.buildFromDag(this, n, n, false);
		Node mbNode = initNodeDag.getRoot();
		
		ArrayList<NodeEdgeRecord> children = mbNode.getChildren();
		for (int j = 0; j< children.size(); ++j) 
		{
			Node predNode = children.get(j).m_node;
			if (predNode.isMB()) continue;
				
			DagPattern possiblyPruned = DagPattern.buildFromDag(initNodeDag);
			Node newmbNode = possiblyPruned.getRoot();
			Node newpredNode = possiblyPruned.m_allNodesArray[predNode.getIndex()];
				
			newmbNode.removeChild(newpredNode, children.get(j).m_edgeType);
			possiblyPruned.computeAllNodesArray();
				
			if (initNodeDag.treeHasMappingTo(possiblyPruned, true, true))
			{
				mbNode.removeChild(predNode, children.get(j).m_edgeType);
				initNodeDag.computeAllNodesArray();
			}
		}
	}

	private void stichChildren(Node crtNode, Node copyNode, boolean keepOut)
	{
		ArrayList<NodeEdgeRecord> children = copyNode.getChildren();
		for (int i = 0; i<children.size(); ++i)
		{
			NodeEdgeRecord crtRecord = children.get(i);

			Node newChild = null;
			if (crtRecord.m_node.isOut() && keepOut) newChild = m_out; //keep the original out
			else 
			{ 
				newChild = new Node(crtRecord.m_node.getLabel(), m_size, crtRecord.m_node.isMB());
				m_size ++;
			}
			crtNode.addChild(newChild, crtRecord.m_edgeType);
			stichChildren(newChild, crtRecord.m_node, keepOut);
		}
	}
	
	private void stichSubtree(Node crtNode, Node copyNode, int edgeType)
	{		
		Node newChild = new Node(copyNode.getLabel(), m_size, copyNode.isMB());
		m_size ++;

		crtNode.addChild(newChild, edgeType);
		stichChildren(newChild, copyNode, false);
	}
	
	

	private void stichPredicates(Node crtNode, Node copyNode)
	{
		ArrayList<NodeEdgeRecord> pchildren = copyNode.getChildren();
		for (int i = 0; i< pchildren.size(); ++i) if (!pchildren.get(i).m_node.isMB())
			stichSubtree(crtNode, pchildren.get(i).m_node, pchildren.get(i).m_edgeType);
	}
	
	public void addPredicateSubtree(Node crtNode, Node copyNode, int edgeType)
	{
		Node newChild = new Node(copyNode.getLabel(), m_size, false);
		m_size ++;
		crtNode.addChild(newChild, edgeType);
		stichChildren(newChild, copyNode, false);
		
		m_allNodesArray = new Node[m_size];
		computeAllNodesArray();
	}
	
	public void stichTree(DagPattern p)
	{	
		stichChildren(m_root, p.getRoot(), true);
		m_allNodesArray = new Node[m_size];
		m_mainBranchNodesList.clear();
		computeMainBranchNodesList();
		computeAllNodesArray();
	}

	

	public static DagPattern getCompensated(DagPattern toCopy, Node compNode)
	{
		DagPattern newDag = DagPattern.buildFromDag(toCopy);

		Node crtNode = newDag.m_out;
		Node crtCopyNode = compNode;

		while (!crtCopyNode.isOut())
		{
			ArrayList<NodeEdgeRecord> children = crtCopyNode.getMBChildren();	
			NodeEdgeRecord crtChild = children.get(0);
			Node newChild = new Node(crtChild.m_node.getLabel(), newDag.m_size, true);
			newDag.m_size++;
			crtNode.addChild(newChild, crtChild.m_edgeType);
			
			newDag.stichPredicates(crtNode, crtCopyNode);
			
			crtCopyNode = crtChild.m_node;
			crtNode = newChild;
		}

		ArrayList<NodeEdgeRecord> pchildren = crtCopyNode.getChildren();
		for (int i = 0; i< pchildren.size(); ++i) 
			newDag.stichSubtree(crtNode, pchildren.get(i).m_node, pchildren.get(i).m_edgeType);

		newDag.m_out.setOut(false);
		crtNode.setOut(true);
		newDag.m_out = crtNode;
		
		newDag.m_allNodesArray = new Node[newDag.m_size];
		newDag.computeMainBranchNodesList();
		newDag.computeAllNodesArray();

		return newDag;
	}
	
	public static DagPattern getExtendedSkeleton(DagPattern toCopy)
	{
		DagPattern newDag = buildFromDag(toCopy);
		newDag.pruneToExtendedSkeleton();
		return newDag;
	}
	

	private boolean pruneToExtendedSkeleton()
	{
		boolean hasPruned = false;
		for (int i = 0; i< m_mainBranchNodesList.size()-1; ++i)
		{
			ArrayList<NodeEdgeRecord> slashPath = getMaxMBSlashPath(new NodeEdgeRecord( m_mainBranchNodesList.get(i),0), false);
			if (pruneToExtendedSkeleton(m_mainBranchNodesList.get(i), slashPath, 0)) hasPruned = true;
		}
		computeMainBranchNodesList();
		computeAllNodesArray();
		
		return hasPruned;
	}
	
	boolean pruneToExtendedSkeleton(Node n, ArrayList<NodeEdgeRecord> slashPath, int npos)
	{
		if ((npos < slashPath.size()) && (!n.getLabel().equals(slashPath.get(npos).m_node.getLabel()))) return false;

		boolean hasPruned = false;
		
		ArrayList<NodeEdgeRecord> children = n.getChildren();
		ArrayList<NodeEdgeRecord> oldChildren = new ArrayList<NodeEdgeRecord>();
		
		for (int i = 0; i< children.size(); ++i)
			oldChildren.add(children.get(i));
				
		for (int i = 0; i< oldChildren.size(); ++i) 
		{
			NodeEdgeRecord crtRecord = oldChildren.get(i);
			if (crtRecord.m_node.isMB()) continue;
			if (crtRecord.m_edgeType == 1) 
			{
				hasPruned = true;
				removeNode(crtRecord.m_node);
			}
			
			else if (pruneToExtendedSkeleton(crtRecord.m_node, slashPath, npos+1)) 
				hasPruned = true;
		}
		
		return hasPruned;
	}
	
	
	private void removeNode(Node n)
	{
		ArrayList<NodeEdgeRecord> parents = n.getParents();
		for (int j=0; j<parents.size();++j) parents.get(j).m_node.removeChild(n, parents.get(j).m_edgeType);

		ArrayList<NodeEdgeRecord> children = n.getChildren();
		for (int j=0; j<children.size();++j) n.removeChild(children.get(j).m_node, children.get(j).m_edgeType);	
	}
	
	
	public void removeNodesInPath(ArrayList<NodeEdgeRecord> path)
	{
		for (int i = 0; i< path.size(); ++i)
			removeNode(path.get(i).m_node);

		computeMainBranchNodesList();
		computeAllNodesArray();
	}
	
	public void removeNodesInPathByFormerIndex(ArrayList<NodeEdgeRecord> path)
	{
		for (int i = 0; i< path.size(); ++i)
			removeNode(m_allNodesArray[path.get(i).m_node.m_formerIndex]);

		computeMainBranchNodesList();
		computeAllNodesArray();
	}

	public void collapseNodes(Node n1, Node n2)
	{		
		ArrayList<NodeEdgeRecord> childrenofn2 = n2.getChildren();
		for (int i=0; i<childrenofn2.size(); ++i)
			n1.addChild(childrenofn2.get(i).m_node, childrenofn2.get(i).m_edgeType);

		ArrayList<NodeEdgeRecord> parentsofn2 = n2.getParents();
		for (int i=0; i<parentsofn2.size(); ++i)
			parentsofn2.get(i).m_node.addChild(n1, parentsofn2.get(i).m_edgeType);
		
		removeNode(n2);

		computeMainBranchNodesList();
		computeAllNodesArray();
	}
	
	public void moveChildFrom(Node oldParent, Node newParent, Node child, int edgeType)
	{
		oldParent.removeChild(child, edgeType);
		newParent.addChild(child, edgeType);
		computeMainBranchNodesList();
	}

	public void replaceChild(Node parent, Node oldChild, Node newChild, int edgeType)
	{
		parent.removeChild(oldChild, edgeType);
		parent.addChild(newChild,  edgeType);
		computeMainBranchNodesList();
	}
	
	public void removeEdge(Node parent, Node child, int edgeType)
	{
		parent.removeChild(child,  edgeType);
		computeMainBranchNodesList();
		computeAllNodesArray();
	}

	public void reFitSize()
	{
		ArrayList<Node> topNodes = getAllNodesTopological();
		for (int i = 0; i< topNodes.size(); ++i)
		{
			topNodes.get(i).setIndex(i);
		}
		m_size = topNodes.size();
		computeAllNodesArray();
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*
	 * sets the seen mask to all nodes accessible downwards from the given node
	 */
	public void walkDagDownwards(Node n, boolean[] seen, boolean keepOnlyMB)
	{
		seen[n.getIndex()] = true;
		ArrayList<NodeEdgeRecord> children = n.getChildren();

		for (int i = 0; i< children.size(); ++i) 
			if (!seen[children.get(i).m_node.getIndex()] && (!keepOnlyMB || children.get(i).m_node.isMB()))
				walkDagDownwards(children.get(i).m_node, seen, keepOnlyMB);

	}
	

	/*
	 * sets the seen mask to all nodes accessible upwards from the given node
	 */
	public void walkDagUpwards(Node n, boolean[] seen)
	{
		seen[n.getIndex()] = true;
		ArrayList<NodeEdgeRecord> parents = n.getParents();

		for (int i = 0; i< parents.size(); ++i) 
			if (!seen[parents.get(i).m_node.getIndex()])
				walkDagUpwards(parents.get(i).m_node, seen);

	}
	
	public ArrayList<Node> getAllNodesTopological()
	{
		int[] countIncoming = new int[m_size];
		for (int i = 0; i<m_size; ++i) if (m_allNodesArray[i]!=null)
			countIncoming[i] = m_allNodesArray[i].getParents().size();
		
		ArrayList<Node> nodes = new ArrayList<Node>();
		nodes.add(m_root);
		int first = 0;
		int last = 0;
		//boolean[] seen = new boolean[m_size];
		//for (int i = 0; i<m_size; ++i) seen[i] = false;
		//seen[nodes.get(first).getIndex()] = true;
		
		while (first<=last)
		{
			ArrayList<NodeEdgeRecord> children = nodes.get(first).getChildren();
			for (int i = 0; i< children.size(); ++i) //if (!seen[children.get(i).m_node.getIndex()]) 
			{
				countIncoming[children.get(i).m_node.getIndex()]--;
				if (0 == countIncoming[children.get(i).m_node.getIndex()])
				{
					//seen[children.get(i).m_node.getIndex()] = true;
					nodes.add(children.get(i).m_node);
					last++;
				}
			}
			first++;
		}
		
		return nodes;
	}



	public boolean isTree()
	{
		for (int i = 0; i < m_mainBranchNodesList.size(); ++i)
			if (m_mainBranchNodesList.get(i).getCountMBChildren()>1 || m_mainBranchNodesList.get(i).getCountMBParents()>1)
				return false;
		return true;
	}
	
	public boolean isExtendedSkeleton()
	{
		DagPattern es = buildFromDag(this);
		return !es.pruneToExtendedSkeleton();	
	}
	
	public boolean hasPredicates()
	{
		for (int i = 0; i<m_size; ++i)
			if (m_allNodesArray[i] != null && !m_allNodesArray[i].isMB()) return true;
		return false;
	}

	public boolean isSkeleton()
	{
		DagPattern s = buildFromDag(this);

		for (int i = 0; i< s.m_mainBranchNodesList.size()-1; ++i)
		{
			Node crtNode = s.m_mainBranchNodesList.get(i);
			ArrayList<NodeEdgeRecord> slashPath = getMaxMBSlashPath(new NodeEdgeRecord(crtNode,0), false);
			ArrayList<NodeEdgeRecord> children = crtNode.getChildren();
			
			for (int j = 0; j< children.size(); ++j)
				if (!children.get(j).m_node.isMB() &&
					children.get(j).m_edgeType == 0 &&
					s.pruneToExtendedSkeleton(children.get(j).m_node, slashPath, 1))
				return false;
		}
		
		return true;
	}
	
	public boolean isAkin(DagPattern candidate)
	{
		ArrayList<NodeEdgeRecord> firstToken = getMaxMBSlashPath(new NodeEdgeRecord(m_root, 0), false);
		ArrayList<NodeEdgeRecord> candidateFirstToken = getMaxMBSlashPath(new NodeEdgeRecord(candidate.m_root, 0), false);
		return Services.areEqual(firstToken,  candidateFirstToken);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////

	
	public boolean isCorrect()
	{
		if (!m_root.isRoot()) return false;
		if (!m_out.isOut()) return false;
		
		if (m_out.getCountMBChildren() !=0) 
		{
			return false;
		}
		if (m_root.getCountMBParents() !=0) 
		{
			return false;
		}
		
		int countRoots = 0;
		int countOuts = 0;
		for (int i =0; i<m_size; ++i) if (null!= m_allNodesArray[i])
		{
			if (m_allNodesArray[i].isRoot()) countRoots++;
			if (m_allNodesArray[i].isOut()) countOuts++;
		}
		
		if (countRoots!=1 || countOuts!=1) 
		{
			return false;
		}
		
		boolean[] seen = new boolean[m_size];
		for (int i = 0; i< m_size; ++i)
			seen[i] = false;
		
		walkDagDownwards(m_root, seen, true);
		int countMBAccesibleByRoot = 0;
		for (int i = 0; i< m_size; ++i)
			if (seen[i]) countMBAccesibleByRoot++;
		
		if (countMBAccesibleByRoot!=m_mainBranchNodesList.size())
		{
			return false;
		}
		
		
		for (int i = 0; i< m_size; ++i)
			seen[i] = false;
		
		walkDagUpwards(m_out, seen);
		int countMBAccesibleByOut = 0;
		for (int i = 0; i< m_size; ++i)
			if (seen[i]) countMBAccesibleByOut++;
		
		if (countMBAccesibleByOut!=m_mainBranchNodesList.size())
		{
			return false;
		}
		
		
		for (int i = 0; i< m_size; ++i)
			seen[i] = false;
		walkDagDownwards(m_root, seen, false);
		int countNodesAccesibleByRoot = 0;
		for (int i = 0; i< m_size; ++i)
			if (seen[i]) countNodesAccesibleByRoot++;
		int countNonNullNodes = 0;
		for (int i = 0; i< m_size; ++i)
			if (m_allNodesArray[i]!=null) countNonNullNodes++;
		if (countNonNullNodes !=countNodesAccesibleByRoot) 
		{
			return false;
		}
		
		return true;
	}
	

	
	





	////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////



	public boolean treeHasMappingTo(DagPattern candidate, boolean root, boolean out)
	{	
		boolean[] restrict = new boolean[candidate.m_size];
		for (int i = 0; i< candidate.m_size; ++i) restrict[i] = true;

		boolean[] mappings = computeTreeMappingsOf(m_root, candidate, root, out, restrict);

		for (int i = 0; i< candidate.getSize(); ++i)
			if (mappings[i]) return true;
		return false;
	}


	public boolean treeHasMappingTo(DagPattern candidate, boolean[] restrictMBMap)
	{
		boolean[] mappings = computeTreeMappingsOf(m_root, candidate, false, false, restrictMBMap);

		for (int i = 0; i< candidate.getSize(); ++i)
			if (mappings[i]) return true;
		return false;
	}

	public boolean treeHasMappingTo(DagPattern candidate, ArrayList<NodeEdgeRecord> restrictMBPath)
	{
		boolean[] restrictMBMap = new boolean[candidate.getSize()];
		for (int i = 0; i< candidate.getSize(); ++i)
			restrictMBMap[i] = false;
		for (int  i = 0; i< restrictMBPath.size(); ++i)
			restrictMBMap[restrictMBPath.get(i).m_node.getIndex()] = true;

		boolean[] mappings = computeTreeMappingsOf(m_root, candidate, false, false, restrictMBMap);

		for (int i = 0; i< candidate.getSize(); ++i)
			if (mappings[i]) return true;
		return false;
	}

	
	private boolean[] computeTreeMappingsOf(Node node, DagPattern candidate, boolean root, boolean out, boolean[] restrictMBMap )
	{
		boolean[][] directMatrix = new boolean[this.m_size][candidate.getSize()];
		boolean[][] recMatrix = new boolean[this.m_size][candidate.getSize()];

		for (int i = 0; i < m_size; i++)
			for (int j = 0; j < candidate.getSize(); j++)
				directMatrix[i][j] = recMatrix[i][j] = false;

		
		ArrayList<Node> dagNodes = getAllNodesTopological();
		ArrayList<Node> candidateNodes = candidate.getAllNodesTopological();
		
		for (int i = dagNodes.size()-1; i>=0; i--)
			for (int j = candidateNodes.size()-1; j>=0; j--)
				dagNodes.get(i).mapCompute(candidateNodes.get(j), directMatrix, recMatrix, root, out, restrictMBMap);
		
		return directMatrix[node.getIndex()];
	}


	////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////


	public static boolean mapsToMerged(DagPattern p1, DagPattern p2)
	{
		ArrayList<Node> mbTree1 = p1.getMainBranchNodesList();
		ArrayList<Node> mbTree2 = p2.getMainBranchNodesList();

		for (int i = 0; i<mbTree1.size(); ++i)
		{
			//find if possible stich/merge position
			boolean canStich = true;
			for (int j = i; j<mbTree1.size() && canStich; ++j)
				if (!mbTree1.get(j).getLabel().equals(mbTree2.get(j-i).getLabel())) canStich = false;		
			if (!canStich) continue;

			//build merged tree
			DagPattern mergedTree = DagPattern.buildFromDag(p1);
			ArrayList<Node> mbMergedTree = mergedTree.getMainBranchNodesList();			
			for (int j = i; j<mbTree1.size() - 1; ++j)
				mergedTree.stichPredicates(mbMergedTree.get(j), mbTree2.get(j-i));		
			mergedTree.m_allNodesArray = new Node[mergedTree.m_size];
			mergedTree.computeAllNodesArray();
			mergedTree = getCompensated(mergedTree,mbTree2.get(mbMergedTree.size()-1-i));
			mergedTree.reFitSize();

			if (!p2.treeHasMappingTo(mergedTree, true, false))
				return false;

		}
		return true;
	}

	public static boolean areSimilar(DagPattern p1, DagPattern p2)
	{
		return mapsToMerged(p1, p2) && mapsToMerged(p2, p1);
	}




	////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////


	public ArrayList<Node> getMainBranchNodesList()
	{
		return m_mainBranchNodesList;
	}

	public Node[] getAllNodesArray()
	{
		return m_allNodesArray;
	}

	public Node getRoot()
	{
		return m_root;
	}

	public Node getOut()
	{
		return m_out;
	}

	public int getSize()
	{
		return m_size;
	}	


	////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////

	/*
	 * Gets the maximum "single branch" (one incoming & one outgoing edge) path
	 * starting from the given node (m_node of rec)
	 * 
	 * the "relaxOnLast" parameter allows checking only for incoming when downwards
	 * and outgoing when upwards
	 */
	public ArrayList<NodeEdgeRecord> getMaxOneEdgeMBPath(NodeEdgeRecord rec, boolean upwards, boolean relaxOnLast)
	{
		ArrayList<NodeEdgeRecord> path = new ArrayList<NodeEdgeRecord>();
		NodeEdgeRecord crtRec = rec;

		while (crtRec.m_node.getCountMBChildren() == 1 && crtRec.m_node.getCountMBParents() == 1)
		{
			path.add(crtRec);

			if (!upwards)
			{
				ArrayList<NodeEdgeRecord> children = crtRec.m_node.getChildren();
				int i = 0; for (; !children.get(i).m_node.isMB(); ++i);
				crtRec = children.get(i);
			}
			else
			{
				ArrayList<NodeEdgeRecord> parents = crtRec.m_node.getParents();
				int i = 0; for (; !parents.get(i).m_node.isMB(); ++i);
				crtRec = parents.get(i);
			}
		}

		if (relaxOnLast)
		{
			if (upwards && crtRec.m_node.getCountMBChildren() == 1) path.add(crtRec);
			if (!upwards && crtRec.m_node.getCountMBParents() == 1) path.add(crtRec);
		}

		return path;
	}

	public ArrayList<NodeEdgeRecord> getMaxMBSlashPath(NodeEdgeRecord rec, boolean upwards)
	{
		ArrayList<NodeEdgeRecord> path =  new ArrayList<NodeEdgeRecord>();
		NodeEdgeRecord crtRecord = rec;

		while (null !=crtRecord)
		{

			path.add(crtRecord);

			if (!upwards) crtRecord = crtRecord.m_node.getSlashMBChild();
			else crtRecord = crtRecord.m_node.getSlashMBParent();
		}	

		return path;
	}

	public ArrayList<NodeEdgeRecord> findMaxMatchMBSlashPath(NodeEdgeRecord rec, ArrayList<NodeEdgeRecord> toMatch, boolean upwards)
	{
		ArrayList<NodeEdgeRecord> newPath = getMaxMBSlashPath(rec, upwards);

		ArrayList<NodeEdgeRecord> retPath = new ArrayList<NodeEdgeRecord>();

		int limit = toMatch.size(); if (newPath.size() < limit) limit = newPath.size();
		for (int i = 0; i< limit; ++i)
		{
			if (newPath.get(i).m_node.getLabel().equals(toMatch.get(i).m_node.getLabel())) retPath.add(newPath.get(i));
			else break;
		}

		return retPath;
	}


	////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////


	/*
	 * This method checks if two MB nodes are collapsible using the article conditions
	 * That is, first checking matching labels
	 * then the possible immediate unsatisfiability of the dag resulting by collapsing these nodes
	 */
	public boolean areCollapsible(Node n1, Node n2)
	{
		if (!n1.getLabel().equals(n2.getLabel()))
			return false;
		
		NodeEdgeRecord rec1 = new NodeEdgeRecord(n1, 0);
		NodeEdgeRecord rec2 = new NodeEdgeRecord(n2, 0);
		
		ArrayList<NodeEdgeRecord> path1 = getMaxMBSlashPath(rec1, false);
		ArrayList<NodeEdgeRecord> path2 = getMaxMBSlashPath(rec2, false);
		
		if (!pathsAreCompatible(path1, path2)) return false;
		
		path1 = getMaxMBSlashPath(rec1, true);
		path2 = getMaxMBSlashPath(rec2, true);
		
		if (!pathsAreCompatible(path1, path2)) return false;
		
		return true;
	}
	
	private boolean pathsAreCompatible(ArrayList<NodeEdgeRecord> path1, ArrayList<NodeEdgeRecord> path2)
	{
		int size1 = path1.size();
		int size2 = path2.size();
		int minsize = size1;
		if (size2<minsize)
			minsize = size2;
		
		for (int i = 1; i<minsize; ++i)
			if (!path1.get(i).m_node.getLabel().equals(path2.get(i).m_node.getLabel()))
				return false;
		
		if (size1!= size2 && path1.get(path1.size()-1).m_node == path2.get(path2.size()-1).m_node)
			return false;
	
		return true;
	}

	public boolean pathExists(Node n1, Node n2)
	{
		boolean[] reachable =  new boolean[m_size];
		for (int i= 0 ; i< m_size; ++i) reachable[i] = false;
		walkDagDownwards(n1,reachable, false);
		return reachable[n2.getIndex()];
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////

	public ArrayList<Node> getAllSlashSubpredicates()
	{
		ArrayList<Node> subpredicates = new ArrayList<Node>();	
		for (int i = 0; i< m_mainBranchNodesList.size(); ++i) m_mainBranchNodesList.get(i).getSlashSubpredicates(subpredicates);
		return subpredicates;
	}



	

	
	
////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////

	boolean respectsExtendedSkeletons(ArrayList<NodeEdgeRecord> slashPath, Node predicate, int npos)
	{	
		if (npos >= slashPath.size()-1 || !predicate.getLabel().equals(slashPath.get(npos+1).m_node.getLabel())) return true;

		ArrayList<NodeEdgeRecord> children = predicate.getChildren();
		//if (children.size() == 0) return false; //we've matched till the end

		for (int i = 0; i< children.size(); ++i) //check predicate's children
		{
			if (children.get(i).m_edgeType == 1) return false; //we've reached a "//" edge while matching
			if (!respectsExtendedSkeletons(slashPath, children.get(i).m_node, npos+1)) return false;
		}

		return true;
	}
	
	/*
	 * Checks if there exists a mapping of path2 into path1 (from startpos to endpos, excluding ends)
	 * that does not provide necessary predicates for the presence of
	 * the argument predicate at the npos position
	 */
	boolean existsMappingInvalid(ArrayList<NodeEdgeRecord> path1, int startpos, int endpos, int npos, 
			Node predicate, ArrayList<NodeEdgeRecord> path2)
	{
		boolean[] existsMapInvalid = new boolean[path1.size()];
		for (int i = 0; i< path1.size(); ++i) existsMapInvalid[i] = true; //for the "-1" node

		for (int i = 0; i<path2.size(); ++i)
		{
			for (int j = endpos-1; j > startpos; --j) 
			{
				Node crtNodePath1 = path1.get(j).m_node;

				//we compute the existence of an invalid mapping between
				//path2[i - size path2] and path1[j - endpos-1]

				existsMapInvalid[j] = false; 

				if (!path2.get(i).m_node.getLabel().equals(crtNodePath1.getLabel())) continue;

				//check if the previous node of path 2
				//generates an invalid mapping into path1
				boolean previousExists = (i == 0 && j == startpos + 1);
				if (path2.get(i).m_edgeType == 1)
				{
					for (int k = startpos+1; k < j; ++k) if (existsMapInvalid[k]) previousExists = true;
				}
				else 
					if (j > (startpos +1) && existsMapInvalid[j-1]) previousExists = true;	

				if (!previousExists) continue;	//no invalid mapping for the previous path pieces
				if (lacksPredicates(path1, predicate, npos, path2.get(i).m_node, j, m_size)) existsMapInvalid[j] = true;	
			}
		}

		for (int j = startpos+1; j< endpos; ++j) if (existsMapInvalid[j]) return true; //path2 cand map invalid to a path1 segment

		return false;

	}

	/*
	 * Checks if by sticking to npos position in path1
	 * the predicate in argument
	 * and at stickPos path2Node's predicates
	 * we don't have a root mapping between pattern(npos-predicate) and the subdag from npos
	 */
	boolean lacksPredicates(ArrayList<NodeEdgeRecord> path1, Node predicate, int npos, Node path2Node, int stickPos, int size)
	{
		if (stickPos < npos) return true; //nothing new by sticking to this position

		//build subdag
		DagPattern subDag = DagPattern.buildFromDag(this, path1.get(npos).m_node, this.m_out, false);

		//stick predicates
		Node stickNode = subDag.m_allNodesArray[path1.get(stickPos).m_node.getIndex()];
		ArrayList<NodeEdgeRecord> children = path2Node.getChildren();
		for (int i = 0; i<children.size(); ++i) if (!children.get(i).m_node.isMB())
			subDag.addPredicateSubtree(stickNode, children.get(i).m_node, children.get(i).m_edgeType);

		DagPattern toMap = DagPattern.buildFromDag(this, path1.get(npos).m_node, path1.get(npos).m_node, true);
		toMap.addPredicateSubtree(toMap.m_root, predicate, 0);

		return !toMap.treeHasMappingTo(subDag, true, false);	
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////	
	public void reduce(RuleStatistics[] stats)
	{
		//first apply rule 1 until saturation
		boolean changed = true;
		while (changed)
		{
			long initTime = (new Date()).getTime();
			changed = Rule1Applier.applyRule1(this);
			stats[1].m_time += (new Date()).getTime() - initTime;
			stats[1].m_attempts++;
			if (changed) stats[1].m_applications++;
		}
		reFitSize();
		
		//then apply the other rules
		changed = true;
		while (changed)
		{
			//apply rule 2
			long initTime = (new Date()).getTime();
			changed = Rule2Applier.applyRule2(this);
			stats[2].m_time += (new Date()).getTime() - initTime;
			stats[2].m_attempts++;
			if (changed) 
			{
				stats[2].m_applications++;
				continue;
			}
			
			//apply rule 3
			initTime = (new Date()).getTime();
			changed = Rule3Applier.applyRule3(this);
			stats[3].m_time += (new Date()).getTime() - initTime;
			stats[3].m_attempts++;
			if (changed) 
			{
				stats[3].m_applications++;
				reFitSize();
				//rule 3 collapses nodes so we can apply rule 1 again
				while (changed)
				{
					initTime = (new Date()).getTime();
					changed = Rule1Applier.applyRule1(this);
					stats[1].m_time += (new Date()).getTime() - initTime;
					stats[1].m_attempts++;
					if (changed) stats[1].m_applications++;
				}
				reFitSize();
				changed = true;
				continue;
			}
			
			//apply rule 4
			initTime = (new Date()).getTime();
			changed = Rule4Applier.applyRule4(this);
			stats[4].m_time += (new Date()).getTime() - initTime;
			stats[4].m_attempts++;
			if (changed) 
			{
				reFitSize();
				stats[4].m_applications++;
				continue;
			}
			
			//apply rule 5
			initTime = (new Date()).getTime();
			changed = Rule5Applier.applyRule5(this);
			stats[5].m_time += (new Date()).getTime() - initTime;
			stats[5].m_attempts++;
			if (changed) 
			{
				stats[5].m_applications++;
				continue;
			}
			
			
			//apply rule 6
			initTime = (new Date()).getTime();
			changed = Rule6Applier.applyRule6(this);			
			stats[6].m_time += (new Date()).getTime() - initTime;
			stats[6].m_attempts++;
			if (changed) 
			{
				stats[6].m_applications++;
				reFitSize();
				//rule 6 collapses nodes so we can apply rule 1 again
				while (changed)
				{
					initTime = (new Date()).getTime();
					changed = Rule1Applier.applyRule1(this);
					stats[1].m_time += (new Date()).getTime() - initTime;
					stats[1].m_attempts++;
					if (changed) stats[1].m_applications++;
				}
				reFitSize();
				changed = true;
				continue;
			}
			
			
			//apply rule 7
			initTime = (new Date()).getTime();
			changed = Rule7Applier.applyRule7(this);
			stats[7].m_time += (new Date()).getTime() - initTime;
			stats[7].m_attempts++;
			if (changed) 
			{
				stats[7].m_applications++;
				reFitSize();
				continue;
			}
			
			
			//apply rule 8
			initTime = (new Date()).getTime();
			changed = Rule8Applier.applyRule8(this);
			stats[8].m_time += (new Date()).getTime() - initTime;
			stats[8].m_attempts++;
			if (changed) 
			{
				stats[8].m_applications++;
				//rule 8 collapses nodes so we can apply rule 1 again
				while (changed)
				{
					initTime = (new Date()).getTime();
					changed = Rule1Applier.applyRule1(this);
					stats[1].m_time += (new Date()).getTime() - initTime;
					stats[1].m_attempts++;
					if (changed) stats[1].m_applications++;
				}
				
				changed = true;
				continue;
			}
			
			
			//apply rule 9
			initTime = (new Date()).getTime();
			changed = Rule9Applier.applyRule9(this);
			stats[9].m_time += (new Date()).getTime() - initTime;
			stats[9].m_attempts++;
			if (changed) 
			{
				stats[9].m_applications++;
				continue;
			}
		}
	}

	public void printNodes(PrintStream stream)
	{

		for (int i=0; i<m_size;++i)
		{
			Node crtNode = m_allNodesArray[i];
			if (null == crtNode) continue;

			ArrayList<NodeEdgeRecord> children = crtNode.getChildren();
			for (int j = 0; j< children.size(); ++j)
			{
				NodeEdgeRecord crtRecord = children.get(j);
				//if (!crtRecord.m_node.isMB()) continue;

				stream.print(crtNode.getLabel() + "(" + crtNode.getIndex() + ") ");

				if (crtRecord.m_edgeType == 0) 
				{
					if (crtRecord.m_node.isMB()) stream.print("/ ");
					else stream.print("p ");
				}
				else 
				{
					if (crtRecord.m_node.isMB()) stream.print("// ");
					else stream.print("pp ");
				}

				stream.println(crtRecord.m_node.getLabel() + "(" + crtRecord.m_node.getIndex() + ") ");		
			}
		}
	}


	public String getQueryString(int start, int end)
	{
		if (!isCorrect())
		{
			System.out.println("dag not correct");
			System.exit(0);
		}
		String query = "";
		for (int i = start; i< end; ++i)
		{
			query += m_mainBranchNodesList.get(i).getLabel()+m_mainBranchNodesList.get(i).getPredicatesString();
			if (i< end-1)
			{
				NodeEdgeRecord ch = m_mainBranchNodesList.get(i).getMBChildren().get(0);
				if (ch.m_edgeType == 0) query+="/";
				else query+="//";
			}
		}

		return query;
	}

}
