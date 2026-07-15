// package com._labor.fakecord.utils;

// import java.util.ArrayList;
// import java.util.List;

// import com._labor.fakecord.domain.dto.ChannelResponseDto;
// import com._labor.fakecord.domain.entity.Channel;
// import com._labor.fakecord.domain.enums.ChannelType;
// import com._labor.fakecord.domain.mappper.Impl.ChannelMapperImpl;

// public final class ChannelTreeBuilder {

//   private ChannelTreeBuilder() {}

//   public static List<ChannelResponseDto> buildTree(List<Channel> allChannels) {
//     List<Channel> categories = allChannels.stream()
//       .filter(channel -> channel.getType() == ChannelType.GUILD_CATEGORY)
//       .toList();

//     List<Channel> channels = allChannels.stream() 
//       .filter(channel -> channel.getType() != ChannelType.GUILD_CATEGORY)
//       .toList();
    
//     List<ChannelResponseDto> tree = new ArrayList<>();
//     for (Channel c : categories) {
//       ChannelResponseDto dto = ChannelMapperImpl.toResponseDto(c);
//       List<ChannelResponseDto> children = channels.stream()
//         .filter(channel -> channel.getParent() != null && channel.getParent().getId().equals(dto.id()))
//         .map(ChannelMapperImpl::toResponseDto)
//         .toList();

//       dto.children().addAll(children);
//       tree.add(dto);
//     }

//     List<ChannelResponseDto> rootChannels = channels.stream()
//       .filter(channel -> channel.getParent() == null)
//       .map(ChannelMapperImpl::toResponseDto)
//       .toList();
    
//     tree.addAll(rootChannels);
//     return tree;
//   }
// }
