package student;

import controllers.Spaceship;
import models.Edge;
import models.Node;
import models.NodeStatus;

import controllers.SearchPhase;
import controllers.RescuePhase;
import java.util.ArrayList;
import java.util.List;

/** An instance implements the methods needed to complete the mission. */
public class MySpaceship implements Spaceship {

	long startTime= System.nanoTime(); // start time of rescue phase

	/** The spaceship is on the location given by parameter state.
	 * Move the spaceship to Planet X and then return (with the spaceship is on
	 * Planet X). This completes the first phase of the mission.
	 * 
	 * If the spaceship continues to move after reaching Planet X, rather than
	 * returning, it will not count. If you return from this procedure while
	 * not on Planet X, it will count as a failure.
	 *
	 * There is no limit to how many steps you can take, but your score is
	 * directly related to how long it takes you to find Planet X.
	 *
	 * At every step, you know only the current planet's ID, the IDs of
	 * neighboring planets, and the strength of the signal from Planet X at
	 * each planet.
	 *
	 * In this rescuePhase,
	 * (1) In order to get information about the current state, use functions
	 * currentID(), neighbors(), and signal().
	 *
	 * (2) Use method onPlanetX() to know if you are on Planet X.
	 *
	 * (3) Use method moveTo(int id) to move to a neighboring planet with the
	 * given ID. Doing this will change state to reflect your new position.
	 */
	@Override
	public void search(SearchPhase state) {
		// TODO: Find the missing spaceship
		ArrayList<Integer> visited = new ArrayList<Integer>();
		distressWalk(state, visited);
	}

	/**The spaceship is on planet currentPlanet(u) given by State s.
	 * Visit every planet reachable along paths of unvisited planets from planet currentPlanet. 
	 * End with the spaceship standing on planet currentPlanet.
	 * Precondition: planet currentPlanet is unvisited. */
	private void dfsWalk(SearchPhase state, ArrayList<Integer> visited) {

		int currentPlanet = state.currentID();
		visited.add(currentPlanet);
		for (NodeStatus neighbor : state.neighbors()) {
			if (!visited.contains(neighbor.id())) {  //neighbor is unvisited
				state.moveTo(neighbor.id());
				dfsWalk(state, visited);
				if (state.onPlanetX()) return;
				state.moveTo(currentPlanet);
			}
		}
	}

	/**The spaceship is on planet currentPlanet(u) given by State s.
	 * Visit every planet reachable along paths of unvisited planets from planet currentPlanet. 
	 * End with the spaceship standing on planet currentPlanet. 
	 * When currentPlanet is Planet X, terminate.	 * 
	 * Precondition: planet currentPlanet is unvisited. */
	private void distressWalk(SearchPhase state, ArrayList<Integer> visited) {
		int currentPlanet = state.currentID();
		visited.add(currentPlanet);
		if (state.onPlanetX()) return;
		Heap<NodeStatus> best = Neighbors(state, visited);
		while (best.size()!=0) { 
			int neighbor = best.poll().id();
			if(!visited.contains(neighbor)) {//neighbor is unvisited
				state.moveTo(neighbor);
				distressWalk(state, visited);
				if (state.onPlanetX()) return;
				state.moveTo(currentPlanet);
			}
		}
	}

	/**Returns a heap of the neighbors of the current node*/
	private Heap<NodeStatus> Neighbors (SearchPhase state, ArrayList<Integer> visited) {
		Heap<NodeStatus> planets = new Heap<NodeStatus>(false);
		for (NodeStatus neighbor: state.neighbors()) {
			if (!visited.contains(neighbor.id())) {
				planets.add(neighbor, neighbor.signal());				
			}
		}
		return planets;
	}

	/** The spaceship is on the location given by state. Get back to Earth
	 * without running out of fuel and return while on Earth. Your ship can
	 * determine how much fuel it has left via method fuelRemaining().
	 * 
	 * In addition, each Planet has some gems. Passing over a Planet will
	 * automatically collect any gems it carries, which will increase your
	 * score; your objective is to return to earth successfully with as many
	 * gems as possible.
	 * 
	 * You now have access to the entire underlying graph, which can be accessed
	 * through parameter state. Functions currentNode() and earth() return Node
	 * objects of interest, and nodes() returns a collection of all nodes on the
	 * graph.
	 *
	 * Note: Use moveTo() to move to a destination node adjacent to your current
	 * node. */
	@Override
	public void rescue(RescuePhase state) {
		// TODO: Complete the rescue mission and collect gems
		ArrayList<Node> visited = new ArrayList<Node>();
		getGems2(state, visited);	
		
	}

	/**Traverses the shortest path to Earth from the current node.*/
	private void toEarth(RescuePhase state) {
		List<Node> path = Paths.minPath(state.currentNode(), state.earth());
		for (int i=1; i < path.size(); i = i+1) {
			state.moveTo(path.get(i));
			if (state.currentNode()==state.earth()) return;
		}
	}
	/**Returns a max-heap of nodes in the graph with the number of gems as the priority.*/
	private Heap<Node> HighGem (RescuePhase state) {
		Heap<Node> gemplanets = new Heap<Node>(false);
		for (Node gemboys: state.nodes()) {
			gemplanets.add(gemboys, gemboys.gems());
		}
		return gemplanets;
	}

	/**Traverses the path back to Earth while collecting gems from other planets in the graph.
	 * On its way to Earth, traverses shortest path to node with highest gem count and then to the node with the 
	 * second highest gem count and so on until it gets to a point where it only has enough fuel to return to Earth.
	 * At this point it traverses the shortest path back to Earth.*/
	private void getGems2(RescuePhase state, ArrayList <Node> visited) {
		Node k = HighGem(state).poll();
		while(state.fuelRemaining() > Paths.pathWeight(Paths.minPath(state.currentNode(), k))
				+ Paths.pathWeight(Paths.minPath(k, state.earth())) && HighGem(state).size()!=0) {
			long totalTime= System.nanoTime() - startTime;
			List<Node> path = Paths.minPath(state.currentNode(), k);
			for (int i=1; i < path.size(); i = i+1) {
				state.moveTo(path.get(i));
			}
			if (totalTime > 20 * 1000000000.0) toEarth(state);
			k = HighGem(state).poll();
		}
		toEarth(state);
	}
}
