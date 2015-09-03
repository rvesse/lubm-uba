# Univ-Bench Artificial Data Generator (UBA)
 
Data Generator for the LUBM Benchmark, this is basically the official code for the generator with some minor tweaks:

- `generate.sh` script for launching
- `pom.xml` and changed directory structure to be able to build with Maven
- Added `-out <dir>` option to control where generated data files are written

## Usage

    > ./generate.sh options
   
Running without any `options` will print the usage summary

Note that the default ontology URL usually passed to the `-onto` argument is `http://swat.cse.lehigh.edu/onto/univ-bench.owl`

## Copyright

The Semantic Web and Agent Technologies (SWAT) Lab, CSE Department, Lehigh University
  
## Contact

### Original Author

Yuanbo Guo	[yug2@lehigh.edu](mailto:yug2@lehigh.edu)

For more information about the benchmark, visit its [homepage](http://www.lehigh.edu/~yug2/Research/SemanticWeb/LUBM/LUBM.htm)

### This Repository

You can file issues against this repository however this is only a convenience repository to provide a usable maven build of the data generator and you should not expect changes or fixes to the code to happen here.
