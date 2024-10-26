import java.io.*;
import java.net.Socket;

public class GestionarApuesta implements Runnable{
    private Socket s;
    private static int auxiliar=-1;
    public GestionarApuesta(Socket socket){s=socket;}
    @Override
    public void run()
    {
        try
        {
            BufferedWriter writer=new BufferedWriter(new OutputStreamWriter(s.getOutputStream(),"UTF-8"));
            BufferedReader reader=new BufferedReader(new InputStreamReader(s.getInputStream(),"UTF-8"));
            //ObjectInputStream ois=new ObjectInputStream(s.getInputStream());
            while(true)
            {
                if(auxiliar!=Servidor.getCont())
                {
                    auxiliar=Servidor.getCont();
                    writer.write(""+auxiliar+"\n");
                    writer.flush();
                }
                System.out.println("");
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {s.close();}
            catch (IOException e){e.printStackTrace();}
        }


    }
}
