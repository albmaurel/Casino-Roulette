import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class GestionarRuleta implements Runnable{
    private Socket s;
    private boolean logged=false;
    private boolean seguir=true;
    private int ganancias;
    ArrayList<String> apuestas=null;
    private boolean primera=false;
    private String usr;
    public GestionarRuleta(Socket socket){s=socket;ganancias=0;}
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

                    if (!ServidorRuleta.getMap().keySet().contains(usuario)) {
                        usr=usuario;
                        ArrayList<String> e = new ArrayList<>();
                        e.add(contrasena);
                        e.add("10000");
                        e.add("T");
                        ServidorRuleta.actualizarUsuarios(usuario,e);
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
                    if (ServidorRuleta.getMap().keySet().contains(usuario)) {
                        ArrayList<String> datos = ServidorRuleta.getMap().get(usuario);
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
                            ServidorRuleta.actualizarUsuarios(usuario,datos);
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
            if(ServidorRuleta.getMap().size()==1)
            {
                ServidorRuleta.iniciarTemporizador();
                long waitTime = aux1 + 100 - System.currentTimeMillis();
                if (waitTime > 0) {
                    try {
                        Thread.sleep(waitTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            long aux;
            while (seguir)
            {
                ganancias=0;
                apuestas=null;
                if(primera==false) {
                    System.out.println(usr+": "+ServidorRuleta.getFin());
                    aux=ServidorRuleta.getFin();
                    writer.write("T"+(ServidorRuleta.getFin())+"\n");
                    writer.flush();
                    primera=true;
                }
                apuestas = (ArrayList<String>) ois.readObject();
                for (String apuesta : apuestas) {
                    System.out.println("Apuesta: "+apuesta);
                    ganancias += calcular(apuesta);
                    //System.out.println(ganancias);
                }
                ArrayList<String> datos=ServidorRuleta.getMap().get(usr);
                datos.set(1,(ganancias+Integer.parseInt(datos.get(1)))+"");
                ServidorRuleta.actualizarUsuarios(usr,datos);
                ServidorRuleta.actualizarRank(usr,ganancias);


                writer.write("N"+ServidorRuleta.getGanador() + "\n");
                writer.flush();
                System.out.println(ServidorRuleta.getGanador());
                String res = "G" + ganancias;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                String leaderboard = ServidorRuleta.generateLeaderboard();
                res += "," + leaderboard;
                res += "\n";
                writer.write(res);
                writer.flush();
                System.out.println(res);
                System.out.println(ganancias);
                // Para que te lo mande en el 0
                long waitTime = aux1 + 100 - System.currentTimeMillis()+1000;
                if (waitTime > 0) {
                    try {
                        Thread.sleep(waitTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
        catch(IOException e)
        {
            try {
                ArrayList<String> datos1 = ServidorRuleta.getMap().get(usr);
                datos1.remove(2);
                datos1.add("F");
                ServidorRuleta.actualizarUsuarios(usr,datos1);
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
        int ganador=ServidorRuleta.getGanador();
        String color=ServidorRuleta.getColorGanador();
        String s1=apuesta.split(" ")[0];
        int valor=Integer.parseInt(apuesta.split(" ")[1]);
        ArrayList<String> datos=ServidorRuleta.getMap().get(usr);
        datos.set(1,(Integer.parseInt(datos.get(1))-valor)+"");
        ServidorRuleta.getMap().put(usr,datos);
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