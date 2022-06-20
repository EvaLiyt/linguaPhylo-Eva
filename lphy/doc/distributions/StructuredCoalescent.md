StructuredCoalescent distribution
=================================
StructuredCoalescent(Double[][] **M**, Integer[] **k**, Taxa **taxa**, Object[] **demes**, Boolean **sort**)
------------------------------------------------------------------------------------------------------------

The structured coalescent distribution over tip-labelled time trees.

### Parameters

- Double[][] **M** - The population process rate matrix which contains the effective population sizes and migration rates. Off-diagonal migration rates are in units of expected migrants per *generation* backwards in time.
- Integer[] **k** - the number of taxa in each population. provide either this or a demes argument.
- Taxa **taxa** - the taxa.
- Object[] **demes** - the deme array, which runs parallel to the taxonArray in the taxa object.
- Boolean **sort** - whether to sort the deme array, before mapping them to the indices of the effective population sizes and migration rates. If not, as default, the pop size indices are determined by the natural order of the deme array, if true, then the indices are the order of sorted deme array.

### Return type

- TimeTree

### Reference

Müller, N. F., Rasmussen, D. A., & Stadler, T. (2017). The structured coalescent and its approximations. Molecular biology and evolution, 34(11), 2970-2981.[https://doi.org/10.1093/molbev/msx186](https://doi.org/10.1093/molbev/msx186)

