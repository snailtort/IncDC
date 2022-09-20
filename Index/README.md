# Index

Index.jar needs three parameters: original data D, DCs discovered on D, tuple numbers in D.

For example, the command "java -jar Index.jar airport_original.csv DC_airport_example.txt 30000" is to build indexes on D, 
where D is airport_original.csv, DCs discovered on D is in DC_airport_example.txt. In this run, we use 30000 tuples in D. 
Before building indexes, we need DCs discovered on D, you can run Hydra or DCFinder to discover DCs on airport_original.csv and save results in DC_airport_example.txt.

Parameter "l" in paper for building equalilty index, you can change it by "l=XX".
For example, the command "java -jar IncDC.jar airport_original.csv DC_airport_example.txt 30000 l=0.65" is to change "l" to "0.65".

