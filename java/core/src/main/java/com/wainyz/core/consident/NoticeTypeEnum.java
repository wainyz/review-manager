package com.wainyz.core.consident;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Yanion_gwgzh
 */

public enum NoticeTypeEnum {
    SYSTEM_ALL(0, null),
    ADD_FRIEND_APPLY(1, "applyUserId","applyUserName"),
    AGREE_FRIEND_APPLY(2, "applyUserId","agreeUserName"),
    WAITING_GENERATION(3, "title"),
    WAITING_SCORING(4,null),
    /**
     * waitingType中 0表示复习题生成id就是文档id，1表示paper生成id就是paperId，2表示打分生成id就是examId，3表示的就是复习打分，id就是fileId,
     */
    OVER_WAITING(5,"title","waitingType","deleteNoticeId"),
    ADD_CLASS_APPLY(6, "applyUserId","applyGoalClassId","description","userName","className"),
    AGREE_CLASS_APPLY(7, "applyGoalClassId","className"),
    REJECT_CLASS_APPLY(8, "rejectClass","className"),
    REJECT_FRIEND_APPLY(9, "applyUserId","rejectUserName"),
    KICK_OUT_CLASS(10, "applyUserId","applyGoalClassId"),
    CLASS_UPDATE(11, "classId","tipInfo","className"),

    ClassMessage(30,"classId","className","senderId","content"),
    FriendMessage(61,"senderId"),
    SYSTEM_ONE(-1, null);
    @JsonProperty
    public final int value;
    public final String[] otherInfo;
    NoticeTypeEnum(int typeValue, String... otherInfo){
        this.value = typeValue;
        this.otherInfo = otherInfo;
    }
    public static final String splitChar = ",";
    public String stringify(String... params){
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < otherInfo.length; i++) {
            //builder.append(otherInfo[i]).append(linkChar).append(params[i]).append(splitChar);
            builder.append(params[i]).append(splitChar);
        }
        builder.delete(builder.length()-1,builder.length());
        return builder.toString();
    }
    public Map<String,String> parser(String content){
        String[] split = content.split(splitChar);
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < otherInfo.length; i++) {
            map.put(otherInfo[i], split[i]);
        }
        return map;
    }
}
