package GraphGenerator;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

public class Main {
	public static void main (String[] args) throws Exception
	{
		int numberQueryTypes = 3;
		String[] queryTypes = new String[numberQueryTypes];
		queryTypes[0]="extendedSkeletonsQueries";
		queryTypes[1]="skeletonsQueries";
		queryTypes[2]="nonSkeletonsQueries";
	
		int numberQueriesLengths = 3;
		int[] queriesLengths = new int[numberQueriesLengths];
		queriesLengths[0]=5;
		queriesLengths[1]=7;
		queriesLengths[2]=9;
	
		int numberCountViews = 6;
		int[] countViews = new int[numberCountViews];
		countViews[0]=4;
		countViews[1]=8;
		countViews[2]=16;
		countViews[3]=32;
		countViews[4]=64;
		countViews[5]=128;
	
		if (args[0].equals("set1"))
		{	
			FileOutputStream fos = new FileOutputStream(new File("graphs_set1.tex"), false); 
			PrintStream fout = new PrintStream(fos);
			fout.println("\\documentclass[a4paper]{article}");
			fout.println("\\usepackage{pgfplots}");
			fout.println("\\begin{document}");
	
			//separate graphs for rewrite time according
			for (int indType = 0; indType < 3; indType++)
				for (int indLength = 0; indLength < numberQueriesLengths; indLength++)
				{
					fout.println("\\begin{figure}");
					fout.println("\\centering");
					fout.println("\\begin{tikzpicture}");
					fout.println("\\begin{axis}");
					fout.println("\t[xlabel=Number of views,ylabel=First rewrite time (ms) ]");
					fout.println("\\addplot coordinates {");
				
					for (int indViews = 0; indViews < numberCountViews; indViews++)
					{
						String resultsFile="rewrite_tests/test1.xml.tests/persistentResults/results_"+queryTypes[indType]+"_"+queriesLengths[indLength]+"_"+countViews[indViews];
						FileInputStream fstream;
						try
						{
							fstream = new FileInputStream(resultsFile);
						} catch (Exception e){continue;}
					
						DataInputStream in = new DataInputStream(fstream);
						BufferedReader br = new BufferedReader(new InputStreamReader(in));

						String line;
						int countLines = 0;
						int sum = 0;
						while ((line = br.readLine()) != null)
						{
							countLines++;
							String time = line.substring(0, line.indexOf(' '));
							sum+=Integer.parseInt(time);
						}
					
						sum = sum/countLines;
						br.close();	
					
						fout.println("("+10*countViews[indViews]+","+sum+")");
					}
					fout.println("};");
					fout.println("\\end{axis}");
					fout.println("\\end{tikzpicture}");
					fout.println("\\caption{query type: "+queryTypes[indType]+", query length:"+ queriesLengths[indLength]+"}");
					fout.println("\\end{figure}");
				}
			
			
				//then make graphs with dag sizes
				
				for (int indLength = 0; indLength < numberQueriesLengths; indLength++)
				{
					fout.println("\\begin{figure}");
					fout.println("\\centering");
					fout.println("\\begin{tikzpicture}");
					fout.println("\\begin{axis}");
					fout.println("\t[xlabel=Number of views,ylabel=First rewrite time (ms) ]");
					for (int indType = 0; indType < 3; indType++)
					{
						fout.println("\\addplot coordinates {");
						for (int indViews = 0; indViews < numberCountViews; indViews++)
						{
							String resultsFile="rewrite_tests/test1.xml.tests/persistentResults/results_"+queryTypes[indType]+"_"+queriesLengths[indLength]+"_"+countViews[indViews];
							FileInputStream fstream;
							try
							{
								fstream = new FileInputStream(resultsFile);
							} catch (Exception e){continue;}
					
							DataInputStream in = new DataInputStream(fstream);
							BufferedReader br = new BufferedReader(new InputStreamReader(in));

							String line;
							int countLines = 0;
							int sum = 0;
							while ((line = br.readLine()) != null)
							{
								countLines++;
								String time = line.substring(line.indexOf(' ')+1, line.length());
								sum+=Integer.parseInt(time);
							}
					
							sum = sum/countLines;
							br.close();	
					
							fout.println("("+10*countViews[indViews]+","+sum+")");
						}
						fout.println("};");
						fout.println("\\addlegendentry{"+queryTypes[indType]+"}");
					}
					
					fout.println("\\end{axis}");
					fout.println("\\end{tikzpicture}");
					fout.println("\\caption{dag sizes for query length:"+ queriesLengths[indLength]+"}");
					fout.println("\\end{figure}");
				}
			
				
				fout.println("\\end{document}");
				fout.close();	
			}
		
			else if (args[0].equals("set2"))
			{
				FileOutputStream fos = new FileOutputStream(new File("graphs_set2.tex"), false); 
				PrintStream fout = new PrintStream(fos);
				fout.println("\\documentclass[a4paper]{article}");
				fout.println("\\usepackage{pgfplots}");
				fout.println("\\begin{document}");
				
				int numberDocuments = 3;
				String[] docNames = new String[numberDocuments];
				docNames[0]="test1";
				docNames[1]="test2";
				docNames[2]="test3";
				
				for (int indDoc = 0; indDoc <3; indDoc++)
				{
					for (int indType = 0; indType < 3; indType++)
					{
						fout.println("\\begin{figure}");
						fout.println("\\centering");
						fout.println("\\begin{tikzpicture}");
						fout.println("\\begin{axis}");
						fout.println("\t[xlabel=Number of views,ylabel=First rewrite time (ms) ]");
						fout.println("\\addplot coordinates {");
					
						int averageTimeInputQuery = -1;
						for (int indViews = 0; indViews < numberCountViews; indViews++)
						{
							String resultsFile="rewrite_tests/"+docNames[indDoc]+".xml.tests/persistentResults/global_results_"+queryTypes[indType]+"_9_"+countViews[indViews];
							FileInputStream fstream;
							try
							{
								fstream = new FileInputStream(resultsFile);
							} catch (Exception e){continue;}
						
							DataInputStream in = new DataInputStream(fstream);
							BufferedReader br = new BufferedReader(new InputStreamReader(in));

							String line;
							int countLines = 0;
							int sumtimesRewrite = 0;
							int sumtimesApply = 0;
							int sumtimesInputQuery = 0;
							
							while ((line = br.readLine()) != null)
							{
								countLines++;
								
								String[] tokens = line.split(" ");	
								sumtimesRewrite+=Integer.parseInt(tokens[0]);
								sumtimesApply +=Integer.parseInt(tokens[1]);
								sumtimesInputQuery+=Integer.parseInt(tokens[2]);
							}
						
							sumtimesRewrite = sumtimesRewrite/countLines;
							sumtimesApply = sumtimesApply/countLines;
							sumtimesInputQuery = sumtimesInputQuery/countLines;
							averageTimeInputQuery = sumtimesInputQuery;
							
							br.close();	
						
							fout.println("("+10*countViews[indViews]+","+(sumtimesRewrite+sumtimesApply)+")");
						}
						fout.println("};");
						
						fout.println("\\addplot[red,sharp plot,update limits=false]");
						fout.println("coordinates {(0,"+averageTimeInputQuery+") ");
						fout.println("(2500,"+averageTimeInputQuery+")};");

						fout.println("\\end{axis}");
						fout.println("\\end{tikzpicture}");
						fout.println("\\caption{document: "+docNames[indDoc]+" query type: "+queryTypes[indType]+"}");
						fout.println("\\end{figure}");
					}
					
				
					
					//average view selectivities
					fout.println("\\begin{figure}");
					fout.println("\\centering");
					fout.println("\\begin{tikzpicture}");
					fout.println("\\begin{axis}");
					fout.println("\t[xlabel=Number of views,ylabel=average view selectvity ]");	
					for (int indType = 0; indType < 3; indType++)
					{
						fout.println("\\addplot coordinates {");
						for (int indViews = 0; indViews < numberCountViews; indViews++)
						{
							
							String resultsFile="rewrite_tests/"+docNames[indDoc]+".xml.tests/persistentResults/viewSelectivities_"+queryTypes[indType]+"_9_"+countViews[indViews];
							FileInputStream fstream;
							try
							{
								fstream = new FileInputStream(resultsFile);
							} catch (Exception e){continue;}
					
							DataInputStream in = new DataInputStream(fstream);
							BufferedReader br = new BufferedReader(new InputStreamReader(in));

							String line;
							int countLines = 0;
							double averageViewSelectivity = 0;
						
							while ((line = br.readLine()) != null)
							{
								countLines++;
							
								String[] tokens = line.split(" ");	
								averageViewSelectivity+=Integer.parseInt(tokens[0]);
							}
					
							averageViewSelectivity = averageViewSelectivity/countLines;
							br.close();	
					
							fout.println("("+10*countViews[indViews]+","+(averageViewSelectivity)+")");
						}
						fout.println("};");
						fout.println("\\addlegendentry{"+queryTypes[indType]+"}");
					}

					fout.println("\\end{axis}");
					fout.println("\\end{tikzpicture}");
					fout.println("\\caption{document: "+docNames[indDoc]+"}");
					fout.println("\\end{figure}");
					
					
					
					
					
					
					
					//maximal view selectivities
					fout.println("\\begin{figure}");
					fout.println("\\centering");
					fout.println("\\begin{tikzpicture}");
					fout.println("\\begin{axis}");
					fout.println("\t[xlabel=Number of views,ylabel=maximal view selectvity ]");	
					for (int indType = 0; indType < 3; indType++)
					{
						fout.println("\\addplot coordinates {");
						for (int indViews = 0; indViews < numberCountViews; indViews++)
						{
							
							String resultsFile="rewrite_tests/"+docNames[indDoc]+".xml.tests/persistentResults/viewSelectivities_"+queryTypes[indType]+"_9_"+countViews[indViews];
							FileInputStream fstream;
							try
							{
								fstream = new FileInputStream(resultsFile);
							} catch (Exception e){continue;}
					
							DataInputStream in = new DataInputStream(fstream);
							BufferedReader br = new BufferedReader(new InputStreamReader(in));

							String line;
							int countLines = 0;
							double averageViewSelectivity = 0;
						
							while ((line = br.readLine()) != null)
							{
								countLines++;
							
								String[] tokens = line.split(" ");	
								averageViewSelectivity+=Integer.parseInt(tokens[1]);
							}
					
							averageViewSelectivity = averageViewSelectivity/countLines;
							br.close();	
					
							fout.println("("+10*countViews[indViews]+","+(averageViewSelectivity)+")");
						}
						fout.println("};");
						fout.println("\\addlegendentry{"+queryTypes[indType]+"}");
					}

					fout.println("\\end{axis}");
					fout.println("\\end{tikzpicture}");
					fout.println("\\caption{document: "+docNames[indDoc]+"}");
					fout.println("\\end{figure}");
					
					
					
					
					
					
					
					//view sizes
					fout.println("\\begin{figure}");
					fout.println("\\centering");
					fout.println("\\begin{tikzpicture}");
					fout.println("\\begin{axis}");
					fout.println("\t[xlabel=Number of views,ylabel=global view file size ]");	
					for (int indType = 0; indType < 3; indType++)
					{
						fout.println("\\addplot coordinates {");
						for (int indViews = 0; indViews < numberCountViews; indViews++)
						{
							
							String resultsFile="rewrite_tests/"+docNames[indDoc]+".xml.tests/persistentResults/viewSizes_"+queryTypes[indType]+"_9_"+countViews[indViews];
							FileInputStream fstream;
							try
							{
								fstream = new FileInputStream(resultsFile);
							} catch (Exception e){continue;}
					
							DataInputStream in = new DataInputStream(fstream);
							BufferedReader br = new BufferedReader(new InputStreamReader(in));

							String line;
							int countLines = 0;
							double averageViewSelectivity = 0;
						
							while ((line = br.readLine()) != null)
							{
								countLines++;
								averageViewSelectivity+=Integer.parseInt(line);
							}
					
							averageViewSelectivity = averageViewSelectivity/countLines;
							br.close();	
					
							fout.println("("+10*countViews[indViews]+","+(averageViewSelectivity)+")");
						}
						fout.println("};");
						fout.println("\\addlegendentry{"+queryTypes[indType]+"}");
					}

					fout.println("\\end{axis}");
					fout.println("\\end{tikzpicture}");
					fout.println("\\caption{document: "+docNames[indDoc]+"}");
					fout.println("\\end{figure}");
				}
				fout.println("\\end{document}");
				fout.close();
			}
		
			else //csv
			{
				
				int numberDocuments = 3;
				String[] docNames = new String[numberDocuments];
				docNames[0]="test1";
				docNames[1]="test2";
				docNames[2]="test3";
				
				for (int indDoc = 0; indDoc <3; indDoc++)
				{
					FileOutputStream fos = new FileOutputStream(new File("subfig2"+(indDoc+1)+".csv"), false); 
					PrintStream fout = new PrintStream(fos);
					for (int indViews = 0; indViews < numberCountViews; indViews++)
					{
						fout.print(countViews[indViews]*10);
						for (int indType = 0; indType < 3; indType++)
						{
							String resultsFile="rewrite_tests/"+docNames[indDoc]+".xml.tests/persistentResults/global_results_"+queryTypes[indType]+"_9_"+countViews[indViews];
							FileInputStream fstream;
							try
							{
								fstream = new FileInputStream(resultsFile);
							} catch (Exception e){continue;}
						
							DataInputStream in = new DataInputStream(fstream);
							BufferedReader br = new BufferedReader(new InputStreamReader(in));

							String line;
							int countLines = 0;
							int sumtimesRewrite = 0;
							int sumtimesApply = 0;
							int sumtimesInputQuery = 0;
							
							while ((line = br.readLine()) != null)
							{
								countLines++;
								
								String[] tokens = line.split(" ");	
								sumtimesRewrite+=Integer.parseInt(tokens[0]);
								sumtimesApply +=Integer.parseInt(tokens[1]);
								sumtimesInputQuery+=Integer.parseInt(tokens[2]);
							}
						
							sumtimesRewrite = sumtimesRewrite/countLines;
							sumtimesApply = sumtimesApply/countLines;
							sumtimesInputQuery = sumtimesInputQuery/countLines;
							fout.print(";"+sumtimesRewrite+";"+sumtimesApply+";"+sumtimesInputQuery);
							
							br.close();	
							
						}
						fout.println();
					}
					fout.close();
				}
			}
	}
}
