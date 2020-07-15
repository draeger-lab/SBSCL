package fern.example;

import java.io.File;

/**
 * Determine the path to the example network files.
 * 
 * @author Florian Erhard
 *
 */
public class ExamplePath {

	public static File find(String exampleFile) {
		File[] poss = new File[] {
				new File("fern/example/data/"+exampleFile),
				new File("examples/"+exampleFile),
				new File("../examples/"+exampleFile),
				new File(exampleFile),
		};
		for(File f : poss)
			if (f.exists()) return f;
		throw new IllegalArgumentException("Could not find example folder! Try customizing the class fern.example.ExamplePath!");
	}
	
	public static boolean exists(String exampleFile) {
		File[] poss = new File[] {
				new File("fern/example/data/"+exampleFile),
				new File("examples/"+exampleFile),
				new File("../examples/"+exampleFile),
				new File(exampleFile),
		};
		for(File f : poss)
			if (f.exists()) return true;
		return false;
	}
	
}
