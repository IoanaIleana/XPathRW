package Rewriting;

import java.util.ArrayList;

public class Rule5Applier 
{
	public static boolean applyRule5(DagPattern d)
	{
		ArrayList<Node> mainBranchNodesList = d.getMainBranchNodesList();

		for (int idx = 0; idx< mainBranchNodesList.size(); idx++)
		{
			Node crtNode = mainBranchNodesList.get(idx);
			ArrayList<Node> slashchildren = crtNode.getSlashMBChildren();
			ArrayList<Node> descchildren = crtNode.getDescMBChildren();
			for (int i =0; i< slashchildren.size(); ++i)
			{
				Node child1 = slashchildren.get(i);
				ArrayList<Node> path1 = new ArrayList<Node>();
				child1.getMaxMBSlashPathDownwards(path1);
				for (int j = 0; j< descchildren.size(); ++j)
				{
					Node child2 = descchildren.get(j);
					ArrayList<Node> path2 = new ArrayList<Node>();
					child2.getMaxMBSlashPathDownwards(path2);

					//find an n2 candidate
					for (int k = 0; k < path1.size() && k<path2.size(); ++k)
					{
						Node n2 = path1.get(k);
						Node n3 = path2.get(k);
						if (!n2.getLabel().equals(n3.getLabel()) ) break; //no more possible equivalence on paths
						if (!d.areCollapsible(n2, n3)) continue; //rule doesn't apply at k
						
						//build subdag starting from n2
						DagPattern spn2 = DagPattern.buildFromDag(d, n2, d.m_out, false);

						//find all candidate predicates
						ArrayList<NodeEdgeRecord> predicates = n3.getPredicateChildren(); //should they be slash only???
						for (int idxPred = 0; idxPred < predicates.size(); ++idxPred)
						{
							NodeEdgeRecord crtPredicate = predicates.get(idxPred);
	
							//build pattern(n2[pred])
							DagPattern n2pred = DagPattern.buildFromDag(d, n2, n2, true);
							n2pred.addPredicateSubtree(n2pred.m_root, crtPredicate.m_node, crtPredicate.m_edgeType);

							//test first mapping condition				
							if (n2pred.treeHasMappingTo(spn2, true, false)) continue; //rule not applicable

							//test second mapping condition
							boolean secCondCheck = true;
							for (int idxn4 = k; idxn4 < path1.size() && secCondCheck; idxn4++)
							{
								if (!d.areCollapsible(path1.get(idxn4), n3)) continue;
								DagPattern collapsed = DagPattern.buildFromDag(d);
								collapsed.collapseNodes(collapsed.getAllNodesArray()[path1.get(idxn4).getIndex()], collapsed.getAllNodesArray()[n3.getIndex()]);

								DagPattern spn2col = DagPattern.buildFromDag(collapsed, collapsed.getAllNodesArray()[n2.getIndex()]);
								if (!n2pred.treeHasMappingTo(spn2col, true, false)) secCondCheck = false;
							}
							if (!secCondCheck) continue;

							//test third mapping condition
							boolean existsNodeReachable = false;
							boolean[] reachable = new boolean[d.getSize()];
							for (int r = 0; r< d.getSize(); ++r)
								reachable[r] = false;
							d.walkDagDownwards(n3, reachable, true);
							for (int idxReach = k; idxReach < path1.size() && !existsNodeReachable; ++idxReach)
								if (reachable[path1.get(idxReach).getIndex()]) existsNodeReachable = true;

							if (!existsNodeReachable)
							{
								ArrayList<Node> secondHalfPath = new ArrayList<Node>();
								for (int g = k; g < path1.size(); ++g) secondHalfPath.add(path1.get(g));
								
								DagPattern tpp2 = DagPattern.treeFromPathWithPredicates(d, secondHalfPath);
								tpp2.addPredicateSubtree(tpp2.getOut(), crtPredicate.m_node, 1);

								if (!n2pred.treeHasMappingTo(tpp2, true, false)) continue;
							}

							//yay, all conditions cheeeeck!!! we can copy the predicate!!!
							d.addPredicateSubtree(n2, crtPredicate.m_node, crtPredicate.m_edgeType);
							//System.out.println("applied rule 5");
							return true;	
						}
					}
				}
			}
		}
		return false;
	}
}
