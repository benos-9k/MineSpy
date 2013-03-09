package minespy.chunkfilters;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import minespy.IChunk;

public class ComposedChunkFilter implements IChunkFilter {

	private final List<IChunkFilter> m_filters = new LinkedList<IChunkFilter>();

	public ComposedChunkFilter() {
		
	}
	
	public ComposedChunkFilter(IChunkFilter inner_) {
		addOuter(inner_);
	}
	
	public ComposedChunkFilter(IChunkFilter outer_, IChunkFilter inner_) {
		addOuter(inner_);
		addOuter(outer_);
	}

	public void addInner(IChunkFilter cff) {
		m_filters.add(0, cff);
	}

	public void addOuter(IChunkFilter cff) {
		m_filters.add(cff);
	}

	@Override
	public IChunk filter(IChunk c) {
		for (IChunkFilter cff : m_filters) {
			c = cff.filter(c);
		}
		return c;
	}

	@Override
	public String getFileName() {
		StringBuilder sb = new StringBuilder();
		for (Iterator<IChunkFilter> it = m_filters.iterator(); it.hasNext(); ) {
			sb.append(it.next().getFileName());
			if (it.hasNext()) sb.append("_");
		}
		return sb.toString();
	}

}
