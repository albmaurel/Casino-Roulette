import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Set;

public class Cliente_GUI {

    // Conjuntos de números rojos y negros según la imagen
    private static final Set<Integer> NUMEROS_ROJOS = Set.of(
            1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36
    );
    private static final Set<Integer> NUMEROS_NEGROS = Set.of(
            2, 4, 6, 8, 10, 11, 13, 15, 17, 20, 22, 24, 26, 28, 29, 31, 33, 35
    );
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    int screenWidth = screenSize.width;
    int screenHeight = screenSize.height;
    private JFrame frame;
    private JTextField textFieldSaldo, textFieldApuesta, textFieldNumeroGanador;
    private JTextArea textAreaApuestas;
    private JLabel labelTemporizador;
    private JDialog dialogGanador;
    private JDialog leaderboard;
    private Timer contadorTimer;
    private long tiempoFinal;
    private Socket juegoSocket;
    private ObjectOutputStream juegoOut;
    private BufferedReader juegoIn;
    private boolean finalizado = false;
    private String usuario;
    private int saldo = 10000;  // Saldo inicial
    private ArrayList<String> apuestas = new ArrayList<>();

    public Cliente_GUI() {
        mostrarLogin();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Cliente_GUI::new);

    }

    private void mostrarLogin() {
        JFrame loginFrame = new JFrame("Login");
        loginFrame.setSize(300, 200);
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        loginFrame.setLayout(new BorderLayout(10, 10));

        JPanel panelLogin = new JPanel();
        panelLogin.setLayout(new GridLayout(4, 2, 10, 10));

        panelLogin.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel labelUsuario = new JLabel("Usuario:");
        JTextField textFieldUsuario = new JTextField();
        JLabel labelContrasena = new JLabel("Contraseña:");
        JPasswordField passwordField = new JPasswordField();
        JButton btnIniciarSesion = new JButton("Iniciar Sesión");
        JButton btnCrearUsuario = new JButton("Crear Usuario");

        panelLogin.add(labelUsuario);
        panelLogin.add(textFieldUsuario);
        panelLogin.add(labelContrasena);
        panelLogin.add(passwordField);

        panelLogin.add(new JLabel());
        panelLogin.add(new JLabel());

        panelLogin.add(btnIniciarSesion);
        panelLogin.add(btnCrearUsuario);

        loginFrame.add(panelLogin, BorderLayout.CENTER);

        loginFrame.setVisible(true);

        btnIniciarSesion.addActionListener(e -> {
            String usuario = textFieldUsuario.getText();
            String contrasena = new String(passwordField.getPassword());

            if (validarLogin(usuario, contrasena)) {
                loginFrame.dispose();
                iniciarInterfaz();
                if (finalizado) {
                    startServerListenerThread();
                }
            } else {
                JOptionPane.showMessageDialog(loginFrame, "Credenciales incorrectas o Usuario conectado, intente nuevamente.");
            }
        });
        btnCrearUsuario.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                usuario = textFieldUsuario.getText();
                String contrasena = new String(passwordField.getPassword());

                // Llamamos al método de crear usuario
                if (crearUsuario(usuario, contrasena)) {
                    System.out.println("Usuario creado exitosamente");
                    loginFrame.dispose();
                    iniciarInterfaz();
                    if (finalizado) {
                        startServerListenerThread();
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "No se pudo crear el usuario", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private boolean validarLogin(String usuario, String contrasena) {
        try {
            juegoSocket = new Socket("localhost", 55555);
            juegoOut = new ObjectOutputStream(juegoSocket.getOutputStream());
            juegoIn = new BufferedReader(new InputStreamReader(juegoSocket.getInputStream()));

            juegoOut.writeObject("L " + usuario + " " + contrasena);
            juegoOut.flush();
            juegoOut.reset();

            String respuesta = juegoIn.readLine();

            if (respuesta != null && respuesta.startsWith("S")) {
                saldo = Integer.parseInt(respuesta.substring(1).trim());
                return true;
            }

            // Si la respuesta es "I", el login es incorrecto
            return false;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean crearUsuario(String usuario, String contrasena) {
        try {
            juegoSocket = new Socket("localhost", 55555);
            juegoOut = new ObjectOutputStream(juegoSocket.getOutputStream());
            juegoIn = new BufferedReader(new InputStreamReader(juegoSocket.getInputStream()));

            juegoOut.writeObject("C " + usuario + " " + contrasena);
            juegoOut.flush();
            juegoOut.reset();

            String respuesta = juegoIn.readLine();

            if (respuesta != null && respuesta.startsWith("S")) {
                saldo = Integer.parseInt(respuesta.substring(1).trim());
                System.out.println(saldo);
                return true;
            }

            return false;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void iniciarInterfaz() {
        frame = new JFrame("Ruleta - " + usuario);
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
        textFieldSaldo = new JTextField(String.valueOf(saldo) + " $");
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
        textAreaApuestas = new JTextArea("Apuestas realizadas:\n");
        textAreaApuestas.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textAreaApuestas);
        scrollPane.setPreferredSize(new Dimension(150, 300));
        panelResultados.add(scrollPane, BorderLayout.CENTER);

        // Temporizador
        labelTemporizador = new JLabel(" ");
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
        finalizado = true;
        /*frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                // Enviar mensaje al servidor
                try {
                    if (juegoOut != null) {
                        juegoOut.writeObject("X");
                        juegoOut.flush();
                        juegoOut.reset();
                    }
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                } finally {
                    frame.dispose();
                    System.exit(0);
                }
            }
        });*/
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
            textFieldSaldo.setText(String.valueOf(saldo) + " $");

            // Guardar y mostrar la apuesta realizada con el nombre completo
            String mensajeApuesta = tipoApuesta + ": " + cantidad + " $" + "\n";
            textAreaApuestas.append(mensajeApuesta);

            if (tipoApuesta.startsWith("Numero")) {
                String numero = tipoApuesta.split(" ")[1];
                mensajeApuesta = "N" + numero + " " + cantidad;
            } else {
                String inicial = tipoApuesta.substring(0, 1).toUpperCase();
                mensajeApuesta = inicial + " " + cantidad;
            }
            System.out.println(mensajeApuesta);
            apuestas.add(mensajeApuesta);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Introduce un número válido en el campo de apuesta.");
        }
    }

    private void vaciarApuestas() {
        this.apuestas.clear();
        for (String apuesta : apuestas) {
            System.out.println(apuesta);
        }
    }

    private void vaciarPanelApuestas() {
        SwingUtilities.invokeLater(() -> textAreaApuestas.setText("Apuestas realizadas:\n"));
    }

    private void startServerListenerThread() {
        Thread serverListenerThread = new Thread(() -> {
            try {
                while (true) {
                    String leido = juegoIn.readLine();
                    if (leido != null) {
                        processServerMessage(leido);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        serverListenerThread.start();
    }

    private void processServerMessage(String leido) {
        if (leido != null) {
            long tiempoServidor = 0;
            if (leido.startsWith("T")) {
                vaciarApuestas();
                vaciarPanelApuestas();
                tiempoServidor = Long.parseLong(leido.substring(1));
                tiempoFinal = tiempoServidor;
                System.out.println(tiempoFinal);
                funcionaContador(juegoOut, juegoIn);
            }
            if (leido.startsWith("N")) {
                String numeroganador = (leido.substring(1));
                SwingUtilities.invokeLater(() -> {
                    dialogGanador = mostrarPopup("El número ganador es: " + numeroganador, "Número Ganador");
                    frame.setEnabled(false);
                });

            } //G5000,usuario ganancias,usuario ganancias.
            if (leido.startsWith("G")) {
               //String ganancias = leido.substring(1);
                String ganancias = leido.substring(1, leido.indexOf(","));
                saldo += Integer.parseInt(ganancias);
                System.out.println("Saldo actualizado: " + saldo);
                SwingUtilities.invokeLater(() -> textFieldSaldo.setText(String.valueOf(saldo) + " $"));
                SwingUtilities.invokeLater(() -> leaderboard = mostrarLeaderboard(leido));
            }
        }
    }

    private void funcionaContador(ObjectOutputStream out, BufferedReader reader) {

        if (contadorTimer != null && contadorTimer.isRunning()) {
            contadorTimer.stop();
        }

        // Crear un Timer que se dispare cada segundo (1000 ms)
        contadorTimer = new Timer(1000, new ActionListener() {
            private boolean accion04Ejecutada = false;

            @Override
            public void actionPerformed(ActionEvent e) {

                long tiempoActual = System.currentTimeMillis();
                long tiempoRestante = (tiempoFinal - tiempoActual) / 1000;

                if (tiempoRestante > 0) {
                    labelTemporizador.setText("Tiempo Restante: " + tiempoRestante + " segundos");

                    if (tiempoRestante == 15 && !accion04Ejecutada) {
                        accion04Ejecutada = true;
                        envioapuestas(apuestas);
                    }
                } else {
                    labelTemporizador.setText("Tiempo Restante: 0 segundos");
                    contadorTimer.stop();

                    if (dialogGanador != null && dialogGanador.isShowing()) {
                        dialogGanador.dispose();
                        dialogGanador = null;
                    }
                    if (leaderboard != null && leaderboard.isShowing()) {
                        leaderboard.dispose();
                        leaderboard = null;
                    }
                    vaciarPanelApuestas();
                    vaciarApuestas();
                    frame.setEnabled(true);
                    frame.requestFocus();
                }
            }
        });

        contadorTimer.start();
    }

    private void envioapuestas(ArrayList<String> apuestas) {
        Thread serverListenerThread = new Thread(() -> {
            try {
                juegoOut.writeObject(apuestas);
                juegoOut.flush();
                juegoOut.reset();
                vaciarApuestas();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        serverListenerThread.start();
    }

    private JDialog mostrarPopup(String mensaje, String titulo) {
        JOptionPane optionPane = new JOptionPane(mensaje, JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);
        JDialog dialog = optionPane.createDialog(titulo);

        dialog.setModal(false);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
            }
        });
        int popupWidth = dialog.getWidth();
        int popupHeight = dialog.getHeight();
        int popupX = (screenWidth / 2) - popupWidth - 10;
        int popupY = (screenHeight / 2) - (popupHeight / 2);
        dialog.setLocation(popupX, popupY);
        dialog.setVisible(true);

        return dialog;
    }

    private JDialog mostrarLeaderboard(String leido) {
        //leido = "G5000,usuario1 15000,usuario2 10000,usuario3 5000,usuario4 2200,usuario5 1000,usuario6 500";
        // Procesar la parte del leaderboard
        String leaderboardData = leido.substring(leido.indexOf(",") + 1); // Parte después de "G5000,"
        String[] usuarios = leaderboardData.split(","); // Dividir por usuarios

        // Crear una lista para la tabla
        java.util.List<String[]> leaderboardList = new ArrayList<>();
        int rank = 1; // Inicia el ranking
        for (String usuario : usuarios) {
            String[] parts = usuario.split(" ");
            if (parts.length == 2) {
                leaderboardList.add(new String[]{rank + "º", parts[0], parts[1]});
                rank++;
            }
        }
        // Crear y mostrar el leaderboard en una ventana personalizada

        // Columnas de la tabla
        String[] columnNames = {"Orden", "Usuario", "Ganancias ($)"};
        // Datos de la tabla
        String[][] data = leaderboardList.toArray(new String[0][]);

        // Crear la tabla
        JTable table = new JTable(data, columnNames);
        table.setEnabled(false); // Deshabilitar edición
        table.setFont(new Font("Arial", Font.PLAIN, 14)); // Fuente
        table.setRowHeight(30); // Altura de fila

        // Centrar contenido de la tabla
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                // Aplicar color según la posición
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) { // No sobrescribir selección
                    if (row == 0) c.setBackground(new Color(255, 215, 0)); // Oro
                    else if (row == 1) c.setBackground(new Color(192, 192, 192)); // Plata
                    else if (row == 2) c.setBackground(new Color(184, 115, 51)); // Bronce
                    else c.setBackground(Color.WHITE); // Resto
                }
                setHorizontalAlignment(SwingConstants.CENTER); // Centrar contenido
                return c;
            }
        };

        // Asignar renderizador a todas las columnas
        for (int column = 0; column < table.getColumnCount(); column++) {
            table.getColumnModel().getColumn(column).setCellRenderer(centerRenderer);
        }

        // Ajustar el ancho de las columnas
        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(50); // Orden
        columnModel.getColumn(1).setPreferredWidth(100); // Usuario
        columnModel.getColumn(2).setPreferredWidth(100); // Ganancias

        // Crear un JScrollPane para manejar contenido grande
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Márgenes

        // Crear un JDialog en lugar de JOptionPane
        JDialog dialog = new JDialog((Frame) null, "Leaderboard", true);
        dialog.setModal(false);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
            }
        });
        dialog.setLayout(new BorderLayout());
        dialog.add(scrollPane, BorderLayout.CENTER);

        // Ajustar tamaño dinámico según el contenido
        int dialogHeight = Math.min(400, leaderboardList.size() * 40 + 50); // Máximo 400px
        dialog.setSize(300, dialogHeight);
        dialog.setLocationRelativeTo(null); // Centrar en pantalla
        dialog.setVisible(true); // Mostrar la ventana
        int leaderboardWidth = dialog.getWidth();
        int leaderboardHeight = dialog.getHeight();
        int leaderboardX = (screenWidth / 2) + 10; // Centrar a la derecha
        int leaderboardY = (screenHeight / 2) - (leaderboardHeight / 2); // Centrar verticalmente
        dialog.setLocation(leaderboardX, leaderboardY);

        return dialog;
    }
}
