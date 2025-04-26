package com.wainyz.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wainyz.core.pojo.po.UserFilePO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Yanion_gwgzh
 */
@Service
public interface UserFileService extends IService<UserFilePO> {
    Long saveFile(String title, MultipartFile file, Long userId, int isPublish);
}
