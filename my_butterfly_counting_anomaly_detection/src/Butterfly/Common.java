package butterfly;


import java.io.*;
import java.util.Map;


class Common {

    public static int[] parseEdge(String line, String delim) {

        String[] tokens = line.split(delim);
        int src = Integer.valueOf(tokens[0]);
        int dst = Integer.valueOf(tokens[1]);
        int sign = Integer.valueOf(tokens[2]);

        return new int[]{src, dst, sign};
    }

    public static void runBatch(Butterfly module, String inputPath, String delim) throws IOException {

        BufferedReader br = new BufferedReader(new FileReader(inputPath));

        int count = 0;

        while(true) {

            final String line = br.readLine();
            if(line == null) {
                break;
            }

            int[] edge = parseEdge(line, delim);
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
    }

    public static void writeOutputs(final Butterfly module, final String outputPath, final int trialNum) throws IOException {

        System.out.println("writing outputs...");

        File dir = new File(outputPath);
        try{
            dir.mkdir();
        }
        catch(Exception e){

        }

        BufferedWriter bw = new BufferedWriter(new FileWriter(outputPath + "/global" + trialNum + ".txt"));
        bw.write("Estimated Global Butterfly Count");
        bw.newLine();
        bw.write(String.valueOf(module.getGlobalButterfly()));
        bw.newLine();
        bw.close();

        bw = new BufferedWriter(new FileWriter(outputPath + "/local_src" + trialNum + ".txt"));
        Map<Integer, Double> localCounts = module.getLocalButterfly_S();
        bw.write("Node Id"+"\t"+"Estimated Local Butterfly Count in node partition src");
        bw.newLine();
        for(int node : localCounts.keySet()) {
            bw.write(node+"\t"+localCounts.get(node));
            bw.newLine();
        }
        bw.close();

        bw = new BufferedWriter(new FileWriter(outputPath + "/local_dst" + trialNum + ".txt"));
        localCounts = module.getLocalButterfly_D();
        bw.write("Node Id"+"\t"+"Estimated Local Butterfly Count in node partition dst");
        bw.newLine();
        for(int node : localCounts.keySet()) {
            bw.write(node+"\t"+localCounts.get(node));
            bw.newLine();
        }
        bw.close();

        System.out.println("done.");

    }



}
