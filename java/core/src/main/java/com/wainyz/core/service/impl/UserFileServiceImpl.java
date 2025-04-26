package com.wainyz.core.service.impl;


import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wainyz.core.mapper.UserFileMapper;
import com.wainyz.core.pojo.domin.ControllerFileDO;
import com.wainyz.core.pojo.po.UserFilePO;
import com.wainyz.core.service.UserFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class UserFileServiceImpl extends ServiceImpl<UserFileMapper, UserFilePO> implements UserFileService {
    private final Logger logger = LoggerFactory.getLogger(UserFileService.class);

    @Value("${server.port}")
    private int workId;
    @Value("1")
    private int dataCenterId;
    @Value("${file.data-file-base-path}")
    private String basePath;

    @Value("${file.controller-file-base-path}")
    private String controllerFileBasePath;

    @Override
    public Long saveFile(String title, MultipartFile file, Long userId, int isPublish) {
        // 通过hutool获取 雪花算法id
        long id = IdUtil.getSnowflake(workId%32, dataCenterId%32).nextId();
        File dir = new File(basePath);
        if (!dir.exists()){
            dir.mkdirs();
        }
        File localFile = new File(basePath + id + ".txt");
        if (!localFile.exists()){
            try {
                localFile.createNewFile();
            } catch (IOException e) {
                logger.error("文件创建失败");
                throw new RuntimeException("文件创建失败");
            }
        }
        //保存文件到本地
        try (
                BufferedOutputStream bufferedInputStream = new BufferedOutputStream(new FileOutputStream(localFile));
                )
        {
            bufferedInputStream.write(file.getBytes());
            
        }catch (Exception e){
            logger.error(e.getMessage());
            throw new RuntimeException("文件保存失败");
        }
        // 保存数据到数据库
        save(UserFilePO.builder()
                .id(id)
                .owner(userId)
                .filePath(localFile.getPath())
                .title(title)
                .isPublic(isPublish)
                .build()
        );
        //保存一份默认的分析表
        File dir1 = new File(controllerFileBasePath);
        if(!dir1.exists()){
            dir1.mkdirs();
        }
        File historyFile = new File(dir1, id + ".txt");
        try{
            if(historyFile.exists()){
                historyFile.delete();
            }else{
                historyFile.createNewFile();
            }
            try(
                    BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(historyFile))
            ){
                bufferedOutputStream.write(ControllerFileDO.DEFAULT_CONTROLLER_FILE_CONTENT.getBytes(StandardCharsets.UTF_8));
            }
        }catch (IOException e){
            throw new RuntimeException("创建文件失败，可能的原因是文件已存在或者打开文件失败。[79]");
        }
        return id;
    }
}
