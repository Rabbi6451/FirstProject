
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

class Student {
    String name;
    String batch;
    String roll;
    boolean isPresent;
    String date;

    public Student(String name, String batch, String roll, String date, boolean isPresent) {
        this.name = name;
        this.batch = batch;
        this.roll = roll;
        this.date = date;
        this.isPresent = isPresent;
    }

    public String toFileString() {
        return "Name: " + name + "\n"
                + "Batch: " + batch + "\n"
                + "Roll: " + roll + "\n"
                + "Status: " + (isPresent ? "Present" : "Absent") + "\n"
                + "Date: " + date + "\n";
    }

    public static Student fromFileString(String line) {
        String[] parts = line.split(",");
        return new Student(
                parts[0].trim(),
                parts[1].trim(),
                parts[2].trim(),
                parts[4].trim(),
                parts[3].trim().equals("Present")
        );
    }
}

class  Attendance extends JFrame {
    private final ArrayList<Student> studentList = new ArrayList<>();
    private final DefaultTableModel tableModel;
    private final JTable studentTable;
    private final File dataFile;

    public Attendance() {
        File directory = new File("D:\\Attendance");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        dataFile = new File(directory, "project14.txt");

        setTitle("Attendance Management System");
        setSize(1200, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(249, 24, 244));
        try {
            Image icon = ImageIO.read(new File("D:\\AttendanceSystem.png"));
            setIconImage(icon);
// Set the icon for the title bar
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading icon: " + e.getMessage());
        }

        JLabel heading = new JLabel("Attendance Management System", JLabel.CENTER);
        heading.setFont(new Font("Courier New", Font.BOLD, 20));
        heading.setBackground(new Color(200, 220, 255)); // Light blue


        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBackground(new Color(20, 240, 220)); // Light green


        JPanel addPanel = new JPanel(new FlowLayout());
        JLabel nameLabel = new JLabel("Student Name: ");
        JTextField nameField = new JTextField(15);
        JLabel batchLabel = new JLabel("Batch: ");
        JTextField batchField = new JTextField(15);
        JLabel rollLabel = new JLabel("Roll: ");
        JTextField rollField = new JTextField(10);
        JButton addButton = new JButton("Add Student");

        addPanel.add(nameLabel);
        addPanel.add(nameField);
        addPanel.add(batchLabel);
        addPanel.add(batchField);
        addPanel.add(rollLabel);
        addPanel.add(rollField);
        addPanel.add(addButton);

        String[] columnNames = {"Serial No.", "Name", "Batch", "Roll", "Status", "Date"};
        tableModel = new DefaultTableModel(columnNames, 0);
        studentTable = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(studentTable);
        studentTable.setBackground(new Color(25, 255, 240)); // Light cream


        inputPanel.add(addPanel, BorderLayout.NORTH);
        inputPanel.add(tableScrollPane, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        controlPanel.setBackground(new Color(220, 220, 255));
        JButton markPresentButton = new JButton("Mark Present");
        JButton saveButton = new JButton("Save to File");
        JButton deleteButton = new JButton("Delete Student");
        JButton searchButton = new JButton("Search by Roll");
        JButton statsButton = new JButton("Show Statistics");

        controlPanel.add(markPresentButton);
        controlPanel.add(saveButton);
        controlPanel.add(deleteButton);
        controlPanel.add(searchButton);
        controlPanel.add(statsButton);

        add(heading, BorderLayout.NORTH);
        add(inputPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String batch = batchField.getText().trim();
            String roll = rollField.getText().trim();
            String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

            if (!name.matches("[a-zA-Z ]+")) {
                JOptionPane.showMessageDialog(null, "Wrong Name! Name should contain only letters.");
                return;
            }
            if (!roll.matches("\\d+")) {
                JOptionPane.showMessageDialog(null, "Wrong Roll! Roll should contain only numbers.", "Warning", 0);
                return;
            }

            if (!name.isEmpty() && !batch.isEmpty() && !roll.isEmpty()) {
                boolean duplicate = false;
                for (Student s : studentList) {
                    if (s.roll.equalsIgnoreCase(roll) && s.batch.equalsIgnoreCase(batch)) {
                        duplicate = true;
                        break;
                    }
                }
                if (duplicate) {
                    JOptionPane.showMessageDialog(null, "It's already exists");
                    return;
                }

                Student newStudent = new Student(name, batch, roll, currentDate, false);
                studentList.add(newStudent);
                tableModel.addRow(new Object[]{
                        studentList.size(),
                        newStudent.name,
                        newStudent.batch,
                        newStudent.roll,
                        "Absent",
                        currentDate
                });

                nameField.setText("");
                batchField.setText("");
                rollField.setText("");
            } else {
                JOptionPane.showMessageDialog(null, "Please fill all fields!");
            }
        });

        markPresentButton.addActionListener(e -> {
            String rollNumber = JOptionPane.showInputDialog("Enter the student's roll number to mark present:");
            if (rollNumber == null || rollNumber.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please enter a roll number.");
                return;
            }

            boolean found = false;
            for (int i = 0; i < studentList.size(); i++) {
                Student student = studentList.get(i);
                if (student.roll.equalsIgnoreCase(rollNumber)) {
                    student.isPresent = true;
                    student.date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                    tableModel.setValueAt("Present", i, 4);
                    tableModel.setValueAt(student.date, i, 5);
                    found = true;
                    break;
                }
            }
            if (!found) {
                JOptionPane.showMessageDialog(null, "Student with Roll Number " + rollNumber + " not found!");
            }
        });

        deleteButton.addActionListener(e -> {
            String rollNumber = JOptionPane.showInputDialog("Enter the roll number of the student to delete:");
            if (rollNumber == null || rollNumber.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please enter a roll number.");
                return;
            }

            Iterator<Student> iterator = studentList.iterator();
            boolean found = false;
            int indexToRemove = -1;

            while (iterator.hasNext()) {
                Student student = iterator.next();
                if (student.roll.equalsIgnoreCase(rollNumber)) {
                    indexToRemove = studentList.indexOf(student);
                    iterator.remove();
                    found = true;
                    break;
                }
            }

            if (found && indexToRemove != -1) {
                tableModel.removeRow(indexToRemove);
                saveToFile();
            } else {
                JOptionPane.showMessageDialog(null, "Student with Roll Number " + rollNumber + " not found!");
            }
        });

        saveButton.addActionListener(e -> {
            int recordsSaved = saveToFile();
            JOptionPane.showMessageDialog(null, recordsSaved + " records saved successfully!");
        });
    }

    private int saveToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(dataFile))) {
            for (Student student : studentList) {
                writer.write(student.toFileString());
                writer.newLine();
            }
            return studentList.size();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving to file: " + e.getMessage());
            return 0;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Attendance frame = new Attendance();
            frame.setVisible(true);
        });
    }
}
