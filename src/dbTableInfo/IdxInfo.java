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
				.append(" 	tablename, ")
				.append(" 	indexname, ")
				.append(" 	substring(indexdef from '\\((.*?)\\)') ")
				.append(" from ")
				.append(" 	pg_indexes ")
				.append(" where ")
				.append(" 	schemaname = '").append(dbName).append("' ")
				.append(" order by ")
				.append(" 	tablename ");
		} else if(DbTableInfo.MSSQL.equals(dbType)) { // MSSQL
			sql	.append(" SELECT ")
				.append("	T.NAME AS TABLE_NAME, ")
				.append("	I.NAME AS INDEX_NAME, ")
				.append("	STUFF(( ")
				.append("		SELECT ', ' + C.NAME ")
				.append("		FROM SYS.INDEX_COLUMNS IC ")
				.append("		JOIN SYS.COLUMNS C ")
				.append("			ON IC.OBJECT_ID = C.OBJECT_ID ")
				.append("			AND IC.COLUMN_ID = C.COLUMN_ID ")
				.append("		WHERE IC.OBJECT_ID = I.OBJECT_ID ")
				.append("			AND IC.INDEX_ID = I.INDEX_ID ")
				.append("		ORDER BY IC.KEY_ORDINAL ")
				.append("		FOR XML PATH('') ")
				.append("	), 1, 2, '') AS COLUMNS, ")
				.append("	I.IS_UNIQUE ")
				.append(" FROM ")
				.append("	SYS.INDEXES I ")
				.append(" JOIN SYS.TABLES T ")
				.append("	ON I.OBJECT_ID = T.OBJECT_ID ")
				.append(" WHERE ")
				.append("	I.NAME IS NOT NULL ")
				.append(" ORDER BY T.NAME, I.NAME ");
		} else if(DbTableInfo.Cubrid.equals(dbType)) { // CUBRID
			sql	.append(" SELECT ")
				.append("	IK.CLASS_NAME AS TABLE_NAME, ")
				.append("	IK.INDEX_NAME, ")
				.append("	GROUP_CONCAT(IK.KEY_ATTR_NAME ORDER BY IK.KEY_ORDER SEPARATOR ', ') AS COLUMNS ")
				.append(" FROM ")
				.append("	DB_INDEX_KEY IK ")
				.append(" WHERE ")
				.append("	IK.CLASS_NAME IN ( ")
				.append("		SELECT CLASS_NAME FROM DB_CLASS WHERE IS_SYSTEM_CLASS = 'NO' AND OWNER_NAME = '").append(dbName).append("' ")
				.append("	) ")
				.append(" GROUP BY IK.CLASS_NAME, IK.INDEX_NAME ")
				.append(" ORDER BY IK.CLASS_NAME, IK.INDEX_NAME ");
		} else if(DbTableInfo.Tibero.equals(dbType)) { // TIBERO (Oracle 호환)
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
		}
		
		System.out.println(sql.toString());
		
		return sql.toString();
	}
}