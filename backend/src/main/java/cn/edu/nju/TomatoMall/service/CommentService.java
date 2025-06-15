package cn.edu.nju.TomatoMall.service;

import cn.edu.nju.TomatoMall.enums.EntityType;
import cn.edu.nju.TomatoMall.models.dto.comment.*;
import org.springframework.data.domain.Page;

/**
 * 评论服务接口
 */
public interface CommentService {

    /** 添加评论
     * @param entityType 实体类型
     * @param entityId 实体ID
     * @param content 评论内容
     * @param rating 评分（可选）
     */
    void comment(EntityType entityType, Integer entityId, String content, Integer rating);

    /**
     * 添加回复
     * @param parentId 父评论ID
     * @param content 回复内容
     */
    void reply(int parentId, String content);

    /**
     * 更新评论/回复
     * @param commentId 评论ID
     * @param content 新的内容（可选）
     * @param rating 新的评分（可选）
     */
    void update(int commentId, String content, Integer rating);

    /**
     * 删除评论/回复
     * @param commentId 评论ID
     */
    void delete(int commentId);

    /** 获取评论列表
     * @param entityType 实体类型
     * @param entityId 实体ID
     * @param page 页码
     * @param size 每页大小
     * @param field 排序字段
     * @param order 排序方式（true为升序，false为降序）
     * @return 评论分页信息
     */
    Page<CommentResponse> getComments(EntityType entityType, int entityId,
                                      int page, int size, String field, boolean order);

    /**
     * 获取回复列表
     * @param parentId 父评论ID
     * @param page 页码
     * @param size 每页大小
     * @param field 排序字段
     * @param order 排序方式（true为升序，false为降序）
     * @return 回复分页信息
     */
    Page<CommentResponse> getReplies(int parentId,
                                     int page, int size, String field, boolean order);

    /**
     * 点赞/取消点赞
     * @param commentId 评论ID
     */
    void toggleLike(int commentId);
}