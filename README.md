# IncDC
The incremental DC discovery algorithm, developed in this submission.

## To run incremental DC discovery:
(1) run Hydra or DCFinder to discovery DCs on a given data set D;

(2) run IncDC to incrementally discovery DCs on D and an incremental data set △D:

A simple example is given as follows (all files are assumed to be in the current directory):

` java -jar IncDC.jar atom_original.csv atom_incremental.csv DC_atom_example.txt index_example_atom.txt 105000 35000 0 ` 

It is to conduct incremental DC discovery on atom_original.csv + atom_incremental.csv, leveraging known DCs in DC_atom_example.txt and predicate space in index_example_atom.txt. Numbers such as 105000, 35000 are used to set the number of tuples in atom_original.csv and atom_incremental.csv,  respectively. Number 0 is used to set the threshold for column select.

For more detailed parameters of different algorithms, please refer to the readme file in separate directories.
