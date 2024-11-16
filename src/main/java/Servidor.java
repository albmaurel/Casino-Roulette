import java.io.IOException;
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


    public static void main(String[] args) {
        try (ServerSocket ss = new ServerSocket(PUERTO)) {
            System.out.println("Servidor iniciado en el puerto " + PUERTO);
            long aux=System.currentTimeMillis();
            while (true) {
                Socket clienteSocket = ss.accept();
                if(clientes.isEmpty())
                {
                    iniciarTemporizador();
                    System.out.println(System.currentTimeMillis());
                    System.out.println(finCiclo);
                    //Desfase auxiliar tiempo clientes
                    while((aux+100)-System.currentTimeMillis()>0)
                    {

                    }
                }
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
    private static void iniciarTemporizador() {
        temporizador.scheduleAtFixedRate(new Tempo(), 0, 55, TimeUnit.MILLISECONDS);
        generador.scheduleAtFixedRate(new GeneraGanador(),0,40, TimeUnit.SECONDS);
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
