package com.wainyz.user.validate;

import lombok.NonNull;

import java.util.*;

/**
 * @author Yanion_gwgzh
 */
public class ImageCodeCheck {
    private static final HashMap<String,String> map = new HashMap<>();
    private static final List<String> imageIdList = new ArrayList<>();
    static{
        map.put("185033", "1f68");
        map.put("185121", "67ad");
        map.put("185156", "e290");
        map.put("185244", "3zy4");
        map.put("185336", "5epl");
        map.forEach((key, value) -> imageIdList.add(key));
    }

    /**
     * 为了不重复使用图片id，每次获取图片id时，先判断是否与上一次获取的图片id相同，如果相同，则获取下一个图片id
     * @param oldImage
     * @return
     */
    public static String getRandomImageId(String oldImage){
        Random random = new Random();
        int i = random.nextInt(0,map.size());
        String s = imageIdList.get(i);
        if(s.equals(oldImage)){
            return imageIdList.get((i+1)%map.size());
        }else{
            return s;
        }
    }
    public static boolean checkImageCode(@NonNull String imageId,@NonNull String imageCode){
        return imageCode.toLowerCase().equals(map.get(imageId));
    }
}
