package edu.lehigh.swat.bench.uba.model;

/** instance count of a class */
public class InstanceCount {
    /** instance number within one department */
    public int num = 0;
    /** total instance num including sub-classes within one department */
    public int total = 0;
    /** index of the current instance within the current department */
    public int count = 0;
    /** total number so far within the current department */
    public int logNum = 0;
}