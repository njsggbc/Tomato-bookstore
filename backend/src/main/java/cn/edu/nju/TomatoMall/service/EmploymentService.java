package cn.edu.nju.TomatoMall.service;

import cn.edu.nju.TomatoMall.models.dto.employment.TokenInfoResponse;
import cn.edu.nju.TomatoMall.models.dto.user.UserBriefResponse;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 雇佣关系管理服务接口
 */
public interface EmploymentService {
    /**
     * 获取商店的token列表
     * @param storeId 商店ID
     * @return token信息列表
     */
    List<TokenInfoResponse> getTokenList(int storeId);

    /**
     * 生成商店员工授权token
     * @param storeId 商店ID
     * @param name token名称
     * @param expiration 过期时间
     * @return 生成的token字符串
     */
    String generateToken(int storeId, String name, LocalDateTime expiration);

    /**
     * 删除商店员工授权token
     * @param storeId 商店ID
     * @param tokenId token ID
     */
    void deleteToken(int storeId, int tokenId);

    /**
     * 验证商店员工授权token
     * @param storeId 商店ID
     * @param token token字符串
     */
    void authToken(int storeId, String token);

    /**
     * 解雇商店员工
     * @param storeId 商店ID
     * @param userId 用户ID
     * @param reason 解雇原因
     */
    void dismiss(int storeId, int userId, String reason);

    /**
     * 获取商店员工列表
     * @param storeId 商店ID
     * @return 员工简要信息列表
     */
    List<UserBriefResponse> getStaffList(int storeId);
}