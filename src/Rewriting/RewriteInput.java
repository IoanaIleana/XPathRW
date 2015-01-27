package Rewriting;

import java.util.ArrayList;

public class RewriteInput {
	public String m_inputQueryString;
	public ArrayList<String> m_viewsStrings;
	
	public DagPattern m_inputQuery;
	public ArrayList<DagPattern> m_views;
	
	public boolean m_isExtendedSkeleton;
	public boolean m_isSkeleton;
	
	
	public RewriteInput(String query, ArrayList<String> views) throws Exception
	{
		m_viewsStrings = views;
		m_inputQueryString = query;
		
		Parser parser = new Parser();
		m_inputQuery = parser.parseQuery(m_inputQueryString);
		
		m_views = new ArrayList<DagPattern>();
		for (int i= 0 ; i < m_viewsStrings.size(); ++i)
			m_views.add(parser.parseQuery(m_viewsStrings.get(i)));
		
		
		m_isExtendedSkeleton = m_inputQuery.isExtendedSkeleton();
		m_isSkeleton = m_inputQuery.isSkeleton();
	}
}
