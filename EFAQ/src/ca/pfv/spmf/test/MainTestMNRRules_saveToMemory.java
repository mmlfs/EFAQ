package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.associationrules.MNRRules.AlgoMNRRules;
import ca.pfv.spmf.algorithms.frequentpatterns.zart.AlgoZart;
import ca.pfv.spmf.algorithms.frequentpatterns.zart.TZTableClosed;
import ca.pfv.spmf.input.transaction_database_list_integers.TransactionDatabase;
import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemset;
import ca.pfv.spmf.patterns.rule_itemset_array_integer_with_count.Rules;

/**
 * Example of how to generate minimal non redundant association rules in source
 * code.
 * 
 * @author Philippe Fournier-Viger, 2008
 */
public class MainTestMNRRules_saveToMemory {

	public static void main(String[] args) throws IOException {

		System.out
				.println("STEP 1 : EXECUTING THE ZART ALGORITHM TO FIND CLOSED ITEMSETS AND MINIMUM GENERATORS");
		String input = fileToPath("contextZart.txt");
		String output = null;
		// Note, above, we set the output file path to null
		// because we want to keep the result into memory for this example

		double minsup = 0.6; // minimum support
		double minconf = 0.6; // minimum confidence

		TransactionDatabase database = new TransactionDatabase();
		try {
			database.loadFile(input);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		database.printDatabase();

		// Applying the Zart algorithm
		AlgoZart zart = new AlgoZart();
		TZTableClosed results = zart.runAlgorithm(database, minsup);
		zart.printStatistics();

		// // PRINT RESULTS FROM THE ZART ALGORITHM
		// int countClosed=0;
		// int countGenerators=0;
		// System.out.println("===================");
		// for(int i=0; i< results.levels.size(); i++){
		// System.out.println("LEVEL : " + i);
		// for(Itemset closed : results.levels.get(i)){
		// System.out.println(" CLOSED : " + closed.toString() + "  supp : " +
		// closed.getAbsoluteSupport());
		// countClosed++;
		// System.out.println("   GENERATORS : ");
		// for(Itemset generator : results.mapGenerators.get(closed)){
		// countGenerators++;
		// System.out.println("     =" + generator.toString());
		// }
		// }
		// }

		System.out.println("STEP 2 : CALCULATING MNR ASSOCIATION RULES");
		// Create MNR rules.
		AlgoMNRRules algoMNR = new AlgoMNRRules();
		Rules rules = algoMNR.runAlgorithm(output, minconf, results, database.size());
		rules.printRules(database.size());
	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException {
		URL url = MainTestMNRRules_saveToMemory.class.getResource(filename);
		return java.net.URLDecoder.decode(url.getPath(), "UTF-8");
	}
}
