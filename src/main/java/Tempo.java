import java.security.Principal;
import java.util.Random;
import java.util.Set;
import java.util.TimerTask;

public class Tempo extends TimerTask {
    @Override
    public void run() {
            Servidor.setInicio(System.currentTimeMillis());
    }
}
