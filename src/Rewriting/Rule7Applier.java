package Rewriting;

import java.util.ArrayList;

public class Rule7Applier {
	public static boolean applyRule7(DagPattern d)
	{
		NodeEdgeRecord recpath2 = new NodeEdgeRecord(null, 1);
		
		ArrayList<Node> mainBranchNodesList = d.getMainBranchNodesList();
		for (int idx = 0; idx< mainBranchNodesList.size(); idx++)
		{
			Node mbn = mainBranchNodesList.get(idx);
			if (mbn.getCountMBChildren()<2 || mbn.getDescMBChildren().size() == 0) continue;	
			
			DagPattern spn1 = DagPattern.buildFromDag(d, mbn);
			spn1.reFitSize();
			Node n1 = spn1.getRoot();
			ArrayList<Node> descchildren = n1.getDescMBChildren();
			
			for (int i = 0; i< descchildren.size(); ++i) 
			{
				Node n2 = descchildren.get(i);
				recpath2.m_node = n2;
				ArrayList<NodeEdgeRecord> path2 = spn1.getMaxOneEdgeMBPath(recpath2, false, false);
				if (path2.size() == 0) //special case 
				{
					DagPattern newDag = DagPattern.buildFromDag(spn1);
					Node newn1 = newDag.getAllNodesArray()[n1.getIndex()];
					Node newn2 = newDag.getAllNodesArray()[n2.getIndex()];
					newDag.removeEdge(newn1, newn2, 1);
					if (newDag.pathExists(newn1, newn2))
					{
						d.removeEdge(d.getAllNodesArray()[n1.m_formerIndex], d.getAllNodesArray()[n2.m_formerIndex], 1);
						return true;
					}
				}

				ArrayList<NodeEdgeRecord> children = path2.get(path2.size()-1).m_node.getMBChildren();
				if (children.get(0).m_edgeType == 0) continue;
				n2 = children.get(0).m_node;
			
				//mark allowed nodes for mapping of p2 nodes
				
				//first detach subdag between n1 and n2 
				boolean[] allowed = new boolean[spn1.getSize()]; 
				for (int a = 0; a< spn1.getSize(); ++a) allowed[a] = false;			
				spn1.walkDagDownwards(n1, allowed, true);					
				
				boolean[] tmp = new boolean[spn1.getSize()]; 
				for (int a = 0; a< spn1.getSize(); ++a) tmp[a] = false;
				spn1.walkDagUpwards(n2,  tmp);		
				for (int a = 0; a < spn1.getSize(); ++a) if (!tmp[a]) allowed[a] = false;
				
				//then remove additional etremities and path2 nodes
				allowed[n1.getIndex()] = false;
				allowed[n2.getIndex()] = false;
				for (int a = 0; a< path2.size(); ++a) allowed[path2.get(a).m_node.getIndex()] = false;

				DagPattern tdp2 = DagPattern.buildFromPath(spn1,path2, false);
				tdp2.reFitSize();
				if (tdp2.treeHasMappingTo(spn1, allowed))
				{
					d.removeNodesInPathByFormerIndex(path2);
					return true;
				}
			}
		}
		
		return false;
	}
}
