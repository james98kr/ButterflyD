package butterfly;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public class ExampleAcc {

    public static void main(String[] ar) throws IOException {

        final String dataPath = "example_graph.txt";
        final String delim = "\t";
        final Butterfly module = new ButterflyAcc(72000, 0);

        BufferedReader br = new BufferedReader(new FileReader(dataPath));

        int count = 0;
        while(true) {

            final String line = br.readLine();
            if(line == null) {
                break;
            }

            int[] edge = Common.parseEdge(line, delim);
            if(edge[2] > 0) {
                module.processAddition(edge[0], edge[1]);
            }
            else {
                module.processDeletion(edge[0], edge[1]);
            }
            if((++count) % 10000 == 0) {
                System.out.println("Number of Elements Processed: " + count +", Estimated Number of Global Butterflies: " + module.getGlobalButterfly());
            }
        }

        br.close();

        return;
    }


}
