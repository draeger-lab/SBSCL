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
| Source commit | `7eb8c6d43f875ae5d8fe53d35d9de47d2b7459a4` |

SHA-256 of the bundled JAR:

```text
cc1fb89eeaf16dbe3c999b1b22a7019c306d2fb6b77e8d04213c435fe2a045ce
```

## Compatibility

The bundled artifact contains Java 8 bytecode and is used with the Java 17 baseline of SBSCL. Its runtime compatibility also depends on the requirements of its transitive dependencies.

## Validation

The artifact was successfully resolved from the in-project Maven repository using an initially empty local Maven repository. All 203 configured Maven test executions passed.

The identical JAR previously passed all 130 numerical FBC test runs using Apache Commons Math and ojAlgo.

This local repository is intended as a temporary solution and should be replaced once OptSolvX is publicly available through Maven.