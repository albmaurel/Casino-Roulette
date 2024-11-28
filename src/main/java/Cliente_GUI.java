
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
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

    // Conjuntos de n�meros rojos y negros seg�n la imagen
    private static final Set<Integer> NUMEROS_ROJOS = Set.of(
            1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36
    );
    private static final Set<Integer> NUMEROS_NEGROS = Set.of(
            2, 4, 6, 8, 10, 11, 13, 15, 17, 20, 22, 24, 26, 28, 29, 31, 33, 35
    );

    //PARTE DE INTERFAZ GRAFICA DE LA RULETA
    private static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    private static int screenWidth = screenSize.width;
    private static int screenHeight = screenSize.height;
    private JFrame frame;
    private JTextField textFieldSaldo, textFieldApuesta, textFieldNumeroGanador;
    private JTextArea textAreaApuestas;

    //PARTE FUNCIONAMIENTO INTERNO INICIAL
    private Socket juegoSocket=null;
    private ObjectOutputStream juegoOut=null;
    private BufferedReader juegoIn=null;
    private static String usuario;
    private boolean finalizadologin = false;
    private static String[] ruletasDisponibles;
    private static String nombreSala;

    //PARTE FUNCIONAMIENTO INTERNO FINAL
    private boolean finalizado = false;
    private JLabel labelTemporizador;
    private JDialog dialogGanador;
    private JDialog dialogEspera;
    private static JTable leaderboard;
    private Timer contadorTimer;
    private long tiempoFinal;
    private ArrayList<String> apuestas = new ArrayList<>();
    private static int saldo=0;
    private static int PUERTO;
    private Socket ruletaSocket=null;
    private ObjectOutputStream ruletaOut=null;
    private BufferedReader ruletaIn=null;
    private static JScrollPane leaderboardScrollPane;
    private static boolean  unavez=false;

    //PARTE DEL GESTOR DE RULETAS
    private DefaultComboBoxModel<String> comboBoxModel;
    private JComboBox<String> comboBoxSalas;
    private JButton botonCrearSala;
    private JButton botonUnirse;
    private JTextField textoNombreSala;

    public Cliente_GUI() {
        mostrarLogin();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Cliente_GUI::new);

    }

    //CODIGO DE LA GUI DE LOGIN, Y SUS PROCEDIMIENTOS
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
        JButton btnIniciarSesion = new JButton("Iniciar Sesion");
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
        loginFrame.setLocationRelativeTo(null);
        loginFrame.setVisible(true);

        btnIniciarSesion.addActionListener(e -> {
            usuario = textFieldUsuario.getText();
            String contrasena = new String(passwordField.getPassword());

            if (validarLogin(usuario, contrasena)) {
                loginFrame.dispose();
                mostrarRuletas();
            } else {
                JOptionPane.showMessageDialog(loginFrame, "Credenciales incorrectas o Usuario conectado, intente nuevamente.");
            }
        });
        btnCrearUsuario.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                usuario = textFieldUsuario.getText();
                String contrasena = new String(passwordField.getPassword());

                if (crearUsuario(usuario, contrasena)) {
                    System.out.println("Usuario creado exitosamente");
                    loginFrame.dispose();
                    mostrarRuletas();
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
                saldo = Integer.parseInt(respuesta.substring(1, respuesta.indexOf(" ")).trim());

                ruletasDisponibles = respuesta.substring(respuesta.indexOf(" ") + 1).split(" ");
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
                saldo = Integer.parseInt(respuesta.substring(1, respuesta.indexOf(" ")).trim());

                ruletasDisponibles = respuesta.substring(respuesta.indexOf(" ") + 1).split(" ");
                return true;
            }

            return false;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    //CODIGO GUI DE LA SELECCIÓN DE RULETAS Y FUNCIONALIDAD

    private void mostrarRuletas() {
        JFrame GestorRuletasFrame = new JFrame("Gestor Ruletas - "+usuario);
        GestorRuletasFrame.setSize(450, 200);
        GestorRuletasFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        GestorRuletasFrame.setLocationRelativeTo(null);
        GestorRuletasFrame.setLayout(new GridBagLayout());

        comboBoxModel = new DefaultComboBoxModel<>();
        comboBoxSalas = new JComboBox<>(comboBoxModel);

        botonCrearSala = new JButton("Crear Sala");
        botonUnirse = new JButton("Unirte");
        textoNombreSala = new JTextField(15);

        JLabel etiquetaSalas = new JLabel("Salas disponibles:");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        GestorRuletasFrame.add(new JLabel("Nombre de la sala:"), gbc);

        gbc.gridx = 1;
        GestorRuletasFrame.add(textoNombreSala, gbc);

        gbc.gridx = 2;
        GestorRuletasFrame.add(botonCrearSala, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        GestorRuletasFrame.add(etiquetaSalas, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        GestorRuletasFrame.add(comboBoxSalas, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        GestorRuletasFrame.add(botonUnirse, gbc);

        if (ruletasDisponibles != null && ruletasDisponibles.length > 0) {

            for (String ruleta : ruletasDisponibles) {
                comboBoxModel.addElement(ruleta);
            }
        }
        botonCrearSala.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                nombreSala = textoNombreSala.getText().trim();
                if(crearSala(nombreSala)) {
                    GestorRuletasFrame.dispose();
                    iniciarInterfaz();
                    if (finalizado) {
                        startServerListenerThread();
                    }
                }
                else {
                    JOptionPane.showMessageDialog(null, "No se pudo crear la sala", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        botonUnirse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                nombreSala = (String) comboBoxSalas.getSelectedItem();
                if(unirseSala(nombreSala)) {
                    GestorRuletasFrame.dispose();
                    iniciarInterfaz();
                    if (finalizado) {
                        startServerListenerThread();                 }
                }
                else {
                    JOptionPane.showMessageDialog(GestorRuletasFrame, "No ha sido posible unirse a la sala, intente nuevamente.");
                }
            }
        });

        GestorRuletasFrame.setVisible(true);
    }


    private boolean crearSala(String nombreSala) {
        try {
            juegoOut.writeObject("C " + nombreSala);
            juegoOut.flush();
            juegoOut.reset();

            String respuesta = juegoIn.readLine();

            if (respuesta != null && respuesta.startsWith("O")) {
                PUERTO = Integer.parseInt(respuesta.substring(1).trim());
                return true;
            }

            return false;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        finally {
            try {
                if (juegoSocket != null && !juegoSocket.isClosed()) {
                    juegoSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean unirseSala(String nombreSala) {
        try {
            juegoOut.writeObject("U " + nombreSala);
            juegoOut.flush();
            juegoOut.reset();

            String respuesta = juegoIn.readLine();

            if (respuesta != null && respuesta.startsWith("O")) {
                PUERTO = Integer.parseInt(respuesta.substring(1).trim());
                return true;
            }

            return false;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        finally {
            try {
                if (juegoSocket != null && !juegoSocket.isClosed()) {
                    juegoSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //CODIGO INTERFAZ PRINCIPAL Y SU FUNCIONAMIENTO

    private void iniciarInterfaz() {
        frame = new JFrame("Ruleta: "+ nombreSala+" ---"+" Usuario: " +usuario);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1300, 600);  // Ajuste del tama�o de la ventana
        frame.setLayout(new BorderLayout());

        // Panel principal de la ruleta en disposici�n horizontal
        JPanel panelRuleta = new JPanel(new BorderLayout());

        // Panel para el 0 a la izquierda ocupando la altura de tres filas
        JButton btnCero = new JButton("0");
        btnCero.setBackground(Color.GREEN);
        btnCero.setPreferredSize(new Dimension(70, 300)); // Tama�o personalizado para destacarlo
        btnCero.addActionListener(e -> manejarApuesta("Numero 0"));

        // Panel para los n�meros restantes, organizado en tres filas y 12 columnas
        JPanel panelNumeros = new JPanel(new GridLayout(3, 12, 5, 5)); // 3 filas y 12 columnas

        // Lista de n�meros organizada en filas horizontales como en una ruleta
        int[][] numerosRuleta = {
                {3, 6, 9, 12, 15, 18, 21, 24, 27, 30, 33, 36},
                {2, 5, 8, 11, 14, 17, 20, 23, 26, 29, 32, 35},
                {1, 4, 7, 10, 13, 16, 19, 22, 25, 28, 31, 34}
        };

        // Crear botones para cada n�mero en el orden deseado
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

        // A�adir el panelCero y panelNumeros al panel principal de la ruleta
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

        String[] columnNames = {"Orden", "Usuario", "Ganancias "};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);

        leaderboard = new JTable(model) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        leaderboard.setFont(new Font("Arial", Font.PLAIN, 11));
        leaderboard.setRowHeight(30);
        leaderboard.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        leaderboard.getTableHeader().setBackground(Color.DARK_GRAY);
        leaderboard.getTableHeader().setForeground(Color.WHITE);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    if (row == 0) c.setBackground(new Color(255, 215, 0)); // Oro
                    else if (row == 1) c.setBackground(new Color(192, 192, 192)); // Plata
                    else if (row == 2) c.setBackground(new Color(184, 115, 51)); // Bronce
                    else c.setBackground(Color.WHITE);
                }
                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        };
        for (int column = 0; column < leaderboard.getColumnCount(); column++) {
            leaderboard.getColumnModel().getColumn(column).setCellRenderer(centerRenderer);
        }

        leaderboardScrollPane = new JScrollPane(leaderboard);
        leaderboardScrollPane.setPreferredSize(new Dimension(225, 300));

        // Agrega el JScrollPane a tu panel principal
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(leaderboardScrollPane, BorderLayout.CENTER);
        panelResultados.add(leaderboardScrollPane, BorderLayout.EAST);

        // Temporizador
        labelTemporizador = new JLabel(" ");
        labelTemporizador.setPreferredSize(new Dimension(200, 40));
        labelTemporizador.setHorizontalAlignment(SwingConstants.CENTER);
        labelTemporizador.setVerticalAlignment(SwingConstants.CENTER);

        // A�adir todos los paneles al frame principal
        frame.add(panelRuleta, BorderLayout.CENTER);
        frame.add(panelApuestas, BorderLayout.NORTH);
        frame.add(panelSaldo, BorderLayout.WEST);
        frame.add(panelResultados, BorderLayout.EAST);
        frame.add(labelTemporizador, BorderLayout.PAGE_END);

        // Aumentar el tama�o de los cuadros de texto
        textFieldSaldo.setPreferredSize(new Dimension(100, 150));  // Ajuste del tama�o
        textFieldApuesta.setPreferredSize(new Dimension(100, 150)); // Ajuste del tama�o

        frame.setLocationRelativeTo(null);

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
                JOptionPane.showMessageDialog(frame, "Saldo insuficiente o apuesta invalida.");
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
            JOptionPane.showMessageDialog(frame, "Introduce un numero valido en el campo de apuesta.");
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
                System.out.println(PUERTO);
                ruletaSocket = new Socket("localhost", PUERTO);
                ruletaOut = new ObjectOutputStream(ruletaSocket.getOutputStream());
                ruletaIn = new BufferedReader(new InputStreamReader(ruletaSocket.getInputStream()));
                while (true) {
                    String leido = ruletaIn.readLine();
                    if (leido != null) {
                        processServerMessage(leido);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                try {
                    if (ruletaSocket != null && !ruletaSocket.isClosed()) {
                        ruletaSocket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        serverListenerThread.start();
    }

    private void processServerMessage(String leido)  {
        if (leido != null) {
            long tiempoServidor = 0;
            if (leido.startsWith("T")) {
                vaciarApuestas();
                vaciarPanelApuestas();
                tiempoServidor = Long.parseLong(leido.substring(1));
                tiempoFinal = tiempoServidor;
                System.out.println(tiempoFinal);
                enviousr(usuario);
                funcionaContador(ruletaOut, ruletaIn);
            }
            if (leido.startsWith("N")) {
                String numeroganador = (leido.substring(1));
                SwingUtilities.invokeLater(() -> {
                    dialogGanador = mostrarPopup("El numero ganador es: " + numeroganador, "Numero Ganador");
                    frame.setEnabled(false);
                });
                SwingUtilities.invokeLater(() -> {dialogEspera = mostrarPopup_v2("No está permitido apostar en 15 segundos...","Información importante");});

            }
            if (leido.startsWith("G")) {
                //String ganancias = leido.substring(1);
                String ganancias = leido.substring(1, leido.indexOf(","));
                saldo += Integer.parseInt(ganancias);
                System.out.println("Saldo actualizado: " + saldo);

                SwingUtilities.invokeLater(() -> textFieldSaldo.setText(String.valueOf(saldo) + " $"));
                SwingUtilities.invokeLater(() ->  mostrarLeaderboard(leido,leaderboard));
                leaderboardScrollPane.revalidate();
                leaderboardScrollPane.repaint();
            }
        }
    }

    private void vaciarTabla(JTable table) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0); // Elimina todas las filas del modelo
    }

    private void funcionaContador(ObjectOutputStream out, BufferedReader reader) {

        if (contadorTimer != null && contadorTimer.isRunning()) {
            contadorTimer.stop();
        }

        final long TIEMPO_FIJO = 55 * 1000; // 55 segundos para ciclos posteriores
        boolean[] esPrimeraIteracion = {true}; // Bandera para la primera vez
        contadorTimer = new Timer(1000, new ActionListener() {
            private boolean accion04Ejecutada = false;

            @Override
            public void actionPerformed(ActionEvent e) {
                long tiempoActual = System.currentTimeMillis();
                long tiempoRestante;

                if (esPrimeraIteracion[0]) {
                    tiempoRestante = (tiempoFinal - tiempoActual) / 1000;
                    if (tiempoRestante>15){
                        unavez=true;
                    }
                } else {
                    tiempoRestante = (tiempoFinal - tiempoActual) / 1000;
                }

                if (tiempoRestante > 0) {
                    labelTemporizador.setText("Tiempo Restante: " + tiempoRestante + " segundos");

                    if (tiempoRestante == 15 && !accion04Ejecutada) {
                        accion04Ejecutada = true;
                        envioapuestas(apuestas);
                    }
                    if(unavez==false && tiempoRestante < 15) {
                        SwingUtilities.invokeLater(() -> {dialogEspera = mostrarPopup_v2("No está permitido apostar en 15 segundos...","Información importante");});
                        frame.setEnabled(false);
                        unavez=true;
                    }
                } else {
                    labelTemporizador.setText("Tiempo Restante: 0 segundos");
                    contadorTimer.stop();

                    if (dialogGanador != null && dialogGanador.isShowing()) {
                        dialogGanador.dispose();
                        dialogGanador = null;
                    }
                    if (dialogEspera != null && dialogEspera.isShowing()) {
                        dialogEspera.dispose();
                        dialogEspera = null;
                    }
                    vaciarTabla(leaderboard);
                    vaciarPanelApuestas();
                    vaciarApuestas();
                    frame.setEnabled(true);
                    frame.requestFocus();

                    tiempoFinal = System.currentTimeMillis() + TIEMPO_FIJO;
                    accion04Ejecutada = false;

                    esPrimeraIteracion[0] = false;

                    contadorTimer.start();
                }
            }
        });

        contadorTimer.start();
    }


    private void envioapuestas(ArrayList<String> apuestas) {
        Thread serverWriterThread = new Thread(() -> {
            try {
                ruletaOut.writeObject(apuestas);
                ruletaOut.flush();
                ruletaOut.reset();
                vaciarApuestas();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        serverWriterThread.start();
    }
    private void enviousr(String usr) {
        Thread serverWriterThread = new Thread(() -> {
            try {
                ruletaOut.writeObject("U"+usr);
                ruletaOut.flush();
                ruletaOut.reset();
                vaciarApuestas();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        serverWriterThread.start();
    }

    private JDialog mostrarPopup(String mensaje, String titulo) {
        JOptionPane optionPane = new JOptionPane(mensaje, JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);
        JDialog dialog = optionPane.createDialog(titulo);

        dialog.setModal(false);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
            }
        });
        int popupWidth = dialog.getWidth();
        int popupHeight = dialog.getHeight();
        int popupX = (screenWidth - 400) / 2; // Centrado para el primer popup
        int popupY = (screenHeight - popupHeight) / 2;
        dialog.setLocation(popupX, popupY);
        dialog.setVisible(true);

        return dialog;
    }

    private JDialog mostrarPopup_v2(String mensaje, String titulo) {
        JOptionPane optionPane = new JOptionPane(mensaje, JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);
        JDialog dialog = optionPane.createDialog(titulo);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setModal(false);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
            }
        });
        int popupWidth = dialog.getWidth();
        int popupHeight = dialog.getHeight();
        int popupX = (screenWidth - 400) / 2 + popupWidth; // Centrado y ajustado para el segundo popup
        int popupY = (screenHeight - popupHeight) / 2;
        dialog.setLocation(popupX, popupY);
        dialog.setVisible(true);

        return dialog;
    }


    private void mostrarLeaderboard(String leido, JTable leaderboard) {
        // Extrae los datos del string
        String leaderboardData = leido.substring(leido.indexOf(",") + 1);
        String[] usuarios = leaderboardData.split(",");

        java.util.List<String[]> leaderboardList = new ArrayList<>();
        int rank = 1;
        for (String usuario : usuarios) {
            String[] parts = usuario.trim().split(" ");
            if (parts.length == 2) {
                leaderboardList.add(new String[]{rank + "º", parts[0], parts[1]});
                rank++;
            }
        }

        // Actualiza el modelo del JTable
        DefaultTableModel model = (DefaultTableModel) leaderboard.getModel();
        model.setRowCount(0); // Vacía filas anteriores
        for (String[] fila : leaderboardList) {
            model.addRow(fila); // Añade nuevas filas
        }
    }



}