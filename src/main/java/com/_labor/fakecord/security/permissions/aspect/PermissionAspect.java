package com._labor.fakecord.security.permissions.aspect;

import java.lang.reflect.Method;
import java.util.UUID;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com._labor.fakecord.domain.enums.ServerRolePermissions;
import com._labor.fakecord.security.permissions.RequirePermission;
import com._labor.fakecord.services.PermissionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class PermissionAspect {
  
  private final PermissionService service;
  private final ExpressionParser parser = new SpelExpressionParser();

  @Before("@annotation(requirePermission)")
  public void checkPermission(JoinPoint joinPoint, RequirePermission requirePermission) {
    UUID userId = getCurrentUserId();

    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();

    EvaluationContext context = new StandardEvaluationContext();
    Object[] args = joinPoint.getArgs();
    String[] paramsName = signature.getParameterNames();

    if (paramsName != null) {
      for (int i = 0; i < paramsName.length; i++) {
        context.setVariable(paramsName[i], args[i]);
      }
    }

    Long serverId = parseSpelLong(requirePermission.serverId(), context, method);

    String channelIdStr = requirePermission.channelId();
    Long channelId = channelIdStr.isBlank() ? null : parseSpelLong(channelIdStr, context, method);

    ServerRolePermissions requiredPermission = requirePermission.value();
    if (channelId != null) {
      service.requestChannelPermission(userId, serverId, channelId, requiredPermission);
    } else {
      service.requirePermission(userId, serverId, requiredPermission);
    }
  }

  private UUID getCurrentUserId() {
    Authentication  auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
      throw new AccessDeniedException("User is not authenticated");
    }
    try {
      return UUID.fromString(auth.getName());
    } catch (IllegalArgumentException e) {
      throw new AccessDeniedException("Invalid user ID in security context");
    }
  }

  private Long parseSpelLong(String expressionStr, EvaluationContext context, Method method) {
    try {
      return parser.parseExpression(expressionStr).getValue(context, Long.class);
    } catch (Exception e) {
      log.error("SpEL parsing error for expression '{}' in method '{}'", expressionStr, method.getName(), e);
      return null;
    }
  }

}
