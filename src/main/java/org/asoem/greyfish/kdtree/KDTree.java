package org.asoem.sico.kdtree;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.MinMaxPriorityQueue;

/**
 * KDTree is a class supporting KD-tree insertion, deletion, equality
 * search, range search, and nearest neighbor(s) using double-precision
 * floating-point keys.  Splitting dimension is chosen naively, by
 * depth modulo K.  Semantics are as follows:
 *
 * <UL>
 * <LI> Two different keys containing identical numbers should retrieve the 
 *      same value from a given KD-tree.  Therefore keys are cloned when a 
 *      node is inserted.
 * <BR><BR>
 * <LI> As with Hashtables, values inserted into a KD-tree are <I>not</I>
 *      cloned.  Modifying a value between insertion and retrieval will
 *      therefore modify the value stored in the tree.
 *</UL>
 *
 * Implements the Nearest Neighbor algorithm (Table 6.4) of
 *
 * <PRE>
 * &#064;techreport{AndrewMooreNearestNeighbor,
 *   author  = {Andrew Moore},
 *   title   = {An introductory tutorial on kd-trees},
 *   institution = {Robotics Institute, Carnegie Mellon University},
 *   year    = {1991},
 *   number  = {Technical Report No. 209, Computer Laboratory, 
 *              University of Cambridge},
 *   address = {Pittsburgh, PA}
 * }
 * </PRE>
 *  
 *
 * @author      Simon Levy, Bjoern Heckel
 * @version     %I%, %G%
 * @since JDK1.2 
 */
public class KDTree<T> {
	// number of milliseconds
	final long m_timeout;

	// K = number of dimensions
	final private int m_dimension;

	// root of KD-tree
	private KDNode<T> m_root;

	// count of nodes
	private int m_count;

	/**
	 * Creates a KD-tree with specified number of dimensions.
	 *
	 * @param k number of dimensions
	 */
	public KDTree(int k) {
		this(k, 0);
	}
	public KDTree(int k, long timeout) {
		this.m_timeout = timeout;
		m_dimension = k;
		m_root = null;
	}


	/** 
	 * Insert a node in a KD-tree.  Uses algorithm translated from 352.ins.c of
	 *
	 *   <PRE>
	 *   &#064;Book{GonnetBaezaYates1991,                                   
	 *     author =    {G.H. Gonnet and R. Baeza-Yates},
	 *     title =     {Handbook of Algorithms and Data Structures},
	 *     publisher = {Addison-Wesley},
	 *     year =      {1991}
	 *   }
	 *   </PRE>
	 *
	 * @param key key for KD-tree node
	 * @param value value at that key
	 *
	 * @throws KeySizeException if key.length mismatches K
	 * @throws KeyDuplicateException if key already in tree
	 */
	public void insert(double [] key, T value) throws KeyDuplicateException {
		edit(key, BaseEditor.newInserter(value));
	}

	/** 
	 * Edit a node in a KD-tree
	 *
	 * @param key key for KD-tree node
	 * @param editor object to edit the value at that key
	 *
	 * @throws KeySizeException if key.length mismatches K
	 * @throws KeyDuplicateException if key already in tree
	 */

	public void edit(double[] key, Editor<T> editor) throws KeyDuplicateException {

		keyCheck(key);

		synchronized (this) {
			// the first insert has to be synchronized
			if (null == m_root) {
				m_root = KDNode.create(new HPoint(key), editor);
				m_count = m_root.deleted ? 0 : 1;
				return;
			}
		}

		m_count += KDNode.edit(new HPoint(key), editor, m_root, 0, m_dimension);
	}

	/** 
	 * Find  KD-tree node whose key is identical to key.  Uses algorithm 
	 * translated from 352.srch.c of Gonnet & Baeza-Yates.
	 *
	 * @param key key for KD-tree node
	 *
	 * @return object at key, or null if not found
	 *
	 * @throws KeySizeException if key.length mismatches K
	 */
	public T search(double [] key) {
		keyCheck(key);
		KDNode<T> kd = KDNode.srch(new HPoint(key), m_root, m_dimension);
		return (kd == null ? null : kd.element);
	}


	public KDNode<T> delete(double [] key) {
		keyCheck(key);

		KDNode<T> t = KDNode.srch(new HPoint(key), m_root, m_dimension);
		if (t != null && KDNode.del(t)) {
			m_count--;
			return t;
		}
		
		return null;
	}

	/**
	 * Find KD-tree node whose key is nearest neighbor to
	 * key. 
	 *
	 * @param key key for KD-tree node
	 *
	 * @return object at node nearest to key, or null on failure
	 *
	 * @throws KeySizeException if key.length mismatches K

	 */
	public T nearest(double [] key) {

		List<T> nbrs = nearest(key, 1, null);
		return nbrs.get(0);
	}

	/**
	 * Find KD-tree nodes whose keys are <i>n</i> nearest neighbors to
	 * key. 
	 *
	 * @param key key for KD-tree node
	 * @param n number of nodes to return
	 *
	 * @return objects at nodes nearest to key, or null on failure
	 *
	 * @throws KeySizeException if key.length mismatches K

	 */
	public List<T> nearest(double [] key, int n) throws IllegalArgumentException {
		return nearest(key, n, null);
	}

	/**
	 * Find KD-tree nodes whose keys are within a given Euclidean distance of
	 * a given key.
	 *
	 * @param key key for KD-tree node
	 * @param d Euclidean distance
	 *
	 * @return objects at nodes with distance of key, or null on failure
	 *
	 * @throws KeySizeException if key.length mismatches K

	 */
	public List<T> nearestEuclidean(double [] key, double dist) {
		return nearestDistance(key, dist, new EuclideanDistance());
	}


	/**
	 * Find KD-tree nodes whose keys are within a given Hamming distance of
	 * a given key.
	 *
	 * @param key key for KD-tree node
	 * @param d Hamming distance
	 *
	 * @return objects at nodes with distance of key, or null on failure
	 *
	 * @throws KeySizeException if key.length mismatches K

	 */
	public List<T> nearestHamming(double [] key, double dist) {

		return nearestDistance(key, dist, new HammingDistance());
	}


	/**
	 * Find KD-tree nodes whose keys are <I>n</I> nearest neighbors to
	 * key. Uses algorithm above.  Neighbors are returned in ascending
	 * order of distance to key. 
	 *
	 * @param key key for KD-tree node
	 * @param n how many neighbors to find
	 * @param checker an optional object to filter matches
	 *
	 * @return objects at node nearest to key, or null on failure
	 *
	 * @throws KeySizeException if key.length mismatches K
	 * @throws IllegalArgumentException if <I>n</I> is negative or
	 * exceeds tree size 
	 */
	public List<T> nearest(double [] key, int n, Predicate<T> checker) throws IllegalArgumentException {

		if (n <= 0) {
			return new LinkedList<T>();
		}

		MinMaxPriorityQueue<KDNode<T>> nnl = getnbrs(key, n, checker);

		n = nnl.size();
		Stack<T> nbrs = new Stack<T>();

		for (int i=0; i<n; ++i) {
			KDNode<T> kd = nnl.removeLast();
			nbrs.push(kd.element);
		}

		return nbrs;
	}


	/** 
	 * Range search in a KD-tree.  Uses algorithm translated from
	 * 352.range.c of Gonnet & Baeza-Yates.
	 *
	 * @param lowk lower-bounds for key
	 * @param uppk upper-bounds for key
	 *
	 * @return array of Objects whose keys fall in range [lowk,uppk]
	 *
	 * @throws KeySizeException on mismatch among lowk.length, uppk.length, or K
	 */
	public List<T> range(double[] lowk, double[] uppk) {

		keyCheck(lowk);
		keyCheck(uppk);

		List<KDNode<T>> found = new LinkedList<KDNode<T>>();
		KDNode.rsearch(new HPoint(lowk), new HPoint(uppk), 
				m_root, 0, m_dimension, found);
		List<T> o = new LinkedList<T>();
		for (KDNode<T> node : found) {
			o.add(node.element);
		}
		return o;
	}

	public int size() { /* added by MSL */
		return m_count;
	}

	public String toString() {
		return m_root.toString(0);
	}

	private MinMaxPriorityQueue<KDNode<T>> getnbrs(double [] key)  {
		return getnbrs(key, m_count, null);
	}


	private MinMaxPriorityQueue<KDNode<T>> getnbrs(double [] key, int n, Predicate<T> checker) {

		keyCheck(key);

		MinMaxPriorityQueue<KDNode<T>> nnl = MinMaxPriorityQueue.maximumSize(n).create();

		// initial call is with infinite hyper-rectangle and max distance
		HRect hr = HRect.infiniteHRect(key.length);
		double max_dist_sqd = Double.MAX_VALUE;
		HPoint keyp = new HPoint(key);

		if (m_count > 0) {
			long timeout = (this.m_timeout > 0) ? 
					(System.currentTimeMillis() + this.m_timeout) : 
						0;
					KDNode.nnbr(m_root, keyp, hr, max_dist_sqd, 0, m_dimension, nnl, checker, timeout);
		}

		return nnl;

	}

	private List<T> nearestDistance(double [] key, double dist, 
			DistanceMetric metric) {

		MinMaxPriorityQueue<KDNode<T>> nnl = getnbrs(key);
		int n = nnl.size();
		Stack<T> nbrs = new Stack<T>();

		for (int i=0; i<n; ++i) {
			KDNode<T> kd = nnl.removeLast();
			HPoint p = kd.point;
			if (metric.distance(kd.point.coord, key) < dist) {
				nbrs.push(kd.element);
			}
		}

		return nbrs;
	}

	public void clear() {
		m_root = null;
		m_count = 0;
	}

	private void keyCheck(double[] key) {
		Preconditions.checkArgument(Preconditions.checkNotNull(key).length == m_dimension, "Key size mismatch");
	}
}

