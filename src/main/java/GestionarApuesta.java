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
        long aux1=System.currentTimeMillis();
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

                    if (!Servidor.getMap().keySet().contains(usuario)) {
                        usr=usuario;
                        ArrayList<String> e = new ArrayList<>();
                        e.add(contrasena);
                        e.add("10000");
                        e.add("T");
                        Servidor.actualizarUsuarios(usuario,e);
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
                    if (Servidor.getMap().keySet().contains(usuario)) {
                        ArrayList<String> datos = Servidor.getMap().get(usuario);
                        if(datos.size()==3 && datos.get(2).equals("T"))
                        {
                            writer.write("I\n");
                            writer.flush();
                        }
                        else if (contrasena.equals(datos.get(0))) {
                            usr=usuario;
                            writer.write("S" + datos.get(1) + "\n");
                            writer.flush();
                            datos.add("T");
                            Servidor.actualizarUsuarios(usuario,datos);
                            logged = true;
                        }
                        else
                        {
                            writer.write("I\n");
                            writer.flush();
                        }
                    }
                    else {
                        writer.write("I\n");
                        writer.flush();
                    }
                }
            }
            if(Servidor.getMap().size()==1)
            {
                Servidor.iniciarTemporizador();
                while((aux1+100)-System.currentTimeMillis()>0)
                {

                }
            }
            long aux;
            while (seguir)
            {
                ganancias=0;
                apuestas=null;
                System.out.println(usr+": "+Servidor.getFin());
                aux=Servidor.getFin();
                writer.write("T"+(Servidor.getFin())+"\n");
                writer.flush();
                apuestas = (ArrayList<String>) ois.readObject();
                for (String apuesta : apuestas) {
                    System.out.println("Apuesta: "+apuesta);
                    ganancias += calcular(apuesta);
                    //System.out.println(ganancias);
                }
                ArrayList<String> datos=Servidor.getMap().get(usr);
                datos.set(1,(ganancias+Integer.parseInt(datos.get(1)))+"");
                Servidor.actualizarUsuarios(usr,datos);
                Servidor.actualizarRank(usr,ganancias);


                writer.write("N"+Servidor.getGanador() + "\n");
                writer.flush();
                System.out.println(Servidor.getGanador());
                String res="";
                res="G"+ganancias;
                Servidor.orderRank();
                for(String str: Servidor.getRanked())
                {
                    res+=","+str+" "+Servidor.getRank().get(str);
                }
                res+="\n";
                System.out.println(res);
                writer.write(res);
                writer.flush();
                System.out.println(ganancias);
                // Para que te lo mande en el 0
                while((aux+100)-System.currentTimeMillis()>0)
                {

                }
            }

        }
        catch(IOException e)
        {
            try {
                ArrayList<String> datos1 = Servidor.getMap().get(usr);
                datos1.remove(2);
                datos1.add("F");
                Servidor.actualizarUsuarios(usr,datos1);
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
