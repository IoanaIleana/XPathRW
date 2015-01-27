package Applier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;

import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.s9api.XdmValue;
import Rewriting.DagPattern;
import Rewriting.RewritePlan;

public class QueryApplier {
	
	public static void makeMaterializedViews(String xmlFile, ArrayList<String> views, String viewFilePrefix) throws Exception
	{
		
		Processor proc = new Processor(false);
		
		FileOutputStream fosall = new FileOutputStream(new File(viewFilePrefix+"_all")); 
		PrintStream foutall = new PrintStream(fosall);
		foutall.println("<doc>");
		Serializer outall = proc.newSerializer(foutall);
		outall.setOutputProperty(Serializer.Property.METHOD, "xml");
		outall.setOutputProperty(Serializer.Property.INDENT, "no");
		outall.setOutputProperty(Serializer.Property.OMIT_XML_DECLARATION, "yes");

		
	
		for (int i = 0; i< views.size()/2; ++i)
		{
			XdmValue val = applyQueryString(xmlFile, views.get(i));
			
			
			FileOutputStream fos = new FileOutputStream(new File(viewFilePrefix+"_"+i)); 
			PrintStream fout = new PrintStream(fos);
			fout.println("<doc>");
			fout.println("<v"+i+">");
			
			foutall.println("<v"+i+">");
			
			
			Serializer out = proc.newSerializer(fout);
			out.setOutputProperty(Serializer.Property.METHOD, "xml");
			out.setOutputProperty(Serializer.Property.INDENT, "no");
			out.setOutputProperty(Serializer.Property.OMIT_XML_DECLARATION, "yes");
			
			proc.writeXdmValue(val, outall);
			
			proc.writeXdmValue(val, out);
			
			fout.println("</v"+i+">");
			foutall.println("</v"+i+">");
			fout.println("</doc>");
			fout.close();
		}

		
			
		foutall.println("</doc>");
		foutall.close();
	}
	
	
	public static XdmValue applyPlanOnViews(RewritePlan plan, DagPattern inputQuery, String viewPrefix) throws Exception
	{
		String query = plan.getSingleViewWholeXQueryOnViewFile(0, inputQuery, viewPrefix);
		XdmValue children = applyWholeQueryString( query);
		MyXdmValueWrapper wr = new MyXdmValueWrapper(children);
		
		for (int i = 1; i < plan.m_viewsIndexes.size(); ++i)
		{	
			query= plan.getSingleViewWholeXQueryOnViewFile(i, inputQuery, viewPrefix);
			XdmValue crt = applyWholeQueryString(query);
			
			long initTime = (new Date()).getTime();
			wr.intersect(crt);
			plan.m_timeInJoin += (new Date()).getTime() - initTime;
		}
		
		return wr.getXdmValue();
	}

	
	public static XdmValue applyQueryString(String docName, String queryString) throws Exception
	{
		String wholeQueryString = "for $var0 in doc(\""+docName+"\")/"+queryString+" return $var0";
		
		Processor proc = new Processor(false);
		XQueryCompiler compiler = proc.newXQueryCompiler();
		 
		XQueryEvaluator evaluator = compiler.compile(wholeQueryString).load();
		XdmValue children = evaluator.evaluate();
		
		return children;
	}
	
	public static XdmValue applyWholeQueryString( String queryString) throws Exception
	{
		Processor proc = new Processor(false);
		XQueryCompiler compiler = proc.newXQueryCompiler();
		 
		XQueryEvaluator evaluator = compiler.compile(queryString).load();
		XdmValue children = evaluator.evaluate();
		
		return children;
	}
	
	
	public static void printResults(XdmValue val) throws Exception
	{
		Processor proc = new Processor(false);
		Serializer out = proc.newSerializer(System.out);
		out.setOutputProperty(Serializer.Property.METHOD, "xml");
		out.setOutputProperty(Serializer.Property.INDENT, "no");
		out.setOutputProperty(Serializer.Property.OMIT_XML_DECLARATION, "yes");

		proc.writeXdmValue(val, out);
	}
	
	public static boolean areEqual(XdmValue val1, XdmValue val2)
	{
		QName uid = new QName("uid");
		
		if (val1.size()!=val2.size()) return false;
		for (int idx1 = 0; idx1<val1.size(); ++idx1)
		{
			XdmNode val1Node = (XdmNode)(val1.itemAt(idx1));
			boolean found = false;
			
			for (int idx2 = 0; idx2<val2.size() && !found; idx2++)
			{
				XdmNode val2Node = (XdmNode)(val2.itemAt(idx2));
				if (val2Node.getAttributeValue(uid).equals(val1Node.getAttributeValue(uid)))
						found = true;
			}
			if (!found) return false;
		}
		return true;
	}
	
	
	public static int computeAverageViewSelectivity(String viewPrefix, ArrayList<String> views) throws Exception
	{
		int sum = 0;
		for (int i = 0; i < views.size(); ++i)
		{	
			String query = "for $var0 in doc(\""+viewPrefix+"_"+i+"\")/doc/v"+i+" return $var0";
			XdmValue crt = applyWholeQueryString(query);
			XdmNode viewNode = (XdmNode)(crt.itemAt(0));
			XdmSequenceIterator iter = viewNode.axisIterator(Axis.CHILD);
			int count =0;
			while (iter.hasNext())
			{
				iter.next();
				count++;
			}
			
			sum+=count;
		}
		
		sum/=views.size();
		return sum;
	}
	
	public static int computeMaxViewSelectivity(String viewPrefix, ArrayList<String> views) throws Exception
	{
		int max = -1;
		for (int i = 0; i < views.size(); ++i)
		{	
			String query = "for $var0 in doc(\""+viewPrefix+"_"+i+"\")/doc/v"+i+" return $var0";
			XdmValue crt = applyWholeQueryString(query);
			XdmNode viewNode = (XdmNode)(crt.itemAt(0));
			XdmSequenceIterator iter = viewNode.axisIterator(Axis.CHILD);
			int count =0;
			while (iter.hasNext())
			{
				iter.next();
				count++;
			}
			
			if (max<count)
				max = count;
		}
		
		return max;
	}
	
	
}
