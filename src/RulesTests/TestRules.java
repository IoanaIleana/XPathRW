package RulesTests;

import java.io.*;

import Rewriting.*;

public class TestRules {

	public static void test(String fInput, String fOutput, String fReference)
	{
		boolean ok = true;
		
		try
		{
			FileInputStream fstream = new FileInputStream(fInput);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
	
			//read rule type
			String rule = br.readLine();
			
			//read first tree
			String query1 = br.readLine();
			
			//read second tree
			String query2 = br.readLine();
	
			in.close();
			
			Parser parser =  new Parser();
			DagPattern tree1 = parser.parseQuery(query1);
			DagPattern tree2 = parser.parseQuery(query2);
			DagPattern dag = DagPattern.buildFromDag(tree1);
			dag.stichTree(tree2);
			
			
			switch (Integer.parseInt(rule))
			{
				case 1 : Rule1Applier.applyRule1(dag); break; 
				case 2 : Rule2Applier.applyRule2(dag); break; 
				case 3 : Rule3Applier.applyRule3(dag); break; 
				case 4 : Rule4Applier.applyRule4(dag); break; 
				case 5 : Rule5Applier.applyRule5(dag); break; 
				case 6 : Rule6Applier.applyRule6(dag); break; 
				case 7 : Rule7Applier.applyRule7(dag); break; 
				case 8 : Rule8Applier.applyRule8(dag); break; 
				case 9 : Rule9Applier.applyRule9(dag); break; 
			}
			
			FileOutputStream fos = new FileOutputStream(new File(fOutput)); 
			PrintStream fout = new PrintStream(fos);
			
			dag.printNodes(fout);
			fout.close();
			
			
			
			
			
			
			BufferedReader mine = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(fOutput))));
			BufferedReader ref = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(fReference))));
			
			String line1, line2;
			while ((line1 = mine.readLine()) !=null )
			{
				line2 = ref.readLine();
				if (null == line2 || !line1.equals(line2))
				{
					ok = false;
					break;
				}
			}
			if (null != ref.readLine()) ok =false;
			
			mine.close();
			ref.close();
			
		} catch (Exception e)
		{
			System.err.println("Error: " + e.getMessage());
			ok = false;
		}
		
		if (ok) System.out.println("ok"); else System.out.println("ko");
	}
	
	
	public static void main(String[] args)
	{
		
		test("rules_test/rule1_1.in", "rules_test/rule1_1.out", "rules_test/rule1_1.ref");
		
		test("rules_test/rule2_1.in", "rules_test/rule2_1.out", "rules_test/rule2_1.ref");	
		test("rules_test/rule2_2.in", "rules_test/rule2_2.out", "rules_test/rule2_2.ref");
		test("rules_test/rule2_3.in", "rules_test/rule2_3.out", "rules_test/rule2_3.ref");
		
		test("rules_test/rule3_1.in", "rules_test/rule3_1.out", "rules_test/rule3_1.ref");
		test("rules_test/rule3_2.in", "rules_test/rule3_2.out", "rules_test/rule3_2.ref");
		test("rules_test/rule3_3.in", "rules_test/rule3_3.out", "rules_test/rule3_3.ref");
		test("rules_test/rule3_4.in", "rules_test/rule3_4.out", "rules_test/rule3_4.ref");
		
		test("rules_test/rule4_1.in", "rules_test/rule4_1.out", "rules_test/rule4_1.ref");
		test("rules_test/rule4_2.in", "rules_test/rule4_2.out", "rules_test/rule4_2.ref");
		test("rules_test/rule4_3.in", "rules_test/rule4_3.out", "rules_test/rule4_3.ref");
		test("rules_test/rule4_4.in", "rules_test/rule4_4.out", "rules_test/rule4_4.ref");
		test("rules_test/rule4_5.in", "rules_test/rule4_5.out", "rules_test/rule4_5.ref");
		
		test("rules_test/rule5_1.in", "rules_test/rule5_1.out", "rules_test/rule5_1.ref");
		test("rules_test/rule5_2.in", "rules_test/rule5_2.out", "rules_test/rule5_2.ref");
		test("rules_test/rule5_3.in", "rules_test/rule5_3.out", "rules_test/rule5_3.ref");
	
		test("rules_test/rule5_4.in", "rules_test/rule5_4.out", "rules_test/rule5_4.ref");
		
		test("rules_test/rule5_5.in", "rules_test/rule5_5.out", "rules_test/rule5_5.ref");
		
		test("rules_test/rule6_1.in", "rules_test/rule6_1.out", "rules_test/rule6_1.ref");
		test("rules_test/rule6_2.in", "rules_test/rule6_2.out", "rules_test/rule6_2.ref");
		test("rules_test/rule6_3.in", "rules_test/rule6_3.out", "rules_test/rule6_3.ref");
		test("rules_test/rule6_4.in", "rules_test/rule6_4.out", "rules_test/rule6_4.ref");
		
		test("rules_test/rule7_1.in", "rules_test/rule7_1.out", "rules_test/rule7_1.ref");
		test("rules_test/rule7_2.in", "rules_test/rule7_2.out", "rules_test/rule7_2.ref");
		test("rules_test/rule7_3.in", "rules_test/rule7_3.out", "rules_test/rule7_3.ref");
		test("rules_test/rule7_4.in", "rules_test/rule7_4.out", "rules_test/rule7_4.ref");
		
		test("rules_test/rule8_1.in", "rules_test/rule8_1.out", "rules_test/rule8_1.ref");
		test("rules_test/rule8_2.in", "rules_test/rule8_2.out", "rules_test/rule8_2.ref");
		test("rules_test/rule8_3.in", "rules_test/rule8_3.out", "rules_test/rule8_3.ref");
		test("rules_test/rule8_4.in", "rules_test/rule8_4.out", "rules_test/rule8_4.ref");
		
		test("rules_test/rule9_1.in", "rules_test/rule9_1.out", "rules_test/rule9_1.ref");
		test("rules_test/rule9_2.in", "rules_test/rule9_2.out", "rules_test/rule9_2.ref");
		test("rules_test/rule9_3.in", "rules_test/rule9_3.out", "rules_test/rule9_3.ref");
		test("rules_test/rule9_4.in", "rules_test/rule9_4.out", "rules_test/rule9_4.ref");
		
	}
}
