# IncDC
The incremental DC discovery algorithm, developed in this submission.

## Dataset
This folder stores all the experiment datasets, which can be split into original dataset D and incremental data set △D.

## To run incremental DC discovery:
(1) run Hydra or DCFinder to discovery DCs on a given data set D;

(2) run IncDC to incrementally discovery DCs on D and an incremental data set △D:

A simple example is given as follows (all files can be found in IncDC/example ):

(1) java -jar Hydra.jar airport_original.csv DC_airport_example.txt 30000

(2) java -jar IncDC.jar airport_original.csv airport_incremental.csv DC_airport_example.txt 30000 10000  

Note that (1) is to conduct DC discovery on airport_original.csv, and to save the discovered DCs in DC_airport_example.txt. (2) is to conduct incremental DC discovery on airport_original.csv + airport_incremental.csv, leveraging known DCs in DC_airport_example.txt. Numbers such as 30000, 10000 are used to set the number of tuples in airport_original.csv and airport_incremental.csv,  respectively. 

For more detailed parameters of different algorithms, please refer to the readme file in separate directories.
