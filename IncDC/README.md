# IncDC

IncDC.jar needs five parameters: original data D, incremental data △D, DCs discovered on D, tuple numbers in D, tuple numbers in △D.

For example, the command "java -jar IncDC.jar airport_original.csv airport_incremental.csv DC_airport_example.txt 30000 10000" is to run incremental DC discovery on D + △D, 
where D is airport_original.csv, △D is airport_incremental.csv, DCs discovered on D is in DC_airport_example.txt. In this run, we use 30000 tuples in D and 10000 tuples in △D. 
Before running IncDC, please run Hydra or DCFinder to discover DCs on airport_original.csv and save results in DC_airport_example.txt in advance.

The design here is to easily vary the size of D, the size of △D, which is required in some experimental studies.

In addition, IncDC.jar can take one more optional parameter: tuples in a single round. This enables us to handle △D as a continuous sets of tuple insertions, which is tested in some experiments.

For example, the command "java -jar IncDC.jar airport_original.csv airport_incremental.csv DC_airport_example.txt 30000 10000 size=500" is to apply the 10000 tuples in △D in 20 rounds; 
in each round, 500 tuples are inserted into D.

For ease of use, the process of index building is integrated into the IncDC.jar, which requires only D and DCs discovered on D (the first and the third parameter).
We measure the time for index building and the time for incremental DC discovery respectively, in the output of the program. Index building is conducted only once ever if △D is handled in multiple rounds.

Parameter "l" in paper for building equalilty index, you can change it by "l=XX".
For example, the command "java -jar IncDC.jar airport_original.csv airport_incremental.csv DC_airport_example.txt 30000 10000 size=500 l=0.65" is to change "l" to "0.65".

