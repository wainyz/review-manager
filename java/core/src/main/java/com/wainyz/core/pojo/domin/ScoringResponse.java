package com.wainyz.core.pojo.domin;

import lombok.Data;

/**
 * {
 *   "questions": [
 *       "score": 100,
 *       "score": 0,
 *       "score": 100,
 *       "score": 100,
 *       "score": 0
 *   ],
 *   "advice": "改进建议：1. 注意方向键 hjkl 对应左下上右，l 是右方向键而非 e\n2. 严格区分大小写命令：F 是反向查找，f 是正向查找\n3. 理解 A 本质是 shift+a 的快捷操作",
 *   "similar": "相关知识：\n1. hjkl 对应 ←↓↑→ 方向移动\n2. 大写命令本质是 shift+小写(如 A=shift+a)\n3. f/F 是行内查找命令，需配合具体字符使用(如 fa 查找下一个a)\n4. gg/G 用于文档级跳转，0/$ 用于行级跳转\n5. vim 所有操作区分大小写，且支持数字前缀(如 3j 下移3行)"
 * }
 * @author Yanion_gwgzh
 */
@Data
public class ScoringResponse {
    private String advice;
    private String similar;
    private int[] scores;

}
