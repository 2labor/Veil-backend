package com._labor.fakecord.domain.mappper;

import java.util.List;

import com._labor.fakecord.domain.dto.AttachmentDto;
import com._labor.fakecord.domain.entity.Attachment;

public interface AttachmentMapper {
  AttachmentDto toDto(Attachment entity);
  List<AttachmentDto> toListDto(List<Attachment> entities);
}
