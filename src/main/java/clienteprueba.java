import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class clienteprueba {
    public static void main(String[] args) {

        try (Socket s = new Socket("localhost", 55555);
             BufferedWriter writer=new BufferedWriter(new OutputStreamWriter(s.getOutputStream(),"UTF-8"));
             BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream(), "UTF-8"));
        )
        {
            int aux=0;
            while(true) {
                String leido=reader.readLine();

                System.out.print(leido+"\n");

            }
            } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}





