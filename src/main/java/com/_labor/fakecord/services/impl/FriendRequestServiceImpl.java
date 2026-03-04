package com._labor.fakecord.services.impl;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com._labor.fakecord.domain.dto.UserProfileShort;
import com._labor.fakecord.domain.enums.RequestSource;
import com._labor.fakecord.repository.FriendRequestRepository;
import com._labor.fakecord.services.FriendRequestService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class FriendRequestServiceImpl implements FriendRequestService {

  private final FriendRequestRepository repository;

  @Override
  public void sendFriendRequest(UUID senderId, UUID targetId, RequestSource source) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'sendFriendRequest'");
  }

  @Override
  public void acceptFriendRequest(UUID currentUser, UUID requesterId) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'acceptFriendRequest'");
  }

  @Override
  public void declineOrCancelRequest(UUID senderId, UUID targetId) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'declineOrCancelRequest'");
  }

  @Override
  public void ignoreRequest(UUID currentUserId, UUID requesterId) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'ignoreRequest'");
  }

  @Override
  public Slice<UserProfileShort> getIncomingRequests(UUID userId, Pageable pageable) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getIncomingRequests'");
  }

  @Override
  public Slice<UserProfileShort> getOutgoingRequests(UUID userId, Pageable pageable) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getOutgoingRequests'");
  }
  
}
