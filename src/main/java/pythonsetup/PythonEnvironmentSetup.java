package pythonsetup;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;

import static org.bytedeco.cpython.helper.python.Py_AddPath;

public class PythonEnvironmentSetup {
    private Map<String, String> config;
    private final String requirementInstallationScript = "./src/main/python/install_requirements.py";
    public PythonEnvironmentSetup(String configFile) {
        this.config = readConfig(configFile);
    }

    public void initialize(){
        //if not target path is given, declare default path
        if (config.get("targetPath").equals("")){
            config.put("targetPath", System.getProperty("user.home") + "/exampleProject_pythonLibraries");
        }

        //install requirements
         callRequirementsInstallationScript();
        //add path to dependencies
        addPythonPath(config.get("targetPath"));
        //add path to nlp_doc
        addPythonPath("./src/main/python");


    }
    private Map readConfig(String configFile){

        try {
            ObjectMapper mapper = new ObjectMapper();

            return  mapper.readValue(Paths.get(configFile).toFile(), Map.class);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Collections.emptyMap();
    }
    public void callRequirementsInstallationScript(){
        ProcessBuilder processBuilder = new ProcessBuilder("python3", requirementInstallationScript);
        processBuilder.redirectErrorStream(true);

        Process process = null;
        try {
            process = processBuilder.start();
            String result = IOUtils.toString(process.getInputStream(), StandardCharsets.UTF_8);
            int exitCode = process.waitFor();
            System.out.println("script finished with exit code: " + exitCode + " and output: \n" + result);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
    public static void addPythonPath(String path){
        try {
            Py_AddPath(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getRequirementsFilePath(){
        return config.get("reqFile");
    }

    public String getRequirementInstallationScript() {
        return requirementInstallationScript;
    }
}
