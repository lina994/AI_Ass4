import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class FileParser {

    public void parse(String fileName) {
        File file = new File("file.txt");
        try (BufferedReader br = new BufferedReader(new FileReader(file));) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.equals("") && line.charAt(0) == '#') {
                    switch (line.charAt(1)) {
                    case 'T':
                        parseNumOfVertex(line.substring(3));
                        break;
                    case 'E':
                        parseEdge(line.substring(2));
                        break;
                    case 'V':
                        parseVertex(line.substring(3));
                        break;
                    }
                }

            }
        } catch (IOException e) {
            System.out.println("Unable to open file");
            e.printStackTrace();
        } finally {
            System.out.println("***read of file is completed***");
        }
    }

    private void parseNumOfVertex(String input) { // #T 4
        String[] array = input.split(" ");
        String s = array[0];
        int numOfVertex = Integer.parseInt(s);
        Main.flooding = new double[numOfVertex];
        Main.vertexMatrix = new Edge[numOfVertex][numOfVertex];
    }

    private void parseEdge(String input) {// #E4 2 4 W4
        String[] array = input.split(" ");
        String vertexName = array[0];
        int firstVertexIndex = Integer.parseInt(array[1]);
        int secondVertexIndex = Integer.parseInt(array[2]);
        float vertexWeight = 0;

        if (array[3].charAt(0) != 'W' || array[3].length() < 2)
            System.out.println("bad file syntax: " + array[3]);
        else
            vertexWeight = Float.parseFloat(array[3].substring(1));

        if (firstVertexIndex < secondVertexIndex)
            Main.vertexMatrix[firstVertexIndex - 1][secondVertexIndex - 1] = new Edge(vertexWeight, vertexName);
        else
            Main.vertexMatrix[secondVertexIndex - 1][firstVertexIndex - 1] = new Edge(vertexWeight, vertexName);
    }

    private void parseVertex(String input) { // #V 2 F 0.4
        String[] array = input.split(" ");
        int vertexIndex = Integer.parseInt(array[0]);
        if (array[1].charAt(0) == 'F')
            Main.flooding[vertexIndex - 1] = Double.parseDouble(array[2]);
        else
            System.out.println("bad file syntax: " + array[1]);

    }
}
