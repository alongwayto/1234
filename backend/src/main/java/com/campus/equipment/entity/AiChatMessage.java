package com.campus.equipment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI聊天消息表
 */
@Data
@TableName("ai_chat_message")
public class AiChatMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 会话ID */
    private Long sessionId;

    /** 消息角色：user-用户 assistant-助手 system-系统 */
    private String role;

    /** 消息内容 */
    private String content;

    /** 消息类型：text-文本 image-图片 card-卡片 */
    private String messageType;

    /** 关联数据（如设备ID、故障ID等）JSON */
    private String metadata;

    /** 耗时（毫秒） */
    private Long costTime;

    /** 逻辑删除：0正常 1删除 */
    private Integer deleted;

    /** 创建时间 */
    private LocalDateTime createTime;
}
