package com.wainyz.core.controller;

import com.wainyz.commons.pojo.vo.ReturnModel;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class SearchControllerTests {
    public static final Logger logger = LoggerFactory.getLogger(SearchControllerTests.class);
    /**
     * 测试 search file by title controller方法
     */
    @Autowired
    private SearchController searchController;

    @Test
    public void testSearchFileByTitleController(){
        // test data
        String titleKey = "vim";
        // test
        ReturnModel returnModel = searchController.searchFileByTitle(titleKey);
        logger.info(returnModel.toString());
        //
        assert returnModel.getData() != null;
    }

}
