package Rewriting;

import java.util.ArrayList;


public class Rule3Applier {
	public static boolean applyRule3(DagPattern d)
	{
		ArrayList<Node> mainBranchNodesList = d.getMainBranchNodesList();

		for (int idx = 0; idx< mainBranchNodesList.size(); idx++)
		{
			Node crtNode = mainBranchNodesList.get(idx);
				
			//Rule 3.i
			ArrayList<Node> descchildren = crtNode.getDescMBChildren();	
			ArrayList<Node> slashchildren = crtNode.getSlashMBChildren();
			
			for (int i =0; i< descchildren.size(); ++i)
			{
				Node n2 = descchildren.get(i);
				ArrayList<Node> path2 = new ArrayList<Node>();
				n2.getMaxMBSlashPathDownwards(path2);
				if (!Services.isSingleIncoming(path2)) continue;
				
				DagPattern tree2 = DagPattern.treeFromPathWithPredicates(d,path2);
				
				for (int j = 0; j< slashchildren.size(); ++j)
				{
					Node n1 = slashchildren.get(j);
					ArrayList<Node> path1 = new ArrayList<Node>();
					if (!n1.getMatchingSlashPath(path2, path1)) continue;
					
					DagPattern tree1 = DagPattern.treeFromPathWithPredicates(d,path1);
		
					if (tree2.treeHasMappingTo(tree1, true, false)) //root mapping
					{
						d.collapseNodes(n1, n2);
						return true;
					}
				}
			}
		
		
			//Rule 3.ii
			ArrayList<NodeEdgeRecord> parents = crtNode.getParents();
			for (int i =0; i< parents.size(); ++i)
			{
				NodeEdgeRecord rec1 = parents.get(i);
				if (rec1.m_edgeType == 1 || !rec1.m_node.isMB()) continue;
				for (int j = 0; j< parents.size(); ++j)
				{
					NodeEdgeRecord rec2 = parents.get(j);
					if (rec2.m_edgeType == 0 || !rec2.m_node.isMB()) continue;

					ArrayList<NodeEdgeRecord> path2 = d.getMaxMBSlashPath(rec2, true); //upwards, one outgoing
					if (!Services.isSingleBranch(path2, false, true, false)) continue;
					if (0 == path2.size()) continue;
					
					ArrayList<NodeEdgeRecord> path1 = d.findMaxMatchMBSlashPath(rec1, path2, true); //upwards
					if (path1.size() != path2.size()) continue;
					
					DagPattern tree1 = DagPattern.buildFromPath(d,Services.reverse(path1), false);
					DagPattern tree2 = DagPattern.buildFromPath(d,Services.reverse(path2), false);


					if (tree2.treeHasMappingTo(tree1, true, false)) //root mapping
					{
						d.collapseNodes(rec1.m_node, rec2.m_node);
						//System.out.println("applied rule 3");
						return true;
					}
				}
			}

		}
		return false;
	}
}
