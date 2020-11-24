FossilBirthDeathTree distribution
=================================
FossilBirthDeathTree(Number **lambda**, Number **mu**, Number **rho**, Number **psi**, Integer **n**, Taxa **taxa**)
--------------------------------------------------------------------------------------------------------------------

A tree of extant species and those sampled through time, which is conceptually embedded in a full species tree produced by a speciation-extinction (birth-death) branching process.<br>Conditioned on root age and (optionally) on number of *extant* taxa.

### Parameters

- Number **lambda** - per-lineage birth rate.
- Number **mu** - per-lineage death rate.
- Number **rho** - proportion of extant taxa sampled.
- Number **psi** - per-lineage sampling-through-time rate.
- Integer **n** - the number of taxa. optional.
- Taxa **taxa** - Taxa object

### Return type

- TimeTree



