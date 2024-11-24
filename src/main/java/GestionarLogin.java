
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

public class GestionarLogin implements Runnable{
    private Socket socket;
    public GestionarLogin(Socket socket)
    {
        this.socket=socket;
    }
    @Override
    public void run() {
        String usr;
        boolean logged=false;
        try
        {
            BufferedWriter writer=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"UTF-8"));
            ObjectInputStream ois=new ObjectInputStream(socket.getInputStream());

            while(!logged) {

                String leido = (String) ois.readObject();
                String aux=leido.split(" ")[0];
                String usuario=leido.split(" ")[1];
                String contrasena=leido.split(" ")[2];
                if(aux.equals("C")) {

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
            while(!elegida)
            {
                String leido=(String) ois.readObject();
                String opcion=leido.split(" ")[1];
                String operacion=leido.split(" ")[0];
                if(operacion.equals("C"))
                {
                    if(!ServidorPrincipal.getRuletas().keySet().contains(opcion))
                    {
                        int puerto=ServidorPrincipal.getPuerto();
                        ServidorRuleta nueva= new ServidorRuleta(opcion,puerto);
                        ServidorPrincipal.putRuletas(opcion, nueva);
                        writer.write("O"+puerto+"\n");
                        writer.flush();
                        elegida=true;
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
                        writer.write("O\n");
                        writer.flush();
                        elegida=true;
                        new Thread(ServidorPrincipal.getRuletas().get(opcion)).start();		            	}
                    else
                    {
                        writer.write("I\n");
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
        }
        finally
        {
            try
            {socket.close();}
            catch (IOException e){e.printStackTrace();}
        }
    }

}
