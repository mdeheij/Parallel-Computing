// A Java program to print topological sorting of a DAG

import java.util.*;

// This class represents a directed graph using adjacency
// list representation
class Graph {
    private int numberOfVertices;   // No. of vertices
    private LinkedList<Integer> adjacencies[]; // Adjacency List
    private int[] inDegree;
    private int visitedVertices;

    //Constructor
    public Graph(int numberOfVertices) {
        this.numberOfVertices = numberOfVertices;
        adjacencies = new LinkedList[numberOfVertices];
        for (int i = 0; i < numberOfVertices; i++)
            adjacencies[i] = new LinkedList();
    }

    // Function to add an edge into the graph
    public void addEdge(int vertex, int target) {
        if (adjacencies.length > vertex && adjacencies.length > target) {
            adjacencies[vertex].add(target);
        } else {
            throw new RuntimeException("Either the vertex or the target for creation of a new edge was not found.");
        }
    }

    // A recursive function used by topologicalSort
    private void topologicalSortUtil(int vertex, boolean visited[],
                                     Stack stack) {
        // Mark the current node as visited.
        visited[vertex] = true;
        Integer i;

        // Recur for all the vertices adjacent to this
        // vertex
        Iterator<Integer> iterator = adjacencies[vertex].iterator();
        while (iterator.hasNext()) {
            i = iterator.next();
            if (!visited[i])
                topologicalSortUtil(i, visited, stack);
        }

        // Push current vertex to stack which stores result
        stack.push(new Integer(vertex));
    }

    // The function to do Topological Sort. It uses
    // recursive topologicalSortUtil()
    public Stack topologicalSort() {
        Stack stack = new Stack();

        // Mark all the vertices as not visited
        boolean visited[] = new boolean[numberOfVertices];
        for (int i = 0; i < numberOfVertices; i++) {
            visited[i] = false;
        }

        // Call the recursive helper function to store
        // Topological Sort starting from all vertices
        // one by one
        for (int i = 0; i < numberOfVertices; i++) {
            if (visited[i] == false) {
                topologicalSortUtil(i, visited, stack);
            }
        }
        return stack;
    }

    // prints a Topological Sort of the complete graph using Khan's algorithm
    public Stack topologicalSortKhan() {
        // Create a array to store in-degrees of all
        // vertices. Initialize all in-degrees as 0.
        int inDegree[] = new int[numberOfVertices];

        // Traverse adjacency lists to fill in-degrees of
        // vertices. This step takes O(V+E) time
        for (int i = 0; i < numberOfVertices; i++) {
            for (int node : adjacencies[i]) {
                inDegree[node]++;
            }
        }

        // Create a queue and enqueue all vertices with
        // in-degree 0
        Queue<Integer> queue = new LinkedList();
        for (int i = 0; i < numberOfVertices; i++) {
            if (inDegree[i] == 0)
                queue.add(i);
        }

        // Initialize count of visited vertices
        int visitedVertices = 0;

        // Create a stack to store result (A topological
        // ordering of the vertices)
        Stack topOrder = new Stack();
        while (!queue.isEmpty()) {
            // Extract front of queue (or perform dequeue)
            // and add it to topological order
            int u = queue.poll();
            topOrder.add(u);

            // Iterate through all its neighbouring nodes
            // of de-queued node u and decrease their in-degree
            // by 1
            for (int node : adjacencies[u]) {
                // If in-degree becomes zero, add it to queue
                if (--inDegree[node] == 0)
                    queue.add(node);
            }
            visitedVertices++;
        }

        // Check if there was a cycle
        if (visitedVertices != numberOfVertices) {
            throw new RuntimeException("A cycle was found in the Graph.");
        }

        return topOrder;
    }

    // prints a Topological Sort of the complete graph using a parallel implementation of Khan's algorithm
    public Stack topologicalSortKhanParallel(int amountOfThreads) {
        // Create a array to store in-degrees of all
        // vertices. Initialize all in-degrees as 0.
        inDegree = new int[numberOfVertices];
        int bound = numberOfVertices / amountOfThreads; // Rely on integer division to floor the result.

        // Create the initialization threads
        Thread[] initThreads = new Thread[amountOfThreads];
        for(int i = 0; i < amountOfThreads; i++) {
            if (i == 0) {
                initThreads[i] = new Thread(new InitializationRunnable(0, bound));
            } else if (i == amountOfThreads) {
                initThreads[i] = new Thread(new InitializationRunnable(i * bound, numberOfVertices));
            } else{
                initThreads[i] = new Thread(new InitializationRunnable(i * bound, i + 1 * bound));
            }
        }

        for(Thread thread: initThreads) {
            thread.start();
        }
        for(Thread thread: initThreads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        // Initialize count of visited vertices
        int visitedVertices = 0;

        // Create a queue and enqueue all vertices with
        // in-degree 0
        Queue<Integer> queue = new LinkedList();
        for (int i = 0; i < numberOfVertices; i++) {
            if (inDegree[i] == 0) {
                queue.add(i);
            }
        }

        // Create a stack to store result (A topological
        // ordering of the vertices)
        Stack topOrder = new Stack();
        while (!queue.isEmpty()) {
            // Extract front of queue (or perform dequeue)
            // and add it to topological order
            int u = queue.poll();
            topOrder.add(u);

            // Iterate through all its neighbouring nodes
            // of de-queued node u and decrease their in-degree
            // by 1
            for (int node : adjacencies[u]) {
                // If in-degree becomes zero, add it to queue
                if (decreaseInDegree(node) == 0) {
                    queue.add(node);
                }
            }
            visitedVertices++;
        }

        // Check if there was a cycle
        if (visitedVertices != numberOfVertices) {
            throw new RuntimeException("A cycle was found in the Graph.");
        }

        return topOrder;
    }

    class InitializationRunnable implements Runnable {
        private int lowerBound;
        private int upperBound;

        public InitializationRunnable(int lowerBound, int upperBound) {
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
        }

        public void run() {
            for (int i = lowerBound; i  < upperBound; i++) {
                for (int node: adjacencies[i]) {
                    increaseInDegree(node);
                }
            }
        }

    }

    private int decreaseInDegree(int nodeToDecrease) {
        inDegree[nodeToDecrease]--;
        return inDegree[nodeToDecrease];
    }

    private synchronized int increaseInDegree(int nodeToIncrease) {
        inDegree[nodeToIncrease]++;
        return inDegree[nodeToIncrease];
    }

    private void increaseVisitedVertices() {
        visitedVertices++;
    }

    private int getVisitedVertices() {
        return visitedVertices;
    }

}
