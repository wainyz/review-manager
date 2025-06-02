package com.wainyz.core.controller;

import com.wainyz.commons.pojo.vo.ReturnModel;
import com.wainyz.core.pojo.po.UserFilePO;
import com.wainyz.core.service.UserFileService;
import com.wainyz.user.pojo.po.UserPO;
import com.wainyz.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static com.wainyz.core.controller.SearchController.URL_MAPPING;


/**
 *
 * @author Yanion_gwgzh
 */
@RestController
@RequestMapping(URL_MAPPING)
public class SearchController {
    // --------------0-------------
    /**
     * 配置controller mapping url
     */
    public static final String URL_MAPPING = "search";
    @Autowired
    private UserFileService userFileService;
    @Autowired
    private UserService userService;

    // ---------------1----------------
    @GetMapping("/file")
    public ReturnModel searchFileByTitle(@RequestParam("key") String titleKey){
        List<UserFilePO> userFilePOList = userFileService.searchFileByTitle(titleKey);
        List<String> resultList = new ArrayList<>(userFilePOList.size());
        userFilePOList.forEach(e->resultList.add(String.valueOf(e.getId())));
        return ReturnModel.success().setData(resultList);
    }
    @GetMapping("/user/{key}")
    public ReturnModel searchUser(@PathVariable("key") String key){
        List<UserPO> list = userService.lambdaQuery().like(UserPO::getUsername, "%" + key + "%").or().like(UserPO::getEmail, "%" + key + "%").list();
        return ReturnModel.success().setData(list);
    }
}
