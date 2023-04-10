
import search.*;
import java.util.*;


// A* search

public class AStar<S, A> {
  static class Node<S, A> implements Comparable<Node<S, A>>{
    S state;
    A action;
    Node<S, A> parent;
    double pathCost;
    double heuristic;
    Node(S state, A action, double pathCost, Node<S, A> parent, double heuristic){
      this.state = state;
      this.action = action;
      this.pathCost = pathCost;
      this.parent = parent;
      this.heuristic = heuristic;
    }
    public int compareTo (Node<S, A> n){
      if (n.pathCost + n.heuristic > pathCost + heuristic){
        return -1;
      }
      else if (n.pathCost + n.heuristic == pathCost + heuristic) {
        return 0;
      }
      else{
        return 1;
      }
    }
  }

  public static <S, A> Solution<S, A> search(HeuristicProblem<S, A> prob) {
    /// Your implementation goes here.
    var node = new Node<S, A>(prob.initialState(), null, 0, null, Double.POSITIVE_INFINITY);
    var frontier = new PriorityQueue<Node<S, A>>();
    var explored = new HashSet<S>();
    var actionsInReverse = new Stack<A>();
    var actions = new ArrayList<A>();
    var cheapestCostFrontierNode = new HashMap<S, Node<S, A>>();

    frontier.add(node);
    cheapestCostFrontierNode.put(node.state, node);
    while (!frontier.isEmpty()) {
      var nodeBeingExpanded = frontier.poll();
      if (explored.contains(nodeBeingExpanded.state)){
        continue;
      }
      if (prob.isGoal(nodeBeingExpanded.state)){
        var currentNode = nodeBeingExpanded;
        while(currentNode.parent != null){
          actionsInReverse.push(currentNode.action);
          currentNode = currentNode.parent;
        }
        while(!actionsInReverse.isEmpty()) {
          actions.add(actionsInReverse.pop());
        }
        return new Solution<S, A>(actions, nodeBeingExpanded.state, nodeBeingExpanded.pathCost);

      }
      explored.add(nodeBeingExpanded.state);
      for (var nextAction : prob.actions(nodeBeingExpanded.state)){
        var nextState = prob.result(nodeBeingExpanded.state, nextAction);
        var child = new Node<S, A>(nextState, nextAction, nodeBeingExpanded.pathCost + prob.cost(nodeBeingExpanded.state, nextAction),nodeBeingExpanded, prob.estimate(nextState));
        if (!cheapestCostFrontierNode.containsKey(child.state) && !explored.contains(child.state)){
          frontier.add(child);
          cheapestCostFrontierNode.put(child.state, child);
        }
        else if (cheapestCostFrontierNode.get(child.state) == null ? false: cheapestCostFrontierNode.get(child.state).pathCost + cheapestCostFrontierNode.get(child.state).heuristic > child.pathCost + child.heuristic){
          frontier.add(child);
          cheapestCostFrontierNode.put(child.state, child);        
        }
      }
    }
    
    return null;
  }     
}
