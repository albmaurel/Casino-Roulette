import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Set;

public class GestionarApuesta implements Runnable{
    private Socket s;
    private boolean logged=false;
    private boolean seguir=true;
    private int ganancias;
    ArrayList<String> apuestas=null;
    private String usr;
    public GestionarApuesta(Socket socket){s=socket;ganancias=0;}
    @Override
    public void run()
    {
        try
        {
            BufferedWriter writer=new BufferedWriter(new OutputStreamWriter(s.getOutputStream(),"UTF-8"));
            ObjectInputStream ois=new ObjectInputStream(s.getInputStream());

            while(!logged) {

                String leido = (String) ois.readObject();
                String aux=leido.split(" ")[0];
                String usuario=leido.split(" ")[1];
                String contrasena=leido.split(" ")[2];
                if(aux.equals("C")) {
                    if (!Servidor.getMap().contains(usuario)) {
                        usr=usuario;
                        ArrayList<String> e = new ArrayList<>();
                        e.add(contrasena);
                        e.add("10000");
                        Servidor.getMap().put(usuario, e);
                        logged = true;
                        writer.write("S10000\n");
                        writer.flush();
                    }
                    else
                    {
                        writer.write("I\n");
                        writer.flush();
                    }
                }else {
                    if (Servidor.getMap().contains(usuario)) {
                        ArrayList<String> datos = Servidor.getMap().get(usuario);
                        if (contrasena.equals(datos.get(0))) {
                            usr=usuario;
                            writer.write("S" + datos.get(1) + "\n");
                            writer.flush();
                            logged = true;
                        }
                    }
                    else {
                        writer.write("I\n");
                        writer.flush();
                    }
                }
            }
            long aux;
            while (seguir)
            {
                aux=Servidor.getInicio();
                writer.write("T"+(Servidor.getInicio()+55000)+"\n");
                writer.flush();
                apuestas = (ArrayList<String>) ois.readObject();
                for (String apuesta : apuestas) {
                    ganancias += calcular(apuesta);
                    System.out.println(ganancias);
                }
                ArrayList<String> datos=Servidor.getMap().get(usr);
                datos.set(1,(ganancias+datos.get(1))+"");
                writer.write("N"+Servidor.getGanador() + "\n");
                writer.flush();
                System.out.println(Servidor.getGanador());

                writer.write("G"+ganancias+"\n");
                writer.flush();
                System.out.println(ganancias);
                ganancias=0;
                apuestas=null;
                // Para que te lo mande en el 0
                while((aux+55000)-System.currentTimeMillis()>0)
                {

                }
            }

        }
        catch(IOException e)
        {
            try {
                s.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        finally
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
        ArrayList<String> datos=Servidor.getMap().get(usr);
        datos.set(1,(Integer.parseInt(datos.get(1))-valor)+"");
        Servidor.getMap().put(usr,datos);
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
