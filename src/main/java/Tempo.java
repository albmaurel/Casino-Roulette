import java.util.TimerTask;

public class Tempo extends TimerTask {
    @Override
    public void run() {
        ServidorRuleta.setFin(System.currentTimeMillis()+55000);
        ServidorRuleta.resetRank();
    }
}

