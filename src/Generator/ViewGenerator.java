package Generator;

import java.util.ArrayList;
import java.util.Date;

import Rewriting.DagPattern;
import Rewriting.Node;
import Rewriting.NodeEdgeRecord;


public class ViewGenerator {
	
	private static final String forbiddenLabel="xxx";
	private static int index = 0;
	
	/* 
	 * copies copyNode's predicate children to the crtNode
	 */
	public static void stichPredicates(Node crtNode, Node copyNode)
	{
		ArrayList<NodeEdgeRecord> children = copyNode.getChildren();
		for (int i = 0; i<children.size(); ++i)
		{
			NodeEdgeRecord crtRecord = children.get(i);	
			if (crtRecord.m_node.isMB()) continue;
			
			Node newChild = new Node(crtRecord.m_node.getLabel(), index, false);
			index++;
			crtNode.addChild(newChild, crtRecord.m_edgeType);
			
			stichPredicates(newChild, crtRecord.m_node);
		}
	}
	
	
	public static DagPattern generateMappedView(
			DagPattern inputQuery,
			double probaSelectToken,
			double probaCopyPredicates,
			double probaChangeChildToDescendant,
			double probaSkipFromChild,
			double probaSkipFromDescendant,
			double probaSkipAfterSkip)
	{
		ArrayList<Node> mainBranchNodes = inputQuery.getMainBranchNodesList();
		int mainBranchLength = mainBranchNodes.size();
		
		int startLastToken = mainBranchLength;
		int startIntermediarySection = inputQuery.getMaxMBSlashPath(new NodeEdgeRecord(mainBranchNodes.get(0), 0), false).size();
		if (startIntermediarySection < mainBranchLength)
		{
			startLastToken = mainBranchLength-inputQuery.getMaxMBSlashPath(new NodeEdgeRecord(mainBranchNodes.get(mainBranchNodes.size()-1), 0), false).size();
		}
		
		int[] keepSameNodes = new int[mainBranchLength]; //0 keep with predicates, 1 keep node with incoming edge, 2 whatever
		for (int i = 0; i<mainBranchLength; ++i)
			keepSameNodes[i] = 2;
		
		//first token
		if (Math.random()<probaSelectToken)
		{
			for (int i = 0; i<startIntermediarySection; ++i)
				keepSameNodes[i] = 1; //just keep nodes
		}
		else keepSameNodes[0] = 1; //just the first node
		
		//intermediary
		if (Math.random()<probaSelectToken)
		{
			for (int i = startIntermediarySection; i<startLastToken; ++i)
				keepSameNodes[i] = 0; //keep nodes and predicates
		}
		
		//last token
		if (Math.random()<probaSelectToken)
		{
			for (int i = startLastToken; i<mainBranchLength; ++i)
				keepSameNodes[i] = 1; //just keep nodes
		}
		else keepSameNodes[mainBranchNodes.size()-1] = 1; //just the last node
		

		index = 0;
		Node newRoot = new Node(mainBranchNodes.get(0).getLabel(), index, true);
		index++;
		if (Math.random() < probaCopyPredicates)
			stichPredicates(newRoot, mainBranchNodes.get(0));
		newRoot.setRoot(true);
		
		Node crtNode = newRoot;
		boolean lastSkipped = false;
		
		for (int i=1;i<mainBranchLength; ++i)
		{
			if (keepSameNodes[i]<2)
			{
				Node newNode = new Node(mainBranchNodes.get(i).getLabel(), index, true);
				index++;
				
				int edgeType = mainBranchNodes.get(i).getMBParents().get(0).m_edgeType;
				if (lastSkipped) edgeType = 2;
				crtNode.addChild(newNode, edgeType);
				
				if (keepSameNodes[i] == 0 || (Math.random()<probaCopyPredicates))
					stichPredicates(newNode,mainBranchNodes.get(i));
				crtNode = newNode;
				lastSkipped= false;
				continue;
			}
			
			if (lastSkipped)
			{
				if (Math.random()<probaSkipAfterSkip) continue;
				
				Node newNode = new Node(mainBranchNodes.get(i).getLabel(), index, true);
				index++;
				crtNode.addChild(newNode, 1); //descendant
				
				if (Math.random()<probaCopyPredicates)
					stichPredicates(newNode,mainBranchNodes.get(i));
				crtNode = newNode;
				lastSkipped = false;
				continue;
			}
			
			int edgeType = mainBranchNodes.get(i).getMBParents().get(0).m_edgeType;
			if ((edgeType == 0 && Math.random()<probaSkipFromChild)
				|| (edgeType == 1 && Math.random()<probaSkipFromDescendant))
			{
				lastSkipped = true;
				continue;
			}
			
			if (edgeType == 0 && (Math.random()<probaChangeChildToDescendant))
				edgeType = 1;
			Node newNode = new Node(mainBranchNodes.get(i).getLabel(), index, true);
			index++;
			crtNode.addChild(newNode, edgeType); 
			lastSkipped = false;
			if (Math.random()<probaCopyPredicates)
				stichPredicates(newNode,mainBranchNodes.get(i));
			
			crtNode = newNode;
		}
		
	
		crtNode.setOut(true);
		return new DagPattern (newRoot, crtNode, index);
	
	}
	
	
	
	
	/* 
	 * generate countQueries distinct queries 
	 * mapping in random lossless prefixes of the
	 * input query
	 */
	public static ArrayList<String> generateMappedViews(
			DagPattern inputQuery,
			int countQueries)
	{
		int timeout = 600000;
		Date init=new Date();
		
		ArrayList<String> queries = new ArrayList<String>();
		ArrayList<DagPattern> queryDags = new ArrayList<DagPattern>();
		
		ArrayList<Node> inputQueryNodes = inputQuery.getMainBranchNodesList();
		
		
		for (int i = 0; i< countQueries; ++i)
		{
			
			boolean ok = false;
			while(!ok)
			{
				if ((new Date()).getTime() - init.getTime() > timeout) return null;
				
				double probaSelectToken = 0.5;
				double probaCopyPredicates = 0.5;
				double probaChangeChildToDescendant = 0.2;
				double probaSkipFromChild = 0.2;
				double probaSkipFromDescendant = 0.2;
				double probaSkipAfterSkip = 0.2;
				
				
				/*
				double probaSelectToken = 2*1/((double)countQueries); //0.5;
				double probaCopyPredicates = 2*1/((double)countQueries); //0.5;
				double probaChangeChildToDescendant = (double)countQueries / 512; //0.2;
				double probaSkipFromChild = (double)countQueries / 512; //0.2;
				double probaSkipFromDescendant = (double)countQueries / 512; //0.2;
				double probaSkipAfterSkip = (double)countQueries / 512;//0.2;
				*/
				
				DagPattern mappedView = generateMappedView(inputQuery, probaSelectToken, probaCopyPredicates,probaChangeChildToDescendant, probaSkipFromChild, probaSkipFromDescendant, probaSkipAfterSkip);

				String viewQueryString =  mappedView.getQueryString(0, mappedView.getMainBranchNodesList().size());
								
				if (!mappedView.treeHasMappingTo(inputQuery, true, false))
				{
					System.out.println("this view doesn't map");
					System.exit(0);
				}
				

				Node lastViewNode = mappedView.getMainBranchNodesList().get(mappedView.getMainBranchNodesList().size()-1);
				ok = true;
				for (int idxNode = 0; idxNode<inputQueryNodes.size()&&ok; ++idxNode) if (inputQueryNodes.get(idxNode).getLabel().equals(lastViewNode.getLabel()))
				{
					DagPattern compView = DagPattern.getCompensated(mappedView, inputQueryNodes.get(idxNode) );
					if (compView.treeHasMappingTo(inputQuery,  true, true) && inputQuery.treeHasMappingTo(compView,  true, true))
					{
						//System.out.println("single rewriting");
						ok = false;
						break;
					}
				}
				
				if (!ok) continue;
				
				for (int j = 0; j< queries.size() && ok; ++j)
					if (mappedView.treeHasMappingTo(queryDags.get(j), true, true) && queryDags.get(j).treeHasMappingTo(mappedView, true, true)) 
						ok = false;
				

				
				if (ok) 
				{
					//System.out.println(viewQueryString);
					queries.add(viewQueryString);
					queryDags.add(mappedView);
				}
			}
		}
		
		return queries;
	}
	
	/* 
	 * generate countQueries distinct queries 
	 * mapping in random lossless prefixes of the
	 * input query; then alters at least one main branch label
	 * to get a forbidden label
	 */
	public static ArrayList<String> generateIncompatibleViews(
			DagPattern inputQuery,
			int countQueries)
	{		
		ArrayList<String> queries = new ArrayList<String>();
		
		for (int i = 0; i< countQueries; ++i)
		{

			DagPattern mappedView = generateMappedView(inputQuery,  0.5, 0.5, 0.5, 0.5, 0.5, 0.5);
				
			//alter this view
			int k = 1+(int)(Math.random()*mappedView.getMainBranchNodesList().size());
			for (int j = 0; j<k; ++j)
			{
				int index = (int)(Math.random()*mappedView.getMainBranchNodesList().size());
				mappedView.getMainBranchNodesList().get(index).setLabel(forbiddenLabel);
			}
			String viewQueryString =  mappedView.getQueryString(0, mappedView.getMainBranchNodesList().size());
			queries.add(viewQueryString);

		}
		
		return queries;
	}
}
	
	
