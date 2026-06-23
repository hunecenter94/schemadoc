package dbTableInfo;

/**
 * tableNm: 테이블명<br>
 * tableTy: 테이블타입<br>
 * comments: 설명
 */
public class TableInfo {
	
	public TableInfo(String[] data) {
		setTableNm(data[0]);
		setTableTy(data[1]);
		setComments(data[2]);
	}
	
	/** 테이블 명 */
	private String tableNm;
	
	/** 테이블 타입 */
	private String tableTy;
	
	/** 설명 */
	private String comments;
	
	public String getTableNm() {
		return tableNm;
	}

	public void setTableNm(String tableNm) {
		this.tableNm = tableNm;
	}

	public String getTableTy() {
		return tableTy;
	}

	public void setTableTy(String tableTy) {
		this.tableTy = tableTy;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}
	
	/** 테이블 정보 조회 SQL
	 * <p>VIEW 조회시 isSelectView: true</p>
	 * @return TABLE_NAME, COMMENTS
	 */
	public static String getTableInfoSQL(String dbType, String isSelectView, String dbName) {
		StringBuilder sql = new StringBuilder();
		
		if(DbTableInfo.Oracle.equals(dbType)) {	// ORACLE
			sql	.append(" SELECT ")
				.append("	TABS.TABLE_NAME, ")
				.append("	TABS.TABLE_TYPE, ")
				.append("	CMTS.COMMENTS ")
				.append(" FROM ");
			
			sql	.append(" ( ")
				.append("	SELECT TABLE_NAME, 'TABLE' AS TABLE_TYPE FROM USER_TABLES ");
			
			if("Y".equals(isSelectView)) {
				sql	.append("	UNION ALL ")
					.append("	SELECT VIEW_NAME, 'VIEW' AS TABLE_TYPE FROM USER_VIEWS ");
			}
			
			sql	.append(" ) TABS ");
			
			sql	.append(" LEFT JOIN USER_TAB_COMMENTS CMTS ")
				.append("	ON TABS.TABLE_NAME = CMTS.TABLE_NAME ")
				.append(" ORDER BY TABS.TABLE_NAME ");
		} else if(DbTableInfo.MySQL.equals(dbType) || DbTableInfo.MariaDB.equals(dbType)) {	// MySQL, MariaDB
			sql	.append(" SELECT ")
				.append("	TABLE_NAME, ")
				.append("	CASE WHEN TABLE_TYPE = 'BASE TABLE' THEN 'TABLE' WHEN TABLE_TYPE = 'VIEW' THEN 'VIEW' END AS TABLE_TYPE, ")
				.append("	TABLE_COMMENT ")
				.append(" FROM ")
				.append("	information_schema.TABLES ")
				.append(" WHERE ")
				.append("	TABLE_SCHEMA = DATABASE() ");
			
			if(!"Y".equals(isSelectView)) {
				sql.append(" AND TABLE_TYPE = 'BASE TABLE' ");
			}
			
			sql	.append(" ORDER BY TABLE_NAME ");
		}else if(DbTableInfo.PostgreSQL.equals(dbType)) { //PostgreSQL
			sql	.append(" select ")
				.append(" 	tb.relname as table_name, ")
				.append(" 	tb.table_type, ")
				.append(" 	tb_dc.description ")
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
				.append(" 	left join pg_description tb_dc ")
				.append(" 		on tb_dc.objsubid = 0 and tb.oid = tb_dc.objoid ")
				.append(" order by ")
				.append(" 		tb.relname ");
		} else if(DbTableInfo.MSSQL.equals(dbType)) { // MSSQL
			sql	.append(" SELECT ")
				.append("	T.TABLE_NAME, ")
				.append("	CASE WHEN T.TABLE_TYPE = 'BASE TABLE' THEN 'TABLE' WHEN T.TABLE_TYPE = 'VIEW' THEN 'VIEW' END AS TABLE_TYPE, ")
				.append("	EP.VALUE AS COMMENTS ")
				.append(" FROM ")
				.append("	INFORMATION_SCHEMA.TABLES T ")
				.append(" LEFT JOIN SYS.TABLES ST ")
				.append("	ON T.TABLE_NAME = ST.NAME ")
				.append(" LEFT JOIN SYS.EXTENDED_PROPERTIES EP ")
				.append("	ON EP.MAJOR_ID = ST.OBJECT_ID ")
				.append("	AND EP.MINOR_ID = 0 ")
				.append("	AND EP.NAME = 'MS_Description' ")
				.append(" WHERE ")
				.append("	T.TABLE_CATALOG = '").append(dbName).append("' ");
			
			if(!"Y".equals(isSelectView)) {
				sql.append(" AND T.TABLE_TYPE = 'BASE TABLE' ");
			}
			
			sql	.append(" ORDER BY T.TABLE_NAME ");
		} else if(DbTableInfo.Cubrid.equals(dbType)) { // CUBRID
			sql	.append(" SELECT ")
				.append("	C.CLASS_NAME AS TABLE_NAME, ")
				.append("	CASE WHEN C.CLASS_TYPE = 'CLASS' THEN 'TABLE' WHEN C.CLASS_TYPE = 'VCLASS' THEN 'VIEW' END AS TABLE_TYPE, ")
				.append("	C.COMMENT AS COMMENTS ")
				.append(" FROM ")
				.append("	DB_CLASS C ")
				.append(" WHERE ")
				.append("	C.IS_SYSTEM_CLASS = 'NO' ")
				.append("	AND C.OWNER_NAME = '").append(dbName).append("' ");
			
			if(!"Y".equals(isSelectView)) {
				sql.append(" AND C.CLASS_TYPE = 'CLASS' ");
			}
			
			sql	.append(" ORDER BY C.CLASS_NAME ");
		} else if(DbTableInfo.Tibero.equals(dbType)) { // TIBERO (Oracle 호환)
			sql	.append(" SELECT ")
				.append("	TABS.TABLE_NAME, ")
				.append("	TABS.TABLE_TYPE, ")
				.append("	CMTS.COMMENTS ")
				.append(" FROM ");
			
			sql	.append(" ( ")
				.append("	SELECT TABLE_NAME, 'TABLE' AS TABLE_TYPE FROM USER_TABLES ");
			
			if("Y".equals(isSelectView)) {
				sql	.append("	UNION ALL ")
					.append("	SELECT VIEW_NAME, 'VIEW' AS TABLE_TYPE FROM USER_VIEWS ");
			}
			
			sql	.append(" ) TABS ");
			
			sql	.append(" LEFT JOIN USER_TAB_COMMENTS CMTS ")
				.append("	ON TABS.TABLE_NAME = CMTS.TABLE_NAME ")
				.append(" ORDER BY TABS.TABLE_NAME ");
		}
		
		System.out.println(sql.toString());
		
		return sql.toString();
	}
	
}
