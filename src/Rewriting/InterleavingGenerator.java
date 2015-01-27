package Rewriting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;



public class InterleavingGenerator {
	static long timeout = 600000;

	private int[] m_lastDifferent;
	private int[] m_position;
	private Node[] m_permNodes;
	private int[] m_edgeTypes;
	
	ArrayList<Node> m_mainBranchNodes;
	int m_countMBN;
	
	private long m_startTime;
	private long m_lastTime;
	
	private ArrayList<DagPattern> m_interleavings;
	
	
	public InterleavingGenerator(DagPattern d)
	{
		m_interleavings = new ArrayList<DagPattern>();
		
		m_position = new int[d.getSize()];
		m_permNodes = new Node[d.getSize()];
		m_lastDifferent = new int[d.getSize()];
		m_edgeTypes = new int[d.getSize()];
		
		for (int i=0; i<d.getSize(); i++) 
			m_position[i] = -1;
		
		m_mainBranchNodes = new ArrayList<Node>();
	    m_mainBranchNodes = d.getMainBranchNodesList();
		m_countMBN = m_mainBranchNodes.size();	

	}
	
	
	private boolean addNewInterleaving(String interleaving)
	{
		try
		{
			Parser parser = new Parser();
			DagPattern d = parser.parseQuery(interleaving);
			
			boolean foundContains = false;
			
			for (int i = 0; i<m_interleavings.size() && !foundContains; ++i)
			{
				long crtTime = (new Date()).getTime();
				if (crtTime > m_lastTime)
				{
					m_lastTime = crtTime;
					long delta = crtTime - m_startTime;
					if (delta%10000 == 0)
						System.out.println(delta);
					if (delta>timeout)
						return false;	
				}
	
				//if the new interleaving is contained in an existent one
				//System.out.println("before mapping");
				if (m_interleavings.get(i).treeHasMappingTo(d, true, true))
					foundContains = true;
				//System.out.println("after mapping");
			}
					
			if (foundContains)
				return true;
			
			//otherwise we will add the new interleaving and remove all the old ones that it contains
			
			ArrayList<DagPattern> newInterleavings = new ArrayList<DagPattern>();
			newInterleavings.add(d);
			
			for (int i = 0; i<m_interleavings.size(); ++i)
			{
				long crtTime = (new Date()).getTime();
				if (crtTime > m_lastTime)
				{
					m_lastTime = crtTime;
					long delta = crtTime - m_startTime;
					if (delta%10000 == 0)
						System.out.println(delta);
					if (delta>timeout)
						return false;	
				}
				
				//if the new interleaving does not contain the existent one keep the existent
				if (!d.treeHasMappingTo(m_interleavings.get(i), true, true))
					newInterleavings.add(m_interleavings.get(i));
			}
			
			m_interleavings = newInterleavings;
		}
		
		catch (IOException e) {return false;}
		return true;
		
	}
	
	private boolean generateCode(int nindex) // false=sortie par timeout
	{
		long crtTime = (new Date()).getTime();
		if (crtTime > m_lastTime)
		{
			m_lastTime = crtTime;
			long delta = crtTime - m_startTime;
			if (delta%10000 == 0)
				System.out.println(delta);
			if (delta>timeout)
				return false;	
		}

		if (nindex == m_countMBN)
		{
			String query = "";
			int i = m_countMBN -1;
			while (i >= 0 )
			{
				int edgeType = 1;
				query += m_permNodes[i].getLabel();
				//System.out.print(m_permNodes[i].getLabel()+"("+m_permNodes[i].getIndex()+") ");
				int last = m_lastDifferent[i];
				while (i > last) {
					if (m_edgeTypes[i] == 0) edgeType = 0;
					query += m_permNodes[i].getPredicatesString();
					i--;
				}
				
				if (i>=0)
				{
					if (edgeType == 0) query+="/";
					else query+="//";
				}
			}
		
			return addNewInterleaving(query);	
		}
		
		
		
		for (int i=0; i<m_countMBN; ++i) 
		{
			crtTime = (new Date()).getTime();
			if (crtTime > m_lastTime)
			{
				m_lastTime = crtTime;
				long delta = crtTime - m_startTime;
				if (delta%10000 == 0)
					System.out.println(delta);
				if (delta>timeout)
					return false;	
			}

			
			Node candidate = m_mainBranchNodes.get(i);
			if (m_position[candidate.getIndex()] != -1) continue;
			
			boolean validToAppend= true;
			boolean validToStick = true;
			int edgeType = 1;
			
			//check if children are all used
			ArrayList<NodeEdgeRecord> children = candidate.getChildren();
			for (int j=0; j< children.size(); ++j)
			{
				Node child = children.get(j).m_node;
				if (!child.isMB()) continue;
				
				int childPosition = m_position[child.getIndex()];
				
				if  (childPosition == -1) //child not yet added
				{
					validToAppend = false;
					validToStick = false;
					break;
				}
				if (children.get(j).m_edgeType == 0) //direct child
				{
					edgeType = 0;
					if (childPosition <=m_lastDifferent[nindex-1])
					{
						validToAppend = false;
						if (m_lastDifferent[nindex-1] == -1 || childPosition<=m_lastDifferent[m_lastDifferent[nindex-1]])
						{
							validToStick = false;
							break; //no need to continue, there is no valid config
						}
					}
				}
			}
			
			
			//try append
			if (validToAppend)
			{
				m_position[candidate.getIndex()] = nindex;
				m_permNodes[nindex] = candidate;
				m_edgeTypes[nindex] = edgeType;
				m_lastDifferent[nindex] = nindex - 1;
				if (false == generateCode(nindex+1)) return false;
				m_position[candidate.getIndex()] = -1;
			}
			
			//try stick
			if (validToStick && nindex > 0 && candidate.getLabel().equals(m_permNodes[nindex-1].getLabel()) && candidate.getIndex() > m_permNodes[nindex-1].getIndex())
			{
				m_position[candidate.getIndex()] = nindex;
				m_permNodes[nindex] = candidate;
				m_edgeTypes[nindex] = edgeType;
				m_lastDifferent[nindex] = m_lastDifferent[nindex-1];
				if (false == generateCode(nindex+1)) return false;
				m_position[candidate.getIndex()] = -1;
			}
		}
		
		return true;
	}
	
	public boolean getContainsAll(DagPattern zeInterleaving)
	{
		m_startTime = (new Date()).getTime();
		m_lastTime = m_startTime;
		
		if (false == generateCode(0))
			return false;
		
		System.out.println(m_interleavings.size());
		for (int i = 0; i< m_interleavings.size(); ++i)
			System.out.println(m_interleavings.get(i).getQueryString(0, m_interleavings.get(i).getMainBranchNodesList().size()));

		if (m_interleavings.size()!=1)
			zeInterleaving = null;
		else
			zeInterleaving = m_interleavings.get(0);
		
		return true;
	}

}
