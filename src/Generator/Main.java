package Generator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import Rewriting.DagPattern;

public class Main {
	public static void main(String args[]) throws Exception
	{	
		if (args[0].equals("inputQueries")) //we need to generate input queries
		{
			ArrayList<String> inputQueries = new ArrayList<String>();
			ArrayList<DagPattern> inputQueriesDags = new ArrayList<DagPattern>();
			
			int queryCategory = Integer.parseInt(args[1]);
			String xmlDocumentName = args[2];
			int countQueries = Integer.parseInt(args[3]);
			int minQueryLength = Integer.parseInt(args[4]);
			String outputFileName = args[5];
			
			switch (queryCategory)
			{
			case 0: InputQueryGenerator.generateInputQueries(
					inputQueries,
					inputQueriesDags,
					xmlDocumentName,
					"slashOnlyQueries",
					countQueries,
					minQueryLength); break;

			case 1: InputQueryGenerator.generateInputQueries(
					inputQueries,
					inputQueriesDags,
					xmlDocumentName,
					"noPredicatesQueries",
					countQueries,
					minQueryLength); break;
			case 2: InputQueryGenerator.generateInputQueries(
					inputQueries,
					inputQueriesDags,
					xmlDocumentName,
					"extendedSkeletonsQueries",
					countQueries,
					minQueryLength); break;
			case 3: InputQueryGenerator.generateInputQueries(
					inputQueries,
					inputQueriesDags,
					xmlDocumentName,
					"skeletonsQueries",
					countQueries,
					minQueryLength); break;
			case 4: InputQueryGenerator.generateInputQueries(
					inputQueries,
					inputQueriesDags,
					xmlDocumentName,
					"nonSkeletonsQueries",
					countQueries,
					minQueryLength); break;

			};
			
			FileOutputStream fos = new FileOutputStream(new File(outputFileName)); 
			PrintStream fout = new PrintStream(fos);
			for (int i = 0; i < inputQueries.size(); ++i)
				fout.println(inputQueries.get(i));
			fout.close();
			return;
		}	
		
		if (args[0].equals("validateInputRewriteFile"))
		{
			if (!InputValidator.validateInputRewriteFile(args[5], args[1],Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4])))
			{
				System.out.println("ERROR in input rewrite file "+args[1]+" "+args[2]+" "+args[3]+" "+args[4]);
				System.exit(0);
			}
		}
		
		else
		{
			if (!InputValidator.validateInputQueryFile(args[3], args[1],Integer.parseInt(args[2])))
			{
				System.out.println("ERROR in input query file "+args[1]+" "+args[2]);
				System.exit(0);
			}
		}

	}
}
