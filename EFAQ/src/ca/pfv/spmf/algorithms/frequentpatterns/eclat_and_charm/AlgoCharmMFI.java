package ca.pfv.spmf.algorithms.frequentpatterns.eclat_and_charm;

/*
 * This file is copyright (c) 2008-2013 Philippe Fournier-Viger
 * 
 * This file is part of the SPMF DATA MINING SOFTWARE
 * (http://www.philippe-fournier-viger.com/spmf).
 * 
 * SPMF is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with
 * SPMF. If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import ca.pfv.spmf.datastructures.triangularmatrix.TriangularMatrix;
import ca.pfv.spmf.input.transaction_database_list_integers.TransactionDatabase;
import ca.pfv.spmf.patterns.itemset_set_integers_with_tids.Itemset;
import ca.pfv.spmf.patterns.itemset_set_integers_with_tids.Itemsets;

/**
 * This is an implementation of the CHARM-MFI algorithm (thesis of L. Szathmary,
 * 2006) that is a simple extension that takes the output of CHARM as input and
 * do post-processing to keep only maximal itemsets. <br/>
 * <br/>
 * 
 * But it was found that the Charm-MFI algorithm is actually incorrect so this
 * implementation was modified to fix the original algorithm and generate the
 * correct result. <br/>
 * <br/>
 * 
 * Note that this algorithm is not very efficient because the maximal itemsets
 * are generated by post-processing.<br/>
 * <br/>
 * 
 * Also, note that this version can save the result to a file or keep it into
 * memory if no output path is provided by the user to the runAlgorithm()
 * method.
 * 
 * @see TriangularMatrix
 * @see TransactionDatabase
 * @see Itemset
 * @see Itemsets
 * @see HashTable
 * @see ITSearchTree
 * @see AlgoCharm
 * @author Philippe Fournier-Viger
 */
public class AlgoCharmMFI {

	// for statistics
	private long		startTimestamp;	// start time of the last execution
	private long		endTimestamp;		// end time of the last execution

	// results
	// The patterns that are found
	// (if the user want to keep them into memory)
	protected Itemsets	maximalItemsets;
	BufferedWriter		writer	= null;	// object to write the output file

	/**
	 * Default constructor
	 */
	public AlgoCharmMFI() {
	}

	public Itemsets runAlgorithm(String output, Itemsets frequentClosed) throws IOException {

		// if the user want to keep the result into memory
		if (output == null) {
			writer = null;
		}
		else { // if the user want to save the result to a file
			writer = new BufferedWriter(new FileWriter(output));
		}

		maximalItemsets = frequentClosed;
		maximalItemsets.setName("FREQUENT MAXIMAL ITEMSETS");

		startTimestamp = System.currentTimeMillis();

		int maxItemsetLength = frequentClosed.getLevels().size();

		// THE ORIGINAL ALGORITHM IS INCORRECT (IT
		// DOES NOT PRODUCE THE SET OF MAXIMAL ITEMSETS IN SOME CASES BECAUSE
		// IT ONLY COMPARES CLOSED ITEMSETS OF SIZE I WITH THOSE OF SIZE I+1.
		// HOWEVER, IT IS POSSIBLE THAT AN ITEMSET IS NOT MAXIMAL BECAUSE
		// OF A CLOSED ITEMSET OF A SIZE LARGER THAN I+1. ):

		// // for closed itemsets of size i=1 to the largest size
		// for (int i = 1; i < maxItemsetLength - 1; i++) {
		// // get the itemsets of size i and i+1
		// List<Itemset> ti = frequentClosed.getLevels().get(i);
		// List<Itemset> tip1 = frequentClosed.getLevels().get(i + 1);
		// // check which one are maximals by comparing itemsets
		// // of size i and i+1
		// findMaximal(ti, tip1);
		// }

		// TO FIX IT THE ALGORITHM, WE HAVE MODIFIED IT AS FOLLOWS:

		// for closed itemsets of size i=1 to the largest size
		for (int i = 1; i < maxItemsetLength - 1; i++) {
			// get the itemsets of size i and i+1
			List<Itemset> ti = frequentClosed.getLevels().get(i);
			for (int j = i + 1; j < maxItemsetLength; j++) {
				List<Itemset> tip1 = frequentClosed.getLevels().get(j);

				// check which one are maximals by comparing itemsets
				// of size i and i+1
				if (i == 2 && j == 3) {
					System.out.println();
				}
				findMaximal(ti, tip1);
			}
		}

		// close the output file if the result was saved to a file
		if (writer != null) {
			for (List<Itemset> level : maximalItemsets.getLevels()) {
				for (int i = 0; i < level.size(); i++) {
					Itemset itemset = level.get(i);
					// if the result should be saved to a file
					writer.write(itemset.toString() + " #SUP: " + itemset.getTidset().size());
					writer.newLine();
				}
			}
			writer.close();
		}

		endTimestamp = System.currentTimeMillis();
		return maximalItemsets; // Return all frequent itemsets found!
	}

	/**
	 * Check if itemsets of size i are closed by comparing them with itemsets of
	 * size j where j > i.
	 * 
	 * @param ti
	 *            itemsets of size i
	 * @param tip1
	 *            itemsets of size j
	 */
	private void findMaximal(List<Itemset> ti, List<Itemset> tip1) {
		// for each itemset of j
		for (Itemset itemsetJ : tip1) {
			// iterates over the itemsets of size i
			Iterator<Itemset> iter = ti.iterator();
			while (iter.hasNext()) {
				Itemset itemsetI = (Itemset) iter.next();
				// if the current itemset of size i is contained
				// in itemset the current itemset of size J
				if (itemsetJ.getItems().containsAll(itemsetI.getItems())) {
					// the itemset of size I is not maximal so we remove it
					iter.remove();
					// NOTE: IF WE WOULD LIKE TO MAKE THIS MORE EFFICIENT
					// WE WOULD USE LINKED-LIST TO STORE ITEMSETS
					// INSTEAD OF ARRAY LISTS.....
					// THE COST FOR THE REMOVE OPERATION WOULD BE SMALLER...
				}
			}
		}

	}

	/**
	 * Print statistics about the algorithm execution to System.out.
	 */
	public void printStats(int transactionCount) {
		System.out.println("=============  CHARM-MFI - STATS =============");
		long temps = endTimestamp - startTimestamp;
		System.out.println(" Transactions count from database : " + transactionCount);
		System.out.println(" Frequent maximal itemsets count : "
				+ maximalItemsets.getItemsetsCount());
		System.out.println(" Total time ~ " + temps + " ms");
		System.out.println("===================================================");
	}

	/**
	 * Get the set of maximal itemsets found by Charm-MFI
	 * 
	 * @return the set of maximal itemsets
	 */
	public Itemsets getItemsets() {
		return maximalItemsets;
	}

}