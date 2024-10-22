# External Builder API Manual for Quassar

## Introduction

This document outlines the communication protocol that Quassar's platform expects from external builders, along with an example Dockerfile specification and the usage of the Java library that supports the protocol. The goal is to provide a comprehensive guide for developers who wish to integrate their Docker images with Quassar.

## Table of Contents
1. [Overview](#overview)
2. [Communication Protocol](#communication-protocol)
3. [Dockerfile Specification](#dockerfile-specification)
4. [Using the Java Library](#using-the-java-library)
5. [Examples](#examples)
6. [FAQs](#faqs)
7. [Contact](#contact)

## Overview

Quassar is an online development platform that allows domain modeling with minimal code using a Domain-Specific Modeling Language (DSML). The models created can be compiled to generate executable code in various programming languages. This code can be included as a library in another project or executed directly on the platform.

External builders are responsible for transforming these models into executable code. To integrate with Quassar, builders must meet specific requirements and be packaged as Docker images, which are then published on Docker Hub or an Artifactory.

This manual details how external builders should communicate with Quassar and provides detailed examples of how to create a compliant builder.

## Communication Protocol

### Communication Flow
The builder must follow this communication flow when interacting with the Quassar platform:
1. **Process Initiation**: Quassar sends a request to the builder with the model information to be compiled.
2. **Request Handling**: The builder receives the request in the specified JSON format.
3. **Compilation Execution**: The builder processes the provided information, compiles the model, and generates the executable code.
4. **Response**: Once the builder completes the compilation process, it sends back a response with the results, including the compilation output and, if successful, the generated code or an equivalent artifact.

### Request Format

The input file for the External Builder API consists of key-value pairs, where each property is described by its name on one line and its corresponding value on the next. For certain properties like `def.file`, the value may span multiple lines, with each line representing a separate entry. At the end of this, a double break line is expected.

Below is an example of the file structure and the corresponding description of each parameter.

### Parameter Descriptions

#### `def.file`
- **Description**: A multi-line property listing file paths to the DSL files to be the input of the builder. Each line contains a path to a model file.
- **Example**:
  /konos/src/io/intino/konos/dsl/ui/UI.tara
  /konos/src/io/intino/konos/dsl/DataHub.tara

#### `project`
- **Description**: The name of the project.
- **Example**: `projectName`

#### `module`
- **Description**: The module name within the project.
- **Example**: `moduleName`

#### `project.path`
- **Description**: The absolute path to the project root.
- **Example**: `/Users/lorenipsum/projectName`

#### `module.path`
- **Description**: The absolute path to the module within the project.
- **Example**: `/Users/lorenipsun/moduleName`

#### `outputpath`
- **Description**: The path where the generated output files should be placed.
- **Example**: `/Users/lorenipsun/moduleName/gen`

#### `final_outputpath`
- **Description**: The final output path for production builds.
- **Example**: `/Users/lorenipsun/moduleName/out`

#### `res.path`
- **Description**: The path to the resource directory for the module.
- **Example**: `/Users/lorenipsun/moduleName/res`

#### `src.path`
- **Description**: The source code directory path for the module.
- **Example**: `/Users/lorenipsun/moduleName/src`

#### `repository.path`
- **Description**: The local repository path, typically for load dsls and save generated.
- **Example**: `/Users/lorenipsun/.m2/repository`

#### `encoding`
- **Description**: The character encoding used for file reading and writing.
- **Example**: `UTF-8`

#### `compilation.mode`
- **Description**: The operation of compilation, typically `Build` for production builds.
- **Example**: `Build`

#### `dsl`
- **Description**: The DSL and version will be used.
- **Example**: `Meta:2.0.0`

#### `generation.package`
- **Description**: The package name where generated files will be placed.
- **Example**: `com.example.dsl`

#### `out.dsl`
- **Description**: The DSL output name.
- **Example**: `lorenipsum`

#### `out.dsl.builder.groupId`
- **Description**: The `groupId` of the DSL builder.
- **Example**: `io.intino`

#### `out.dsl.builder.artifactId`
- **Description**: The `artifactId` of the DSL builder.
- **Example**: `konos`

#### `out.dsl.builder.version`
- **Description**: The version of the DSL builder.
- **Example**: `13.0.0`

#### `out.dsl.runtime.groupId`
- **Description**: The `groupId` of the runtime environment for the DSL.
- **Example**: `io.intino.magritte`

#### `out.dsl.runtime.artifactId`
- **Description**: The `artifactId` of the runtime environment for the DSL.
- **Example**: `framework`

#### `out.dsl.runtime.version`
- **Description**: The version of the runtime environment for the DSL.
- **Example**: `5.2.1`

This structured format ensures that all relevant information needed for building and compiling the project is provided in a clear and organized way.

### Error Handling

The builder should return clear and precise error messages. Errors should relate to issues with the compilation process or internal builder failures. Example error response:

## Dockerfile Specification

To integrate an external builder with Quassar, a Docker image must be created that meets the following requirements:

	1.	Base Image: The image should be based on a stable version of Java, as the support library is currently available in Java.
	2.	Dependency Installation: Ensure that the Dockerfile installs all necessary dependencies for compiling the model.
	3.	Expose Port: If the builder interacts with the platform via an HTTP API, the corresponding port should be exposed.
	4.	Entry Command: The container’s entry command should be the execution of the compiler that will process the models received from Quassar.

### Example dockerfile
The Dockerfile of the example creates a Docker image based on the slim version of OpenJDK 21. The image is configured to run a builder (indicated by the labels) and is designed to handle build operations. Below is a breakdown of the Dockerfile:

```
FROM openjdk:21-jdk-slim
LABEL maintainer="octavio.roncal <octavioroncal@siani.es>"
LABEL version="1.3.0"
LABEL description="Loren Ipsum compiler"
LABEL operations="Build"
LABEL targets="Java"
COPY out/build/builder/builder.jar /root/app/
COPY out/build/builder/lib /root/app/lib
COPY docker/run-builder.sh /root/app/
WORKDIR /root/app
RUN chmod +x /root/app/run-builder.sh
ENTRYPOINT ["/root/app/run-builder.sh"]
```
### Dockerfile Breakdown
* Base Image:
  * openjdk:21-jdk-slim: This is a lightweight version of the Java Development Kit (JDK) 21. It provides the necessary Java environment for running the builder.
* Labels:
  * LABEL maintainer="octavio.roncal <octavioroncal@siani.es>": Specifies the maintainer of the Docker image.
  * LABEL version="1.3.0": Denotes the version of the Docker image (1.3.0).
  * LABEL description="Loren Ipsum compiler": Describes the purpose of the image as a “Loren Ipsum compiler.”
  * LABEL operations="Build": Indicates the type of operations this image performs (Build). This label is mandatory.
  * LABEL targets="Java": Specifies the target language for this builder (Java). This label is mandatory.
* Copying Files:
  * COPY out/build/builder/builder.jar /root/app/: Copies the builder.jar file, which contains the main executable, into the /root/app/ directory.
  * COPY out/build/builder/lib /root/app/lib: Copies the lib directory containing additional dependencies into /root/app/lib.
  * COPY docker/run-builder.sh /root/app/: Copies the run-builder.sh script into /root/app/.
* Working Directory:
  * WORKDIR /root/app: Sets the working directory inside the container to /root/app, where all subsequent commands and execution will take place.
* Permissions:
  * RUN chmod +x /root/app/run-builder.sh: Makes the run-builder.sh script executable by adding execution permissions (+x).
* Entry Point:
  * ENTRYPOINT ["/root/app/run-builder.sh"]: Configures the container to execute the run-builder.sh script when it starts, making it the default command for the container.

### run-builder.sh Script

The run-builder.sh script is a simple Bash script that runs the builder.jar and directs the output to a log file:
```
java -jar /root/app/builder.jar /root/project/args.txt > /root/project/output.log 2>&1
```
### Labels operations and targets
The operations and targets labels are mandatory for the Docker image:
* operations: Defines the operations that the builder can perform. In this case, it is set to "Build", indicating that the builder will compile or build the provided project.
* targets: Specifies the target language of generated code. In this case, "Java" is the target, meaning that this builder is set up to generate java classes from the model.

As part of the communication protocol, the Quassar building platform will place the argument file at the path `/root/project/args.txt` inside the container. This file contains all the necessary input and configuration parameters required by the builder to execute the compilation process. The builder must read from this file to initiate the build process. Additionally, all output from the build process, including both standard output and error messages, should be logged into the file `/root/project/output.log`. This ensures that any relevant information, including error diagnostics and build status, is captured for review.





This Dockerfile uses OpenJDK 21 and Maven to build the project and generate the executable. Make sure to adjust it based on your specific needs.

## Using the Java Library

Quassar provides a Java library that facilitates interaction with the platform through the External Builder API. Below is an explanation of how to integrate it into your project.

### Installation

You can add the library dependency to your pom.xml (if you’re using Maven):
```
<dependency>
    <groupId>io.quassar</groupId>
    <artifactId>quassar-builder-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Builder Initialization

To initialize the builder and connect it to the platform, follow these steps:

```
import com.quassar.sdk.QuassarBuilder;

public class BuilderApp {
    public static void main(String[] args) {
        QuassarBuilder builder = new QuassarBuilder();
        builder.start();
    }
}
```
### Key Methods

The library provides the following key methods for interaction:

	•	start(): Starts the builder and listens for compilation requests.
	•	compileModel(ModelData modelData): Takes model data and compiles it into executable code.
	•	sendResult(Result result): Sends the compilation result back to Quassar.


### Example Usage

## FAQs

### What is a builder in Quassar?

A builder is a component responsible for taking a domain model and transforming it into executable code or a ready-to-use artifact.

### How does the builder communicate with Quassar?

The builder uses an API that communicates with Quassar via JSON-formatted requests and responses.

## Contact

For more information, questions, or support, contact our team at support@quassar.io.