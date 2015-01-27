package Rewriting;

import java.util.ArrayList;


public class Rule4Applier {
	public static boolean applyRule4(DagPattern d)
	{
		ArrayList<Node> mainBranchNodesList = d.getMainBranchNodesList();

		for (int idx = 0; idx< mainBranchNodesList.size(); idx++)
		{
			Node crtNode = mainBranchNodesList.get(idx);

			//Rule 4.i
			ArrayList<NodeEdgeRecord> children = crtNode.getChildren();
			for (int i = 0; i< children.size(); ++i)
			{
				NodeEdgeRecord rec1 = children.get(i);
				if (rec1.m_edgeType == 1 || !rec1.m_node.isMB()) continue;
				for (int j = 0; j< children.size(); ++j)
				{
					NodeEdgeRecord rec2 = children.get(j);
					if (rec2.m_edgeType == 0 || !rec2.m_node.isMB()) continue;

					ArrayList<NodeEdgeRecord> maxpath2 = d.getMaxOneEdgeMBPath(rec2, false, true); //downwards, relax cond on last
					if (0 == maxpath2.size()) continue;

					ArrayList<NodeEdgeRecord> maxpath1 = d.getMaxMBSlashPath(rec1, false); //downwards, no restriction
					if (0 == maxpath1.size()) continue;

					DagPattern spn1 = DagPattern.buildFromDag(d, rec1.m_node);

					//try all positions of path2
					ArrayList<NodeEdgeRecord> path2 = new ArrayList<NodeEdgeRecord>();
					for (int endpath2 = 0; endpath2<maxpath2.size(); endpath2 ++) 
					{	
						path2.add(maxpath2.get(endpath2));
						if (maxpath2.get(endpath2).m_node.getSlashMBChild() != null) continue;
						
						DagPattern tpn2 = DagPattern.buildFromPath(d, path2, false);

						//try all positions for the end of path1
						ArrayList<NodeEdgeRecord> path1 = new ArrayList<NodeEdgeRecord>();
						for (int endpath1 = 0; endpath1<maxpath1.size(); endpath1++)
						{
							path1.add(maxpath1.get(endpath1));
							if (!tpn2.treeHasMappingTo(spn1, path1)) continue;


							//try all "n4" nodes 
							boolean valid = true;
							int lengthp2 = path2.size();
							DagPattern tpath1 = DagPattern.buildFromPath(d,path1, true);

							ArrayList<NodeEdgeRecord> childrenOfN3 = path2.get(lengthp2-1).m_node.getMBChildren();
							for (int k = 0; k< childrenOfN3.size() && valid; ++k)
							{
								NodeEdgeRecord childRec = childrenOfN3.get(k);
								if (!childRec.m_node.isMB() || childRec.m_edgeType!=1) continue;

								path2.add(childRec);
								DagPattern tpath2 = DagPattern.buildFromPath(d, path2, true);

								if (tpath2.treeHasMappingTo(tpath1, false, false)) valid = false;
								path2.remove(lengthp2);			
							}

							if (valid) //we found p1!!! yaaaay!!!
							{
								for (int k = 0; k< childrenOfN3.size(); ++k)
									path1.get(path1.size()-1).m_node.addChild(childrenOfN3.get(k).m_node, 1);
								d.removeNodesInPath(path2);

								//System.out.println("applied rule 4");
								return true;
							}

						}
					}
				}
			}



			//Rule 4.ii
			ArrayList<NodeEdgeRecord> parents = crtNode.getParents();
			for (int i = 0; i< parents.size(); ++i)
			{
				NodeEdgeRecord rec1 = parents.get(i);
				if (rec1.m_edgeType == 1 || !rec1.m_node.isMB()) continue;
				for (int j = 0; j< parents.size(); ++j)
				{
					NodeEdgeRecord rec2 = parents.get(j);
					if (rec2.m_edgeType == 0 || !rec2.m_node.isMB()) continue;

					ArrayList<NodeEdgeRecord> maxpath2 = d.getMaxOneEdgeMBPath(rec2, true, true); //upwards, relax cond on last
					if (0 == maxpath2.size()) continue;

					ArrayList<NodeEdgeRecord> maxpath1 = d.getMaxMBSlashPath(rec1, true); //upwards, no restriction
					if (0 == maxpath1.size()) continue;

					//try all positions of path2
					ArrayList<NodeEdgeRecord> path2 = new ArrayList<NodeEdgeRecord>();
					for (int endpath2 = 0; endpath2<maxpath2.size(); endpath2 ++)
					{
						path2.add(maxpath2.get(endpath2));
						if (maxpath2.get(endpath2).m_node.getSlashMBParent() != null) continue;

						DagPattern tpn2 = DagPattern.buildFromPath(d, path2, false);

						//try all positions for the end of path1
						ArrayList<NodeEdgeRecord> path1 = new ArrayList<NodeEdgeRecord>();
						for (int endpath1 = 0; endpath1<maxpath1.size(); endpath1++)
						{
							path1.add(maxpath1.get(endpath1));
							DagPattern tp1 = DagPattern.buildFromPath(d, Services.reverse(path1), false);
							if (!tpn2.treeHasMappingTo(tp1, path1)) continue;

							//try all "n4" nodes 
							boolean valid = true;
							int lengthp2 = path2.size();
							DagPattern tpath1 = DagPattern.buildFromPath(d,Services.reverse(path1), true);

							ArrayList<NodeEdgeRecord> parentsOfN3 = path2.get(lengthp2-1).m_node.getMBParents();
							for (int k = 0; k< parentsOfN3.size() && valid; ++k)
							{
								NodeEdgeRecord parentRec = parentsOfN3.get(k);
								if (!parentRec.m_node.isMB() || parentRec.m_edgeType!=1) continue;

								path2.add(parentRec);
								DagPattern tpath2 = DagPattern.buildFromPath(d, Services.reverse(path2), true);

								if (tpath2.treeHasMappingTo(tpath1, false, false)) valid = false;
								path2.remove(lengthp2);			
							}

							if (valid) //we found p1!!! yaaaay!!!
							{
								for (int k = 0; k< parentsOfN3.size(); ++k)
									parentsOfN3.get(k).m_node.addChild(path1.get(path1.size()-1).m_node, 1);
								d.removeNodesInPath(path2);

								//System.out.println("applied rule 4");
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
