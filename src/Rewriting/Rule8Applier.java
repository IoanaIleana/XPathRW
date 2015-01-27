package Rewriting;

import java.util.ArrayList;


public class Rule8Applier {
	public static boolean applyRule8(DagPattern d)
	{
		ArrayList<Node> mainBranchNodesList = d.getMainBranchNodesList();
		for (int idx = 0; idx< mainBranchNodesList.size(); idx++)
		{
			Node crtNode = mainBranchNodesList.get(idx);
			ArrayList<NodeEdgeRecord> path1 = d.getMaxMBSlashPath(new NodeEdgeRecord(crtNode, 0), false);
			if (path1.size()<=2) continue;
					
			boolean[] isOnPath = new boolean[d.getSize()];
			for (int i = 0; i< d.getSize(); ++i) isOnPath[i] = false;
			for (int i = 0; i < path1.size(); ++i) isOnPath[path1.get(i).m_node.getIndex()] = true;
			
			ArrayList<Node> descchildren = crtNode.getDescMBChildren();
			for (int j = 0; j< descchildren.size(); ++j)
			{
				ArrayList<NodeEdgeRecord> path2 = d.getMaxOneEdgeMBPath(new NodeEdgeRecord(descchildren.get(j), 1), false, false);
				if (path2.size() == 0) continue;
				Node lastp2Node = path2.get(path2.size()-1).m_node;
				ArrayList<Node> desc = lastp2Node.getDescMBChildren();
				if (0 == desc.size()) continue;
				
				if (!isOnPath[desc.get(0).getIndex()]) continue;
				
				boolean[][] topDownMappings = new boolean[path2.size()][path1.size()];
				boolean[][] topDownRecMappings = new boolean[path2.size()][path1.size()];
				
				for (int p = 0; p<path2.size(); ++p)
					for (int q = 0; q<path1.size(); ++q)
						topDownMappings[p][q] = topDownRecMappings[p][q] = false;
				
				for (int p = 0; p<path2.size(); ++p)
					for (int q = 1; q<path1.size()-1; q++)
					{
						if (topDownRecMappings[p][q-1]) topDownRecMappings[p][q] = true;
						if (!path2.get(p).m_node.getLabel().equals(path1.get(q).m_node.getLabel())) continue;
						
						if (p>0 && path2.get(p).m_edgeType == 0 && !topDownMappings[p-1][q-1]) continue;
						if (p>0 && path2.get(p).m_edgeType == 1 && !topDownRecMappings[p-1][q-1]) continue;
						
						topDownMappings[p][q] = topDownRecMappings[p][q] = true;
					}
			
				boolean[][] bottomUpMappings = new boolean[path2.size()][path1.size()];
				boolean[][] bottomUpRecMappings = new boolean[path2.size()][path1.size()];
				
				for (int p = 0; p<path2.size(); ++p)
					for (int q = 0; q<path1.size(); ++q)
						bottomUpMappings[p][q] = bottomUpRecMappings[p][q] = false;
				
				for (int p = path2.size()-1; p>=0; p--)
					for (int q = path1.size()-2; q>=1; q--)
					{
						if (bottomUpRecMappings[p][q+1]) bottomUpRecMappings[p][q] = true;
						if (!path2.get(p).m_node.getLabel().equals(path1.get(q).m_node.getLabel())) continue;
						
						if (p<path2.size()-1 && path2.get(p+1).m_edgeType == 0 && !bottomUpMappings[p+1][q+1]) continue;
						if (p<path2.size()-1 && path2.get(p+1).m_edgeType == 1 && !bottomUpRecMappings[p+1][q+1]) continue;
						
						bottomUpMappings[p][q] = bottomUpRecMappings[p][q] = true;
					}
				
				for (int p = 0; p<path2.size(); ++p)
				{
					int count = 0;
					for (int q = 0; q<path1.size() && count<2; ++q)
						if (topDownMappings[p][q] && bottomUpMappings[p][q]) count++;
					if (count == 1)
					{
						for (int q = 0; q<path1.size(); ++q)
							if (topDownMappings[p][q])
							{
								d.collapseNodes(path1.get(q).m_node, path2.get(p).m_node);
								return true;
							}
					}
				}
			}
		}
		
		return false;
	}
}
