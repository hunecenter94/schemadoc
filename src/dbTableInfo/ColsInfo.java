package dbTableInfo;

/**
 * tableNm: 테이블명<br>
 * colNm: 컬럼명<br>
 * dataTy: 데이터 타입<br>
 * dataLen: 데이터 길이<br>
 * notNull: Not Null 여부<br>
 * comments: 컬럼 설명<br>
 * key: key(PK/FK)<br>
 * fkInfo: FK 정보
 */
public class ColsInfo {
	
	public ColsInfo(String[] data) {
		setTableNm(data[0]);
		setColNm(data[1]);
		setDataTy(data[2]);
		setDataLen(data[3]);
		setNotNull(data[4]);
		setComments(data[5]);
		setKey(data[6]);
		setFkInfo(data[7]);
	}
	
	/** 테이블 명 */
	private String tableNm;
	
	/** 컬럼 명 */
	private String colNm;
	
	/** 데이터 타입 */
	private String dataTy;
	
	/** 데이터 길이 */
	private String dataLen;
	
	/** not null 여부 */
	private String notNull;
	
	/** 컬럼 설명 */
	private String comments;
	
	/** key(PK/FK) */
	private String key;
	
	/** FK 정보 */
	private String fkInfo;
	
	public String getTableNm() {
		return tableNm;
	}

	public void setTableNm(String tableNm) {
		this.tableNm = tableNm;
	}

	public String getColNm() {
		return colNm;
	}

	public void setColNm(String colNm) {
		this.colNm = colNm;
	}

	public String getDataTy() {
		return dataTy;
	}

	public void setDataTy(String dataTy) {
		this.dataTy = dataTy;
	}

	public String getDataLen() {
		return dataLen;
	}

	public void setDataLen(String dataLen) {
		this.dataLen = dataLen;
	}

	public String getNotNull() {
		return notNull;
	}

	public void setNotNull(String notNull) {
		this.notNull = notNull;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}
	
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getFkInfo() {
		return fkInfo;
	}

	public void setFkInfo(String fkInfo) {
		this.fkInfo = fkInfo;
	}
	
	/** 컬럼 정보 조회 SQL
	 * @return TABLE_NAME, COLUMN_NAME, DATA_TYPE, DATA_LENGTH, NOT NULL, COMMENTS, KEY_TYPE, FK_INFO
	 */
	public static String getColsInfoSQL(String dbType) {
		StringBuilder sql = new StringBuilder();
		
		if(DbTableInfo.Oracle.equals(dbType)) {	// ORACLE
			sql	.append(" SELECT ")
				.append("	COLS.TABLE_NAME, ")
				.append("	COLS.COLUMN_NAME, ")
				.append("	COLS.DATA_TYPE, ")
				.append("	COLS.DATA_LENGTH, ")
				.append("	CASE WHEN COLS.NULLABLE = 'N' THEN 'Yes' ELSE 'No' END AS \"NOT NULL\", ")
				.append("	COL_CMTS.COMMENTS, ")
				.append("	REF_CONS.KEY_TYPE, ")
				.append("	REF_CONS.FK_INFO ")
				.append(" FROM USER_TAB_COLUMNS COLS ")
				.append(" LEFT JOIN USER_COL_COMMENTS COL_CMTS ")
				.append("	ON COLS.TABLE_NAME = COL_CMTS.TABLE_NAME ")
				.append("	AND COLS.COLUMN_NAME = COL_CMTS.COLUMN_NAME ")
				.append(" LEFT JOIN ( ")
				.append("	SELECT ")
				.append("		CONS.TABLE_NAME, ")
				.append("		CONS_COLS.COLUMN_NAME, ")
				.append("		CASE ")
				.append("			WHEN CONS.CONSTRAINT_TYPE = 'P' THEN 'PK' ")
				.append("			WHEN CONS.CONSTRAINT_TYPE = 'R' THEN 'FK' ")
				.append("		END AS KEY_TYPE, ")
				.append("		CASE ")
				.append("			WHEN CONS.CONSTRAINT_TYPE = 'R' ")
				.append("			THEN REF_CONS.TABLE_NAME || '.' || REF_COLS.COLUMN_NAME ")
				.append("		END AS FK_INFO ")
				.append("	FROM USER_CONSTRAINTS CONS ")
				.append("	JOIN USER_CONS_COLUMNS CONS_COLS ")
				.append("		ON CONS.CONSTRAINT_NAME = CONS_COLS.CONSTRAINT_NAME ")
				.append("	LEFT JOIN USER_CONSTRAINTS REF_CONS ")
				.append("		ON CONS.R_CONSTRAINT_NAME = REF_CONS.CONSTRAINT_NAME ")
				.append("	LEFT JOIN USER_CONS_COLUMNS REF_COLS ")
				.append("		ON REF_CONS.CONSTRAINT_NAME = REF_COLS.CONSTRAINT_NAME ")
				.append("		AND CONS_COLS.POSITION = REF_COLS.POSITION ")
				.append("	WHERE CONS.CONSTRAINT_TYPE IN ('P','R') ")
				.append(" ) REF_CONS ")
				.append("	ON COLS.TABLE_NAME = REF_CONS.TABLE_NAME ")
				.append("	AND COLS.COLUMN_NAME = REF_CONS.COLUMN_NAME ")
				.append(" ORDER BY COLS.TABLE_NAME, COLS.COLUMN_ID ");
		} else if(DbTableInfo.MySQL.equals(dbType)||DbTableInfo.MariaDB.equals(dbType)) {	// MySQL, MariaDB
			sql	.append(" SELECT ")
				.append("	COLS.TABLE_NAME, ")
				.append("	COLS.COLUMN_NAME, ")
				.append("	COLS.DATA_TYPE, ")
				.append("	COLS.CHARACTER_MAXIMUM_LENGTH, ")
				.append("	IF(COLS.IS_NULLABLE = 'NO', 'Yes', 'No') AS `NOT NULL`, ")
				.append("	COLS.COLUMN_COMMENT, ")
				.append("	CASE ")
				.append("		WHEN COL_USG.REFERENCED_TABLE_NAME IS NOT NULL THEN 'FK' ")
				.append("		WHEN COLS.COLUMN_KEY = 'PRI' THEN 'PK' ")
				.append("	END AS `COLUMN_KEY`, ")
				.append("	CASE ")
				.append("		WHEN COL_USG.REFERENCED_TABLE_NAME IS NOT NULL ")
				.append("		THEN CONCAT(COL_USG.REFERENCED_TABLE_NAME, '.', COL_USG.REFERENCED_COLUMN_NAME) ")
				.append("	END AS `FK_INFO` ")
				.append(" FROM ")
				.append("	information_schema.COLUMNS COLS ")
				.append(" LEFT JOIN information_schema.KEY_COLUMN_USAGE COL_USG ")
				.append("	ON COLS.TABLE_NAME = COL_USG.TABLE_NAME ")
				.append("	AND COLS.COLUMN_NAME = COL_USG.COLUMN_NAME ")
				.append("	AND COLS.TABLE_SCHEMA = COL_USG.TABLE_SCHEMA ")
				.append("	AND COLS.TABLE_SCHEMA = COL_USG.CONSTRAINT_SCHEMA ")
				.append("	AND COL_USG.REFERENCED_TABLE_NAME IS NOT NULL ")
				.append(" WHERE ")
				.append("	COLS.TABLE_SCHEMA = DATABASE() ")
				.append(" ORDER BY COLS.TABLE_NAME, COLS.ORDINAL_POSITION ");
		} else if(DbTableInfo.PostgreSQL.equals(dbType)) { //PostgreSQL
			
		}
		
		System.out.println(sql.toString());
		
		return sql.toString();
	}
}
