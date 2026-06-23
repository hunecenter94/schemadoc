package dbTableInfo;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 사용자가 지정한 jar 파일 경로에서 JDBC 드라이버를 동적으로 로드하여 커넥션을 생성한다. <br>
 * DriverManager를 사용하지 않고 java.sql.Driver 인스턴스를 직접 생성/호출하므로,
 * 시스템 클래스로더가 아닌 URLClassLoader로 로드한 드라이버도 정상적으로 사용할 수 있다. <br>
 * (DriverManager.getConnection()은 시스템 클래스로더로 로드된 드라이버만 인식하기 때문에 직접 connect()를 호출한다.)
 */
public class JdbcDriverLoader {

	/**
	 * 지정한 jar 파일에서 driverClassName 클래스를 로드하여 DB에 접속한다.
	 *
	 * @param jarPath         JDBC 드라이버 jar 파일의 절대경로
	 * @param driverClassName 드라이버 클래스 풀패키지명 (예: oracle.jdbc.driver.OracleDriver)
	 * @param url             JDBC 접속 URL
	 * @param id              접속 ID
	 * @param pwd             접속 PW
	 * @return Connection (호출한 곳에서 close 책임)
	 * @throws Exception jar 경로가 잘못되었거나, 클래스가 없거나, 접속에 실패한 경우
	 */
	public static Connection getConnection(String jarPath, String driverClassName, String url, String id, String pwd) throws Exception {
		if(jarPath == null || jarPath.trim().isEmpty()) {
			throw new IllegalArgumentException("JDBC 드라이버 jar 파일 경로가 지정되지 않았습니다.");
		}

		File jarFile = new File(jarPath);
		if(!jarFile.exists() || !jarFile.isFile()) {
			throw new IllegalArgumentException("JDBC 드라이버 jar 파일을 찾을 수 없습니다: " + jarPath);
		}

		URL jarUrl;
		try {
			jarUrl = jarFile.toURI().toURL();
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("JDBC 드라이버 jar 경로가 올바르지 않습니다: " + jarPath, e);
		}

		// 이 jar 전용 클래스로더 생성 (부모는 현재 클래스로더 -> java.sql.* 인터페이스는 공유)
		URLClassLoader jarClassLoader = new URLClassLoader(new URL[] { jarUrl }, JdbcDriverLoader.class.getClassLoader());

		Class<?> driverClass = Class.forName(driverClassName, true, jarClassLoader);
		Driver driver = (Driver) driverClass.getDeclaredConstructor().newInstance();

		Properties props = new Properties();
		props.setProperty("user", id == null ? "" : id);
		props.setProperty("password", pwd == null ? "" : pwd);

		Connection conn = driver.connect(url, props);
		if(conn == null) {
			throw new SQLException("드라이버가 해당 URL을 처리할 수 없습니다. URL 형식 또는 드라이버 버전을 확인하세요. URL=" + url);
		}

		return conn;
	}

	/**
	 * 단순 연결 테스트용 (커넥션을 즉시 닫는다)
	 *
	 * @return 연결 성공 여부
	 */
	public static boolean testConnection(String jarPath, String driverClassName, String url, String id, String pwd) {
		Connection conn = null;
		try {
			conn = getConnection(jarPath, driverClassName, url, id, pwd);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if(conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
