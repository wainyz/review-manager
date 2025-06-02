package com.wainyz.core.controller;

import cn.hutool.core.util.IdUtil;
import com.wainyz.commons.consistent.GatewayConsistent;
import com.wainyz.commons.pojo.vo.ReturnModel;
import com.wainyz.core.consident.NoticeTypeEnum;
import com.wainyz.core.pojo.domain.Friends;
import com.wainyz.core.pojo.domain.Notice;
import com.wainyz.core.service.FriendsService;
import com.wainyz.core.service.NoticeService;
import com.wainyz.user.pojo.po.UserPO;
import com.wainyz.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Yanion_gwgzh
 */
@RestController
@RequestMapping("/friends")
public class FriendController {
    @Autowired
    private FriendsService friendsService;
    @Autowired
    private NoticeService noticeService;
    @Autowired
    private UserService userService;

    /**
     * 添加好友申请
     * @param friendId
     * @param userId
     * @return
     */
    @GetMapping("/apply/add/{friendId}")
    public ReturnModel applyAddFriend(@PathVariable("friendId")Long friendId, @RequestAttribute(GatewayConsistent.USER_ID) Long userId){
        Long smallId = userId < friendId? userId: friendId;
        Long bigId = !userId.equals(smallId) ? userId: friendId;
        // 检查是否是合法用户
        Long count = friendsService.lambdaQuery().eq(Friends::getSmallid, smallId).eq(Friends::getBigid, bigId).count();
        if(count != 0){
            return ReturnModel.fail().setMessage("好友已存在.");
        }
        // 检查是否已经发送了请求
        count = noticeService.lambdaQuery()
                .eq(Notice::getUserid, friendId)
                .eq(Notice::getType, NoticeTypeEnum.ADD_FRIEND_APPLY.value)
                .like(Notice::getContent, NoticeTypeEnum.ADD_FRIEND_APPLY.otherInfo[0] + "=" + userId + ",%")
                .count();
        if(count != 0 ){
            return ReturnModel.fail().setMessage("已存在好友申请。");
        }
        // 发送通知
        if (noticeService.addFriendApplyNotice(userId,friendId)) {
            return ReturnModel.success();
        }else{
            return ReturnModel.fail();
        }
    }
    /**
     * 同意好友申请
     * @param friendId
     * @param userId
     * @return
     */
    @GetMapping("/apply/agree/{friendId}")
    public ReturnModel applyAgreeFriend(@PathVariable("friendId")Long friendId, @RequestAttribute(GatewayConsistent.USER_ID) Long userId){
        Long smallId = userId < friendId? userId: friendId;
        Long bigId = userId != smallId? userId: friendId;
        // 发送通知(校验步骤包含在这里面，所以将保存到数据库的操作放在后面)
        if (noticeService.agreeFriendApplyNotice(userId,friendId)) {
            Friends friends = new Friends();
            friends.setId(IdUtil.getSnowflake().nextId());
            friends.setSmallid(smallId);
            friends.setBigid(bigId);
            if (friendsService.save(friends)) {
                return ReturnModel.success().setMessage("好友添加成功。");
            }
            else {
                //这里缺少一点回退notice的操作
                return ReturnModel.fail().setMessage("好友添加失败。");
            }
        }else{
            return ReturnModel.fail();
        }
    }
    @GetMapping("/list")
    public ReturnModel getUserAllFriends(@RequestAttribute(GatewayConsistent.USER_ID) Long userId){
        List<Friends> list = friendsService.lambdaQuery().eq(Friends::getSmallid, userId).or().eq(Friends::getBigid, userId).list();
        // 查找到对应的用户信息，然后返回给用户
        List<Long> collect = list.stream().map(friends -> {
            if (friends.getBigid() == userId) {
                return friends.getSmallid();
            } else {
                return friends.getBigid();
            }
        }).collect(Collectors.toList());
        if (collect.isEmpty()) {
            return ReturnModel.success().setData(List.of());
        }
        List<UserPO> resultList = userService.lambdaQuery().in(UserPO::getUserId, collect).list();
        return ReturnModel.success().setData(resultList);
    }
    @GetMapping("/delete/{friendId}")
    public ReturnModel deleteFriend(@RequestAttribute(GatewayConsistent.USER_ID) Long userId,@PathVariable("friendId") Long friendId){
       Long bigId = userId > friendId?userId:friendId;
       Long smallId = userId < friendId?userId:friendId;
       try {
           Friends friends = friendsService.lambdaQuery().eq(Friends::getBigid, bigId).eq(Friends::getSmallid, smallId).list().get(0);
           friendsService.removeById(friends.getId());
       }catch (Exception e){
           return ReturnModel.fail().setMessage("好友不存在.");
       }
       return ReturnModel.success().setMessage("删除成功.");
    }
}
