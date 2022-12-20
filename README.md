# java-Python-bridge-example
this is a small example project to demonstrate how to use Python4j to execute Python code in Java.

## Basic Python4j Usage
Python4j can be included as a maven dependency
```xml
        <dependency>
            <groupId>org.nd4j</groupId>
            <artifactId>python4j-core</artifactId>
            <version>1.0.0-M2.1</version>
        </dependency>
```

Whenever executing Python code, the following try-structure is used: 

```java
try(PythonGIL pythonGIL = PythonGIL.lock()) {
        try(PythonGC gc = PythonGC.watch()) {
            PythonExecutioner.exec(...);
        }
    }
```
This locks the GIL and assures proper garbage collection. 
The ```PythonExecutioner.exec(...)``` takes Python code as a string and executes it. Additionally, a list of input variables and a list of output variables can be given. These variables are of type   ```PythonVariable``` which wraps a Python variable including a name, type and value. 

The input- and output-variables are mapped through their names to the corresponding variables in the Python code. 

```java
try(PythonGIL pythonGIL = PythonGIL.lock()) {
    try(PythonGC gc = PythonGC.watch()) {

        //create input variables
        List<PythonVariable> inputs = new ArrayList<>();
        PythonVariable a = new PythonVariable<>("a", PythonTypes.FLOAT, 0.37);
        PythonVariable b = new PythonVariable<>("b", PythonTypes.FLOAT, 13.0);
        inputs.add(a); 
        inputs.add(b); 
        
        //create output variables
        List<PythonVariable> outputs = new ArrayList<>();
        PythonVariable c = new PythonVariable<>("c", PythonTypes.INT);
        inputs.add(c);

        String code =  "c = int((a + b)*1000)";

        PythonExecutioner.exec(code, inputs, outputs);

        System.out.println(c.getValue());
    }
}
````
If the Python code to be executed includes external libraries, the paths to these libraries have to be added beforehand in java using ```Py_AddPath(package_path)```

## Example Project
In this example project, a simple text phrase is parsed and the parts-of-speech (POS) are tagged using the Python library [spaCy](https://spacy.io/).

To set up all dependencies on Python side, there is the class [PythonEnvironmentSetup](https://github.com/ArDoCo/java-python-bridge-example/blob/experimental/src/main/java/pythonsetup/PythonEnvironmentSetup.java).
The Method ```initialize()``` calls [install_requirements.py](https://github.com/ArDoCo/java-python-bridge-example/blob/experimental/src/main/python/install_requirements.py), that installs all dependencies from a requirements.txt file to a predefined directory. The requirements.txt and the target directory for the packages are specyfied in the [conf.json](https://github.com/ArDoCo/java-python-bridge-example/blob/experimental/src/main/resources/conf.json). If the target directory field in the conf.json is empty, the required packages will be installed in the useres home directory. 

After the requirements have been installed, the library path from the conf.json is added such that Python4j can find them. 

The class [PythonDependencyChecker](https://github.com/ArDoCo/java-python-bridge-example/blob/experimental/src/main/java/pythonsetup/PythonDependencyChecker.java) implements some static methods to obtain dependency or package versions and to check whether libraries are available. 

### Example Use Case

For the example use case, a text phrase can be tagged with the POS. To do so, [nlp_doc.py](https://github.com/ArDoCo/java-python-bridge-example/blob/experimental/src/main/python/nlp_doc.py) implements a simple class that parses a text using spaCy and returns the POS-taggs as a Python dict. 

The corresponding Java class [NLPDoc](https://github.com/ArDoCo/java-python-bridge-example/blob/experimental/src/main/java/nlp/NLPDoc.java) instantiates this Python class with the given text phrase and reads out the Python dict with the POS-tags into a Map. 

Example code for the initial setup and the usage can be found in the [Example](https://github.com/ArDoCo/java-python-bridge-example/blob/experimental/src/main/java/example/Example.java) class.