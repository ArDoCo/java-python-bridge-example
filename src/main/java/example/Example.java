package example;

import nlp.NLPDoc;
import pythonsetup.PythonEnvironmentSetup;

import java.util.Map;

public class Example {
    public static void main(String[] args) {
        PythonEnvironmentSetup envSetup =
                new PythonEnvironmentSetup("./src/main/resources/conf.json");

        envSetup.initialize();

        NLPDoc doc = new NLPDoc("This is merely a small test phrase");
        Map<String, String> pos = doc.getPOSMap();

        for(String token: pos.keySet()){
            System.out.println(token + ":" + pos.get(token));
        }
    }
}
