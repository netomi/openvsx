package org.eclipse.openvsx.storage.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.eclipse.openvsx.entities.FileResource;
import org.eclipse.openvsx.entities.Namespace;
import org.eclipse.openvsx.storage.IStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.net.URI;

@Aspect
@Component
@ConditionalOnExpression("!'${ovsx.storage.cdn.prefix-url}'.isEmpty()")
public class CdnStorageServiceAspect {

    @Value("${ovsx.storage.cdn.prefix-url:}")
    String cdnPrefixUrl;

    @Around("execution(* org.eclipse.openvsx.storage.*StorageService.getLocation(..))")
    public Object getLocation(ProceedingJoinPoint joinPoint) throws Throwable {
        var storageService = (IStorageService) joinPoint.getTarget();
        var fileResource = (FileResource) joinPoint.getArgs()[0];
        return URI.create(cdnPrefixUrl + "/" + storageService.getObjectKey(fileResource));
    }

    @Around("execution(* org.eclipse.openvsx.storage.*StorageService.getNamespaceLogoLocation(..))")
    public Object getNamespaceLogoLocation(ProceedingJoinPoint joinPoint) {
        var storageService = (IStorageService) joinPoint.getTarget();
        var namespace = (Namespace) joinPoint.getArgs()[0];
        return URI.create(cdnPrefixUrl + "/" + storageService.getObjectKey(namespace));
    }
}