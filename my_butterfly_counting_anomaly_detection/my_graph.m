fileID1 = fopen('output_spot/listofcounts.txt', 'r');
A = fscanf(fileID1, '%f');
fclose(fileID1);

fileID2 = fopen('output_spot/listofalerts.txt', 'r');
B = fscanf(fileID2, '%f');
fclose(fileID2);

fileID3 = fopen('output_spot/listoftimestampy.txt', 'r');
C = fscanf(fileID3, '%f');
fclose(fileID3);

fileID4 = fopen('output_spot/listoftimestampz.txt', 'r');
D = fscanf(fileID4, '%f');
fclose(fileID4);



subplot(1,2,1);
plot(C(1:length(A)),A);
title('actual number of butterflies');
xlabel('Number of edges processed');
ylabel("Number of butterfly counts");
%plot(A);
subplot(1,2,2);
plot(D(1:length(B)),B);
title("detected number of butterflies");
xlabel("Number of edges processed");
ylabel("Number of butterfly counts");