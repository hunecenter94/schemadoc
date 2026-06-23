package schemaDoc;
import javax.swing.*;

import dbTableInfo.DbTableInfo;

import java.awt.*;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;

public class DbSchemaSwingApp extends JFrame {
	private static final long serialVersionUID = 1L;
	private JComboBox<String> dbTypeCombo;
    private JTextField hostField;
    private JTextField portField;
    private JTextField dbNameField;
    private JTextField idField;
    private JPasswordField pwdField;
    private JLabel statusLabel;
    private JComboBox<String> viewSelectCombo;
    private JComboBox<String> oracleConnTypeCombo; // Oracle SID/Service Name 선택
    private JLabel oracleConnTypeLabel;             // 라벨도 같이 보이고 숨김 처리

    public DbSchemaSwingApp() {
        setTitle("DB → Excel Tool");
        setSize(400, 380);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        add(createInputPanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    // ✔️ 입력폼
    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("DB 접속 정보"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        dbTypeCombo = new JComboBox<>(new String[]{"MySQL", "Oracle", "MariaDB","PostgreSQL","MSSQL","Cubrid","Tibero"});
        hostField = new JTextField("127.0.0.1");
        portField = new JTextField("3306");
        dbNameField = new JTextField();
        idField = new JTextField();
        pwdField = new JPasswordField();
        viewSelectCombo = new JComboBox<>(new String[]{"Y","N"});
        oracleConnTypeCombo = new JComboBox<>(new String[]{"SERVICE_NAME", "SID"});

        addField(panel, gbc, 0, "DB 종류", dbTypeCombo);
        addField(panel, gbc, 2, "Host", hostField);
        addField(panel, gbc, 3, "Port", portField);
        addField(panel, gbc, 4, "DB명", dbNameField);
        addField(panel, gbc, 5, "ID", idField);
        addField(panel, gbc, 6, "Password", pwdField);
        addField(panel, gbc, 7, "VIEW 선택 여부", viewSelectCombo);
        oracleConnTypeLabel = addField(panel, gbc, 8, "Oracle 접속방식", oracleConnTypeCombo);

        //  DB 종류가 Oracle일 때만 접속방식 콤보 활성화
        dbTypeCombo.addActionListener(e -> updateOracleConnTypeVisibility());
        dbTypeCombo.addActionListener(e -> updateDefaultPort());
        updateOracleConnTypeVisibility(); // 초기 상태 반영

        return panel;
    }

    //  추가: Oracle일 때만 접속방식 선택 활성화 (DB명 라벨도 SID/Service Name에 맞게 안내)
    private void updateOracleConnTypeVisibility() {
        boolean isOracle = "Oracle".equals(dbTypeCombo.getSelectedItem());
        oracleConnTypeCombo.setEnabled(isOracle);
        oracleConnTypeLabel.setEnabled(isOracle);
    }

    //  추가: DB 종류 변경 시 기본 Port 값 자동 입력
    private void updateDefaultPort() {
        String dbType = (String) dbTypeCombo.getSelectedItem();
        String defaultPort = "3306";
        switch (dbType) {
            case "MySQL":
            case "MariaDB":
                defaultPort = "3306";
                break;
            case "Oracle":
                defaultPort = "1521";
                break;
            case "PostgreSQL":
                defaultPort = "5432";
                break;
            case "MSSQL":
                defaultPort = "1433";
                break;
            case "Cubrid":
                defaultPort = "33000";
                break;
            case "Tibero":
                defaultPort = "8629";
                break;
        }
        portField.setText(defaultPort);
    }

    //  변경: JLabel을 반환하도록 수정 (Oracle 접속방식 라벨을 따로 컨트롤하기 위함)
    private JLabel addField(JPanel panel, GridBagConstraints gbc, int y, String label, JComponent comp) {
        gbc.gridx = 0;
        gbc.gridy = y;
        JLabel jLabel = new JLabel(label);
        panel.add(jLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0; // 입력창이 남은 공간 다 먹음
        panel.add(comp, gbc);

        return jLabel;
    }

    // ✔️ 버튼 + 상태
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel btnPanel = new JPanel();

        JButton testBtn = new JButton("연결 테스트");
        JButton downloadBtn = new JButton("엑셀 다운로드");

        btnPanel.add(testBtn);
        btnPanel.add(downloadBtn);

        statusLabel = new JLabel("상태: 대기중");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        panel.add(btnPanel, BorderLayout.CENTER);
        panel.add(statusLabel, BorderLayout.SOUTH);

        // 이벤트
        testBtn.addActionListener(e -> testConnection());
        downloadBtn.addActionListener(e -> downloadExcel());

        return panel;
    }

    //DB연결 테스트
    private void testConnection() {

        setFormEnabled(false);
        statusLabel.setText("연결 테스트 중...");

        new SwingWorker<Boolean, Void>() {

            @Override
            protected Boolean doInBackground() {
                String dbType = (String) dbTypeCombo.getSelectedItem();
                String host = hostField.getText();
                String port = portField.getText();
                String dbName = dbNameField.getText();
                String id = idField.getText();
                String pwd = new String(pwdField.getPassword());
                String oracleConnType = (String) oracleConnTypeCombo.getSelectedItem(); //  추가

                String url = "";

                try {
                    if ("MySQL".equals(dbType) || "MariaDB".equals(dbType)) {
                        url = "jdbc:mysql://" + host + ":" + port + "/" + dbName;
                        Class.forName("com.mysql.cj.jdbc.Driver");
                    } else if ("Oracle".equals(dbType)) {
                        //  변경: SID/Service Name 분기 처리
                        if ("SID".equals(oracleConnType)) {
                            url = "jdbc:oracle:thin:@" + host + ":" + port + ":" + dbName;
                        } else {
                            url = "jdbc:oracle:thin:@" + host + ":" + port + "/" + dbName;
                        }
                        Class.forName("oracle.jdbc.driver.OracleDriver");
                    } else if("PostgreSQL".equals(dbType)) {
                    	url = "jdbc:postgresql://" + host + ":" + port + "/" + dbName;
                        Class.forName("org.postgresql.Driver");
                    } else if("MSSQL".equals(dbType)) {
                    	url = "jdbc:sqlserver://" + host + ":" + port + ";databaseName=" + dbName + ";encrypt=false";
                        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                    } else if("Cubrid".equals(dbType)) {
                    	url = "jdbc:cubrid:" + host + ":" + port + ":" + dbName + ":::";
                        Class.forName("cubrid.jdbc.driver.CUBRIDDriver");
                    } else if("Tibero".equals(dbType)) {
                    	url = "jdbc:tibero:thin:@" + host + ":" + port + ":" + dbName;
                        Class.forName("com.tmax.tibero.jdbc.TbDriver");
                    }

                    Connection conn = DriverManager.getConnection(url, id, pwd);
                    conn.close();
                    return true;

                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    statusLabel.setText(success ? "연결 성공 ✅" : "연결 실패 ❌");
                } catch (Exception e) {
                    statusLabel.setText("연결 실패 ❌");
                }
                setFormEnabled(true);
            }

        }.execute();
    }

    private void downloadExcel() {

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("db_schema.xlsx"));

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();

        // 확장자 자동 추가
        if (!file.getName().endsWith(".xlsx")) {
            file = new File(file.getAbsolutePath() + ".xlsx");
        }

        statusLabel.setText("엑셀 생성 중...");
        setFormEnabled(false);
        
        File finalFile = file;

        //  Oracle이 아닌 경우 null로 전달 (DbTableInfo 쪽에서 무시됨)
        String dbType = (String) dbTypeCombo.getSelectedItem();
        String oracleConnType = "Oracle".equals(dbType) ? (String) oracleConnTypeCombo.getSelectedItem() : null;

        new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() throws Exception {
                // 기존 엑셀 생성 로직 호출
                DbTableInfo.excelDown(
                    dbType,
                    hostField.getText(),
                    portField.getText(),
                    idField.getText(),
                    new String(pwdField.getPassword()),
                    dbNameField.getText(),
                    oracleConnType,
                    (String) viewSelectCombo.getSelectedItem(),
                    finalFile
                );
                return null;
            }

            @Override
            protected void done() {
            	 setFormEnabled(true);
                statusLabel.setText("완료 ✅");

                JOptionPane.showMessageDialog(null, "엑셀 생성 완료!");

                // 👉 자동으로 파일 열기
                try {
                    Desktop.getDesktop().open(finalFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }.execute();
    }

    // ✔️ 실행
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new DbSchemaSwingApp().setVisible(true);
        });
    }
    
    private void setFormEnabled(boolean enabled) {
        dbTypeCombo.setEnabled(enabled);
        hostField.setEnabled(enabled);
        portField.setEnabled(enabled);
        dbNameField.setEnabled(enabled);
        idField.setEnabled(enabled);
        pwdField.setEnabled(enabled);
        viewSelectCombo.setEnabled(enabled);

        //  Oracle 접속방식 콤보는 DB종류가 Oracle일 때만 다시 활성화
        boolean isOracle = "Oracle".equals(dbTypeCombo.getSelectedItem());
        oracleConnTypeCombo.setEnabled(enabled && isOracle);
        oracleConnTypeLabel.setEnabled(enabled && isOracle);

        // 버튼도 같이 막기
        for (Component comp : ((JPanel)((JPanel)getContentPane()
                .getComponent(1)).getComponent(0)).getComponents()) {
            comp.setEnabled(enabled);
        }
    }
}
