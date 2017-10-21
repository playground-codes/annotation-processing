## Annotation Processing

A handy and powerful technique for generating additional source files during compilation.
* Source files can be any type of files (Java files, documentation, resources, and etc).
* It can only be used to generate new files, not to change existing ones.

### Annotation Processing API
The annotation is done in multiple rounds. Each round includes:
 1. Compiler searches for the annotations.
 2. Compiler chooses annotation processor suited for these annotations.
 3. Each annotation processor is called on the corresponding sources.
 4. If any files are generated during this process, another round is started with the generated files as its input.
 5. This process completes when no new files are generated.

### Steps
Applying annotation processing technique to generate source files includes the following steps:
* Define required @annotations and how it can be used.
* Write custom processors which extends `AbstractProcessor` class.
* In each processor
  * Filter corresponding annotations.
  * Verify the correctness of annotated elements and use `Messager` to show useful messages. 
  * Retrieve all necessary information(i.e: class name, method name, argument type) which are used to generate source files.
  * Generate output files (by using `Filer`).

### Some useful libraries
* [AutoService](https://github.com/google/auto/tree/master/service): Generates processor metadata file.
* [JavaPoet](https://github.com/square/javapoet): Generates `.java` source files.

### References
1. http://www.baeldung.com/java-annotation-processing-builder