package Generator;

import java.io.File;
import java.util.Date;
import java.util.ArrayList;

import Rewriting.DagPattern;
import Rewriting.Parser;

import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.WhitespaceStrippingPolicy;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.s9api.XdmValue;

public class InputQueryGenerator 
{
	public static void generateInputQueries(
			ArrayList<String> queryStrings,
			ArrayList<DagPattern> queryDags,
			String xmlFileName,
			String queryType, 
			int countQueries,
			int queryLength) throws Exception
	{
		Processor proc = new Processor(false);
		XPathCompiler xpath = proc.newXPathCompiler();
		xpath.declareNamespace("saxon", "http://saxon.sf.net/"); 

		DocumentBuilder builder = proc.newDocumentBuilder();
		builder.setLineNumbering(true);
		builder.setWhitespaceStrippingPolicy(WhitespaceStrippingPolicy.ALL);
		XdmNode rootDoc = builder.build(new File(xmlFileName));
		
		XPathSelector selector = xpath.compile(".").load();
		selector.setContextItem(rootDoc);
		
		XdmValue roots = selector.evaluate();
		XdmNode firstNode = (XdmNode)roots.itemAt(0);
		XdmSequenceIterator iter = firstNode.axisIterator(Axis.CHILD);
		firstNode = (XdmNode)iter.next();
		
		int documentDepth = getDepth(firstNode);
		Parser parser = new Parser();
		
		Date inittimer = new Date();
		
		boolean allowDescendentEdgesInMainBranch = true;
		if (queryType.equals("slashOnlyQueries")) 
			allowDescendentEdgesInMainBranch = false;
		boolean allowPredicates = true;
		if (queryType.equals("slashOnlyQueries") || queryType.equals("noPredicatesQueries")) 
			allowPredicates = false;
		
		boolean allowFirstPredDescendent = true;
		if (queryType.equals("extendedSkeletonsQueries"))
			allowFirstPredDescendent = false;
		
		
		
		
		for (int i = 0; i<countQueries; ++i)
		{
			boolean queryIsValid = false;	
			while (!queryIsValid)
			{
				if ((new Date()).getTime() - inittimer.getTime() >600000) return; //we exit by timeout, with as many queries as possible
			
				ArrayList<XdmNode> nodes =  new ArrayList<XdmNode>();
				ArrayList<Integer> edgeTypes = new ArrayList<Integer>();
				nodes.add(firstNode);
				edgeTypes.add(Integer.valueOf(0));				
				String queryString = ""; 
				
				if (false == generateInputQueryMainBranch(nodes, edgeTypes, firstNode, queryLength, documentDepth, allowDescendentEdgesInMainBranch))
					continue; //we haven't reached the correct length
				
				ArrayList<XdmNode> slashPrefix = new ArrayList<XdmNode>(); 	
				for (int idxNode = nodes.size()-1; idxNode>=0; idxNode--)
				{
					String partQuery = "";
					if (idxNode > 0)
					{
						if (edgeTypes.get(idxNode).intValue() == 0) partQuery = "/";
						else partQuery = "//";
					}	
					partQuery += nodes.get(idxNode).getNodeName();
					
					if (idxNode<nodes.size()-1 && edgeTypes.get(idxNode+1) != 0) slashPrefix.clear();
					slashPrefix.add(0, nodes.get(idxNode));

					if (allowPredicates)
					{
						boolean allowDescendentPredicates = true;
						if ((queryType.equals("extendedSkeletonsQueries") || queryType.equals("skeletonsQueries")) && idxNode< nodes.size()-1 )
							allowDescendentPredicates = false;
						partQuery+=generatePredicates(nodes.get(idxNode), slashPrefix, 0, false, allowFirstPredDescendent, allowDescendentPredicates);
					}

					queryString = partQuery+queryString;
				}
				
				DagPattern queryDag = parser.parseQuery(queryString);
				//queryDag.pruneRedundantPredicates();
				
				if (queryType.equals("extendedSkeletonsQueries") && !queryDag.isExtendedSkeleton())
				{
					System.out.println("error: not an extended skeleton");
					return;
				}
				else if (queryType.equals("skeletonsQueries") && !queryDag.isSkeleton())
				{
					System.out.println("error: not a skeleton");
					return;
				}
				
				
				if (queryType.equals("skeletonsQueries") && queryDag.isExtendedSkeleton()) continue;
				if (queryType.equals("nonSkeletonsQueries") && (queryDag.isExtendedSkeleton() || queryDag.isSkeleton())) continue;
				
				queryIsValid = true;
				
				for (int idxQuery = 0; idxQuery< queryDags.size() && queryIsValid; ++idxQuery)
				{
					if (queryDag.treeHasMappingTo(queryDags.get(idxQuery), true, true)
						&& queryDags.get(idxQuery).treeHasMappingTo(queryDag, true, true))
							queryIsValid = false;
				}
				
				if (queryIsValid)
				{
					queryDags.add(queryDag);
					queryStrings.add(queryString);
				}
				
				
			}
		}
	}
								
		
	
	public static boolean generateInputQueryMainBranch(
			ArrayList<XdmNode> nodes,
			ArrayList<Integer> edgeTypes,
			XdmNode crtNode, 
			int queryLength,
			int docDepth,
			boolean allowDescendentEdges) throws Exception
	{	
		int crtDocumentDepth = 0;
		int crtQueryLength = 1;
			
		boolean lastSkipped = false;
			
		while (true)
		{
			crtDocumentDepth++;
				
			if (crtQueryLength == queryLength) 
				return true;
				
			//select a child
			XdmNode child = null;		
			XdmSequenceIterator iter = crtNode.axisIterator(Axis.CHILD);
			int countChildren = 0;
			while (iter.hasNext())
			{
				if (null!=(child=(XdmNode)iter.next()).getNodeName()) 
					countChildren++;
			}
			if (0==countChildren) 
				return false; //we haven't reached the proper length and no more children are available
				
			int index =(int)( Math.random()*(countChildren)); //from 0 to countChildren-1
				
			iter = crtNode.axisIterator(Axis.CHILD);
			int crtCount = 0;
			while (iter.hasNext())
			{
				if (null!=(child=(XdmNode)iter.next()).getNodeName()) 
				{
					if (crtCount == index) break;
					crtCount++;
				}
			}
							
			//we've selected a child
			//see if we skip it or not
			if (allowDescendentEdges && (queryLength-crtQueryLength<docDepth-crtDocumentDepth) && Math.random()<0.33)
			{
				lastSkipped = true;
				crtNode = child;
				continue;
			}
				
				
			//we will not skip it and create an edge
			nodes.add(child);
			crtQueryLength++;
			if (lastSkipped || (allowDescendentEdges && Math.random()>0.5)) 
				edgeTypes.add(Integer.valueOf(1));
			else
				edgeTypes.add(Integer.valueOf(0));
			crtNode = child;        
		}
	}
	
	
	public static String generatePredicates(XdmNode crtNode, ArrayList<XdmNode> slashPrefix, int crtPosInSlashPrefix, boolean lastSkipped, boolean firstCanBeNonSlash, boolean allowDescendentPredicates)
    {
		if (crtPosInSlashPrefix >3) return "";
		
    	String query="";
		XdmNode child = null;
		XdmSequenceIterator iter = crtNode.axisIterator(Axis.CHILD);
		
		int countPaths = 0;
		while (iter.hasNext() && countPaths<3) 
		{
			if (null==(child=(XdmNode)iter.next()).getNodeName() || Math.random() < 0.5) 
				continue; //no predicate on this child
			
			countPaths++;
			
			boolean nextCanBeDescendant = (allowDescendentPredicates || (firstCanBeNonSlash && crtPosInSlashPrefix == 0));

			//we've selected a child
			//see if we skip it or not
			if (nextCanBeDescendant && Math.random()>0.5)
			{
				query+=generatePredicates(child, slashPrefix, crtPosInSlashPrefix+1, true, firstCanBeNonSlash, true);
				continue;
			}

			//we will not skip it and create an edge
			if (lastSkipped || (nextCanBeDescendant && Math.random()>0.5)) 
				query +="[.//"+child.getNodeName() + generatePredicates(child, slashPrefix, crtPosInSlashPrefix+1, false, firstCanBeNonSlash, true) +"]";
			else
			{
				if (crtPosInSlashPrefix < slashPrefix.size() && !crtNode.getNodeName().equals(slashPrefix.get(crtPosInSlashPrefix).getNodeName()))
					query +="["+child.getNodeName() + generatePredicates(child, slashPrefix, crtPosInSlashPrefix+1, false, firstCanBeNonSlash, true) +"]";
				else query +="["+child.getNodeName() + generatePredicates(child, slashPrefix, crtPosInSlashPrefix+1, false, firstCanBeNonSlash, allowDescendentPredicates) +"]";
			}
		}
		return query;
    }


	

    
    public static int getDepth(XdmNode crtNode) throws Exception
    {
    	int depth = 0;
    	XdmNode child = null;
    	
    	XdmSequenceIterator	iter = crtNode.axisIterator(Axis.CHILD);
		while (iter.hasNext() ) 
		{
			child=(XdmNode)iter.next();
			if (null == child.getNodeName()) continue;
				
			int childDepth = getDepth(child);
			if (childDepth > depth)
				depth = childDepth; 
		}
		
		return depth + 1;
    }
    
    public static int getDocumentDepth(String xmlInputFileName) throws Exception
    {
		Processor proc = new Processor(false);
		XPathCompiler xpath = proc.newXPathCompiler();
		xpath.declareNamespace("saxon", "http://saxon.sf.net/"); // not actually used, just for demonstration

		DocumentBuilder builder = proc.newDocumentBuilder();
		builder.setLineNumbering(true);
		builder.setWhitespaceStrippingPolicy(WhitespaceStrippingPolicy.ALL);
		XdmNode rootDoc = builder.build(new File(xmlInputFileName));
		
		XPathSelector selector = xpath.compile(".").load();
		selector.setContextItem(rootDoc);
		
		XdmValue roots = selector.evaluate();
		XdmNode crtNode = (XdmNode)roots.itemAt(0);
		XdmSequenceIterator iter = crtNode.axisIterator(Axis.CHILD);
		crtNode = (XdmNode)iter.next();
		
		return getDepth(crtNode);	
    }
    
    
}

