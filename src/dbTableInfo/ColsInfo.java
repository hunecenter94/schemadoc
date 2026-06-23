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
	public static String getColsInfoSQL(String dbType, String isSelectView, String dbName) {
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
			sql	.append(" select ")
				.append(" 	tb.relname as table_name, ")
				.append(" 	col_type.column_name, ")
				.append(" 	col_type.udt_name as data_type, ")
				.append(" 	col_type.character_maximum_length, ")
				.append(" 	case ")
				.append(" 		when col_type.is_nullable = 'NO' then 'Yes' ")
				.append(" 		when col_type.is_nullable = 'YES' then 'No' ")
				.append(" 	end as not_null, ")
				.append(" 	col_dc.description, ")
				.append(" 	case ")
				.append(" 		when const_type.constraint_type = 'PRIMARY KEY' then 'PK' ")
				.append(" 		when const_type.constraint_type = 'FOREIGN KEY' then 'FK' ")
				.append(" 	end as column_key, ")
				.append(" 	case when const_type.constraint_type = 'FOREIGN KEY' then concat(const_type.parent_table, '.', const_type.parent_column) end as fk_info ")
				.append(" from ")
				.append(" 	( ")
				.append(" 		select ")
				.append(" 			pc.relname, ")
				.append(" 			pc.oid, ")
				.append(" 			case ")
				.append(" 				when tb.table_type = 'BASE TABLE' then 'TABLE' ")
				.append(" 				when tb.table_type = 'VIEW' then 'VIEW' ")
				.append(" 			end as table_type ")
				.append(" 		from ")
				.append(" 			pg_catalog.pg_class pc ")
				.append(" 			join information_schema.tables tb ")
				.append(" 				on tb.table_schema = '").append(dbName).append("' ")
				.append(" 				and pc.relname = tb.table_name ");
			
			if(!"Y".equals(isSelectView)) {
				sql.append(" 			and tb.table_type = 'BASE TABLE' ");
			}

			sql	.append(" 	) tb ")
				.append(" 	left join pg_attribute col ")
				.append(" 		on tb.oid = col.attrelid ")
				.append(" 	left join pg_description col_dc ")
				.append(" 		on col_dc.objsubid <> 0 ")
				.append(" 		and tb.oid = col_dc.objoid ")
				.append(" 		and col_dc.objoid = col.attrelid ")
				.append(" 		and col_dc.objsubid = col.attnum ")
				.append(" 	left join information_schema.columns col_type ")
				.append(" 		on col_type.table_schema = '").append(dbName).append("' ")
				.append(" 		and col_type.table_name = tb.relname ")
				.append(" 		and col_type.column_name = col.attname ")
				.append(" 		and col_type.ordinal_position = col.attnum ")
				.append(" 	left join ( ")
				.append(" 		select ")
				.append(" 			tc.constraint_type, ")
				.append(" 			tc.table_name as child_table, ")
				.append(" 			kcu.column_name as child_column, ")
				.append(" 			ccu.table_name as parent_table, ")
				.append(" 			ccu.column_name as parent_column ")
				.append(" 		from ")
				.append(" 			information_schema.table_constraints as tc ")
				.append(" 		join information_schema.key_column_usage as kcu ")
				.append(" 			on tc.constraint_name = kcu.constraint_name ")
				.append(" 			and tc.table_schema = kcu.table_schema ")
				.append(" 		join information_schema.constraint_column_usage as ccu ")
				.append(" 			on ccu.constraint_name = tc.constraint_name ")
				.append(" 			and ccu.table_schema = tc.table_schema ")
				.append(" 		where ")
				.append(" 			tc.constraint_type = 'PRIMARY KEY' or tc.constraint_type = 'FOREIGN KEY' ")
				.append(" 	) const_type ")
				.append(" 		on const_type.child_table = tb.relname ")
				.append(" 		and const_type.child_column = col_type.column_name ")
				.append(" where ")
				.append(" 	col.attstattarget = '-1' ")
				.append(" order by ")
				.append(" 	tb.relname, col.attnum ");
		}
		
		System.out.println(sql.toString());
		
		return sql.toString();
	}
}
