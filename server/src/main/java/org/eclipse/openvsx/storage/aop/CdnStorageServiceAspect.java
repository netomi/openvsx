/********************************************************************************
 * Copyright (c) 2025 Eclipse Foundation and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.openvsx.storage.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.eclipse.openvsx.entities.FileResource;
import org.eclipse.openvsx.entities.Namespace;
import org.eclipse.openvsx.storage.IStorageService;
import org.eclipse.openvsx.storage.LocalStorageService;
import org.eclipse.openvsx.util.UrlUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

/**
 * An aspect to rewrite the actual file location returned by a specific {@link IStorageService} instance.
 *
 * This allows to seamlessly use a CDN like cloudflare in front of the actual cloud storage in use.
 * File served via local storage will not be rewritten.
 */
@Aspect
@Component
@ConditionalOnExpression("!'${ovsx.storage.cdn.prefix-url}'.isEmpty()")
public class CdnStorageServiceAspect {

    @Value("${ovsx.storage.cdn.prefix-url:}")
    String cdnPrefixUrl;

    @Around("execution(* org.eclipse.openvsx.storage.*StorageService.getLocation(..))")
    public Object getLocation(ProceedingJoinPoint joinPoint) throws Throwable {
        var storageService = (IStorageService) joinPoint.getTarget();
        // Do not rewrite files located in the local storage.
        // The StorageUtilService class handles local storage separately, this is an additional safeguard.
        if (storageService instanceof LocalStorageService) {
            return joinPoint.proceed();
        } else {
            var fileResource = (FileResource) joinPoint.getArgs()[0];
            return UrlUtil.createURI(cdnPrefixUrl, storageService.getObjectKey(fileResource));
        }
    }

    @Around("execution(* org.eclipse.openvsx.storage.*StorageService.getNamespaceLogoLocation(..))")
    public Object getNamespaceLogoLocation(ProceedingJoinPoint joinPoint) throws Throwable {
        var storageService = (IStorageService) joinPoint.getTarget();
        // Do not rewrite logos located in the local storage.
        // The StorageUtilService class handles local storage separately, this is an additional safeguard.
        if (storageService instanceof LocalStorageService) {
            return joinPoint.proceed();
        } else {
            var namespace = (Namespace) joinPoint.getArgs()[0];
            return UrlUtil.createURI(cdnPrefixUrl, storageService.getObjectKey(namespace));
        }
    }
}