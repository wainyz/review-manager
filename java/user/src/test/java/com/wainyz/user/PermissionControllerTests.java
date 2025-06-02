package com.wainyz.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wainyz.commons.pojo.vo.ReturnModel;
import com.wainyz.user.controller.PermissionApiController;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;

@SpringBootTest
@ActiveProfiles("test")
public class PermissionControllerTests {
    @Autowired
    PermissionApiController permissionApiController;
    private final Logger logger = LoggerFactory.getLogger(PermissionControllerTests.class);

    /**
     * 测试获取rabbitmq的队列中的数据。
     */
    @Test
    public void testGetRabbitMqInfo(){
        permissionApiController.init();
        logger.info("[system]开始测试：有权限操作。");
        ReturnModel returnModel = permissionApiController.showRabbitMqData(3L);
        assert returnModel.getCode() == 200;
        logger.info(returnModel.toString());
        logger.info("[system]开始测试：无权限请求.");
        ReturnModel returnModel1 = permissionApiController.showRabbitMqData(1L);
        assert returnModel1.getCode() != 200;
        logger.info(returnModel1.toString());
    }
    /**
     * 测试获取redis中的websocket 连接数量。
     */
    @Test
    public void testGetWebsocketCount(){
        permissionApiController.init();
        logger.info("[system]开始测试：有权限操作。");
        ReturnModel returnModel = permissionApiController.showWebsocket(3L);
        assert returnModel.getCode() == 200;
        logger.info(returnModel.toString());
        logger.info("[system]开始测试：无权限请求.");
        ReturnModel returnModel1 = permissionApiController.showWebsocket(1L);
        assert returnModel1.getCode() != 200;
        logger.info(returnModel1.toString());
    }
    /**
     * 测试获取封禁用户
     */
    @Test
    public void testGetBanUsers() throws JsonProcessingException {
        permissionApiController.init();
        logger.info("[system]开始测试：有权限操作。");
        ReturnModel returnModel = permissionApiController.showBanUsers(3L);
        assert returnModel.getCode() == 200;
        logger.info(returnModel.toString());
        logger.info("[system]开始测试：无权限请求.");
        ReturnModel returnModel1 = permissionApiController.showBanUsers(1L);
        assert returnModel1.getCode() != 200;
        logger.info(returnModel1.toString());
    }
    /**
     * 测试 解封userId=2，并再次封禁
     */
    @Test
    public void testLiftUserAndBanUser() throws JsonProcessingException {
        permissionApiController.init();
        logger.info("[system]开始测试：尝试解封。");
        logger.info("封禁名单如下：{}", permissionApiController.showBanUsers(3L));
        ReturnModel returnModel = permissionApiController.updateBanUser(3L, 2L, System.currentTimeMillis() - 1);
        logger.info("更新之后: {}",returnModel);

        logger.info("[system]开始测试：封禁用户.");
        returnModel = permissionApiController.banUser(3L,2L, LocalDate.of(3025,1,1).toEpochSecond(LocalTime.now(), ZoneOffset.UTC));
        logger.info("更新之后: {}",returnModel);
    }
    /**
     * 测试获取用户权限，更改用户权限，查看系统注册的权限
     *
     */
    @Test
    public void testUserPermission() throws JsonProcessingException {
        permissionApiController.init();
        logger.info("[system]开始测试：尝试获取用户permission以及系统注册的权限。");
        logger.info("userId=3用户权限：{}", permissionApiController.getUserPermission(3L));
        logger.info("系统注册的权限: {}",permissionApiController.getPermissionRegistry());
        logger.info("[system]开始测试：更改用户权限.");
        ReturnModel returnModel = permissionApiController.updateUserPermission(3L, 3L, (1 << 4) - 1);
        logger.info("更新之后userId=3用户权限: {}",returnModel);
        //恢复userId=3用户的最高权限
        permissionApiController.updateUserPermission(3L,3L,Integer.MAX_VALUE);
    }
}
