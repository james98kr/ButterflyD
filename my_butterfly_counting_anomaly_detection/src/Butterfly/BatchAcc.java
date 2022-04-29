package butterfly;


import java.io.IOException;
import java.util.Random;


public class BatchAcc {

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
        final int numberOfTrials = Integer.valueOf(args[3]);
        System.out.println("number_of_trials: " + numberOfTrials);

        for(int trial = 0; trial < numberOfTrials; trial++) {
            final ButterflyAcc module = new ButterflyAcc(memoryBudget, new Random().nextInt());

            System.out.println("start running Butterfly (Accurate Ver.)...(" + trial + "/" + numberOfTrials + ")");
            Common.runBatch(module, inputPath, "\t");
            System.out.println("Butterfly (Accurate Ver.) terminated ...");
            System.out.println("Estimated number of global butterflies: " + module.getGlobalButterfly());

            Common.writeOutputs(module, outputPath, trial);
        }
        return;
    }

    private static void printError() {
        System.err.println("Usage: run_acc.sh input_path output_path memory_budget number_of_trials");
        System.err.println("- memory_budget should be an integer greater than or equal to 2.");
    }

}
