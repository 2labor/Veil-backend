package com._labor.fakecord.infrastructure.id.Impl;

import org.springframework.stereotype.Component;

import com._labor.fakecord.infrastructure.id.IdGenerator;
import com.github.f4b6a3.tsid.TsidCreator;

@Component
public class TsidGenerator implements IdGenerator{

  @Override
  public Long nextId() {
    return TsidCreator.getTsid().toLong();
  }
  
}
