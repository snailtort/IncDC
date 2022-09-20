# IncDC

IncDC.jar needs six parameters: original data D, incremental data △D, DCs discovered on D, tuple numbers in D, tuple numbers in △D, index Ind($\Sigma$).

For example, the command "java -jar IncDC.jar airport_original.csv airport_incremental.csv DC_airport_example.txt 30000 10000 airport_original_index.txt" is to run incremental DC discovery on D + △D, 
where D is airport_original.csv, △D is airport_incremental.csv, DCs discovered on D is in DC_airport_example.txt, index is airport_original_index.txt. In this run, we use 30000 tuples in D and 10000 tuples in △D. 

The design here is to easily vary the size of D, the size of △D, which is required in some experimental studies.

In addition, IncDC.jar can take one more optional parameter: tuples in a single round. This enables us to handle △D as a continuous sets of tuple insertions, which is tested in some experiments.

For example, the command "java -jar IncDC.jar airport_original.csv airport_incremental.csv DC_airport_example.txt 30000 10000 airport_original_index.txt size=500" is to apply the 10000 tuples in △D in 20 rounds; 
in each round, 500 tuples are inserted into D.

Parameter "l" in paper for building equalilty index, you can change it by "l=XX".
For example, the command "java -jar IncDC.jar airport_original.csv airport_incremental.csv DC_airport_example.txt 30000 10000 airport_original_index.txt size=500 l=0.65" is to change "l" to "0.65".

