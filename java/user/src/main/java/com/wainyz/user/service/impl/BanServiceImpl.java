package com.wainyz.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wainyz.commons.pojo.domin.UserDO;
import com.wainyz.user.mapper.BanMapper;
import com.wainyz.user.pojo.po.Ban;
import com.wainyz.user.pojo.vo.BanUserVo;
import com.wainyz.user.service.BanService;
import com.wainyz.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author Yanion_gwgzh
 */
@Service
public class BanServiceImpl extends ServiceImpl<BanMapper, Ban> implements BanService {
    @Autowired
    private UserService userService;
    @Override
    public BanUserVo convertBanUserVo(Ban ban){
        BanUserVo banUserVo = new BanUserVo();
        banUserVo.setUserId(ban.getId());
        banUserVo.setLiftTime(ban.getLiftTime().getTime());
        UserDO userDO = userService.getUserById(ban.getId());
        banUserVo.setEmail(userDO.getEmail());
        banUserVo.setUsername(userDO.getUsername());
        return banUserVo;
    }

    @Override
    public List<BanUserVo> getAllBanUsers() {
        List<Ban> list = lambdaQuery().select().list();
        return list.stream().map(ban -> convertBanUserVo(ban)).toList();
    }

    /**
     * 检查用户的封禁情况，并移除过期封禁
     *
     * @param userId
     * @return
     */
    @Override
    public BanUserVo checkBanInfo(Long userId) {
        Ban one = lambdaQuery().eq(Ban::getId, userId).one();
        if(one == null ){
            return null;
        }
        if(one.getLiftTime().before(new Date())){
            removeById(userId);
            return null;
        }else{
            return convertBanUserVo(one);
        }
    }
}
