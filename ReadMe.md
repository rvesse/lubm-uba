# Univ-Bench Artificial Data Generator (UBA)
 
Data Generator for the LUBM Benchmark, this is the original code for the generator rewritten to have a proper CLI and be much more scalable:

- Improvements
    - `generate.sh` script for launching
    - Refactor code to make it cleaner while keeping behaviour as-is
    - Use log4j for logging
    - Added support for N-Triples and Turtle format outputs
    - Added support for compressed output (GZip)
    - Use a proper command line parsing library that provides meaningful built in help and parsing errors
    - New command line options:
        - Added `-o <dir>`/`--output <dir>` option to control where generated data files are written
        - Added `--format <format>` option to control the output format, supports `OWL`, `DAML`, `NTRIPLES` and `TURTLE`
        - Added `--compress` option which compresses output files with GZip as they are generated
        - Added `--consolidate <mode>` option which controls how many files are generates.  `None` generates 1 file per university department, `Partial` generates 1 file per university and `Full` generates a single file
        - Added `-t <threads>`/`--threads <threads>` option to allow parallel data generation for better performance
        - Added `--quiet` option to reduce logging verbosity
        - Added `--max-time <minutes>` option to specify the maximum amount of time to allow data generation to run for before forcibly aborting
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

### Performance Tuning

There are a number of parameters that can be used to tune the performance of the generator.  The best combination will depend on the hardware on which you are generating the data.

#### Multi-threading

We strongly suggest using `--threads` to set the number of threads, typically you should set this to twice the number of processor cores (assuming an Hyper-threading enabled).  Using this option will give you substantially better performance than not using it.

#### Consolidation

Using consolidation will reduce the number of files generated though total IO will be roughly the same. With `--consolidate Partial` you get a file per university (which can still be a lot of files at scale) while `--consolidate Full` will produce only a single file **but** will take longer because the IO to the single file can't be done in parallel.

#### Compression

The `--compress` option trades processing power for substantially reduced IO. The reduced IO which is invaluable at larger scales, for example with 8000 universities and `--consolidate Full` the compressed N-Triples output file is 2.5GB while the uncompressed output is 85GB.

Using it with `--consolidate Full` may reduce overall performance because all the compression happens on a single thread whereas with other consolidation modes compression is spread across all threads.

#### Output Format

The value given for `--format` controls the output data format and can have an effect on the amount of IO done and the performance.

`TURTLE` is the most compact format but is most expensive to produce because the reduction to prefixed name form takes time.  `NTRIPLES` and `OWL` are typically the fastest formats to produce.

#### Combining Compression and Consolidation

Whether combining `--consolidate` and `--compress` is worth it will depend on whether you are using a HDD or a SSD and perhaps more importantly the amount of free disk space you have since at large scales the data generated will be in the hundreds of gigabytes range uncompressed.

For example generating data like so:

    > ./generate.sh --quiet --timing -u 1000 --format NTRIPLES  --consolidate Full --threads 8

Produces the follow performance numbers (on a quad core system with 4GB JVM Heap):

Disk | `--compress` | Time           | File Size (Approx.)
------ | ----------------- | --------------- | -----------
SSD | No                 | 175s          | 24 GB
SSD | Yes                |  301s         | 730 MB
HDD | No                | 457s           | 24 GB
HDD | Yes               | 525s           | 730 MB

For generating data like so:

    > ./generate.sh --quiet --timing -u 1000 --format NTRIPLES  --consolidate Full --threads 8

Produces the follow performance numbers (on a quad core system with 4GB JVM Heap):

Disk | `--compress` | Time           | File Size (Approx.)
------ | ----------------- | --------------- | -----------
SSD | No                 | 140s          | 24 GB
SSD | Yes                | 276s          | 730 MB
HDD | No                | 408s           | 24 GB
HDD | Yes               | 424s           | 730 MB

In both cases enabling compression roughly doubles the time taken on an SSD while on a HDD is only about 10% slower.

Remember that you can use the `--output` option to specify where the data files are generated and thus control what kind of disk the data is written to, if you fail to specify this then files are generated in your working directory i.e. where you launched the generator from.

Note that at larger scales we would recommend enabling compression regardless because otherwise you are liable to exhaust disk space.


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

You can file issues against this repository if they are specific to this version of the data generator.  While the generator here differs substantially from the original **all** changes have been implemented such that the data generated remains identical.

The provided  `compareOutput.sh` script in this repository will generate data using the original code plus the rewritten code (using a variety of supported modes and output formats) and verifies that the generated data is identical.
