package com.wainyz.core.mapper;

import com.wainyz.core.pojo.domain.Notice;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Yanion_gwgzh
* @description 针对表【notice】的数据库操作Mapper
* @createDate 2025-05-13 13:36:33
* @Entity com.wainyz.core.pojo.domain.Notice
*/
@Mapper
public interface NoticeMapper extends BaseMapper<Notice> {
}




