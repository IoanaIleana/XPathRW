package Generator;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.s9api.XdmValue;
import Rewriting.DagPattern;
import Rewriting.Parser;

public class InputValidator {
	
	public static boolean validateInputQueryFile(String filePrefix, String queryType, int queryLength) throws Exception
	{
		String fileName = "rewrite_tests/"+filePrefix+".xml.tests/"+queryType+queryLength+".in";
		
		FileInputStream fstream = new FileInputStream(fileName);	
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		
		Processor proc = new Processor(false);
		XQueryCompiler compiler = proc.newXQueryCompiler();
		Parser parser = new Parser();
		
		ArrayList<DagPattern> queryDags = new ArrayList<DagPattern>();
		
		for (int i = 0; i<10; ++i)
		{
			String query = br.readLine();
			if (null == query)
			{
				System.out.println("not enough queries");
				return false;
			}
			
			//check that query selects somth in document
			String inputXQuery = "for $var0 in doc(\""+"xml_docs/"+filePrefix+"_uid.xml"+"\")/"+query+" return $var0";
			XQueryEvaluator evaluator = compiler.compile(inputXQuery).load();
			XdmValue inputXQueryEval = evaluator.evaluate();
			
			if (0 == inputXQueryEval.size())
			{
				System.out.println("query selects nothing "+query);
				return false;
			}
			
			DagPattern queryDag = parser.parseQuery(query);
			if (queryDag.getMainBranchNodesList().size() != queryLength)
			{
				System.out.println("query does not have the proper length "+query);
				return false;
			}
			
			if (queryType.equals("extendedSkeletonsQueries") && !queryDag.isExtendedSkeleton()) 
			{
				System.out.println("wrong extended skeleton query "+query);
				return false;
			}
			if (queryType.equals("skeletonsQueries") && (!queryDag.isSkeleton() || queryDag.isExtendedSkeleton())) 
			{
				System.out.println("wrong skeleton and non extended skeleton query "+query);
				return false;
			}
			if (queryType.equals("nonSkeletonsQueries") && queryDag.isSkeleton()) 
			{
				System.out.println("wrong non skeleton query  "+query);
				return false;
			}
			
			queryDags.add(queryDag);
		}
		
		for (int i = 0; i<10; ++i)
			for (int j = i+1; j<10; ++j)
				if (queryDags.get(i).treeHasMappingTo(queryDags.get(j), true, true) &&
					queryDags.get(j).treeHasMappingTo(queryDags.get(i), true, true))
				{
					System.out.println("queries "+i+" and "+j+" are equivalent");
					return false;
				}
			
		
		br.close();
		
		return true;
	}
	
	
	public static boolean validateInputRewriteFile(String filePrefix, String queryType, int queryLength, int viewCount, int queryIndex) throws Exception
	{
		
		QName uid = new QName("uid");
		
		
		String base="rewrite_tests/"+filePrefix+".xml.tests/"+queryType+"/inputs/input_";
		String fileName = base+queryLength+"_"+viewCount+"_"+queryIndex;
		
		FileInputStream fstream = new FileInputStream(fileName);	
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		
		Processor proc = new Processor(false);
		XQueryCompiler compiler = proc.newXQueryCompiler();
		Parser parser = new Parser();
		
		String query =  br.readLine();
		DagPattern queryDag = parser.parseQuery(query);
		
		DagPattern[] prefixes = new DagPattern[queryDag.getMainBranchNodesList().size()];
		for (int i = 0; i< queryDag.getMainBranchNodesList().size(); ++i)
			prefixes[i] = DagPattern.getLosslessPrefix(queryDag, i+1);
		
		 
		String inputXQuery = "for $var0 in doc(\""+"xml_docs/"+filePrefix+"_uid.xml"+"\")/"+query+" return $var0";
		XQueryEvaluator evaluator = compiler.compile(inputXQuery).load();
		XdmValue inputXQueryEval = evaluator.evaluate();
		
		br.readLine();

		ArrayList<String> mappedViews = new ArrayList<String>();
		ArrayList<DagPattern> mappedViewDags = new ArrayList<DagPattern>();
		
		//ArrayList<String> incViews = new ArrayList<String>();
		//ArrayList<DagPattern> incViewDags = new ArrayList<DagPattern>();
		
		for (int i = 0; i< viewCount; ++i)
		{
			String view = br.readLine();
			if (view == null)
			{
				System.out.println("missing mapped view");
				return false;
			}
			if (view.equals(query))
			{
				System.out.println("view is same as query");
				return false;
			}
			
			String viewXQuery = "for $var0 in doc(\""+"xml_docs/"+filePrefix+"_uid.xml"+"\")/"+view+" return $var0";
			evaluator = compiler.compile(viewXQuery).load();
			XdmValue viewXQueryEval = evaluator.evaluate();
			
			for (int idxInput = 0; idxInput<inputXQueryEval.size(); ++idxInput)
			{
				XdmNode inputQueryNode = (XdmNode)(inputXQueryEval.itemAt(idxInput));
				boolean found = false;
				
				for (int idxView = 0; idxView<viewXQueryEval.size() && !found; idxView++)
				{
					XdmNode viewNode = (XdmNode)(viewXQueryEval.itemAt(idxView));
					XdmSequenceIterator iter = viewNode.axisIterator(Axis.DESCENDANT_OR_SELF,inputQueryNode.getNodeName());
					
					
					while (iter.hasNext() && !found)
					{
						XdmNode child = (XdmNode)iter.next();
						if (child.getAttributeValue(uid).equals(inputQueryNode.getAttributeValue(uid)))
							found = true;
					}
				}
					
				if (!found) 
				{
					System.out.println("according to saxon mapped view "+i+" does not map in query");
					System.out.println(view);
					System.out.println(query);
					return false;
				}
			}
			
			DagPattern viewDag = parser.parseQuery(view);
			if (!viewDag.treeHasMappingTo(queryDag, true, false))
			{
				System.out.println("according to code check mapped view "+i+" does not map in query");
				System.out.println(view);
				System.out.println(query);
				return false;
			}
			
			
			for (int idxPref = 0; idxPref<queryDag.getMainBranchNodesList().size() ;++idxPref)
			{
				if (viewDag.treeHasMappingTo(prefixes[idxPref], true, true) && prefixes[idxPref].treeHasMappingTo(viewDag, true, true))
				{
					System.out.println("view "+i+" is equivalent to prefix "+idxPref);
				}
			}
			
			for (int j = 0; j<mappedViewDags.size(); ++j)
				if (mappedViewDags.get(j).treeHasMappingTo(viewDag, true, true) && viewDag.treeHasMappingTo(mappedViewDags.get(j), true, true))
				{
					System.out.println("view are equivalent "+i+" "+j);
					System.out.println(view+" " +mappedViews.get(j));
					return false;
				}
			
			mappedViewDags.add(viewDag);
			mappedViews.add(view);
			
		}
		
		br.close();	
		return true;
	}
}
