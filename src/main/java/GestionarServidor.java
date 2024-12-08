import java.io.BufferedWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

public class GestionarServidor implements Runnable{
    private Socket socket;
    //Constructor
    public GestionarServidor(Socket socket)
    {
        this.socket=socket;
    }
    @Override
    public void run() {
        String usr="";
        boolean logged=false;
        boolean salir=false;
        try
        {
            BufferedWriter writer=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"UTF-8"));
            ObjectInputStream ois=new ObjectInputStream(socket.getInputStream());

            while(!logged) {
                //En este bucle se gestiona el login
                String leido = (String) ois.readObject();
                if(leido.equals("FIN"))
                {
                    //podria cerrar aqui el socket pero al tenerlo en el finally no es necesario
                    salir=true;
                    break;
                }
                String aux=leido.split(" ")[0];
                String usuario=leido.split(" ")[1];
                String contrasena=leido.split(" ")[2];
                if(aux.equals("C")) {
                    //Caso  nuevo usuario, se le asigna un saldo inicial de 10000
                    if (!ServidorPrincipal.getRegistrados().keySet().contains(usuario)) {
                        usr=usuario;
                        ArrayList<String> e = new ArrayList<>();
                        e.add(contrasena);
                        e.add("10000");
                        e.add("T");
                        ServidorPrincipal.putRegistrados(usuario,e);
                        logged = true;
                        String ruletas="";
                        for(String s:ServidorPrincipal.getRuletas().keySet())
                        {
                            ruletas+=s+" ";
                        }
                        writer.write("S10000"+" "+ruletas+"\n");
                        writer.flush();
                    }
                    else
                    {

                        writer.write("I\n");
                        writer.flush();
                    }
                }else {
                    //Si lo contiene se recupera el saldo que tenía anterormente.
                    if (ServidorPrincipal.getRegistrados().keySet().contains(usuario)) {
                        ArrayList<String> datos = ServidorPrincipal.getRegistrados().get(usuario);
                        if(datos.size()==3 && datos.get(2).equals("T"))
                        {
                            writer.write("I\n");
                            writer.flush();
                        }
                        else if (contrasena.equals(datos.get(0))) {
                            usr=usuario;
                            String ruletas="";
                            for(String s:ServidorPrincipal.getRuletas().keySet())
                            {
                                ruletas+=s+" ";
                            }
                            writer.write("S" + datos.get(1)+ " "+ruletas+ "\n");
                            writer.flush();
                            datos.add("T");
                            ServidorPrincipal.putRegistrados(usuario,datos);
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

            boolean elegida=false;
            while(!elegida && !salir)
            {
                Object o= ois.readObject();
                //Caso cerrar la conexión, leido FIN
                if(o.equals("FIN"))
                {
                    ArrayList<String> datos1 = ServidorPrincipal.getRegistrados().get(usr);
                    datos1.remove(2);
                    datos1.add("F");
                    ServidorPrincipal.putRegistrados(usr, datos1);
                    //no es necesario cerrar socket ya que se cierra en el finally
                    break;
                }
                //Caso elegir un ruleta
                else
                {
                    String leido=(String) o;
                    String opcion=leido.split(" ")[1];
                    String operacion=leido.split(" ")[0];
                    if(operacion.equals("C"))
                    {
                        //Si no existe se crea una nueva ruleta(hasta 20) y se comienza su ejecución
                        if(!ServidorPrincipal.getRuletas().keySet().contains(opcion) && ServidorPrincipal.getRuletas().size()<=20)
                        {
                            int puerto=ServidorPrincipal.getPuerto();
                            ServidorRuleta nueva= new ServidorRuleta(opcion,puerto);
                            ServidorPrincipal.putRuletas(opcion, nueva);
                            elegida=true;
                            ArrayList<String> aux=ServidorPrincipal.getRegistrados().get(usr);
                            nueva.actualizarUsuarios(usr,aux);
                            Thread ruletaThread = new Thread(ServidorPrincipal.getRuletas().get(opcion));
                            ruletaThread.start();
                            writer.write("O"+puerto+"\n");
                            writer.flush();
                        }
                        else
                        {
                            writer.write("I\n");
                            writer.flush();
                        }
                    }
                    else
                    {
                        if(ServidorPrincipal.getRuletas().keySet().contains(opcion))
                        {
                            //Si ya estaba creada simplemente devolvemos su puerto para que el cliente se pueda conectar a ella
                            ArrayList<String> aux=ServidorPrincipal.getRegistrados().get(usr);
                            ServidorPrincipal.getRuletas().get(opcion).actualizarUsuarios(usr,aux);
                            elegida=true;
                            writer.write("O"+ServidorPrincipal.getRuletas().get(opcion).getPuerto()+"\n");
                            System.out.println(ServidorPrincipal.getRuletas().get(opcion).getPuerto());
                            writer.flush();}
                        else
                        {
                            writer.write("I\n");
                            writer.flush();
                        }
                    }
                }
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        finally
        {
            try
            {
                if(socket!=null) {
                    socket.close();
                }
            }
            catch (IOException e){e.printStackTrace();}
        }
    }

}
