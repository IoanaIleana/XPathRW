package Rewriting;

import java.util.ArrayList;


public class Rule2Applier {
	public static boolean applyRule2(DagPattern d)
	{
		ArrayList<Node> mainBranchNodesList = d.getMainBranchNodesList();
		for (int idx = 0; idx< mainBranchNodesList.size(); idx++)
		{
			Node crtNode = mainBranchNodesList.get(idx);
			
			//Rule 2.i
			ArrayList<Node> slashChildren = crtNode.getSlashMBChildren();
			ArrayList<Node> descChildren = crtNode.getDescMBChildren();
			for (int i =0; i< slashChildren.size(); ++i)
			{
				Node child1 = slashChildren.get(i);
				for (int j = 0; j< descChildren.size(); ++j)
				{
					Node child2 = descChildren.get(j);		
					if (!d.areCollapsible(child1, child2) && !d.pathExists(child1, child2))
					{
						d.moveChildFrom(crtNode, child1, child2, 1);
						return true;
					}
				}
			}
			
			
			//Rule 2.ii
			ArrayList<Node> slashParents = crtNode.getSlashMBParents();
			ArrayList<Node> descParents = crtNode.getDescMBParents();
			for (int i =0; i< slashParents.size(); ++i)
			{
				Node parent1 = slashParents.get(i);
				for (int j = 0; j< descParents.size(); ++j)
				{
					Node parent2 = descParents.get(j);
					if (!d.areCollapsible(parent1, parent2) && !d.pathExists(parent2, parent1))
					{
						d.replaceChild(parent2, crtNode, parent1, 1);
						return true;
					}
				}
			}
		
		}
		return false;
	}
}
