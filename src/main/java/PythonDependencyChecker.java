import org.nd4j.python4j.*;

import java.util.Collections;
import java.util.HashMap;

public class PythonDependencyChecker {

    public String getPythonVersion(){
        try(PythonGIL pythonGIL = PythonGIL.lock()) {
            try(PythonGC gc = PythonGC.watch()) {
                PythonVariable out = new PythonVariable<>("version", PythonTypes.STR);
                PythonExecutioner.exec(
                        """
                                import sys
                                version = sys.version.split(' ')[0]
                                """,
                        null, Collections.singletonList(out)
                );
                return (String)out.getValue();
            }
        }
    }

    public HashMap<String, String> getAvailableLibraries() {
        try (PythonGIL pythonGIL = PythonGIL.lock()) {
            try (PythonGC gc = PythonGC.watch()) {
                PythonVariable out = new PythonVariable<>("packages", PythonTypes.DICT);
                PythonExecutioner.exec(
                        """
                                from pip._internal.operations import freeze
                                packages = {x.split('==')[0]:x.split('==')[-1] for x in freeze.freeze()}
                                """,
                        null, Collections.singletonList(out)
                );

                HashMap<String, String> installedModules = (HashMap) out.getValue();
                return installedModules;
            }
        }
    }

    public boolean versionSatisfied(String minimumVersion, String actualVersion){

        String[] actualVersionSplit = actualVersion.split("\\.");
        String[] minimumVersionSplit = minimumVersion.split("\\.");

        if(!(actualVersionSplit.length == minimumVersionSplit.length)){
            throw new IllegalArgumentException();
        }

        for(int i = actualVersionSplit.length-1; i >=0; i--){
            if (Integer.parseInt(actualVersionSplit[i]) < Integer.parseInt(minimumVersionSplit[i])){
                return false;
            }
        }
        return true;
    }

    public boolean requiredLibrariesAvailable(HashMap<String, String> requiredLibraries){
        HashMap<String, String> availableLibraries = getAvailableLibraries();

        for(String lib:requiredLibraries.keySet()){
            if(availableLibraries.keySet().contains(lib)){
                String requiredVersion = requiredLibraries.get(lib);
                String availableVersion = availableLibraries.get(lib);
                if(!versionSatisfied(requiredVersion, availableVersion)){
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
}
