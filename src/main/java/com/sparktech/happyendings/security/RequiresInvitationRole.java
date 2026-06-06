package com.sparktech.happyendings.security;

import com.sparktech.happyendings.model.enums.InvitationRole;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresInvitationRole {
    InvitationRole[] value();
}
