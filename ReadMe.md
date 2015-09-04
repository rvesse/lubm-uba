# Univ-Bench Artificial Data Generator (UBA)
 
Data Generator for the LUBM Benchmark, this is basically the official code for the generator with some minor tweaks:

- `generate.sh` script for launching
- `pom.xml` and changed directory structure to be able to build with Maven
- Added `-o <dir>`/`--output <dir>` option to control where generated data files are written
- Use a proper command line parsing library that provides meaningful built in help and parsing errors
- Bug fix to use OS specific file separator

## Usage

    > ./generate.sh options
   
Run the following to see the usage summary:

    > ./generate.sh --help

## Copyright

### Original Code

The Semantic Web and Agent Technologies (SWAT) Lab, CSE Department, Lehigh University

### Modified Code

Rob Vesse
  
## Contact

### Original Author

Yuanbo Guo	[yug2@lehigh.edu](mailto:yug2@lehigh.edu)

For more information about the benchmark, visit its [homepage](http://www.lehigh.edu/~yug2/Research/SemanticWeb/LUBM/LUBM.htm)

### This Repository

You can file issues against this repository if they are specific to this version of the data generator.  While the generator here may differ from the original any changes have been done such that the data generated remains identical.
