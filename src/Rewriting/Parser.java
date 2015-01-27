package Rewriting;

import java.io.IOException;
import java.io.StringReader;
import java.lang.Character;
import java.util.Stack;

public class Parser {

	private char ch;
	private StringReader sr;
	private int m_crtIndex;
	private Node m_crtNode;
	private Node m_out;
	private Stack<Node> m_stack;

	
	public DagPattern parseQuery(String query) throws IOException
	{
		ch = 0;
		sr = null;
		m_crtIndex = 0;
		m_crtNode = null;
		m_out = null;
		m_stack = new Stack<Node>();
		
		sr = new StringReader(query);
		
		//read start of this query
		ch = (char)sr.read();
		
		String label = readLabel();
		Node root = new Node(label, m_crtIndex, true);
		root.setRoot(true);
		m_crtNode = root;
		m_out = m_crtNode;
		m_crtIndex++;
		
		while(ch!= (char)-1){
			switch (ch) {
				case '/': {
					readEdge(sr);
					break;
				}
				case '[': {
					readOpenQualifier(sr);
					break;
				}
				case ']': {
					readEndQualifier();
					ch = (char) sr.read();
					break;
				}
			}
		}
		
		m_out.setOut(true);
		return new DagPattern(root, m_out, m_crtIndex);	
	}

	
	private String readLabel() throws IOException
	{
		StringBuffer buffer = new StringBuffer();
		while(ch != (char)-1 && ch != '/' && ch != '[' && ch != ']')
		{
			buffer.append(ch);
			ch = (char)sr.read();
		}
		String label = new String(buffer);
		return label;
	}

	
	private void readEdge(StringReader sr) throws IOException
	{
	
		int edgeType = 0;
		ch = (char) sr.read();
		if (ch == '/') {
			ch = (char) sr.read();
			edgeType = 1;
		}
		else if (!Character.isLetter(ch)) return; 
		
		String label = readLabel();
		boolean isMBN = m_stack.isEmpty();
		
		Node newNode = new Node(label, m_crtIndex, isMBN);	
		m_crtNode.addChild(newNode, edgeType);
		
		m_crtNode = newNode;
		if (isMBN)
		{
			m_out = m_crtNode;
		}
		m_crtIndex++;
	}

	private void readOpenQualifier(StringReader sr) throws IOException
	{
		m_stack.push(m_crtNode);
		ch = (char) sr.read();
		int edgeType = 0;
		if (ch == '.') 
		{
			ch = (char) sr.read();
			ch = (char) sr.read();
			ch = (char) sr.read();
			edgeType = 1;
		}
			
		String label = readLabel();
		boolean isMBN = m_stack.isEmpty();
			
		Node newNode = new Node(label, m_crtIndex, isMBN);	
		m_crtNode.addChild(newNode, edgeType);
			
		m_crtNode = newNode;
		if (isMBN)
		{
			m_out = m_crtNode;
		}
		m_crtIndex++;
	}


	private void readEndQualifier()
	{
		m_crtNode = (Node) m_stack.pop();
	}
}
	
