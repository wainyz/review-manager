package com.wainyz.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wainyz.user.pojo.po.Ban;
import com.wainyz.user.pojo.vo.BanUserVo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface BanService extends IService<Ban> {
    BanUserVo checkBanInfo(Long userId);
    BanUserVo convertBanUserVo(Ban ban);
    List<BanUserVo> getAllBanUsers();
}
