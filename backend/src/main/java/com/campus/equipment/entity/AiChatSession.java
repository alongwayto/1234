package com.campus.equipment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI聊天会话表
 */
@Data
@TableName("ai_chat_session")
public class AiChatSession {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 会话标题（首条消息摘要） */
    private String title;

    /** 会话类型：device-设备咨询 fault-故障咨询 general-通用问答 */
    private String sessionType;

    /** 关联设备ID（如有） */
    private Long deviceId;

    /** 关联故障ID（如有） */
    private Long faultId;

    /** 消息数量 */
    private Integer messageCount;

    /** 最后活跃时间 */
    private LocalDateTime lastActiveTime;

    /** 逻辑删除：0正常 1删除 */
    @TableLogic
    private Integer deleted;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
