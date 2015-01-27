package Rewriting;

import java.util.ArrayList;

public class RewriteResult {
	
	public RewriteInput m_input;
	public ArrayList<RewritePlan> m_plans;
	
	int m_rewriteMethod; //0 fast, 1 full, 2 naive
	
	public long m_timeToFirstRewrite;
	public long m_totalTime;
	
	public long m_timeOnViews;
	public long m_timeOnOrigDoc;
	
	public RewriteResult(RewriteInput input, int rewriteMethod) throws Exception
	{	
		m_input = input;
		
		m_plans = new ArrayList<RewritePlan>();
		
		m_rewriteMethod = rewriteMethod;
		
		m_timeToFirstRewrite = -1;
		m_totalTime = -1;	
		m_timeOnViews = -1;
		m_timeOnOrigDoc = -1;
	}
}
