package src;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ServidorRuleta implements Runnable{
    private final String id;
    private int puerto;
    private long finCiclo;
    private static int ganador;
    private static String colorGanador;
    private final ScheduledExecutorService temporizador = Executors.newScheduledThreadPool(1); //El temporizador propio de una ruleta
    private final ScheduledExecutorService generador = Executors.newScheduledThreadPool(1); //El generador de números ganadores de una ruleta
    private final ExecutorService poolClientes = Executors.newCachedThreadPool();
    private ConcurrentHashMap<String,ArrayList<String>> registrados=new ConcurrentHashMap<>(); //Los usuarios activos de una ruleta
    private ConcurrentHashMap<String, Integer> rank= new ConcurrentHashMap<>();//Auxiliar para calcular el ranking en cada ronda
    private ArrayList<String> ranked= new ArrayList<>();
    //Constructor
    public ServidorRuleta(String id, int port)
    {
        this.id=id;
        puerto=port;
    }
    @Override
    public void run() {
    	//Creamos el servidor propio de una ruleta
        try (ServerSocket ss= new ServerSocket(puerto)) {
            System.out.println("Servidor ruleta iniciado en el puerto " + puerto);
            while (true) {
                Socket clienteSocket = ss.accept();
                GestionarRuleta cliente = new GestionarRuleta(clienteSocket,this);
                poolClientes.execute(cliente);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            poolClientes.shutdown();
            temporizador.shutdown();
            
        }
    }
    //Recuperar el fin de una ronda
    public synchronized long getFin()
    {
        return finCiclo;
    }
    //Asignar el final del ciclo de una ronda
    public synchronized void setFin(long i)
    {
        finCiclo=i;
    }
    //Recuperar el puerto de la ruleta
    public synchronized int getPuerto() {
    	return puerto;
    } 
    //Recuperar los usuarios registrados en una ruleta
    public ConcurrentHashMap<String,ArrayList<String>> getMap()
    {
        return registrados;
    }
    //Añadir un registrado o actualizar uno existente
    public void actualizarUsuarios(String key, ArrayList<String> datos)
    {
        registrados.put(key, datos);
    }
    //Recuperar el ranking
    public ConcurrentHashMap<String,Integer> getRank()
    {
        return rank;
    }
    //Añadir o sobrescribir elementos del ranking
    public void actualizarRank(String key, int a)
    {
        rank.put(key, a);
    }
    //Reiniciar el ranking
    public void resetRank()
    {
        rank.clear();
    }
    //Generar la leaderboard
    public synchronized String generateLeaderboard() {
        orderRank(); // Actualiza la lista ordenada
        return ranked.stream()
                .map(entry -> entry.split(" ")[0] + " " + entry.split(" ")[1]) // Formato "usuario ganancias"
                .reduce("", (acc, entry) -> acc + "," + entry)
                .substring(1); // Elimina la coma inicial
    }
    //Función para ordenar la leaderboard 
    public synchronized void orderRank() {
        ranked = rank.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())) // Orden descendente
                .map(e -> e.getKey() + " " + e.getValue()) // Formato "usuario ganancias"
                .collect(Collectors.toCollection(ArrayList::new));
    }
    //Recuperar la clasificación
    public ArrayList<String> getRanked()
    {
        return ranked;
    }
    //Dar cominezo al temoporizador que controla los tiempos de una ronda
    public void iniciarTemporizador() {
        temporizador.scheduleAtFixedRate(new Runnable() {
        	public void run() {
                setFin(System.currentTimeMillis()+55000);
                resetRank();
        	}
        }, 0, 55, TimeUnit.SECONDS);
        generador.scheduleAtFixedRate(new GeneraGanador(),0,55, TimeUnit.SECONDS);

    }
    //Recuperar el número ganador
    public static int getGanador() {
        return ganador;
    }
    //Recuperar el color del número ganador
    public static String getColorGanador() {
        return colorGanador;
    }
    //Establecer un número ganador y su color
    public static void setGanador(String color,int numero) {
        ganador = numero;
        colorGanador = color;
    }
    //Recuperar el Id
    public String getId()
    {
    	return id;
    }


}