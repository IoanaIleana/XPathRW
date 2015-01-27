package Rewriting;

import java.util.ArrayList;


public class Services {

	
	public static ArrayList<NodeEdgeRecord> reverse(ArrayList<NodeEdgeRecord> path)
	{
		ArrayList<NodeEdgeRecord> revList = new ArrayList<NodeEdgeRecord>();
		for (int i=path.size()-1; i>=0; i--)
			revList.add(path.get(i));
		
		return revList;
	}
	
	public static boolean areEqual(ArrayList<NodeEdgeRecord> path1, ArrayList<NodeEdgeRecord> path2)
	{
		if (path1.size() != path2.size()) return false;
		
		for (int i = 0; i < path1.size(); ++ i)
			if (!path1.get(i).m_node.getLabel().equals(path2.get(i).m_node.getLabel())) return false;
		
		return true;
	}
	
	public static boolean isSingleBranch(ArrayList<NodeEdgeRecord> path, boolean oneIncoming, boolean oneOutgoing, boolean relaxOnLast)
	{
		int i = 0;
		for (i= 0; i< path.size()-2; ++i)
		{
			if (oneIncoming && path.get(i).m_node.getCountMBParents() >=2) return false;
			if (oneOutgoing && path.get(i).m_node.getCountMBChildren() >=2) return false;
		}
		if (oneIncoming && path.get(i).m_node.getCountMBParents() >=2) return false;
		if (oneOutgoing && !relaxOnLast && path.get(i).m_node.getCountMBChildren() >=2) return false;
		
		return true;
	}
	
	public static boolean isSingleIncoming(ArrayList<Node> path)
	{
		for (int i = 0; i<path.size(); ++i)
			if (path.get(i).getCountMBParents() >1) return false;
		return true;
	}
	
	public static boolean isSingleOutgoing(ArrayList<Node> path)
	{
		for (int i = 0; i<path.size(); ++i)
			if (path.get(i).getCountMBChildren() >1) return false;
		return true;
	}
}
