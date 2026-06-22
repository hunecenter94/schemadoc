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
	public static String getTableInfoSQL(String dbType, String includeView, String dbName) {
		StringBuilder sql = new StringBuilder();
		
		if(DbTableInfo.Oracle.equals(dbType)) {	// ORACLE
			sql	.append(" SELECT ")
				.append("	TABS.TABLE_NAME, ")
				.append("	TABS.TABLE_TYPE, ")
				.append("	CMTS.COMMENTS ")
				.append(" FROM ");
			
			sql	.append(" ( ")
				.append("	SELECT TABLE_NAME, 'TABLE' AS TABLE_TYPE FROM USER_TABLES ");
			
			if("Y".equals(includeView)) {
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
			
			if(!"Y".equals(includeView)) {
				sql.append(" AND TABLE_TYPE = 'BASE TABLE' ");
			}
			
			sql	.append(" ORDER BY TABLE_NAME ");
		}else if(DbTableInfo.PostgreSQL.equals(dbType)) { //PostgreSQL
			sql	.append(" select ")
				.append("     tb.relname as table_name, ")
				.append("     tb.table_type, ")
				.append("     tb_dc.description ")
				.append(" from ")
				.append("     ( ")
				.append("         select ")
				.append("             pc.relname, ")
				.append("             pc.oid, ")
				.append("             case ")
				.append("                 when tb.table_type = 'BASE TABLE' then 'TABLE' ")
				.append("                 when tb.table_type = 'VIEW' then 'VIEW' ")
				.append("             end as table_type ")
				.append("         from ")
				.append("             pg_catalog.pg_class pc ")
				.append("             join information_schema."tables" tb ")
				.append("                 on tb.table_schema = '").append(dbName).append("' ")
				.append("                 and pc.relname = tb.table_name ")
				.append("     ) tb ")
				.append("     left join pg_description tb_dc ")
				.append("         on tb_dc.objsubid = 0 and tb.oid = tb_dc.objoid ");
			
			if(!"Y".equals(includeView)) {
				sql	.append(" where ")
					.append("     tb.table_type = 'BASE TABLE' ");
			}
			
			sql	.append(" order by ")
				.append("     tb.relname ");
		}
		
		System.out.println(sql.toString());
		
		return sql.toString();
	}
	
}
