# ButterflyD
This code counts the number of butterflies(4-node cliques) in bipartite graphs for anomaly detection. Whenever there is an sudden increase in the approximate total number of butterflies in the graph, the model considers it as an abnormal activity and triggers an alarm.

First of all, you must unzip the dataset files in `my_butterfly_counting_anomaly_detection/dataset/itunes`.

Then move to the `my_butterfly_counting_anomaly_detection` directory, and execute file using the `make` command.

If you want to use your own dataset, open up the `my_butterfly_counting_anomaly_detection/Makefile` file and change the datapath for `runbutterfly_acc.sh` and `runbutterfly_spot.sh`.

Original code for ThinkD: https://github.com/kijungs/thinkd
