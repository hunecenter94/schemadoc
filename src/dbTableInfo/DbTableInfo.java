package dbTableInfo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * DB 명세서 Excel 파일 출력 소스 <br>
 * OracleDB / MariaDB
 */
public class DbTableInfo {
	
	/* DB 정보 */
	public static final String MariaDB = "MariaDB";
	public static final String Oracle = "Oracle";
	public static final String MySQL = "MySQL";
	public static final String PostgreSQL = "PostgreSQL";
	
	/** 엑셀 파일 시트  작성 제외 테이블 */
	private static final String[] NON_WRITE_TABLE_ARR = {
		/* 제외할 테이블만 작성
		"TEST_TABLE" 
		*/
	};
	
	/** 엑셀 파일 데이터 길이 표시 데이터 타입 */
	private static final String[] VISBLE_LENTH_DATA_TYPE_ARR = {
		//oracle
		"NVARCHAR2",
		"CHAR",
		"VARCHAR2",
		//mysql, maraiDB
		"varchar"
	};
	
	/* COL_* 수정 금지 */
	private static final Integer COL_A = 0;
	private static final Integer COL_B = 1;
	private static final Integer COL_C = 2;
	private static final Integer COL_D = 3;
	private static final Integer COL_E = 4;
	private static final Integer COL_F = 5;
	private static final Integer COL_G = 6;
	private static final Integer COL_H = 7;
	private static final Integer COL_I = 8;
	/* COL_* 수정 금지 */
	
	public static void excelDown(String dbType, String host, String port, String id, String pwd, String dbName, String oracleType, String viewSelect, File excelFile) { 
		
		Connection conn = getConnection(dbType, host, port, id, pwd, dbName, oracleType);
		if(conn == null) {
			return;
		}
		
		List<TableInfo> tableInfoList = getTableInfo(conn, dbType, viewSelect);
		List<ColsInfo> colsInfoList = getColsInfo(conn, dbType);
		List<IdxInfo> idxInfoList = getIdxInfo(conn, dbType);
		Map<String, List<ColsInfo>> colsMap = new HashMap<>();
		Map<String, List<IdxInfo>> idxMap = new HashMap<>();

		Set<String> nonWriteTableSet = new HashSet<>();
		if(NON_WRITE_TABLE_ARR.length > 0) {
			for(String tableNm : NON_WRITE_TABLE_ARR) {
				nonWriteTableSet.add(tableNm);
			}
		}
		
		Set<String> visbleLengthdataTypeSet = new HashSet<>();
		if(VISBLE_LENTH_DATA_TYPE_ARR.length > 0) {
			for(String dataType : VISBLE_LENTH_DATA_TYPE_ARR) {
				visbleLengthdataTypeSet.add(dataType);
			}
		}
		
		for(TableInfo tableInfo : tableInfoList) {
			colsMap.put(tableInfo.getTableNm(), new ArrayList<ColsInfo>());
			idxMap.put(tableInfo.getTableNm(), new ArrayList<IdxInfo>());
		}
		
		for(ColsInfo colsInfo : colsInfoList) {
			if(colsMap.containsKey(colsInfo.getTableNm())) {
				colsMap.get(colsInfo.getTableNm()).add(colsInfo);
			}
		}
		
		for(IdxInfo idxInfo : idxInfoList) {
			if(idxMap.containsKey(idxInfo.getTableNm())) {
				idxMap.get(idxInfo.getTableNm()).add(idxInfo);
			}
		}
		
		List<ColsInfo> tmpColsInfoList = null;
		List<IdxInfo> tmpIdxInfoList = null;
		
		int rowNum = 0;
		int cnt = 1;

		try {
			System.out.println("Excel Making...");
			FileOutputStream fos = new FileOutputStream(excelFile);

			XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet sheet = null;
			XSSFRow row = null;
			XSSFCell cell = null;
			CellRangeAddress merge = null;
			CellStyle headerStyle = workbook.createCellStyle();
			headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());	// 셀 배경색
			headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);					// 셀 배경색 채우기
			headerStyle.setBorderTop(BorderStyle.THIN);										// 테두리(상)
			headerStyle.setBorderBottom(BorderStyle.THIN);									// 테두리(하)
			headerStyle.setBorderLeft(BorderStyle.THIN);									// 테두리(좌)
			headerStyle.setBorderRight(BorderStyle.THIN);									// 테두리(우)
			headerStyle.setAlignment(HorizontalAlignment.CENTER);							// 가로 가운데 정렬
			headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);						// 세로 가운데 정렬
			
			XSSFFont headerFont = workbook.createFont();
			headerFont.setFontHeightInPoints((short) 12);	// 글자 크기
			headerFont.setFontName("맑은 고딕");				// 글꼴
			headerFont.setBold(true);						// 진하게
			headerStyle.setFont(headerFont);
			
			CellStyle dataStyle = workbook.createCellStyle();
			dataStyle.setBorderTop(BorderStyle.THIN);					// 테두리(상)
			dataStyle.setBorderBottom(BorderStyle.THIN);				// 테두리(하)
			dataStyle.setBorderLeft(BorderStyle.THIN);					// 테두리(좌)
			dataStyle.setBorderRight(BorderStyle.THIN);					// 테두리(우)
			dataStyle.setAlignment(HorizontalAlignment.CENTER);			// 가로 가운데 정렬
			dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);	// 세로 가운데 정렬
			
			XSSFFont dataFont = workbook.createFont();
			dataFont.setFontHeightInPoints((short) 11);	// 글자 크기
			dataFont.setFontName("맑은 고딕");				// 글꼴
			dataFont.setBold(false);					// 진하게
			dataStyle.setFont(dataFont);
			
			sheet = workbook.createSheet("DBINFO");
			sheet.setColumnWidth(COL_A, getColumnPixelWidth(36));
			sheet.setColumnWidth(COL_B, getColumnPixelWidth(165));
			sheet.setColumnWidth(COL_C, getColumnPixelWidth(245));
			
			XSSFRow gubun = sheet.createRow(1);
			gubun.setHeight(getRowHeight(24.75));
			cell = gubun.createCell(COL_B);
			cell.setCellValue("구분");
			cell.setCellStyle(headerStyle);
			cell = gubun.createCell(COL_C);
			cell.setCellValue(dbType);
			cell.setCellStyle(dataStyle);
			
			XSSFRow space = sheet.createRow(2);
			space.setHeight(getRowHeight(24.75));
			cell = space.createCell(COL_B);
			cell.setCellValue("공간명");
			cell.setCellStyle(headerStyle);
			cell = space.createCell(COL_C);
			cell.setCellValue(dbName);
			cell.setCellStyle(dataStyle);
			XSSFRow owner = sheet.createRow(3);
			owner.setHeight(getRowHeight(24.75));
			cell = owner.createCell(1);
			cell.setCellValue("사용자명");
			cell.setCellStyle(headerStyle);
			cell = owner.createCell(2);
			cell.setCellValue(id);
			cell.setCellStyle(dataStyle);
			for(TableInfo tableInfo : tableInfoList) {
				if(nonWriteTableSet.contains(tableInfo.getTableNm())) {
					continue;
				}
				cnt = 1;
				rowNum = 0;
				
				tmpColsInfoList = colsMap.get(tableInfo.getTableNm());
				tmpIdxInfoList = idxMap.get(tableInfo.getTableNm());
				
				sheet = workbook.createSheet(tableInfo.getTableNm());
				sheet.setColumnWidth(COL_A, getColumnPixelWidth(36));
				sheet.setColumnWidth(COL_B, getColumnPixelWidth(49));
				sheet.setColumnWidth(COL_C, getColumnPixelWidth(277));
				sheet.setColumnWidth(COL_D, getColumnPixelWidth(277));
				sheet.setColumnWidth(COL_E, getColumnPixelWidth(165));
				sheet.setColumnWidth(COL_F, getColumnPixelWidth(125));
				sheet.setColumnWidth(COL_G, getColumnPixelWidth(85));
				sheet.setColumnWidth(COL_H, getColumnPixelWidth(85));
				sheet.setColumnWidth(COL_I, getColumnPixelWidth(293));
				
				// 테이블 정보
				row = sheet.createRow(++rowNum);
				row.setHeight(getRowHeight(24.75));

				cell = row.createCell(COL_B);
				cell.setCellValue("DB명");
				cell.setCellStyle(headerStyle);
				cell = row.createCell(COL_C);
				cell.setCellStyle(headerStyle);
				merge = new CellRangeAddress(rowNum, rowNum, COL_B, COL_C);
				sheet.addMergedRegion(merge);
				
				cell = row.createCell(COL_D);
				cell.setCellValue("테이블 영문명");
				cell.setCellStyle(headerStyle);
				cell = row.createCell(COL_E);
				cell.setCellStyle(headerStyle);
				merge = new CellRangeAddress(rowNum, rowNum, COL_D, COL_E);
				sheet.addMergedRegion(merge);
				
				cell = row.createCell(COL_F);
				cell.setCellValue("테이블 한글명");
				cell.setCellStyle(headerStyle);
				cell = row.createCell(COL_G);
				cell.setCellStyle(headerStyle);
				cell = row.createCell(COL_H);
				cell.setCellStyle(headerStyle);
				merge = new CellRangeAddress(rowNum, rowNum, COL_F, COL_H);
				sheet.addMergedRegion(merge);
				
				cell = row.createCell(COL_I);
				cell.setCellValue("테이블 유형");
				cell.setCellStyle(headerStyle);

				row = sheet.createRow(++rowNum);
				row.setHeight(getRowHeight(24.75));

				cell = row.createCell(COL_B);
				cell.setCellValue(dbName);
				cell.setCellStyle(dataStyle);
				cell = row.createCell(COL_C);
				cell.setCellStyle(dataStyle);
				merge = new CellRangeAddress(rowNum, rowNum, COL_B, COL_C);
				sheet.addMergedRegion(merge);
				
				cell = row.createCell(COL_D);
				cell.setCellValue(tableInfo.getTableNm());
				cell.setCellStyle(dataStyle);
				cell = row.createCell(COL_E);
				cell.setCellStyle(dataStyle);
				merge = new CellRangeAddress(rowNum, rowNum, COL_D, COL_E);
				sheet.addMergedRegion(merge);
				
				cell = row.createCell(COL_F);
				cell.setCellValue(tableInfo.getComments());
				cell.setCellStyle(dataStyle);
				cell = row.createCell(COL_H);
				cell.setCellStyle(dataStyle);
				merge = new CellRangeAddress(rowNum, rowNum, COL_F, COL_H);
				sheet.addMergedRegion(merge);
				
				cell = row.createCell(COL_I);
				cell.setCellValue("일반 테이블");
				cell.setCellStyle(dataStyle);

				row = sheet.createRow(++rowNum);
				row.setHeight(getRowHeight(24.75));

				cell = row.createCell(COL_B);
				cell.setCellValue("테이블 설명");
				cell.setCellStyle(headerStyle);
				cell = row.createCell(COL_C);
				cell.setCellStyle(headerStyle);
				merge = new CellRangeAddress(rowNum, rowNum, COL_B, COL_C);
				sheet.addMergedRegion(merge);
				
				cell = row.createCell(COL_D);
				if("".equals(replaceNull(tableInfo.getComments()))) {
					System.out.println(tableInfo.getTableNm() + ": Table Comments is NULL.");
				}
				cell.setCellValue(tableInfo.getComments());
				cell.setCellStyle(dataStyle);
				cell = row.createCell(COL_E);
				cell.setCellStyle(dataStyle);
				cell = row.createCell(COL_F);
				cell.setCellStyle(dataStyle);
				cell = row.createCell(COL_G);
				cell.setCellStyle(dataStyle);
				cell = row.createCell(COL_H);
				cell.setCellStyle(dataStyle);
				cell = row.createCell(COL_I);
				cell.setCellStyle(dataStyle);
				merge = new CellRangeAddress(rowNum, rowNum, COL_D, COL_I);
				sheet.addMergedRegion(merge);

				// 컬럼 정보
				row = sheet.createRow(++rowNum);
				row.setHeight(getRowHeight(24.75));

				cell = row.createCell(COL_B);
				cell.setCellValue("NO");
				cell.setCellStyle(headerStyle);
				
				cell = row.createCell(COL_C);
				cell.setCellValue("컬럼 영문명");
				cell.setCellStyle(headerStyle);
				
				cell = row.createCell(COL_D);
				cell.setCellValue("컬럼 한글명");
				cell.setCellStyle(headerStyle);
				
				cell = row.createCell(COL_E);
				cell.setCellValue("데이터 타입");
				cell.setCellStyle(headerStyle);
				
				cell = row.createCell(COL_F);
				cell.setCellValue("데이터 길이");
				cell.setCellStyle(headerStyle);
				
				cell = row.createCell(COL_G);
				cell.setCellValue("Not Null");
				cell.setCellStyle(headerStyle);
				
				cell = row.createCell(COL_H);
				cell.setCellValue("KEY");
				cell.setCellStyle(headerStyle);
				
				cell = row.createCell(COL_I);
				cell.setCellValue("FK 정보");
				cell.setCellStyle(headerStyle);
				if(tmpColsInfoList != null && tmpColsInfoList.size() > 0) {
					for(ColsInfo colsInfo : tmpColsInfoList) {
						row = sheet.createRow(++rowNum);
						row.setHeight(getRowHeight(19.5));

						cell = row.createCell(COL_B);
						cell.setCellValue(cnt++);
						cell.setCellStyle(dataStyle);
						
						cell = row.createCell(COL_C);
						cell.setCellValue(colsInfo.getColNm());
						cell.setCellStyle(dataStyle);
						
						cell = row.createCell(COL_D);
						cell.setCellValue(colsInfo.getComments());
						if("".equals(replaceNull(colsInfo.getComments()))) {
							System.out.println(colsInfo.getTableNm() + "." + colsInfo.getColNm() + ": Column Comments is NULL.");
						}
						cell.setCellStyle(dataStyle);
						
						cell = row.createCell(COL_E);
						cell.setCellValue(colsInfo.getDataTy());
						cell.setCellStyle(dataStyle);
						
						cell = row.createCell(COL_F);
						if(visbleLengthdataTypeSet.contains(colsInfo.getDataTy())) {
							cell.setCellValue(Integer.parseInt(colsInfo.getDataLen()));
						}
						cell.setCellStyle(dataStyle);
						
						cell = row.createCell(COL_G);
						cell.setCellValue(colsInfo.getNotNull());
						cell.setCellStyle(dataStyle);
						
						cell = row.createCell(COL_H);
						cell.setCellValue(colsInfo.getKey());
						cell.setCellStyle(dataStyle);
						
						cell = row.createCell(COL_I);
						cell.setCellValue(colsInfo.getFkInfo());
						cell.setCellStyle(dataStyle);
					}
				}
				
				// 인덱스 정보
				row = sheet.createRow(++rowNum);
				row.setHeight(getRowHeight(24.75));

				cell = row.createCell(COL_B);
				cell.setCellValue("INDEX");
				cell.setCellStyle(headerStyle);
				cell = row.createCell(COL_C);
				cell.setCellStyle(headerStyle);
				merge = new CellRangeAddress(rowNum, rowNum, COL_B, COL_C);
				sheet.addMergedRegion(merge);
				
				cell = row.createCell(COL_D);
				cell.setCellValue("INDEX KEY");
				cell.setCellStyle(headerStyle);
				cell = row.createCell(COL_E);
				cell.setCellStyle(headerStyle);
				cell = row.createCell(COL_F);
				cell.setCellStyle(headerStyle);
				cell = row.createCell(COL_G);
				cell.setCellStyle(headerStyle);
				cell = row.createCell(COL_H);
				cell.setCellStyle(headerStyle);
				cell = row.createCell(COL_I);
				cell.setCellStyle(headerStyle);
				merge = new CellRangeAddress(rowNum, rowNum, COL_D, COL_I);
				sheet.addMergedRegion(merge);
				
				if(tmpIdxInfoList != null && tmpIdxInfoList.size() > 0) {
					for(IdxInfo idxInfo : tmpIdxInfoList) {
						row = sheet.createRow(++rowNum);
						row.setHeight(getRowHeight(19.5));

						cell = row.createCell(COL_B);
						cell.setCellValue(idxInfo.getIdxNm());
						cell.setCellStyle(dataStyle);
						cell = row.createCell(COL_C);
						cell.setCellStyle(dataStyle);
						merge = new CellRangeAddress(rowNum, rowNum, COL_B, COL_C);
						sheet.addMergedRegion(merge);
						
						cell = row.createCell(COL_D);
						cell.setCellValue(idxInfo.getCols());
						cell.setCellStyle(dataStyle);
						cell = row.createCell(COL_E);
						cell.setCellStyle(dataStyle);
						cell = row.createCell(COL_F);
						cell.setCellStyle(dataStyle);
						cell = row.createCell(COL_G);
						cell.setCellStyle(dataStyle);
						cell = row.createCell(COL_H);
						cell.setCellStyle(dataStyle);
						cell = row.createCell(COL_I);
						cell.setCellStyle(dataStyle);
						merge = new CellRangeAddress(rowNum, rowNum, COL_D, COL_I);
						sheet.addMergedRegion(merge);
					}
				}
			}
			
			workbook.write(fos);
			workbook.close();
			fos.close();
		} catch (Exception e) {
		    e.printStackTrace();
		}catch (Throwable e) { 
		    e.printStackTrace();
		}
		System.out.println("DbTableInfo Finish.");
	}
	
	/** DB 커넥션 연결 */
	private static Connection getConnection(String dbType, String host, String port, String id, String pwd, String dbName, String oracleType) {
		Connection conn = null;
		String dbUrl = null;
		System.out.println(dbType);
		try {
			if(Oracle.equals(dbType)) {
				Class.forName("oracle.jdbc.driver.OracleDriver");
				if("SID".equals(oracleType)) {
					dbUrl = "jdbc:oracle:thin:@"+host+":"+port+":"+dbName;
				}else {
					dbUrl = "jdbc:oracle:thin:@"+host+":"+port+"/"+dbName;
				}
				
			} else if (MySQL.equals(dbType) || MariaDB.equals(dbType)) {
	    	    Class.forName("com.mysql.cj.jdbc.Driver");
				dbUrl = "jdbc:mysql://"+host+":"+port+"/"+dbName;
			}
			
			conn = DriverManager.getConnection(dbUrl, id, pwd);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			System.out.println("DB getConnection Error!!");
		}
		
		return conn;
	}
	
	private static List<TableInfo> getTableInfo(Connection conn, String dbType, String isSelectView) {
		System.out.println("Start getTableInfo");
		String[][] data = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try {
			ps = conn.prepareStatement(TableInfo.getTableInfoSQL(dbType, isSelectView));
			rs = ps.executeQuery();
			
			data = getData(rs, 3, conn);
			
			if(rs != null) {
				try {
					rs.close();
				} catch(Exception e) {
					e.printStackTrace(); System.exit(0);
				}
			}
			
			if(ps != null) {
				try {
					ps.close();
				} catch(Exception e) {
					e.printStackTrace(); System.exit(0);
				}
			}
		} catch(Exception e) {
			e.printStackTrace(); System.exit(0);
		} finally {
			if(rs != null) {
				try {
					rs.close();
				} catch(Exception e) {
					e.printStackTrace(); System.exit(0);
				}
			}
			
			if(ps != null) {
				try {
					ps.close();
				} catch(Exception e) {
					e.printStackTrace(); System.exit(0);
				}
			}
		}
		
		return convertTableInfo(data);
	}
	
	private static List<ColsInfo> getColsInfo(Connection conn, String dbType) {
		System.out.println("Start getColsInfo");
		String[][] data = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try {
			ps = conn.prepareStatement(ColsInfo.getColsInfoSQL(dbType));
			rs = ps.executeQuery();
			
			data = getData(rs, 8, conn);
			
			if(rs != null) {
				try {
					rs.close();
				} catch(Exception e) {
					e.printStackTrace(); System.exit(0);
				}
			}
			
			if(ps != null) {
				try {
					ps.close();
				} catch(Exception e) {
					e.printStackTrace(); System.exit(0);
				}
			}
		} catch(Exception e) {
			e.printStackTrace(); System.exit(0);
		} finally {
			if(rs != null) {
				try {
					rs.close();
				} catch(Exception e) {
					e.printStackTrace(); System.exit(0);
				}
			}
			
			if(ps != null) {
				try {
					ps.close();
				} catch(Exception e) {
					e.printStackTrace(); System.exit(0);
				}
			}
		}
		
		return convertColsInfo(data);
	}
	
	private static List<IdxInfo> getIdxInfo(Connection conn, String dbType) {
		System.out.println("Start getIdxInfo");
		String[][] data = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try {
			ps = conn.prepareStatement(IdxInfo.getIdxInfoSQL(dbType));
			rs = ps.executeQuery();
			
			data = getData(rs, 3, conn);
			
			if(rs != null) {
				try {
					rs.close();
				} catch(Exception e) {
					e.printStackTrace(); System.exit(0);
				}
			}
			
			if(ps != null) {
				try {
					ps.close();
				} catch(Exception e) {
					e.printStackTrace(); System.exit(0);
				}
			}
		} catch(Exception e) {
			e.printStackTrace(); System.exit(0);
		} finally {
			if(rs != null) {
				try {
					rs.close();
				} catch(Exception e) {
					e.printStackTrace(); System.exit(0);
				}
			}
			
			if(ps != null) {
				try {
					ps.close();
				} catch(Exception e) {
					e.printStackTrace(); System.exit(0);
				}
			}
		}
		
		return convertIdxInfo(data);
	}
	
	/** sql 실행 결과 가져오기 */
	private static String[][] getData(ResultSet rs, int iNum, Connection conn) {
		Vector<Vector<String>> vResultSet = new Vector<>();
		String[][] saData = null;
		Vector<String> vData = new Vector<>();
		
		try {
			if(rs != null) {
				while(rs.next()) {
					Vector<String> vRow = new Vector<>(iNum);
					for(int i = 1; i < iNum + 1; i++) {
						vRow.addElement(rs.getString(i));
					}
					vResultSet.addElement(vRow);
				}
			}
			
			int iTemp = 0;
			saData = new String[vResultSet.size()][iNum];
			Enumeration<Vector<String>> eResult = vResultSet.elements();
			
			while(eResult.hasMoreElements()) {
				vData = (Vector<String>) eResult.nextElement();
				for(int i = 0; i < iNum; i++) {
					saData[iTemp][i] = (String) vData.elementAt(i);
				}
				iTemp++;
			}
		} catch(Exception e) {
			e.printStackTrace(); System.exit(0);
		} finally {
			if(rs != null) {
				try {
					rs.close();
				} catch(Exception e) {
					e.printStackTrace(); System.exit(0);
				}
			}
		}
		
		return saData;
	}
	
	private static List<TableInfo> convertTableInfo(String[][] data) {
		System.out.println("Start convertTableInfo");
		List<TableInfo> tableInfoList = null;
		
		if(data.length > 0) {
			tableInfoList = new ArrayList<>();
			TableInfo tableInfo = null;
			for(String[] row : data) {
				tableInfo = new TableInfo(row);
				tableInfoList.add(tableInfo);
			}
		}
		
		return tableInfoList;
	}
	
	private static List<ColsInfo> convertColsInfo(String[][] data) {
		System.out.println("Start convertColsInfo");
		List<ColsInfo> colsInfoList = null;
		
		if(data.length > 0) {
			colsInfoList = new ArrayList<>();
			ColsInfo colsInfo = null;
			for(String[] row : data) {
				colsInfo = new ColsInfo(row);
				colsInfoList.add(colsInfo);
			}
		}
		
		return colsInfoList;
	}
	
	private static List<IdxInfo> convertIdxInfo(String[][] data) {
		System.out.println("Start convertIdxInfo");
		List<IdxInfo> idxInfoList = null;
		
		if(data.length > 0) {
			idxInfoList = new ArrayList<>();
			IdxInfo idxInfo = null;
			for(String[] row : data) {
				idxInfo = new IdxInfo(row);
				idxInfoList.add(idxInfo);
			}
		}
		
		return idxInfoList;
	}
	
	private static short getRowHeight(double height) {
		return (short) (height * 20);
	}
	
	private static Integer getColumnPixelWidth(Integer width) {
		return width * 32;
	}
	
	private static String replaceNull(String str) {
		if(str == null) return "";
		return str.trim();
	}
	
}
