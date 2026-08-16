package com._labor.fakecord.security.permissions;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com._labor.fakecord.domain.enums.ServerRolePermissions;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {
  ServerRolePermissions value();
  String serverId() default "#serverId";
  String channelId() default "";
}
