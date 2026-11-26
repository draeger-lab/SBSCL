
# Bundled OptSolvX Dependency

OptSolvX is temporarily provided through the in-project Maven repository until it is available through a public Maven repository.

## Artifact Information

| Property | Value |
|---|---|
| Group ID | `org.optsolvx` |
| Artifact ID | `optsolvx` |
| Version | `0.1.0-SNAPSHOT` |
| Classifier | `jdk8` |
| Source repository | https://github.com/draeger-lab/OptSolvX |
| Source commit | `7eab6d88cc69cda911833cc7e72ec3552276cad0` |

SHA-256 of the bundled JAR:

```text
bcaecdc28b252bfe5e4b01e3236d0c77e31617593cbc80a25a7056675b0db066
```

## Compatibility

The bundled artifact contains Java 8 bytecode and is used with the Java 17 baseline of SBSCL. Its runtime compatibility also depends on the requirements of its transitive dependencies.


## Validation

The bundled artifact includes a workaround for incorrect
infeasibility results in ojAlgo 55.0.0 by selecting its
legacy LP solver.

The following validations were completed successfully:

- All 27 OptSolvX unit tests.
- All 203 configured SBSCL Maven test executions.
- All 130 numerical SBML/FBC test runs using Apache Commons
  Math and ojAlgo.
- Objective value validation for the BiGG models
  `e_coli_core` and `iAF1260`.
- Dependency resolution from the in-project Maven repository
  using an initially empty local Maven repository.
- SHA-256 verification of the resolved OptSolvX artifact.