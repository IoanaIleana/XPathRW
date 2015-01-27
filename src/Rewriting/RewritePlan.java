package Rewriting;

import java.util.ArrayList;

public class RewritePlan {
	
	//indices of the views in the view list of the RewriteResult
	public ArrayList<Integer> m_viewsIndexes;
	
	//length of the sub-prefix where the view maps (gives compensation)
	public ArrayList<Integer> m_prefLengthForViews;
	
	//length of the prefix where the plan maps (gives final compensation)
	public int m_prefixWhereMaps;
	
	public int m_rewritingType; //0 fast, 1 akin, 2 with interleavings, 3 not a rewrite
		
	//statistics on rules application
	public RuleStatistics[] m_statistics;
	
	public DagPattern m_initialDag;
	public DagPattern m_initialAkinDag;
	public DagPattern m_reducedDag;
	public DagPattern m_finalTree;

	public long m_timeInReduce;
	public long m_timeInSlowRewrite;
	public long m_timeInJoin;

	public RewritePlan()
	{
		m_viewsIndexes = new ArrayList<Integer>();
		m_prefLengthForViews = new ArrayList<Integer>();
		
		
		m_statistics = new RuleStatistics[10];
		for (int i = 0; i<10; ++i)
			m_statistics[i] = new RuleStatistics();
		
		m_initialDag = null;
		m_initialAkinDag = null;
		m_reducedDag = null;
		m_finalTree = null;
		
		m_prefixWhereMaps = -1;
		m_rewritingType = 3; //no rewrite

		
		m_timeInReduce = -1;
		m_timeInSlowRewrite = -1;
		m_timeInJoin = 0;
	}

/*
	public String getCompensatedViewXQuery(int viewIndex, DagPattern inputQuery, ArrayList<DagPattern> views)
	{
		int realViewIndex = m_viewsIndexes.get(viewIndex);
		DagPattern losslessPrefix =  DagPattern.getLosslessPrefix(inputQuery, m_prefixWhereMaps);
		DagPattern compensatedView = DagPattern.getCompensated(views.get(realViewIndex), losslessPrefix.getMainBranchNodesList().get(m_prefLengthForViews.get(viewIndex)-1));
	
		return compensatedView.getQueryString(0,compensatedView.getMainBranchNodesList().size());
	}
*/	
	
	public String getFinalTreeXQueryWithoutCompensation()
	{
		return m_finalTree.getQueryString(0, m_finalTree.getMainBranchNodesList().size());
	}

	
	public String getSingleViewWholeXQueryOnViewFile(int viewIndex, DagPattern inputQuery, String viewPrefix)
	{
		int realViewIndex = m_viewsIndexes.get(viewIndex);
		return "for $var"+realViewIndex+" in doc(\""+viewPrefix+"_"+realViewIndex+"\")/doc/v"+realViewIndex+"/"+inputQuery.getQueryString(m_prefLengthForViews.get(viewIndex)-1, m_prefixWhereMaps) + " return $var"+realViewIndex;
	}
	
}
