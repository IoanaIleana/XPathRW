package Rewriting;
import java.util.ArrayList;


public class Node {

	private ArrayList<NodeEdgeRecord> m_children;
	private ArrayList<NodeEdgeRecord> m_parents;
	
	private ArrayList<Node> m_slashMBChildren;
	private ArrayList<Node> m_slashMBParents;
	private ArrayList<Node> m_slashPredChildren;
	private ArrayList<Node> m_slashPredParents;
	
	private ArrayList<Node> m_descMBChildren;
	private ArrayList<Node> m_descMBParents;
	private ArrayList<Node> m_descPredChildren;
	private ArrayList<Node> m_descPredParents;

	private String m_label; //we append the "test" value to the "label" value!!!!
	private int m_index; //unique id
	public int m_formerIndex;

	private boolean m_isMB;
	private boolean m_isRoot;
	private boolean m_isOut;


	public Node(String label, int index, boolean isMB)
	{
		m_label = new String(label);
		m_index = index;
		m_formerIndex = index;
		m_isMB = isMB;

		m_isRoot = false;
		m_isOut = false;

		m_children = new ArrayList<NodeEdgeRecord>();
		m_parents = new ArrayList<NodeEdgeRecord>();
		
		m_slashMBChildren= new ArrayList<Node>();
		m_descMBChildren= new ArrayList<Node>();
		m_slashPredChildren= new ArrayList<Node>();
		m_descPredChildren= new ArrayList<Node>();
		
		m_slashMBParents= new ArrayList<Node>();
		m_descMBParents= new ArrayList<Node>();
		m_slashPredParents= new ArrayList<Node>();
		m_descPredParents= new ArrayList<Node>();
	}
	
	public Node(Node copy)
	{
		this(copy.getLabel(), copy.getIndex(), copy.isMB());
	}

	public void setLabel(String label)
	{
		m_label = new String(label);
	}
	
	public String getLabel()
	{
		return m_label;
	}
	
	
	public int getIndex()
	{
		return m_index;
	}
	
	public void setIndex(int index)
	{
		m_formerIndex = m_index;
		m_index = index;
	}

	public boolean isMB()
	{
		return m_isMB;
	}
	
	public void setAsPred()
	{
		m_isMB = false;
	}

	public void setOut(boolean out)
	{
		m_isOut = out;
	}
	
	public boolean isOut()
	{
		return m_isOut;
	}
	
	public void setRoot(boolean root)
	{
		m_isRoot = root;
	}
	
	public boolean isRoot()
	{
		return m_isRoot;
	}
	
	private void addSlashMBChild(Node node)
	{
		m_slashMBChildren.add(node);
		node.m_slashMBParents.add(this);
	}
	
	private void addDescMBChild(Node node)
	{	
		m_descMBChildren.add(node);
		node.m_descMBParents.add(this);
	}
	
	
	private void addSlashPredChild(Node node)
	{
		m_slashPredChildren.add(node);
		if (this.m_isMB)
			node.m_slashMBParents.add(this);
		else
			node.m_slashPredParents.add(this);
	}
	
	private void addDescPredChild(Node node)
	{
		m_descPredChildren.add(node);
		if (this.m_isMB)
			node.m_descMBParents.add(this);
		else
			node.m_descPredParents.add(this);
	}
	
	public void addChild(Node child, int edgeType)
	{
		for (int i =0; i< m_children.size(); ++i)
			if (m_children.get(i).m_node == child)
			{
				if (m_children.get(i).m_edgeType == 1 && edgeType == 0)
				{
					if (!child.isMB()) 
					{
						System.out.println("error");
						System.exit(0);
					}
					
					m_children.get(i).m_edgeType = 0;
					int k = 0;
					while (child.m_parents.get(k).m_node != this) k++;
					child.m_parents.get(k).m_edgeType = 0;
					
					removeDescMBChild(child);
					addSlashMBChild(child);
				}
				return; 
			}

		m_children.add(new NodeEdgeRecord(child, edgeType));
		child.m_parents.add(new NodeEdgeRecord(this, edgeType));
		
		if (child.isMB())
		{
			if (edgeType == 0) addSlashMBChild(child);
			else addDescMBChild(child);
		}
		else
		{
			if (edgeType == 0) addSlashPredChild(child);
			else addDescPredChild(child);
		}
	}
	
	private static ArrayList<Node> removeFromNodeList(Node node, ArrayList<Node> list)
	{
		ArrayList<Node> newList = new ArrayList<Node>();
		for (int i = 0; i< list.size(); ++i)
			if (list.get(i)!=node) newList.add(list.get(i));
		return newList;
	}
	
	private static ArrayList<NodeEdgeRecord> removeFromEdgeList(Node node, int edgeType, ArrayList<NodeEdgeRecord> list)
	{
		ArrayList<NodeEdgeRecord> newList = new ArrayList<NodeEdgeRecord>();
		for (int i = 0; i< list.size(); ++i)
			if (list.get(i).m_node!=node || list.get(i).m_edgeType!=edgeType) newList.add(list.get(i));
		return newList;
	}
	
	private void removeSlashMBChild(Node node)
	{	
		m_slashMBChildren = removeFromNodeList(node, m_slashMBChildren);
		node.m_slashMBParents = removeFromNodeList(this, node.m_slashMBParents);
	}
	
	private void removeDescMBChild(Node node)
	{
		m_descMBChildren = removeFromNodeList(node, m_descMBChildren);
		node.m_descMBParents = removeFromNodeList(this,  node.m_descMBParents);		
	}
	
	
	private void removeSlashPredChild(Node node)
	{
		m_slashPredChildren = removeFromNodeList(node, m_slashPredChildren);
		if (this.m_isMB)
			node.m_slashMBParents = removeFromNodeList(this, node.m_slashMBParents);
		else
			node.m_slashPredParents = removeFromNodeList(this, node.m_slashPredParents);
	}
	
	private void removeDescPredChild(Node node)
	{
		m_descPredChildren = removeFromNodeList(node, m_descPredChildren);
		if (this.m_isMB)
			node.m_descMBParents = removeFromNodeList(this, node.m_descMBParents);
		else
			node.m_descPredParents = removeFromNodeList(this, node.m_descPredParents);
	}
	
	public void removeChild(Node child, int edgeType)
	{
		m_children = removeFromEdgeList(child, edgeType, m_children);
		child.m_parents = removeFromEdgeList(this, edgeType, child.m_parents);
		if (child.isMB())
		{
			if (edgeType == 0) removeSlashMBChild(child);
			else removeDescMBChild(child);
		}
		else
		{
			if (edgeType == 0) removeSlashPredChild(child);
			else removeDescPredChild(child);
		}
	}
	
	
	public ArrayList<NodeEdgeRecord> getChildren()
	{
		return m_children;
	}
	
	public ArrayList<Node> getSlashPredChildren()
	{
		return m_slashPredChildren;
	}
	
	public ArrayList<Node> getSlashMBChildren()
	{
		return m_slashMBChildren;
	}
	
	public ArrayList<Node> getDescMBChildren()
	{
		return m_descMBChildren;
	}
	
	public ArrayList<Node> getDescPredChildren()
	{
		return m_descPredChildren;
	}
	
	public ArrayList<Node> getSlashMBParents()
	{
		return m_slashMBParents;
	}
	
	public ArrayList<Node> getDescMBParents()
	{
		return m_descMBParents;
	}
	
	public ArrayList<NodeEdgeRecord> getParents()
	{
		return m_parents;
	}
	
	int getCountMBChildren()
	{
		return (m_slashMBChildren.size()+m_descMBChildren.size());
	}
	
	int getCountMBParents()
	{
		return (m_slashMBParents.size()+m_descMBParents.size());
	}
	
	public ArrayList<NodeEdgeRecord> getMBChildren()
	{
		ArrayList<NodeEdgeRecord> children = new ArrayList<NodeEdgeRecord>();
		for (int i = 0; i< m_children.size(); ++i) if (m_children.get(i).m_node.isMB()) children.add(m_children.get(i));
		return children;
	}
	
	public ArrayList<NodeEdgeRecord> getMBParents()
	{
		ArrayList<NodeEdgeRecord> parents = new ArrayList<NodeEdgeRecord>();
		for (int i = 0; i< m_parents.size(); ++i) if (m_parents.get(i).m_node.isMB()) parents.add(m_parents.get(i));
		return parents;
	}
	
	public ArrayList<NodeEdgeRecord> getPredicateChildren()
	{
		ArrayList<NodeEdgeRecord> children = new ArrayList<NodeEdgeRecord>();
		for (int i = 0; i< m_children.size(); ++i) if (!m_children.get(i).m_node.isMB()) children.add(m_children.get(i));
		return children;
	}
	
	
	
	
	

	NodeEdgeRecord getSlashMBChild()
	{
		if (0 == m_slashMBChildren.size()) return null;
		return new NodeEdgeRecord(m_slashMBChildren.get(0), 0);
	}
	

	NodeEdgeRecord getSlashMBParent()
	{
		if (0 == m_slashMBParents.size()) return null;
		return new NodeEdgeRecord(m_slashMBParents.get(0), 0);
	}


	void getSlashSubpredicates(ArrayList<Node> predicates)
	{
		for (int i = 0; i< m_slashPredChildren.size(); ++i)
		{
			predicates.add(m_slashPredChildren.get(i));
			m_slashPredChildren.get(i).getSlashSubpredicates(predicates);
		}			
	}
	
	public String getPredicatesString()
	{
		String query = "";
		if (!m_isMB) query+=m_label;

		for (int i = 0; i< m_slashPredChildren.size(); ++i)
			query += "["+m_slashPredChildren.get(i).getPredicatesString()+"]";
		for (int i = 0; i< m_descPredChildren.size(); ++i)
			query += "[.//"+m_descPredChildren.get(i).getPredicatesString()+"]";				
		return query;
	}
	
	boolean isPredEq(Node predicate)
	{
		if (!m_label.equals(predicate.m_label) || m_children.size()!=predicate.m_children.size()) return false;
		
		boolean[] matched = new boolean[m_children.size()];
		for (int i = 0; i<m_children.size(); ++i) matched[i] = false;
		
		for (int i = 0; i< m_children.size(); ++i)
		{
			boolean found = false;
			for (int j = 0; j< predicate.m_children.size() && !found; ++j) if (!matched[j])
			{
				if (m_children.get(i).m_edgeType == predicate.m_children.get(j).m_edgeType &&
					m_children.get(i).m_node.isPredEq(predicate.m_children.get(j).m_node))
				{
					found = true;
					matched[j] = true;
				}
			}
			
			if (!found) return false;
		}
		return true;
	}
	
	
	boolean alreadyHasSlashPredicate(Node predicate)
	{
		for (int i = 0; i< m_slashPredChildren.size(); ++i)
			if (m_slashPredChildren.get(i).isPredEq(predicate))
				return true;
		return false;
	}

	
	public boolean getSingleEdgeSlashPath(ArrayList<Node> path)
	{
		Node crtNode = this;
		while (true)
		{
			if (crtNode.getCountMBParents()!=1) return false;
			path.add(crtNode);
			if (crtNode.m_slashMBChildren.size()==0) return true;
			if (crtNode.m_descMBChildren.size()!=0) return false;
			
			crtNode = crtNode.m_slashMBChildren.get(0);
		}		
	}
	
	public void getMaxMBSlashPathDownwards(ArrayList<Node> path)
	{
		Node crtNode = this;
		while (true)
		{
			path.add(crtNode);
			if (crtNode.m_slashMBChildren.size()==0) return ;
			crtNode = crtNode.m_slashMBChildren.get(0);
		}		
	}
	
	public boolean getMaxMBSlashPathDownwards_SingleIncoming(ArrayList<Node> path)
	{
		Node crtNode = this;
		while (true)
		{
			if (crtNode.getCountMBParents() > 1) return false;
			path.add(crtNode);
			if (crtNode.m_slashMBChildren.size()==0) return true;
			crtNode = crtNode.m_slashMBChildren.get(0);
		}		
	}
	
	public void getMaxMBSlashPathUpwards(ArrayList<Node> path)
	{
		Node crtNode = this;
		while (true)
		{
			path.add(crtNode);
			if (crtNode.m_slashMBParents.size()==0) return ;
			crtNode = crtNode.m_slashMBParents.get(0);
		}		
	}
	
	public boolean getMatchingSlashPath(ArrayList<Node> pathToMatch, ArrayList<Node> pathToFill)
	{
		Node crtNode = this;
		for (int i = 0; i<pathToMatch.size(); ++i)
		{
			if (!crtNode.getLabel().equals(pathToMatch.get(i).getLabel())) return false;
			pathToFill.add(crtNode);
			if (crtNode.m_slashMBChildren.size()==0) 
			{
				if (i == pathToMatch.size()-1) return true;
				else return false;
			}
			else crtNode = crtNode.m_slashMBChildren.get(0);
		}	
		return true;
	}
	
	public boolean getMatchingSlashPathUpwards(ArrayList<Node> pathToMatch, ArrayList<Node> pathToFill)
	{
		Node crtNode = this;
		for (int i = 0; i<pathToMatch.size(); ++i)
		{
			if (!crtNode.getLabel().equals(pathToMatch.get(i).getLabel())) return false;
			pathToFill.add(crtNode);
			if (crtNode.m_slashMBParents.size()==0) 
			{
				if (i == pathToMatch.size()-1) return true;
				else return false;
			}
			else crtNode = crtNode.m_slashMBParents.get(0);
		}	
		return true;
	}
	
	/*
	 * Non-reccursive method!
	 * Test (bottom-up) if a mapping exists between the subtrees rooted at this node and at the canidate node
	 * param root -> root mapping
	 * param out -> out mapping
	 */
	public void mapCompute(Node candidate, 
							boolean[][] directMapMatrix, 
							boolean[][] recMapMatrix, 
							boolean root, 
							boolean out, 
							boolean[] restrictMainBranchMapping)
	{

		int countChildren = this.m_children.size();
		int countCandidateChildren = candidate.m_children.size();
		
		
		//initial values for the recMapMatrix
		for (int i = 0; i < countCandidateChildren; ++i) if (recMapMatrix[m_index][candidate.m_children.get(i).m_node.m_index])
			recMapMatrix[m_index][candidate.m_index] = true;
		
		//test if this node can map to the candidate node
		if (!this.m_label.equals(candidate.m_label) 
			|| m_isMB && !candidate.m_isMB 
			|| (root && (this.m_isRoot && !candidate.m_isRoot)) //for root mappings
			|| (out && (this.m_isOut && !candidate.m_isOut)) //for out mappings
			|| (m_isMB && !restrictMainBranchMapping[candidate.getIndex()])) //for restricted mappings
		{
			return;
		}

		//test if all children map
		for (int i = 0; i< countChildren; ++i)
		{
			NodeEdgeRecord currentChildRecord = this.m_children.get(i);
			if (currentChildRecord.m_edgeType == 0) //direct child edge
			{
				boolean found = false;
				for (int j = 0; j < countCandidateChildren && !found; ++j)
				{
					NodeEdgeRecord candidateChildRecord = candidate.m_children.get(j);
					if (candidateChildRecord.m_edgeType == 0 && directMapMatrix[currentChildRecord.m_node.m_index][candidateChildRecord.m_node.m_index])
						found = true;
				}
				if (!found) return; //no mapping for one child -> no mapping
			}
			
			else //descendant edge
			{
				boolean found = false;
				for (int j = 0; j < countCandidateChildren && !found; ++j)
				{
					NodeEdgeRecord candidateChildRecord = candidate.m_children.get(j);
					if  (recMapMatrix[currentChildRecord.m_node.m_index][candidateChildRecord.m_node.m_index])
						found = true;
				}
				if (!found) return; //no mapping for one child -> no mapping
			}
		}
		directMapMatrix[this.m_index][candidate.m_index] = recMapMatrix[this.m_index][candidate.m_index] = true; 
	}
	
	
	
	
	
}
