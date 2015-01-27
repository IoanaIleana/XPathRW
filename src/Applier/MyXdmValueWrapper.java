package Applier;

import java.util.ArrayList;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmEmptySequence;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;

public class MyXdmValueWrapper {
	private XdmNode[] m_items;
	private String[] m_ids;
	private int m_size;
	static QName uid = new QName("uid");
	
	private void sift(int crtIndex, int size)
	{
		while (true)
		{
			int k = crtIndex;
			if (2*crtIndex+1<size && m_ids[2*crtIndex+1].compareTo(m_ids[k])>0)  k = 2*crtIndex+1;
			if (2*crtIndex+2<size && m_ids[2*crtIndex+2].compareTo(m_ids[k])>0)  k = 2*crtIndex+2;
			if (k == crtIndex) break;
			String aux = m_ids[crtIndex]; m_ids[crtIndex] = m_ids[k]; m_ids[k] = aux;
			XdmNode auxnode = m_items[crtIndex]; m_items[crtIndex] = m_items[k]; m_items[k] = auxnode;
			crtIndex = k;
		}
	}
	
	public MyXdmValueWrapper(XdmValue val)
	{
		m_size = val.size();
		m_items = new XdmNode[m_size];
		m_ids = new String[m_size];
		
		for (int i = 0; i<m_size; ++i)
		{
			m_items[i] = (XdmNode)val.itemAt(i);
			m_ids[i] = m_items[i].getAttributeValue(uid);
		}
		
		//sift
		for (int n = m_size/2; n>=0; n--)
			sift(n, m_size);
		
		//sort
		for (int n = m_size - 1; n>0; n--)
		{
			String aux = m_ids[0]; m_ids[0] = m_ids[n]; m_ids[n] = aux;
			XdmNode auxnode = m_items[0]; m_items[0] = m_items[n]; m_items[n] = auxnode;
			sift(0, n);
		}
	}
	
	public void intersect(XdmValue val2)
	{
		MyXdmValueWrapper wrapper2 = new MyXdmValueWrapper(val2);
		
		ArrayList<XdmNode> intersectList = new ArrayList<XdmNode>();
		int i = 0;
		int j = 0;
		while (i<m_size && j<wrapper2.m_size)
		{
			int compare = m_ids[i].compareTo(wrapper2.m_ids[j]);
			if (compare < 0) i++;
			else if (compare > 0) j++;
			else
			{
				intersectList.add(m_items[i]);
				i++;
				j++;
			}
		}
		
		m_size = intersectList.size();
		for (i = 0; i< intersectList.size(); ++i)
		{
			m_items[i] = intersectList.get(i);
			m_ids[i] = m_items[i].getAttributeValue(uid);
		}
	}
	
	public XdmValue getXdmValue()
	{
		XdmValue seq = XdmEmptySequence.getInstance();
		for (int i = 0; i< m_size; ++i)
			seq = seq.append(m_items[i]);
		return seq;
	}
}
