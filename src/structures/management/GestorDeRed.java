package structures.management;

import model.LecturaSensor;
import java.util.*;
import java.util.stream.Collectors;

/**
 * GestorDeRed - Módulo Integrador de Estructuras de Datos
 * VERSION ESTUDIANTE
 *
 * Objetivo: Implementar la gestión de la red utilizando las colecciones de Java (JCF).
 * El éxito de este módulo no es solo que el código compile, sino que elijas
 * la estructura correcta para cada problema.
 */
public class GestorDeRed {
    // 1. Historial de Eventos: Registro lineal de todas las actividades
    private List<String> historialEventos;

    // 2. Navegador de Alertas: Permite moverse adelante y atrás entre alertas
    private Deque<String> navegadorAlertas;
    private String alertaActual;

    // 3. Ciclo de Monitoreo: Gestión de la rotación de sensores a consultar
    private Queue<String> colaMonitoreo;

    // 4. Topología de la Red: Grafo de conexiones entre estaciones (Lista de adyacencia)
    private Map<String, List<String>> topologiaRed;

    // 5. Buffer de Ingesta: Cola de espera para procesar lecturas entrantes
    private Queue<LecturaSensor> bufferIngesta;



    public GestorDeRed() {   // Constructor: se ejecuta al crear un GestorDeRed y deja las 5 colecciones listas
        // LinkedList: borrar el primer elemento es O(1) (en ArrayList seria O(n)).
        this.historialEventos = new LinkedList<>();   // Lista doblemente enlazada para el historial de eventos
        this.navegadorAlertas = new ArrayDeque<>();   // Deque (cola de dos extremos) para navegar alertas adelante/atrás
        this.colaMonitoreo = new ArrayDeque<>();      // Cola FIFO con el ciclo de sensores a consultar
        this.topologiaRed = new HashMap<>();          // Mapa sensor -> lista de vecinos (lista de adyacencia del grafo)
        this.bufferIngesta = new ArrayDeque<>();      // Cola FIFO con las lecturas pendientes de procesar
    }   // Fin del constructor



    // =========================================================================
    // SECCIÓN 1: HISTORIAL Y EVENTOS
    // =========================================================================



    public void agregarEvento(String evento) {   // Metodo público que recibe la descripción del evento, ej. "Sensor EST-01 activado"
        // new java.util.Date() crea la fecha/hora actual; SimpleDateFormat la convierte a texto "año-mes-día hora:minuto"
        String marca = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(new java.util.Date());
        // Agrega al FINAL del historial: la marca entre corchetes + un espacio + el texto del evento
        historialEventos.add("[" + marca + "] " + evento);
    }



    public List<String> obtenerEventosRecientes(int limite) {   // Devuelve los últimos 'limite' eventos del historial
        if (limite <= 0) return new ArrayList<>();   // Si piden 0 o negativo, devuelve una lista vacía
        if (historialEventos.size() <= limite) return new ArrayList<>(historialEventos);   // Si hay menos eventos que el límite, devuelve una copia de todos
        // Se devuelve una COPIA: subList es solo una vista del historial.
        // subList(desde, hasta) toma los últimos 'limite' elementos; new ArrayList<>(...) los copia a una lista independiente
        return new ArrayList<>(historialEventos.subList(historialEventos.size() - limite, historialEventos.size()));
    }



    public void limpiarHistorialAntiguo(int mantener) {   // Conserva solo los últimos 'mantener' eventos
        int limite = Math.max(mantener, 0);   // Si 'mantener' es negativo, se usa 0 para evitar un bucle raro
        while (historialEventos.size() > limite) {   // Mientras haya más eventos de los permitidos...
            historialEventos.remove(0);   // ...elimina el más antiguo (índice 0, el primero); en LinkedList es O(1)
        }
    }



    // =========================================================================
    // SECCIÓN 2: NAVEGACIÓN DE ALERTAS
    // =========================================================================

    public void registrarAlerta(String alerta) {
        navegadorAlertas.addLast(alerta);
        alertaActual = alerta;
    }

    public String navegarAlSiguiente() {
        // TODO 2: Implementar la lógica de desplazamiento hacia adelante.
        // Debe devolver la siguiente alerta o null si no hay más.
        // Recuerda actualizar el estado de 'alertaActual'.
        return null;
    }

    public String navegarAlAnterior() {
        // TODO 3: Implementar la lógica de desplazamiento hacia atrás.
        // Debe devolver la alerta anterior o null si es la primera.
        // Recuerda actualizar el estado de 'alertaActual'.
        return null;
    }

    public String getAlertaActual() { return alertaActual; }

    // =========================================================================
    // SECCIÓN 3: CICLO DE MONITOREO
    // =========================================================================

    public void programarConsulta(String idSensor) {   // Agrega un sensor al ciclo de monitoreo
        if (idSensor == null || colaMonitoreo.contains(idSensor)) return;   // Si el id es nulo o ya está en la cola, no hace nada (evita duplicados)
        colaMonitoreo.add(idSensor);   // Si no estaba, lo agrega al final de la cola
    }

    public String obtenerSiguienteSensor() {   // Devuelve el sensor que toca consultar y lo deja al final de la fila
        String sensor = colaMonitoreo.poll();   // Saca el sensor del frente de la cola (null si la cola está vacía)
        if (sensor != null) {   // Solo si realmente había un sensor...
            colaMonitoreo.add(sensor);   // ...lo vuelve a meter al final: así se logra la rotación (round-robin)
        }   // Fin del if
        return sensor;   // Devuelve el sensor consultado (o null si no había ninguno)
    }

    // =========================================================================
    // SECCIÓN 4: TOPOLOGÍA DE RED
    // =========================================================================

    public void registrarConexion(String sensorA, String sensorB) {   // Conecta dos sensores en ambos sentidos
        if (sensorA == null || sensorB == null || sensorA.equals(sensorB)) return;   // Ignora ids nulos y conexiones de un sensor consigo mismo
        // computeIfAbsent: devuelve la lista de vecinos de A; si A no existía en el mapa, crea una lista vacía y la guarda
        List<String> vecinosA = topologiaRed.computeIfAbsent(sensorA, k -> new ArrayList<>());
        // Lo mismo para B: así ambos sensores quedan registrados en el grafo
        List<String> vecinosB = topologiaRed.computeIfAbsent(sensorB, k -> new ArrayList<>());
        if (!vecinosA.contains(sensorB)) vecinosA.add(sensorB);   // Agrega B como vecino de A solo si no estaba (evita duplicados)
        if (!vecinosB.contains(sensorA)) vecinosB.add(sensorA);   // Agrega A como vecino de B solo si no estaba (conexión bidireccional)
    }

    /** Registra una estacion en la red sin conectarla (nace aislada). */
    public void registrarSensor(String idSensor) {   // Metodo nuevo: permite que exista un sensor sin conexiones
        if (idSensor == null) return;   // Ignora ids nulos
        topologiaRed.computeIfAbsent(idSensor, k -> new ArrayList<>());   // Si no existe, lo crea con lista de vecinos vacía (queda aislado)
    }

    public List<String> obtenerVecinos(String idSensor) {   // Devuelve los sensores conectados a idSensor
        // getOrDefault: si el sensor no existe en el mapa devuelve una lista vacía (evita null);
        // new ArrayList<>(...) devuelve una COPIA para que quien llama no pueda alterar el grafo interno
        return new ArrayList<>(topologiaRed.getOrDefault(idSensor, Collections.emptyList()));
    }

    public List<String> detectarEstacionesAisladas() {   // Devuelve los sensores registrados que no tienen ninguna conexión
        List<String> aisladas = new ArrayList<>();   // Lista donde se acumularán los sensores aislados
        for (Map.Entry<String, List<String>> entrada : topologiaRed.entrySet()) {   // Recorre cada par (sensor -> lista de vecinos) del mapa
            if (entrada.getValue().isEmpty()) {   // Si la lista de vecinos de ese sensor está vacía...
                aisladas.add(entrada.getKey());   // ...el sensor está aislado: se agrega su id al resultado
            }   // Fin del if
        }   // Fin del for
        return aisladas;   // Devuelve la lista (vacía si ninguno está aislado)
    }

    // =========================================================================
    // SECCIÓN 5: BUFFER DE INGESTA
    // =========================================================================

    public void recibirLectura(LecturaSensor lectura) {
        bufferIngesta.add(lectura);
    }

    public LecturaSensor procesarSiguienteLectura() {   // Procesa la lectura más antigua del buffer (orden FIFO)
        return bufferIngesta.poll();   // poll() saca y devuelve el primero de la cola; si está vacía devuelve null (no lanza excepción)
    }

    public List<LecturaSensor> filtrarAlertasCriticas(double limitePm25) {
        // TODO EXTRA: Usar Streams para filtrar lecturas con PM2.5 superior al límite.
        // Debe retornar una lista con las lecturas filtradas.
        return null;
    }
}
