import java.io.IOException;
import java.lang.reflect.Array;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.*;

public class Servidor {
    private static final int PUERTO = 55555;
    private static long finCiclo;
    private static int ganador;
    private static String colorGanador;
    private static final ScheduledExecutorService temporizador = Executors.newScheduledThreadPool(1);
    private static final ScheduledExecutorService generador = Executors.newScheduledThreadPool(1);
    private static final ExecutorService poolClientes = Executors.newCachedThreadPool();
    private static final ArrayList<GestionarApuesta> clientes = new ArrayList<>();
    private static ConcurrentHashMap<String,ArrayList<String>> registrados=new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, Integer> rank= new ConcurrentHashMap<>();
    private static ArrayList<String> ranked= new ArrayList<>();


    public static void main(String[] args) {
        try (ServerSocket ss = new ServerSocket(PUERTO)) {
            System.out.println("Servidor iniciado en el puerto " + PUERTO);


            while (true) {
                Socket clienteSocket = ss.accept();
                GestionarApuesta cliente = new GestionarApuesta(clienteSocket);
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

    public static void orderRank()
    {
        ArrayList<String> lista=new ArrayList<>();
        int menores=0;
        for(String s:rank.keySet())
        {
            for(String t:rank.keySet())
            {
                if(rank.get(s)>rank.get(t))
                {menores++;}
            }
            lista.add(menores,s);
            menores=0;
        }
        ranked=lista;

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

}
