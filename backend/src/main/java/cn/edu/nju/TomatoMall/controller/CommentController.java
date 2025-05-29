package cn.edu.nju.TomatoMall.controller;

import cn.edu.nju.TomatoMall.models.dto.comment.CommentDTO;
import cn.edu.nju.TomatoMall.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    /**
     * 创建新评论
     * @param commentDTO 评论信息
     * @return 创建的评论
     */
    @PostMapping
    public ResponseEntity<CommentDTO> createComment(@Valid @RequestBody CommentDTO commentDTO) {
        return ResponseEntity.ok(commentService.createComment(commentDTO));
    }

    /**
     * 更新评论
     * @param commentId 评论ID
     * @param commentDTO 更新的评论信息
     * @return 更新后的评论
     */
    @PutMapping("/{commentId}")
    public ResponseEntity<CommentDTO> updateComment(
            @PathVariable int commentId,
            @Valid @RequestBody CommentDTO commentDTO) {
        return ResponseEntity.ok(commentService.updateComment(commentId, commentDTO));
    }

    /**
     * 获取商品评论（分页）
     * @param itemId 商品ID
     * @param pageable 分页参数
     * @return 评论列表
     */
    @GetMapping("/item/{itemId}")
    public ResponseEntity<Page<CommentDTO>> getItemComments(
            @PathVariable int itemId,
            Pageable pageable) {
        return ResponseEntity.ok(commentService.getItemCommentsPaged(itemId, pageable));
    }

    /**
     * 获取商店评论（分页）
     * @param shopId 商店ID
     * @param pageable 分页参数
     * @return 评论列表
     */
    @GetMapping("/shop/{shopId}")
    public ResponseEntity<Page<CommentDTO>> getShopComments(
            @PathVariable int shopId,
            Pageable pageable) {
        return ResponseEntity.ok(commentService.getShopCommentsPaged(shopId, pageable));
    }

    /**
     * 点赞评论
     * @param commentId 评论ID
     * @return 更新后的评论
     */
    @PostMapping("/{commentId}/like")
    public ResponseEntity<CommentDTO> likeComment(@PathVariable int commentId) {
        return ResponseEntity.ok(commentService.likeComment(commentId));
    }

    /**
     * 回复评论
     * @param parentId 父评论ID
     * @param replyDTO 回复内容
     * @return 创建的回复
     */
    @PostMapping("/{parentId}/reply")
    public ResponseEntity<CommentDTO> replyToComment(
            @PathVariable int parentId,
            @Valid @RequestBody CommentDTO replyDTO) {
        return ResponseEntity.ok(commentService.replyToComment(parentId, replyDTO));
    }

    /**
     * 删除评论
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return 无内容
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable int commentId,
            @RequestParam int userId) {
        commentService.deleteComment(commentId, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * 获取用户评论
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 评论列表
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<CommentDTO>> getUserComments(
            @PathVariable int userId,
            Pageable pageable) {
        return ResponseEntity.ok(commentService.getUserCommentsPaged(userId, pageable));
    }

    /**
     * 获取评论的回复列表
     * @param commentId 评论ID
     * @return 回复列表
     */
    @GetMapping("/{commentId}/replies")
    public ResponseEntity<List<CommentDTO>> getReplies(@PathVariable int commentId) {
        return ResponseEntity.ok(commentService.getReplies(commentId));
    }

    /**
     * 获取商品的平均评分
     * @param itemId 商品ID
     * @return 平均评分
     */
    @GetMapping("/item/{itemId}/rating")
    public ResponseEntity<Double> getItemAverageRating(@PathVariable int itemId) {
        return ResponseEntity.ok(commentService.getItemAverageRating(itemId));
    }

    /**
     * 获取商店的平均评分
     * @param shopId 商店ID
     * @return 平均评分
     */
    @GetMapping("/shop/{shopId}/rating")
    public ResponseEntity<Double> getShopAverageRating(@PathVariable int shopId) {
        return ResponseEntity.ok(commentService.getShopAverageRating(shopId));
    }
} 