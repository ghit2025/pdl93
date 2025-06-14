package AnSintDesRec;

import ALexico.AnLexico;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class AnSemantico {
    private TablaSimbolos tablaSimbolos;
    private Map<String, Map<String, Object>> tablaLexico; // tabla original del léxico para desplazamientos
    private AnLexico lexico;
    private String funcionActual;
    private StringBuilder dumpLocales = new StringBuilder();

    // Constructor que recibe la tabla de símbolos ya creada por el léxico
    public AnSemantico(AnLexico lexico, Map<String, Map<String, Object>> tablaLexico) {
        this.lexico = lexico;
        this.tablaLexico = tablaLexico;
        this.tablaSimbolos = new TablaSimbolos();
        this.funcionActual = null;
    }

    // ==================== FUNCIONES PRINCIPALES SEGÚN PDF SEMÁNTICO ====================

    /**
     * Añade el tipo de un identificador a la tabla de símbolos
     * Función obligatoria según el PDF de análisis semántico
     */
    public void anadeTipoTS(String lexema, String tipo) {
        EntradaTS e = tablaSimbolos.buscar(funcionActual, lexema);
        if (e == null) {
            e = new EntradaTS();
            tablaSimbolos.insertar(funcionActual, lexema, e);
        }
        e.put("tipo", tipo);
    }


    /**
     * Busca el tipo de un identificador en la tabla de símbolos
     * Función obligatoria según el PDF de análisis semántico
     */
    public String buscaTipoTS(String lexema) {
        EntradaTS e = tablaSimbolos.buscar(funcionActual, lexema);
        if (e == null) {
            throw new RuntimeException("Error semántico: Variable '" + lexema + "' no declarada en la línea " + lexico.getLinea());
        }

        String tipo = (String) e.get("tipo");

        if (tipo == null) {
            throw new RuntimeException("Error semántico: Variable '" + lexema +
                    "' usada antes de ser declarada en la línea " + lexico.getLinea());
        }

        return tipo;
    }

    /**
     * Añade el desplazamiento de un identificador (ya implementado en léxico, pero por completitud)
     */
    public void anadeDesplTS(String lexema, int despl) {
        EntradaTS e = tablaSimbolos.buscar(funcionActual, lexema);
        if (e == null) {
            e = new EntradaTS();
            tablaSimbolos.insertar(funcionActual, lexema, e);
        }
        e.put("despl", despl);
    }

    /**
     * Busca el desplazamiento de un identificador
     */
    public int buscaDesplTS(String lexema) {
        EntradaTS e = tablaSimbolos.buscar(funcionActual, lexema);
        if (e == null) {
            throw new RuntimeException("Error semántico: Variable '" + lexema + "' no declarada en la línea " + lexico.getLinea());
        }

        Integer despl = (Integer) e.get("despl");

        if (despl == null) {
            throw new RuntimeException("Error semántico: Variable '" + lexema +
                    "' sin desplazamiento asignado en la línea " + lexico.getLinea());
        }

        return despl;
    }

    // ==================== FUNCIONES SEMÁNTICAS ESPECÍFICAS ====================

    public void validarDeclaracion(String lexema, String tipo) {
        EntradaTS e = tablaSimbolos.buscar(funcionActual, lexema);
        if (e != null && e.get("tipo") != null && e.get("categoria") != null) {
            throw new RuntimeException("Error semántico: Variable '" + lexema + "' ya declarada en la línea " + lexico.getLinea());
        }

        anadeTipoTS(lexema, tipo);
        anadeCategoriaTS(lexema, "variable");
    }



    /**
     * Valida una asignación (reglas S' → = E ; y S' → |= E ;)
     */
    public void validarAsignacion(String lexema, String tipoExpresion, String operador) {
        String tipoVariable = buscaTipoTS(lexema);

        if (operador.equals("|=")) {
            // Operador |= solo válido para boolean (especificación de tu grupo)
            if (!tipoVariable.equals("boolean") || !tipoExpresion.equals("boolean")) {
                throw new RuntimeException("Error semántico: Operador '|=' solo válido para tipo 'boolean' en la línea " + lexico.getLinea());
            }
        } else if (operador.equals("=")) {
            // Asignación normal - tipos deben ser compatibles
            if (!esCompatibleAsignacion(tipoVariable, tipoExpresion)) {
                throw new RuntimeException("Error semántico: No se puede asignar '" + tipoExpresion +
                        "' a variable de tipo '" + tipoVariable + "' en la línea " + lexico.getLinea());
            }
        }
    }

    /**
     * Comprueba compatibilidad de tipos en operaciones (reglas de expresiones)
     */
    public String comprobarTipos(String tipo1, String tipo2, String operacion) {
        if (tipo1.equals("tipo_error") || tipo2.equals("tipo_error")) {
            return "tipo_error";
        }

        switch (operacion) {
            case "+":
                if (tipo1.equals("int") && tipo2.equals("int")) {
                    return "int";
                } else if (tipo1.equals("string") && tipo2.equals("string")) {
                    return "string";
                }
                break;
            case ">":
                if (tipo1.equals("int") && tipo2.equals("int")) {
                    return "boolean";
                } else if (tipo1.equals("string") && tipo2.equals("string")) {
                    return "boolean";
                }
                break;
            case "!":
                if (tipo1.equals("boolean")) {
                    return "boolean";
                }
                break;
        }

        throw new RuntimeException("Error semántico: Operador '" + operacion +
                "' no válido entre '" + tipo1 + "' y '" + tipo2 + "' en la línea " + lexico.getLinea());
    }

    /**
     * Añade categoría a un identificador (variable, funcion, etc.)
     */
    public void anadeCategoriaTS(String lexema, String categoria) {
        EntradaTS e = tablaSimbolos.buscar(funcionActual, lexema);
        if (e == null) {
            e = new EntradaTS();
            tablaSimbolos.insertar(funcionActual, lexema, e);
        }
        e.put("categoria", categoria);
    }

    /**
     * Añade información de función a la tabla de símbolos (regla F → function H id (A) {C})
     */
    public void validarDeclaracionFuncion(String lexema, String tipoRetorno, int numParam) {
        EntradaTS e = tablaSimbolos.buscar(null, lexema);
        if (e != null && e.get("tipo") != null) {
            throw new RuntimeException("Error semántico: Función '" + lexema + "' ya declarada en la línea " + lexico.getLinea());
        }

        if (e == null) {
            e = new EntradaTS();
            tablaSimbolos.insertar(null, lexema, e);
        }
        e.put("tipo", "funcion");
        e.put("categoria", "funcion");
        e.put("TipoRetorno", tipoRetorno);
        e.put("numParam", numParam);
    }

    /**
     * Añade tipo de parámetro a una función
     */
    public void anadeTipoParamTS(String lexema, int numeroParam, String tipoParam) {
        EntradaTS e = tablaSimbolos.buscar(null, lexema);
        if (e == null) {
            throw new RuntimeException("Error semántico: Función '" + lexema + "' no declarada en la línea " + lexico.getLinea());
        }

        e.put("TipoParam" + String.format("%02d", numeroParam), tipoParam);
    }

    // ==================== FUNCIONES AUXILIARES ====================

    /**
     * Verifica compatibilidad de tipos en asignación
     */
    private boolean esCompatibleAsignacion(String tipoVariable, String tipoExpresion) {
        if (tipoExpresion.equals("tipo_error")) {
            return false;
        }
        return tipoVariable.equals(tipoExpresion);
    }

    /**
     * Obtiene el tipo básico de un literal según el código del token
     */
    public String obtenerTipoLiteral(String codigoToken) {
        switch (codigoToken) {
            case "entero":
                return "int";
            case "cadena":
                return "string";
            default:
                return "tipo_error";
        }
    }

    /**
     * Obtiene el lexema de un token id usando su desplazamiento
     */
    public String obtenerLexemaPorDespl(int desplazamiento) {
        for (Map.Entry<String, Map<String, Object>> entrada : tablaLexico.entrySet()) {
            String lexema = entrada.getKey();
            Map<String, Object> atributos = entrada.getValue();
            Integer despl = (Integer) atributos.get("despl");
            if (despl != null && despl == desplazamiento) {
                return lexema;
            }
        }
        throw new RuntimeException("Error semántico: No se encontró lexema con desplazamiento " +
                desplazamiento + " en la línea " + lexico.getLinea());
    }


    // ==================== GETTERS PARA DEBUGGING ====================

    public TablaSimbolos getTablaSimbolos() {
        return tablaSimbolos;
    }

    public void iniciarFuncion(String nombre) {
        funcionActual = nombre;
        tablaSimbolos.crearLocal(nombre);
    }

    public void finalizarFuncion() {
        if (funcionActual != null) {
            dumpLocales.append(tablaSimbolos.dumpLocal(funcionActual));
            tablaSimbolos.eliminarLocal(funcionActual);
            funcionActual = null;
        }
    }

    public void volcarTablas(String archivo) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(archivo))) {
            bw.write(tablaSimbolos.dump());
            bw.write(dumpLocales.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public AnLexico getLexico() {
        return lexico;
    }
}
