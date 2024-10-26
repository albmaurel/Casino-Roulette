import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.util.Set;

public class Cliente_GUI {

    private JFrame frame;
    private JTextField textFieldSaldo, textFieldApuesta, textFieldNumeroGanador;
    private JTextArea textAreaApuestas;
    private JLabel labelTemporizador;

    private String apuestaActual = "";
    private int cantidadApuesta = 0;
    private int saldo = 10000;  // Saldo inicial
    private Socket socket;
    private BufferedWriter out;
    private BufferedReader in;

    // Conjuntos de números rojos y negros según la imagen
    private static final Set<Integer> NUMEROS_ROJOS = Set.of(
            3, 6, 9, 12, 15, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36
    );
    private static final Set<Integer> NUMEROS_NEGROS = Set.of(
            2, 4, 8, 10, 11, 13, 17, 20, 22, 24, 26, 28, 29, 31, 33, 35
    );

    public Cliente_GUI() {
        iniciarInterfaz();
        conectarAlServidor();
    }

    private void iniciarInterfaz() {
        frame = new JFrame("Ruleta - Cliente");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 600);
        frame.setLayout(new BorderLayout());

        // Panel para la ruleta (números)
        JPanel panelRuleta = new JPanel(new GridLayout(4, 10));
        for (int i = 0; i <= 36; i++) {
            JButton btnNumero = new JButton(String.valueOf(i));
            if (i == 0) {
                btnNumero.setBackground(Color.GREEN);  // Verde para el número 0
            } else if (NUMEROS_ROJOS.contains(i)) {
                btnNumero.setBackground(Color.RED);  // Rojo para números en el conjunto de rojos
            } else if (NUMEROS_NEGROS.contains(i)) {
                btnNumero.setBackground(Color.BLACK);  // Negro para números en el conjunto de negros
                btnNumero.setForeground(Color.WHITE);  // Texto en blanco para visibilidad
            }

            // Listener para cada botón de número
            btnNumero.addActionListener(e -> manejarApuesta("Número " + btnNumero.getText()));
            panelRuleta.add(btnNumero);
        }

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

        // Panel para saldo y apuestas
        JPanel panelSaldo = new JPanel(new GridLayout(3, 2));
        panelSaldo.add(new JLabel("Saldo:"));
        textFieldSaldo = new JTextField(String.valueOf(saldo));
        textFieldSaldo.setEditable(false);
        panelSaldo.add(textFieldSaldo);
        panelSaldo.add(new JLabel("Apuesta:"));
        textFieldApuesta = new JTextField();
        panelSaldo.add(textFieldApuesta);

        // Panel para mostrar apuestas y resultados
        JPanel panelResultados = new JPanel(new BorderLayout());
        textAreaApuestas = new JTextArea();
        textAreaApuestas.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textAreaApuestas);
        panelResultados.add(scrollPane, BorderLayout.CENTER);

        // Panel para mostrar el número ganador
        JPanel panelNumeroGanador = new JPanel();
        panelNumeroGanador.add(new JLabel("Número Ganador:"));
        textFieldNumeroGanador = new JTextField(10);
        textFieldNumeroGanador.setEditable(false);
        panelNumeroGanador.add(textFieldNumeroGanador);

        // Temporizador
        labelTemporizador = new JLabel("Tiempo Restante: ");

        // Añadir todos los paneles al frame principal
        frame.add(panelRuleta, BorderLayout.CENTER);
        frame.add(panelApuestas, BorderLayout.NORTH);
        frame.add(panelSaldo, BorderLayout.WEST);
        frame.add(panelResultados, BorderLayout.EAST);
        frame.add(panelNumeroGanador, BorderLayout.SOUTH);
        frame.add(labelTemporizador, BorderLayout.PAGE_END);

        frame.setVisible(true);
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
            textFieldSaldo.setText(String.valueOf(saldo));

            // Guardar y mostrar la apuesta realizada
            String mensajeApuesta =  tipoApuesta + " " + cantidad;
            textAreaApuestas.append(mensajeApuesta + "\n");

            // Enviar la apuesta al servidor si se desea
            enviarApuesta(tipoApuesta, cantidad);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Introduce un número válido en el campo de apuesta.");
        }
    }

    private void conectarAlServidor() {
        try {
            socket = new Socket("localhost", 55555);
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // Puedes iniciar un hilo para escuchar mensajes del servidor aquí
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void enviarApuesta(String tipoApuesta, int cantidad) {
        try {
            if (out != null) {
                out.write(tipoApuesta +" "+ cantidad + "\n");
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Cliente_GUI());
    }
}