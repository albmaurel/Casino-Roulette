import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Set;

public class GestionarApuesta implements Runnable{
    private Socket s;
    private int auxiliar=-1;
    private int ganancias;
    private int contador;
    ArrayList<String> apuestas=null;
    public GestionarApuesta(Socket socket){s=socket;ganancias=0;}
    @Override
    public void run()
    {
        try
        {
            BufferedWriter writer=new BufferedWriter(new OutputStreamWriter(s.getOutputStream(),"UTF-8"));
            ObjectInputStream ois=new ObjectInputStream(s.getInputStream());
            while(true)
            {
                contador=Servidor.getContador();
                if(auxiliar!=contador)
                {
                    auxiliar=Servidor.getContador();
                    if(auxiliar==25)
                    {
                        int res=auxiliar-25;
                        writer.write(res + "4\n");
                        writer.flush();
                    }else if(auxiliar==0){
                        writer.write(auxiliar + "2\n");
                        writer.flush();
                    }else if(auxiliar<=24 && auxiliar>20)
                    {
                        if(apuestas==null) {
                            apuestas = (ArrayList<String>) ois.readObject();
                            for (String apuesta : apuestas) {
                                ganancias += calcular(apuesta);
                                System.out.println(ganancias);
                            }
                        }
                    }
                    else if(auxiliar==20)
                    {
                        apuestas=null;
                        writer.write("S"+auxiliar + "\n");
                        System.out.println("S"+auxiliar);
                        writer.write(Servidor.getGanador() + "\n");
                        System.out.println(Servidor.getGanador());
                        writer.write(ganancias+"\n");
                        System.out.println(ganancias);
                        writer.flush();
                    }
                    else if(auxiliar>25)
                    {
                        int auxi=auxiliar-25;
                        writer.write(auxi + "\n");
                        writer.flush();
                    }
                    else
                    {
                        writer.write(auxiliar + "\n");
                        writer.flush();
                    }
                }
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally
        {
            try
            {s.close();}
            catch (IOException e){e.printStackTrace();}
        }


    }
    private int calcular(String apuesta)
    {
        int ganador=Servidor.getGanador();
        String color=Servidor.getColorGanador();
        String s1=apuesta.split(" ")[0];
        int valor=Integer.parseInt(apuesta.split(" ")[1]);
        if(s1.length()==1)
        {
            if(s1.equals(color))
            {
                return 2*valor;
            }
            else if(ganador%2==0 && s1.equals("P"))
            {
                return 2*valor;
            }
            else if(ganador%2!=0 && s1.equals("I"))
            {
                return 2*valor;
            }
            else {return 0;}
        }
        else
        {
            int numero=Integer.parseInt(s1.substring(1));
            if(numero==ganador)
            {
                return 36*valor;
            }else{
            return 0;
            }
        }
    }
}
