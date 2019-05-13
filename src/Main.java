import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Scanner;

public class Main {

    // globals
    static Scanner scan = new Scanner(System.in);
    static BayesianNetwork bn;
    static Edge[][] vertexMatrix;
    static double[] flooding;
    static final double LEAKAGE = 0.001;
    static int globalInt = 0;

    public static void main(String[] args) {
        FileParser parser = new FileParser();
        parser.parse("file.txt");

        bn = new BayesianNetwork();
        buildNetwork();

        File outfile = new File("results.txt");

        try {
            FileWriter writer = new FileWriter(outfile);
            outfile.createNewFile();
            printToConsole(bn);
            printToFile(writer, bn);
            preProbabilityReasoning(writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void buildNetwork() {
        // create F,E nodes
        Node n;
        for (int i = 0; i < vertexMatrix.length; i++) {
            n = new F_node(bn.nodes.size(), i);
            bn.nodes.add(n);
            n = new E_node(bn.nodes.size(), i);
            bn.nodes.add(n);
        }

        // create B nodes
        for (int i = 0; i < vertexMatrix.length; i++)
            for (int j = 0; j < vertexMatrix.length; j++)
                if (vertexMatrix[i][j] != null) {
                    n = new B_node(bn.nodes.size(), i, j, vertexMatrix[i][j].weight, vertexMatrix[i][j].name);
                    bn.nodes.add(n);
                }

        // create connections
        for (int i = 0; i < bn.nodes.size(); i++)
            if (bn.nodes.get(i).type == 2) { // B
                // find parents and connect
                int v1 = ((B_node) bn.nodes.get(i)).vertex1;
                int v2 = ((B_node) bn.nodes.get(i)).vertex2;
                for (int j = 0; j < bn.nodes.size(); j++)
                    if (bn.nodes.get(j).type == 1
                            && (((F_node) bn.nodes.get(j)).vertex == v1 || ((F_node) bn.nodes.get(j)).vertex == v2)) {// parent
                        bn.nodes.get(j).children.add(bn.nodes.get(i));
                        bn.nodes.get(i).parents.add(bn.nodes.get(j));
                    }
                    // find children and connect
                    else if (bn.nodes.get(j).type == 3
                            && (((E_node) bn.nodes.get(j)).vertex == v1 || ((E_node) bn.nodes.get(j)).vertex == v2)) {// child
                        bn.nodes.get(j).parents.add(bn.nodes.get(i));
                        bn.nodes.get(i).children.add(bn.nodes.get(j));
                    }
            }

        // set probabilities for all nodes
        for (int i = 0; i < bn.nodes.size(); i++) {
            int numOfParents = (bn.nodes.get(i).parents == null) ? 0 : bn.nodes.get(i).parents.size();
            bn.nodes.get(i).probabilityTable = new double[(int) Math.pow(2, numOfParents)][numOfParents + 1];
        }

        for (int i = 0; i < bn.nodes.size(); i++) {
            if (bn.nodes.get(i).type == 1)// F
                bn.nodes.get(i).probabilityTable[0][0] = flooding[((F_node) bn.nodes.get(i)).vertex];
        }

        // _____________
        // |v1 v2 p |
        // |___________|
        // | T T - |
        // | T F - |
        // | F T - |
        // | F F - |
        // _____________

        for (int i = 0; i < bn.nodes.size(); i++) {
            if (bn.nodes.get(i).type == 2) {// B
                double w = ((B_node) bn.nodes.get(i)).weight;
                setTandF(bn.nodes.get(i).probabilityTable);
                int last = bn.nodes.get(i).probabilityTable[0].length - 1;
                for (int j = 0; j < bn.nodes.get(i).probabilityTable.length; j++) {
                    int sum = 0;
                    for (int k = 0; k < bn.nodes.get(i).probabilityTable[0].length - 1; k++)
                        sum += bn.nodes.get(i).probabilityTable[j][k];
                    if (sum == 0)
                        bn.nodes.get(i).probabilityTable[j][last] = LEAKAGE;
                    else if (sum == 1)
                        bn.nodes.get(i).probabilityTable[j][last] = calcProbB(w);
                    else if (sum == 2)
                        bn.nodes.get(i).probabilityTable[j][last] = 1 - ((1 - calcProbB(w)) * (1 - calcProbB(w)));
                    else
                        System.out.println("problem O_O");
                }
            }
        }

        for (int i = 0; i < bn.nodes.size(); i++) {
            if (bn.nodes.get(i).type == 3) {// E
                setTandF(bn.nodes.get(i).probabilityTable);
                int last = bn.nodes.get(i).probabilityTable[0].length - 1;
                for (int j = 0; j < bn.nodes.get(i).probabilityTable.length; j++) {// line
                    boolean isZero = true;
                    double noisyOr = 1;
                    for (int l = 0; l < bn.nodes.get(i).parents.size(); l++) {// parents
                        if (bn.nodes.get(i).probabilityTable[j][l] == 1) {
                            double w = ((B_node) bn.nodes.get(i).parents.get(l)).weight;// weight of parent edge
                            double pi;
                            if (w > 4)
                                pi = 0.8;
                            else
                                pi = 0.4;
                            noisyOr *= (1 - pi);
                            isZero = false;
                        }
                    }
                    if (isZero)
                        bn.nodes.get(i).probabilityTable[j][last] = LEAKAGE;
                    else
                        bn.nodes.get(i).probabilityTable[j][last] = 1 - noisyOr;
                }
            }
        }
    }

    private static double calcProbB(double w) {
        return 0.6 / w;
    }

    private static void setTandF(double[][] matrix) {
        for (int i = 0; i < matrix[0].length - 1; i++) {// [0,0] to [0,2]
            matrix[0][i] = 0;
        }
        for (int i = 1; i < matrix.length; i++) {
            matrix[i] = matrix[i - 1].clone();
            for (int j = 0; j < matrix[0].length - 1; j++) {
                if (matrix[i][j] == 0) {
                    matrix[i][j] = 1;
                    break;
                } else if (matrix[i][j] == 1) {
                    matrix[i][j] = 0;
                } else
                    System.out.println("problem O_O");

            }
        }
    }

    private static void printEvidence(LinkedList<Evidence> evidenceList) {
        System.out.println("Evidence List:");
        for (Evidence e : evidenceList) {
            if (e.type == 1) {// F
                if (e.isTrue)
                    System.out.println("\tvertex " + (((FloodingEvidence) e).vertex + 1) + " flooding");
                else
                    System.out.println("\tvertex " + (((FloodingEvidence) e).vertex + 1) + " not flooding");

            } else if (e.type == 2) {// B
                if (e.isTrue)
                    System.out.println("\tedge " + ((BlockageEvidence) e).edgeName + " blockage");
                else
                    System.out.println("\tedge " + ((BlockageEvidence) e).edgeName + " not blockage");

            } else if (e.type == 3) {// E
                if (e.isTrue)
                    System.out.println("\tvertex " + (((EvacueesEvidence) e).vertex + 1) + " evacuees");
                else
                    System.out.println("\tvertex " + (((EvacueesEvidence) e).vertex + 1) + " not evacuees");
            } else
                System.out.println("\tvertex type is not 1/2/3 ... strange O_O");

        }
        System.out.println("");

    }

    private static void printEvidenceToFile(FileWriter writer, LinkedList<Evidence> evidenceList) throws IOException {
        writer.write("Evidence List:");
        for (Evidence e : evidenceList) {
            if (e.type == 1) {// F
                if (e.isTrue)
                    writer.write("\tvertex " + (((FloodingEvidence) e).vertex + 1) + " flooding\n");
                else
                    writer.write("\tvertex " + (((FloodingEvidence) e).vertex + 1) + " not flooding\n");

            } else if (e.type == 2) {// B
                if (e.isTrue)
                    writer.write("\tedge " + ((BlockageEvidence) e).edgeName + " blockage\n");
                else
                    writer.write("\tedge " + ((BlockageEvidence) e).edgeName + " not blockage\n");

            } else if (e.type == 3) {// E
                if (e.isTrue)
                    writer.write("\tvertex " + (((EvacueesEvidence) e).vertex + 1) + " evacuees\n");
                else
                    writer.write("\tvertex " + (((EvacueesEvidence) e).vertex + 1) + " not evacuees\n");
            } else
                writer.write("\tvertex type is not 1/2/3 ... strange O_O\n");

        }
        writer.write("\n");

    }

    private static void topologicalSort(LinkedList<Node> topologicalList, BayesianNetwork copyBN) {
        for (Node node : copyBN.nodes)
            if (node.type == 1)// F
                topologicalList.add(node);
        for (Node node : copyBN.nodes)
            if (node.type == 2)// B
                topologicalList.add(node);
        for (Node node : copyBN.nodes)
            if (node.type == 3)// E
                topologicalList.add(node);
    }

    private static void preProbabilityReasoning(FileWriter writer) throws IOException {
        // copy bn is made in probabilityReasoning
        // evidence list
        LinkedList<Evidence> evidenceList = new LinkedList<Evidence>();

        // get inputs from user
        String input = "";
        while (!input.equals("q")) {

            System.out.println("\nPlease enter an action:");
            System.out.println("'e' for adding edge evidence\n'v' for adding vertex evidence\n'q' for quit");
            System.out.println("'pr' for Probability Reasoning");
            System.out.println("'reset' for reset evidence list");
            System.out.println("'printEv' for printing the evidence list");

            input = scan.next(); // while input!="quit"{}
            if (!input.equals("q")) {
                if (input.equals("v")) { // vertex
                    System.out.println("Please enter vertex number (starting from 1)");
                    int v = (scan.nextInt() - 1);
                    System.out.println("Please enter the number of the report:");
                    System.out.println("1 - Flooding");
                    System.out.println("2 - no Flooding");
                    System.out.println("3 - Evacuees");
                    System.out.println("4 - no Evacuees");
                    int n = scan.nextInt();
                    if (n == 1) {
                        Evidence e = new FloodingEvidence(true, v);
                        evidenceList.add(e);
                    }
                    if (n == 2) {
                        Evidence e = new FloodingEvidence(false, v);
                        evidenceList.add(e);
                    }
                    if (n == 3) {
                        Evidence e = new EvacueesEvidence(true, v);
                        evidenceList.add(e);
                    }
                    if (n == 4) {
                        Evidence e = new EvacueesEvidence(false, v);
                        evidenceList.add(e);
                    }
                } else if (input.equals("e")) { // edge
                    System.out.println("Please enter the edge name (example: 1)");
                    String e = scan.next();
                    System.out.println("Please enter the number of the report:");
                    System.out.println("1 - Blockage");
                    System.out.println("2 - no Blockage");
                    int n = scan.nextInt();
                    if (n == 1) {
                        Evidence ev = new BlockageEvidence(true, e);
                        evidenceList.add(ev);
                    }
                    if (n == 2) {
                        Evidence ev = new BlockageEvidence(false, e);
                        evidenceList.add(ev);
                    }

                } else if (input.equals("pr")) { // probability reasoning
                    printEvidence(evidenceList);
                    probabilisticReasoning(writer, evidenceList);
                } else if (input.equals("reset")) { // reset evidences and copyBN
                    evidenceList = new LinkedList<Evidence>();
                    writer.write("\nEvidence list was reset. Now empty!\n\n");
                } else if (input.equals("printEv")) { // print evidences
                    printEvidence(evidenceList);
                    printEvidenceToFile(writer, evidenceList);
                } else
                    System.out.println("Try again. We believe in you...");

            }

        }

    }

    private static void probabilisticReasoning(FileWriter writer, LinkedList<Evidence> evidenceList)
            throws IOException {

        BayesianNetwork copyBN = BayesianNetwork.fresh_copy(bn);
        LinkedList<Node> topologicalList = new LinkedList<Node>();
        topologicalSort(topologicalList, copyBN);

        int n;
        System.out.println("\nPlease enter an action:");
        System.out.println("1 for print probability for each of the vertices (enumeration)"); // print format P(A=t) ,
                                                                                              // P(A=f)
        System.out.println("2 for print posterior probabilities for each of the vertices ()"); // print format P(A| B, C
                                                                                               // ,...)
        System.out.println("3 for print probability of path");
        System.out.println("4 for print path with highest probability");

        n = scan.nextInt();
        if (n == 1) { // enumeration
            double t[];
            for (int i = 0; i < topologicalList.size(); i++) {
                t = enumerationAsk(topologicalList.get(i), evidenceList, copyBN,
                        (LinkedList<Node>) topologicalList.clone());
                if (topologicalList.get(i).type == 1) {
                    System.out.println("Vertex " + (((F_node) topologicalList.get(i)).vertex + 1) + ":");
                    System.out.println("\tP(Flooding=false)=" + t[0]);
                    System.out.println("\tP(Flooding=true)=" + t[1]);
                    writer.write("Vertex " + (((F_node) topologicalList.get(i)).vertex + 1) + ":\n");
                    writer.write("\tP(Flooding=false)=" + t[0] + "\n");
                    writer.write("\tP(Flooding=true)=" + t[1] + "\n");
                } else if (topologicalList.get(i).type == 2) {
                    System.out.println("Edge " + ((B_node) topologicalList.get(i)).name + ":");
                    System.out.println("\tP(Blocakge=false)=" + t[0]);
                    System.out.println("\tP(Blocakge=true)=" + t[1]);
                    writer.write("Edge " + ((B_node) topologicalList.get(i)).name + ":\n");
                    writer.write("\tP(Blocakge=false)=" + t[0] + "\n");
                    writer.write("\tP(Blocakge=true)=" + t[1] + "\n");
                } else if (topologicalList.get(i).type == 3) {
                    System.out.println("Vertex " + (((E_node) topologicalList.get(i)).vertex + 1) + ":");
                    System.out.println("\tP(Evacuees=false)=" + t[0]);
                    System.out.println("\tP(Evacuees=true)=" + t[1]);
                    writer.write("Vertex " + (((E_node) topologicalList.get(i)).vertex + 1) + ":\n");
                    writer.write("\tP(Evacuees=false)=" + t[0] + "\n");
                    writer.write("\tP(Evacuees=true)=" + t[1] + "\n");

                }
            }

        } else if (n == 2) { // posterior
            printEvidence(evidenceList);
            printEvidenceToFile(writer, evidenceList);
            for (int i = 0; i < topologicalList.size(); i++)
                if (evidenceContains(topologicalList.get(i), evidenceList) != -1) {
                    updateprobabilityTable(topologicalList.get(i), evidenceList, copyBN); // n in evidence
                }
            printToFile(writer, copyBN);
            printToConsole(copyBN);
        } else if (n == 3) {
            System.out.println(
                    "Please enter a path (set of vertexes) to get a probability it is free from blockages. example: 1,2,3");
            String s = scan.next();
            LinkedList<Integer> path = new LinkedList<Integer>();
            String[] array = s.split(",");
            for (String string : array) {
                path.add(Integer.parseInt(string));
            }
            double pathProbability = calcPathProbability(path, evidenceList, copyBN, topologicalList);
            if (pathProbability == 0)
                System.out.println("path is not valid!");
            else {
                System.out.println("Probability the path is free is: " + pathProbability);
                writer.write("Probability the path is free is: " + pathProbability + "\n");
            }
        } else if (n == 4) {// bonus
            int factorialOfVertex = factorial(vertexMatrix.length);
            int[][] result = new int[factorialOfVertex][vertexMatrix.length];
            int arr[] = new int[vertexMatrix.length];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = i + 1;
            }
            int size = arr.length;

            permute(arr, 0, size - 1, result);

            // for (int i = 0; i< result.length; i++) {
            // for (int j = 0; j< result[0].length; j++) {
            // System.out.print(result[i][j] + " ");
            // }
            // System.out.println(" ");
            // }

            // 5.What is the path from a given location to a goal that has the highest
            // probability of being free from blockages? (bonus)
            System.out.println(
                    "Please enter an init vertex (starting from 1) to get a path with highest probability to be free from blockages");
            int init = scan.nextInt();
            System.out.println(
                    "Please enter goal vertex (starting from 1) to get a path with highest probability to be free from blockages");
            int goal = scan.nextInt();

            LinkedList<Integer> bestPath = null;
            double bestPathProbability = 0;
            LinkedList<Integer> path;
            double pathProbability;

            for (int i = 0; i < result.length; i++) {
                if (result[i][0] == init) {
                    path = new LinkedList<Integer>();

                    for (int j = 0; j < result[i].length; j++) {
                        path.add(result[i][j]);
                        if (result[i][j] == goal)
                            break;
                    }
                    pathProbability = calcPathProbability(path, evidenceList, copyBN, topologicalList);
                    // System.out.println(pathProbability);
                    if (pathProbability > bestPathProbability) {
                        bestPathProbability = pathProbability;
                        bestPath = path;
                    }
                }
            }

            if (bestPathProbability == 0)
                System.out.println("There are no valid paths between the vertices. Sorry... ");
            else {
                System.out.print("The path with the highest probability (" + bestPathProbability + ") is: ");
                for (int i = 0; i < bestPath.size(); i++)
                    System.out.print(bestPath.get(i) + ",");
                System.out.println("");

                writer.write("The path with the highest probability (" + bestPathProbability + ") is: ");
                for (int i = 0; i < bestPath.size(); i++)
                    writer.write(bestPath.get(i) + ",");
                writer.write("\n");
            }
        } else
            System.out.println("You asked to type 1/2/3/4...");
    }

    private static int factorial(int n) {
        if (n == 0 || n == 1)
            return 1;
        return n * factorial(n - 1);
    }

    public static void swap(int arr[], int id1, int id2) {
        int temp;
        temp = arr[id1];
        arr[id1] = arr[id2];
        arr[id2] = temp;
    }

    public static void addPermute(int arr[], int size, int[][] result) {
        for (int i = 0; i <= size; i++) {
            result[globalInt][i] = arr[i];
        }
    }

    public static void permute(int arr[], int start_idx, int end_idx, int[][] result) {
        if (start_idx == end_idx) {
            addPermute(arr, end_idx, result);
            globalInt++;

        }
        for (int i = start_idx; i <= end_idx; i++) {
            swap(arr, start_idx, i);
            permute(arr, start_idx + 1, end_idx, result);
            swap(arr, start_idx, i);
        }
    }

    private static double calcPathProbability(LinkedList<Integer> path, LinkedList<Evidence> evidenceList,
            BayesianNetwork bNet, LinkedList<Node> topologicalList) {

        // for(int i=0;i<path.size();i++)
        // System.out.print(path.get(i)+" ");
        // System.out.println("");

        // check if the path exists
        boolean validPath = true;
        for (int i = 0; i < path.size() - 1; i++) {
            if (vertexMatrix[path.get(i) - 1][path.get(i + 1) - 1] == null
                    && vertexMatrix[path.get(i + 1) - 1][path.get(i) - 1] == null) {
                // System.out.println("path not exists");
                return 0;
            }
        }
        LinkedList<Evidence> updatedEvidenceList = cloneEvidenceList(evidenceList);
        BayesianNetwork copyBN = BayesianNetwork.fresh_copy(bNet);
        LinkedList<Node> copyTopologicalList = (LinkedList<Node>) topologicalList.clone();
        double pathProbability = 1;

        for (int i = 0; i < path.size() - 1; i++) { // i - index in path
            String name;
            if (path.get(i) < path.get(i + 1))
                name = vertexMatrix[path.get(i) - 1][path.get(i + 1) - 1].name;
            else
                name = vertexMatrix[path.get(i + 1) - 1][path.get(i) - 1].name;

            int inEvidence = -1;
            Node currNode = null;
            for (Node node : copyBN.nodes) {
                if (node.type == 2 && ((B_node) node).name.equals(name)) { // find edge in bn
                    currNode = node;
                    inEvidence = evidenceContains(node, updatedEvidenceList); // look for edge in evidence
                }
            }
            if (inEvidence == 1) { // blockage
                // System.out.println("path contains edge with prob 0");
                // pathProbability=0;
                return 0;
            } else if (inEvidence == -1) {
                double currentStepProbability = 0;
                double t[];
                for (int j = 0; j < topologicalList.size(); j++) {
                    if (topologicalList.get(j).type == 2 && ((B_node) topologicalList.get(j)).name.equals(name)) {
                        t = enumerationAsk(topologicalList.get(j), (LinkedList<Evidence>) updatedEvidenceList.clone(),
                                BayesianNetwork.fresh_copy(copyBN), (LinkedList<Node>) topologicalList.clone());
                        currentStepProbability = t[0];
                        break;
                    }

                }
                pathProbability *= currentStepProbability;

            }

            // add not blockage of the edge to evidence list and update probability tables
            // (to fit the chain rule)
            BlockageEvidence bEv = new BlockageEvidence(false, name);
            updatedEvidenceList.add(bEv);
        }
        return pathProbability;
    }

    private static double calcPathProbabilityBonus(LinkedList<Integer> currPathCopy, int init, int goal,
            LinkedList<Evidence> evidenceList, BayesianNetwork bNet, LinkedList<Node> topologicalList) {
        LinkedList<Integer> fullPath = new LinkedList<Integer>();
        fullPath = (LinkedList<Integer>) currPathCopy.clone();
        fullPath.addFirst(init);
        fullPath.addLast(goal);
        return calcPathProbability(fullPath, evidenceList, bNet, topologicalList);
    }

    private static LinkedList<Evidence> cloneEvidenceList(LinkedList<Evidence> original) {
        LinkedList<Evidence> freshCopy = new LinkedList<Evidence>();
        for (Evidence evidence : original) {
            Evidence newEvidence = null;
            if (evidence.type == 1)
                newEvidence = new FloodingEvidence(evidence.isTrue, ((FloodingEvidence) evidence).vertex);
            else if (evidence.type == 2)
                newEvidence = new BlockageEvidence(evidence.isTrue, ((BlockageEvidence) evidence).edgeName);
            else
                newEvidence = new EvacueesEvidence(evidence.isTrue, ((EvacueesEvidence) evidence).vertex);

            freshCopy.add(newEvidence);
        }
        return freshCopy;
    }

    private static LinkedList<Evidence> shallowCopyEvidenceList(LinkedList<Evidence> original) {
        LinkedList<Evidence> freshCopy = new LinkedList<Evidence>();
        for (Evidence evidence : original) {
            freshCopy.add(evidence);
        }
        return freshCopy;
    }

    private static int evidenceContains(Node node, LinkedList<Evidence> evidenceList) {
        for (Evidence e : evidenceList) {
            if (e.type == 1 && node.type == 1)// F
                if (((FloodingEvidence) e).vertex == ((F_node) node).vertex) {
                    if (e.isTrue)
                        return 1;
                    else
                        return 0;
                }
            if (e.type == 2 && node.type == 2)// B
                if (((BlockageEvidence) e).edgeName.equals(((B_node) node).name))
                    if (e.isTrue)
                        return 1;
                    else
                        return 0;
            if (e.type == 3 && node.type == 3)// E
                if (((EvacueesEvidence) e).vertex == ((E_node) node).vertex)
                    if (e.isTrue)
                        return 1;
                    else
                        return 0;
        }

        return -1;
    }

    private static void updateprobabilityTable(Node node, LinkedList<Evidence> evidenceList, BayesianNetwork copyBN) {
        // printEvidence(evidenceList);
        // printToConsole(copyBN);
        for (Evidence e : evidenceList) {
            if (e.type == 1 && node.type == 1) {// F
                if (((FloodingEvidence) e).vertex == ((F_node) node).vertex) {
                    int flooding = 0;
                    if (e.isTrue) {
                        flooding = 1;
                    }
                    // update children
                    for (Node child : copyBN.nodes) {
                        if (child.type == 2 && node.children.contains(child)) {// is a child
                            int parentIndexInParents = child.parents.indexOf(node);
                            LinkedList<Integer> linesToDelete = new LinkedList<Integer>();
                            for (int i = 0; i < child.probabilityTable.length; i++)
                                if (child.probabilityTable[i][parentIndexInParents] == (1 - flooding))
                                    linesToDelete.add(i);
                            // create new table without this lines
                            double newTable[][] = new double[child.probabilityTable.length
                                    - linesToDelete.size()][child.probabilityTable[0].length - 1];
                            int nextLineToFill = 0;
                            for (int j = 0; j < child.probabilityTable.length; j++)
                                if (!linesToDelete.contains(j)) {
                                    int nextColumnToFill = 0;
                                    for (int k = 0; k < child.probabilityTable[0].length; k++) {
                                        if (k < parentIndexInParents || k > parentIndexInParents) {
                                            newTable[nextLineToFill][nextColumnToFill] = child.probabilityTable[j][k];
                                            nextColumnToFill++;
                                        }
                                    }
                                    nextLineToFill++;
                                }
                            child.probabilityTable = newTable;
                            child.parents.remove(parentIndexInParents);
                        }
                    }
                    // delete node
                    node.children.clear();
                    for (int i = 0; i < copyBN.nodes.size(); i++)
                        if (copyBN.nodes.get(i) == node)
                            copyBN.nodes.remove(i);
                }
            }
            if (e.type == 2 && node.type == 2) {// B
                if (((BlockageEvidence) e).edgeName.equals(((B_node) node).name)) {
                    int blockage = 0;
                    if (e.isTrue) {
                        blockage = 1;
                    }
                    // update children
                    for (Node child : copyBN.nodes) {
                        if (child.type == 3 && node.children.contains(child)) {// is a child
                            int parentIndexInParents = child.parents.indexOf(node);
                            LinkedList<Integer> linesToDelete = new LinkedList<Integer>();
                            for (int i = 0; i < child.probabilityTable.length; i++)
                                if (child.probabilityTable[i][parentIndexInParents] == (1 - blockage))
                                    linesToDelete.add(i);
                            // create new table without this lines
                            double newTable[][] = new double[child.probabilityTable.length
                                    - linesToDelete.size()][child.probabilityTable[0].length - 1];
                            int nextLineToFill = 0;
                            for (int j = 0; j < child.probabilityTable.length; j++)
                                if (!linesToDelete.contains(j)) {
                                    int nextColumnToFill = 0;
                                    for (int k = 0; k < child.probabilityTable[0].length; k++) {
                                        if (k < parentIndexInParents || k > parentIndexInParents) {
                                            newTable[nextLineToFill][nextColumnToFill] = child.probabilityTable[j][k];
                                            nextColumnToFill++;
                                        }
                                    }
                                    nextLineToFill++;
                                }
                            child.probabilityTable = newTable;
                            child.parents.remove(parentIndexInParents);
                        }
                    }
                    node.children.clear();
                    // delete pointers to node from its parents
                    for (Node parent : copyBN.nodes)
                        if (parent.type == 1 && node.parents.contains(parent)) {// is a parent
                            // find node's index in parent's children
                            int childIndexInChildren = parent.children.indexOf(node);
                            parent.children.remove(childIndexInChildren);
                        }
                    node.parents.clear();
                    // delete node
                    for (int i = 0; i < copyBN.nodes.size(); i++)
                        if (copyBN.nodes.get(i) == node)
                            copyBN.nodes.remove(i);
                }
            }
            if (e.type == 3 && node.type == 3)// E
                if (((EvacueesEvidence) e).vertex == ((E_node) node).vertex) {
                    for (Node parent : copyBN.nodes)
                        if (parent.type == 2 && node.parents.contains(parent)) {// is a parent
                            // find node's index in parent's children
                            int childIndexInChildren = parent.children.indexOf(node);
                            parent.children.remove(childIndexInChildren);
                        }
                    node.parents.clear();
                    // delete node
                    for (int i = 0; i < copyBN.nodes.size(); i++)
                        if (copyBN.nodes.get(i) == node)
                            copyBN.nodes.remove(i);
                }
        }
    }

    private static void printToFile(FileWriter writer, BayesianNetwork bays) throws IOException {
        writer.write("~~~~~~~~~~~~~~~~~~~~~~~~~~~~ start ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
        int lastNode = -1;
        for (Node node : bays.nodes) {
            if (node.type == 1) {
                writer.write("VERTEX = " + (((F_node) node).vertex + 1) + ":\n");
                double[][] table = node.probabilityTable;
                writer.write("    P(Flooding) = " + table[0][0] + "\n");
                writer.write("    P(not Flooding) = " + (1 - table[0][0]) + "\n");
                lastNode = ((F_node) node).vertex + 1;
            }

            if (node.type == 3) {

                if (((E_node) node).vertex + 1 != lastNode)
                    writer.write("VERTEX = " + (((E_node) node).vertex + 1) + ":\n");

                double[][] table = node.probabilityTable;
                for (int i = 0; i < table.length; i++) { // table rows
                    writer.write("    P(Evacuees| ");
                    for (int j = 0; j < table[i].length - 1; j++) { // table columns
                        if (table[i][j] == 0)
                            writer.write("not ");
                        writer.write("Blockage " + ((B_node) node.parents.get(j)).name);
                        if (j < table.length - 3)
                            writer.write(", ");
                    }
                    writer.write(") = " + table[i][table[i].length - 1] + "\n");

                    writer.write("    P(not Evacuees| ");
                    for (int j = 0; j < table[i].length - 1; j++) { // table columns
                        if (table[i][j] == 0)
                            writer.write("not ");
                        writer.write("Blockage " + ((B_node) node.parents.get(j)).name);
                        if (j < table.length - 3)
                            writer.write(", ");
                    }
                    writer.write(") = " + (1 - table[i][table[i].length - 1]) + "\n");
                }
            }
        }

        for (Node node : bays.nodes) {
            if (node.type == 2) {
                writer.write("EDGE = " + ((B_node) node).name + ":\n");
                double[][] table = node.probabilityTable;
                for (int i = 0; i < table.length; i++) { // table rows
                    writer.write("    P(Blocakge| ");
                    for (int j = 0; j < table[i].length - 1; j++) { // table columns
                        if (table[i][j] == 0)
                            writer.write("not ");
                        writer.write("flooding " + (((F_node) node.parents.get(j)).vertex + 1));
                        if (j < table.length - 3)
                            writer.write(", ");
                    }
                    writer.write(") = " + table[i][table[i].length - 1] + "\n");

                    writer.write("    P(not Blocakge| ");
                    for (int j = 0; j < table[i].length - 1; j++) { // table columns
                        if (table[i][j] == 0)
                            writer.write("not ");
                        writer.write("flooding " + (((F_node) node.parents.get(j)).vertex + 1));
                        if (j < table.length - 3)
                            writer.write(", ");
                    }
                    writer.write(") = " + (1 - table[i][table[i].length - 1]) + "\n");
                }

            }
        }
        writer.write("~~~~~~~~~~~~~~~~~~~~~~~~~~~~ end ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~`\n\n");
    }

    private static void printToConsole(BayesianNetwork bays) {
        System.out.print("~~~~~~~~~~~~~~~~~~~~~~~~~~~~ start ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
        int lastNode = -1;
        for (Node node : bays.nodes) {
            if (node.type == 1) {
                System.out.print("VERTEX = " + (((F_node) node).vertex + 1) + ":\n");
                double[][] table = node.probabilityTable;
                System.out.printf("    P(Flooding) = " + "%.4f" + "\n", table[0][0]);
                System.out.printf("    P(not Flooding) = " + "%.4f" + "\n", (1 - table[0][0]));
                lastNode = ((F_node) node).vertex + 1;
            }

            if (node.type == 3) {
                if (((E_node) node).vertex + 1 != lastNode)
                    System.out.print("VERTEX = " + (((E_node) node).vertex + 1) + ":\n");

                double[][] table = node.probabilityTable;
                for (int i = 0; i < table.length; i++) { // table rows
                    System.out.printf("    P(Evacuees| ");
                    for (int j = 0; j < table[i].length - 1; j++) { // table columns
                        if (table[i][j] == 0)
                            System.out.printf("not ");
                        System.out.printf("Blockage " + ((B_node) node.parents.get(j)).name);
                        if (j < table.length - 3)
                            System.out.printf(", ");
                    }
                    System.out.printf(") = " + "%.4f" + "\n", table[i][table[i].length - 1]);

                    System.out.printf("    P(not Evacuees| ");
                    for (int j = 0; j < table[i].length - 1; j++) { // table columns
                        if (table[i][j] == 0)
                            System.out.printf("not ");
                        System.out.printf("Blockage " + ((B_node) node.parents.get(j)).name);
                        if (j < table.length - 3)
                            System.out.printf(", ");
                    }
                    System.out.printf(") = " + "%.4f" + "\n", (1 - table[i][table[i].length - 1]));
                }
            }
        }

        for (Node node : bays.nodes) {
            if (node.type == 2) {
                System.out.printf("EDGE = " + ((B_node) node).name + ":\n");
                double[][] table = node.probabilityTable;
                for (int i = 0; i < table.length; i++) { // table rows
                    System.out.printf("    P(Blocakge| ");
                    for (int j = 0; j < table[i].length - 1; j++) { // table columns
                        if (table[i][j] == 0)
                            System.out.printf("not ");
                        System.out.printf("flooding " + (((F_node) node.parents.get(j)).vertex + 1));
                        if (j < table.length - 3)
                            System.out.printf(", ");
                    }
                    System.out.printf(") = " + "%.4f" + "\n", table[i][table[i].length - 1]);

                    System.out.printf("    P(not Blocakge| ");
                    for (int j = 0; j < table[i].length - 1; j++) { // table columns
                        if (table[i][j] == 0)
                            System.out.printf("not ");
                        System.out.printf("flooding " + (((F_node) node.parents.get(j)).vertex + 1));
                        if (j < table.length - 3)
                            System.out.printf(", ");
                    }
                    System.out.printf(") = " + "%.4f" + "\n", (1 - table[i][table[i].length - 1]));
                }

            }
        }
        System.out.printf("~~~~~~~~~~~~~~~~~~~~~~~~~~~~ end ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~`\n\n");
    }

    private static double[] normalize(double[] arr) {
        double sum = 0;
        for (int i = 0; i < arr.length; i++)
            sum += arr[i];
        for (int i = 0; i < arr.length; i++)
            arr[i] /= sum;
        return arr;
    }

    private static double[] enumerationAsk(Node X, LinkedList<Evidence> evidenceList, BayesianNetwork bnetwork,
            LinkedList<Node> topologicalList) {
        double[] q = new double[2];
        int index = evidenceContains(X, evidenceList);

        if (index != -1) {
            // updateprobabilityTable(X,evidenceList,bnetwork); //X in evidence
            if (index == 0) {// false
                q[0] = 1;
                q[1] = 0;
            }
            if (index == 1) { // true
                q[0] = 0;
                q[1] = 1;
            }
            return q;
        }

        LinkedList<Evidence> copyE1 = new LinkedList<Evidence>();
        copyE1 = shallowCopyEvidenceList(evidenceList);
        copyE1.add(evidenceForNode(X, false));
        q[0] = enumerateAll((LinkedList<Node>) topologicalList.clone(), copyE1);

        LinkedList<Evidence> copyE2 = new LinkedList<Evidence>();
        copyE2 = shallowCopyEvidenceList(evidenceList);
        copyE2.add(evidenceForNode(X, true));
        q[1] = enumerateAll((LinkedList<Node>) topologicalList.clone(), copyE2);
        return normalize(q);
    }

    private static double enumerateAll(LinkedList<Node> vars, LinkedList<Evidence> evidenceList) {
        if (vars.isEmpty())
            return 1;
        Node Y = vars.removeFirst();
        int row = calculateRowInTalbeByEvidence(evidenceList, Y);
        double[][] table = Y.probabilityTable;

        int isContains = evidenceContains(Y, evidenceList);
        if (isContains != -1) { // contains
            if (isContains == 0) { // y=false
                LinkedList<Evidence> copyE = new LinkedList<Evidence>();
                copyE = shallowCopyEvidenceList(evidenceList);
                return (1 - table[row][table[row].length - 1]) * enumerateAll((LinkedList<Node>) vars.clone(), copyE);
            } else { // t=true
                LinkedList<Evidence> copyE = new LinkedList<Evidence>();
                copyE = shallowCopyEvidenceList(evidenceList);
                return (table[row][table[row].length - 1]) * enumerateAll((LinkedList<Node>) vars.clone(), copyE);
            }
        }

        double sum = 0;
        LinkedList<Evidence> copyE1 = new LinkedList<Evidence>();
        copyE1 = shallowCopyEvidenceList(evidenceList);
        copyE1.add(evidenceForNode(Y, false));
        sum += ((1 - table[row][table[row].length - 1]) * enumerateAll((LinkedList<Node>) vars.clone(), copyE1));

        LinkedList<Evidence> copyE2 = new LinkedList<Evidence>();
        copyE2 = shallowCopyEvidenceList(evidenceList);
        copyE2.add(evidenceForNode(Y, true));
        sum += ((table[row][table[row].length - 1]) * enumerateAll((LinkedList<Node>) vars.clone(), copyE2));

        return sum;
    }

    private static Evidence evidenceForNode(Node node, boolean value) {
        if (node.type == 1)
            return new FloodingEvidence(value, ((F_node) node).vertex);
        if (node.type == 2)
            return new BlockageEvidence(value, ((B_node) node).name);
        else
            return new EvacueesEvidence(value, ((E_node) node).vertex);

    }

    private static int calculateRowInTalbeByEvidence(LinkedList<Evidence> evidence, Node Y) {
        int row = 0b0; // row in table

        if (Y.type == 1) {
            return 0;
        } else if (Y.type == 2) {
            for (Node n : Y.parents) {
                F_node p = (F_node) n;
                for (Evidence ev : evidence) {
                    if (ev.type == 1) {
                        int evVer = ((FloodingEvidence) ev).vertex;
                        if (p.vertex == evVer) {
                            if (ev.isTrue) {
                                row = row * 0b10 + 0b1;
                            } else {
                                row = row * 0b10 + 0b0;
                            }
                        }
                    }
                }
            }
        } else if (Y.type == 3) {
            for (Node n : Y.parents) {
                B_node p = (B_node) n;
                for (Evidence ev : evidence) {
                    if (ev.type == 2) {
                        String evName = ((BlockageEvidence) ev).edgeName;
                        if (evName.equals(p.name)) {
                            if (ev.isTrue) {
                                row = row * 0b10 + 0b1;
                            } else {
                                row = row * 0b10 + 0b0;
                            }
                        }
                    }
                }
            }
        }

        return row;
    }
}