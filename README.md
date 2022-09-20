# IncDC
The incremental DC discovery algorithm, developed in this submission.

## Hydra and DCFinder
[Hydra ](https://dl.acm.org/doi/10.14778/3157794.3157800)(resp. [DCFinder](https://dl.acm.org/doi/10.14778/3368289.3368293)) has been implemented by student(s) of the information systems group at the Hasso-Plattner-Institut (HPI) in the context of the Metanome project. We are really grateful for the open source implementaion of [Hydra and DCFinder](https://github.com/HPI-Information-Systems/metanome-algorithms) . 

## Dataset
This folder stores all the experiment datasets, which can be split into original dataset D and incremental data set △D.

## To run incremental DC discovery:
(1) run Algorithm Index to build the indexes on a given data set D, based on the DCs discovered on D. This is the pre-processing step of IncDC.

(2) run Algorithm IncDC to incrementally discover DCs in response to an incremental data set △D, by leveraging the indexes built in (1).

A simple example is given as follows (all files can be found in IncDC/example ):

(1) java -jar Index.jar airport_original.csv DC_airport_example.txt 30000

(2) java -jar IncDC.jar airport_original.csv airport_incremental.csv DC_airport_example.txt 30000 10000 airport_origin_index.ind 

Note that (1) is to build the indexes on dataset airport_original.csv for DCs in DC_airport_example.txt; the indexes are automatically saved in airport_origin_index.ind. (2) is to conduct incremental DC discovery in response to airport_incremental.csv based on airport_original.csv and DCs in DC_airport_example.txt, by leveraging the indexes stored in airport_origin_index.ind. Herein, the numbers such as 30000, 10000 are used to set the number of tuples in airport_original.csv and airport_incremental.csv, respectively.

Note that after (2), the index file airport_origin_index.ind will be updated to reflect the changes incurred by airport_incremental.csv. Therefore, IncDC can be called again for new incremental data without the need of (1) again. That is, the indexes are automatically maintained during IncDC and are not required to be rebuilt for new incremental data with IncDC.


For more detailed parameters of Algorithms Index and IncDC, please refer to the readme files in their separate directories.
