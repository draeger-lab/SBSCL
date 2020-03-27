package test;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import cern.colt.bitvector.BitVector;

import fern.analysis.IntQueue;
import fern.analysis.IntSearchStructure;
import fern.analysis.IntStack;
import fern.tools.NumberTools;
import fern.tools.Stochastics;
import fern.tools.gnuplot.ArrayMatrixAxes;
import fern.tools.gnuplot.Axes;
import fern.tools.gnuplot.CollectionAxes;
import fern.tools.gnuplot.TransposedArrayMatrixAxes;

public class ToolsTest {

	/**
	 * @param args
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		
		int[] c = {1,2,3,4,5,6,7,8,9};
		NumberTools.cumSum(c);
		System.out.println(Arrays.toString(c));
		
		IntSearchStructure q = new IntStack();
		for (int i=0; i<100; i++)
			q.add(i);
		
		while (!q.isEmpty())
			System.out.println(q.get());
		
		
		BitVector bv = new BitVector(10);
		bv.set(3);
		bv.set(4);
		bv.set(9);
		System.out.println(Arrays.toString(NumberTools.getContentAsArray(bv)));
		
		int[][] array = {{1,2,3},{10,20,30},{100,200,300}};
		Collection list = new LinkedList();
		list.add(new int[] {4,5});
		list.add(new int[] {40,50});
		list.add(new int[] {400,500});
		
		Axes axes = new ArrayMatrixAxes(array);
		Axes axes2 = new CollectionAxes(list);
		Axes axes3 = new ArrayMatrixAxes(array);
		
		

		
		for (String row : axes)
			System.out.println(row);
		
		for (int i=0; i<10; i++)
			System.out.println(Stochastics.getInstance().getExponential(1));
		System.out.println();
		for (int i=0; i<10; i++)
			System.out.println(Stochastics.getInstance().getExponential(100));
		
		
	}

}
