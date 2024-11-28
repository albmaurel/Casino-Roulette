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
    private static int puerto;
    private long finCiclo;
    private static int ganador;
    private static String colorGanador;
    private final ScheduledExecutorService temporizador = Executors.newScheduledThreadPool(1);
    private final ScheduledExecutorService generador = Executors.newScheduledThreadPool(1);
    private final ExecutorService poolClientes = Executors.newCachedThreadPool();
    private ConcurrentHashMap<String,ArrayList<String>> registrados=new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Integer> rank= new ConcurrentHashMap<>();
    private ArrayList<String> ranked= new ArrayList<>();
    public ServidorRuleta(String id, int port)
    {
        this.id=id;
        puerto=port;
    }
    @Override
    public void run() {
        try (ServerSocket ss = new ServerSocket(puerto)) {
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
    public synchronized long getFin()
    {
        return finCiclo;
    }
    public synchronized void setFin(long i)
    {
        finCiclo=i;
    }
    public static synchronized int getPuerto() {
        return puerto;
    }
    public ConcurrentHashMap<String,ArrayList<String>> getMap()
    {
        return registrados;
    }
    public void actualizarUsuarios(String key, ArrayList<String> datos)
    {
        registrados.put(key, datos);
        System.out.println(registrados);
    }
    public ConcurrentHashMap<String,Integer> getRank()
    {
        return rank;
    }
    public void actualizarRank(String key, int a)
    {
        rank.put(key, a);
    }
    public void resetRank()
    {
        rank.clear();
    }

    public synchronized String generateLeaderboard() {
        orderRank(); // Actualiza la lista ordenada
        return ranked.stream()
                .map(entry -> entry.split(" ")[0] + " " + entry.split(" ")[1]) // Formato "usuario ganancias"
                .reduce("", (acc, entry) -> acc + "," + entry)
                .substring(1); // Elimina la coma inicial
    }

    public synchronized void orderRank() {
        ranked = rank.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())) // Orden descendente
                .map(e -> e.getKey() + " " + e.getValue()) // Formato "usuario ganancias"
                .collect(Collectors.toCollection(ArrayList::new));
    }
    public ArrayList<String> getRanked()
    {
        return ranked;
    }
    public void iniciarTemporizador() {
        temporizador.scheduleAtFixedRate(new Runnable() {
            public void run() {
                setFin(System.currentTimeMillis()+55000);
                resetRank();
            }
        }, 0, 55, TimeUnit.SECONDS);
        generador.scheduleAtFixedRate(new GeneraGanador(),0,55, TimeUnit.SECONDS);

    }
    public static int getGanador() {
        return ganador;
    }

    public static String getColorGanador() {
        return colorGanador;
    }

    public static void setGanador(String color,int numero) {
        ganador = numero;
        colorGanador = color;
    }

}