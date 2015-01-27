package Rewriting;

import java.util.Date;

public class RewriteBuilder {
	public static void testRewrites(RewriteResult result)
	{	
		Date globalTimer = new Date();
		
		int mbLength = result.m_input.m_inputQuery.getMainBranchNodesList().size();
		int countViews = result.m_input.m_views.size();
		
		DagPattern[] losslessPrefixes = new DagPattern[mbLength];
		
		boolean[][] viewMapsIntoPrefix = new boolean[countViews][mbLength];
		for (int i = 0; i< mbLength; ++i)
		{
			losslessPrefixes[i] =  DagPattern.getLosslessPrefix(result.m_input.m_inputQuery, i+1);
			for (int j = 0; j< countViews; ++j)
				viewMapsIntoPrefix[j][i] = result.m_input.m_views.get(j).treeHasMappingTo(losslessPrefixes[i], true,true);
		}
		
		for (int i = mbLength-1; i< mbLength; ++i)
		{
			RewritePlan plan = new RewritePlan();
			plan.m_prefixWhereMaps = i+1;
			for (int j = 0; j< countViews; ++j)
			{
				DagPattern crtView = result.m_input.m_views.get(j);
				
				int k = 0;
				while (k<=i && !viewMapsIntoPrefix[j][k]) k++; //find first subprefix (longest compensation)
				if (k > i) continue; //no map
				
				//System.out.println("view length: "+crtView.getMainBranchNodesList().size()+ " subprefix length "+(k+1));
				
				plan.m_viewsIndexes.add(j);
				plan.m_prefLengthForViews.add(k+1);
						
				DagPattern compensatedView = DagPattern.getCompensated(crtView, losslessPrefixes[i].getMainBranchNodesList().get(k));
				
				//prune view if extended skeleton
				if (result.m_input.m_isExtendedSkeleton) compensatedView = DagPattern.getExtendedSkeleton(compensatedView);
		
				/*
				//add to akin plan if xp-slashslash
				if (result.m_input.m_isSkeleton && compensatedView.isAkin(losslessPrefixes[i]))
				{
					if (null == plan.m_initialAkinDag) plan.m_initialAkinDag = DagPattern.buildFromDag(compensatedView);
					else plan.m_initialAkinDag.stichTree(compensatedView);
				}
				*/
				
				//add to initial plan anyway
				if (null == plan.m_initialDag) plan.m_initialDag = DagPattern.buildFromDag(compensatedView);
				else plan.m_initialDag.stichTree(compensatedView);
			}
			
			//no view can participate in this plan
			if (null == plan.m_initialDag) continue;
			
			//we have a plan to test
			result.m_plans.add(plan);
			
			//check if we have an akin dag to test frist
			if (plan.m_initialAkinDag != null) 
			{
				Date redInit = new Date();
				plan.m_initialAkinDag.reduce(plan.m_statistics);		
				plan.m_timeInReduce = (new Date()).getTime() - redInit.getTime();
				
				if (!plan.m_initialAkinDag.isTree()) //we discard the whole plan!
				{
					System.out.println("akin plan dows not reduce to a tree");
					continue;			
				}
				
				if (losslessPrefixes[i].treeHasMappingTo(plan.m_initialAkinDag, true, true))		
				{
					plan.m_finalTree=plan.m_initialAkinDag;
					plan.m_rewritingType = 1; //akin
					if (result.m_timeToFirstRewrite == -1) result.m_timeToFirstRewrite = (new Date()).getTime() - globalTimer.getTime();
					continue;
				}
			}
			
			//reduce = apply rules
			plan.m_reducedDag = DagPattern.buildFromDag(plan.m_initialDag);		
			Date redInit = new Date();
			plan.m_reducedDag.reduce(plan.m_statistics);		
			plan.m_timeInReduce = (new Date()).getTime() - redInit.getTime();
			
			//did we get a tree after reduce?
			if (plan.m_reducedDag.isTree())
			{
				plan.m_finalTree = plan.m_reducedDag;			
				if (losslessPrefixes[i].treeHasMappingTo(plan.m_finalTree, true, true))		
				{
					plan.m_rewritingType = 0; //fast
					if (result.m_timeToFirstRewrite == -1) result.m_timeToFirstRewrite = (new Date()).getTime() - globalTimer.getTime();
				}
				continue;
			}
			
			//we didn't get a tree after reduce
			if (!result.m_input.m_isExtendedSkeleton && result.m_rewriteMethod == 1) //complete rewrite for outside XPes
			{
				System.out.println("computing interleavings");
				Date interInit = new Date();
				InterleavingGenerator generator = new InterleavingGenerator(plan.m_reducedDag);
				if (false == generator.getContainsAll(plan.m_finalTree)) System.out.println("exit by timeout");
				plan.m_timeInSlowRewrite = (new Date()).getTime() - interInit.getTime();
				
				if (null == plan.m_finalTree) continue; //found no interleaving (because of timeout or non-union-freedom) 
				
				if (losslessPrefixes[i].treeHasMappingTo(plan.m_finalTree, true, true))	
				{
					plan.m_rewritingType = 2; //with interleavings
					if (result.m_timeToFirstRewrite == -1) result.m_timeToFirstRewrite = (new Date()).getTime() - globalTimer.getTime();
				}
			}
		}
		result.m_totalTime = (new Date()).getTime() - globalTimer.getTime();
	}
}
