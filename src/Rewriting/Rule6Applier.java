package Rewriting;

import java.util.ArrayList;

public class Rule6Applier {
	public static boolean applyRule6(DagPattern d)
	{
		ArrayList<Node> mainBranchNodesList = d.getMainBranchNodesList();
		for (int idx = 0; idx< mainBranchNodesList.size(); idx++)
		{
			Node crtNode = mainBranchNodesList.get(idx);
			ArrayList<Node> descChildren = crtNode.getDescMBChildren();
			
			for (int i =0; i< descChildren.size(); ++i)
			{
				Node n1 = descChildren.get(i);
				ArrayList<Node> path1 = new ArrayList<Node>();
				if (!n1.getSingleEdgeSlashPath(path1) || path1.size()==1) continue;
				
				for (int j = i+1; j< descChildren.size(); ++j)
				{
					Node n2 = descChildren.get(j);
					ArrayList<Node> path2 = new ArrayList<Node>();
					if (!n2.getSingleEdgeSlashPath(path2) || path2.size()==1 || path2.size()!=path1.size()) continue;
					
					boolean ok=true;
					for (int k = 0; k<path1.size() && ok; ++k)
						if (!path1.get(k).getLabel().equals(path2.get(k).getLabel())) ok = false;
					if (!ok) continue;
					
					DagPattern tree1 = DagPattern.treeFromPathWithPredicates(d, path1);
					tree1.reFitSize();
					
					DagPattern tree2 = DagPattern.treeFromPathWithPredicates(d, path2);		
					tree2.reFitSize();
					
					if (!DagPattern.areSimilar(tree1, tree2)) continue;
					
					d.collapseNodes(n1, n2);
					//System.out.println("applied rule 6");
					return true;
				}
			}
		}
		return false;
	}
}


