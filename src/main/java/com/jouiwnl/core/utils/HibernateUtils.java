package com.jouiwnl.core.utils;

import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;

public final class HibernateUtils {

    private HibernateUtils() {
    }

    public static <T> T unproxy(T entity) {
        Hibernate.initialize(entity);
        if (entity instanceof HibernateProxy) {
            return (T) ((HibernateProxy) entity).getHibernateLazyInitializer().getImplementation();
        }
        return entity;
    }
}
