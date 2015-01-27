package Rewriting;

import java.util.ArrayList;

public class Rule9Applier {
	public static boolean applyRule9(DagPattern d)
	{
		ArrayList<Node> mainBranchNodesList = d.getMainBranchNodesList();
		ArrayList<Node> subPredicates = d.getAllSlashSubpredicates();
		

		for (int idx = 0; idx< mainBranchNodesList.size(); idx++)
		{
			Node crtNode = mainBranchNodesList.get(idx);
			
			//get a maximal slash path
			if (null!= crtNode.getSlashMBParent()) continue;				
			ArrayList<NodeEdgeRecord> path1 = d.getMaxMBSlashPath(new NodeEdgeRecord(crtNode,0), false);
			
			int[] pathMask = new int[d.getSize()];
			for (int i = 0; i< d.getSize(); ++i) pathMask[i] = -1;
			for (int i = 0; i<path1.size(); ++i) pathMask[path1.get(i).m_node.getIndex()] = i;
			
			
			for (int i = 0; i< subPredicates.size(); ++i)
			{
				Node predicate = subPredicates.get(i);
				
				//mark all positions where the predicate respects the ES condition
				//and is not already there!
				boolean[] valid = new boolean[path1.size()];
				for (int j = 0; j< path1.size(); ++j) 
					if (!path1.get(j).m_node.isOut())
						valid[j] = d.respectsExtendedSkeletons(path1, predicate, j) && !path1.get(j).m_node.alreadyHasSlashPredicate(predicate);
					else 
						valid[j] = !path1.get(j).m_node.alreadyHasSlashPredicate(predicate);
				
				for (int startpos = 0; startpos<path1.size(); ++startpos) //possible start position for the second path
				{
					Node path2parent = path1.get(startpos).m_node;
					ArrayList<NodeEdgeRecord> children = path2parent.getChildren();
					
					for (int j = 0; j< children.size(); ++j)
					{
						//find a path2 canidate
						if (children.get(j).m_edgeType !=1 || !children.get(j).m_node.isMB()) continue;					
						ArrayList<NodeEdgeRecord> path2 = d.getMaxOneEdgeMBPath(children.get(j), false, false);
						if (path2.size() == 0 || path2.get(path2.size()-1).m_node.getMBChildren().get(0).m_edgeType == 0) continue; //not a valid path2
						Node path2child = path2.get(path2.size()-1).m_node.getMBChildren().get(0).m_node;
						int endpos = pathMask[path2child.getIndex()];
						if (endpos == -1) continue; //the child is not on path1
						
						for (int npos = startpos; npos <= endpos; ++npos) if (valid[npos])
						{
							if (!d.existsMappingInvalid(path1, startpos, endpos, npos, predicate, path2))
							{
								d.addPredicateSubtree(path1.get(npos).m_node, predicate, 0);
								//System.out.println("applied rule 9");
								return true;
							}	
						}
					}
				}
			}
		}
		return false;
	}
}