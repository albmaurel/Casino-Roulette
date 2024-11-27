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
    private final int puerto;
    private static long finCiclo;
    private static int ganador;
    private static String colorGanador;
    private static final ScheduledExecutorService temporizador = Executors.newScheduledThreadPool(1);
    private static final ScheduledExecutorService generador = Executors.newScheduledThreadPool(1);
    private static final ExecutorService poolClientes = Executors.newCachedThreadPool();
    private static final ArrayList<GestionarRuleta> clientes = new ArrayList<>();
    private static ConcurrentHashMap<String,ArrayList<String>> registrados=new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, Integer> rank= new ConcurrentHashMap<>();
    private static ArrayList<String> ranked= new ArrayList<>();
    public ServidorRuleta(String id, int puerto)
    {
        this.id=id;
        this.puerto=puerto;
    }
    @Override
    public void run() {
        try (ServerSocket ss = new ServerSocket(puerto)) {
            System.out.println("Servidor ruleta iniciado en el puerto " + puerto);


            while (true) {
                Socket clienteSocket = ss.accept();
                GestionarRuleta cliente = new GestionarRuleta(clienteSocket);
                clientes.add(cliente);
                poolClientes.execute(cliente);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            poolClientes.shutdown();
            temporizador.shutdown();
        }
    }
    public static synchronized long getFin()
    {
        return finCiclo;
    }
    public static synchronized void setFin(long i)
    {
        finCiclo=i;
    }
    public static synchronized ConcurrentHashMap<String,ArrayList<String>> getMap()
    {
        return registrados;
    }
    public static synchronized  void actualizarUsuarios(String key, ArrayList<String> datos)
    {
        registrados.put(key, datos);
    }
    public static synchronized ConcurrentHashMap<String,Integer> getRank()
    {
        return rank;
    }
    public static synchronized  void actualizarRank(String key, int a)
    {
        rank.put(key, a);
    }
    public static synchronized void resetRank()
    {
        rank.clear();
    }

    public static synchronized String generateLeaderboard() {
        orderRank(); // Actualiza la lista ordenada
        return ranked.stream()
                .map(entry -> entry.split(" ")[0] + " " + entry.split(" ")[1]) // Formato "usuario ganancias"
                .reduce("", (acc, entry) -> acc + "," + entry)
                .substring(1); // Elimina la coma inicial
    }

    public static synchronized void orderRank() {
        ranked = rank.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())) // Orden descendente
                .map(e -> e.getKey() + " " + e.getValue()) // Formato "usuario ganancias"
                .collect(Collectors.toCollection(ArrayList::new));
    }
    public static synchronized ArrayList<String> getRanked()
    {
        return ranked;
    }
    public static void iniciarTemporizador() {
        temporizador.scheduleAtFixedRate(new Tempo(), 0, 55, TimeUnit.SECONDS);
        generador.scheduleAtFixedRate(new GeneraGanador(),0,55, TimeUnit.SECONDS);

    }
    public static synchronized int getGanador() {
        return ganador;
    }

    public static synchronized String getColorGanador() {
        return colorGanador;
    }

    public static synchronized void setGanador(String color,int numero) {
        ganador = numero;
        colorGanador = color;
    }
    public void agregarCliente(String idCliente, Socket socketCliente) {
        // TODO Auto-generated method stub

    }






}
