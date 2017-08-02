package org.openmrs.module.registrationcore.api.db.hibernate.util;

import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;

public class HibernateUtil {

    public static <T>  T deproxy (T obj) {
        if (obj != null && isHibernateProxy(obj)) {
            // Unwrapping Proxy
            HibernateProxy proxy = (HibernateProxy) obj;
            LazyInitializer li = proxy.getHibernateLazyInitializer();
            return (T)  li.getImplementation();
        }
        return obj;
    }


    public static boolean isHibernateProxy(Object obj) {
        return obj instanceof HibernateProxy;
    }
}
