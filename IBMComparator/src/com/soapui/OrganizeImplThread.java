package com.soapui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class OrganizeImplThread implements Runnable{

	private Thread t;
//	private String threadName;
	
	private  static InputStream inXlsx;
	
	private  static XSSFWorkbook workbook;
	private String filepath;
	private String destpath;
	
	OrganizeImplThread(String filepath, String destpath) throws Exception{
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
		String xl_mapname, mapInputName;
		DataFormatter formatter = new DataFormatter();
		
		File folder = new File(filepath);
		File moveToFolder, tempFile=null;
		StringTokenizer st_param;
		
		XSSFSheet partnerSheet = workbook.getSheet(Config.getInstance().getProperty("InXlsxSheetMapName"));
		System.out.println("Run Thread -- "+filepath);
		rowIterator = partnerSheet.iterator();
		row = (XSSFRow) rowIterator.next();
		while (rowIterator.hasNext()) {
			row = (XSSFRow) rowIterator.next();
	        xl_mapname = formatter.formatCellValue(row.getCell(0));
	        xl_mapname = StringUtils.remove(xl_mapname,".x4");
	        Collection<File> files = FileUtils.listFiles(folder, new WildcardFileFilter("*"+xl_mapname+"*"), null);
		
			Iterator<File> it = files.iterator();
	        while(it.hasNext()){
	        	
	           File mapInput = it.next();
	           mapInputName = mapInput.getName();
	           
				
	           st_param = new StringTokenizer(mapInputName,"-");
	           Collection<File> files1 = FileUtils.listFiles(folder, new WildcardFileFilter(st_param.nextToken()+"-"+st_param.nextToken()+"*"), null);
	           Iterator<File> it1 = files1.iterator();
	           try{
		           while(it1.hasNext()){
		        	   tempFile = it1.next();
		        	   if(tempFile.getName().contains(xl_mapname)){
		        		   if(it1.hasNext()){
		        			   row.createCell(1).setCellValue("X");
		        			   
		        			   moveToFolder = new File(destpath+"\\"+xl_mapname+"\\Input");
								if(!moveToFolder.exists())
									moveToFolder.mkdir();
								FileUtils.copyFileToDirectory(mapInput, moveToFolder, false );
								
			        		   File mapOutput = it1.next();
			        		   moveToFolder = new File(destpath+"\\"+xl_mapname+"\\Output");
								if(!moveToFolder.exists())
									moveToFolder.mkdir();
								FileUtils.copyFileToDirectory(mapOutput, moveToFolder, false );
		        		   }
		        		   break;
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
