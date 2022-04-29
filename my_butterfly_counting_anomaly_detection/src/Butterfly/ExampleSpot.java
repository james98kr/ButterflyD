package butterfly;


import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;


public class ExampleSpot {

    public static void main(String[] ar) throws IOException {

        final String dataPath = "example_graph_with_timestamps.txt";
        final String delim = "\t";
        final ButterflySpot module = new ButterflySpot(2000, 86400, new Random().nextInt());

        final BufferedReader br = new BufferedReader(new FileReader(dataPath));
        final Date date = new Date();
        final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        while(true) {

            final String line = br.readLine();
            if(line == null) {
                break;
            }

            final String[] tokens = line.split(delim);
            int src = Integer.valueOf(tokens[0]);
            int dst = Integer.valueOf(tokens[1]);
            long timestamp = Long.valueOf(tokens[2]);

            if(module.process(src, dst, timestamp)) {
                double currentGlobalCount = module.getGlobalButterfly();
                date.setTime(timestamp);
                System.out.println("Alert at " + format.format(date) + " with " + currentGlobalCount + " triangles!");
            }
        }
        br.close();

        return;
    }


}
