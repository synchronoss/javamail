# JavaMail library

JavaMail library is a patched version of v1.5.2 of the JavaMail API (https://javaee.github.io/javamail/). See the "patches" folder for the list of patches applied and the javamail-1.5.2-src.zip for the original source.

## Building pre-requisites

Amazon Corretto 8 is required to be installed before building. Ensure either the default JDK or JAVA_HOME is set to Amazon Corretto 8 e.g.

```
export JAVA_HOME=<Path to Amazon Corretto 8>
```

## How to build

To generate a JAR, run the following command from the root of the folder:

```
./gradlew jar
```

The JAR will be located in the `build/libs` folder.
