package com.wainyz.core.manager;


import com.wainyz.core.pojo.domain.ControllerFileDO;
import com.wainyz.core.pojo.domain.HistoryFileDO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 用于管理数据文件，获取，保存，转换等。对于原文件，历史作答文件，控制文件等都进行管理。
 * 1. 向同层同事之间提供数据：定义各数据文件的保存路径，其他对象通过访问该管理的单例对象来获取这些常量，防止四处注入配置文件的混乱
 * 2. 获取四种文件的json内容
 * 3. 保存四种文件的方法
 * 4. 删除文件的方法
 * @author Yanion_gwgzh
 */
@Component
public class DataFileManager {
    //---------------0--------------- 初始化以及一些常量定义，一些通用底层方法
    /*
    生命周期方法
     */
    public DataFileManager(
            @Value("${file.data-file-base-path}")
            String dataFileBasePath,
            @Value("${file.controller-file-base-path}")
            String controllerFileBasePath,
            @Value("${file.deepseek-response}")
            String currenQuestionFileBasePath
                      ){
        this.ControllerFileBasePath = controllerFileBasePath;
        this.CurrenQuestionFileBasePath = currenQuestionFileBasePath;
        this.DataFileBasePath = dataFileBasePath;
    }
    /*
    获取文件的统一步骤
     */
    private File getFile0(String dirPath, String fileName)throws IOException  {
        // 检查路径与文件
        File dir = new File(dirPath);
        File file = new File(dirPath, fileName);
        if (!file.exists() && !dir.exists()){
            throw new IOException("文件不存在【34】");
        }
        return file;
    }

    /**
     * 获取文件时默认添加后缀
     * @param dirPath
     * @param fileId
     * @return
     * @throws IOException 文件不存在
     */
    public File getFile(String dirPath, String fileId)throws IOException  {
        return getFile0(dirPath, fileId + FileSuffix);
    }
    /*
    保存文件的统一步骤
     */
    private void saveFile(String dirPath, String fileId, String content)throws IOException {
        // 检查路径与文件
        File dir = new File(dirPath);
        File file = new File(dirPath, fileId + FileSuffix);
        if (!dir.exists()){
            try {
                if (!dir.mkdirs()) {
                    throw new IOException("创建目录失败，请检查外部干扰带来的影响。【72】");
                }
                file.createNewFile();
            }catch (IOException e){
                throw new IOException("准备好条件后，文件仍创建失败，请检查外部干扰带来的影响。【50】");
            }
        }
        // 确保文件存在后，保存文件
        try (
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                BufferedOutputStream bufferedOutputStream= new BufferedOutputStream(fileOutputStream);
        ){
            bufferedOutputStream.write(content.getBytes(FileContentEncoding));
        }
    }

    //===============0==============
    //---------------1--------------
    /*
    文件路径配置
     */
    public final String DataFileBasePath;
    public final String ControllerFileBasePath;

    /**
     * 需要提供文件Id才能找到文件的历史答题记录保存地址
     * @param fileId 文件Id
     * @return 历史答题记录保存地址
     */
    public final String getHistoryFileBasePath(String fileId){
        return ControllerFileBasePath+fileId+"/";
    }
    /**
     * 需要提供文件Id才能找到answer存放地址
     * @param fileId 文件Id
     * @return 历史答题记录保存地址
     */
    public final String getAnswerFileBasePath(String fileId){
        return ControllerFileBasePath + ANSWER_FILE_PREFIX +fileId+"/";
    }
    public final String getExamFileBasePath(String userId){
        return CurrenQuestionFileBasePath + userId + '/';
    }
    public final String CurrenQuestionFileBasePath;
    /*
    编码配置
     */
    public final Charset FileContentEncoding = StandardCharsets.UTF_8;
    /*
    文件的默认后缀
     */
    public final String FileSuffix = ".txt";
    //===============1==============

    //---------------2--------------

    /**
     * 获取原文件内容
     * @param fileId
     * @return 如果没有文件，则返回空字符串
     */
    public String getDataFileContent(String fileId){
        try {
            File file = getFile(DataFileBasePath, fileId);
            try(
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
            ){
                return new String(bufferedInputStream.readAllBytes(), FileContentEncoding);
            }
        } catch (IOException e) {
            return "";
        }
    }

    /**
     * @param fileId
     * @return
     */
    public long getDataFileSize(String fileId){
        return new File(DataFileBasePath,fileId + ".txt").length();
    }
    /**
     * 获取控制文件内容
     * @param fileId
     * @return 如果没有文件，则返回默认控制文件内容
     */
    public String getControllerFileContent(String fileId){
        try {
            File file = getFile(ControllerFileBasePath, fileId);
            try(
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
            ){
                return new String(bufferedInputStream.readAllBytes(), FileContentEncoding);
            }
        } catch (IOException e) {
            return ControllerFileDO.DEFAULT_CONTROLLER_FILE_CONTENT;
        }
    }
    public String getControllerFileContent(File file){
        try(
                BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
        ){
            return new String(bufferedInputStream.readAllBytes(), FileContentEncoding);
        } catch (IOException e) {
            throw new RuntimeException("传入了不存在的file.[164]");
        }
    }
    /**
     * 获取当前question文件内容
     * @param fileId
     * @return 如果没有文件，则返回空
     */
    public String getCurrenQuestionContent(String fileId){
        try {
            File file = getFile(CurrenQuestionFileBasePath, fileId);
            try(
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
            ){
                return new String(bufferedInputStream.readAllBytes(), FileContentEncoding);
            }
        } catch (IOException e) {
            return "";
        }
    }
    /**
     * warning：这个方法高度耦合了json格式，需要特别注意
     * 获取历史question内容的所有内容,返回的是一个json数组字符串
     * @param fileId
     * @return 如果没有文件，则返回默认控制文件内容
     */
    public String getHistoryFilesContent(String fileId){
        try {
            File dir = new File(getHistoryFileBasePath(fileId));
            if (!dir.exists() || !dir.isDirectory()){
                return "";
            }
            StringBuilder builder = new StringBuilder();
            builder.append("[");
            for (File file : dir.listFiles()) {
                try(
                        BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
                        ){
                    String timestamp = file.getName().substring(0, file.getName().length() - 4);
                    builder.append("{\"timestamp\":")
                            .append(timestamp)
                            .append(",").append("\"content\":");
                    builder.append(new String(bufferedInputStream.readAllBytes(), FileContentEncoding)).append("},");
                }
            }
            builder.delete(builder.length()-1,builder.length());
            builder.append("]");
            return builder.toString();
        } catch (IOException e) {
            return "";
        }
    }
    public File[] getHistoryFiles(String fileId){
        File dir = new File(getHistoryFileBasePath(fileId));
        if (!dir.exists() || !dir.isDirectory()){
            return new File[0];
        }
        return dir.listFiles();
    }
    public File[] getAllControllerFile(){
        File dir = new File(ControllerFileBasePath);
        return dir.listFiles();
    }
    public File[] getAllRawFile(){
        return new File(DataFileBasePath).listFiles();
    }
    //================2==============
    //----------------3--------------
    public void saveDataFile(String fileId, String content)throws IOException {
        saveFile(DataFileBasePath, fileId, content);
    }
    public void saveControllerFile(String fileId, String content)throws IOException {
        saveFile(ControllerFileBasePath, fileId, content);
    }
    public void saveControllerFile(String fileId, ControllerFileDO controllerFileDO) throws Exception {
        saveControllerFile(fileId, controllerFileDO.toJsonString());
    }
    public void saveCurrenQuestionFile(String fileId, String content)throws IOException {
        saveFile(CurrenQuestionFileBasePath, fileId, content);
    }
    public void saveHistoryQuestionFile(String fileId, String content)throws IOException {
        saveFile(getHistoryFileBasePath(fileId), String.valueOf(System.currentTimeMillis()), content);
    }
    public void  saveHistoryQuestionFile(String fileId, HistoryFileDO historyFileDO) throws Exception {
        saveHistoryQuestionFile(fileId, historyFileDO.toJsonString());
    }

    //=================3==============
    //-----------------4--------------
    public void deleteDataFile(String fileId){
        File file = new File(DataFileBasePath, fileId);
        file.delete();
        deleteControllerFile(fileId);
        deleteCurrentQuestionFile(fileId);
        deleteAllHistoryQuestionFile(fileId);
    }
    public void deleteControllerFile(String fileId){
        new File(ControllerFileBasePath, fileId).delete();
    }
    public void deleteCurrentQuestionFile(String fileId){
        new File(CurrenQuestionFileBasePath, fileId).delete();
    }
    public void deleteHistoryQuestionFile(String rawFileId, String historyFileId){
        File file = new File(getHistoryFileBasePath(rawFileId), historyFileId);
        file.delete();
    }
    public void deleteAllHistoryQuestionFile(String rawFileId){
        File dir = new File(getHistoryFileBasePath(rawFileId));
        if (!dir.exists() || !dir.isDirectory()){
            return;
        }
        for (File file : dir.listFiles()) {
            file.delete();
        }
    }
    //================4=================
    //-----------------5--------------存储和获取answer
    public static final String ANSWER_FILE_PREFIX = "answer";
    public void saveAnswerFile(String fileId, String content)throws IOException {
        saveFile(getAnswerFileBasePath(fileId),fileId, content);
    }
    public String getAnswerFileContent(String fileId) throws IOException {
        File file = getFile(getAnswerFileBasePath(fileId), fileId);
        try(
                BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
        ){
            return new String(bufferedInputStream.readAllBytes(), FileContentEncoding);
        } catch (IOException e) {
            throw new FileNotFoundException("answer file不存在.[299]");
        }
    }
    public void deleteAnswerFile(String fileId){
        new File(getAnswerFileBasePath(fileId), fileId).delete();
    }
    // ---------------6-----------
    public void saveExamFile(Long userId, Long examId, String examContent) throws IOException, NumberFormatException {
        saveFile(getExamFileBasePath(String.valueOf(userId)), String.valueOf(examId),examContent);
    }
    public void deleteExamFile(String userId, String examId){
        new File(getExamFileBasePath(userId),examId).delete();
    }
}
