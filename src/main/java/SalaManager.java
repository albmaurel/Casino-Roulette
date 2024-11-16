import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SalaManager extends JFrame {

    private DefaultComboBoxModel<String> comboBoxModel;
    private JComboBox<String> comboBoxSalas;
    private JButton botonCrearSala;
    private JButton botonUnirse;
    private JTextField textoNombreSala;

    public SalaManager() {
        // Configuración básica del JFrame
        setTitle("Gestión de Salas");
        setSize(450, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout()); // Diseño profesional con GridBagLayout

        // Crear el modelo del JComboBox
        comboBoxModel = new DefaultComboBoxModel<>();
        comboBoxSalas = new JComboBox<>(comboBoxModel);

        // Crear botones y campo de texto
        botonCrearSala = new JButton("Crear Sala");
        botonUnirse = new JButton("Unirte");
        textoNombreSala = new JTextField(15); // Campo para ingresar el nombre de la sala

        // Etiqueta para el JComboBox
        JLabel etiquetaSalas = new JLabel("Salas disponibles:");

        // Configuración del layout usando GridBagConstraints
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Primera fila: Campo de texto y botón Crear Sala
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        add(new JLabel("Nombre de la sala:"), gbc);

        gbc.gridx = 1;
        add(textoNombreSala, gbc);

        gbc.gridx = 2;
        add(botonCrearSala, gbc);

        // Segunda fila: Etiqueta de salas
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        add(etiquetaSalas, gbc);

        // Segunda fila: JComboBox de salas
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        add(comboBoxSalas, gbc);

        // Tercera fila: Botón Unirte
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        add(botonUnirse, gbc);

        // Acción para crear una sala
        botonCrearSala.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nombreSala = textoNombreSala.getText().trim();
                if (!nombreSala.isEmpty()) {
                    comboBoxModel.addElement(nombreSala);
                    JOptionPane.showMessageDialog(SalaManager.this, "Sala creada: " + nombreSala);
                    textoNombreSala.setText(""); // Limpiar el campo de texto después de crear la sala
                } else {
                    JOptionPane.showMessageDialog(SalaManager.this, "Por favor, ingrese un nombre para la sala.");
                }
            }
        });

        // Acción para unirse a una sala
        botonUnirse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String salaSeleccionada = (String) comboBoxSalas.getSelectedItem();
                if (salaSeleccionada != null) {
                    JOptionPane.showMessageDialog(SalaManager.this, "Te has unido a la sala: " + salaSeleccionada);
                } else {
                    JOptionPane.showMessageDialog(SalaManager.this, "Por favor, selecciona una sala.");
                }
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SalaManager salaManager = new SalaManager();
            salaManager.setVisible(true);
        });
    }
}
