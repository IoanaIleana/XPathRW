package Experiments;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;

import net.sf.saxon.s9api.XdmValue;

import Applier.QueryApplier;
import Generator.ViewGenerator;
import Rewriting.DagPattern;
import Rewriting.Parser;
import Rewriting.RewriteBuilder;
import Rewriting.RewriteInput;
import Rewriting.RewritePlan;
import Rewriting.RewriteResult;

public class Main {

	public static void main (String[] args) throws Exception
	{
		if (args[0].equals("computeRewrites"))
		{
			String fileName = args[1];
			String resultsFileName = args[2];
			
			ArrayList<String> views = new ArrayList<String>();
			String query = null;

			FileInputStream fstream = new FileInputStream(fileName);	
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			query = br.readLine();
			br.readLine();
			String view;
			while ((view = br.readLine()) != null) views.add(view);
			br.close();		
			
			RewriteInput input = new RewriteInput(query, views);
			RewriteResult result = new RewriteResult(input, 1);
						
			System.out.println("testing rewrites");
			RewriteBuilder.testRewrites(result);
			System.out.println("done testing rewrites");
			if (result.m_timeToFirstRewrite == -1) 
			{
				System.out.println("error!!!");
				System.exit(0);
			}
			System.out.println(result.m_timeToFirstRewrite);
			
			
			//add to stats
			FileOutputStream fos = new FileOutputStream(new File(resultsFileName), true); 
			PrintStream fout = new PrintStream(fos);
			fout.println(result.m_timeToFirstRewrite+" "+result.m_plans.get(0).m_initialDag.getSize());			
			fout.close();	
			
			
			//detailed stats file
			fos = new FileOutputStream(new File(resultsFileName+"_detailed"), true);
			fout = new PrintStream(fos);	
			RewritePlan plan=result.m_plans.get(0);
			fout.println("time in reduce: "+plan.m_timeInReduce);
			fout.println("initial dag size: "+plan.m_initialDag.getSize());
			for (int i = 1; i<10; ++i)
					fout.println("r"+i+" time: "+plan.m_statistics[i].m_time
							+" attempts: "+ plan.m_statistics[i].m_attempts
							+" applications: "+ plan.m_statistics[i].m_applications);
			fout.println("\n");
				
			fout.close();	
			
			return;			
		}
		
		else if (args[0].equals("computeAll"))
		{
			String fileName = args[1];
			String resultsFileName = args[2];
			String viewPrefix = args[3];
			String xmlFile = args[4];
			
			ArrayList<String> views = new ArrayList<String>();
			String query = null;
			FileInputStream fstream = new FileInputStream(fileName);	
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			query = br.readLine();
			br.readLine();
			String view;
			while ((view = br.readLine()) != null) views.add(view);
			br.close();		
			
			RewriteInput input = new RewriteInput(query, views);
			RewriteResult result = new RewriteResult(input, 1);
						
			System.out.println("testing rewrites");
			RewriteBuilder.testRewrites(result);
			System.out.println("done testing rewrites");
			if (result.m_timeToFirstRewrite == -1) 
			{
				System.out.println("error!!!");
				System.exit(0);
			}
			System.out.println(result.m_timeToFirstRewrite);
			
			long refTime = new Date().getTime();
			XdmValue val = QueryApplier.applyQueryString(xmlFile, input.m_inputQueryString);
			result.m_timeOnOrigDoc = (new Date()).getTime()-refTime;
			
			refTime = new Date().getTime();
			XdmValue val1 = QueryApplier.applyPlanOnViews(result.m_plans.get(0),input.m_inputQuery, viewPrefix);
			result.m_timeOnViews = (new Date()).getTime()-refTime;
			/*
			if (!QueryApplier.areEqual(val,  val1))
			{
				System.out.println(val1.size()+ " " + val.size());
				System.out.println("error");
				System.exit(0);
			}
			*/
			/*XdmValue val2= QueryApplier.applyQueryString(xmlFile, result.m_plans.get(0).getFinalTreeXQueryWithoutCompensation());
			
			if (!QueryApplier.areEqual(val,  val2))
			{
				System.out.println(val.size()+ " " + val2.size());
				System.out.println("error");
				System.exit(0);
			}
			*/
			//add to stats
			FileOutputStream fos = new FileOutputStream(new File(resultsFileName), true); 
			PrintStream fout = new PrintStream(fos);
			fout.println(result.m_timeToFirstRewrite+" "+result.m_timeOnViews+" "+result.m_timeOnOrigDoc +" "+result.m_plans.get(0).m_timeInJoin+" dag size "+result.m_plans.get(0).m_initialDag.getSize()+ " mb nodes "+result.m_plans.get(0).m_initialDag.getMainBranchNodesList().size());
			
						
			fout.close();	
			
			return;		
		}
		
		else if (args[0].equals("makeViews"))
		{

			String fileName = args[1];
			String viewPrefix = args[2];
			String xmlFile = args[3];
			
			ArrayList<String> views = new ArrayList<String>();
			String query = null;

			FileInputStream fstream = new FileInputStream(fileName);	
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			query = br.readLine();
			br.readLine();
			String view;
			while ((view = br.readLine()) != null) views.add(view);
			br.close();		
		
						
			QueryApplier.makeMaterializedViews(xmlFile, views, viewPrefix);
			
			return;	
		}
		
		else if (args[0].equals("computeViewStats"))
		{
			String fileName = args[1];
			String resultsFileName = args[2];
			String viewPrefix = args[3];
			String xmlFile = args[4];
			int countViews = Integer.parseInt(args[5]);
			
			ArrayList<String> views = new ArrayList<String>();
			String query = null;
			FileInputStream fstream = new FileInputStream(fileName);	
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			query = br.readLine();
			br.readLine();
			String view;
			while ((view = br.readLine()) != null && views.size()<countViews) views.add(view);
			br.close();	
			
			int averageViewSelectivity = QueryApplier.computeAverageViewSelectivity(viewPrefix, views);
			int maxViewSelectivity = QueryApplier.computeMaxViewSelectivity(viewPrefix, views);
			
			//add to stats
			FileOutputStream fos = new FileOutputStream(new File(resultsFileName), true); 
			PrintStream fout = new PrintStream(fos);
			fout.println(averageViewSelectivity+" "+maxViewSelectivity);
			fout.close();	
			
			return;		
		}
		
		
		
		//generate views
		String basedir="rewrite_tests/test3.xml.tests/";
		String queryType = args[1];
		int queryLength = Integer.parseInt(args[2]);
		
		int numberCountViews = 6;
		int[] countViews = new int[numberCountViews];
		countViews[0]=4;
		countViews[1]=8;
		countViews[2]=16;
		countViews[3]=32;
		countViews[4]=64;
		countViews[5]=128;
		

		String fileName = queryType+queryLength+".in";
			
		FileInputStream fstream = new FileInputStream(basedir+fileName);	
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		
		String query = null;
		int indexQuery = 0;

		while ((query = br.readLine()) != null)
		{
			Parser parser = new Parser();
			DagPattern queryDag = parser.parseQuery(query);	
				
			for (int j = 0; j<numberCountViews; j++)
			{
				boolean ok=false;
				int countIter = 0;
				while (!ok && countIter<5)
				{
					ArrayList<String> allViews = new ArrayList<String>();
						
					System.out.println(queryType+" "+indexQuery+": "+ query);
					System.out.println("query length : "+queryLength+"; generating "+countViews[j]+" views, tentative "+ countIter);
					
					ArrayList<String> mappedViews = ViewGenerator.generateMappedViews(queryDag, countViews[j]);
					if (null == mappedViews)
					{
						System.out.println("could not generate mapped in given timeout");
						j = numberCountViews;
						break;
					}
					for (int k = 0; k < mappedViews.size(); ++k) allViews.add(mappedViews.get(k));
					ArrayList<String> incViews = ViewGenerator.generateIncompatibleViews(queryDag, 9*countViews[j]);
					if (null == incViews)
					{
						System.out.println("could not generate inc in given timeout");
						j = numberCountViews;
						break;
					}
					for (int k = 0; k < mappedViews.size(); ++k) allViews.add(incViews.get(k));
					
					
					
					
					
					RewriteInput input = new RewriteInput(query, allViews);
					RewriteResult result = new RewriteResult(input, 0);
					
					countIter++;
					System.out.println("testing rewrites");
					RewriteBuilder.testRewrites(result);
					System.out.println("done testing rewrites");
					if (result.m_timeToFirstRewrite == -1) 
					{
						System.out.println("no rewrite found with these views");
						continue;
					}
					
						
					ok = true;
					System.out.println("found a rewrite: "+result.m_timeToFirstRewrite+" "+result.m_totalTime);
					
					//time file
					FileOutputStream fos = new FileOutputStream(new File(basedir+"results/results_"+queryType+"_"+queryLength+"_"+countViews[j]), true); 
					PrintStream fout = new PrintStream(fos);
					fout.println(result.m_timeToFirstRewrite+ " "+result.m_totalTime);
					fout.close();
					
					//detailed stats file
					fos = new FileOutputStream(new File(basedir+"results/detailed_results_"+queryType+"_"+queryLength+"_"+countViews[j]), true); 
					fout = new PrintStream(fos);
					fout.println("\n\n\n");
					fout.println(queryType+" "+indexQuery+": "+ query);
					fout.println("time to first rewrite: "+result.m_timeToFirstRewrite+ ", total time: "+result.m_totalTime);	
					fout.println("plans checked: "+result.m_plans.size());
					
					for (int p = 0; p<result.m_plans.size(); ++p) if (result.m_plans.get(p).m_rewritingType<3)
					{
						fout.println("\nrewriting: plan number "+p);
						RewritePlan plan=result.m_plans.get(p);
						
						
						fout.println("rewriting type : "+plan.m_rewritingType);
						fout.println("prefix length : "+plan.m_prefixWhereMaps);
						fout.println("time in reduce: "+plan.m_timeInReduce);
						fout.println("initial dag size: "+plan.m_initialDag.getSize());
						for (int i = 1; i<10; ++i)
							fout.println("r"+i+" time: "+plan.m_statistics[i].m_time
									+" attempts: "+ plan.m_statistics[i].m_attempts
									+" applications: "+ plan.m_statistics[i].m_applications);
						
					}
					fout.close();	
							
					//make new input file
					fos = new FileOutputStream(new File(basedir+queryType+"/inputs/input_"+queryLength+"_"+countViews[j]+"_"+indexQuery));
					fout = new PrintStream(fos);
					fout.println(query+"\n");
					
					for (int indv = 0; indv<allViews.size(); indv++)
						fout.println(allViews.get(indv));
					fout.close();	
				}

			}
			indexQuery++;
		}
		br.close();	
		
		
		
	
		 
	}
}
