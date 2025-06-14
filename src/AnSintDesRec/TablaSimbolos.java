package AnSintDesRec;

import java.util.HashMap;
import java.util.Map;

public class TablaSimbolos {
    private Map<String, EntradaTS> global = new HashMap<>();
    private Map<String, Map<String, EntradaTS>> locales = new HashMap<>();

    public Map<String, EntradaTS> getGlobal() {
        return global;
    }

    public Map<String, Map<String, EntradaTS>> getLocales() {
        return locales;
    }

    public void crearLocal(String funcion) {
        locales.putIfAbsent(funcion, new HashMap<>());
    }

    public void eliminarLocal(String funcion) {
        locales.remove(funcion);
    }

    public String dumpLocal(String funcion) {
        StringBuilder sb = new StringBuilder();
        Map<String, EntradaTS> local = locales.get(funcion);
        if (local != null) {
            sb.append("TABLA LOCAL ").append(funcion).append(":\n");
            for (Map.Entry<String, EntradaTS> e : local.entrySet()) {
                sb.append("* LEXEMA : '").append(e.getKey()).append("'\n");
                for (Map.Entry<String, Object> atr : e.getValue().getAtributos().entrySet()) {
                    sb.append("  + ").append(atr.getKey()).append(" : ");
                    Object v = atr.getValue();
                    if (v instanceof String) {
                        sb.append('\'').append(v).append('\'');
                    } else {
                        sb.append(v);
                    }
                    sb.append("\n");
                }
            }
        }
        return sb.toString();
    }

    public void insertar(String funcion, String lexema, EntradaTS entrada) {
        if (funcion == null) {
            global.put(lexema, entrada);
        } else {
            locales.computeIfAbsent(funcion, k -> new HashMap<>()).put(lexema, entrada);
        }
    }

    public EntradaTS buscar(String funcion, String lexema) {
        if (funcion != null) {
            Map<String, EntradaTS> local = locales.get(funcion);
            if (local != null && local.containsKey(lexema)) {
                return local.get(lexema);
            }
        }
        return global.get(lexema);
    }

    public String dump() {
        StringBuilder sb = new StringBuilder();
        sb.append("TABLA GLOBAL:\n");
        for (Map.Entry<String, EntradaTS> e : global.entrySet()) {
            sb.append("* LEXEMA : '").append(e.getKey()).append("'\n");
            for (Map.Entry<String, Object> atr : e.getValue().getAtributos().entrySet()) {
                sb.append("  + ").append(atr.getKey()).append(" : ");
                Object v = atr.getValue();
                if (v instanceof String) {
                    sb.append('\'').append(v).append('\'');
                } else {
                    sb.append(v);
                }
                sb.append("\n");
            }
        }
        for (Map.Entry<String, Map<String, EntradaTS>> func : locales.entrySet()) {
            sb.append("TABLA LOCAL ").append(func.getKey()).append(":\n");
            for (Map.Entry<String, EntradaTS> e : func.getValue().entrySet()) {
                sb.append("* LEXEMA : '").append(e.getKey()).append("'\n");
                for (Map.Entry<String, Object> atr : e.getValue().getAtributos().entrySet()) {
                    sb.append("  + ").append(atr.getKey()).append(" : ");
                    Object v = atr.getValue();
                    if (v instanceof String) {
                        sb.append('\'').append(v).append('\'');
                    } else {
                        sb.append(v);
                    }
                    sb.append("\n");
                }
            }
        }
        return sb.toString();
    }
}
