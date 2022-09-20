# Index

Index.jar needs three parameters: original data D, DCs discovered on D, tuple numbers in D.

For example, the command "java -jar Index.jar airport_original.csv DC_airport_example.txt 30000" is to build indexes on D for DCs discovered on D,
where D is airport_original.csv, DCs discovered on D is in DC_airport_example.txt, and we use 30000 tuples in D.
The built indexes are automatically saved in airport_original_index.ind to be used by Algorithm IncDC.
 
Before building indexes, DCs discovered on D should be already known (one of the the input of incremental DC discovery). You can run Hydra or DCFinder to discover DCs on airport_original.csv and save the results in DC_airport_example.txt as follows: "java -jar Hydra.jar airport_original.csv DC_airport_example.txt 30000". Note that the copyrights of Hydra and DCFinder belong to their own developers. 

There is one more parameter "l" for building equalilty indexes (Algorithm 4 in the paper). You can change it with "l=XX".
For example, the command "java -jar IncDC.jar airport_original.csv DC_airport_example.txt 30000 l=0.65" is to set "l" to be 0.65.