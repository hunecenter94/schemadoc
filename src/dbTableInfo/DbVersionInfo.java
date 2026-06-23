package dbTableInfo;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * DB 종류 + 버전별 JDBC 접속 메타 정보 <br>
 * - driverClassName : 버전이 달라도 보통 동일하지만, 일부 구버전은 클래스명 자체가 다르므로 버전별로 관리 <br>
 * - urlTemplate      : {HOST}, {PORT}, {DBNAME} 치환 토큰 사용 <br>
 * - defaultPort       : 버전 선택 시 자동으로 채워줄 기본 포트 <br>
 * - jarHint           : 해당 버전에 필요한 권장 jar 파일명 (안내용 문구, 실제 로드와는 무관) <br>
 *
 * 주의: 실제로 어떤 jar를 쓰든 클래스명이 같다면 동작한다. 즉 이 클래스가 강제하는 것은 "선택한 jar가
 * 이 driverClassName을 포함하고 있어야 한다"는 점뿐이며, jar 파일 자체의 유효성은 보장하지 않는다.
 */
public class DbVersionInfo {

	public final String dbType;
	public final String version;
	public final String driverClassName;
	public final String urlTemplate;
	public final String defaultPort;
	public final String jarHint;

	public DbVersionInfo(String dbType, String version, String driverClassName, String urlTemplate, String defaultPort, String jarHint) {
		this.dbType = dbType;
		this.version = version;
		this.driverClassName = driverClassName;
		this.urlTemplate = urlTemplate;
		this.defaultPort = defaultPort;
		this.jarHint = jarHint;
	}

	/** dbType -> 버전 목록(순서 보장) */
	private static final Map<String, Map<String, DbVersionInfo>> REGISTRY = new LinkedHashMap<>();

	private static void register(DbVersionInfo info) {
		REGISTRY.computeIfAbsent(info.dbType, k -> new LinkedHashMap<>()).put(info.version, info);
	}

	static {
		// ===== Oracle =====
		// 클래스명/URL은 9i 이후 동일. jar(ojdbc6/8/10/11/17)만 다르다.
		register(new DbVersionInfo(DbTableInfo.Oracle, "11g",
				"oracle.jdbc.driver.OracleDriver",
				"jdbc:oracle:thin:@{HOST}:{PORT}:{DBNAME}", "1521", "ojdbc6.jar"));
		register(new DbVersionInfo(DbTableInfo.Oracle, "12c",
				"oracle.jdbc.driver.OracleDriver",
				"jdbc:oracle:thin:@{HOST}:{PORT}/{DBNAME}", "1521", "ojdbc7.jar / ojdbc8.jar"));
		register(new DbVersionInfo(DbTableInfo.Oracle, "18c/19c",
				"oracle.jdbc.driver.OracleDriver",
				"jdbc:oracle:thin:@{HOST}:{PORT}/{DBNAME}", "1521", "ojdbc8.jar"));
		register(new DbVersionInfo(DbTableInfo.Oracle, "21c",
				"oracle.jdbc.driver.OracleDriver",
				"jdbc:oracle:thin:@{HOST}:{PORT}/{DBNAME}", "1521", "ojdbc8.jar / ojdbc11.jar"));
		register(new DbVersionInfo(DbTableInfo.Oracle, "23ai",
				"oracle.jdbc.driver.OracleDriver",
				"jdbc:oracle:thin:@{HOST}:{PORT}/{DBNAME}", "1521", "ojdbc11.jar / ojdbc17.jar"));

		// ===== MySQL =====
		register(new DbVersionInfo(DbTableInfo.MySQL, "5.x",
				"com.mysql.jdbc.Driver",
				"jdbc:mysql://{HOST}:{PORT}/{DBNAME}", "3306", "mysql-connector-java-5.x.jar"));
		register(new DbVersionInfo(DbTableInfo.MySQL, "8.x",
				"com.mysql.cj.jdbc.Driver",
				"jdbc:mysql://{HOST}:{PORT}/{DBNAME}", "3306", "mysql-connector-j-8.x.jar"));

		// ===== MariaDB =====
		register(new DbVersionInfo(DbTableInfo.MariaDB, "10.x",
				"org.mariadb.jdbc.Driver",
				"jdbc:mariadb://{HOST}:{PORT}/{DBNAME}", "3306", "mariadb-java-client-3.x.jar"));
		register(new DbVersionInfo(DbTableInfo.MariaDB, "10.x (MySQL 드라이버 호환)",
				"com.mysql.cj.jdbc.Driver",
				"jdbc:mysql://{HOST}:{PORT}/{DBNAME}", "3306", "mysql-connector-j-8.x.jar"));

		// ===== PostgreSQL =====
		register(new DbVersionInfo(DbTableInfo.PostgreSQL, "9.x ~ 17.x",
				"org.postgresql.Driver",
				"jdbc:postgresql://{HOST}:{PORT}/{DBNAME}", "5432", "postgresql-42.x.jar"));

		// ===== MSSQL =====
		// 2000은 클래스명/URL이 다르므로 별도 등록
		register(new DbVersionInfo(DbTableInfo.MSSQL, "2000",
				"com.microsoft.jdbc.sqlserver.SQLServerDriver",
				"jdbc:microsoft:sqlserver://{HOST}:{PORT};DatabaseName={DBNAME}", "1433", "msutil.jar+msbase.jar+mssqlserver.jar"));
		register(new DbVersionInfo(DbTableInfo.MSSQL, "2005 ~ 2008 R2",
				"com.microsoft.sqlserver.jdbc.SQLServerDriver",
				"jdbc:sqlserver://{HOST}:{PORT};databaseName={DBNAME}", "1433", "sqljdbc4.jar"));
		register(new DbVersionInfo(DbTableInfo.MSSQL, "2012 ~ 2016",
				"com.microsoft.sqlserver.jdbc.SQLServerDriver",
				"jdbc:sqlserver://{HOST}:{PORT};databaseName={DBNAME}", "1433", "sqljdbc42.jar"));
		register(new DbVersionInfo(DbTableInfo.MSSQL, "2017 이상",
				"com.microsoft.sqlserver.jdbc.SQLServerDriver",
				"jdbc:sqlserver://{HOST}:{PORT};databaseName={DBNAME};encrypt=false", "1433", "mssql-jdbc-12.x.jre11.jar"));

		// ===== Cubrid =====
		register(new DbVersionInfo(DbTableInfo.Cubrid, "9.x ~ 11.x",
				"cubrid.jdbc.driver.CUBRIDDriver",
				"jdbc:cubrid:{HOST}:{PORT}:{DBNAME}:::", "33000", "cubrid_jdbc.jar"));

		// ===== Tibero =====
		register(new DbVersionInfo(DbTableInfo.Tibero, "5",
				"com.tmax.tibero.jdbc.TbDriver",
				"jdbc:tibero:thin:@{HOST}:{PORT}:{DBNAME}", "8629", "tibero5-jdbc.jar"));
		register(new DbVersionInfo(DbTableInfo.Tibero, "6 이상",
				"com.tmax.tibero.jdbc.TbDriver",
				"jdbc:tibero:thin:@{HOST}:{PORT}:{DBNAME}", "8629", "tibero6-jdbc.jar"));
	}

	/** DB 종류에 해당하는 버전 목록 (콤보박스용) */
	public static String[] getVersions(String dbType) {
		Map<String, DbVersionInfo> versions = REGISTRY.get(dbType);
		if(versions == null) return new String[0];
		return versions.keySet().toArray(new String[0]);
	}

	/** DB 종류 + 버전에 해당하는 메타정보 조회 */
	public static DbVersionInfo get(String dbType, String version) {
		Map<String, DbVersionInfo> versions = REGISTRY.get(dbType);
		if(versions == null) return null;
		return versions.get(version);
	}

	/** URL 템플릿의 토큰을 실제 값으로 치환 */
	public String buildUrl(String host, String port, String dbName) {
		return urlTemplate
				.replace("{HOST}", host)
				.replace("{PORT}", port)
				.replace("{DBNAME}", dbName);
	}

}
