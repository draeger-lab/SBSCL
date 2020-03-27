package fern.analysis;

import java.util.LinkedList;

import cern.colt.bitvector.BitVector;
import fern.analysis.NetworkSearchAction.NeighborType;
import fern.network.Network;
import fern.network.modification.ModifierNetwork;
import fern.tools.NumberTools;

/**
 * This class can be used as a base class for many analysis algorithms (like AutocatalticNetworkDetection, 
 * ShortestPath, ...). It creates adjacency lists for the molecule species (which are
 * usually not present in {@link Network} implementing classes) and presents methods for a
 * general breath first search and depth first search, which can be controlled by a {@link NetworkSearchAction}
 * 
 * @author 	Florian Erhard
 * @see 	NetworkSearchAction
 */
public class AnalysisBase {

	/**
	 * Contains the network.
	 */
	protected Network network 			= null;
	
	/**
	 * Contains the original network if <code>network</code> is a <code>ModifierNetwork</code>.
	 * Otherwise it contains also net <code>network</code>.
	 */
	protected Network originalNetwork 	= null;
	
	/**
	 * Contains the adjacency list for molecule species towards reactions, where the species
	 * is a reactant.
	 */ 
	protected int[][] adjListAsRea 		= null;
	
	/**
	 * Contains the adjacency list for molecule species towards reactions, where the species
	 * is a product.
	 */ 
	protected int[][] adjListAsPro 		= null;
	
	/**
	 * Creates an analysis instance. In order to do that, a {@link Network} is required.
	 * If the network is a {@link ModifierNetwork}, the original network is also
	 * discovered and stored.
	 *  
	 * @param network the network for analysis
	 */
	public AnalysisBase(Network network) {
		this.network = network;
		originalNetwork = network instanceof ModifierNetwork ? ((ModifierNetwork)network).getOriginalNetwork() : network;
	}
	
	/**
	 * Creates the adjacency lists for the molecule species. A subclass has to invoke this
	 * before it can use the protected fields adjListAsRea and adjListAsPro. 
	 */
	@SuppressWarnings("unchecked")
	protected void createSpeciesAdjacencyLists() {
		adjListAsPro = new int[network.getNumSpecies()][];
		adjListAsRea = new int[network.getNumSpecies()][];
		
		LinkedList<Integer>[] proCreate = new LinkedList[network.getNumSpecies()];
		for (int i=0; i<proCreate.length; i++) proCreate[i] = new LinkedList<Integer>();
		for (int i=0; i<network.getNumReactions(); i++)
			for (int j : network.getProducts(i))
				proCreate[j].add(i);
		LinkedList<Integer>[] reaCreate = new LinkedList[network.getNumSpecies()];
		for (int i=0; i<reaCreate.length; i++) reaCreate[i] = new LinkedList<Integer>();
		for (int i=0; i<network.getNumReactions(); i++)
			for (int j : network.getReactants(i))
				reaCreate[j].add(i);
		
		for (int i=0; i<proCreate.length; i++)
			adjListAsPro[i] = NumberTools.toIntArray(proCreate[i]);
		for (int i=0; i<reaCreate.length; i++)
			adjListAsRea[i] = NumberTools.toIntArray(reaCreate[i]);
	}
	
	/**
	 * Performs a breath first search starting at the given sources (which means the contents of <code>speciesSource</code>
	 * and <code>reactionSource</code> are the initial content of the queue. The search is controlled by an {@link NetworkSearchAction}.
	 * 
	 * @param speciesSource		indices of the species to start with
	 * @param reactionSource	indices of the reactions to start with
	 * @param action			controls what species/reactions have to be visited and what to do after discovering/finishing a species/reaction
	 * @return					number of visited species/reactions
	 */
	public int bfs(int[] speciesSource, int[] reactionSource, NetworkSearchAction action) {
		return search(new IntQueue(),speciesSource,reactionSource,action);
	}
	
	/**
	 * Performs a depth first search starting at the given sources (which means the contents of <code>speciesSource</code>
	 * and <code>reactionSource</code> are the initial content of the stack. The search is controlled by an {@link NetworkSearchAction}.
	 * 
	 * @param speciesSource		indices of the species to start with
	 * @param reactionSource	indices of the reactions to start with
	 * @param action			controls what species/reactions have to be visited and what to do after discovering/finishing a species/reaction
	 * @return					number of visited species/reactions
	 */
	public int dfs(int[] speciesSource, int[] reactionSource, NetworkSearchAction action) {
		return search(new IntStack(),speciesSource,reactionSource,action);
	}
	
	/**
	 * Performs a search starting at the given sources (which means the contents of <code>speciesSource</code>
	 * and <code>reactionSource</code> are the initial content of the search structure {@link IntSearchStructure}.
	 * The search is controlled by an {@link NetworkSearchAction}.
	 * 
	 * @param str				the search structure (fifo/lifo)
	 * @param speciesSource		indices of the species to start with
	 * @param reactionSource	indices of the reactions to start with
	 * @param action			controls what species/reactions have to be visited and what to do after discovering/finishing a species/reaction
	 * @return					number of visited species/reactions
	 * @see AnalysisBase#bfs(int[], int[], NetworkSearchAction)
	 * @see AnalysisBase#dfs(int[], int[], NetworkSearchAction)
	 */
	public int search(IntSearchStructure str, int[] speciesSource, int[] reactionSource, NetworkSearchAction action) {
		if (adjListAsPro==null) createSpeciesAdjacencyLists();
		
		action.initialize(this.network);
		
		BitVector reactionDiscovered = new BitVector(network.getNumReactions());
		BitVector speciesDiscovered = new BitVector(network.getNumSpecies());
		
		if (decode(network.getNumReactions())!=network.getNumReactions() ||
				decode(network.getNumSpecies())!=network.getNumSpecies())
			throw new IllegalArgumentException("Net is too big");
		
		int queueElement;
		int index;
		int count = 0;
		
		for (int i : speciesSource) {
			str.add(i);
			speciesDiscovered.set(i);
			action.speciesDiscovered(i);
		}
		
		for (int i : reactionSource) {
			str.add(encode(i));
			reactionDiscovered.set(i);
			action.reactionDiscovered(i);
		}
		
		while (!str.isEmpty()) {
			++count;
			queueElement = str.get();
			index = decode(queueElement);
			
			
			if (queueElement!=index) { // reaction
				action.reactionFinished(index);
				
				for (int n : network.getReactants(index))
					if (!speciesDiscovered.get(n) && action.checkSpecies(n,NeighborType.Reactant)){
						speciesDiscovered.set(n);
						str.add(n);
						action.speciesDiscovered(n);
					}
				for (int n : network.getProducts(index))
					if (!speciesDiscovered.get(n) && action.checkSpecies(n,NeighborType.Product)){
						speciesDiscovered.set(n);
						str.add(n);
						action.speciesDiscovered(n);
					}
				Iterable<Integer> additionals = action.getAdditionalReactionNeighbors(index);
				if (additionals!=null)
					for (int n : additionals)
						if (!speciesDiscovered.get(n) && action.checkSpecies(n,NeighborType.Additional)){
							speciesDiscovered.set(n);
							str.add(n);
							action.speciesDiscovered(n);
						}
				
			} else { // species
				action.speciesFinished(index);
				
				for (int n : adjListAsRea[index])
					if (!reactionDiscovered.get(n) && action.checkReaction(n, NeighborType.Reactant)){
						reactionDiscovered.set(n);
						str.add(encode(n));
						action.reactionDiscovered(n);
					}
				for (int n : adjListAsPro[index])
					if (!reactionDiscovered.get(n) && action.checkReaction(n, NeighborType.Product)){
						reactionDiscovered.set(n);
						str.add(encode(n));
						action.reactionDiscovered(n);
					}
				Iterable<Integer> additionals = action.getAdditionalSpeciesNeighbors(index);
				if (additionals!=null)
					for (int n : additionals) 
						if (!reactionDiscovered.get(n) && action.checkReaction(n, NeighborType.Additional)){
							reactionDiscovered.set(n);
							str.add(encode(n));
							action.reactionDiscovered(n);
						}
			}
		}
		
		action.finished();
		return count;
	}
	
	private int encode(int i) {
		return i | (1 << 31);
	}
	
	private int decode(int i) {
		return i & ~(1 << 31);
	}
	
	
	
}
