package com.campus.equipment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.equipment.entity.AiChatMessage;
import com.campus.equipment.entity.AiChatSession;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI聊天会话 Mapper
 */
@Mapper
public interface AiChatSessionMapper extends BaseMapper<AiChatSession> {
}

/**
 * AI聊天消息 Mapper
 */
@Mapper
public interface AiChatMessageMapper extends BaseMapper<AiChatMessage> {
}
