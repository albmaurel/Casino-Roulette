import java.util.Random;
import java.util.Set;
import java.util.TimerTask;

public class GeneraGanador extends TimerTask {
    private static final Set<Integer> NUMEROS_ROJOS = Set.of(
            1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36
    );
    private static final Set<Integer> NUMEROS_NEGROS = Set.of(
            2, 4, 6, 8, 10, 11, 13, 15, 17, 20, 22, 24, 26, 28, 29, 31, 33, 35
    );
    @Override
    public void run() {
        Servidor.setInicio(System.currentTimeMillis());
        Random rand = new Random();
        int ganador;
        ganador = rand.nextInt(0, 37);
        if (NUMEROS_ROJOS.contains(ganador)) {
                Servidor.setGanador("R", ganador);
        } else if (NUMEROS_NEGROS.contains(ganador)) {
                Servidor.setGanador("N", ganador);
        } else {
                Servidor.setGanador("0", ganador);
        }
    }
}
