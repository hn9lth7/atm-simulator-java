import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.Map;

public class ATMSimulator extends JFrame {
    private final Map<Integer, Integer> atmNotes;
    private final JTextField amountField;
    private final JTextArea resultArea;

    private ATMSimulator() {
        atmNotes = new HashMap<>();
        atmNotes.put(200, 10);
        atmNotes.put(100, 10);
        atmNotes.put(50, 10);
        atmNotes.put(20, 10);
        atmNotes.put(10, 10);

        setTitle("ATMSimulator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 400);
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("Сума для видачі."));
        amountField = new JTextField(10);
        inputPanel.add(amountField);
        JButton withdrawButton = new JButton("Видати");
        inputPanel.add(withdrawButton);
        add(inputPanel, BorderLayout.NORTH);

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        add(new JScrollPane(resultArea), BorderLayout.CENTER);

        withdrawButton.addActionListener(e -> processWithdrawal());

        setVisible(true);
    }

    private void processWithdrawal() {
        int requestedAmount;
        try {
            requestedAmount = Integer.parseInt(amountField.getText().trim());
            if (requestedAmount <= 0) {
                showMessage("Введіть додатню суму.", "Помилка", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException ex) {
            showMessage("Некоректне число. Введіть ціле число.", "Помилка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int totalCash = atmNotes.entrySet().stream()
                .mapToInt(entry -> entry.getKey() * entry.getValue())
                .sum();

        if (totalCash < requestedAmount) {
            showMessage("Недостатньо коштів у банкоматі.\n"
                       + "Доступно" + totalCash + "грн.", "Помилка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Map<Integer, Integer> tempNotes = new HashMap<>(atmNotes);
        Map<Integer, Integer> dispensed = new TreeMap<>(Collections.reverseOrder());
        int remaining = requestedAmount;

        java.util.List<Integer> denominations = new ArrayList<>(atmNotes.keySet());
        denominations.sort(Collections.reverseOrder());

        for (int denom : denominations) {
            int available = tempNotes.get(denom);
            int needed = remaining / denom;
            int take = Math.min(available, needed);
            if (take > 0) {
                dispensed.put(denom, take);
                remaining -= take * denom;
                tempNotes.put(denom, available - take);
            }
        }

        if (remaining == 0) {
            atmNotes.clear();
            atmNotes.putAll(tempNotes);
            StringBuilder sb = new StringBuilder("Видано" + requestedAmount + "грн:\n");
            for (Map.Entry<Integer, Integer> entry : dispensed.entrySet()) {
                sb.append(entry.getValue()).append(" x ").append(entry.getKey()).append(" грн\n");
            }
            sb.append("\nЗалишок у банкоматі:\n");
            for (Map.Entry<Integer, Integer> entry : atmNotes.entrySet()) {
                if (entry.getValue() > 0) {
                    sb.append(entry.getValue()).append(" x ").append(entry.getKey()).append("грн\n");
                }
            }
            resultArea.setText(sb.toString());
            showMessage("Операцію виконано успішно!", "Успіх", JOptionPane.INFORMATION_MESSAGE);
        } else {
            showMessage("Неможливо видати точну суму наявними купюрами.\n"
                    + "Спробуйте іншу суму.", "Помилка", JOptionPane.ERROR_MESSAGE);
        }
        amountField.setText("");
    }

    private void showMessage(String msg, String title, int type) {
        JOptionPane.showMessageDialog(this, msg, title, type);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ATMSimulator::new);
    }
}