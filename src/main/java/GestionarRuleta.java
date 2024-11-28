import java.io.*;
import java.net.Socket;
import java.sql.SQLOutput;
import java.util.ArrayList;

public class GestionarRuleta implements Runnable{
    private Socket s;
    private boolean logged=false;
    private boolean seguir=true;
    private int ganancias;
    private ArrayList<String> apuestas=null;
    private boolean primera=false;
    private  String usr;
    private ServidorRuleta sr;
    public GestionarRuleta(Socket socket,ServidorRuleta sr){s=socket;ganancias=0;this.sr=sr;}
    @Override
    public void run()
    {
        long aux1=System.currentTimeMillis();
        try
        {
            BufferedWriter writer=new BufferedWriter(new OutputStreamWriter(s.getOutputStream(),"UTF-8"));
            ObjectInputStream ois=new ObjectInputStream(s.getInputStream());


            if(sr.getMap().size()==1)
            {
                sr.iniciarTemporizador();
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
                    System.out.println(usr+": "+sr.getFin());
                    aux=sr.getFin();
                    writer.write("T"+(sr.getFin())+"\n");
                    writer.flush();
                    usr=(String)ois.readObject();
                    usr=usr.substring(1);
                    System.out.println("U: "+usr);
                    primera=true;
                }
                apuestas = (ArrayList<String>) ois.readObject();
                for (String apuesta : apuestas) {
                    System.out.println("Apuesta: "+apuesta);
                    ganancias += calcular(apuesta);
                    //System.out.println(ganancias);
                }
                ArrayList<String> datos=sr.getMap().get(usr);
                datos.set(1,(ganancias+Integer.parseInt(datos.get(1)))+"");
                sr.actualizarUsuarios(usr,datos);
                sr.actualizarRank(usr,ganancias);


                writer.write("N"+sr.getGanador() + "\n");
                writer.flush();
                //System.out.println(sr.getGanador());
                String res = "G" + ganancias;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                String leaderboard = sr.generateLeaderboard();
                res += "," + leaderboard;
                res += "\n";
                writer.write(res);
                writer.flush();
                //System.out.println(res);
                //System.out.println(ganancias);
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
                ArrayList<String> datos1 = sr.getMap().get(usr);
                datos1.remove(2);
                datos1.add("F");
                sr.actualizarUsuarios(usr,datos1);
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
        int ganador=sr.getGanador();
        String color=sr.getColorGanador();
        String s1=apuesta.split(" ")[0];
        int valor=Integer.parseInt(apuesta.split(" ")[1]);
        ArrayList<String> datos=sr.getMap().get(usr);
        datos.set(1,(Integer.parseInt(datos.get(1))-valor)+"");
        sr.getMap().put(usr,datos);
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