import java.util.Random;
import java.util.Set;
import java.util.TimerTask;

public class GeneraGanador extends TimerTask {
    //Conjuntos usados para tener los grupos de colores.
    private static final Set<Integer> NUMEROS_ROJOS = Set.of(
            1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36
    );
    private static final Set<Integer> NUMEROS_NEGROS = Set.of(
            2, 4, 6, 8, 10, 11, 13, 15, 17, 20, 22, 24, 26, 28, 29, 31, 33, 35
    );

    //En el método run se saca el número ganador y se asigna al atributo ganador del servidor ruleta, se guarda además un código para distinguir el color.
    @Override
    public void run() {

        Random rand = new Random();
        int ganador;
        ganador = rand.nextInt(0, 37);
        if (NUMEROS_ROJOS.contains(ganador)) {
            ServidorRuleta.setGanador("R", ganador);
        } else if (NUMEROS_NEGROS.contains(ganador)) {
            ServidorRuleta.setGanador("N", ganador);
        } else {
            ServidorRuleta.setGanador("O", ganador);
        }
    }
}