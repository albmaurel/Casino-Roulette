import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.*;

public class Servidor {
    private static final int PUERTO = 55555;
    private static int contador = 65;
    private static int ganador;
    private static String colorGanador;
    private static final ScheduledExecutorService temporizador = Executors.newScheduledThreadPool(1);
    private static final ExecutorService poolClientes = Executors.newCachedThreadPool();
    private static final ArrayList<GestionarApuesta> clientes = new ArrayList<>();

    public static void main(String[] args) {
        try (ServerSocket ss = new ServerSocket(PUERTO)) {
            iniciarTemporizador();
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

    private static void iniciarTemporizador() {
        temporizador.scheduleAtFixedRate(new Tempo(), 0, 1, TimeUnit.SECONDS);
    }

    // Métodos sincronizados para gestionar el estado del servidor
    public static synchronized int getContador() {
        return contador;
    }

    public static synchronized void setContador(int valor) {
        contador = valor;
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
