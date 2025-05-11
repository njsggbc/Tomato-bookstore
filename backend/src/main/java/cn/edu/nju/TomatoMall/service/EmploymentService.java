package cn.edu.nju.TomatoMall.service;

import cn.edu.nju.TomatoMall.models.dto.employment.TokenGenerateRequest;
import cn.edu.nju.TomatoMall.models.dto.employment.TokenInfoResponse;
import cn.edu.nju.TomatoMall.models.dto.user.UserBriefResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface EmploymentService {
    List<TokenInfoResponse> getTokenList(int storeId);

    String generateToken(int storeId, String name, LocalDateTime expiration);

    void deleteToken(int storeId, int tokenId);

    void authToken(int storeId, String token);

    void dismiss(int storeId, int userId);

    List<UserBriefResponse> getStaffList(int storeId);
}
