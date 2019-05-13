# Hurricane Evacuation Problem: Locate the Blockages and Evacuees

Probabilistic reasoning using Bayes networks, with scenarios similar to the hurricane evacuation problem environment of [assignment 1](https://github.com/lina994/AI_Ass1 "assignment description").

Using the following principles:

* Artificial Intelligence:
    * Reasoning under uncertainty
    * Bayesian Networks
    * Enumeration (Enumeration Ask and Enumerate All)
    * Probability Reasoning
* Topological sort
* OOP, Files I/O


## Domain Description

* Binary-valued occupation of vertices (contains people to be evacuated, or not).
* Binary random variable Fl(v) standing in for "flooding" at vertex v.
* Binary random variable Ev(v) standing in for "people to evacuate" at each vertex v.
* Binary variable B(e) standing in for "blocked" for each edge e.
* The flooding events are assumed independent, with known distributions.
* The blockages are noisy-or distributed given the flooding at incident vertices, with
    * pi  = (1 - qi) = 0.6 * 1/w(e).

* There are people at vertex v, with noisy or distributions given all the edge blockages at all edges incident on v, with:
    * pi = (1 - qi) = 0.8   for an edge with weight greater then 4.
    * pi = (1 - qi) = 0.4   for shorter edges.
* All noisy-or node have a leakage probability of 0.001, that is, they are true with probability 0.001 when all the causes are inactive.

### types of BN nodes

* Blockages (one for each edge).
* Flooding (one for each vertex).
* Evacuees present (one for each vertex).

## Running

### Input

file.txt file include graph description and parameters such as P(Fl(v)=true))
For example:

    #T 4           ; number of vertices n in graph (from 1 to n)
    #V 1 F 0.2     ; Vertex 1, probability flooding 0.2
    #V 2 F 0.4     ; Vertex 2, probability flooding 0.4
    #E1 1 2 W1     ; Edge1 between vertices 1 and 2, weight 1
    #E2 2 3 W3     ; Edge2 between vertices 2 and 3, weight 3
    #E3 3 4 W3     ; Edge3 between vertices 3 and 4, weight 3
    #E4 2 4 W4     ; Edge4 between vertices 2 and 4, weight 4

<br>

### Graph visualization

![graph](https://github.com/lina994/AI_Ass4/blob/master/resources/input_example.png?raw=true "graph")


We will get the following Bayesian network:

![graph](https://github.com/lina994/AI_Ass4/blob/master/resources/bayesian_network.png?raw=true "Bayesian network")

<br>

Additional input will be provided by the user via the terminal:

* Locations where flooding, blockages, or evacuees are reported either present or absent.
* Reset evidence list to empty.

Once evidence is instantiated, you can perform reasoning about:

* Probability that each of the vertices contains evacuees.
* Probability that each of the vertices is flooded.
* Probability that each of the edges is blocked.
* Probability that a certain path is free from blockages.
* Path from a given location to a goal that has the highest probability of being free from blockages.

For example:

    Please enter an action:
    'e' for adding edge evidence
    'v' for adding vertex evidence
    'q' for quit
    'pr' for Probability Reasoning
    'reset' for reset evidence list
    'printEv' for printing the evidence list
    > v
    Please enter vertex number (starting from 1)
    > 1
    Please enter the number of the report:
    1 - Flooding
    2 - no Flooding
    3 - Evacuees
    4 - no Evacuees
    > 1
    Please enter an action:
    'e' for adding edge evidence
    'v' for adding vertex evidence
    'q' for quit
    'pr' for Probability Reasoning
    'reset' for reset evidence list
    'printEv' for printing the evidence list
    > pr
    Please enter an action:
    1 for print probability for each of the vertices (enumeration)
    2 for print posterior probabilities for each of the vertices ()
    3 for print probability of path
    4 for print path with highest probability
    > 1
    ...
    Please enter an action:
    'e' for adding edge evidence
    'v' for adding vertex evidence
    'q' for quit
    'pr' for Probability Reasoning
    'reset' for reset evidence list
    'printEv' for printing the evidence list
    > q

### Output

* Detailed output will be displayed in the terminal
* A summary will be saved in the results.txt  file


## Authors

* Alina
    * [github](https://github.com/lina994 "github")
* Elina
    * [github](https://github.com/ElinaS21 "github")


## Official assignment description
[assignment 4](https://www.cs.bgu.ac.il/~shimony/AI2019/AIass4_2019.html "assignment description")


