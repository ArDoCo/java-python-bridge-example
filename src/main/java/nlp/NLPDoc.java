package nlp;

import org.nd4j.python4j.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class NLPDoc {

    private Map<String, String> POSMap;

    public NLPDoc(String text) {
        try(PythonGIL pythonGIL = PythonGIL.lock()) {
            try (PythonGC gc = PythonGC.watch()) {
                //import the python class
                PythonExecutioner.exec("from nlp_doc import NLPDoc");
                //instantiate python class with text phrase
                PythonExecutioner.exec("nlp_doc = NLPDoc(text)",
                        Collections.singletonList(new PythonVariable<>("text", PythonTypes.STR, text)), null
                );
                this.POSMap = readInPOS();
            }
        }
    }
    private Map<String, String> readInPOS(){
        //define output variable
        PythonVariable<Map>posDict = new PythonVariable<>("pos_dict", PythonTypes.DICT);
        //read out python dict into posDict
        PythonExecutioner.exec("pos_dict = nlp_doc.get_pos()", null, Collections.singletonList(posDict));
        //return Map from posDict
        return posDict.getValue();
    }
    public Map<String, String> getPOSMap(){
        return this.POSMap;
    }
}
