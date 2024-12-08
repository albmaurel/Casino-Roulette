import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServidorPrincipal {
    private static ServerSocket ss;
    private static int puerto=55555;
    private static int puertos;
    private static ConcurrentHashMap<String, ServidorRuleta> ruletas; //Contedrá las ruletas activas
    private static ConcurrentHashMap<String,ArrayList<String>> registrados=new ConcurrentHashMap<>();//Contedrá los usuarios que se han registrado alguna vez en alguna ruleta
    private static ExecutorService pool; // Pool de hilos para manejar clientes


    public static void main(String[] args) {

        // Aceptar ruletas indefinidamente
        puertos=55555;
        try {
            ss= new ServerSocket(puerto);
            System.out.println("ServidorPrincipar iniciado en el puerto: "+puerto);
            ruletas=new ConcurrentHashMap<>();
            pool=Executors.newCachedThreadPool();
            while(true)
            {
                Socket socket= ss.accept();
                System.out.println("Nuevo cliente conectado desde "+ socket.getInetAddress());
                pool.execute(new GestionarServidor(socket));
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            pool.shutdown();
        }


    }
    //Recuperar los registrados
    public static ConcurrentHashMap<String,ArrayList<String>> getRegistrados()
    {
        return registrados;
    }
    //Añadir un nuevo registrado
    public static void putRegistrados(String nom,ArrayList<String> datos)
    {
        registrados.put(nom,datos);
    }
    //Recuperar las ruletas
    public static ConcurrentHashMap<String,ServidorRuleta> getRuletas()
    {
        return ruletas;
    }
    //Añadir una nueva ruleta
    public static void putRuletas(String nom,ServidorRuleta s)
    {
        ruletas.put(nom,s);
    }
    //Incrementar el número de puerto y devolver el nuevo
    public static synchronized int getPuerto()
    {
        puertos++;
        return puertos;
    }
}