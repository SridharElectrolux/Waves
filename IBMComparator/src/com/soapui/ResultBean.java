package com.soapui;

import java.io.File;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;

public class ResultBean {
	String requestFileName;
	String responseFileName;
	String actualResponseFileName;
	String request;
	File reqFile;
	
	HashMap<String,File> expOut = new HashMap<String,File>();
	String response;
	HashMap<String,File> actOut = new HashMap<String,File>();
	String actualResponse;
	HashMap<String,String> resStatusMap = new HashMap<String,String>();
	HashMap<String,String> resStatusEDIMap = new HashMap<String,String>();
	String result;
	HashMap<String,String> resFileMap = new HashMap<String,String>();
	HashMap<String,String> resFileEDIMap = new HashMap<String,String>();
	HashMap<String,Boolean> ediFlag = new HashMap<String,Boolean>();
	HashMap<String,Boolean> changeNameFlag = new HashMap<String,Boolean>();
	HashMap<String,Boolean> multiAppendFlag = new HashMap<String,Boolean>();
	HashMap<String,Integer> waitTimer = new HashMap<String,Integer>();
	HashMap<String,String> comMethod = new HashMap<String,String>();
	HashMap<String,String> fileNamePattern = new HashMap<String,String>();
	HashMap<String,String> ignoreSeg = new HashMap<String,String>();
	HashMap<String,String> ignoreField = new HashMap<String,String>();
	HashMap<String, String> headStart = new HashMap<String, String>();
	String resultFile;
//	String resultFileBcompare;
	String consolidatedFilePath ;
	//boolean ediFlag=false;
	public void addHeadStart (String key, String headStart){
		this.headStart.put(key, headStart);
	}
	public String getHeadeStart(String key){
		return headStart.get(key);
	}
	public String getIgnoreSeg (String key){
		return ignoreSeg.get(key);
	}
	
	public void addIgnoreSeg (String key, String seg){
		ignoreSeg.put(key, seg);
	}
	public String getIgnoreField (String key){
		return ignoreField.get(key);
	}
	
	public void addIgnoreField (String key, String seg){
		ignoreField.put(key, seg);
	}
	public String getKey(String fileName){
		String key;
		key = fileName.substring(0,StringUtils.ordinalIndexOf(fileName, "-", 2))+"-"+fileName.substring(fileName.indexOf("("), fileName.indexOf(")")+1);
		return key;
	}
	public void addExpOutput(File f){
		expOut.put(getKey(f.getName()), f);	
	}
	public void addActOutput(File f){
		if(f!=null)
		actOut.put(getKey(f.getName()), f);	
	}
	
	public void addResultStatus(String key, String resultStatus){
		resStatusMap.put(key, resultStatus);
	}
	
	public void addResultStatusEDI(String key, String resultStatus){
		resStatusEDIMap.put(key, resultStatus);
	}
	
	public String getResultStatus(String key){
		return resStatusMap.get(key);
	}
	
	public String getResultStatusEDI(String key){
		return resStatusEDIMap.get(key);
	}
	
	public void addResultFilePath(String key, String resultFile){
		resFileMap.put(key, resultFile);
	}
	public void addResultFilePathEDI(String key, String resultFile){
		resFileEDIMap.put(key, resultFile);
	}
	public String getResultFilePath(String key){
		return resFileMap.get(key);
	}
	public String getResultFilePathEDI(String key){
		return resFileEDIMap.get(key);
	}
	public HashMap<String,File> getExpOutput(){
		return expOut;
	}
	public HashMap<String,File> getActOutput(){
		return actOut;
	}
	public boolean getEdiFlag(String key){
		return ediFlag.get(key);
	}
	public void setEdiFlag(String key, boolean flag){
		this.ediFlag.put(key, flag);
	}
	public boolean getChangeNameFlag(String key){
		return changeNameFlag.get(key);
	}
	public void setChangeNameFlag(String key, boolean flag){
		this.changeNameFlag.put(key, flag);
	}
	public boolean getMultiappendFlag(String key){
		return multiAppendFlag.get(key);
	}
	public void setMultiappendFlag(String key, boolean flag){
		this.multiAppendFlag.put(key, flag);
	}
	public int getWaitTimer(String key){
		return waitTimer.get(key);
	}
	public void setWaitTimer(String key, int time){
		this.waitTimer.put(key, time);
	}
	public String getComMethod(String key){
		return comMethod.get(key);
	}
	public void setComMethod(String key, String method){
		this.comMethod.put(key, method);
	}
	public String getFileNamePattern(String key){
		return fileNamePattern.get(key);
	}
	public void setFileNamePattern(String key, String pattern){
		this.fileNamePattern.put(key, pattern);
	}
	public String getConsolidatedFilePath() {
		return consolidatedFilePath;
	}
	public void setConsolidatedFilePath(String consolidatedFilePath) {
		this.consolidatedFilePath = consolidatedFilePath;
	}
	public String getResultFile() {
		return resultFile;
	}
	public void setResultFile(String resultFile) {
		this.resultFile = resultFile;
	}
	
/*	public String getResultFileBcompare() {
		return resultFileBcompare;
	}
	public void setResultFileBcompare(String resultFileBcompare) {
		this.resultFileBcompare = resultFileBcompare;
	}
	*/
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public String getRequest() {
		return request;
	}
	public void setRequest(String request) {
		this.request = request;
	}
	public String getResponse() {
		return response;
	}
	public void setResponse(String response) {
		this.response = response;
	}
	public String getActualResponse() {
		return actualResponse;
	}
	public void setActualResponse(String actualResponse) {
		this.actualResponse = actualResponse;
	}
	String resultStatus;
	String db_Code_Error = "N";
	String dateSignum_Error="N";
	String time;
	public String getRequestFileName() {
		return requestFileName;
	}
	public void setRequestFileName(String requestFileName) {
		this.requestFileName = requestFileName;
	}
	public String getResponseFileName() {
		return responseFileName;
	}
	public void setResponseFileName(String responseFileName) {
		this.responseFileName = responseFileName;
	}
	public String getActualResponseFileName() {
		return actualResponseFileName;
	}
	public void setActualResponseFileName(String actualResponseFileName) {
		this.actualResponseFileName = actualResponseFileName;
	}
	public String getResultStatus() {
		return resultStatus;
	}
	public void setResultStatus(String resultStatus) {
		this.resultStatus = resultStatus;
	}
	
	
	public String getDb_Code_Error() {
		return db_Code_Error;
	}
	public void setDb_Code_Error(String db_Code_Error) {
		this.db_Code_Error = db_Code_Error;
	}
	public String getDateSignum_Error() {
		return dateSignum_Error;
	}
	public void setDateSignum_Error(String dateSignum_Error) {
		this.dateSignum_Error = dateSignum_Error;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public void setRequestFile(File path) {
		this.reqFile = path;
		
	}
	public File getRequestFile() {
		return this.reqFile;
		
	}
}
