package com.soapui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class OrgFlowName implements Runnable{

	private Thread t;
//	private String threadName;
	
	private  static InputStream inXlsx;
	
	private  static XSSFWorkbook workbook;
	private String filepath;
	private String destpath;
	
	OrgFlowName(String filepath, String destpath) throws Exception{
		if(inXlsx ==null)
			inXlsx = new FileInputStream(Config.getInstance().getProperty("InputXlsx"));
		if(workbook == null)
			workbook = new XSSFWorkbook(inXlsx);
		this.filepath = filepath;
		this.destpath = destpath;
		System.out.println("New Thread Created-- "+filepath);
	//	t = new Thread(this);
	//	t.start();
	}
	public void run(){
		
		Iterator < Row >  rowIterator;
		XSSFRow row;
		String xl_mapname, flowInputName, xl_flowname, uniqueId, splitId;
		DataFormatter formatter = new DataFormatter();
		
		File folder = new File(filepath);
		File moveToFolder, tempFile=null;
		StringTokenizer st_param;
		
		XSSFSheet partnerSheet = workbook.getSheet(Config.getInstance().getProperty("InXlsxSheetFlowNames"));
		System.out.println("Run Thread -- "+filepath);
		rowIterator = partnerSheet.iterator();
		row = (XSSFRow) rowIterator.next();
		while (rowIterator.hasNext()) {
			row = (XSSFRow) rowIterator.next();
			xl_flowname = formatter.formatCellValue(row.getCell(0));
	        xl_mapname = formatter.formatCellValue(row.getCell(1));
	        xl_mapname = StringUtils.remove(xl_mapname,".x4");
	        Collection<File> files = FileUtils.listFiles(folder, new WildcardFileFilter("*"+xl_flowname+"*"), null);
		
			Iterator<File> it = files.iterator();
	        while(it.hasNext()){
	        	
	           File flowInput = it.next();
	           flowInputName = flowInput.getName();
	           
				
	           st_param = new StringTokenizer(flowInputName,"-");
	           uniqueId = st_param.nextToken();
	           splitId = st_param.nextToken();
	           Collection<File> files1 = FileUtils.listFiles(folder, new WildcardFileFilter(uniqueId+"*"), null);
	           Iterator<File> it1 = files1.iterator();
	           try{
		           while(it1.hasNext()){
		        	   tempFile = it1.next();
		        	   if (tempFile.getName().matches("[0-9]{8}-0000-0-[0-9]{14}-?\\{?.*\\}?.TXT")){
	        			   row.createCell(2).setCellValue("X");
	        			   
	        			   moveToFolder = new File(destpath+"\\"+xl_flowname+"\\Input");
							if(!moveToFolder.exists())
								moveToFolder.mkdir();
							FileUtils.copyFileToDirectory(tempFile, moveToFolder, false );
						
		        		   
		        	   }
		        	   if(tempFile.getName().contains("-Final")){
		        		   moveToFolder = new File(destpath+"\\"+xl_flowname+"\\Output");
							if(!moveToFolder.exists())
								moveToFolder.mkdir();
							FileUtils.copyFileToDirectory(tempFile, moveToFolder, false );
		        	   }
		           }
	           }catch(Exception e){
	        	   e.printStackTrace();
	        	   System.out.println("Exception FileName --"+tempFile);
	           }
	        }
		}
		try{
		OutputStream outXlsx = new FileOutputStream(Config.getInstance().getProperty("InputXlsx"));
		workbook.write(outXlsx);
		System.out.println("Thread Ending -- "+filepath);
		outXlsx.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
