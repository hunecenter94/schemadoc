package dbTableInfo;

/**
 * tableNm: 테이블명<br>
 * idxNm: 인덱스명<br>
 * cols: 컬럼 목록
 */
public class IdxInfo {
	
	public IdxInfo(String[] data) {
		setTableNm(data[0]);
		setIdxNm(data[1]);
		setCols(data[2]);
	}
	
	/** 테이블 명 */
	private String tableNm;
	
	/** 인덱스 명 */
	private String idxNm;
	
	/** 컬럼 목록 */
	private String cols;
	
	public String getTableNm() {
		return tableNm;
	}

	public void setTableNm(String tableNm) {
		this.tableNm = tableNm;
	}

	public String getIdxNm() {
		return idxNm;
	}

	public void setIdxNm(String idxNm) {
		this.idxNm = idxNm;
	}

	public String getCols() {
		return cols;
	}

	public void setCols(String cols) {
		this.cols = cols;
	}

	/** 컬럼 정보 조회 SQL
	 * @return TABLE_NAME, INDEX_NAME, COLUMNS
	 */
	public static String getIdxInfoSQL(String dbType, String dbName) {
		StringBuilder sql = new StringBuilder();
		
		if(DbTableInfo.Oracle.equals(dbType)) {	// ORACLE
			sql	.append(" SELECT ")
				.append("	COLS.TABLE_NAME, ")
				.append("	COLS.INDEX_NAME, ")
				.append("	LISTAGG(COLS.COLUMN_NAME, ', ') WITHIN GROUP (ORDER BY COLS.COLUMN_POSITION) AS COLUMNS, ")
				.append("	INDS.GENERATED ")
				.append(" FROM ")
				.append("	USER_IND_COLUMNS COLS ")
				.append(" LEFT JOIN USER_INDEXES INDS ")
				.append("	ON COLS.INDEX_NAME = INDS.INDEX_NAME ")
				.append("	AND COLS.TABLE_NAME = INDS.TABLE_NAME ")
				.append(" GROUP BY COLS.TABLE_NAME, COLS.INDEX_NAME, INDS.GENERATED ")
				.append(" ORDER BY COLS.TABLE_NAME, INDS.GENERATED DESC, COLS.INDEX_NAME ");
		} else if(DbTableInfo.MySQL.equals(dbType)||DbTableInfo.MariaDB.equals(dbType)) {	// MySQL, MariaDB
			sql	.append(" SELECT ")
				.append("	TABLE_NAME, ")
				.append("	INDEX_NAME, ")
				.append("	GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX SEPARATOR ', ') AS COLUMNS, ")
				.append("	NON_UNIQUE ")
				.append(" FROM ")
				.append("	information_schema.STATISTICS ")
				.append(" WHERE ")
				.append("	TABLE_SCHEMA = DATABASE() ")
				.append(" GROUP BY TABLE_NAME, INDEX_NAME, NON_UNIQUE ")
				.append(" ORDER BY TABLE_NAME, NON_UNIQUE, INDEX_NAME ");
		} else if(DbTableInfo.PostgreSQL.equals(dbType)) { //PostgreSQL
			sql	.append(" select ")
				.append("     tablename, ")
				.append("     indexname, ")
				.append("     substring(indexdef FROM '\((.*?)\)') ")
				.append(" from ")
				.append("     pg_indexes ")
				.append(" where ")
				.append("     schemaname = '").append(dbName).append("' ")
				.append(" order by ")
				.append("     tablename ");
		}
		
		System.out.println(sql.toString());
		
		return sql.toString();
	}
}
