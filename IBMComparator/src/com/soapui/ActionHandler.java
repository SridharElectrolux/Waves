package com.soapui;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;

import com.jcraft.jsch.*;
import com.jcraft.jsch.ChannelSftp.LsEntry;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.DataFormatter;

public class ActionHandler {

	

	static String resultDir = null;
	// test1 GIT
	static String fileName = null;
	static int passCount = 0;
	static int failureCount = 0;
	static HashMap<String,String> DirMap = new HashMap<String,String>();
//	static ThreadGroup threadGroup = new ThreadGroup("OrgThreadGrp");



//	public String runAllSoapRequest(ArrayList<ResultBean> beanList, String url, String filePath, String authentication)
	public String runAllTestCases(ArrayList<ResultBean> beanList,String filePath, ChannelSftp sftpChannel,boolean simulate)
			throws Exception {
		String outputString = null, data=null, key;
		
		passCount = 0;
		failureCount = 0;
		try {
			Calendar cal = Calendar.getInstance();
			//SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yy HH.mm.ss");
			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddHHmmss");
			SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyyHHmmss");
			fileName = FilenameUtils.getBaseName(filePath);
			resultDir = Constants.RESULTDIR + fileName + "_" + sdf.format(cal.getTime());

			String expectedResultPath = resultDir + Constants.EXPECTEDREQRESPATH;
			String actualResultPath = resultDir + Constants.ACTUALREQRESPATH;
			String comparisonResultPath = resultDir + Constants.COMPARISONRESULTPATH;

			unzipFile(filePath, expectedResultPath);

			File folder = new File(expectedResultPath);
			File[] listOfFiles = folder.listFiles();
			Arrays.sort(listOfFiles);
			String reqFileName = null;
			String flowName, xl_flowname,xl_ignoreSeg, xl_compareMethod, xl_headStart,xl_fileNamePattern,xl_multiappend="",xl_ignoreField,xl_changeName="";
			int xl_timer=0;
			InputStream inXlsx;
			DataFormatter formatter = new DataFormatter();
			XSSFWorkbook workbook;
			Iterator < Row >  rowIterator;
			XSSFRow row;
			
			inXlsx = new FileInputStream(Config.getInstance().getProperty("InputXlsx"));
			workbook = new XSSFWorkbook(inXlsx);
			XSSFSheet partnerSheet = workbook.getSheet(Config.getInstance().getProperty("InXlsxSheetFlowNames"));
			HashMap<String,String> inSentList= new HashMap<String,String>();
			
			for(int j=0;j < listOfFiles.length;j++){
				File flowFolder = listOfFiles[j];
				File[] ioFiles = flowFolder.listFiles();
				int noOfFiles = ioFiles.length;
				for (int i = 0; i < noOfFiles; i++) {
				//	String timeTaken = "";
					File responseFile = null;
					String request = "";
					ArrayList<File> inFileList = new ArrayList<File>();
					ResultBean bean = new ResultBean();
					File path = ioFiles[i];
	
					reqFileName = path.getName();
					String ext = FilenameUtils.getExtension(reqFileName);
					if (!"TXT".equalsIgnoreCase(ext)) {
						continue;
					}
					if (reqFileName.matches("[0-9]{8}-0000-0-[0-9]{14}-?\\{?.*\\}?.TXT")){
						System.out.println("Input File -- "+reqFileName);
						if(inSentList.containsKey(reqFileName))
							continue;
						
						request = FileUtils.readFileToString(path,StandardCharsets.UTF_8);
						String temp = path.getName().substring(0,path.getName().indexOf("-"));
						Collection<File> files = FileUtils.listFiles(flowFolder, new WildcardFileFilter(temp+"*"), null);
						if(files.size()==1)
							continue;
						inSentList.put(reqFileName, "");
						inFileList.add(path);
						Iterator<File> it = files.iterator();
				        while(it.hasNext()){
				           File v = it.next();
				          if( v.getName().indexOf("-Final")!=-1 && v.getName().indexOf("-SCBN-Final")==-1){
				        	  responseFile = v;
				        	  key =  bean.getKey(responseFile.getName());
				        	  rowIterator = partnerSheet.iterator();
				        	  row = (XSSFRow) rowIterator.next();
				        	  flowName = key.substring(key.indexOf("-(")+2, key.indexOf(")"));
				        	  while (rowIterator.hasNext()) {
				      			row = (XSSFRow) rowIterator.next();
				      			xl_flowname = formatter.formatCellValue(row.getCell(0));
				      			xl_ignoreSeg = formatter.formatCellValue(row.getCell(3));
				      			xl_compareMethod = formatter.formatCellValue(row.getCell(4));
				      			xl_headStart = formatter.formatCellValue(row.getCell(5));
				      			xl_fileNamePattern = formatter.formatCellValue(row.getCell(6));
				      			xl_multiappend = formatter.formatCellValue(row.getCell(7));
				      			xl_timer= Integer.parseInt(formatter.formatCellValue(row.getCell(8))==""?"0":formatter.formatCellValue(row.getCell(8)));
				      			xl_ignoreField = formatter.formatCellValue(row.getCell(9));
				      			xl_changeName = formatter.formatCellValue(row.getCell(10));
				      			if(xl_compareMethod.equals(""))
				      				xl_compareMethod = "BCOM";
				      			if(flowName.equals(xl_flowname)){
				      				data = FileUtils.readFileToString(responseFile,StandardCharsets.UTF_8);
						        	bean.addExpOutput(responseFile);					        	
						        	bean.addIgnoreSeg(key, xl_ignoreSeg);
						        	bean.setComMethod(key, xl_compareMethod);
						        	bean.setFileNamePattern(key, xl_fileNamePattern);
						        	bean.setEdiFlag(key, false);
						        	bean.addHeadStart(key,xl_headStart);
						        	bean.addIgnoreField(key, xl_ignoreField);
						        	bean.setChangeNameFlag(key,xl_changeName.equals("Yes")?true:false);
						        	bean.setMultiappendFlag(key,xl_multiappend.equals("Yes")?true:false);
						        	bean.setWaitTimer(key,xl_timer);
						        	if(data.startsWith("UNA")||data.startsWith("UNB")){
						        		data = data.replaceAll("\\r\\n", ""); //Remove line feed for FOLD80 scenario. For other scenarios where line feed is there are removed
						        		data = data.replaceAll("'(?!\\n)", "'\n");
							        	bean.setEdiFlag(key, true);
							        	FileUtils.writeStringToFile(responseFile, data);
									}
				      				break;
				      			}
				      				
				        	  }
				        	  
				          }
				        }
					}else{
						continue;
					}
					
					try {
						if(!simulate){
							Date inFileDate = sdf1.parse(StringUtils.substring(reqFileName, StringUtils.ordinalIndexOf(reqFileName, "-", 3)+1, StringUtils.ordinalIndexOf(reqFileName, "-", 4)));
					//		Date inFileDate = sdf1.parse(StringUtils.substringBefore(StringUtils.substringAfterLast(reqFileName, "-"),"."));
							Date toFileDate = DateUtils.addSeconds(inFileDate, xl_timer);
							boolean multiFlag = false;
							if(xl_multiappend.equals("Yes")){
								multiFlag = true;
								for (int k = i+1; k < noOfFiles; k++) {
									File nextFile = ioFiles[k];
									String nextFileName = nextFile.getName();
									if (nextFileName.matches("[0-9]{8}-0000-0-[0-9]{14}-?\\{?.*\\}?.TXT")){
										Date nextFileDate = sdf1.parse(StringUtils.substring(nextFileName, StringUtils.ordinalIndexOf(nextFileName, "-", 3)+1, StringUtils.ordinalIndexOf(nextFileName, "-", 4)));
										if(nextFileDate.before(toFileDate)){
											inFileList.add(nextFile);
											inSentList.put(nextFileName, "");
										}
									}
								}
							}
							getOutputFile(sftpChannel,inFileList,xl_timer,multiFlag,bean);
						 // getOutputFile(sftpChannel, request, reqFileName,bean);
						}
						else {
							String temp = path.getName().substring(0,path.getName().indexOf("-"));
							Collection<File> files = FileUtils.listFiles(flowFolder, new WildcardFileFilter(temp+"*"), null);
							
							Iterator<File> it = files.iterator();
					        while(it.hasNext()){
					           File v = it.next();
					          if( v.getName().indexOf("-SCBN")!=-1){
					        	  responseFile = v;
					        	  bean.addActOutput(responseFile);
					        	  data = FileUtils.readFileToString(responseFile,StandardCharsets.UTF_8);
					        	  if(data.startsWith("UNA")||data.startsWith("UNB")){
					        		  	data = data.replaceAll("\\r\\n", ""); //Remove line feed for FOLD80 scenario. For other scenarios where line feed is there are removed
							        	data = data.replaceAll("'(?!\\n)", "'\n");
							        	FileUtils.writeStringToFile(responseFile, data);
					        	  }
					          }
					        }
						}
				     
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					HashMap<String,File> actOut = bean.getActOutput();
					HashMap<String, File> expOut = bean.getExpOutput();
					
					for(Map.Entry<String, File> entry: expOut.entrySet()){
						key = entry.getKey();
						File expFile = entry.getValue();
						File actFile = actOut.get(key);
						String method = bean.getComMethod(key);
						flowName = key.substring(key.indexOf("-(")+2, key.indexOf(")"));
						boolean actOutFlag = false;
						if(method.equals("EDICOM")){
							for (Map.Entry<String, File> actEntry: actOut.entrySet()){ //Search in Actual Output if the flow has atleast one Actual Output
								String actKey = actEntry.getKey();
								if(actKey.contains(flowName)){
									actOutFlag = true;
									break;
								}
							}
							if(actOutFlag)
								EDICompareWithMxn(expFile,actOut,comparisonResultPath,bean,key);
							else
								bean.addResultStatusEDI(key, "Failed");
						//	EDICompare(expFile, actFile, comparisonResultPath,bean,key);	
							if (actFile!=null)
								BcompareFile(expFile,actFile,comparisonResultPath,bean,key);
							else
								bean.addResultStatus(key, "Failed");
						}
						else
							if (actFile!=null){	
								if(method.equals("FLATCOM")){
									compareFlatFile(expFile, actFile, comparisonResultPath, bean, key);
									BcompareFile(expFile, actFile, comparisonResultPath, bean, key);
								}
								else if(method.equals("BCOM"))
									BcompareFile(expFile,actFile,comparisonResultPath,bean,key);
							}
							else{
								if(method.equals("FLATCOM")){
									bean.addResultStatus(key, "Failed");
									bean.addResultStatusEDI(key, "Failed");
								}
								else if(method.equals("BCOM"))
									bean.addResultStatus(key, "Failed");
							}
					}
					for(Map.Entry<String, File> entry: expOut.entrySet()){
						key = entry.getKey();
						String method = bean.getComMethod(key);
						if(method.equals("EDICOM")||method.equals("FLATCOM")){
							if(bean.getResultStatusEDI(key).equals("Failed"))
								failureCount++;
							else
								passCount++;
						}
						else if(method.equals("BCOM")||method.equals("FLATCOM")){
							if(bean.getResultStatus(key).equals("Failed"))
								failureCount++;
							else
								passCount++;
						}
					}
					bean.setRequestFileName(reqFileName);
					bean.setRequestFile(path);
	
					bean.setRequest(request);
					beanList.add(bean);
				//	bean.setConsolidatedFilePath(createConsolidatedFile(bean, url, "SFTP Connection"));
				}
			}

			HashMap<String,int[]> reportMap = createReportMap(beanList);
			createXSLFile(beanList,reportMap);
			createHTMLFile(beanList,reportMap);
			workbook.close();
			inXlsx.close();
		} catch (Exception e) {
			outputString = "Error" + e.getMessage();
			e.printStackTrace();
			return outputString;
		}
		outputString = "Success#" + resultDir;
		
		return outputString;
	}
	
	public static HashMap<String,int[]> createReportMap(ArrayList<ResultBean> resultBeanList) throws Exception {
		
		HashMap<String,int[]> reportMap = new HashMap<String,int[]>();
		String method,status="";
		
		for (ResultBean resultBean : resultBeanList) {
			HashMap<String,File> expOutMap = resultBean.getExpOutput();

			HashMap<String,Boolean> inputCountFlag = new HashMap<String,Boolean>();
			for(Map.Entry<String, File> entry : expOutMap.entrySet()){
				String key = entry.getKey();
				method = resultBean.getComMethod(key);
				String flowName = key.substring(key.indexOf("-(")+2, key.indexOf(")"));
				int[] value = {0,0,0,0};
				if(!inputCountFlag.containsKey(flowName))
					inputCountFlag.put(flowName, false);
				if(reportMap.containsKey(flowName)){
					value = reportMap.get(flowName);
					if(!inputCountFlag.get(flowName)){
						value[0]=value[0]+1;
						inputCountFlag.replace(flowName, true);
					}
					value[1]=value[1]+1;
					if(method.equals("EDICOM")||method.equals("FLATCOM")){
						status = resultBean.getResultStatusEDI(key);
					}
					else if(method.equals("BCOM")){
						status = resultBean.getResultStatus(key);
					}
					if("Failed".equalsIgnoreCase(status)){
						value[2]=value[2]+1;
					}
					else{
						value[3]=value[3]+1;
					}
					reportMap.replace(flowName,value);
				}
				else{
					value[0]=1;
					value[1]=1;
					inputCountFlag.replace(flowName, true);
					if(method.equals("EDICOM")||method.equals("FLATCOM")){
						status = resultBean.getResultStatusEDI(key);
					}
					else if(method.equals("BCOM")){
						status = resultBean.getResultStatus(key);
					}
					if("Failed".equalsIgnoreCase(status)){
						value[2]=1;
						value[3]=0;
					}
					else{
						value[2]=0;
						value[3]=1;
					}
					reportMap.put(flowName,value);
				}
			}
			
			}
		return reportMap;
	}

	public static void createConsolidatedHtmlReport(HashMap<String,int[]> reportMap,String summaryFile) throws Exception {
		
		
		StringBuffer htmlBuffer = new StringBuffer();

		
		htmlBuffer.append(
				"<script type=text/javascript	src=file://C:/soaptester/config/js/jquery-latest.js></script><script type=text/javascript src=file://C:/soaptester/config/js/jquery.tablesorter.js></script><script type=text/javascript> $(function() { $(\"#myTable\").tablesorter();});</script> <table id=myTable class=tablesorter border=1 style=\"width: 60%; margin-right: 30px;margin-left: 30px;margin-top: 30px; margin-bottom: 30px;\">");
		htmlBuffer.append("<thead><tr  bgcolor=blue>");
		
		htmlBuffer.append("<th   ><font color=white>");
		htmlBuffer.append("&nbspFlow Name&nbsp");
		htmlBuffer.append("</font></th>");
		htmlBuffer.append("<th   ><font color=white>");
		htmlBuffer.append("&nbspNo Of Inputs&nbsp");
		htmlBuffer.append("</font></th>");
		htmlBuffer.append("<th   ><font color=white>");
		htmlBuffer.append("&nbspXIB Outputs&nbsp");
		htmlBuffer.append("</font></th>");
		htmlBuffer.append("<th   ><font color=white>");
		htmlBuffer.append("&nbspNo Of Failed&nbsp");
		htmlBuffer.append("</font></th>");
	/*	htmlBuffer.append("<th   ><font color=white>");
		htmlBuffer.append("&nbspCompare Method&nbsp");
		htmlBuffer.append("</font></th>");*/
		htmlBuffer.append("<th   ><font color=white>");
		htmlBuffer.append("No Of Passed");
		htmlBuffer.append("</font></th>");

		htmlBuffer.append("</tr></thead><tbody bgcolor =White>");
		
		for(Map.Entry<String, int[]> reportEntry : reportMap.entrySet()){
			String key = reportEntry.getKey();
			int[] value = reportEntry.getValue();
			htmlBuffer.append("<tr >");
			htmlBuffer.append("<td ><font color=\"black\" size=\"2\">"+key+"</font></td>");
			htmlBuffer.append("<td ><font color=\"black\" size=\"2\">"+value[0]+"</font></td>");
			htmlBuffer.append("<td ><font color=\"black\" size=\"2\">"+value[1]+"</font></td>");
			htmlBuffer.append("<td ><font color=\"black\" size=\"2\">"+value[2]+"</font></td>");
			htmlBuffer.append("<td ><font color=\"black\" size=\"2\">"+value[3]+"</font></td>");
			htmlBuffer.append("</tr>");
		}
		htmlBuffer.append("</tbody></table>");
		if (htmlBuffer.length() != 0) {
			FileUtils.writeStringToFile(new File(resultDir + "/" + summaryFile), htmlBuffer.toString());
		}
	}
	public static void createHTMLFile(ArrayList<ResultBean> resultBeanList,HashMap<String,int[]> reportMap) throws Exception {

		StringBuffer htmlBuffer = new StringBuffer();
		String datetime = StringUtils.substringAfterLast(resultDir, "_");
		String reportFileName = "Report_"+datetime+".html";
		String summaryFile = "Summary_"+datetime+".html";
		String displayMethod="";
		
		createConsolidatedHtmlReport(reportMap,summaryFile);

		htmlBuffer.append("<h1 align=center style=\"font-size:35px;\">Electrolux EDI Project- Test Automation Report</h1><title>Electrolux EDI Project- Test Automation Report</title>");
		htmlBuffer.append("<table  border=1 style=\"width: 70%;margin-top: 50px;\" bgcolor=White align=center >");
		htmlBuffer.append("<tr >");
		htmlBuffer.append("<td >");
		htmlBuffer.append("DateTime ");
		htmlBuffer.append("</td>");
		htmlBuffer.append("<td>");
		htmlBuffer.append(datetime);
		htmlBuffer.append("</td>");
		htmlBuffer.append("</tr>");
		htmlBuffer.append("<tr>");
		htmlBuffer.append("<td >");
		htmlBuffer.append("Total Number of TCs");
		htmlBuffer.append("</td>");
		htmlBuffer.append("<td>");
		htmlBuffer.append(passCount + failureCount);
		htmlBuffer.append("</td>");
		htmlBuffer.append("</tr>");
		htmlBuffer.append("<tr>");
		htmlBuffer.append("<td  >");
		htmlBuffer.append("Total Number of Passed TCs");
		htmlBuffer.append("</td>");
		htmlBuffer.append("<td>");
		htmlBuffer.append(passCount);
		htmlBuffer.append("</td>");
		htmlBuffer.append("</tr>");
		htmlBuffer.append("<tr>");
		htmlBuffer.append("<td >");
		htmlBuffer.append("Total Number of Failed TCs");
		htmlBuffer.append("</td>");
		htmlBuffer.append("<td>");
		htmlBuffer.append(failureCount);
		htmlBuffer.append("</td>");
		htmlBuffer.append("</tr>");
		htmlBuffer.append("<tr>");
		htmlBuffer.append("<td >");
		htmlBuffer.append("<a href=\"./" + summaryFile + "\" target=_blank>Summary Report</a>");
		htmlBuffer.append("");
		htmlBuffer.append("</td>");
		htmlBuffer.append("<td>");
		htmlBuffer.append("");
		htmlBuffer.append("</td>");
		htmlBuffer.append("</tr>");
		htmlBuffer.append("</table>");

		htmlBuffer.append(
				"<script type=text/javascript	src=file://C:/soaptester/config/js/jquery-latest.js></script><script type=text/javascript src=file://C:/soaptester/config/js/jquery.tablesorter.js></script><script type=text/javascript> $(function() { $(\"#myTable\").tablesorter();});</script> <table id=myTable class=tablesorter border=1 style=\"width: 100%; margin-right: 30px;margin-left: 30px;margin-top: 30px; margin-bottom: 30px;\">");
		htmlBuffer.append("<thead><tr  bgcolor=blue>");
		
		htmlBuffer.append("<th   ><font color=white>");
		htmlBuffer.append("&nbspID&nbsp");
		htmlBuffer.append("</font></th>");
		htmlBuffer.append("<th   ><font color=white>");
		htmlBuffer.append("&nbspResult&nbsp");
		htmlBuffer.append("</font></th>");
		htmlBuffer.append("<th   ><font color=white>");
		htmlBuffer.append("&nbspBeyond<br>Compare&nbsp");
		htmlBuffer.append("</font></th>");
		htmlBuffer.append("<th   ><font color=white>");
		htmlBuffer.append("&nbspIgnore-Fields&nbsp");
		htmlBuffer.append("</font></th>");
	/*	htmlBuffer.append("<th   ><font color=white>");
		htmlBuffer.append("&nbspCompare Method&nbsp");
		htmlBuffer.append("</font></th>");*/
		htmlBuffer.append("<th   ><font color=white>");
		htmlBuffer.append("FlowName");
		htmlBuffer.append("</font></th>");
		htmlBuffer.append("<th   ><font color=white>");
		htmlBuffer.append("Input Message");
		htmlBuffer.append("</font></th>");
		htmlBuffer.append("<th   ><font color=white>");
		htmlBuffer.append("XIB Output");
		htmlBuffer.append("</font></th>");
		htmlBuffer.append("<th   ><font color=white>");

		htmlBuffer.append("SCBN Output");
		htmlBuffer.append("</font></th>");

		htmlBuffer.append("</tr></thead><tbody bgcolor =White>");

		String actOutFileName="",expOutFilePath="",actOutFilePath="",method="",ignoreField="",actOutFileParent="";
		String mainResult="",mainStatus="",bStatus="",bResult="";
		int Id=1;
		for (ResultBean resultBean : resultBeanList) {
			File reqFile = resultBean.getRequestFile();
			String requestFileName = reqFile.getName();
			String requestFileParent = reqFile.getParentFile().getName();
			String requestFilePath = "." + Constants.EXPECTEDREQRESPATH
					+ requestFileParent + "/"+requestFileName;
			HashMap<String,File> expOutMap = resultBean.getExpOutput();
			HashMap<String,File> actOutMap = resultBean.getActOutput();
			int expOutCount = expOutMap.size();
			int i=1;
			for(Map.Entry<String, File> entry : expOutMap.entrySet()){
				expOutFilePath="";
				actOutFileName="";
				actOutFileParent="";
				actOutFilePath="";
				mainResult="";
				mainStatus="";
				bStatus="";
				bResult="";
				ignoreField="";
				
				String key = entry.getKey();
				String flowName = key.substring(key.indexOf("-(")+2, key.indexOf(")"));
				String expOutFileName = entry.getValue().getName();
				String expOutFileParent = entry.getValue().getParentFile().getName();
				method = resultBean.getComMethod(key);
				ignoreField = resultBean.getIgnoreField(key);
				expOutFilePath = "." + Constants.EXPECTEDREQRESPATH
						+ expOutFileParent + "/" +expOutFileName;
				if(actOutMap.containsKey(key)){
					actOutFileName = actOutMap.get(key).getName();	
					actOutFileParent = actOutMap.get(key).getParentFile().getName();
					actOutFilePath = "." + Constants.EXPECTEDREQRESPATH
							+ actOutFileParent + "/" +actOutFileName;
				}
				if(method.equals("EDICOM")||method.equals("FLATCOM")){
					mainStatus = resultBean.getResultStatusEDI(key);
					mainResult = resultBean.getResultFilePathEDI(key);
					bStatus = resultBean.getResultStatus(key);
					bResult = resultBean.getResultFilePath(key);
				}
				else if(method.equals("BCOM")){
					mainStatus = resultBean.getResultStatus(key);
					mainResult = resultBean.getResultFilePath(key);
					bStatus = resultBean.getResultStatus(key);
					bResult = resultBean.getResultFilePath(key);
				}
				
				htmlBuffer.append("<tr >");
				htmlBuffer.append("<td ><font color=\"black\" size=\"1\">"+datetime+"-"+Id+"</font></td>");
				htmlBuffer.append("<td   >");
				
				if ("Failed".equalsIgnoreCase(mainStatus)) {
					String resultFilePath = mainResult;
					if (resultFilePath!=null){
						resultFilePath = "." + resultFilePath.substring(resultFilePath.indexOf(Constants.COMPARISONRESULTPATH),resultFilePath.length());
						htmlBuffer.append(
								"<a href=\"" + resultFilePath + "\" target=_blank><font color=\"red\">Failed</font></a>");
					}
					else
						htmlBuffer.append(
								"<font color=\"red\">Failed</font>");

				} else {
					htmlBuffer.append(
							"<font color=\"green\">Passed</font>");
						//	"<a href=\"" + resultFileName + "\" target=_blank><font color=\"green\">Passed</font><a>");
				}
				htmlBuffer.append("</td>");
				htmlBuffer.append("<td   >");
				if (!bStatus.equals("")){
					if ("Failed".equalsIgnoreCase(bStatus)) {
						String resultFilePath = bResult;
						if (resultFilePath!=null){
							resultFilePath = "." + resultFilePath.substring(resultFilePath.indexOf(Constants.COMPARISONRESULTPATH),resultFilePath.length());
							htmlBuffer.append(
									"<a href=\"" + resultFilePath + "\" target=_blank><font color=\"red\">Failed</font></a>");
						}
						else
							htmlBuffer.append(
									"<font color=\"red\">Failed</font>");

					} else {
						htmlBuffer.append(
								"<font color=\"green\">Passed</font>");
					}
				}
				htmlBuffer.append("</td>");
				if(ignoreField.equals(""))
					htmlBuffer.append("<td ><font color=\"black\" size=\"1\"></font></td>");
				else
					htmlBuffer.append("<td ><font color=\"black\" size=\"1\">"+""+ignoreField+"</font></td>");
			/*	if(method.equals("EDICOM"))
					displayMethod = "Custom_EDI";
				else if(method.equals("FLATCOM"))
					displayMethod = "Custom_FlatFile";
				else if(method.equals("BCOM"))
					displayMethod = "BeyoundCompare";
				htmlBuffer.append("<td ><font color=\"black\" size=\"1\">"+displayMethod+"</font></td>");*/
				htmlBuffer.append("<td ><font color=\"black\" size=\"1\">"+flowName+"</font></td>");
				if(i==1){
					htmlBuffer.append("<td  rowspan=\""+expOutCount+"\" >");
					htmlBuffer.append("<a href=\"" + requestFilePath + "\" target=_blank><font color=\"black\" size=\"1\">"
							+ requestFileName + "</font></a>");
					htmlBuffer.append("</td>");
				}

				htmlBuffer.append("<td   >");
				htmlBuffer.append("<a href=\"" + expOutFilePath + "\" target=_blank><font color=\"black\" size=\"1\">"
						+ expOutFileName + "</font></a>");
				htmlBuffer.append("</td>");

				htmlBuffer.append("<td   >");
				if(method.equals("EDICOM")){
					for (Map.Entry<String, File> actEntry: actOutMap.entrySet()){
						String actKey = actEntry.getKey();
						if(actKey.contains(flowName)){
							actOutFileName = actEntry.getValue().getName();					
							actOutFileParent = actOutMap.get(key).getParentFile().getName();
							actOutFilePath = "." + Constants.EXPECTEDREQRESPATH
									+ actOutFileParent + "/" +actOutFileName;
						htmlBuffer.append("<a href=\"" + actOutFilePath + "\" target=_blank><font color=\"black\" size=\"1\">"
								+ actOutFileName + "</font></a><br>");
						}
					}
				}
				else
					htmlBuffer.append("<a href=\"" + actOutFilePath + "\" target=_blank><font color=\"black\" size=\"1\">"
							+ actOutFileName + "</font></a>");
				htmlBuffer.append("</td>");

				htmlBuffer.append("</tr>");
				i++;
				Id++;
			}
			
		}
		htmlBuffer.append("</tbody></table>");

		if (htmlBuffer.length() != 0) {
			FileUtils.writeStringToFile(new File(resultDir + "/" + reportFileName), htmlBuffer.toString());
		}
	}

	public static void createXSLFile(ArrayList<ResultBean> resultBeanList,HashMap<String,int[]> reportMap) throws Exception {


		String datetime = StringUtils.substringAfterLast(resultDir, "_");
		InputStream inXlsx = new FileInputStream(Config.getInstance().getProperty("TemplateReport"));
		XSSFWorkbook workbook = new XSSFWorkbook(inXlsx);
		String mainStatus="",bStatus="";

		try {
			XSSFSheet sheet = workbook.getSheet("Report");
			sheet.setDefaultColumnWidth(15);
			XSSFRow urlRow = sheet.createRow((short) 1);
			urlRow.createCell(0).setCellValue("DateTime");
			urlRow.createCell(1).setCellValue(datetime);
			XSSFRow totalRow = sheet.createRow((short) 2);
			totalRow.createCell(0).setCellValue("Number Of Total TCs");
			totalRow.createCell(1).setCellValue(passCount + failureCount);
			XSSFRow passRow = sheet.createRow((short) 3);
			passRow.createCell(0).setCellValue("Number Of Passed TCs");
			passRow.createCell(1).setCellValue(passCount);
			XSSFRow failRow = sheet.createRow((short) 4);
			failRow.createCell(0).setCellValue("Number Of Failed TCs");
			failRow.createCell(1).setCellValue(failureCount);
			XSSFRow rowhead = sheet.createRow((short) 5);
			int i =0;
			rowhead.createCell(i++).setCellValue("ID");
			rowhead.createCell(i++).setCellValue("Result");
			rowhead.createCell(i++).setCellValue("Beyond Compare");
			rowhead.createCell(i++).setCellValue("Ignore-Fields");
			//rowhead.createCell(i++).setCellValue("Compare Method");
			rowhead.createCell(i++).setCellValue("Flow Name");
			rowhead.createCell(i++).setCellValue("Input Message");
			rowhead.createCell(i++).setCellValue("XIB Output");
			rowhead.createCell(i++).setCellValue("SCBN Output");
			rowhead.createCell(i++).setCellValue("XIB FileName");
			rowhead.createCell(i++).setCellValue("SCBN FileName");
			rowhead.createCell(i++).setCellValue("FileName Pattern");
			rowhead.createCell(i++).setCellValue("SCBN FileName Patten Match");
		
			int count = 4;
			int Id = 1;
			String actOutFilePath,displayMethod="",actOutFileName="";
			for (ResultBean bean : resultBeanList) {
				String requestFileName = bean.getRequestFileName();
				HashMap<String,File> expOutMap = bean.getExpOutput();
				HashMap<String,File> actOutMap = bean.getActOutput();
				for(Map.Entry<String, File> entry : expOutMap.entrySet()){
					mainStatus="";
					bStatus="";
					actOutFileName="";
					String key = entry.getKey();
					String flowName = key.substring(key.indexOf("-(")+2, key.indexOf(")"));
					String expOutFilePath = entry.getValue().getPath().replace("F:\\TestAutomationTool\\result", "\\\\EUWS2523\\result");
					String expOutFileName = StringUtils.substringBetween(expOutFilePath, "{", "}");
					expOutFileName = expOutFileName==null?"":expOutFileName;
					String method = bean.getComMethod(key);
					String ignoreField = bean.getIgnoreField(key);
					String fileNamePattern = bean.getFileNamePattern(key);
				/*	if(method.equals("EDICOM"))
						displayMethod = "Custom_EDI";
					else if(method.equals("FLATCOM"))
						displayMethod = "Custom_FlatFile";
					else if(method.equals("BCOM"))
						displayMethod = "BeyoundCompare";*/
					if(method.equals("EDICOM")|| method.equals("FLATCOM")){
						mainStatus = bean.getResultStatusEDI(key);
						bStatus = bean.getResultStatus(key);
					}
					else if(method.equals("BCOM")){
						mainStatus = bean.getResultStatus(key);
						bStatus = bean.getResultStatus(key);
					}
					if(actOutMap.containsKey(key)){
						actOutFilePath = actOutMap.get(key).getPath().replace("F:\\TestAutomationTool\\result", "\\\\EUWS2523\\result");
						actOutFileName = StringUtils.substringBetween(actOutFilePath, "{", "}");
						actOutFileName = actOutFileName==null?"":actOutFileName;
					}
					else
						actOutFilePath = "";
					
					rowhead = sheet.createRow((short) count + 3);
					rowhead.setHeight((short) 400);
					i=0;
					rowhead.createCell(i++).setCellValue(datetime+"-"+Id);
					rowhead.createCell(i++).setCellValue(mainStatus);
					rowhead.createCell(i++).setCellValue(bStatus);
					if(ignoreField.equals(""))
						rowhead.createCell(i++).setCellValue(ignoreField);
					else
						rowhead.createCell(i++).setCellValue(""+ignoreField);
				//	rowhead.createCell(i++).setCellValue(displayMethod);
					rowhead.createCell(i++).setCellValue(flowName);
					rowhead.createCell(i++).setCellValue(requestFileName);
					rowhead.createCell(i++).setCellValue(expOutFilePath);
					rowhead.createCell(i++).setCellValue(actOutFilePath);
					rowhead.createCell(i++).setCellValue(expOutFileName);
					rowhead.createCell(i++).setCellValue(actOutFileName);
					rowhead.createCell(i++).setCellValue(fileNamePattern);
					count++;
					Id++;
				}
				
			}
			
			XSSFSheet summarySheet = workbook.createSheet("Summary");
			XSSFRow rowhead1 = summarySheet.createRow((short) 0);
			i=0;
			rowhead1.createCell(i++).setCellValue("Flow Name");
			rowhead1.createCell(i++).setCellValue("No Of Inputs ");
			rowhead1.createCell(i++).setCellValue("XIB Outputs");
			rowhead1.createCell(i++).setCellValue("No Of Failed ");
			rowhead1.createCell(i++).setCellValue("No Of Passed");
			int j=1;
			for(Map.Entry<String, int[]> reportEntry : reportMap.entrySet()){
				String key = reportEntry.getKey();
				int[] value = reportEntry.getValue();
				i=0;
				rowhead1 = summarySheet.createRow((short) j);
				rowhead1.createCell(i++).setCellValue(key);
				rowhead1.createCell(i++).setCellValue(value[0]);
				rowhead1.createCell(i++).setCellValue(value[1]);
				rowhead1.createCell(i++).setCellValue(value[2]);
				rowhead1.createCell(i++).setCellValue(value[3]);
				j++;
			}

			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			String reportFileName = fileName+"_"+datetime+".xlsm";
			FileOutputStream fileOut = new FileOutputStream(resultDir + "/" + reportFileName);
			workbook.write(fileOut);
			workbook.close();
			fileOut.close();
			inXlsx.close();
		}
	}

	//public static String getOutputFile(ChannelSftp sftpChannel, String inputData , String inFileName, ResultBean bean){
	public static String getOutputFile(ChannelSftp sftpChannel, ArrayList<File> inFileList,int timer,boolean multiFlag, ResultBean bean){
		
		String outFile = "",key;
		int waitCount = 20,i;
		int inFilesCount = inFileList.size();
		int timer_minute = timer/60;
		int cur_min =0;
		//boolean multiFlag = false;
		Calendar cal = Calendar.getInstance();
		try{
		if(multiFlag){
    		multiFlag = true;
    		int minute = cal.get(Calendar.MINUTE);
    		int sec = cal.get(Calendar.SECOND);
    		int mod = minute%timer_minute;
    		if(mod==0)
    			Thread.sleep((60-sec)*1000);
    		else
	    		if(mod>(timer_minute/2 + 1))
	    			Thread.sleep((timer_minute-mod+1)*60000);
    	}
        for(int j=0;j<inFilesCount;j++){
        	File inFile = inFileList.get(j);
        //	String inputData = FileUtils.readFileToString(inFile,StandardCharsets.UTF_8);
        	sendFileToSftp(sftpChannel,inFile,inFile.getName());
        }
  
		HashMap<String,File> expOutMap = bean.getExpOutput();
		for(Map.Entry<String, File> entry: expOutMap.entrySet()){
			key = entry.getKey();
			cal = Calendar.getInstance();
	        cur_min = cal.get(Calendar.MINUTE);
	        multiFlag = bean.getMultiappendFlag(key);
	        timer = bean.getWaitTimer(key);
	        timer_minute = timer/60;
			if(multiFlag){
	        	int waitTime = timer_minute - (cur_min%timer_minute) + 1 ;
	        	Thread.sleep(waitTime * 60000);
	        }
	        else
	        	Thread.sleep(1000);
			File respFile = getFileFromSftp(sftpChannel,key,entry.getValue().getParent(),multiFlag,bean.getChangeNameFlag(key));
			if(respFile!=null)
				bean.addActOutput(respFile);
			else{
				i=0;
				while(i<waitCount){
					Thread.sleep(2000);
					respFile = getFileFromSftp(sftpChannel,key,entry.getValue().getParent(),multiFlag,bean.getChangeNameFlag(key));
					if(respFile!=null){
						bean.addActOutput(respFile);
						break;
					}
					i++;
				}
			}
			if(respFile!=null){
			String data = FileUtils.readFileToString(respFile,StandardCharsets.UTF_8);
      	    if(data.startsWith("UNA")||data.startsWith("UNB")){
      		  	data = data.replaceAll("\\r\\n", ""); //Remove line feed for FOLD80 scenario. For other scenarios where line feed is there are removed
		        	data = data.replaceAll("'(?!\\n)", "'\n");
		        	FileUtils.writeStringToFile(respFile, data);
      	    }
			}
		}
		
		outFile = outFile + "&&&";
		}catch (Exception e){
			e.printStackTrace();
		}
		return outFile;
	}
	public static void sendFileToSftp(ChannelSftp sftpChannel, File inFile, String inFilename){
		
	  //  ByteArrayOutputStream bout = new ByteArrayOutputStream();

	    try
	    {
	    /*	byte[] buffer = new byte[inputData.length()];
			buffer = inputData.getBytes(StandardCharsets.UTF_8);
			bout.write(buffer);
			byte[] b = bout.toByteArray();*/
			byte[] b = FileUtils.readFileToByteArray(inFile);
	        
	        OutputStream out= null;
	        sftpChannel.cd("/in");
	        out= sftpChannel.put(inFilename);
	        out.write(b);
	        out.close();

	    }
	    catch(SftpException | IOException e)
	    {
	        System.out.println(e);
	        e.printStackTrace();
	    }
	}
	
	public static File getFileFromSftp(ChannelSftp sftpChannel, String key,String path, boolean multiAppendFlag,boolean changeName){
		
		String sftpFileName="";
		String flowName = key.substring(key.indexOf("-(")+2, key.indexOf(")"));
		String uniqueId = key.substring(0,key.indexOf(flowName)-2);
	    Vector<LsEntry> lsList = new Vector<LsEntry>();
	    File f=null;
	    

	    try
	    {
	        InputStream out= null;
	      //  sftpChannel.cd("/outbound");
	        lsList = sftpChannel.ls("/out");
	        Iterator it = lsList.iterator();
	        while(it.hasNext()){
	        	LsEntry file = (LsEntry) it.next();
	        	sftpFileName = file.getFilename();
	        	if(multiAppendFlag||changeName){
	        		if(sftpFileName.contains(flowName)){
		        		//sftpChannel.cd("/outbound");
		        		out= sftpChannel.get("/out/"+sftpFileName);
		        		//String output = IOUtils.toString(out,StandardCharsets.UTF_8);
		        		String newSftpFileName = uniqueId+"-"+sftpFileName.substring(StringUtils.ordinalIndexOf(sftpFileName, "-", 2)+1);
		    	        f = new File(path + "\\"+newSftpFileName);
		    	        FileUtils.copyInputStreamToFile(out, f);
		    	        //FileUtils.writeStringToFile(f, output,StandardCharsets.UTF_8);
		    	        break;
		        	}
	        	}
	        	else
		        	if(sftpFileName.contains(flowName)&& sftpFileName.contains(uniqueId)){
		        		//sftpChannel.cd("/outbound");
		        		out= sftpChannel.get("/out/"+sftpFileName);
		        		//String output = IOUtils.toString(out,StandardCharsets.UTF_8);
		    	        f = new File(path + "\\"+sftpFileName);
		    	        FileUtils.copyInputStreamToFile(out, f);
		    	       // FileUtils.writeStringToFile(f, output,StandardCharsets.UTF_8);
		    	        break;
		        	}
	        }
	        
	    }
	    catch(SftpException | IOException e)
	    {
	        System.out.println(e);
	        e.printStackTrace();
	    }
	    
	    return f;
	}


	public static void compareFlatFile(File expFile, File actFile, String comparisonResultPath,ResultBean bean, String key ){//(String XIBFile, String SCBNFile) {

		try {
			String fileAsString = FileUtils.readFileToString(expFile,StandardCharsets.UTF_8);
			String fileAsString2 = FileUtils.readFileToString(actFile,StandardCharsets.UTF_8);
			
			
			//Split records of String "fileAsString" and save in ArrayList al
			String headStart = bean.getHeadeStart(key);
			String splitString = "\\n(?="+headStart+")";
			String str[] = fileAsString.split(splitString);//("\\n(?=UNX)");
			List<String> al = new ArrayList<String>();
			al = Arrays.asList(str);
			
			
			//Split records of String "fileAsString2" and save in ArrayList al2
			String str1[] = fileAsString2.split(splitString);//("\\n(?=UNX)");
			List<String> al2 = new ArrayList<String>();
			al2 = Arrays.asList(str1);
			
			displayRecords(al,al2,actFile.getPath(),comparisonResultPath,bean,key);
			
		} catch (IOException e) {
			e.printStackTrace();
			}
		 
	}
		
	public static String replaceIgnoreSeg(StringBuffer sb,String ignoreSeg){
		String tag="";
		int strIdx=0,endIdx=0;
		String line=sb.toString();
		String str[]=ignoreSeg.split(";");
		int len = str.length;
		for(int i=0;i<len;i++){
			tag = StringUtils.substringBefore(str[i], ":");
			strIdx = Integer.parseInt(StringUtils.substringBetween(str[i],":", "-"));
			endIdx = Integer.parseInt(StringUtils.substringAfterLast(str[i], "-"));
			int numberOfSpaces=endIdx-strIdx;
			String s="";
			for(int j=0;j<numberOfSpaces;j++){
				s=s+" ";
			}
			if (line.startsWith(tag)){
				line=sb.replace(strIdx, endIdx, s).toString();
			}
		}	
		return line;
	}
			
	public static void displayRecords(List<String> list1,List<String> list2, String actFilePath, String comparisonResultPath,ResultBean bean, String key ){
		try{	
			
			
			ArrayList<String> found= new ArrayList<String>();
			ArrayList<String> notfound= new ArrayList<String>();
			String ignoreSeg = bean.getIgnoreSeg(key);

			for(int i=0;i<list1.size();i++){
					BufferedReader reader1 = new BufferedReader(new StringReader(list1.get(i)));
					String l1 = reader1.readLine();
					String str1= l1;
					
		
					for(int j=0;j<list2.size();j++){
						BufferedReader reader2 = new BufferedReader(new StringReader(list2.get(j)));
						String l2 = reader2.readLine();
						String str2= l2;
						boolean areEqual = true;
					
						while (l1 != null || l2 != null){
							StringBuffer buff=new StringBuffer(l1);
							if(ignoreSeg!="")
								l1=replaceIgnoreSeg(buff,ignoreSeg);
								//l1=buff.replace(strIdx, endIdx, "            ").toString();
							buff=new StringBuffer(l2);
							if(ignoreSeg!="")
								l2=replaceIgnoreSeg(buff,ignoreSeg);
								//l2=buff.replace(strIdx, endIdx, "            ").toString();
							
							if(l1 == null || l2 == null){
				                areEqual = false;
				                break;
				            }
				            if(! l1.equals(l2)){
				                areEqual = false;
				                break;
				            }
				             
				            l1 = reader1.readLine();
				            l2 = reader2.readLine();
				            		             
				           // lineNum++;
					    }
						
						if(areEqual){
							found.add(str1);
							break;
						}
						/*if((str1.equals(str2)) && !areEqual){
							record_data1.add(str1);
							break;
					
						}*/
						
					
						
					}
					if(l1 != null){
						notfound.add(str1);
					}
					l1=reader1.readLine();
			}
	        
			StringBuffer s= new StringBuffer("<html><body>");
			s.append("<table  border=1 style=\"width: 90%;margin-top: 50px;\" align=center >");
			//s.append("<tr><td>Total Blocks in XIB : </td>");
	        //s.append("<td>"+list1.size()+"</td></tr>");
	      if(notfound.size()==0){
				bean.addResultStatusEDI(key, "Passed");
				
			}
			else{
			    bean.addResultStatusEDI(key, "Failed");
			    if(found.size()>=0){
			    s.append("<tr><td>Status</td>");
			    s.append("<td><font color=\"red\">Failed</font></td>");
			    s.append("<tr><td>Total XIB blocks</td>");
		        s.append("<td>"+list1.size()+"</td></tr>");
		        	        
		        
		        	s.append("<tr><td>Found XIB blocks</td>");
				    s.append("<td>"+found.size()+"</td></tr>");
				    //if(notfound.size()>0){
					s.append("<tr><td>Not Found XIB blocks</td>");
					s.append("<td>"+notfound.size()+"</td></tr>");
				    
					s.append("<tr><td>Found XIB Blocks</td>");
					s.append("<td>");//p.print(s.toString());
					//"Found XIB blocks :\n");
					for(int i=0;i<found.size();i++)
					{
						s.append(found.get(i)+"<br>");
						
						//writer.write(record_data.get(i));
				
					}
				}
	   
				s.append("</td></tr>");
				if(notfound.size()>0){
					s.append("<tr><td>Not found XIB blocks</td>");
					s.append("<td>");
					for(int i=0;i<notfound.size();i++)
					{
						s.append(notfound.get(i)+"<br>");
				
					}
				}		
						
			s.append("</td></tr>");
			s.append("</table></body></html>");
			
			// Storing output in HTML file
			String resultFile = comparisonResultPath + FilenameUtils.getBaseName(FilenameUtils.getName(actFilePath)) + "_FLAT.html";
			bean.addResultFilePathEDI(key, resultFile);
			FileUtils.writeStringToFile(new File(bean.getResultFilePathEDI(key)), s.toString());

			}
		}catch(IOException e){
				e.printStackTrace();
			}
		}


	public static String EDICompareWithMxn(File expFile, HashMap<String, File> actOut, String comparisonResultPath,ResultBean bean, String key ) 
			throws Exception{
		
		String[] actTokens,expTokens ;
		String expData, actData="",ignoreSeg,actId="",expId="";
		boolean match = true,unaMatch = true,expUnaFlag;
		int matchCount=0, noMatchCount=0;
		BufferedReader actReader=null,expReaderTemp=null,expReader=null;
		StringBuffer buffer = new StringBuffer();
		StringBuffer noMatchBuffer = new StringBuffer();
		StringBuffer unaNoMatchBuffer = new StringBuffer();
		StringBuffer matchBuffer = new StringBuffer();
		String result = "No Difference", actUnaData, actLine,expUNH=null;
		Pattern p = Pattern.compile("^[A-Z]{3}\\+\\w*");
		Matcher matcher ;
		ignoreSeg = bean.getIgnoreSeg(key);
		
		String flowName = key.substring(key.indexOf("-(")+2, key.indexOf(")"));
		String uniqueId = expFile.getName().substring(0,expFile.getName().indexOf("-"));
		
		expData = FileUtils.readFileToString(expFile,StandardCharsets.UTF_8);
		expTokens = expData.split("\\n(?=UNH)");
		int expUNHCount = expTokens.length -1 ;
		
		for(String t1: expTokens){
			t1 = t1.replaceAll("\\nUNZ.*\\n", "");
			expReader = new BufferedReader(new StringReader(t1));
			expReaderTemp = expReader;
			String expLine = expReaderTemp.readLine();
			int expLineCount = StringUtils.countMatches(t1, "\n")+1;
			if(expLine.startsWith("UNA") || expLine.startsWith("UNB"))
				expUnaFlag=true;
			else
				expUnaFlag=false;
			
			match = true;
			for (Map.Entry<String, File> actEntry: actOut.entrySet()){
				String actKey = actEntry.getKey();
				if(actKey.contains(flowName)){
					File actFile = actOut.get(actKey);
					actData = FileUtils.readFileToString(actFile,StandardCharsets.UTF_8);
					actTokens = actData.split("\\n(?=UNH)");
					for(String t2: actTokens){
						
						expReader = new BufferedReader(new StringReader(t1));
						expReaderTemp = expReader;
						expLine = expReaderTemp.readLine();
						if(expLine.startsWith("UNH")){
							expUNH = expLine;
							expLine = expLine.replaceAll("UNH\\+\\d*\\+", "UNH++");
						}
						t2 = t2.replaceAll("\\nUNZ.*\\n", "");
						actReader = new BufferedReader(new StringReader(t2));
						actLine = actReader.readLine();
						if((actLine.startsWith("UNA") || actLine.startsWith("UNB")) && expUnaFlag){
							actUnaData = actTokens[0];
							actReader = new BufferedReader(new StringReader(actUnaData));
							actLine = actReader.readLine();
							while(actLine!=null && expLine!=null){
								if(expLine.startsWith("UNB"))
									expLine=expLine.replaceAll("\\+\\d+:\\d+\\+\\d+'\\Z", "'");
								if(actLine.startsWith("UNB"))
									actLine=actLine.replaceAll("\\+\\d+:\\d+\\+\\d+\\+*1'\\Z", "'");
								if(!expLine.equals(actLine)){
									unaMatch = false;
									buffer.append("\nUNA/UNB Status : Not match\n");
								//	buffer.append("\nActual Data -------------------------------------\n"+t1);
									break;
								}
								expLine = expReaderTemp.readLine();
								actLine = actReader.readLine();
							}
							if(!unaMatch)
								unaNoMatchBuffer.append(actFile.getName()+"\n");
							break;
						}
						if(actLine.startsWith("UNH"))
							actLine = actLine.replaceAll("UNH\\+\\d*\\+", "UNH++");
						int actLineCount = StringUtils.countMatches(t2, "\n")+1;
						if(actLineCount != expLineCount){
							match = false;
							continue;
						}
						
						while(actLine!=null && expLine!=null){
							if(!actLine.startsWith("UNT") && !expLine.startsWith("UNT")){
								matcher = p.matcher(actLine);
								if (matcher.find())
									actId = matcher.group(0);
								matcher = p.matcher(expLine);
								if (matcher.find())
									expId = matcher.group(0);
								if(!ignoreSeg.contains(actId) && !ignoreSeg.contains(expId))
									if(!actLine.equals(expLine)){
										match=false;
										break;
									}
							}	
							expLine = expReaderTemp.readLine();
							actLine = actReader.readLine();
								
						}
						if(actLine==null && expLine==null){
							match = true;
							matchCount++;
							matchBuffer.append(expUNH+"\n");
						/*	buffer.append("\n\nMatch Found\n");
							buffer.append("\nActual Data -------------------------------\n"+t1);
							buffer.append("\nExpected Data -----------------------------\n"+t2);*/

							break;
						}
					}
					if(expUnaFlag)
						continue;
					if(match)
						break;
				}
			}
			if(!match){
				//buffer.append("\nNo Match Found\n");
				noMatchCount++;
				noMatchBuffer.append(expUNH+"\n");
			}
		}
		StringBuffer htmlBuffer = new StringBuffer();
		htmlBuffer.append("<table  border=1 style=\"width: 70%;margin-top: 50px;\" align=center >");
		htmlBuffer.append("<tr >");
		htmlBuffer.append("<td >");
		htmlBuffer.append("SCBN UNA/UNB Not correct ");
		htmlBuffer.append("</td>");
		htmlBuffer.append("<td>");
		htmlBuffer.append(unaNoMatchBuffer.toString().replaceAll("\\n", "<br>"));
		htmlBuffer.append("</td>");
		htmlBuffer.append("</tr>");
		htmlBuffer.append("<tr>");
		htmlBuffer.append("<td  >");
		htmlBuffer.append("Total UNH in XIB");
		htmlBuffer.append("</td>");
		htmlBuffer.append("<td>");
		htmlBuffer.append(expUNHCount);
		htmlBuffer.append("</td>");
		htmlBuffer.append("</tr>");
		htmlBuffer.append("<tr>");
		htmlBuffer.append("<td >");
		htmlBuffer.append("Total UNH Found");
		htmlBuffer.append("</td>");
		htmlBuffer.append("<td>");
		htmlBuffer.append(matchCount);
		htmlBuffer.append("</td>");
		htmlBuffer.append("</tr>");
		htmlBuffer.append("<tr>");
		htmlBuffer.append("<td >");
		htmlBuffer.append("Total UNH Not-Found");
		htmlBuffer.append("</td>");
		htmlBuffer.append("<td>");
		htmlBuffer.append(noMatchCount);
		htmlBuffer.append("</td>");
		htmlBuffer.append("</tr>");
		htmlBuffer.append("<tr>");
		htmlBuffer.append("<td >");
		htmlBuffer.append("Found UNH");
		htmlBuffer.append("</td>");
		htmlBuffer.append("<td>");
		htmlBuffer.append(matchBuffer.toString().replaceAll("\\n", "<br>"));
		htmlBuffer.append("</td>");
		htmlBuffer.append("</tr>");
		htmlBuffer.append("<tr>");
		htmlBuffer.append("<td >");
		htmlBuffer.append("Not-Found UNH");
		htmlBuffer.append("</td>");
		htmlBuffer.append("<td>");
		htmlBuffer.append(noMatchBuffer.toString().replaceAll("\\n", "<br>"));
		htmlBuffer.append("</td>");
		htmlBuffer.append("</tr>");
		htmlBuffer.append("</table>");
			
		
		if(unaNoMatchBuffer.length()==0 && noMatchCount==0){
			bean.addResultStatusEDI(key, "Passed");
		}
		else{
			bean.addResultStatusEDI(key, "Failed");
			String resultFile = comparisonResultPath + FilenameUtils.getBaseName(FilenameUtils.getName(expFile.getPath())) + "_EDI.html";
			bean.addResultFilePathEDI(key, resultFile);
			FileUtils.writeStringToFile(new File(bean.getResultFilePathEDI(key)), htmlBuffer.toString());
		}
		if (actReader!=null)
			actReader.close();
		expReader.close();
		expReaderTemp.close();
		return htmlBuffer.toString();
	}
	
	public static String EDICompare(File expFile, File actFile, String comparisonResultPath,ResultBean bean, String key )
			throws Exception{
		
		String[] actTokens,expTokens ;
		String expData, actData,ignoreSeg,actId="",expId="";
		boolean match = true,unaMatch = true;
		int matchCount=0, noMatchCount=0;
		BufferedReader actReader=null,expReaderTemp=null,expReader=null;
		StringBuffer buffer = new StringBuffer();
		StringBuffer noMatchBuffer = new StringBuffer();
		StringBuffer matchBuffer = new StringBuffer();
		String result = "No Difference", actUnaData, actLine,expUNH=null;
		expData = FileUtils.readFileToString(expFile,StandardCharsets.UTF_8);
		actData = FileUtils.readFileToString(actFile,StandardCharsets.UTF_8);
		actTokens = actData.split("\\n(?=UNH)");		
		expTokens = expData.split("\\n(?=UNH)");
		int expUNHCount = expTokens.length -1 ;
		ignoreSeg = bean.getIgnoreSeg(key);
		
		Pattern p = Pattern.compile("^[A-Z]{3}\\+\\w*");
		Matcher matcher ;
		
		for(String t1: expTokens){
			t1 = t1.replaceAll("\\nUNZ.*\\n", "");
			expReader = new BufferedReader(new StringReader(t1));
			expReaderTemp = expReader;
			String expLine = expReader.readLine();
			int expLineCount = StringUtils.countMatches(t1, "\n")+1;
			if(expLine.startsWith("UNA") || expLine.startsWith("UNB")){ 
				actUnaData = actTokens[0];
				actReader = new BufferedReader(new StringReader(actUnaData));
				actLine = actReader.readLine();
				while(actLine!=null && expLine!=null){
					if(expLine.startsWith("UNB"))
						expLine=expLine.replaceAll("\\+\\d+:\\d+\\+\\d+", "");
					if(actLine.startsWith("UNB"))
						actLine=actLine.replaceAll("\\+\\d+:\\d+\\+\\d+", "");
					if(!expLine.equals(actLine)){
						unaMatch = false;
						buffer.append("\nUNA/UNB Status : Not match\n");
					//	buffer.append("\nActual Data -------------------------------------\n"+t1);
						break;
					}
					expLine = expReaderTemp.readLine();
					actLine = actReader.readLine();
				}
				if(unaMatch)
					buffer.append("\nUNA/UNB Status : Match\n");
				continue;
			}
			if(expLine.startsWith("UNH")){
				expUNH = expLine;
				expLine = expLine.replaceAll("UNH\\+\\d*\\+", "UNH++");
			}
			match = true;
			for(String t2: actTokens){
				t2 = t2.replaceAll("\\nUNZ.*\\n", "");
				expReaderTemp = expReader;
				actReader = new BufferedReader(new StringReader(t2));
				actLine = actReader.readLine();
				if(actLine.startsWith("UNA"))
					continue;
				if(actLine.startsWith("UNH"))
					actLine = actLine.replaceAll("UNH\\+\\d*\\+", "UNH++");
				int actLineCount = StringUtils.countMatches(t2, "\n")+1;
				if(actLineCount != expLineCount){
					match = false;
					continue;
				}
				
				while(actLine!=null && expLine!=null){
					if(!actLine.startsWith("UNT") && !expLine.startsWith("UNT")){
						matcher = p.matcher(actLine);
						if (matcher.find())
							actId = matcher.group(0);
						matcher = p.matcher(expLine);
						if (matcher.find())
							expId = matcher.group(0);
						if(!ignoreSeg.contains(actId) && !ignoreSeg.contains(expId))
							if(!actLine.equals(expLine)){
								match=false;
								break;
							}
					}	
					expLine = expReaderTemp.readLine();
					actLine = actReader.readLine();
						
				}
				if(actLine==null && expLine==null){
					match = true;
					matchCount++;
					matchBuffer.append(expUNH+"\n");

					break;
				}
			}
			if(!match){
				noMatchCount++;
				noMatchBuffer.append(expUNH+"\n");
			}				
		}
		
		StringBuffer htmlBuffer = new StringBuffer();
		htmlBuffer.append("<table  border=1 style=\"width: 70%;margin-top: 50px;\" align=center >");
		htmlBuffer.append("<tr >");
		htmlBuffer.append("<tr>");
		htmlBuffer.append("<td >");
		htmlBuffer.append("UNA/UNB Status ");
		htmlBuffer.append("</td>");
		htmlBuffer.append("<td>");
		if(unaMatch)		
			
			htmlBuffer.append("<font color=\"green\">Passed</font>");
		else
			htmlBuffer.append("<font color=\"red\">Failed</font>");
		htmlBuffer.append("</td>");
		htmlBuffer.append("</tr>");
		htmlBuffer.append("<tr>");
		htmlBuffer.append("<td  >");
		htmlBuffer.append("Total UNH in XIB");
		htmlBuffer.append("</td>");
		htmlBuffer.append("<td>");
		htmlBuffer.append(expUNHCount);
		htmlBuffer.append("</td>");
		htmlBuffer.append("</tr>");
		htmlBuffer.append("<tr>");
		htmlBuffer.append("<td >");
		htmlBuffer.append("Total UNH Match");
		htmlBuffer.append("</td>");
		htmlBuffer.append("<td>");
		htmlBuffer.append(matchCount);
		htmlBuffer.append("</td>");
		htmlBuffer.append("</tr>");
		htmlBuffer.append("<tr>");
		htmlBuffer.append("<td >");
		htmlBuffer.append("Total UNH No-Match");
		htmlBuffer.append("</td>");
		htmlBuffer.append("<td>");
		htmlBuffer.append(noMatchCount);
		htmlBuffer.append("</td>");
		htmlBuffer.append("</tr>");
		htmlBuffer.append("<tr>");
		htmlBuffer.append("<td >");
		htmlBuffer.append("Matching UNH's");
		htmlBuffer.append("</td>");
		htmlBuffer.append("<td>");
		htmlBuffer.append(matchBuffer.toString().replaceAll("\\n", "<br>"));
		htmlBuffer.append("</td>");
		htmlBuffer.append("</tr>");
		htmlBuffer.append("<tr>");
		htmlBuffer.append("<td >");
		htmlBuffer.append("No-Matching UNH's");
		htmlBuffer.append("</td>");
		htmlBuffer.append("<td>");
		htmlBuffer.append(noMatchBuffer.toString().replaceAll("\\n", "<br>"));
		htmlBuffer.append("</td>");
		htmlBuffer.append("</tr>");
		htmlBuffer.append("</table>");
			
		
		if(unaMatch && noMatchCount==0){
			bean.addResultStatusEDI(key, "Passed");
		}
		else{
			bean.addResultStatusEDI(key, "Failed");
			String resultFile = comparisonResultPath + FilenameUtils.getBaseName(FilenameUtils.getName(actFile.getPath())) + "_EDI.html";
			bean.addResultFilePathEDI(key, resultFile);
			FileUtils.writeStringToFile(new File(bean.getResultFilePathEDI(key)), htmlBuffer.toString());
		}
		
		actReader.close();
		expReader.close();
		expReaderTemp.close();
		
		return htmlBuffer.toString();
	}
	
	public static String BcompareFile(File expFile, File actFile, String comparisonResultPath,ResultBean bean,String key )
			throws Exception{
		
		Process process;
		String result=null;
		String expectedPath, actualPath;
		expectedPath = expFile.getPath();
		actualPath = actFile.getPath();
		String BcompPath = Config.getInstance().getProperty("BComp");
		String BcomparePath = Config.getInstance().getProperty("BCompare");
		String BcompareScript = Config.getInstance().getProperty("BCompareScript");
		int exitValue= 0;
		try{
			String resultFile = comparisonResultPath + FilenameUtils.getBaseName(FilenameUtils.getName(expectedPath)) + ".html";
			process = new ProcessBuilder(BcompPath, "/qc=binary",expectedPath,actualPath).start();
			process.waitFor();
			exitValue = process.exitValue();
			if (exitValue == 1){
				result =  "No Difference";
				bean.addResultStatus(key, "Passed");
			}
			else{
				process = new ProcessBuilder(BcomparePath, "@"+BcompareScript,expectedPath,actualPath,resultFile,"/closescript").start();
				bean.addResultStatus(key, "Failed");
				bean.addResultFilePath(key, resultFile);
				result = resultFile;
			}
		    
		}catch(Exception e){

		   }
		return result;
	}
	

	public static void unzipFile(String filePath, String destDirectory) {
		FileInputStream fis = null;
		ZipInputStream zipIs = null;
		ZipEntry zEntry = null;
		File destDir = new File(destDirectory);
		if (!destDir.exists()) {
			destDir.mkdirs();
		}

		try {
			fis = new FileInputStream(filePath);
			zipIs = new ZipInputStream(new BufferedInputStream(fis));
			while ((zEntry = zipIs.getNextEntry()) != null) {
				try {
					byte[] tmp = new byte[4 * 1024];
					FileOutputStream fos = null;
					String fileName = zEntry.getName().toString();
					File file = new File(fileName);
					if (file.getName().indexOf(".TXT") != -1 || file.getName().indexOf(".txt") != -1) {
						String opFilePath = destDir + "/" + file.getParentFile().getParentFile().getName() +"/"+file.getName();
						fos = FileUtils.openOutputStream(new File(opFilePath));
						//fos = new FileOutputStream(opFilePath);
						int size = 0;
						while ((size = zipIs.read(tmp)) != -1) {
							fos.write(tmp, 0, size);
						}
						fos.flush();
						fos.close();
					} else {
						getFiles(file, destDir, filePath);
					}

				} catch (Exception ex) {
					System.out.println("Error11 ::" + ex.getMessage());
				}
			}
			zipIs.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void getFiles(File file, File destDir, String filePath) throws Exception {
		try {
			FileOutputStream fos = null;
			if (file.isDirectory()) {
				if (file.isDirectory()) {
					for (File tempFile : file.listFiles()) {
						getFiles(tempFile, destDir, filePath);
					}

				}
			} else if (file.getName().indexOf(".TXT") != -1) {
				String opFilePath = destDir + "/"
						+ file.getName();
				fos = new FileOutputStream(opFilePath);
				
				String filePathString = file.getPath();// .substring(file.getPath().indexOf("\\")+1,file.getPath().length());

				file = new File((filePath + filePathString).replace(".zip", "\\"));
				fos.write(FileUtils.readFileToByteArray(file));
				fos.flush();
				fos.close();
			}
		} catch (Exception e) {
			System.out.println("Error ::" + e.getMessage());
		}
	}
	
	
	public static void RunThreadedOrgFlowName(String filepath, String destpath, ThreadGroup threadGroup) throws Exception {
		
		File folder = new File(filepath);
		File[] listOfFiles = folder.listFiles();
		int noOfFiles = listOfFiles.length;
		int noOfThreads = NumberUtils.toInt(Config.getInstance().getProperty("ThreadCount"));
		
		for(int i=0;i<noOfFiles;i++){
			File fileObj = listOfFiles[i];
			if(fileObj.isDirectory()){
				RunThreadedOrgFlowName(fileObj.getPath(), destpath, threadGroup);
			}
			else{
				//new OrganizeImplThread(filepath,destpath);
				while(true){
					if(threadGroup.activeCount()<noOfThreads){
						Thread t = new Thread(threadGroup,new OrgFlowName(filepath,destpath),"" );
						t.start();
						break;
					}
					else{
						System.out.println("Sleeping -- "+filepath);
						Thread.sleep(10000);
					}
				}
				break;
			}
		}
	}
	
	public static void RunThreadedOrganizeTestFile(String filepath, String destpath, ThreadGroup threadGroup) throws Exception {
		
		File folder = new File(filepath);
		File[] listOfFiles = folder.listFiles();
		int noOfFiles = listOfFiles.length;
		int noOfThreads = NumberUtils.toInt(Config.getInstance().getProperty("ThreadCount"));
		
		for(int i=0;i<noOfFiles;i++){
			File fileObj = listOfFiles[i];
			if(fileObj.isDirectory()){
				RunThreadedOrganizeTestFile(fileObj.getPath(), destpath, threadGroup);
			}
			else{
				//new OrganizeImplThread(filepath,destpath);
				while(true){
					if(threadGroup.activeCount()<noOfThreads){
						Thread t = new Thread(threadGroup,new OrganizeImplThread(filepath,destpath),"" );
						t.start();
						break;
					}
					else{
						System.out.println("Sleeping -- "+filepath);
						Thread.sleep(10000);
					}
				}
				break;
			}
		}
	}
	
	//This function is same as RunThreadedOrganizeTestFile but not Threaded. Single instance . 
	public static void OrganizeTestFileMapNameMain(String filepath, String destpath) throws Exception {
		
		InputStream inXlsx = new FileInputStream(Config.getInstance().getProperty("InputXlsx"));
		
		XSSFWorkbook workbook = new XSSFWorkbook(inXlsx);
		OrganizeTestFilesMapNameSub(filepath, destpath,workbook);
		
	//	inXlsx.close();
		workbook.close();

	}
	
	public static void OrganizeTestFilesMapNameSub (String filepath, String destpath,XSSFWorkbook workbook) throws Exception{
		
		Iterator < Row >  rowIterator;
		XSSFRow row;
		String xl_mapname, mapInputName;
		DataFormatter formatter = new DataFormatter();
		
		File folder = new File(filepath);
		File[] listOfFiles = folder.listFiles();
		File moveToFolder, tempFile=null;
		int noOfFiles = listOfFiles.length;
		StringTokenizer st_param;
		
		XSSFSheet partnerSheet = workbook.getSheet(Config.getInstance().getProperty("InXlsxSheetMapName"));
		
		try{
			for(int i=0;i<noOfFiles;i++){
				File fileObj = listOfFiles[i];
				if(fileObj.isDirectory()){
					OrganizeTestFilesMapNameSub(fileObj.getPath(), destpath, workbook);
				}
				else{
					rowIterator = partnerSheet.iterator();
		 			row = (XSSFRow) rowIterator.next();
		 			while (rowIterator.hasNext()) {
		 				row = (XSSFRow) rowIterator.next();
		 		        xl_mapname = formatter.formatCellValue(row.getCell(0));
		 		        xl_mapname = StringUtils.remove(xl_mapname,".x4");
		 		       Collection<File> files = FileUtils.listFiles(folder, new WildcardFileFilter("*"+xl_mapname+".TXT"), null);
						
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
		 			OutputStream outXlsx = new FileOutputStream(Config.getInstance().getProperty("InputXlsx"));
		 			workbook.write(outXlsx);
		 			outXlsx.close();
					break;
				}
			}
		}catch (Exception e){
				e.printStackTrace();
				//System.out.println("Exception FileName --"+testFileName);
				//throw e;
				}
}
	public static void OrganizeTestFilesMain (String filepath, String destpath, String type) throws Exception{
		
		DirMap.clear();
		InputStream inXlsx = new FileInputStream(Config.getInstance().getProperty("InputXlsx"));
		
		XSSFWorkbook workbook = new XSSFWorkbook(inXlsx);
		OrganizeTestFilesxlsx(filepath, destpath,workbook, type);
		
		OutputStream outXlsx = new FileOutputStream(Config.getInstance().getProperty("InputXlsx"));
		workbook.write(outXlsx);
	//	inXlsx.close();
		workbook.close();
		outXlsx.close();
	}
	public static void OrganizeTestFilesxlsx (String filepath, String destpath,XSSFWorkbook workbook, String type) throws Exception{
		
		Iterator < Row >  rowIterator;
		XSSFRow row;
		String xl_receiver, xl_msgType, xl_sender;
//		HashMap<String,String> missingMap = new HashMap<String,String>();
		boolean inFlag = false;
		
		DataFormatter formatter = new DataFormatter();
		File folder = new File(filepath);
		File rootFolder = null,moveToFolder, outFolder;
		File[] listOfFiles = folder.listFiles();
		int noOfFiles = listOfFiles.length;
		String testFileName = null,nextFileName=null,testId, param, sender, receiver, msgType;
		StringTokenizer st_param;
		
		XSSFSheet partnerSheet = workbook.getSheet(Config.getInstance().getProperty("InXlsxSheetPartnerName"));
		try{
			for(int i=0;i<noOfFiles;i++){
				inFlag = false;
				File fileObj = listOfFiles[i];
				if(fileObj.isFile()){
					testFileName = fileObj.getName();
					if (testFileName.indexOf("-") == -1)
						continue;
				//	if (!testFileName.matches("[0-9]{8}-0000-0-[0-9]{14}.TXT")){
					try {
						if(!testFileName.contains("(") && !testFileName.contains(")"))
						{
							nextFileName= listOfFiles[i+1].getName();
							if(testFileName.substring(0, testFileName.indexOf("-")).equals(nextFileName.substring(0, testFileName.indexOf("-")))){
								testFileName = nextFileName;
								inFlag = true;
							}
							else{
								moveToFolder = new File(destpath+"\\UnOrganized");
								if(!moveToFolder.exists())
									moveToFolder.mkdir();
								FileUtils.copyFileToDirectory(fileObj, moveToFolder, false );
							}
						}
						if(testFileName.contains("(") && testFileName.contains(")")) {
				 			param = testFileName.substring(testFileName.indexOf("(")+1, testFileName.indexOf(")"));
				 			st_param = new StringTokenizer(param,"-");
				 			sender = StringUtils.stripStart(st_param.nextToken(),"0");
				 			receiver = StringUtils.stripStart(st_param.nextToken(),"0");
				 			msgType = st_param.nextToken();
				 			testId = testFileName.substring(0, testFileName.indexOf("-"));
				 		//	if (!DirMap.containsKey(testId)&& !missingMap.containsKey(testId)){
				 		//		System.out.println("Building HashMap -- " +testFileName);
					 			rowIterator = partnerSheet.iterator();
					 			row = (XSSFRow) rowIterator.next();
				//	 			found = false;
					 			while (rowIterator.hasNext()) {
					 				
					 		         row = (XSSFRow) rowIterator.next();
					 		         xl_sender = formatter.formatCellValue(row.getCell(0));
					 		         xl_receiver = formatter.formatCellValue(row.getCell(1));//.getStringCellValue();
					 		         xl_msgType = formatter.formatCellValue(row.getCell(2));//.getStringCellValue();
						 			if (receiver.equals(xl_receiver) && msgType.equals(xl_msgType) && sender.equals(xl_sender) ){
						 				if(!DirMap.containsKey(testId)){
						 					if (type.equals("RCV"))
						 						DirMap.put(testId, xl_receiver+"\\"+xl_msgType);
						 					else if(type.equals("MSG"))
						 						DirMap.put(testId, xl_msgType+"\\"+xl_receiver);
						 				}
						 				if (testFileName.endsWith("Final.TXT")){
						 					row.createCell(3).setCellValue("X");
					 						rootFolder = new File(destpath+"\\"+DirMap.get(testId));
					 						if(inFlag)
					 							moveToFolder = new File(rootFolder.getPath()+"\\Input");
					 						else
					 							moveToFolder = new File(rootFolder.getPath()+"\\Output");
											if (!moveToFolder.exists())
												moveToFolder.mkdir();
						 				}
						 				else
						 				{
						 					rootFolder = new File(destpath+"\\"+DirMap.get(testId));
						 					if(inFlag)
						 						moveToFolder = new File(rootFolder.getPath()+"\\Input");
						 					else
						 						moveToFolder = new File(rootFolder.getPath()+"\\Intermediate");
											if (!moveToFolder.exists())
												moveToFolder.mkdir();
						 				}

									//	FileUtils.moveFileToDirectory(fileObj, moveToFolder, false );
										FileUtils.copyFileToDirectory(fileObj, moveToFolder, false );
		//				 				found = true;
						 				break;
						 			}
					 			}
					 			if(!DirMap.containsKey(testId)){
					 				moveToFolder = new File(destpath+"\\UnOrganized");
									if(!moveToFolder.exists())
										moveToFolder.mkdir();
									FileUtils.copyFileToDirectory(fileObj, moveToFolder, false );
					 			}
					 		//	if (!found && !missingMap.containsKey(testId))
					 		//		missingMap.put(testId, "");
				 		//	}
			 			}
					}catch(Exception e){
						e.printStackTrace();
						System.out.println("Exception FileName --"+testFileName);
					}
				}
				else
					OrganizeTestFilesxlsx(fileObj.getPath(), destpath, workbook,type);
			}
		}catch (Exception e){
			e.printStackTrace();
			System.out.println("Exception FileName --"+testFileName);
			//throw e;
		}
	}
}