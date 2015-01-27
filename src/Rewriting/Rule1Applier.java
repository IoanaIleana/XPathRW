package Rewriting;

import java.util.ArrayList;

public class Rule1Applier {
	public static boolean applyRule1(DagPattern d)
	{
		ArrayList<Node> mainBranchNodesList = d.getMainBranchNodesList();

		for (int idx = 0; idx< mainBranchNodesList.size(); idx++)
		{
			Node crtNode = mainBranchNodesList.get(idx);
			
			//Rule 1.i
			ArrayList<Node> slashchildren = crtNode.getSlashMBChildren();	
			for (int i =0; i< slashchildren.size(); ++i)
			{
				Node child1 = slashchildren.get(i);
				for (int j = i+1; j< slashchildren.size(); ++j)
				{			
					Node child2 = slashchildren.get(j);
					if (child1.getLabel().equals(child2.getLabel())) 
					{
						d.collapseNodes(child1, child2);
						return true;
					}
				}
			}
			
			
			//Rule 1.ii
			ArrayList<Node> slashparents = crtNode.getSlashMBParents();
			for (int i =0; i< slashparents.size(); ++i)
			{
				Node parent1 = slashparents.get(i);
				for (int j = i+1; j< slashparents.size(); ++j)
				{
					Node parent2 = slashparents.get(j);			
					if (parent1.getLabel().equals(parent2.getLabel())) 
					{
						d.collapseNodes(parent1, parent2);
						return true;
					}
				}
			}
		}
		
		return false;
	}
}
