package com.wainyz.user.controller;

import com.wainyz.commons.pojo.vo.ReturnModel;
import com.wainyz.user.validate.ImageCodeCheck;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.wainyz.commons.config.UrlConfiguration.PUBLIC_PREFIX;

/**
 * @author Yanion_gwgzh
 */
@RestController
@RequestMapping(PUBLIC_PREFIX)
public class ImageController {
    @GetMapping("/image/{image_id}")
    public ResponseEntity<Resource> getImage(@PathVariable("image_id") String imageId) {
        // 从类路径加载图片（例如 src/main/resources/images/example.jpg）
        Resource image = new ClassPathResource("images/"+ imageId +".png");

        // 设置响应头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        return ResponseEntity.ok()
                .headers(headers)
                .body(image);
    }

    /**
     * 获取新的验证码图片
     * @param imageId
     * @return
     */
    @PostMapping("/image/flash")
    public ReturnModel flashImage(@RequestParam("image_id") String imageId) {
        String randomImageId = ImageCodeCheck.getRandomImageId(imageId);
        return ReturnModel.success().setData(randomImageId);
    }
}
