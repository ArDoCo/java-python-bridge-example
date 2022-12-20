package pythonsetup;

import org.nd4j.python4j.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PythonDependencyChecker {
    private PythonDependencyChecker(){

    }
    private static Map<String, String> readRequirements(String requirementsFilePath){
        Map<String, String> requirementsMap = new HashMap<>();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(
                    requirementsFilePath));
            String line = reader.readLine();
            while (line != null) {
                String[] module = line.split("==");
                if(module.length > 1) {
                    requirementsMap.put(module[0], module[1]);
                }
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return requirementsMap;
    }
    public static String getPythonVersion(){
        try(PythonGIL pythonGIL = PythonGIL.lock()) {
            try(PythonGC gc = PythonGC.watch()) {
                PythonVariable<String> version = new PythonVariable<>("version", PythonTypes.STR);
                PythonExecutioner.exec(
                        """
                                import sys
                                version = sys.version.split(' ')[0]
                                """,
                        null, Collections.singletonList(version)
                );
                return version.getValue();
            }
        }
    }
    public static String getPipVersion(){
        try(PythonGIL pythonGIL = PythonGIL.lock()) {
            try(PythonGC gc = PythonGC.watch()) {
                PythonVariable<String> version = new PythonVariable<>("version", PythonTypes.STR);
                PythonExecutioner.exec(
                        """
                                import pip
                                version = pip.__version__
                                """,
                        null, Collections.singletonList(version)
                );
                return version.getValue();
            }
        }
    }
    public static Map<String, String> getAvailableLibraries() {
        try (PythonGIL pythonGIL = PythonGIL.lock()) {
            try (PythonGC gc = PythonGC.watch()) {
                //obtain all available python packages with version using pip freeze and read it into a map
                PythonVariable<Map> out = new PythonVariable<>("packages", PythonTypes.DICT);
                PythonExecutioner.exec(
                        """
                                from pip._internal.operations import freeze
                                packages = {x.split('==')[0]:x.split('==')[-1] for x in freeze.freeze()}
                                """,
                        null, Collections.singletonList(out)
                );
                return out.getValue();
            }
        }
    }

    public static boolean minVersionSatisfied(String minVersion, String actualVersion){

        String[] actualVersionSplit = actualVersion.split("\\.");
        String[] minVersionSplit = minVersion.split("\\.");

        if(actualVersionSplit.length != minVersionSplit.length){
            throw new IllegalArgumentException();
        }

        for(int i = actualVersionSplit.length-1; i >=0; i--){
            if (Integer.parseInt(actualVersionSplit[i]) < Integer.parseInt(minVersionSplit[i])){
                return false;
            }
        }
        return true;
    }

    public static boolean maxVersionSatisfied(String maxVersion, String actualVersion){

        String[] actualVersionSplit = actualVersion.split("\\.");
        String[] maxVersionSplit = maxVersion.split("\\.");

        if(actualVersionSplit.length != maxVersionSplit.length){
            throw new IllegalArgumentException();
        }

        for(int i = actualVersionSplit.length-1; i >=0; i--){
            if (Integer.parseInt(actualVersionSplit[i]) < Integer.parseInt(maxVersionSplit[i])){
                return true;
            }
        }
        return false;
    }

    public static boolean requiredLibrariesAvailable(Map<String, String> requiredLibraries){
        Map<String, String> availableLibraries = getAvailableLibraries();

        for(String lib:requiredLibraries.keySet()){
            if(availableLibraries.containsKey(lib)){
                String requiredVersion = requiredLibraries.get(lib);
                String availableVersion = availableLibraries.get(lib);
                if(!minVersionSatisfied(requiredVersion, availableVersion)){
                    // version too low
                    return false;
                }
            } else {
                // library can not be found
                return false;
            }
        }
        return true;
    }
    public static boolean requiredLibrariesAvailable(String requirementsFile){
        return requiredLibrariesAvailable(readRequirements(requirementsFile));
    }
}
