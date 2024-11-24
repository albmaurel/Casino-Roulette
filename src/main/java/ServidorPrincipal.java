
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
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
    private static ConcurrentHashMap<String, ServidorRuleta> ruletas;
    private static ConcurrentHashMap<String,ArrayList<String>> registrados=new ConcurrentHashMap<>();
    private static ExecutorService pool; // Pool de hilos para manejar clientes


    public static void main(String[] args) {

        // TODO Auto-generated method stub
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
                pool.execute(new GestionarLogin(socket));
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }


    }
    public static synchronized ConcurrentHashMap<String,ArrayList<String>> getRegistrados()
    {
        return registrados;
    }
    public static synchronized void putRegistrados(String nom,ArrayList<String> datos)
    {
        registrados.put(nom,datos);
    }
    public static synchronized ConcurrentHashMap<String,ServidorRuleta> getRuletas()
    {
        return ruletas;
    }
    public static synchronized void putRuletas(String nom,ServidorRuleta s)
    {
        ruletas.put(nom,s);
    }
    public static synchronized int getPuerto()
    {
        puertos++;
        return puertos;
    }
}


