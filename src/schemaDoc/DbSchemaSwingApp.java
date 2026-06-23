package schemaDoc;
import javax.swing.*;

import dbTableInfo.DbTableInfo;
import dbTableInfo.DbVersionInfo;
import dbTableInfo.JdbcDriverLoader;

import java.awt.*;
import java.io.File;

public class DbSchemaSwingApp extends JFrame {
	private static final long serialVersionUID = 1L;
	private JComboBox<String> dbTypeCombo;
    private JComboBox<String> dbVersionCombo;   // 추가: DB 버전 선택
    private JTextField jarPathField;            // 추가: JDBC 드라이버 jar 경로
    private JButton jarChooseBtn;                // 추가: jar 파일 선택 버튼
    private JLabel jarHintLabel;                 // 추가: 선택한 버전의 권장 jar 안내 문구
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
        setSize(600, 520);
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
        dbVersionCombo = new JComboBox<>();
        hostField = new JTextField("127.0.0.1");
        portField = new JTextField("3306");
        dbNameField = new JTextField();
        idField = new JTextField();
        pwdField = new JPasswordField();
        viewSelectCombo = new JComboBox<>(new String[]{"Y","N"});
        oracleConnTypeCombo = new JComboBox<>(new String[]{"SERVICE_NAME", "SID"});

        int row = 0;
        addField(panel, gbc, row++, "DB 종류", dbTypeCombo);
        addField(panel, gbc, row++, "DB 버전", dbVersionCombo);
        row++; // jar 경로는 버튼이 같이 들어가서 별도 처리
        addJarPathField(panel, gbc, row++);
        jarHintLabel = new JLabel(" ");
        jarHintLabel.setFont(jarHintLabel.getFont().deriveFont(Font.PLAIN, 11f));
        jarHintLabel.setForeground(Color.GRAY);
        gbc.gridx = 1; gbc.gridy = row++;
        panel.add(jarHintLabel, gbc);

        addField(panel, gbc, row++, "Host", hostField);
        addField(panel, gbc, row++, "Port", portField);
        addField(panel, gbc, row++, "DB명", dbNameField);
        addField(panel, gbc, row++, "ID", idField);
        addField(panel, gbc, row++, "Password", pwdField);
        addField(panel, gbc, row++, "VIEW 선택 여부", viewSelectCombo);
        oracleConnTypeLabel = addField(panel, gbc, row++, "Oracle 접속방식", oracleConnTypeCombo);

        //  DB 종류 변경 시: 버전 목록 갱신 + 기본 포트 갱신 + Oracle 접속방식 콤보 표시여부 갱신
        dbTypeCombo.addActionListener(e -> {
            updateVersionCombo();
            updateOracleConnTypeVisibility();
        });
        //  DB 버전 변경 시: 기본 포트 + 권장 jar 안내 문구 갱신
        dbVersionCombo.addActionListener(e -> updateVersionDependentFields());

        updateVersionCombo();             // 초기 상태 반영 (버전 목록 채우기)
        updateOracleConnTypeVisibility(); // 초기 상태 반영

        return panel;
    }

    //  추가: jar 경로 입력칸 + 찾아보기 버튼을 한 행에 배치
    private void addJarPathField(JPanel panel, GridBagConstraints gbc, int y) {
        gbc.gridx = 0;
        gbc.gridy = y;
        panel.add(new JLabel("JDBC Jar 경로"), gbc);

        JPanel jarRow = new JPanel(new BorderLayout(5, 0));
        jarPathField = new JTextField();
        jarChooseBtn = new JButton("찾아보기");
        jarChooseBtn.addActionListener(e -> chooseJarFile());
        jarRow.add(jarPathField, BorderLayout.CENTER);
        jarRow.add(jarChooseBtn, BorderLayout.EAST);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(jarRow, gbc);
    }

    //  추가: jar 파일 선택 다이얼로그
    private void chooseJarFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JDBC Driver (*.jar)", "jar"));
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            jarPathField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    //  추가: DB 종류가 바뀌면 해당 DB의 버전 목록으로 콤보를 갱신
    private void updateVersionCombo() {
        String dbType = (String) dbTypeCombo.getSelectedItem();
        String[] versions = DbVersionInfo.getVersions(dbType);

        dbVersionCombo.removeAllItems();
        for (String v : versions) {
            dbVersionCombo.addItem(v);
        }
        if (dbVersionCombo.getItemCount() > 0) {
            dbVersionCombo.setSelectedIndex(dbVersionCombo.getItemCount() - 1); // 최신 버전을 기본 선택
        }

        updateVersionDependentFields();
    }

    //  추가: 선택된 DB종류+버전에 맞춰 기본 Port와 권장 jar 안내 문구를 갱신
    private void updateVersionDependentFields() {
        String dbType = (String) dbTypeCombo.getSelectedItem();
        String version = (String) dbVersionCombo.getSelectedItem();
        if (dbType == null || version == null) return;

        DbVersionInfo info = DbVersionInfo.get(dbType, version);
        if (info == null) return;

        portField.setText(info.defaultPort);
        jarHintLabel.setText("권장 JDBC 드라이버: " + info.jarHint + "  (드라이버 클래스: " + info.driverClassName + ")");
    }

    //  추가: Oracle일 때만 접속방식 선택 활성화 (DB명 라벨도 SID/Service Name에 맞게 안내)
    private void updateOracleConnTypeVisibility() {
        boolean isOracle = "Oracle".equals(dbTypeCombo.getSelectedItem());
        oracleConnTypeCombo.setEnabled(isOracle);
        oracleConnTypeLabel.setEnabled(isOracle);
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

    //  추가: 현재 폼 값으로 DbVersionInfo를 조회하고, Oracle 접속방식에 따라 URL을 보정한 뒤 반환
    private DbVersionInfo resolveVersionInfo() {
        String dbType = (String) dbTypeCombo.getSelectedItem();
        String dbVersion = (String) dbVersionCombo.getSelectedItem();
        return DbVersionInfo.get(dbType, dbVersion);
    }

    private String buildJdbcUrl(DbVersionInfo info, String dbType) {
        String url = info.buildUrl(hostField.getText(), portField.getText(), dbNameField.getText());

        //  Oracle은 SID/Service Name 접속방식에 따라 구분자(: 또는 /)가 다르므로 필요시 덮어쓴다.
        if ("Oracle".equals(dbType)) {
            String oracleConnType = (String) oracleConnTypeCombo.getSelectedItem();
            String host = hostField.getText();
            String port = portField.getText();
            String dbName = dbNameField.getText();
            if ("SID".equals(oracleConnType)) {
                url = "jdbc:oracle:thin:@" + host + ":" + port + ":" + dbName;
            } else {
                url = "jdbc:oracle:thin:@" + host + ":" + port + "/" + dbName;
            }
        }
        return url;
    }

    //DB연결 테스트
    private void testConnection() {

        setFormEnabled(false);
        statusLabel.setText("연결 테스트 중...");

        new SwingWorker<Boolean, Void>() {

            @Override
            protected Boolean doInBackground() {
                String dbType = (String) dbTypeCombo.getSelectedItem();
                DbVersionInfo versionInfo = resolveVersionInfo();
                if (versionInfo == null) {
                    System.out.println("등록되지 않은 DB 종류/버전입니다: " + dbType);
                    return false;
                }

                String jarPath = jarPathField.getText();
                String url = buildJdbcUrl(versionInfo, dbType);
                String id = idField.getText();
                String pwd = new String(pwdField.getPassword());

                return JdbcDriverLoader.testConnection(jarPath, versionInfo.driverClassName, url, id, pwd);
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

        String dbType = (String) dbTypeCombo.getSelectedItem();
        String dbVersion = (String) dbVersionCombo.getSelectedItem();
        String jarPath = jarPathField.getText();
        //  Oracle이 아닌 경우 null로 전달 (DbTableInfo 쪽에서 무시됨)
        String oracleConnType = "Oracle".equals(dbType) ? (String) oracleConnTypeCombo.getSelectedItem() : null;

        new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() throws Exception {
                // 기존 엑셀 생성 로직 호출 (DB버전 / jar경로 전달)
                DbTableInfo.excelDown(
                    dbType,
                    dbVersion,
                    jarPath,
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
        dbVersionCombo.setEnabled(enabled);
        jarPathField.setEnabled(enabled);
        jarChooseBtn.setEnabled(enabled);
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
