package butterfly;


import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;


public class BatchSpot {

    public static void main(String[] args) throws IOException {

        if(args.length < 4) {
            printError();
            System.exit(-1);
        }

        final String inputPath = args[0];
        System.out.println("input_path: " + inputPath);
        final String outputPath = args[1];
        System.out.println("output_path: " + outputPath);
        final int memoryBudget = Integer.valueOf(args[2]);
        System.out.println("memory_budget: " + memoryBudget);
        final int timeWindow = Integer.valueOf(args[3]);
        System.out.println("time_window: " + timeWindow);


        System.out.println("start running Butterfly-Spot...");

        final ButterflySpot module = new ButterflySpot(memoryBudget, timeWindow, new Random().nextInt());
        final Long2DoubleOpenHashMap timeToMaxButterflyCount = new Long2DoubleOpenHashMap();

        final Date date = new Date();
        final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        final BufferedReader br = new BufferedReader(new FileReader(inputPath));

        /****************************************************/
        final BufferedWriter y = new BufferedWriter(new FileWriter(outputPath+"/listofcounts.txt"));
        final BufferedWriter y1 = new BufferedWriter(new FileWriter(outputPath+"/listoftimestampy.txt"));

        final BufferedWriter z = new BufferedWriter(new FileWriter(outputPath+"/listofalerts.txt"));
        final BufferedWriter z1 = new BufferedWriter(new FileWriter(outputPath+"/listoftimestampz.txt"));


        long prevtime = 0;
        long prevtime2 = 0;
        boolean isthisfirst = false;
        /****************************************************/

        while(true) {

            final String line = br.readLine();
            if(line == null) {
                break;
            }

            final String[] tokens = line.split("\t");
            int src = Integer.valueOf(tokens[0]);
            int dst = Integer.valueOf(tokens[1]);
            long timestamp = Long.valueOf(tokens[2]);
            if (prevtime == 0){
                prevtime = Long.valueOf(tokens[2]);
            }

            if(module.process(src, dst, timestamp)) {
                isthisfirst = true;
                double currentGlobalCount = module.getGlobalButterfly();
                if (timeToMaxButterflyCount.containsKey(timestamp)) {
                    currentGlobalCount = Math.max(timeToMaxButterflyCount.get(timestamp), currentGlobalCount);
                }
                else {
                    date.setTime(timestamp);
                    //if (!isthisfirst){
                        //System.out.println("---> Peak butterfly count " + timeToMaxButterflyCount.get(prevtime) + ", from " + prevtime + " to " +  prevtime2);
                    //}
                    System.out.println("Alert at " + format.format(date) + " with " + currentGlobalCount + " butterflies!" + "          " + timestamp + "        " + module.threshold());
                    //prevtime = timestamp;
                    //isthisfirst = false;
                    //prevtime2 = timestamp;
                    timeToMaxButterflyCount.put(timestamp, currentGlobalCount);
                }
            }

            /****************************************************/
            y.write(String.valueOf((int)module.getGlobalButterfly()));
            y1.write(String.valueOf(timestamp));
            y.newLine();
            y1.newLine();
            if (isthisfirst == true) {
                z.write(String.valueOf((int)module.getGlobalButterfly()));
                z.newLine();
                z1.write(String.valueOf(timestamp));
                z1.newLine();
            }
            else {
                z.write("0");
                z.newLine();
                z1.write(String.valueOf(timestamp));
                z1.newLine();
            }
            isthisfirst = false;
            /****************************************************/            
        }
        br.close();
        
        //System.out.println("---> Peak butterfly count " + timeToMaxButterflyCount.get(prevtime) + ", from " + prevtime + " to " +  prevtime2);
        
        System.out.println("Butterfly-Spot terminated...");
        System.out.println("writing outputs...");

        File dir = new File(outputPath);
        try{
            dir.mkdir();
        }
        catch(Exception e){
        }

        final BufferedWriter bw = new BufferedWriter(new FileWriter(outputPath+"/time_to_global.txt"));
        final long[] timestamps = timeToMaxButterflyCount.keySet().toLongArray();
        Arrays.sort(timestamps);
        bw.write("Timestamp"+"\t"+"Estimated Global Butterfly Count");
        bw.newLine();
        for(final long timestamp : timestamps) {
            bw.write(timestamp + "\t" + timeToMaxButterflyCount.get(timestamp));
            bw.newLine();
        }
        bw.close();

        System.out.println("done.");
    }

    private static void printError() {
        System.err.println("Usage: run_spot.sh input_path output_path memory_budget time_window");
        System.err.println("- memory_budget should be an integer greater than or equal to 2.");
        System.err.println("- time_window should be an integer greater than 0.");
    }

}
