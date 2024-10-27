import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.lang.reflect.Array;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Set;

public class Cliente_GUI {

    private JFrame frame;
    private JTextField textFieldSaldo, textFieldApuesta, textFieldNumeroGanador;
    private JTextArea textAreaApuestas;
    private JLabel labelTemporizador;
    private JDialog dialogGanador;

    private int saldo = 10000;  // Saldo inicial
    private Socket socket;
    private ObjectOutputStream out;
    private BufferedReader in;
    private ArrayList<String> apuestas = new ArrayList<>();

    // Conjuntos de números rojos y negros según la imagen
    private static final Set<Integer> NUMEROS_ROJOS = Set.of(
            1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36
    );
    private static final Set<Integer> NUMEROS_NEGROS = Set.of(
            2, 4, 6, 8, 10, 11, 13, 15, 17, 20, 22, 24, 26, 28, 29, 31, 33, 35
    );

    public Cliente_GUI() {
        iniciarInterfaz();
    }

    private void iniciarInterfaz() {
        frame = new JFrame("Ruleta - Cliente");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 600);  // Ajuste del tamaño de la ventana
        frame.setLayout(new BorderLayout());

        // Panel principal de la ruleta en disposición horizontal
        JPanel panelRuleta = new JPanel(new BorderLayout());

        // Panel para el 0 a la izquierda ocupando la altura de tres filas
        JButton btnCero = new JButton("0");
        btnCero.setBackground(Color.GREEN);
        btnCero.setPreferredSize(new Dimension(70, 300)); // Tamaño personalizado para destacarlo
        btnCero.addActionListener(e -> manejarApuesta("Numero 0"));

        // Panel para los números restantes, organizado en tres filas y 12 columnas
        JPanel panelNumeros = new JPanel(new GridLayout(3, 12, 5, 5)); // 3 filas y 12 columnas

        // Lista de números organizada en filas horizontales como en una ruleta
        int[][] numerosRuleta = {
                {3, 6, 9, 12, 15, 18, 21, 24, 27, 30, 33, 36},
                {2, 5, 8, 11, 14, 17, 20, 23, 26, 29, 32, 35},
                {1, 4, 7, 10, 13, 16, 19, 22, 25, 28, 31, 34}
        };

        // Crear botones para cada número en el orden deseado
        for (int fila = 0; fila < 3; fila++) {
            for (int col = 0; col < 12; col++) {
                int numero = numerosRuleta[fila][col];
                JButton btnNumero = new JButton(String.valueOf(numero));

                // Colores para rojo y negro
                if (NUMEROS_ROJOS.contains(numero)) {
                    btnNumero.setBackground(Color.RED);
                    btnNumero.setForeground(Color.WHITE);
                } else if (NUMEROS_NEGROS.contains(numero)) {
                    btnNumero.setBackground(Color.BLACK);
                    btnNumero.setForeground(Color.WHITE);
                }

                final int numeroApuesta = numero;
                btnNumero.addActionListener(e -> manejarApuesta("Numero " + numeroApuesta));
                panelNumeros.add(btnNumero);
            }
        }

        // Añadir el panelCero y panelNumeros al panel principal de la ruleta
        panelRuleta.add(btnCero, BorderLayout.WEST);
        panelRuleta.add(panelNumeros, BorderLayout.CENTER);

        // Panel para apuestas adicionales (color, par/impar)
        JPanel panelApuestas = new JPanel(new GridLayout(2, 2));
        JButton btnRojo = new JButton("Rojo");
        btnRojo.setBackground(Color.RED);
        btnRojo.addActionListener(e -> manejarApuesta("Rojo"));

        JButton btnNegro = new JButton("Negro");
        btnNegro.setBackground(Color.BLACK);
        btnNegro.setForeground(Color.WHITE);
        btnNegro.addActionListener(e -> manejarApuesta("Negro"));

        JButton btnPar = new JButton("Par");
        btnPar.addActionListener(e -> manejarApuesta("Par"));

        JButton btnImpar = new JButton("Impar");
        btnImpar.addActionListener(e -> manejarApuesta("Impar"));

        panelApuestas.add(btnRojo);
        panelApuestas.add(btnNegro);
        panelApuestas.add(btnPar);
        panelApuestas.add(btnImpar);

        JPanel panelSaldo = new JPanel(new GridLayout(2, 1));

        // Crear un panel para colocar saldo y apuesta
        JLabel labelSaldo = new JLabel("Saldo");
        labelSaldo.setHorizontalAlignment(SwingConstants.CENTER);
        panelSaldo.add(labelSaldo);
        textFieldSaldo = new JTextField(String.valueOf(saldo)+" $");
        textFieldSaldo.setEditable(false);
        textFieldSaldo.setHorizontalAlignment(SwingConstants.CENTER);
        panelSaldo.add(textFieldSaldo);

        JLabel labelApuesta = new JLabel("Apuesta");
        labelApuesta.setHorizontalAlignment(SwingConstants.CENTER);
        panelSaldo.add(labelApuesta);
        textFieldApuesta = new JTextField();
        textFieldApuesta.setHorizontalAlignment(SwingConstants.CENTER);
        panelSaldo.add(textFieldApuesta);


        // Panel para mostrar apuestas y resultados
        JPanel panelResultados = new JPanel(new BorderLayout());
        textAreaApuestas = new JTextArea(" Apuestas realizadas:\n");
        textAreaApuestas.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textAreaApuestas);
        scrollPane.setPreferredSize(new Dimension(150, 300));
        panelResultados.add(scrollPane, BorderLayout.CENTER);

        // Temporizador
        labelTemporizador = new JLabel("Tiempo Restante: ");
        labelTemporizador.setPreferredSize(new Dimension(200, 40));
        labelTemporizador.setHorizontalAlignment(SwingConstants.CENTER);
        labelTemporizador.setVerticalAlignment(SwingConstants.CENTER);

        // Añadir todos los paneles al frame principal
        frame.add(panelRuleta, BorderLayout.CENTER);
        frame.add(panelApuestas, BorderLayout.NORTH);
        frame.add(panelSaldo, BorderLayout.WEST);
        frame.add(panelResultados, BorderLayout.EAST);
        frame.add(labelTemporizador, BorderLayout.PAGE_END);

        // Aumentar el tamaño de los cuadros de texto
        textFieldSaldo.setPreferredSize(new Dimension(100, 150));  // Ajuste del tamaño
        textFieldApuesta.setPreferredSize(new Dimension(100, 150)); // Ajuste del tamaño

        frame.setVisible(true);

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                // Enviar mensaje al servidor
                try {
                    if (out != null) {
                        out.writeObject("X"); // Enviar el mensaje de cierre
                        out.flush();
                    }
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                } finally {
                    frame.dispose(); // Cerrar la ventana
                    System.exit(0); // Terminar el programa
                }
            }
        });
    }

    private void manejarApuesta(String tipoApuesta) {
        try {
            int cantidad = Integer.parseInt(textFieldApuesta.getText());
            if (cantidad <= 0 || cantidad > saldo) {
                JOptionPane.showMessageDialog(frame, "Saldo insuficiente o apuesta inválida.");
                return;
            }

            // Descontar la apuesta del saldo
            saldo -= cantidad;
            textFieldSaldo.setText(String.valueOf(saldo)+" $");

            // Guardar y mostrar la apuesta realizada con el nombre completo
            String mensajeApuesta = tipoApuesta + ": " + cantidad+ " $"+"\n";
            textAreaApuestas.append(mensajeApuesta);

            if (tipoApuesta.startsWith("Numero")) {
                String numero = tipoApuesta.split(" ")[1];
                mensajeApuesta = "N" + numero + " " + cantidad+"\n";
            } else {
                String inicial = tipoApuesta.substring(0, 1).toUpperCase();
                mensajeApuesta = inicial + " " + cantidad+"\n";
            }
            apuestas.add(mensajeApuesta);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Introduce un número válido en el campo de apuesta.");
        }
    }

    private void vaciarApuestas(){
        this.apuestas.clear();
    }

    // Este método maneja la conexión y la recepción de datos
    public void iniciarConexion(String host, int puerto) {
        try (Socket socket = new Socket(host, puerto);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            while (true) {
                String leido = reader.readLine();
                if (leido.equals("0")) {
                    labelTemporizador.setText("Tiempo Restante: " + leido + " segundos");
                    out.writeObject(apuestas); // Accede a apuestas desde la instancia
                    out.flush();
                    this.vaciarApuestas();

                    if (dialogGanador != null && dialogGanador.isShowing()) {
                        dialogGanador.dispose();
                        dialogGanador = null;
                    }
                } else if (leido.equals("20")) {
                    labelTemporizador.setText("Tiempo Restante: " + leido + " segundos");
                    String numeroganador = reader.readLine();
                    dialogGanador = mostrarPopup("El número ganador es: " + numeroganador, "Número Ganador");

                    String ganancias = reader.readLine();
                    saldo += Integer.parseInt(ganancias); // Actualiza el saldo
                } else {
                    labelTemporizador.setText("Tiempo Restante: " + leido + " segundos");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JDialog mostrarPopup(String mensaje, String titulo) {
        JOptionPane optionPane = new JOptionPane(mensaje, JOptionPane.INFORMATION_MESSAGE);
        JDialog dialog = optionPane.createDialog(titulo);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);
        return dialog;
    }

    public static void main(String[] args) {
        Cliente_GUI clienteGUI = new Cliente_GUI();
        clienteGUI.iniciarConexion("localhost", 55555); // Inicia la conexión
    }
}
