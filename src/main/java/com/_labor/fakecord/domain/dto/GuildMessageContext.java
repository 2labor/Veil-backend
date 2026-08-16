package com._labor.fakecord.domain.dto;

import java.util.UUID;
import com._labor.fakecord.domain.enums.ChannelType;

import lombok.Builder;

@Builder
public record GuildMessageContext(
        Long serverId,
        ChannelType channelType,
        String channelName
) implements MessageContext {

    @Override
    public ChannelType getChannelType() {
        return channelType;
    }

    @Override
    public UUID getRecipientId() {
        return null; 
    }

    @Override
    public Long getServiceId() {
        return serverId;
    }

    @Override
    public String getChannelName() {
        return channelName;
    }
}