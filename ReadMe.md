# Univ-Bench Artificial Data Generator (UBA)
 
Data Generator for the LUBM Benchmark, this is basically the official code for the generator with some minor tweaks:

- Improvements
    - `generate.sh` script for launching
    - Refactor code to make it cleaner while keeping behaviour as-is
    - Use log4j for logging
    - Added support for N-Triples and Turtle format outputs
    - Added support for compressed output (Gzip)
    - Use a proper command line parsing library that provides meaningful built in help and parsing errors
    - New command line options:
        - Added `-o <dir>`/`--output <dir>` option to control where generated data files are written
        - Added `--format <format>` option to control the output format, supports `OWL`, `DAML`, `NTRIPLES` and `TURTLE`
        - Added `--compress` option which compresses output files with GZip as they are generated
        - Added `--consolidate` option which produces a single file per university rather than a file per university department.  This avoids issues with excessive numbers of files being generated for larger numbers of universities.
        - Added `-t <threads>`/`--threads <threads>` option to allow parallel data generation for better performance
        - Added `--quiet` option to reduce logging verbosity
        - Added `--max-time <minutes>` option to specify the maximum amount of time to allow data generation to run for
- Build Changes
    - Now requires Java 1.7
    - `pom.xml` and changed directory structure to be able to build with Maven
    - Build a shaded JAR with defined main class so the JAR can be run directly
    - Added useful dependencies
- Bug fixes
     - Use OS specific filename separator character
     - Check for errors when writing files

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

You can file issues against this repository if they are specific to this version of the data generator.  While the generator here may differ from the original any changes have been done and verified such that the data generated remains identical.
