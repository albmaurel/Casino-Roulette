import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Servidor {
    private static int contador=40;
    private static int fase=0;
    private static int Ganador;
    private static String colorG;

    public static void main(String[] args)
    {
        ExecutorService pool = Executors.newCachedThreadPool();
        Timer t=new Timer();
        Tempo tempo=new Tempo();
        t.scheduleAtFixedRate(tempo,new Date(),1000);
        try( ServerSocket ss=new ServerSocket(55555);)
        {
            try {

                while(true) {
                    Socket cl = ss.accept();
                    Thread th = new Thread(new GestionarApuesta(cl));
                    th.start();
                }
            } catch (IOException e) {
                e.printStackTrace();            }
        } catch (IOException e) {
            e.printStackTrace();
            pool.shutdown();
        }
    }
    public static void decCont()
    {
        contador--;
    }
    public static void setCont(int n)
    {
        contador=n;
    }
    public static int getCont()
    {
        return contador;
    }
    public static void setFase(int c)
    {
        fase=c;
    }
    public static int getFase()
    {
        return fase;
    }
    public static void setGanador(String c, int num)
    {
        colorG=c;
        Ganador=num;
    }
    public static int getGanador()
    {
        return Ganador;
    }
    public static String getColorG()
    {
        return colorG;
    }
}
