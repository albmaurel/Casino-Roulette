import java.security.Principal;
import java.util.TimerTask;

public class Tempo extends TimerTask {
    @Override
    public void run() {
        if(Servidor.getFase()==0)
        {
            Servidor.incCont();
            if(Servidor.getCont()==40)
            {
                Servidor.setFase(1);
                Servidor.rsCont();
            }
        }
        else if(Servidor.getFase()==1)
        {
            Servidor.incCont();
            if(Servidor.getCont()==5)
            {
                Servidor.setFase(2);
                Servidor.rsCont();
            }
        }
        else
        {
            Servidor.incCont();
            if(Servidor.getCont()==20)
            {
                Servidor.setFase(0);
                Servidor.rsCont();
            }
        }
    }

}
