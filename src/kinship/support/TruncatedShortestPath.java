/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University 
 * of California
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * http://jung.sourceforge.net/license.txt for a description.
 */
package kinship.support;

import java.util.HashMap;
import java.util.Map;

import kinship.model.KinshipModel;

import edu.uci.ics.jung.algorithms.shortestpath.Distance;
import edu.uci.ics.jung.algorithms.shortestpath.ShortestPath;
import edu.uci.ics.jung.graph.Graph;

/**
 * Computes the shortest path distances for graphs whose edges are not weighted
 * (using BFS).
 */

public class TruncatedShortestPath<V, E> implements ShortestPath<V, E>, Distance<V> {

	private Map<V, Map<V, Number>> mDistanceMap;

	private Graph<V, E> mGraph;

	public KinshipModel myWorld;

	/**
	 * Constructs and initializes algorithm
	 * 
	 * @param g
	 *            the graph
	 */
	public TruncatedShortestPath(Graph<V, E> g, KinshipModel myWorld) {
		mDistanceMap = new HashMap<V, Map<V, Number>>();
		mGraph = g;
		this.myWorld = myWorld;
	}

	/**
	 * @see edu.uci.ics.jung.algorithms.shortestpath.Distance#getDistance(edu.uci.ics.jung.graph.ArchetypeVertex,
	 *      edu.uci.ics.jung.graph.ArchetypeVertex)
	 */
	public Number getDistance(V source, V target) {
		Map<V, Number> sourceSPMap = getDistanceMap(source);
		return sourceSPMap.get(target);
	}

	/**
	 * @see edu.uci.ics.jung.algorithms.shortestpath.Distance#getDistanceMap(edu.uci.ics.jung.graph.ArchetypeVertex)
	 */
	public Map<V, Number> getDistanceMap(V source) {
		Map<V, Number> sourceSPMap = mDistanceMap.get(source);
		if (sourceSPMap == null) {
			computeShortestPathsFromSource(source);
			sourceSPMap = mDistanceMap.get(source);
		}
		return sourceSPMap;
	}

	/**
	 * Computes the shortest path distance from the source to target. If the
	 * shortest path distance has not already been computed, then all pairs
	 * shortest paths will be computed.
	 * 
	 * @param source
	 *            the source node
	 * @param target
	 *            the target node
	 * @return the shortest path value (if the target is unreachable, NPE is
	 *         thrown)
	 * @deprecated use getDistance
	 */
	public int getShortestPath(V source, V target) {
		return getDistance(source, target).intValue();
	}

	/**
	 * Computes the shortest path distances from a given node to all other
	 * nodes.
	 * 
	 * @param graph
	 *            the graph
	 * @param source
	 *            the source node
	 * @return A <code>Map</code> whose keys are target vertices and whose
	 *         values are <code>Integers</code> representing the shortest path
	 *         distance
	 */
	private void computeShortestPathsFromSource(V source) {
		TruncatedDistanceLabeller<V, E> labeler = new TruncatedDistanceLabeller<V, E>(this.myWorld);
		labeler.labelDistances(mGraph, source);
		mDistanceMap.put(source, labeler.getDistanceDecorator());
	}

	/**
	 * Clears all stored distances for this instance. Should be called whenever
	 * the graph is modified (edge weights changed or edges added/removed). If
	 * the user knows that some currently calculated distances are unaffected by
	 * a change, <code>reset(V)</code> may be appropriate instead.
	 * 
	 * @see #reset(V)
	 */
	public void reset() {
		mDistanceMap.clear();
	}

	/**
	 * Clears all stored distances for the specified source vertex
	 * <code>source</code>. Should be called whenever the stored distances from
	 * this vertex are invalidated by changes to the graph.
	 * 
	 * @see #reset()
	 */
	public void reset(V v) {
		mDistanceMap.remove(v);
	}

	public Map<V, E> getIncomingEdgeMap(V source) {
		return null;
	}
}
