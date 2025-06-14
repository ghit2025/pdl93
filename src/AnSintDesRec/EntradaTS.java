package AnSintDesRec;

import java.util.HashMap;
import java.util.Map;

public class EntradaTS {
    private Map<String, Object> atributos = new HashMap<>();

    public void put(String clave, Object valor) {
        atributos.put(clave, valor);
    }

    public Object get(String clave) {
        return atributos.get(clave);
    }

    public Map<String, Object> getAtributos() {
        return atributos;
    }
}
